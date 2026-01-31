import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import Layout from '../components/Layout';
import { Book } from '../types';
import { bookService } from '../services/bookService';

const CartAddedPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const book = location.state?.addedBook as Book | undefined;
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    // bookが存在しない場合は書籍一覧にリダイレクト
    if (!book) {
      navigate('/books', { replace: true });
    }
  }, [book, navigate]);

  if (!book) {
    return null;
  }

  return (
    <Layout>
      <div className="max-w-3xl mx-auto">
        <div className="bg-white rounded-lg shadow-lg p-8">
          {/* 成功アイコンとメッセージ */}
          <div className="flex items-center justify-center gap-3 mb-6">
            <div className="w-12 h-12 bg-primary rounded-full flex items-center justify-center flex-shrink-0">
              <svg
                className="w-8 h-8 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-primary-darker">
              カートに入れました
            </h2>
          </div>

          {/* 書籍情報 */}
          <div className="bg-accent-lighter rounded-lg p-6 mb-6">
            <div className="flex gap-6 items-start">
              {/* 書籍カバー画像（左） */}
              <div className="flex-shrink-0">
                <img
                  src={
                    imageError
                      ? '/api/images/covers/no-image'
                      : bookService.getBookCoverUrl(book.bookId)
                  }
                  alt={book.bookName}
                  onError={() => setImageError(true)}
                  className="w-32 h-auto rounded shadow-md"
                />
              </div>
              
              {/* 書籍情報（右） */}
              <div className="flex-1">
                <p className="text-lg font-semibold text-gray-800 mb-3">
                  {book.bookName}
                </p>
                <p className="text-gray-600 mb-2">著者: {book.author}</p>
                <p className="text-gray-600 mb-2">出版社: {book.publisher.publisherName}</p>
                <p className="text-xl font-bold text-primary mt-4">
                  ¥{book.price.toLocaleString()}
                </p>
              </div>
            </div>
          </div>

          {/* ボタン */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button
              onClick={() => navigate('/cart')}
              className="btn-primary px-8 py-3"
            >
              買い物カゴを見る
            </button>
            <button
              onClick={() => navigate('/books')}
              className="btn-primary px-8 py-3"
            >
              買い物を続ける
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default CartAddedPage;


