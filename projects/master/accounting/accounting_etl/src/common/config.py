# ETL処理の共通設定・定数定義

import os

# 環境設定（環境変数で切り替え可能）
# 'local': ローカル開発環境（HsqlDB/PostgreSQL）
# 'aws': AWS本番環境（S3 + Glue）
ENVIRONMENT = os.getenv('ETL_ENVIRONMENT', 'local')

# データベース接続情報（ローカル環境用）
# 本番環境ではこの設定は使用されず、S3ベースのデータソースを使用します
DB_CONFIGS = {
    'source': {
        'driver': os.getenv('SOURCE_DB_DRIVER', 'org.hsqldb.jdbc.JDBCDriver'),
        'jdbc_url': os.getenv('SOURCE_DB_URL', 'jdbc:hsqldb:hsql://localhost:9001/testdb'),
        'host': os.getenv('SOURCE_DB_HOST', 'localhost'),
        'port': int(os.getenv('SOURCE_DB_PORT', '9001')),
        'database': os.getenv('SOURCE_DB_NAME', 'testdb'),
        'user': os.getenv('SOURCE_DB_USER', 'SA'),
        'password': os.getenv('SOURCE_DB_PASSWORD', ''),
        'jar_path': os.getenv('HSQLDB_JAR_PATH', '../../../../../hsqldb/lib/hsqldb.jar')
    },
    'target': {
        'driver': os.getenv('TARGET_DB_DRIVER', 'org.hsqldb.jdbc.JDBCDriver'),
        'jdbc_url': os.getenv('TARGET_DB_URL', 'jdbc:hsqldb:hsql://localhost:9001/testdb'),
        'host': os.getenv('TARGET_DB_HOST', 'localhost'),
        'port': int(os.getenv('TARGET_DB_PORT', '9001')),
        'database': os.getenv('TARGET_DB_NAME', 'testdb'),
        'user': os.getenv('TARGET_DB_USER', 'SA'),
        'password': os.getenv('TARGET_DB_PASSWORD', ''),
        'jar_path': os.getenv('HSQLDB_JAR_PATH', '../../../../../hsqldb/lib/hsqldb.jar')
    }
}

# データベース接続情報（S3ベース - AWS環境用）
S3_DATA_SOURCES = {
    'sales': {
        'input_path': 's3://glue-files/input/sales_txn_export/',
        'output_path': 's3://glue-files/output/accounting_staging/',
        'format': 'parquet',
        'partition_keys': ['batch_id', 'accounting_date']
    },
    'hr': {
        'input_path': 's3://glue-files/input/hr_employee_org_export/',
        'output_path': 's3://glue-files/output/accounting_staging/',
        'format': 'parquet',
        'partition_keys': ['batch_id', 'accounting_date']
    },
    'inventory': {
        'input_path': 's3://glue-files/input/inv_movement_export/',
        'output_path': 's3://glue-files/output/accounting_staging/',
        'format': 'parquet',
        'partition_keys': ['batch_id', 'accounting_date']
    }
}

# S3バケット設定
S3_CONFIG = {
    'bucket': 'glue-files',
    'input_prefix': 'input',
    'output_prefix': 'output',
    'error_prefix': 'error-data',
    'temp_prefix': 'temp'
}

# ソースシステム設定
SOURCE_SYSTEMS = {
    'SALE': {
        'table': 'sales_txn_export',
        'key_fields': ['source_txn_id', 'source_line_id'],
        'batch_size': 10000
    },
    'HR': {
        'table': 'hr_employee_org_export',
        'key_fields': ['employee_id', 'effective_start_date'],
        'batch_size': 5000
    },
    'INV': {
        'table': 'inv_movement_export',
        'key_fields': ['movement_id', 'movement_line_id'],
        'batch_size': 15000
    }
}

# ジャーナルカテゴリマッピング
JOURNAL_CATEGORY_MAPPING = {
    'SALE': {
        'ORDER': 'Sales',
        'SHIP': 'Sales',
        'INVOICE': 'Sales',
        'CM': 'Sales',
        'PMT': 'Payment'
    },
    'HR': {
        'PAYROLL': 'Payroll'
    },
    'INV': {
        'RCV': 'Inventory',
        'ISS': 'Inventory',
        'TRF': 'Inventory',
        'ADJ': 'Inventory',
        'CNT': 'Inventory',
        'CST': 'Cost'
    }
}

# 会計科目マッピング
ACCOUNT_MAPPING = {
    'SALE': {
        'INVOICE': {
            'default': '4100',  # 売上高
            'tax': '2300'       # 売上税
        },
        'ORDER': {
            'default': '4100'
        }
    },
    'HR': {
        'PAYROLL': {
            'salary': '6100',   # 給与
            'bonus': '6110',    # 賞与
            'tax': '2400'       # 給与税
        }
    },
    'INV': {
        'RCV': {
            'default': '1200'   # 在庫資産
        },
        'ISS': {
            'default': '5100'   # 売上原価
        },
        'ADJ': {
            'default': '5200'   # 在庫調整
        }
    }
}

# エラーコード定義
ERROR_CODES = {
    'E_SALE_001': 'Invalid transaction type',
    'E_SALE_002': 'Missing source transaction ID',
    'E_SALE_003': 'Missing source line ID',
    'E_CAL_001': 'Invalid accounting date',
    'E_MAP_005': 'Journal category mapping failed',
    'E_CUR_001': 'Invalid currency code',
    'E_CUR_002': 'Exchange rate not found',
    'E_ACC_001': 'Account mapping failed',
    'E_MDM_001': 'Product master not found',
    'E_MDM_002': 'Customer master not found',
    'E_TAX_001': 'Tax calculation error',
    'E_INV_001': 'Invalid movement type',
    'E_ACC_002': 'Inventory account mapping failed',
    'E_MDM_ITEM': 'Item master not found',
    'E_COST_001': 'Cost calculation error',
    'E_KEY_DUP': 'Duplicate key error',
    'E_ACC_PAY': 'Payroll account mapping failed',
    'E_PAY_SUM': 'Payroll amount validation failed',
    'E_MAP_CAT': 'Category mapping failed',
    'W_ZERO': 'Zero amount warning'
}

# 日付フォーマット
DATE_FORMATS = {
    'period_name': '%Y-%m',
    'accounting_date': '%Y-%m-%d',
    'timestamp': '%Y-%m-%d %H:%M:%S'
}

# バリデーション設定
VALIDATION_RULES = {
    'max_amount': 999999999999.99,
    'min_amount': -999999999999.99,
    'max_description_length': 500,
    'max_reference_length': 100
}
