-- ========================================
-- 会計トランザクションインターフェーステーブル
-- ========================================
-- 会計パッケージ向け 統合インターフェース（仕訳相当の受け皿）

CREATE TABLE accounting_txn_interface (
  -- 識別/監査
  interface_id        BIGINT IDENTITY PRIMARY KEY, -- インターフェースID（主キー）
  batch_id            VARCHAR(40)  NOT NULL,                                -- バッチID（取込単位の識別子）
  source_system       VARCHAR(40)  NOT NULL,                                -- 上流システム名（HR:人事/SALE:売上/INV:在庫等）
  source_doc_type     VARCHAR(30)  NOT NULL,                                -- 上流伝票タイプ（受注/請求/入出庫/給与等）
  source_doc_id       VARCHAR(64)  NOT NULL,                                -- 上流ドキュメントID
  source_line_id      VARCHAR(64)  NOT NULL,                                -- 上流ドキュメント明細ID
  event_timestamp     TIMESTAMP    NOT NULL,                                -- 上流イベント発生日時
  load_timestamp      TIMESTAMP    NOT NULL,                                 -- インターフェースへのロード日時
  status_code         VARCHAR(20)  NOT NULL,                                 -- 処理ステータス（READY:準備完了/ERROR:エラー/POSTED:転記済等）
  error_code          VARCHAR(50),                                          -- エラーコード
  error_message       VARCHAR(1000),                                        -- エラーメッセージ

  -- 会社/会計カレンダ/通貨
  ledger_id           VARCHAR(30)  NOT NULL,                                -- 元帳/台帳ID
  legal_entity_id     VARCHAR(30)  NOT NULL,                                -- 法人エンティティID
  business_unit       VARCHAR(30)  NOT NULL,                                -- 事業単位コード
  company_code        VARCHAR(30)  NOT NULL,                                -- 会社コード
  accounting_date     DATE         NOT NULL,                                -- 会計日付
  period_name         VARCHAR(15)  NOT NULL,                                -- 会計期間（例：2025-08）
  journal_category    VARCHAR(30)  NOT NULL,                                -- 仕訳カテゴリ（売上/仕入/経費/資産等）
  currency_code       CHAR(3)      NOT NULL,                                -- 通貨コード（ISO 4217）
  exchange_rate_type  VARCHAR(20),                                          -- 為替レートタイプ（Corporate:会社/Spot:スポット等）
  exchange_rate       NUMERIC(18,6),                                        -- 為替レート

  -- セグメント（柔軟に増やせるように汎化）
  segment_account     VARCHAR(30)  NOT NULL,                                -- セグメント：勘定科目コード
  segment_department  VARCHAR(30),                                          -- セグメント：部門/コストセンタコード
  segment_product     VARCHAR(30),                                          -- セグメント：商品/製品コード
  segment_project     VARCHAR(30),                                          -- セグメント：プロジェクトコード
  segment_interco     VARCHAR(30),                                          -- セグメント：会社間取引コード
  segment_custom1     VARCHAR(30),                                          -- セグメント：カスタム項目1
  segment_custom2     VARCHAR(30),                                          -- セグメント：カスタム項目2

  -- 取引先/サブレジャ
  customer_id         VARCHAR(40),                                          -- 顧客ID（売掛金管理）
  supplier_id         VARCHAR(40),                                          -- 仕入先ID（買掛金管理）
  bank_account_id     VARCHAR(40),                                          -- 銀行口座ID
  invoice_id          VARCHAR(40),                                          -- 請求書ID
  invoice_line_id     VARCHAR(40),                                          -- 請求書明細ID
  po_id               VARCHAR(40),                                          -- 発注書ID
  receipt_id          VARCHAR(40),                                          -- 入庫ID
  asset_id            VARCHAR(40),                                          -- 固定資産ID（資産管理連携）
  project_id          VARCHAR(40),                                          -- プロジェクトID（プロジェクト会計）

  -- 金額/数量（入力通貨と記帳通貨を分離）
  entered_dr          NUMERIC(19,4),                                        -- 入力通貨 借方金額
  entered_cr          NUMERIC(19,4),                                        -- 入力通貨 貸方金額
  accounted_dr        NUMERIC(19,4),                                        -- 記帳通貨 借方金額
  accounted_cr        NUMERIC(19,4),                                        -- 記帳通貨 貸方金額
  quantity            NUMERIC(19,6),                                        -- 数量（在庫等の物量管理用）
  uom_code            VARCHAR(10),                                          -- 単位コード（EA:個/KG:キログラム/L:リットル等）

  -- 税/収益認識
  tax_code            VARCHAR(30),                                          -- 税コード（消費税/源泉税等）
  tax_rate            NUMERIC(9,4),                                         -- 税率
  tax_amount_entered  NUMERIC(19,4),                                        -- 税額（入力通貨）
  revrec_rule_code    VARCHAR(40),                                          -- 収益認識ルールコード（IFRS15/ASC606関連）
  revrec_start_date   DATE,                                                 -- 収益認識開始日
  revrec_end_date     DATE,                                                 -- 収益認識終了日

  -- 説明/参照
  description         VARCHAR(500),                                         -- 摘要・説明
  reference1          VARCHAR(100),                                         -- 参照情報1
  reference2          VARCHAR(100),                                         -- 参照情報2
  reference3          VARCHAR(100),                                         -- 参照情報3
  reference4          VARCHAR(100),                                         -- 参照情報4
  reference5          VARCHAR(100),                                         -- 参照情報5

  -- 取消/反転
  reversal_flag       BOOLEAN,                                              -- 取消/反転フラグ（TRUE:取消仕訳）
  reversed_interface_id BIGINT,                                             -- 取消対象インターフェースID（元仕訳へのリンク）

  -- 作成/更新
  created_by          VARCHAR(60),                                          -- 作成者
  created_at          TIMESTAMP,                                            -- 作成日時
  updated_by          VARCHAR(60),                                          -- 更新者
  updated_at          TIMESTAMP                                             -- 更新日時
);

-- インデックス
CREATE INDEX idx_accounting_txn_interface_batch ON accounting_txn_interface (batch_id);
CREATE INDEX idx_accounting_txn_interface_src   ON accounting_txn_interface (source_system, source_doc_type, source_doc_id, source_line_id);
CREATE INDEX idx_accounting_txn_interface_seg   ON accounting_txn_interface (segment_account, segment_department, segment_project);
CREATE INDEX idx_accounting_txn_interface_date  ON accounting_txn_interface (accounting_date, period_name);

