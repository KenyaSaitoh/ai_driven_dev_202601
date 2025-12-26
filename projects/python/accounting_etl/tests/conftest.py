# pytest共通設定とフィクスチャ

import pytest
import os
import sys
from pathlib import Path

# プロジェクトルートをパスに追加
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))


@pytest.fixture(scope="session")
def project_root_dir():
    """プロジェクトルートディレクトリのパス"""
    return project_root


@pytest.fixture(scope="session")
def test_data_dir(project_root_dir):
    """テストデータディレクトリのパス"""
    return project_root_dir / "test_data"


@pytest.fixture(scope="session")
def expected_data_dir(test_data_dir):
    """期待値データディレクトリのパス"""
    return test_data_dir / "expected"


@pytest.fixture(scope="session")
def output_dir(project_root_dir):
    """出力ディレクトリのパス"""
    return project_root_dir / "output"


@pytest.fixture
def csv_reader():
    """CSV読み込みヘルパー"""
    import csv
    
    def read_csv_as_dict(file_path):
        """CSVを辞書のリストとして読み込む"""
        records = []
        with open(file_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                records.append(row)
        return records
    
    return read_csv_as_dict


@pytest.fixture
def key_matcher():
    """キーマッチングヘルパー"""
    
    def create_key_index(records, key_fields):
        """
        レコードリストをキーフィールドでインデックス化
        
        Args:
            records: レコードのリスト
            key_fields: キーフィールドのリスト（例: ['source_system', 'source_doc_id', 'source_line_id']）
        
        Returns:
            {(key1, key2, ...): record} の辞書
        """
        index = {}
        for record in records:
            key = tuple(record.get(field) for field in key_fields)
            index[key] = record
        return index
    
    return create_key_index


@pytest.fixture
def decimal_comparator():
    """数値比較ヘルパー（丸め誤差を考慮）"""
    from decimal import Decimal
    
    def compare_decimal(actual, expected, tolerance=Decimal('0.01')):
        """
        Decimal型の数値を比較（許容誤差あり）
        
        Args:
            actual: 実際の値
            expected: 期待値
            tolerance: 許容誤差（デフォルト: 0.01）
        
        Returns:
            bool: 許容範囲内ならTrue
        """
        if actual is None and expected is None:
            return True
        if actual is None or expected is None:
            return False
        
        # 文字列から Decimal に変換
        if isinstance(actual, str):
            actual = Decimal(actual) if actual else Decimal('0')
        if isinstance(expected, str):
            expected = Decimal(expected) if expected else Decimal('0')
        
        return abs(Decimal(actual) - Decimal(expected)) <= tolerance
    
    return compare_decimal

