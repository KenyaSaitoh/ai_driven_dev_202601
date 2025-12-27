import api from './api';
import { Book, CategoryMap } from '../types';

export const bookService = {
  // 書籍一覧取得
  getAllBooks: async (): Promise<Book[]> => {
    const response = await api.get<Book[]>('/books');
    return response.data;
  },

  // 書籍詳細取得
  getBookById: async (id: number): Promise<Book> => {
    const response = await api.get<Book>(`/books/${id}`);
    return response.data;
  },

  // 書籍検索（静的クエリ - JPQL）
  searchBooksJpql: async (categoryId?: number, keyword?: string): Promise<Book[]> => {
    const params: { categoryId?: number; keyword?: string } = {};
    if (categoryId !== undefined && categoryId !== 0) {
      params.categoryId = categoryId;
    }
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await api.get<Book[]>('/books/search/jpql', { params });
    return response.data;
  },

  // 書籍検索（動的クエリ - Criteria API）
  searchBooksCriteria: async (categoryId?: number, keyword?: string): Promise<Book[]> => {
    const params: { categoryId?: number; keyword?: string } = {};
    if (categoryId !== undefined && categoryId !== 0) {
      params.categoryId = categoryId;
    }
    if (keyword) {
      params.keyword = keyword;
    }
    
    const response = await api.get<Book[]>('/books/search/criteria', { params });
    return response.data;
  },

  // 書籍検索（デフォルト - 後方互換性）
  searchBooks: async (categoryId?: number, keyword?: string): Promise<Book[]> => {
    return bookService.searchBooksJpql(categoryId, keyword);
  },

  // カテゴリ一覧取得
  getAllCategories: async (): Promise<CategoryMap> => {
    const response = await api.get<CategoryMap>('/books/categories');
    return response.data;
  },

  // 書籍表紙画像URL取得
  getBookCoverUrl: (bookId: number): string => {
    return `/api/images/covers/${bookId}`;
  },
};

