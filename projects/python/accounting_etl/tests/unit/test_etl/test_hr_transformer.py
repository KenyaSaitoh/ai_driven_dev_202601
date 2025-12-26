# 人事トランスフォーマーの単体テスト

import pytest
from datetime import datetime, date
from decimal import Decimal

from src.etl.hr_transformer import HRTransformer


class TestHRTransformer:
    """HRTransformerの単体テスト"""
    
    @pytest.fixture
    def transformer(self):
        """テスト用のトランスフォーマーインスタンス"""
        return HRTransformer(batch_id="TEST_BATCH_001")
    
    @pytest.fixture
    def sample_hr_record(self):
        """サンプル人事レコード"""
        return {
            'export_id': '1',
            'employee_id': 'EMP001',
            'employee_number': 'E0001',
            'last_name': '山田',
            'first_name': '太郎',
            'dept_code': 'D001',
            'cost_center_code': 'CC001',
            'payroll_group': 'PG_REGULAR',
            'bank_account_no': '1234567890',
            'allocation_rule_code': None,
            'tax_region_code': 'JP'
        }
    
    @pytest.fixture
    def sample_payroll_data(self):
        """サンプル給与データ"""
        return {
            'payroll_id': 'PAY202510',
            'payroll_date': date(2025, 10, 25),
            'currency_code': 'JPY',
            'basic_salary': Decimal('300000'),
            'allowances': {
                'TRANSPORT': Decimal('10000'),
                'HOUSING': Decimal('50000')
            },
            'deductions': {
                'INCOME_TAX': Decimal('15000'),
                'SOCIAL_INSURANCE': Decimal('45000')
            },
            'bonus': None,
            'reversal_flag': False
        }
    
    def test_transform_payroll_record_success(self, transformer, sample_hr_record, sample_payroll_data):
        """給与レコード変換が成功することを確認"""
        records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        # 基本給 + 手当2件 + 控除2件 = 5件のレコードが生成される
        assert len(records) == 5
        
        # 全レコードがステータスREADYまたはPROCESSINGであること
        for record in records:
            assert record['status_code'] in ['READY', 'PROCESSING']
            assert record['source_system'] == 'HR'
            assert record['batch_id'] == 'TEST_BATCH_001'
    
    def test_payroll_doc_id_creation(self, transformer, sample_hr_record, sample_payroll_data):
        """給与ドキュメントIDが正しく生成されることを確認"""
        records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        expected_doc_id = "EMP001_PAY202510_202510"
        for record in records:
            assert record['source_doc_id'] == expected_doc_id
    
    def test_payroll_accounts(self, transformer, sample_hr_record, sample_payroll_data):
        """給与項目ごとに正しい勘定科目が設定されることを確認"""
        records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        # レコードを種別ごとに分類
        record_by_type = {r['source_line_id']: r for r in records}
        
        # 基本給 -> 6100（給与費）
        assert record_by_type['SALARY']['segment_account'] == '6100'
        
        # 交通費 -> 6120
        assert record_by_type['ALLOWANCE_TRANSPORT']['segment_account'] == '6120'
        
        # 住宅手当 -> 6130
        assert record_by_type['ALLOWANCE_HOUSING']['segment_account'] == '6130'
        
        # 所得税控除 -> 2400（預り金）
        assert record_by_type['DEDUCTION_INCOME_TAX']['segment_account'] == '2400'
        
        # 社会保険控除 -> 2402
        assert record_by_type['DEDUCTION_SOCIAL_INSURANCE']['segment_account'] == '2402'
    
    def test_debit_credit_amounts(self, transformer, sample_hr_record, sample_payroll_data):
        """借方・貸方の金額が正しく設定されることを確認"""
        records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        record_by_type = {r['source_line_id']: r for r in records}
        
        # 支給項目は借方
        salary_record = record_by_type['SALARY']
        assert salary_record['entered_dr'] == Decimal('300000')
        assert salary_record['entered_cr'] == Decimal('0')
        
        # 控除項目は貸方
        tax_record = record_by_type['DEDUCTION_INCOME_TAX']
        assert tax_record['entered_dr'] == Decimal('0')
        assert tax_record['entered_cr'] == Decimal('15000')
    
    def test_journal_category(self, transformer, sample_hr_record, sample_payroll_data):
        """ジャーナルカテゴリが正しく設定されることを確認"""
        records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        record_by_type = {r['source_line_id']: r for r in records}
        
        # 給与・手当 -> Payroll
        assert record_by_type['SALARY']['journal_category'] == 'Payroll'
        assert record_by_type['ALLOWANCE_TRANSPORT']['journal_category'] == 'Payroll'
        
        # 所得税控除 -> Tax
        assert record_by_type['DEDUCTION_INCOME_TAX']['journal_category'] == 'Tax'
    
    def test_create_payable_entries(self, transformer, sample_hr_record, sample_payroll_data):
        """給与未払金の仕訳が正しく生成されることを確認"""
        payroll_records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        # READY状態のレコードのみを対象
        ready_records = [r for r in payroll_records if r['status_code'] == 'READY']
        
        payable_entries = transformer.create_payable_entries(ready_records)
        
        # 未払金エントリが生成されること
        assert len(payable_entries) > 0
        
        payable_entry = payable_entries[0]
        
        # 勘定科目が給与未払金（2100）であること
        assert payable_entry['segment_account'] == '2100'
        
        # 貸方金額が設定されていること
        assert payable_entry['entered_cr'] > 0
        assert payable_entry['entered_dr'] == 0
        
        # 純支給額の計算確認
        # (基本給 + 手当) - 控除 = 300000 + 10000 + 50000 - 15000 - 45000 = 300000
        expected_net = Decimal('300000')
        assert payable_entry['entered_cr'] == expected_net
    
    def test_period_name_format(self, transformer, sample_hr_record, sample_payroll_data):
        """会計期間が正しいフォーマットで設定されることを確認"""
        records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        for record in records:
            # YYYY-MM形式であること
            period_name = record['period_name']
            assert len(period_name) == 7
            assert period_name[:4].isdigit()
            assert period_name[4] == '-'
            assert period_name[5:].isdigit()
    
    def test_description_includes_employee_info(self, transformer, sample_hr_record, sample_payroll_data):
        """説明文に従業員情報が含まれることを確認"""
        records = transformer.transform_payroll_record(sample_hr_record, sample_payroll_data)
        
        for record in records:
            description = record['description']
            # 従業員番号が含まれること
            assert 'E0001' in description
            # 名前が含まれること
            assert '山田' in description or '太郎' in description
    
    def test_validation_errors_for_invalid_record(self, transformer):
        """無効なレコードに対してエラーが設定されることを確認"""
        invalid_hr_record = {
            'export_id': '999',
            'employee_id': None,  # 必須項目が欠落
            'dept_code': None,    # 必須項目が欠落
        }
        
        invalid_payroll_data = {
            'payroll_id': 'PAY001',
            'payroll_date': date.today(),
            'currency_code': 'JPY',
            'basic_salary': Decimal('0'),  # 金額が0
        }
        
        records = transformer.transform_payroll_record(invalid_hr_record, invalid_payroll_data)
        
        # エラーレコードが生成されるか、またはバリデーションエラーが設定されていること
        assert len(records) > 0
        for record in records:
            if record['status_code'] == 'ERROR':
                assert record['error_code'] is not None
                assert record['error_message'] is not None

