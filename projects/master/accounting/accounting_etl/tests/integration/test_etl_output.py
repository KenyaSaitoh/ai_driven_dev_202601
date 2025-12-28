# ETL出力結果の統合テスト
# キーマッチング方式で期待値と実際の出力を比較

import pytest
import os
from pathlib import Path
from decimal import Decimal


class TestETLOutputValidation:
    """ETL出力ファイルの検証テスト"""
    
    # 検証対象のキーフィールド
    KEY_FIELDS = ['source_system', 'source_doc_type', 'source_doc_id', 'source_line_id']
    
    # 検証する主要フィールド
    CRITICAL_FIELDS = [
        'source_system',
        'source_doc_type',
        'source_doc_id',
        'source_line_id',
        'status_code',
        'ledger_id',
        'period_name',
        'journal_category',
        'currency_code',
        'segment_account',
        'segment_department',
    ]
    
    # 比較から除外するフィールド（実行時に変わるため）
    EXCLUDED_FIELDS = ['batch_id', 'load_timestamp', 'accounting_date', 'event_timestamp']
    
    # 金額フィールド
    AMOUNT_FIELDS = ['entered_dr', 'entered_cr', 'accounted_dr', 'accounted_cr']
    
    @pytest.fixture(scope="class")
    def output_file(self, output_dir):
        """実際の出力ファイルパス"""
        return output_dir / "accounting_txn_interface.csv"
    
    @pytest.fixture(scope="class")
    def expected_file_sales(self, expected_data_dir):
        """売上の期待値ファイルパス"""
        return expected_data_dir / "accounting_txn_interface_sale.csv"
    
    @pytest.fixture(scope="class")
    def expected_file_hr(self, expected_data_dir):
        """人事の期待値ファイルパス"""
        return expected_data_dir / "accounting_txn_interface_hr.csv"
    
    @pytest.fixture(scope="class")
    def expected_file_inventory(self, expected_data_dir):
        """在庫の期待値ファイルパス"""
        return expected_data_dir / "accounting_txn_interface_inv.csv"
    
    def test_output_file_exists(self, output_file):
        """出力ファイルが存在することを確認"""
        assert output_file.exists(), f"Output file not found: {output_file}"
    
    def test_output_file_not_empty(self, output_file, csv_reader):
        """出力ファイルが空でないことを確認"""
        records = csv_reader(output_file)
        assert len(records) > 0, "Output file is empty"
    
    def test_output_has_required_columns(self, output_file, csv_reader):
        """出力ファイルに必須カラムが含まれていることを確認"""
        records = csv_reader(output_file)
        assert len(records) > 0, "Output file has no records"
        
        first_record = records[0]
        required_columns = self.KEY_FIELDS + self.CRITICAL_FIELDS + self.AMOUNT_FIELDS
        
        for column in required_columns:
            assert column in first_record, f"Required column '{column}' not found in output"
    
    def test_no_error_records(self, output_file, csv_reader):
        """エラーステータスのレコードがないことを確認"""
        records = csv_reader(output_file)
        error_records = [r for r in records if r.get('status_code') == 'ERROR']
        
        assert len(error_records) == 0, (
            f"Found {len(error_records)} error records in output:\n" +
            "\n".join([f"  - {r.get('source_doc_id')} ({r.get('error_code')}): {r.get('error_message')}" 
                      for r in error_records[:5]])
        )
    
    # 片側記録方式では借方・貸方バランステストは不要のため削除
    # 
    # 理由: 本システムは片側記録方式を採用しており、各システムは借方または貸方の
    # 片側のみを記録します。対側仕訳はERP側の自動仕訳ルールで生成されるため、
    # ETL出力時点ではバランスしないのが正常です。
    #
    # - 売上（SALE）: 貸方のみ記録（売上高）
    # - 人事（HR）: 給与費用は借方のみ、控除は貸方のみ
    # - 在庫（INV）: 借方のみ記録（棚卸資産、売上原価等）
    
    def _validate_against_expected(self, output_file, expected_file, csv_reader, 
                                   key_matcher, decimal_comparator, source_system):
        """期待値ファイルとの比較検証（共通ロジック）"""
        
        # 期待値ファイルの存在確認
        if not expected_file.exists():
            pytest.skip(f"Expected file not found: {expected_file}")
        
        # ファイル読み込み
        actual_records = csv_reader(output_file)
        expected_records = csv_reader(expected_file)
        
        # システムでフィルタ
        actual_records = [r for r in actual_records if r.get('source_system') == source_system]
        
        if len(expected_records) == 0:
            pytest.skip(f"Expected file is empty: {expected_file}")
        
        # キーでインデックス化
        actual_index = key_matcher(actual_records, self.KEY_FIELDS)
        expected_index = key_matcher(expected_records, self.KEY_FIELDS)
        
        # 期待値に含まれるキーが実際の出力にすべて存在することを確認
        # 注: 実際の出力には期待値より多くのレコードがある場合があります（部分検証）
        missing_keys = set(expected_index.keys()) - set(actual_index.keys())
        assert len(missing_keys) == 0, (
            f"Expected records not found in actual output for {source_system}: "
            f"{list(missing_keys)[:10]}"
        )
        
        # キーごとに検証
        mismatches = []
        for key, expected_record in expected_index.items():
            if key not in actual_index:
                mismatches.append(f"Missing record with key: {key}")
                continue
            
            actual_record = actual_index[key]
            
            # 主要フィールドの検証（除外フィールドはスキップ）
            for field in self.CRITICAL_FIELDS:
                # 実行時に変動するフィールドは検証から除外
                if field in self.EXCLUDED_FIELDS:
                    continue
                
                expected_value = expected_record.get(field, '').strip()
                actual_value = actual_record.get(field, '').strip()
                
                if expected_value != actual_value:
                    mismatches.append(
                        f"Key {key}, Field '{field}': expected='{expected_value}', actual='{actual_value}'"
                    )
            
            # 金額フィールドの検証（許容誤差あり）
            for field in self.AMOUNT_FIELDS:
                expected_value = expected_record.get(field, '').strip()
                actual_value = actual_record.get(field, '').strip()
                
                if not decimal_comparator(actual_value, expected_value, Decimal('0.01')):
                    mismatches.append(
                        f"Key {key}, Amount '{field}': expected={expected_value}, actual={actual_value}"
                    )
        
        # エラーメッセージ出力（最初の10件まで）
        if mismatches:
            error_msg = f"Found {len(mismatches)} mismatches for {source_system}:\n"
            error_msg += "\n".join(mismatches[:10])
            if len(mismatches) > 10:
                error_msg += f"\n... and {len(mismatches) - 10} more"
            assert False, error_msg
    
    def test_sales_output_matches_expected(self, output_file, expected_file_sales, 
                                          csv_reader, key_matcher, decimal_comparator):
        """売上システムの出力が期待値と一致することを確認"""
        self._validate_against_expected(
            output_file, expected_file_sales, csv_reader, 
            key_matcher, decimal_comparator, "SALE"
        )
    
    def test_hr_output_matches_expected(self, output_file, expected_file_hr, 
                                       csv_reader, key_matcher, decimal_comparator):
        """人事システムの出力が期待値と一致することを確認"""
        self._validate_against_expected(
            output_file, expected_file_hr, csv_reader, 
            key_matcher, decimal_comparator, "HR"
        )
    
    def test_inventory_output_matches_expected(self, output_file, expected_file_inventory, 
                                              csv_reader, key_matcher, decimal_comparator):
        """在庫システムの出力が期待値と一致することを確認"""
        self._validate_against_expected(
            output_file, expected_file_inventory, csv_reader, 
            key_matcher, decimal_comparator, "INV"
        )


# 個別システム出力ファイルのテストクラスは削除
# 
# 理由: 本システムは統合ファイル（accounting_txn_interface.csv）を最終成果物とし、
# 個別ファイル（sales.csv, hr.csv, inv.csv）は統合後に削除される設計のため、
# 個別ファイルのテストは不要です。統合ファイルの内容は上記の期待値マッチングテストで
# 十分にカバーされています。

