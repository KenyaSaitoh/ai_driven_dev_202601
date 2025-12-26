-- ========================================
-- 在庫移動エクスポートテーブル
-- ========================================
-- 在庫入出庫/評価差異/棚差を会計化するためのエクスポート

CREATE TABLE inv_movement_export (
  export_id            BIGINT IDENTITY PRIMARY KEY, -- エクスポートID（主キー）
  batch_id             VARCHAR(40) NOT NULL,                                -- バッチID（取込単位の識別子）
  movement_id          VARCHAR(40) NOT NULL,                                -- 在庫移動トランザクションID
  movement_line_id     VARCHAR(40) NOT NULL,                                -- 在庫移動明細ID
  movement_type        VARCHAR(30) NOT NULL,                                -- 移動タイプ（RCV:入庫/ISS:出庫/TRF:振替/ADJ:調整/CNT:棚卸/CST:原価差異）
  movement_status      VARCHAR(20) NOT NULL,                                -- 移動ステータス（POSTED:確定/PENDING:保留等）
  movement_timestamp   TIMESTAMP   NOT NULL,                                -- 移動発生日時

  item_code            VARCHAR(40) NOT NULL,                                -- 品目コード
  item_description     VARCHAR(200),                                        -- 品目名
  lot_no               VARCHAR(60),                                         -- ロット番号
  serial_no            VARCHAR(60),                                         -- シリアル番号

  inventory_org        VARCHAR(30) NOT NULL,                                -- 在庫組織コード
  subinventory_code    VARCHAR(30),                                         -- サブ在庫コード
  locator_code         VARCHAR(60),                                         -- ロケーションコード（倉庫内位置）
  location_code        VARCHAR(30),                                         -- 事業所/倉庫コード

  quantity             NUMERIC(19,6) NOT NULL,                              -- 数量
  uom_code             VARCHAR(10),                                         -- 単位コード（EA/KG/L等）
  unit_cost            NUMERIC(19,6),                                       -- 記録単価
  cost_method          VARCHAR(20),                                         -- 原価計算方法（STD:標準/AVG:平均/FIFO:先入先出等）
  std_cost             NUMERIC(19,6),                                       -- 標準原価
  avg_cost             NUMERIC(19,6),                                       -- 平均原価
  variance_amount      NUMERIC(19,4),                                       -- 差異金額（標準差異/棚差等）

  source_doc_type      VARCHAR(20),                                         -- 発生元伝票タイプ（PO:発注/SO:受注/WIP:製造/ADJ:調整等）
  source_doc_id        VARCHAR(40),                                         -- 発生元伝票ID
  wip_job_id           VARCHAR(40),                                         -- 製造オーダーID
  project_code         VARCHAR(40),                                         -- プロジェクトコード

  currency_code        CHAR(3),                                             -- 通貨コード（ISO 4217）
  exchange_rate        NUMERIC(18,6),                                       -- 為替レート

  reason_code          VARCHAR(30),                                         -- 理由コード
  reference1           VARCHAR(100),                                        -- 参照情報1
  reference2           VARCHAR(100),                                        -- 参照情報2
  update_timestamp     TIMESTAMP NOT NULL                                -- 更新タイムスタンプ
);

-- インデックス
CREATE INDEX idx_inv_move_src  ON inv_movement_export (movement_id, movement_line_id);
CREATE INDEX idx_inv_move_item ON inv_movement_export (item_code, lot_no, serial_no);
CREATE INDEX idx_inv_move_loc  ON inv_movement_export (inventory_org, subinventory_code, locator_code);
