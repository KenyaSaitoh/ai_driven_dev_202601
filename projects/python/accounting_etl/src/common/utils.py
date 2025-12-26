# ETL処理の共通ユーティリティ関数

import boto3
import uuid
from datetime import datetime, timezone
from decimal import Decimal, ROUND_HALF_UP
import re
from typing import Optional, Dict, Any, List
import logging

logger = logging.getLogger(__name__)


# ETL処理の共通ユーティリティクラス
class ETLUtils:

    # バッチIDを生成する
    # フォーマット: {source_system}_{timestamp}_{uuid}
    @staticmethod
    def generate_batch_id(source_system: str) -> str:
        timestamp = datetime.now(timezone.utc).strftime('%Y%m%d_%H%M%S')
        uuid_part = str(uuid.uuid4())[:8]
        return f"{source_system}_{timestamp}_{uuid_part}"

    # 日付から期間名（YYYY-MM）を生成する
    @staticmethod
    def format_period_name(date_value) -> str:
        if isinstance(date_value, str):
            date_value = datetime.strptime(date_value.split()[0], '%Y-%m-%d')
        return date_value.strftime('%Y-%m')

    # 通貨コードの妥当性チェック（ISO 4217準拠）
    @staticmethod
    def validate_currency_code(currency_code: str) -> bool:
        if not currency_code or len(currency_code) != 3:
            return False
        return currency_code.upper() in ['USD', 'EUR', 'JPY', 'GBP', 'AUD', 'CAD', 'CHF', 'CNY', 'SEK', 'NZD']

    # 為替レートを取得・計算する（実装では外部サービスやテーブル参照）
    @staticmethod
    def calculate_exchange_rate(amount: Decimal, from_currency: str, to_currency: str, 
                              rate_date: datetime) -> Optional[Decimal]:
        # TODO: 実際の実装では為替レートテーブルやAPIから取得
        if from_currency == to_currency:
            return Decimal('1.0')
        
        # サンプル固定レート（実運用では動的取得）
        sample_rates = {
            ('USD', 'JPY'): Decimal('150.0'),
            ('EUR', 'JPY'): Decimal('165.0'),
            ('JPY', 'USD'): Decimal('0.0067'),
            ('JPY', 'EUR'): Decimal('0.0061')
        }
        
        rate_key = (from_currency, to_currency)
        return sample_rates.get(rate_key, Decimal('1.0'))

    # 金額と取引種別に基づいて借方・貸方を決定する
    @staticmethod
    def determine_debit_credit(amount: Decimal, account_type: str, 
                             transaction_type: str) -> tuple[Decimal, Decimal]:
        amount = abs(amount)
        
        # 基本的な仕訳ルール
        if account_type in ['asset', 'expense']:
            if transaction_type in ['increase', 'debit']:
                return amount, Decimal('0')  # 借方
            else:
                return Decimal('0'), amount  # 貸方
        elif account_type in ['liability', 'equity', 'revenue']:
            if transaction_type in ['increase', 'credit']:
                return Decimal('0'), amount  # 貸方
            else:
                return amount, Decimal('0')  # 借方
        else:
            # デフォルトは借方
            return amount, Decimal('0')

    # 金額の妥当性チェックと正規化
    @staticmethod
    def validate_amount(amount: Any) -> Optional[Decimal]:
        if amount is None:
            return Decimal('0')
        
        try:
            if isinstance(amount, str):
                # 文字列の場合、数値以外の文字を除去
                amount = re.sub(r'[^\d.-]', '', amount)
                if not amount:
                    return Decimal('0')
            
            decimal_amount = Decimal(str(amount))
            
            # 範囲チェック
            max_amount = Decimal('999999999999.99')
            min_amount = Decimal('-999999999999.99')
            
            if decimal_amount > max_amount or decimal_amount < min_amount:
                logger.warning(f"Amount out of range: {decimal_amount}")
                return None
            
            # 小数点以下4桁に丸める
            return decimal_amount.quantize(Decimal('0.0001'), rounding=ROUND_HALF_UP)
            
        except Exception as e:
            logger.error(f"Amount validation error: {e}")
            return None

    # 文字列のクリーニング（NULL、空白処理、長さ制限）
    @staticmethod
    def clean_string(value: Any, max_length: int = None) -> str:
        if value is None:
            return ''
        
        cleaned = str(value).strip()
        
        if max_length and len(cleaned) > max_length:
            cleaned = cleaned[:max_length]
            logger.warning(f"String truncated to {max_length} characters")
        
        return cleaned

    # 複合キーを作成する
    @staticmethod
    def create_composite_key(*args) -> str:
        cleaned_args = [str(arg).strip() for arg in args if arg is not None]
        return '|'.join(cleaned_args)

    # エラーコードからエラーメッセージを取得する
    @staticmethod
    def get_error_message(error_code: str, additional_info: str = None) -> str:
        from .config import ERROR_CODES
        
        base_message = ERROR_CODES.get(error_code, f"Unknown error: {error_code}")
        
        if additional_info:
            return f"{base_message} - {additional_info}"
        
        return base_message

    # 処理統計をログ出力する
    @staticmethod
    def log_processing_stats(processed_count: int, error_count: int, 
                           start_time: datetime, source_system: str):
        end_time = datetime.now(timezone.utc)
        duration = (end_time - start_time).total_seconds()
        
        logger.info(f"""
Processing Statistics:
  Source System: {source_system}
  Processed Records: {processed_count}
  Error Records: {error_count}
  Success Rate: {((processed_count - error_count) / processed_count * 100):.2f}%
  Duration: {duration:.2f} seconds
  Records/Second: {(processed_count / duration):.2f}
        """)


# データ品質検証クラス
class DataQualityValidator:

    # 必須フィールドの検証
    @staticmethod
    def validate_mandatory_fields(record: Dict[str, Any], mandatory_fields: List[str]) -> List[str]:
        errors = []
        for field in mandatory_fields:
            if field not in record or record[field] is None or record[field] == '':
                errors.append(f"Missing mandatory field: {field}")
        return errors

    # 日付フォーマットの検証
    @staticmethod
    def validate_date_format(date_str: str, format_pattern: str) -> bool:
        try:
            datetime.strptime(date_str, format_pattern)
            return True
        except (ValueError, TypeError):
            return False

    # 参照整合性の検証（マスタ存在チェック）
    @staticmethod
    def validate_reference_integrity(record: Dict[str, Any], 
                                   reference_tables: Dict[str, List[str]]) -> List[str]:
        errors = []
        # TODO: 実際のマスタテーブルとの突合
        # 現在はサンプル実装
        return errors


# S3操作のヘルパークラス
class S3Helper:

    def __init__(self, bucket_name: str):
        self.bucket_name = bucket_name
        self.s3_client = boto3.client('s3')

    # エラーデータをS3にアップロードする
    def upload_error_data(self, data: str, key: str) -> bool:
        try:
            self.s3_client.put_object(
                Bucket=self.bucket_name,
                Key=key,
                Body=data,
                ContentType='text/csv'
            )
            return True
        except Exception as e:
            logger.error(f"S3 upload error: {e}")
            return False

    # S3からルックアップデータを読み込む
    def read_lookup_data(self, key: str) -> Optional[str]:
        try:
            response = self.s3_client.get_object(
                Bucket=self.bucket_name,
                Key=key
            )
            return response['Body'].read().decode('utf-8')
        except Exception as e:
            logger.error(f"S3 read error: {e}")
            return None
