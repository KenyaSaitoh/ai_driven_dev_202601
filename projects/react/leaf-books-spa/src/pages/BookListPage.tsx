import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import BookCard from '../components/BookCard';
import { Book } from '../types';
import { bookService } from '../services/bookService';

const BookListPage: React.FC = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadBooks();
  }, []);

  const loadBooks = async () => {
    try {
      setLoading(true);
      const data = await bookService.getAllBooks();
      setBooks(data);
    } catch (err: any) {
      setError('書籍の読み込みに失敗しました');
      console.error('Failed to load books:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center min-h-96">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-gray-600">読み込み中...</p>
          </div>
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="text-center py-12">
          <p className="error-text text-lg">{error}</p>
          <button onClick={loadBooks} className="btn-primary mt-4">
            再読み込み
          </button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <h2 className="text-4xl font-bold text-primary-darker text-center mb-6">
        書籍を買い物カゴに入れてください
      </h2>
      <hr className="mb-8 border-t-2 border-primary opacity-50" />

      {books.length === 0 ? (
        <p className="text-center text-gray-600 py-12">書籍がありません</p>
      ) : (
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
      )}
    </Layout>
  );
};

export default BookListPage;

