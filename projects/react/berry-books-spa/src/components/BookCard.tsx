import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Book } from '../types';
import { useCart } from '../contexts/CartContext';
import { bookService } from '../services/bookService';

interface BookCardProps {
  book: Book;
}

const BookCard: React.FC<BookCardProps> = ({ book }) => {
  const { addItem } = useCart();
  const navigate = useNavigate();
  const [imageError, setImageError] = useState(false);

  const handleAddToCart = () => {
    addItem(book);
    // カート追加確認ページに遷移
    navigate('/cart/added', { state: { addedBook: book } });
  };

  const coverUrl = bookService.getBookCoverUrl(book.bookId);
  const fallbackImage = '/api/images/covers/no-image';

  return (
    <tr className="hover:bg-accent-lighter transition-colors duration-200">
      <td className="text-center py-2">
        <img
          src={imageError ? fallbackImage : coverUrl}
          alt={book.bookName}
          onError={() => setImageError(true)}
          className="book-thumbnail"
        />
      </td>
      <td className="px-4 py-3">{book.bookName}</td>
      <td className="px-4 py-3">{book.author}</td>
      <td className="px-4 py-3">{book.category.categoryName}</td>
      <td className="px-4 py-3">{book.publisher.publisherName}</td>
      <td className="px-4 py-3 text-right">{book.price.toLocaleString()}</td>
      <td className="px-4 py-3 text-center">{book.quantity}</td>
      <td className="px-4 py-3 text-center">
        {book.quantity > 0 ? (
          <button
            onClick={handleAddToCart}
            className="btn-primary-sm"
          >
            買い物カゴへ
          </button>
        ) : (
          <span className="out-of-stock">入荷待ち</span>
        )}
      </td>
    </tr>
  );
};

export default BookCard;

