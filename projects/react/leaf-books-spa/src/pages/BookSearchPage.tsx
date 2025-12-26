import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import BookCard from '../components/BookCard';
import { Book, CategoryMap } from '../types';
import { bookService } from '../services/bookService';

const BookSearchPage: React.FC = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [categories, setCategories] = useState<CategoryMap>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');

  const [categoryId, setCategoryId] = useState<number>(0);
  const [keyword, setKeyword] = useState<string>('');

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const data = await bookService.getAllCategories();
      setCategories(data);
    } catch (err: any) {
      console.error('Failed to load categories:', err);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      setLoading(true);
      const data = await bookService.searchBooks(
        categoryId === 0 ? undefined : categoryId,
        keyword || undefined
      );
      setBooks(data);
    } catch (err: any) {
      setError('検索に失敗しました');
      console.error('Failed to search books:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <h2 className="text-4xl font-bold text-primary-darker text-center mb-6">
        条件を入力して書籍を検索してください
      </h2>
      <hr className="mb-8 border-t-2 border-primary opacity-50" />

      <form onSubmit={handleSearch} className="max-w-3xl mx-auto bg-white p-10 rounded-xl mb-8">
        <table className="w-full">
          <tbody>
            <tr>
              <td className="w-48 py-3 font-semibold text-primary-darker">カテゴリ</td>
              <td className="py-3">
                <select
                  value={categoryId}
                  onChange={(e) => setCategoryId(Number(e.target.value))}
                  className="form-input"
                >
                  <option value={0}>すべて</option>
                  {Object.entries(categories).map(([name, id]) => (
                    <option key={id} value={id}>
                      {name}
                    </option>
                  ))}
                </select>
              </td>
            </tr>
            <tr>
              <td className="py-3 font-semibold text-primary-darker">検索キーワード</td>
              <td className="py-3">
                <input
                  type="text"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  className="form-input"
                  placeholder="書籍名や著者名で検索"
                />
              </td>
            </tr>
          </tbody>
        </table>

        {error && (
          <div className="mt-4 p-3 bg-error-light border border-error rounded-lg">
            <p className="error-text">{error}</p>
          </div>
        )}

        <button type="submit" className="btn-primary mt-6" disabled={loading}>
          {loading ? '検索中...' : '検索実行'}
        </button>
      </form>

      {loading && (
        <div className="flex items-center justify-center min-h-96">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-gray-600">検索中...</p>
          </div>
        </div>
      )}

      {!loading && books.length > 0 && (
        <div>
          <h3 className="text-2xl font-bold text-primary-darker mb-4">検索結果: {books.length}件</h3>
          <table className="table-primary">
            <thead>
              <tr>
                <th className="w-32">画像</th>
                <th>書籍名</th>
                <th className="w-48">著者</th>
                <th className="w-32">カテゴリ</th>
                <th className="w-48">出版社</th>
                <th className="w-28 text-right">価格</th>
                <th className="w-24 text-center">在庫数</th>
                <th className="w-40"></th>
              </tr>
            </thead>
            <tbody>
              {books.map((book) => (
                <BookCard key={book.bookId} book={book} />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!loading && books.length === 0 && categoryId === 0 && keyword === '' && (
        <p className="text-center text-gray-600 py-12">検索条件を入力して検索してください</p>
      )}

      {!loading && books.length === 0 && (categoryId !== 0 || keyword !== '') && (
        <p className="text-center text-gray-600 py-12">該当する書籍が見つかりませんでした</p>
      )}
    </Layout>
  );
};

export default BookSearchPage;

