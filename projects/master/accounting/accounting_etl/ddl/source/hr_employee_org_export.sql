-- ========================================
-- 人事システムエクスポートテーブル
-- ========================================
-- HR（人事）→ 会計：部門/コストセンタ、配賦キー、従業員属性 等のマスタ流通を想定

CREATE TABLE hr_employee_org_export (
  export_id            BIGINT IDENTITY PRIMARY KEY, -- エクスポートID（主キー）
  batch_id             VARCHAR(40) NOT NULL,                                -- バッチID（取込単位の識別子）
  effective_start_date DATE        NOT NULL,                                -- 有効開始日
  effective_end_date   DATE        NOT NULL,                                -- 有効終了日

  employee_id          VARCHAR(30) NOT NULL,                                -- 社員ID（内部管理用）
  employee_number      VARCHAR(30) NOT NULL,                                -- 社員番号（表示用・人事コード）
  last_name            VARCHAR(60) NOT NULL,                                -- 姓
  first_name           VARCHAR(60) NOT NULL,                                -- 名
  last_name_kana       VARCHAR(60),                                         -- 姓（カナ）
  first_name_kana      VARCHAR(60),                                         -- 名（カナ）
  email                VARCHAR(120),                                        -- メールアドレス
  phone                VARCHAR(30),                                         -- 電話番号

  employment_type      VARCHAR(20),                                         -- 雇用形態（正社員/契約社員/派遣社員等）
  status               VARCHAR(20),                                         -- ステータス（Active/Inactive等）
  hire_date            DATE,                                                -- 入社日
  termination_date     DATE,                                                -- 退職日

  org_code             VARCHAR(30) NOT NULL,                                -- 組織コード（上位組織）
  dept_code            VARCHAR(30) NOT NULL,                                -- 部門コード（会計部門と1:1推奨）
  cost_center_code     VARCHAR(30),                                         -- コストセンタコード
  location_code        VARCHAR(30),                                         -- 勤務地コード
  manager_employee_id  VARCHAR(30),                                         -- 上司社員ID

  job_code             VARCHAR(30),                                         -- 職種コード
  job_name             VARCHAR(100),                                        -- 職種名
  grade_code           VARCHAR(20),                                         -- 等級コード
  payroll_group        VARCHAR(30),                                         -- 給与グループ

  allocation_rule_code VARCHAR(30),                                         -- 配賦ルールコード（複数部門への配賦比率セット）
  tax_region_code      VARCHAR(20),                                         -- 課税地域コード
  social_insurance_id  VARCHAR(40),                                         -- 社会保険番号

  bank_account_no      VARCHAR(34),                                         -- 銀行口座番号
  bank_branch_code     VARCHAR(15),                                         -- 銀行支店コード
  payment_method       VARCHAR(20),                                         -- 支払方法（振込/現金等）

  update_timestamp     TIMESTAMP NOT NULL                                -- 更新タイムスタンプ
);

-- インデックス
CREATE INDEX idx_hr_emp_eff ON hr_employee_org_export (employee_id, effective_start_date, effective_end_date);
CREATE INDEX idx_hr_dept    ON hr_employee_org_export (dept_code, cost_center_code);
