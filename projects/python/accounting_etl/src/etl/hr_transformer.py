# 人事システムから会計ランディングへの変換処理

from decimal import Decimal
from datetime import datetime, timezone, date
from typing import Dict, Any, List, Optional
import logging

from src.common.config import JOURNAL_CATEGORY_MAPPING, ACCOUNT_MAPPING, ERROR_CODES
from src.common.utils import ETLUtils, DataQualityValidator

logger = logging.getLogger(__name__)


# 人事データの変換処理クラス
class HRTransformer:

    def __init__(self, batch_id: str):
        self.batch_id = batch_id
        self.source_system = 'HR'
        self.utils = ETLUtils()
        self.validator = DataQualityValidator()

    # 人事レコード + 給与データを会計ランディング形式に変換する
    # 給与項目ごとに複数のランディングレコードを生成
    def transform_payroll_record(self, hr_record: Dict[str, Any], payroll_data: Dict[str, Any]) -> List[Dict[str, Any]]:
        try:
            # 必須項目のバリデーション
            if not hr_record.get('employee_id'):
                return [self._create_error_record(hr_record, 'E_VALIDATION', 'Missing required field: employee_id')]
            if not hr_record.get('dept_code'):
                return [self._create_error_record(hr_record, 'E_VALIDATION', 'Missing required field: dept_code')]
            
            staging_records = []
            
            # 基本給
            if payroll_data.get('basic_salary'):
                salary_record = self._create_payroll_record(
                    hr_record, payroll_data, 'SALARY', payroll_data['basic_salary']
                )
                staging_records.append(salary_record)

            # 諸手当
            for allowance_type, amount in payroll_data.get('allowances', {}).items():
                if amount and amount != 0:
                    allowance_record = self._create_payroll_record(
                        hr_record, payroll_data, f'ALLOWANCE_{allowance_type}', amount
                    )
                    staging_records.append(allowance_record)

            # 控除項目
            for deduction_type, amount in payroll_data.get('deductions', {}).items():
                if amount and amount != 0:
                    deduction_record = self._create_payroll_record(
                        hr_record, payroll_data, f'DEDUCTION_{deduction_type}', -amount
                    )
                    staging_records.append(deduction_record)

            # 賞与
            if payroll_data.get('bonus'):
                bonus_record = self._create_payroll_record(
                    hr_record, payroll_data, 'BONUS', payroll_data['bonus']
                )
                staging_records.append(bonus_record)

            return staging_records

        except Exception as e:
            logger.error(f"Error transforming HR record {hr_record.get('export_id')}: {e}")
            return [self._create_error_record(hr_record, 'E_TRANSFORM', str(e))]

    # 給与項目の単一レコードを作成
    def _create_payroll_record(self, hr_record: Dict[str, Any], payroll_data: Dict[str, Any], 
                              payroll_type: str, amount: Decimal) -> Dict[str, Any]:
        
        # 会計日付の決定
        payroll_date = payroll_data.get('payroll_date', date.today())
        if isinstance(payroll_date, str):
            payroll_date = datetime.strptime(payroll_date, '%Y-%m-%d').date()

        # 配賦処理
        allocation_records = self._apply_allocation_rules(hr_record, amount)
        
        base_record = {
            # 識別/監査
            'batch_id': self.batch_id,
            'source_system': self.source_system,
            'source_doc_type': 'PAYROLL',
            'source_doc_id': self._create_payroll_doc_id(hr_record, payroll_data),
            'source_line_id': payroll_type,
            'event_timestamp': payroll_data.get('payroll_date', datetime.now()),
            'load_timestamp': datetime.now(timezone.utc),
            'status_code': 'PROCESSING',
            'error_code': None,
            'error_message': None,

            # 会社/会計カレンダ/通貨
            'ledger_id': 'GL001',
            'legal_entity_id': 'COMP001',
            'business_unit': 'BU001',
            'company_code': 'COMP001',
            'accounting_date': payroll_date,
            'period_name': self.utils.format_period_name(payroll_date),
            'journal_category': self._get_payroll_journal_category(payroll_type),
            'currency_code': payroll_data.get('currency_code', 'JPY'),
            'exchange_rate_type': 'Corporate',
            'exchange_rate': self._get_exchange_rate(payroll_data),

            # セグメント
            'segment_account': self._get_payroll_account(payroll_type),
            'segment_department': hr_record.get('dept_code'),
            'segment_product': None,
            'segment_project': self._get_project_allocation(hr_record),
            'segment_interco': None,
            'segment_custom1': hr_record.get('cost_center_code'),
            'segment_custom2': hr_record.get('payroll_group'),

            # 取引先/サブレジャ
            'customer_id': None,
            'supplier_id': None,
            'bank_account_id': hr_record.get('bank_account_no'),
            'invoice_id': None,
            'invoice_line_id': None,
            'po_id': None,
            'receipt_id': None,
            'asset_id': None,
            'project_id': self._get_project_allocation(hr_record),

            # 金額/数量
            'quantity': None,
            'uom_code': None,

            # 税/収益認識
            'tax_code': self._get_payroll_tax_code(payroll_type, hr_record),
            'tax_rate': None,
            'tax_amount_entered': None,
            'revrec_rule_code': None,
            'revrec_start_date': None,
            'revrec_end_date': None,

            # 説明/参照
            'description': self._create_payroll_description(hr_record, payroll_type),
            'reference1': hr_record.get('employee_number'),
            'reference2': hr_record.get('payroll_group'),
            'reference3': hr_record.get('allocation_rule_code'),
            'reference4': payroll_data.get('payroll_id'),
            'reference5': None,

            # 取消/反転
            'reversal_flag': payroll_data.get('reversal_flag', False),
            'reversed_interface_id': None,

            # 作成/更新
            'created_by': 'ETL_HR',
            'updated_by': 'ETL_HR'
        }

        # 借方・貸方の設定
        self._set_payroll_debit_credit(base_record, payroll_type, amount)

        # データ品質チェック
        validation_errors = self._validate_record(base_record)
        if validation_errors:
            base_record['status_code'] = 'ERROR'
            base_record['error_code'] = 'E_VALIDATION'
            base_record['error_message'] = '; '.join(validation_errors)
        else:
            base_record['status_code'] = 'READY'

        return base_record

    # 給与ドキュメントIDの作成
    def _create_payroll_doc_id(self, hr_record: Dict[str, Any], payroll_data: Dict[str, Any]) -> str:
        employee_id = hr_record.get('employee_id', '')
        payroll_id = payroll_data.get('payroll_id', '')
        payroll_date = payroll_data.get('payroll_date', '')
        
        if isinstance(payroll_date, date):
            payroll_date = payroll_date.strftime('%Y%m')
        
        return f"{employee_id}_{payroll_id}_{payroll_date}"

    # 給与項目のジャーナルカテゴリ決定
    def _get_payroll_journal_category(self, payroll_type: str) -> str:
        if 'SALARY' in payroll_type or 'ALLOWANCE' in payroll_type:
            return 'Payroll'
        elif 'BONUS' in payroll_type:
            return 'Bonus'
        elif 'DEDUCTION' in payroll_type:
            if 'TAX' in payroll_type:
                return 'Tax'
            else:
                return 'Payroll'
        else:
            return 'Payroll'

    # 給与項目の勘定科目決定
    def _get_payroll_account(self, payroll_type: str) -> str:
        account_mapping = {
            'SALARY': '6100',           # 給与費
            'BONUS': '6110',            # 賞与費
            'ALLOWANCE_TRANSPORT': '6120',  # 交通費
            'ALLOWANCE_HOUSING': '6130',    # 住宅手当
            'ALLOWANCE_FAMILY': '6140',     # 扶養手当
            'DEDUCTION_INCOME_TAX': '2400', # 所得税預り金
            'DEDUCTION_RESIDENT_TAX': '2401', # 住民税預り金
            'DEDUCTION_SOCIAL_INSURANCE': '2402', # 社会保険料預り金
            'DEDUCTION_PENSION': '2403',    # 厚生年金預り金
            'DEDUCTION_HEALTH': '2404',     # 健康保険預り金
            'DEDUCTION_EMPLOYMENT': '2405', # 雇用保険預り金
        }
        
        return account_mapping.get(payroll_type, '6100')  # デフォルト給与費

    # 給与項目の借方・貸方設定
    def _set_payroll_debit_credit(self, record: Dict[str, Any], payroll_type: str, amount: Decimal):
        amount = abs(amount)
        exchange_rate = record.get('exchange_rate', Decimal('1.0'))

        if payroll_type.startswith('DEDUCTION'):
            # 控除項目：預り金(Cr) / 給与費(Dr)の一部相殺
            record['entered_dr'] = Decimal('0')
            record['entered_cr'] = amount
        else:
            # 支給項目：給与費(Dr) / 給与未払金(Cr)
            record['entered_dr'] = amount
            record['entered_cr'] = Decimal('0')

        # 記帳通貨への換算
        record['accounted_dr'] = record['entered_dr'] * exchange_rate
        record['accounted_cr'] = record['entered_cr'] * exchange_rate

    # 配賦ルールの適用
    def _apply_allocation_rules(self, hr_record: Dict[str, Any], amount: Decimal) -> List[Dict[str, Any]]:
        allocation_rule = hr_record.get('allocation_rule_code')
        
        if not allocation_rule:
            return [{'department': hr_record.get('dept_code'), 'amount': amount, 'percentage': 100}]

        # TODO: 実装では配賦ルールマスタから取得
        # サンプル配賦ルール
        if allocation_rule == 'SPLIT_50_50':
            return [
                {'department': hr_record.get('dept_code'), 'amount': amount * Decimal('0.5'), 'percentage': 50},
                {'department': f"{hr_record.get('dept_code')}_SHARED", 'amount': amount * Decimal('0.5'), 'percentage': 50}
            ]
        else:
            return [{'department': hr_record.get('dept_code'), 'amount': amount, 'percentage': 100}]

    # プロジェクト配賦の取得
    def _get_project_allocation(self, hr_record: Dict[str, Any]) -> Optional[str]:
        # 実装では工数連携システムから取得
        return None

    # 為替レートの取得
    def _get_exchange_rate(self, payroll_data: Dict[str, Any]) -> Decimal:
        currency_code = payroll_data.get('currency_code', 'JPY')
        payroll_date = payroll_data.get('payroll_date', date.today())
        
        return self.utils.calculate_exchange_rate(
            Decimal('1.0'), currency_code, 'JPY', payroll_date
        )

    # 給与税コードの取得
    def _get_payroll_tax_code(self, payroll_type: str, hr_record: Dict[str, Any]) -> Optional[str]:
        if 'TAX' in payroll_type:
            tax_region = hr_record.get('tax_region_code', 'JP')
            return f"PAYROLL_TAX_{tax_region}"
        return None

    # 給与説明文の作成
    def _create_payroll_description(self, hr_record: Dict[str, Any], payroll_type: str) -> str:
        parts = []
        
        # 従業員情報
        employee_number = hr_record.get('employee_number', '')
        last_name = hr_record.get('last_name', '')
        first_name = hr_record.get('first_name', '')
        
        if employee_number:
            parts.append(f"Emp: {employee_number}")
        
        if last_name and first_name:
            parts.append(f"{last_name} {first_name}")
        
        # 給与項目種別
        payroll_type_desc = {
            'SALARY': '基本給',
            'BONUS': '賞与',
            'ALLOWANCE_TRANSPORT': '交通費',
            'ALLOWANCE_HOUSING': '住宅手当',
            'ALLOWANCE_FAMILY': '扶養手当',
            'DEDUCTION_INCOME_TAX': '所得税',
            'DEDUCTION_RESIDENT_TAX': '住民税',
            'DEDUCTION_SOCIAL_INSURANCE': '社会保険料'
        }.get(payroll_type, payroll_type)
        
        parts.append(payroll_type_desc)

        description = ' | '.join(parts)
        return self.utils.clean_string(description, 500)

    # レコードの妥当性チェック
    def _validate_record(self, target_record: Dict[str, Any]) -> List[str]:
        errors = []

        # 必須フィールドチェック
        mandatory_fields = [
            'source_doc_id', 'source_line_id', 'accounting_date',
            'currency_code', 'segment_account', 'segment_department'
        ]
        errors.extend(self.validator.validate_mandatory_fields(target_record, mandatory_fields))

        # 金額チェック
        entered_dr = target_record.get('entered_dr', Decimal('0'))
        entered_cr = target_record.get('entered_cr', Decimal('0'))
        
        if entered_dr == 0 and entered_cr == 0:
            errors.append("Both debit and credit amounts are zero")

        # 部門コードチェック
        if not target_record.get('segment_department'):
            errors.append("Department code is required for payroll entries")

        return errors

    # エラーレコードの作成
    def _create_error_record(self, hr_record: Dict[str, Any], error_code: str, 
                           error_message: str) -> Dict[str, Any]:
        return {
            'batch_id': self.batch_id,
            'source_system': self.source_system,
            'source_doc_type': 'PAYROLL',
            'source_doc_id': hr_record.get('employee_id', ''),
            'source_line_id': 'ERROR',
            'event_timestamp': datetime.now(),
            'status_code': 'ERROR',
            'error_code': error_code,
            'error_message': self.utils.get_error_message(error_code, error_message),
            'load_timestamp': datetime.now(timezone.utc),
            'created_by': 'ETL_HR',
            'updated_by': 'ETL_HR'
        }

    # 給与未払金の仕訳を作成
    def create_payable_entries(self, payroll_records: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        payable_records = []
        
        # 従業員ごとの給与合計を計算
        employee_totals = {}
        for record in payroll_records:
            if record['status_code'] != 'READY':
                continue
                
            employee_id = record['reference1']  # employee_number
            if employee_id not in employee_totals:
                employee_totals[employee_id] = {
                    'total_dr': Decimal('0'),
                    'total_cr': Decimal('0'),
                    'record_template': record
                }
            
            employee_totals[employee_id]['total_dr'] += record['entered_dr']
            employee_totals[employee_id]['total_cr'] += record['entered_cr']

        # 給与未払金の仕訳を作成
        for employee_id, totals in employee_totals.items():
            net_payable = totals['total_dr'] - totals['total_cr']
            
            if net_payable > 0:
                template = totals['record_template']
                payable_record = template.copy()
                payable_record.update({
                    'source_line_id': 'PAYABLE',
                    'segment_account': '2100',  # 給与未払金
                    'entered_dr': Decimal('0'),
                    'entered_cr': net_payable,
                    'accounted_dr': Decimal('0'),
                    'accounted_cr': net_payable * template.get('exchange_rate', Decimal('1.0')),
                    'description': f"Payroll Payable - {employee_id}"
                })
                payable_records.append(payable_record)

        return payable_records
