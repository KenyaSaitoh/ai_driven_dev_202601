// ==================== User & Auth ====================

export interface User {
  customerId: number;
  customerName: string;
  email: string;
  birthday?: string;
  address?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  customerName: string;
  email: string;
  password: string;
  birthday?: string;
  address?: string;
}

export interface LoginResponse {
  customerId: number;
  customerName: string;
  email: string;
  birthday?: string;
  address?: string;
}

// ==================== Book ====================

export interface Book {
  bookId: number;
  bookName: string;
  author: string;
  price: number;
  quantity: number;
  version: number;
  category: Category;
  publisher: Publisher;
}

export interface Category {
  categoryId: number;
  categoryName: string;
}

export interface Publisher {
  publisherId: number;
  publisherName: string;
}

export interface CategoryMap {
  [key: string]: number;
}

// ==================== Cart ====================

export interface CartItem {
  bookId: number;
  bookName: string;
  publisherName: string;
  price: number;
  count: number;
  version: number;
}

export interface CartItemRequest {
  bookId: number;
  bookName: string;
  publisherName: string;
  price: number;
  count: number;
  version: number;
}

// ==================== Order ====================

export interface OrderRequest {
  cartItems: CartItemRequest[];
  totalPrice: number;
  deliveryPrice: number;
  deliveryAddress: string;
  settlementType: number;
}

export interface OrderResponse {
  orderTranId: number;
  orderDate: string;
  totalPrice: number;
  deliveryPrice: number;
  deliveryAddress: string;
  settlementType: number;
  orderDetails: OrderDetailResponse[];
}

export interface OrderDetailResponse {
  orderDetailId: number;
  bookId: number;
  bookName: string;
  publisherName: string;
  price: number;
  count: number;
}

export interface OrderHistoryResponse {
  orderDate: string;
  orderTranId: number;
  orderDetailId: number;
  bookName: string;
  publisherName: string;
  price: number;
  count: number;
}

// ==================== Error ====================

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
}

// ==================== Context ====================

export interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
}

export interface CartContextType {
  items: CartItem[];
  totalPrice: number;
  itemCount: number;
  addItem: (book: Book, count?: number) => void;
  removeItem: (bookId: number) => void;
  updateItemCount: (bookId: number, count: number) => void;
  clearCart: () => void;
}

