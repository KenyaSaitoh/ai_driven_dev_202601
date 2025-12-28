// 社員情報
export interface Employee {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  email: string;
  jobRank: number;  // 1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR
  departmentId: number;
  departmentName: string;
}

// ログインリクエスト
export interface LoginRequest {
  employeeCode: string;
  password: string;
}

// ワークフロー
export interface Workflow {
  operationId: number;
  workflowId: number;
  workflowType: string;  // CREATE, DELETE, PRICE_TEMP_ADJUSTMENT
  state: string;  // CREATED, APPLIED, APPROVED
  operationType: string;  // CREATE, APPLY, APPROVE, REJECT
  operatedAt: string;
  operatedBy: number;
  operatorName?: string;
  operatorCode?: string;
  jobRank?: number;
  departmentId?: number;
  departmentName?: string;
  operationReason?: string;
  // ワークフロータイプ別のフィールド
  bookId?: number;  // DELETE, PRICE_TEMP_ADJUSTMENT用
  bookName?: string;  // CREATE用
  author?: string;  // CREATE用
  price?: number;  // CREATE, PRICE_TEMP_ADJUSTMENT用
  imageUrl?: string;  // CREATE用
  categoryId?: number;  // CREATE用
  categoryName?: string;
  publisherId?: number;  // CREATE用
  publisherName?: string;
  startDate?: string;  // PRICE_TEMP_ADJUSTMENT用（YYYY-MM-DD）
  endDate?: string;  // PRICE_TEMP_ADJUSTMENT用（YYYY-MM-DD）
  applyReason?: string;
}

// ワークフロー作成リクエスト
export interface WorkflowCreateRequest {
  workflowType: string;  // CREATE, DELETE, PRICE_TEMP_ADJUSTMENT
  createdBy: number;  // 社員ID
  // ワークフロータイプに応じて必要なフィールドが変わる
  bookId?: number;
  bookName?: string;
  author?: string;
  price?: number;
  imageUrl?: string;
  categoryId?: number;
  publisherId?: number;
  startDate?: string;
  endDate?: string;
  applyReason?: string;
}

// ワークフロー操作リクエスト
export interface WorkflowOperationRequest {
  operatedBy: number;  // 操作者の社員ID
  operationReason?: string;  // 操作理由（差戻時は必須）
}

// カテゴリ
export interface Category {
  categoryId: number;
  categoryName: string;
}

// カテゴリマップ（バックエンドが返す形式）
export interface CategoryMap {
  [key: string]: number;  // { "Java": 1, "SpringBoot": 2 }
}

// 出版社
export interface Publisher {
  publisherId: number;
  publisherName: string;
}

// 書籍
export interface Book {
  bookId: number;
  bookName: string;
  author: string;
  price: number;
  imageUrl?: string;
  quantity?: number;
  version?: number;
  category: {
    categoryId: number;
    categoryName: string;
  };
  publisher: {
    publisherId: number;
    publisherName: string;
  };
}

// エラーレスポンス
export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
}

