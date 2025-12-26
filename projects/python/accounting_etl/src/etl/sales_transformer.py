# 売上システムから会計ランディングへの変換処理

from decimal import Decimal
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional
import logging

from src.common.config import JOURNAL_CATEGORY_MAPPING, ACCOUNT_MAPPING, ERROR_CODES
from src.common.utils import ETLUtils, DataQualityValidator

logger = logging.getLogger(__name__)


# 売上データの変換処理クラス
class SalesTransformer:

    def __init__(self, batch_id: str):
        self.batch_id = batch_id
        self.source_system = 'SALE'
        self.utils = ETLUtils()
        self.validator = DataQualityValidator()

    # 単一の売上レコードを会計ランディング形式に変換する
    def transform_record(self, source_record: Dict[str, Any]) -> Dict[str, Any]:
        try:
            # 基本的な変換処理
            target_record = self._create_base_record(source_record)
            
            # 売上固有の変換処理
            self._transform_sales_specific_fields(source_record, target_record)
            
            # 金額計算
            self._calculate_amounts(source_record, target_record)
            
            # データ品質チェック
            validation_errors = self._validate_record(target_record)
            if validation_errors:
                target_record['status_code'] = 'ERROR'
                target_record['error_code'] = 'E_VALIDATION'
                target_record['error_message'] = '; '.join(validation_errors)
            else:
                target_record['status_code'] = 'READY'

            return target_record

        except Exception as e:
            logger.error(f"Error transforming sales record {source_record.get('export_id')}: {e}")
            return self._create_error_record(source_record, 'E_TRANSFORM', str(e))

    # 基本的なレコード構造を作成
    def _create_base_record(self, source_record: Dict[str, Any]) -> Dict[str, Any]:
        # 会計日付の決定（請求日 > イベント日）
        accounting_date = source_record.get('invoice_date') or source_record.get('event_timestamp')
        if isinstance(accounting_date, str):
            accounting_date = datetime.strptime(accounting_date.split()[0], '%Y-%m-%d').date()

        return {
            # 識別/監査
            'batch_id': self.batch_id,
            'source_system': self.source_system,
            'source_doc_type': source_record.get('txn_type', ''),
            'source_doc_id': source_record.get('source_txn_id', ''),
            'source_line_id': source_record.get('source_line_id', ''),
            'event_timestamp': source_record.get('event_timestamp'),
            'load_timestamp': datetime.now(timezone.utc),
            'status_code': 'PROCESSING',
            'error_code': None,
            'error_message': None,

            # 会社/会計カレンダ/通貨
            'ledger_id': 'GL001',  # デフォルト総勘定元帳
            'legal_entity_id': 'COMP001',  # デフォルト法人
            'business_unit': 'BU001',  # デフォルト事業単位
            'company_code': 'COMP001',
            'accounting_date': accounting_date,
            'period_name': self.utils.format_period_name(accounting_date),
            'journal_category': self._get_journal_category(source_record.get('txn_type')),
            'currency_code': source_record.get('currency_code', 'JPY'),
            'exchange_rate_type': 'Corporate',
            'exchange_rate': self._get_exchange_rate(source_record),

            # セグメント
            'segment_account': self._get_account_segment(source_record),
            'segment_department': self._get_department_segment(source_record),
            'segment_product': self._normalize_product_code(source_record.get('product_code')),
            'segment_project': None,
            'segment_interco': None,
            'segment_custom1': source_record.get('campaign_code'),
            'segment_custom2': source_record.get('salesperson_code'),

            # 取引先/サブレジャ
            'customer_id': self._normalize_customer_id(source_record.get('customer_code')),
            'supplier_id': None,
            'bank_account_id': None,
            'invoice_id': source_record.get('invoice_id'),
            'invoice_line_id': source_record.get('source_line_id'),
            'po_id': None,
            'receipt_id': None,
            'asset_id': None,
            'project_id': None,

            # 数量
            'quantity': self.utils.validate_amount(source_record.get('quantity_shipped')),
            'uom_code': source_record.get('uom_code'),

            # 税/収益認識
            'tax_code': source_record.get('tax_code'),
            'tax_rate': self.utils.validate_amount(source_record.get('tax_rate')),
            'tax_amount_entered': self.utils.validate_amount(source_record.get('tax_amount')),
            'revrec_rule_code': source_record.get('revrec_rule_code'),
            'revrec_start_date': source_record.get('invoice_date'),
            'revrec_end_date': None,

            # 説明/参照
            'description': self._create_description(source_record),
            'reference1': source_record.get('order_id'),
            'reference2': source_record.get('invoice_id'),
            'reference3': source_record.get('shipment_id'),
            'reference4': source_record.get('reference1'),
            'reference5': source_record.get('reference2'),

            # 取消/反転
            'reversal_flag': source_record.get('return_flag', False) or source_record.get('cancel_flag', False),
            'reversed_interface_id': None,

            # 作成/更新
            'created_by': 'ETL_SALES',
            'updated_by': 'ETL_SALES'
        }

    # 売上固有フィールドの変換
    def _transform_sales_specific_fields(self, source_record: Dict[str, Any], target_record: Dict[str, Any]):
        # 取引種別に応じた追加設定
        txn_type = source_record.get('txn_type', '')
        
        if txn_type == 'INVOICE':
            # 売上計上
            target_record['journal_category'] = 'Sales'
        elif txn_type == 'PMT':
            # 入金処理
            target_record['journal_category'] = 'Payment'
            target_record['segment_account'] = '1100'  # 現金・預金
        elif txn_type in ['ORDER', 'SHIP']:
            # 受注・出荷（前受など）
            target_record['journal_category'] = 'Sales'

    # 金額の計算処理（片側記録方式）
    def _calculate_amounts(self, source_record: Dict[str, Any], target_record: Dict[str, Any]):
        net_amount = self.utils.validate_amount(source_record.get('net_amount', 0))
        tax_amount = self.utils.validate_amount(source_record.get('tax_amount', 0))
        currency_code = source_record.get('currency_code', 'JPY')
        exchange_rate = target_record.get('exchange_rate', Decimal('1.0'))

        # 取引種別による借方・貸方の決定（片側記録方式）
        txn_type = source_record.get('txn_type', '')
        
        if txn_type == 'INVOICE' or txn_type == 'SHIP':
            # 売上：貸方のみ記録（売上高）
            # 対側仕訳（借方：売掛金）はERP側で自動生成
            if source_record.get('return_flag'):
                # 返品の場合は借方（売上高マイナス）
                target_record['entered_dr'] = net_amount
                target_record['entered_cr'] = Decimal('0')
            else:
                # 通常の売上：貸方のみ
                target_record['entered_dr'] = Decimal('0')
                target_record['entered_cr'] = net_amount
        elif txn_type == 'PMT':
            # 入金：借方のみ記録（現金預金）
            # 対側仕訳（貸方：売掛金）はERP側で自動生成
            target_record['entered_dr'] = net_amount
            target_record['entered_cr'] = Decimal('0')
        else:
            # その他の場合はデフォルト（貸方）
            target_record['entered_dr'] = Decimal('0')
            target_record['entered_cr'] = abs(net_amount) if net_amount != 0 else Decimal('0')

        # 記帳通貨への換算
        target_record['accounted_dr'] = target_record['entered_dr'] * exchange_rate
        target_record['accounted_cr'] = target_record['entered_cr'] * exchange_rate

    # ジャーナルカテゴリの取得
    def _get_journal_category(self, txn_type: str) -> str:
        return JOURNAL_CATEGORY_MAPPING.get('SALE', {}).get(txn_type, 'Sales')

    # 勘定科目セグメントの決定
    def _get_account_segment(self, source_record: Dict[str, Any]) -> str:
        txn_type = source_record.get('txn_type', '')
        tax_code = source_record.get('tax_code', '')
        
        account_map = ACCOUNT_MAPPING.get('SALE', {}).get(txn_type, {})
        
        if tax_code and 'tax' in account_map:
            return account_map['tax']
        else:
            return account_map.get('default', '4100')  # デフォルト売上高

    # 部門セグメントの決定
    def _get_department_segment(self, source_record: Dict[str, Any]) -> Optional[str]:
        # 顧客コードから部門へのマッピング（実装では外部マスタ参照）
        customer_code = source_record.get('customer_code')
        if customer_code:
            # サンプル：顧客コードの先頭2桁を部門とする
            return f"DEPT_{customer_code[:2]}"
        return None

    # 為替レートの取得
    def _get_exchange_rate(self, source_record: Dict[str, Any]) -> Decimal:
        # ソースにレートがある場合はそれを使用
        if source_record.get('exchange_rate'):
            return Decimal(str(source_record['exchange_rate']))
        
        # ない場合は通貨コードから計算
        currency_code = source_record.get('currency_code', 'JPY')
        accounting_date = source_record.get('invoice_date') or source_record.get('event_timestamp')
        
        return self.utils.calculate_exchange_rate(
            Decimal('1.0'), currency_code, 'JPY', accounting_date
        )

    # 商品コードの正規化
    def _normalize_product_code(self, product_code: str) -> Optional[str]:
        if not product_code:
            return None
        # MDMとの連携（実装では外部マスタ参照）
        return product_code.strip().upper()

    # 顧客IDの正規化
    def _normalize_customer_id(self, customer_code: str) -> Optional[str]:
        if not customer_code:
            return None
        # 顧客マスタとの連携（実装では外部マスタ参照）
        return customer_code.strip().upper()

    # 説明文の作成
    def _create_description(self, source_record: Dict[str, Any]) -> str:
        parts = []
        
        if source_record.get('product_name'):
            parts.append(source_record['product_name'])
        
        if source_record.get('order_id'):
            parts.append(f"Order: {source_record['order_id']}")
        
        if source_record.get('customer_code'):
            parts.append(f"Customer: {source_record['customer_code']}")

        description = ' | '.join(parts)
        return self.utils.clean_string(description, 500)

    # レコードの妥当性チェック
    def _validate_record(self, target_record: Dict[str, Any]) -> List[str]:
        errors = []

        # 必須フィールドチェック
        mandatory_fields = [
            'source_doc_type', 'source_doc_id', 'source_line_id',
            'accounting_date', 'currency_code', 'segment_account'
        ]
        errors.extend(self.validator.validate_mandatory_fields(target_record, mandatory_fields))

        # 金額チェック
        entered_dr = target_record.get('entered_dr', Decimal('0'))
        entered_cr = target_record.get('entered_cr', Decimal('0'))
        
        if entered_dr == 0 and entered_cr == 0:
            errors.append("Both debit and credit amounts are zero")

        # 通貨コードチェック
        if not self.utils.validate_currency_code(target_record.get('currency_code', '')):
            errors.append("Invalid currency code")

        return errors

    # エラーレコードの作成
    def _create_error_record(self, source_record: Dict[str, Any], error_code: str, 
                           error_message: str) -> Dict[str, Any]:
        return {
            'batch_id': self.batch_id,
            'source_system': self.source_system,
            'source_doc_type': source_record.get('txn_type', ''),
            'source_doc_id': source_record.get('source_txn_id', ''),
            'source_line_id': source_record.get('source_line_id', ''),
            'event_timestamp': source_record.get('event_timestamp'),
            'status_code': 'ERROR',
            'error_code': error_code,
            'error_message': self.utils.get_error_message(error_code, error_message),
            'load_timestamp': datetime.now(timezone.utc),
            'created_by': 'ETL_SALES',
            'updated_by': 'ETL_SALES'
        }

    # 税金のための別仕訳を作成
    def create_tax_entry(self, source_record: Dict[str, Any], main_record: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        tax_amount = self.utils.validate_amount(source_record.get('tax_amount'))
        if not tax_amount or tax_amount == 0:
            return None

        tax_record = main_record.copy()
        tax_record.update({
            'source_line_id': f"{source_record.get('source_line_id', '')}_TAX",
            'segment_account': '2300',  # 売上税
            'entered_dr': Decimal('0'),
            'entered_cr': tax_amount,
            'accounted_dr': Decimal('0'),
            'accounted_cr': tax_amount * main_record.get('exchange_rate', Decimal('1.0')),
            'description': f"Sales Tax - {main_record.get('description', '')}",
            'tax_amount_entered': tax_amount
        })

        return tax_record
