# 在庫システムから会計ランディングへの変換処理

from decimal import Decimal
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional
import logging

from src.common.config import JOURNAL_CATEGORY_MAPPING, ACCOUNT_MAPPING, ERROR_CODES
from src.common.utils import ETLUtils, DataQualityValidator

logger = logging.getLogger(__name__)


# 在庫データの変換処理クラス
class InventoryTransformer:

    def __init__(self, batch_id: str):
        self.batch_id = batch_id
        self.source_system = 'INV'
        self.utils = ETLUtils()
        self.validator = DataQualityValidator()

    # 単一の在庫移動レコードを会計ランディング形式に変換する
    # 在庫移動の種類によって複数の仕訳を生成する場合がある
    def transform_record(self, source_record: Dict[str, Any]) -> List[Dict[str, Any]]:
        try:
            staging_records = []
            
            # メイン仕訳を作成
            main_record = self._create_base_record(source_record)
            self._transform_inventory_specific_fields(source_record, main_record)
            self._calculate_amounts(source_record, main_record)
            
            # バリデーション
            validation_errors = self._validate_record(main_record)
            if validation_errors:
                main_record['status_code'] = 'ERROR'
                main_record['error_code'] = 'E_VALIDATION'
                main_record['error_message'] = '; '.join(validation_errors)
            else:
                main_record['status_code'] = 'READY'

            staging_records.append(main_record)

            # 差異仕訳を作成（標準原価差異など）
            # 片側記録方式：差異も借方のみ記録
            variance_record = self._create_variance_record(source_record, main_record)
            if variance_record:
                staging_records.append(variance_record)

            # 片側記録方式：対側仕訳はERP側で自動生成されるため作成しない
            # contra_recordは不要

            return staging_records

        except Exception as e:
            logger.error(f"Error transforming inventory record {source_record.get('export_id')}: {e}")
            return [self._create_error_record(source_record, 'E_TRANSFORM', str(e))]

    # 基本的なレコード構造を作成
    def _create_base_record(self, source_record: Dict[str, Any]) -> Dict[str, Any]:
        
        # 会計日付の決定
        movement_timestamp = source_record.get('movement_timestamp')
        if isinstance(movement_timestamp, str):
            accounting_date = datetime.strptime(movement_timestamp.split()[0], '%Y-%m-%d').date()
        else:
            accounting_date = movement_timestamp.date() if movement_timestamp else None

        return {
            # 識別/監査
            'batch_id': self.batch_id,
            'source_system': self.source_system,
            'source_doc_type': source_record.get('movement_type', ''),
            'source_doc_id': source_record.get('movement_id', ''),
            'source_line_id': source_record.get('movement_line_id', ''),
            'event_timestamp': source_record.get('movement_timestamp'),
            'load_timestamp': datetime.now(timezone.utc),
            'status_code': 'PROCESSING',
            'error_code': None,
            'error_message': None,

            # 会社/会計カレンダ/通貨
            'ledger_id': 'GL001',
            'legal_entity_id': 'COMP001',
            'business_unit': 'BU001',
            'company_code': 'COMP001',
            'accounting_date': accounting_date,
            'period_name': self.utils.format_period_name(accounting_date),
            'journal_category': self._get_journal_category(source_record.get('movement_type')),
            'currency_code': source_record.get('currency_code', 'JPY'),
            'exchange_rate_type': 'Corporate',
            'exchange_rate': self._get_exchange_rate(source_record),

            # セグメント
            'segment_account': self._get_account_segment(source_record),
            'segment_department': self._get_department_segment(source_record),
            'segment_product': self._normalize_item_code(source_record.get('item_code')),
            'segment_project': source_record.get('project_code'),
            'segment_interco': None,
            'segment_custom1': source_record.get('inventory_org'),
            'segment_custom2': source_record.get('subinventory_code'),

            # 取引先/サブレジャ
            'customer_id': None,
            'supplier_id': self._get_supplier_from_source_doc(source_record),
            'bank_account_id': None,
            'invoice_id': None,
            'invoice_line_id': None,
            'po_id': self._get_po_from_source_doc(source_record),
            'receipt_id': source_record.get('movement_id') if source_record.get('movement_type') == 'RCV' else None,
            'asset_id': None,
            'project_id': source_record.get('project_code'),

            # 金額/数量
            'quantity': self.utils.validate_amount(source_record.get('quantity')),
            'uom_code': source_record.get('uom_code'),

            # 税/収益認識
            'tax_code': None,
            'tax_rate': None,
            'tax_amount_entered': None,
            'revrec_rule_code': None,
            'revrec_start_date': None,
            'revrec_end_date': None,

            # 説明/参照
            'description': self._create_description(source_record),
            'reference1': source_record.get('source_doc_id'),
            'reference2': source_record.get('wip_job_id'),
            'reference3': source_record.get('lot_no'),
            'reference4': source_record.get('serial_no'),
            'reference5': source_record.get('reason_code'),

            # 取消/反転
            'reversal_flag': self._is_reversal_transaction(source_record),
            'reversed_interface_id': None,

            # 作成/更新
            'created_by': 'ETL_INV',
            'updated_by': 'ETL_INV',

            # 金額（片側記録方式：_calculate_amounts で設定）
            'entered_dr': Decimal('0'),
            'entered_cr': Decimal('0'),
            'accounted_dr': Decimal('0'),
            'accounted_cr': Decimal('0')
        }

    # 在庫固有フィールドの変換
    def _transform_inventory_specific_fields(self, source_record: Dict[str, Any], target_record: Dict[str, Any]):
        
        movement_type = source_record.get('movement_type', '')
        
        # 移動種別に応じた勘定科目の調整
        if movement_type == 'RCV':
            # 入庫：在庫資産増加
            target_record['journal_category'] = 'Inventory'
        elif movement_type == 'ISS':
            # 出庫：売上原価計上
            target_record['journal_category'] = 'Cost'
        elif movement_type in ['ADJ', 'CNT']:
            # 調整・棚卸：在庫調整
            target_record['journal_category'] = 'Adjustment'
        elif movement_type == 'TRF':
            # 振替：在庫移動
            target_record['journal_category'] = 'Transfer'

    # 金額の計算処理（片側記録方式）
    def _calculate_amounts(self, source_record: Dict[str, Any], target_record: Dict[str, Any]):
        
        quantity = self.utils.validate_amount(source_record.get('quantity', 0))
        unit_cost = self._determine_unit_cost(source_record)
        exchange_rate = target_record.get('exchange_rate', Decimal('1.0'))
        movement_type = source_record.get('movement_type', '')

        # 移動金額の計算
        movement_amount = abs(quantity * unit_cost)

        # 移動種別による借方・貸方の決定（片側記録方式）
        # すべて借方のみ記録、対側はERP側で自動生成
        if movement_type == 'RCV':
            # 入庫：借方=在庫資産（対側：買掛金はERP側で自動生成）
            target_record['entered_dr'] = movement_amount
            target_record['entered_cr'] = Decimal('0')
            target_record['segment_account'] = '1300'  # 棚卸資産
        elif movement_type == 'ISS':
            # 出庫：借方=売上原価（対側：棚卸資産はERP側で自動生成）
            target_record['entered_dr'] = movement_amount
            target_record['entered_cr'] = Decimal('0')
            target_record['segment_account'] = '5100'  # 売上原価
        elif movement_type in ['ADJ', 'CNT']:
            # 調整：すべて借方記録
            if quantity >= 0:
                # 増加調整：借方=棚卸資産（対側：棚卸差異はERP側で自動生成）
                target_record['entered_dr'] = movement_amount
                target_record['entered_cr'] = Decimal('0')
                target_record['segment_account'] = '1300'  # 棚卸資産
            else:
                # 減少調整：借方=棚卸差異（対側：棚卸資産はERP側で自動生成）
                target_record['entered_dr'] = movement_amount
                target_record['entered_cr'] = Decimal('0')
                target_record['segment_account'] = '5200'  # 棚卸差異
        elif movement_type == 'TRF':
            # 振替：借方のみ記録（対側はERP側で自動生成）
            target_record['entered_dr'] = movement_amount
            target_record['entered_cr'] = Decimal('0')
        elif movement_type == 'CST':
            # 原価修正：借方=在庫資産（対側：棚卸差異はERP側で自動生成）
            # 原価修正は金額が入力されていないため、スキップまたは0処理
            # 実装ではvariance_amountが設定されている場合のみ処理
            if movement_amount > 0:
                target_record['entered_dr'] = movement_amount
                target_record['entered_cr'] = Decimal('0')
                target_record['segment_account'] = '1200'  # 在庫評価勘定
            else:
                # 金額がない場合は金額0で記録（ERRORとして処理される）
                target_record['entered_dr'] = Decimal('0')
                target_record['entered_cr'] = Decimal('0')
        else:
            # 未知の移動タイプ：金額0で記録（ERRORとして処理される）
            target_record['entered_dr'] = Decimal('0')
            target_record['entered_cr'] = Decimal('0')

        # 記帳通貨への換算
        target_record['accounted_dr'] = target_record['entered_dr'] * exchange_rate
        target_record['accounted_cr'] = target_record['entered_cr'] * exchange_rate

    # 単価の決定
    def _determine_unit_cost(self, source_record: Dict[str, Any]) -> Decimal:
        cost_method = source_record.get('cost_method', 'STD')
        
        if cost_method == 'STD':
            # 標準原価
            return self.utils.validate_amount(source_record.get('std_cost', 0))
        elif cost_method == 'AVG':
            # 平均原価
            return self.utils.validate_amount(source_record.get('avg_cost', 0))
        else:
            # その他は記録単価
            return self.utils.validate_amount(source_record.get('unit_cost', 0))

    # 差異仕訳の作成
    def _create_variance_record(self, source_record: Dict[str, Any], main_record: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        
        variance_amount = self.utils.validate_amount(source_record.get('variance_amount'))
        if not variance_amount or variance_amount == 0:
            return None

        variance_record = main_record.copy()
        variance_record.update({
            'source_line_id': f"{source_record.get('movement_line_id', '')}_VAR",
            'segment_account': '5200',  # 在庫差異
            'journal_category': 'Variance',
            'description': f"Inventory Variance - {main_record.get('description', '')}",
        })

        # 差異の借方・貸方
        if variance_amount > 0:
            # 不利差異
            variance_record['entered_dr'] = variance_amount
            variance_record['entered_cr'] = Decimal('0')
        else:
            # 有利差異
            variance_record['entered_dr'] = Decimal('0')
            variance_record['entered_cr'] = abs(variance_amount)

        # 記帳通貨への換算
        exchange_rate = main_record.get('exchange_rate', Decimal('1.0'))
        variance_record['accounted_dr'] = variance_record['entered_dr'] * exchange_rate
        variance_record['accounted_cr'] = variance_record['entered_cr'] * exchange_rate

        return variance_record

    # 対側仕訳の作成は不要（片側記録方式）
    # ERP側の自動仕訳ルールで対側を生成
    def _create_contra_record(self, source_record: Dict[str, Any], main_record: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        # 片側記録方式では対側仕訳を作成しない
        return None

    # ジャーナルカテゴリの取得
    def _get_journal_category(self, movement_type: str) -> str:
        return JOURNAL_CATEGORY_MAPPING.get('INV', {}).get(movement_type, 'Inventory')

    # 勘定科目セグメントの決定
    def _get_account_segment(self, source_record: Dict[str, Any]) -> str:
        movement_type = source_record.get('movement_type', '')
        cost_method = source_record.get('cost_method', 'STD')
        
        account_map = ACCOUNT_MAPPING.get('INV', {}).get(movement_type, {})
        return account_map.get('default', '1200')  # デフォルト在庫資産

    # 部門セグメントの決定
    def _get_department_segment(self, source_record: Dict[str, Any]) -> Optional[str]:
        # 在庫組織から部門へのマッピング
        inventory_org = source_record.get('inventory_org')
        if inventory_org:
            # サンプル：在庫組織コードを部門コードにマッピング
            org_dept_mapping = {
                'ORG001': 'DEPT_PROD',
                'ORG002': 'DEPT_SALES',
                'ORG003': 'DEPT_SERVICE'
            }
            return org_dept_mapping.get(inventory_org, f"DEPT_{inventory_org}")
        return None

    # 為替レートの取得
    def _get_exchange_rate(self, source_record: Dict[str, Any]) -> Decimal:
        if source_record.get('exchange_rate'):
            return Decimal(str(source_record['exchange_rate']))
        
        currency_code = source_record.get('currency_code', 'JPY')
        movement_timestamp = source_record.get('movement_timestamp')
        
        return self.utils.calculate_exchange_rate(
            Decimal('1.0'), currency_code, 'JPY', movement_timestamp
        )

    # 品目コードの正規化
    def _normalize_item_code(self, item_code: str) -> Optional[str]:
        if not item_code:
            return None
        # 品目マスタとの連携（実装では外部マスタ参照）
        return item_code.strip().upper()

    # ソースドキュメントからサプライヤIDを取得
    def _get_supplier_from_source_doc(self, source_record: Dict[str, Any]) -> Optional[str]:
        source_doc_type = source_record.get('source_doc_type')
        if source_doc_type == 'PO':
            # 発注書の場合、発注先を取得（実装では外部マスタ参照）
            return f"SUPP_{source_record.get('source_doc_id', '')[:10]}"
        return None

    # ソースドキュメントから発注書IDを取得
    def _get_po_from_source_doc(self, source_record: Dict[str, Any]) -> Optional[str]:
        source_doc_type = source_record.get('source_doc_type')
        if source_doc_type == 'PO':
            return source_record.get('source_doc_id')
        return None

    # 取消トランザクションかどうかの判定
    def _is_reversal_transaction(self, source_record: Dict[str, Any]) -> bool:
        reason_code = source_record.get('reason_code', '')
        return 'REVERSAL' in reason_code.upper() or 'CANCEL' in reason_code.upper()

    # 説明文の作成
    def _create_description(self, source_record: Dict[str, Any]) -> str:
        parts = []
        
        movement_type = source_record.get('movement_type', '')
        item_description = source_record.get('item_description', '')
        location_code = source_record.get('location_code', '')
        
        # 移動種別の日本語説明
        movement_desc = {
            'RCV': '入庫',
            'ISS': '出庫',
            'TRF': '振替',
            'ADJ': '調整',
            'CNT': '棚卸',
            'CST': '原価修正'
        }.get(movement_type, movement_type)
        
        parts.append(movement_desc)
        
        if item_description:
            parts.append(item_description[:50])  # 長い場合は短縮
        
        if location_code:
            parts.append(f"Location: {location_code}")

        description = ' | '.join(parts)
        return self.utils.clean_string(description, 500)

    # レコードの妥当性チェック
    def _validate_record(self, target_record: Dict[str, Any]) -> List[str]:
        errors = []

        # 必須フィールドチェック
        mandatory_fields = [
            'source_doc_type', 'source_doc_id', 'source_line_id',
            'accounting_date', 'segment_account', 'quantity'
        ]
        errors.extend(self.validator.validate_mandatory_fields(target_record, mandatory_fields))

        # 数量チェック
        quantity = target_record.get('quantity', Decimal('0'))
        if quantity == 0:
            errors.append("Quantity cannot be zero")

        # 金額チェック
        entered_dr = target_record.get('entered_dr', Decimal('0'))
        entered_cr = target_record.get('entered_cr', Decimal('0'))
        
        if entered_dr == 0 and entered_cr == 0:
            errors.append("Both debit and credit amounts are zero")

        return errors

    # エラーレコードの作成
    def _create_error_record(self, source_record: Dict[str, Any], error_code: str, 
                           error_message: str) -> Dict[str, Any]:
        return {
            'batch_id': self.batch_id,
            'source_system': self.source_system,
            'source_doc_type': source_record.get('movement_type', ''),
            'source_doc_id': source_record.get('movement_id', ''),
            'source_line_id': source_record.get('movement_line_id', ''),
            'event_timestamp': source_record.get('movement_timestamp'),
            'status_code': 'ERROR',
            'error_code': error_code,
            'error_message': self.utils.get_error_message(error_code, error_message),
            'load_timestamp': datetime.now(timezone.utc),
            'created_by': 'ETL_INV',
            'updated_by': 'ETL_INV'
        }
