-- ========================================
-- 売上トランザクションエクスポートテーブル
-- ========================================
-- 受注～出荷～売上/請求～入金に関わるトランザクションの会計転記用エクスポート

CREATE TABLE sales_txn_export (
  export_id            BIGINT IDENTITY PRIMARY KEY, -- エクスポートID（主キー）
  batch_id             VARCHAR(40) NOT NULL,                                -- バッチID（取込単位の識別子）
  source_txn_id        VARCHAR(40) NOT NULL,                                -- 原票トランザクションID（受注/請求等）
  source_line_id       VARCHAR(40) NOT NULL,                                -- 原票明細ID
  txn_type             VARCHAR(20) NOT NULL,                                -- トランザクションタイプ（ORDER:受注/SHIP:出荷/INVOICE:請求/CM:返品/PMT:入金等）
  txn_status           VARCHAR(20) NOT NULL,                                -- トランザクションステータス（OPEN:進行中/CLOSED:完了/CANCELLED:取消等）
  event_timestamp      TIMESTAMP   NOT NULL,                                -- イベント発生日時

  order_id             VARCHAR(40),                                         -- 受注ID
  order_date           DATE,                                                -- 受注日
  invoice_id           VARCHAR(40),                                         -- 請求ID
  invoice_date         DATE,                                                -- 請求日
  shipment_id          VARCHAR(40),                                         -- 出荷ID
  shipment_date        DATE,                                                -- 出荷日

  customer_code        VARCHAR(40) NOT NULL,                                -- 顧客コード
  bill_to_site         VARCHAR(60),                                         -- 請求先サイト
  ship_to_site         VARCHAR(60),                                         -- 出荷先サイト
  salesperson_code     VARCHAR(30),                                         -- 営業担当者コード
  campaign_code        VARCHAR(30),                                         -- キャンペーンコード

  product_code         VARCHAR(40) NOT NULL,                                -- 商品コード
  product_name         VARCHAR(200),                                        -- 商品名
  quantity_ordered     NUMERIC(19,6),                                       -- 受注数量
  quantity_shipped     NUMERIC(19,6),                                       -- 出荷数量
  uom_code             VARCHAR(10),                                         -- 単位コード（EA/KG/L等）

  unit_price           NUMERIC(19,6),                                       -- 単価
  discount_amount      NUMERIC(19,4),                                       -- 値引額
  tax_code             VARCHAR(30),                                         -- 税コード
  tax_rate             NUMERIC(9,4),                                        -- 税率
  tax_amount           NUMERIC(19,4),                                       -- 税額
  gross_amount         NUMERIC(19,4),                                       -- 総額（税込/税抜は運用定義による）
  net_amount           NUMERIC(19,4),                                       -- 純額

  currency_code        CHAR(3) NOT NULL,                                    -- 通貨コード（ISO 4217）
  exchange_rate        NUMERIC(18,6),                                       -- 為替レート

  payment_term_code    VARCHAR(20),                                         -- 支払条件コード
  due_date             DATE,                                                -- 支払期限日

  revrec_rule_code     VARCHAR(40),                                         -- 収益認識ルールコード（IFRS15/ASC606関連）
  return_flag          BOOLEAN,                                             -- 返品フラグ
  cancel_flag          BOOLEAN,                                             -- キャンセルフラグ
  cancel_reason        VARCHAR(200),                                        -- キャンセル理由

  reference1           VARCHAR(100),                                        -- 参照情報1
  reference2           VARCHAR(100),                                        -- 参照情報2
  update_timestamp     TIMESTAMP NOT NULL                                -- 更新タイムスタンプ
);

-- インデックス
CREATE INDEX idx_sales_src  ON sales_txn_export (source_txn_id, source_line_id);
CREATE INDEX idx_sales_cust ON sales_txn_export (customer_code, invoice_id, due_date);
CREATE INDEX idx_sales_prod ON sales_txn_export (product_code);
