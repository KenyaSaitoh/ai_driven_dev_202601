// 社員情報（LoginResponseと同じ構造）
export interface Employee {
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  email: string;
  jobRank: number;  // 1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR
  departmentId: number | null;
  departmentName: string | null;
}

// ログインリクエスト
export interface LoginRequest {
  employeeCode: string;
  password: string;
}

// ワークフロー（WorkflowTOと同じ構造）
export interface Workflow {
  operationId: number;
  workflowId: number;
  workflowType: string;  // ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE
  state: string;  // CREATED, APPLIED, APPROVED
  operationType: string;  // CREATE, APPLY, APPROVE, REJECT
  operatedAt: string;  // ISO 8601形式
  operatedBy: number;
  operatorName?: string | null;
  operatorCode?: string | null;
  jobRank?: number | null;
  departmentId?: number | null;
  departmentName?: string | null;
  operationReason?: string | null;
  // ワークフロータイプ別のフィールド
  bookId?: number | null;  // REMOVE_BOOK, ADJUST_BOOK_PRICE用
  bookName?: string | null;  // ADD_NEW_BOOK用
  author?: string | null;  // ADD_NEW_BOOK用
  price?: number | null;  // ADD_NEW_BOOK, ADJUST_BOOK_PRICE用（BigDecimal→number）
  originalPrice?: number | null;  // ADJUST_BOOK_PRICE用の元の価格
  imageUrl?: string | null;  // ADD_NEW_BOOK用
  categoryId?: number | null;  // ADD_NEW_BOOK用
  categoryName?: string | null;
  publisherId?: number | null;  // ADD_NEW_BOOK用
  publisherName?: string | null;
  startDate?: string | null;  // ADJUST_BOOK_PRICE用（YYYY-MM-DD）
  endDate?: string | null;  // ADJUST_BOOK_PRICE用（YYYY-MM-DD）
  applyReason?: string | null;
}

// ワークフロー作成リクエスト
export interface WorkflowCreateRequest {
  workflowType: string;  // ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE
  createdBy: number;  // 社員ID
  // ワークフロータイプに応じて必要なフィールドが変わる
  bookId?: number;
  bookName?: string;
  author?: string;
  price?: number;  // BigDecimalだが、JSONでは数値として扱う
  imageUrl?: string;
  categoryId?: number;
  publisherId?: number;
  startDate?: string;  // YYYY-MM-DD形式
  endDate?: string;  // YYYY-MM-DD形式
  applyReason?: string;
}

// ワークフロー更新リクエスト
export interface WorkflowUpdateRequest {
  updatedBy: number;  // 更新者の社員ID
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
  error: string;
  message: string;
}

