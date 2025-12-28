-- ========================================
-- 人事システム給与実績エクスポートテーブル
-- ========================================
-- HR（人事）→ 会計：給与・手当・控除の実績データを会計仕訳に変換
-- 
-- 従業員マスタ（hr_employee_org_export）と employee_id でジョイン
-- INNER JOIN方式で処理（給与実績がある従業員のみ変換対象）

CREATE TABLE hr_payroll_export (
  export_id            BIGINT IDENTITY PRIMARY KEY, -- エクスポートID（主キー）
  batch_id             VARCHAR(40) NOT NULL,                                -- バッチID（取込単位の識別子）
  
  payroll_id           VARCHAR(30) NOT NULL,                                -- 給与ID（例: PAY202501_001）
  employee_id          VARCHAR(30) NOT NULL,                                -- 社員ID（hr_employee_org_exportとのジョインキー）
  
  payroll_period       VARCHAR(7)  NOT NULL,                                -- 給与期間（YYYY-MM形式、例: 2025-01）
  payment_date         DATE        NOT NULL,                                -- 支給日
  
  -- 給与項目
  basic_salary         DECIMAL(15, 2) NOT NULL DEFAULT 0,                   -- 基本給
  overtime_pay         DECIMAL(15, 2) DEFAULT 0,                            -- 残業代
  night_shift_pay      DECIMAL(15, 2) DEFAULT 0,                            -- 深夜勤務手当
  holiday_pay          DECIMAL(15, 2) DEFAULT 0,                            -- 休日勤務手当
  
  -- 手当項目
  allowance_housing         DECIMAL(15, 2) DEFAULT 0,                       -- 住宅手当
  allowance_transportation  DECIMAL(15, 2) DEFAULT 0,                       -- 交通費
  allowance_family          DECIMAL(15, 2) DEFAULT 0,                       -- 家族手当
  allowance_position        DECIMAL(15, 2) DEFAULT 0,                       -- 役職手当
  allowance_special         DECIMAL(15, 2) DEFAULT 0,                       -- 特別手当
  
  -- 控除項目
  deduction_tax             DECIMAL(15, 2) DEFAULT 0,                       -- 所得税
  deduction_resident_tax    DECIMAL(15, 2) DEFAULT 0,                       -- 住民税
  deduction_insurance       DECIMAL(15, 2) DEFAULT 0,                       -- 社会保険料（合計）
  deduction_pension         DECIMAL(15, 2) DEFAULT 0,                       -- 厚生年金
  deduction_health          DECIMAL(15, 2) DEFAULT 0,                       -- 健康保険
  deduction_employment      DECIMAL(15, 2) DEFAULT 0,                       -- 雇用保険
  deduction_other           DECIMAL(15, 2) DEFAULT 0,                       -- その他控除
  
  -- その他
  bonus                DECIMAL(15, 2) DEFAULT 0,                            -- 賞与（0の場合はNULL扱い）
  adjustment_amount    DECIMAL(15, 2) DEFAULT 0,                            -- 調整額
  
  -- 支給情報
  gross_pay            DECIMAL(15, 2) NOT NULL DEFAULT 0,                   -- 総支給額（基本給＋手当＋賞与）
  total_deduction      DECIMAL(15, 2) NOT NULL DEFAULT 0,                   -- 総控除額
  net_pay              DECIMAL(15, 2) NOT NULL DEFAULT 0,                   -- 手取額（総支給額－総控除額）
  
  -- メタ情報
  currency_code        VARCHAR(3)  NOT NULL DEFAULT 'JPY',                  -- 通貨コード（ISO 4217）
  reversal_flag        BOOLEAN     NOT NULL DEFAULT FALSE,                  -- 逆仕訳フラグ（True: 取消仕訳）
  payment_status       VARCHAR(20) DEFAULT 'PENDING',                       -- 支払ステータス（PENDING/PAID/CANCELLED）
  payment_method       VARCHAR(20) DEFAULT 'BANK_TRANSFER',                 -- 支払方法（BANK_TRANSFER/CASH/CHECK）
  
  update_timestamp     TIMESTAMP NOT NULL                                -- 更新タイムスタンプ
);

-- インデックス
CREATE INDEX idx_hr_payroll_emp     ON hr_payroll_export (employee_id, payroll_period);
CREATE INDEX idx_hr_payroll_period  ON hr_payroll_export (payroll_period, payment_date);
CREATE INDEX idx_hr_payroll_payment ON hr_payroll_export (payment_date, payment_status);

-- 外部キー（論理的な関連）
-- hr_employee_org_export.employee_id とジョイン

-- コメント
COMMENT ON TABLE  hr_payroll_export                        IS '人事システム給与実績エクスポート';
COMMENT ON COLUMN hr_payroll_export.payroll_id             IS '給与ID（一意識別子）';
COMMENT ON COLUMN hr_payroll_export.employee_id            IS '社員ID（hr_employee_org_exportとのジョインキー）';
COMMENT ON COLUMN hr_payroll_export.payroll_period         IS '給与期間（YYYY-MM形式）';
COMMENT ON COLUMN hr_payroll_export.payment_date           IS '支給日';
COMMENT ON COLUMN hr_payroll_export.basic_salary           IS '基本給';
COMMENT ON COLUMN hr_payroll_export.allowance_housing      IS '住宅手当';
COMMENT ON COLUMN hr_payroll_export.allowance_transportation IS '交通費';
COMMENT ON COLUMN hr_payroll_export.deduction_tax          IS '所得税控除';
COMMENT ON COLUMN hr_payroll_export.deduction_insurance    IS '社会保険料控除';
COMMENT ON COLUMN hr_payroll_export.bonus                  IS '賞与（0の場合はNULL扱い）';
COMMENT ON COLUMN hr_payroll_export.reversal_flag          IS '逆仕訳フラグ（True: 取消仕訳、False: 通常仕訳）';
COMMENT ON COLUMN hr_payroll_export.gross_pay              IS '総支給額（基本給＋手当＋賞与）';
COMMENT ON COLUMN hr_payroll_export.net_pay                IS '手取額（総支給額－総控除額）';

