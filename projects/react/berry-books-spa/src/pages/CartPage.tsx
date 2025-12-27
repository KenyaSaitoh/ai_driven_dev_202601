import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { useCart } from '../contexts/CartContext';
import { useAuth } from '../contexts/AuthContext';
import { bookService } from '../services/bookService';

const CartPage: React.FC = () => {
  const navigate = useNavigate();
  const { items, totalPrice, removeItem, clearCart } = useCart();
  const { user } = useAuth();
  const [selectedItems, setSelectedItems] = useState<Set<number>>(new Set());

  const deliveryPrice = 500; // 配送料

  const handleToggleSelect = (bookId: number) => {
    const newSelected = new Set(selectedItems);
    if (newSelected.has(bookId)) {
      newSelected.delete(bookId);
    } else {
      newSelected.add(bookId);
    }
    setSelectedItems(newSelected);
  };

  const handleRemoveSelected = () => {
    if (selectedItems.size === 0) {
      alert('削除する書籍を選択してください');
      return;
    }

    if (window.confirm(`選択した${selectedItems.size}件の書籍をカートから削除しますか？`)) {
      selectedItems.forEach((bookId) => removeItem(bookId));
      setSelectedItems(new Set());
    }
  };

  const handleClear = () => {
    if (window.confirm('カートの中身をすべて削除しますか？')) {
      clearCart();
      setSelectedItems(new Set());
    }
  };

  const handleOrder = () => {
    if (items.length === 0) {
      alert('カートが空です');
      return;
    }

    if (!user) {
      alert('ログインしてください');
      navigate('/');
      return;
    }

    // 注文確認ページに遷移
    navigate('/orders/confirm');
  };

  const [imageErrors, setImageErrors] = useState<Set<number>>(new Set());

  const handleImageError = (bookId: number) => {
    setImageErrors((prev) => new Set(prev).add(bookId));
  };

  return (
    <Layout>
      <h2 className="text-4xl font-bold text-primary-darker text-center mb-6">
        現在の買い物カゴの内容です
      </h2>
      <hr className="mb-8 border-t-2 border-primary opacity-50" />

      {items.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600 text-lg mb-6">カートは空です</p>
          <button onClick={() => navigate('/books')} className="btn-primary">
            書籍一覧へ
          </button>
        </div>
      ) : (
        <>
          <table className="table-primary mb-6">
            <thead>
              <tr>
                <th className="w-32">画像</th>
                <th>書籍名</th>
                <th className="w-24 text-center">注文数</th>
                <th className="w-32 text-right">価格</th>
                <th className="w-20 text-center">削除</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.bookId}>
                  <td className="text-center py-2">
                    <img
                      src={
                        imageErrors.has(item.bookId)
                          ? '/api/images/covers/no-image'
                          : bookService.getBookCoverUrl(item.bookId)
                      }
                      alt={item.bookName}
                      onError={() => handleImageError(item.bookId)}
                      className="book-thumbnail"
                    />
                  </td>
                  <td className="px-4 py-3">{item.bookName}</td>
                  <td className="px-4 py-3 text-center">{item.count}</td>
                  <td className="px-4 py-3 text-right">{item.price.toLocaleString()}</td>
                  <td className="px-4 py-3 text-center">
                    <input
                      type="checkbox"
                      checked={selectedItems.has(item.bookId)}
                      onChange={() => handleToggleSelect(item.bookId)}
                      className="w-5 h-5 accent-primary cursor-pointer"
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="bg-accent-light p-6 rounded-lg mb-6">
            <div className="flex justify-between items-center mb-2">
              <span className="text-lg font-semibold text-primary-darker">注文金額合計</span>
              <span className="text-xl font-bold text-primary-darker">
                ¥{totalPrice.toLocaleString()}
              </span>
            </div>
            <div className="flex justify-between items-center mb-2">
              <span className="text-lg font-semibold text-primary-darker">配送料</span>
              <span className="text-xl font-bold text-primary-darker">
                ¥{deliveryPrice.toLocaleString()}
              </span>
            </div>
            <hr className="my-3 border-primary" />
            <div className="flex justify-between items-center">
              <span className="text-xl font-bold text-primary-darker">合計</span>
              <span className="text-xl font-bold text-primary-darker">
                ¥{(totalPrice + deliveryPrice).toLocaleString()}
              </span>
            </div>
          </div>

          <div className="flex flex-wrap gap-4">
            <button onClick={() => navigate('/books')} className="btn-primary">
              買い物を続ける
            </button>
            <button onClick={handleRemoveSelected} className="btn-primary">
              選択した書籍をカートから削除する
            </button>
            <button onClick={handleClear} className="btn-primary">
              買い物カゴをクリアする
            </button>
            <button onClick={handleOrder} className="btn-primary">
              買い物を終了し注文する
            </button>
          </div>
        </>
      )}
    </Layout>
  );
};

export default CartPage;

