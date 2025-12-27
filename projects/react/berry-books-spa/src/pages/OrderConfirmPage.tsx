import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { useCart } from '../contexts/CartContext';
import { useAuth } from '../contexts/AuthContext';
import { orderService } from '../services/orderService';

const OrderConfirmPage: React.FC = () => {
  const navigate = useNavigate();
  const { items, totalPrice, clearCart } = useCart();
  const { user } = useAuth();
  
  const [deliveryAddress, setDeliveryAddress] = useState<string>('');
  const [settlementType, setSettlementType] = useState<number>(1);
  const [ordering, setOrdering] = useState(false);
  const [error, setError] = useState<string>('');
  const [addressError, setAddressError] = useState<string>('');
  const [settlementError, setSettlementError] = useState<string>('');

  const deliveryPrice = 500;

  useEffect(() => {
    // カートが空の場合は書籍一覧にリダイレクト
    if (items.length === 0) {
      navigate('/books', { replace: true });
      return;
    }

    // 初期値を設定
    if (user?.address) {
      setDeliveryAddress(user.address);
    }
  }, [items.length, navigate, user]);

  const validateForm = (): boolean => {
    let isValid = true;
    setAddressError('');
    setSettlementError('');

    if (!deliveryAddress.trim()) {
      setAddressError('配送先住所を入力してください');
      isValid = false;
    }

    if (!settlementType) {
      setSettlementError('決済方法を選択してください');
      isValid = false;
    }

    return isValid;
  };

  const handleOrder = async () => {
    if (!validateForm()) {
      return;
    }

    if (!window.confirm('注文します。よろしいですか？')) {
      return;
    }

    setError('');
    setOrdering(true);

    try {
      const orderRequest = {
        cartItems: items.map((item) => ({
          bookId: item.bookId,
          bookName: item.bookName,
          publisherName: item.publisherName,
          price: item.price,
          count: item.count,
          version: item.version,
        })),
        totalPrice: totalPrice + deliveryPrice,
        deliveryPrice,
        deliveryAddress: deliveryAddress.trim(),
        settlementType,
      };

      const orderResponse = await orderService.createOrder(orderRequest);
      clearCart();
      
      // 注文成功ページに遷移（注文情報を渡す）
      navigate('/orders/success', { 
        state: { orderResponse },
        replace: true 
      });
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || '注文に失敗しました';
      setError(errorMessage);
      console.error('Order failed:', err);
    } finally {
      setOrdering(false);
    }
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <Layout>
      <h2 className="text-4xl font-bold text-primary-darker text-center mb-6">
        以下の内容で注文します
      </h2>
      <hr className="mb-8 border-t-2 border-primary opacity-50" />

      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow-lg p-8 mb-6">
          {/* 顧客名 */}
          <div className="mb-6 pb-6 border-b border-gray-200">
            <div className="grid grid-cols-3 gap-4">
              <div className="font-semibold text-primary-darker">顧客名</div>
              <div className="col-span-2 text-lg">{user?.customerName} 様</div>
            </div>
          </div>

          {/* 注文内容 */}
          <div className="mb-6 pb-6 border-b border-gray-200">
            <div className="grid grid-cols-3 gap-4">
              <div className="font-semibold text-primary-darker">注文内容</div>
              <div className="col-span-2">
                <table className="w-full border border-gray-300">
                  <thead className="bg-accent-light">
                    <tr>
                      <th className="px-4 py-2 text-left border-b border-gray-300">書籍名</th>
                      <th className="px-4 py-2 text-right border-b border-gray-300 w-32">金額</th>
                      <th className="px-4 py-2 text-center border-b border-gray-300 w-24">注文数</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((item) => (
                      <tr key={item.bookId} className="border-b border-gray-200">
                        <td className="px-4 py-2">{item.bookName}</td>
                        <td className="px-4 py-2 text-right">¥{item.price.toLocaleString()}</td>
                        <td className="px-4 py-2 text-center">{item.count}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* 注文金額合計 */}
          <div className="mb-6 pb-6 border-b border-gray-200">
            <div className="grid grid-cols-3 gap-4">
              <div className="font-semibold text-primary-darker">注文金額合計</div>
              <div className="col-span-2 text-xl font-bold text-primary">
                ¥{totalPrice.toLocaleString()}
              </div>
            </div>
          </div>

          {/* 配送料金 */}
          <div className="mb-6 pb-6 border-b border-gray-200">
            <div className="grid grid-cols-3 gap-4">
              <div className="font-semibold text-primary-darker">配送料金</div>
              <div className="col-span-2 text-xl font-bold text-primary">
                ¥{deliveryPrice.toLocaleString()}
              </div>
            </div>
          </div>

          {/* 配送先住所 */}
          <div className="mb-6 pb-6 border-b border-gray-200">
            <div className="grid grid-cols-3 gap-4">
              <div className="font-semibold text-primary-darker">
                配送先住所 <span className="text-error">*</span>
              </div>
              <div className="col-span-2">
                <input
                  type="text"
                  value={deliveryAddress}
                  onChange={(e) => setDeliveryAddress(e.target.value)}
                  maxLength={40}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                  placeholder="配送先住所を入力してください"
                />
                {addressError && (
                  <p className="error-text mt-2">{addressError}</p>
                )}
              </div>
            </div>
          </div>

          {/* 決済方法 */}
          <div className="mb-6">
            <div className="grid grid-cols-3 gap-4">
              <div className="font-semibold text-primary-darker">
                決済方法 <span className="text-error">*</span>
              </div>
              <div className="col-span-2">
                <div className="flex gap-4">
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="radio"
                      value={1}
                      checked={settlementType === 1}
                      onChange={(e) => setSettlementType(Number(e.target.value))}
                      className="w-4 h-4 text-primary accent-primary cursor-pointer mr-2"
                    />
                    <span className="text-gray-700">銀行振り込み</span>
                  </label>
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="radio"
                      value={2}
                      checked={settlementType === 2}
                      onChange={(e) => setSettlementType(Number(e.target.value))}
                      className="w-4 h-4 text-primary accent-primary cursor-pointer mr-2"
                    />
                    <span className="text-gray-700">クレジットカード</span>
                  </label>
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="radio"
                      value={3}
                      checked={settlementType === 3}
                      onChange={(e) => setSettlementType(Number(e.target.value))}
                      className="w-4 h-4 text-primary accent-primary cursor-pointer mr-2"
                    />
                    <span className="text-gray-700">着払い</span>
                  </label>
                </div>
                {settlementError && (
                  <p className="error-text mt-2">{settlementError}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* エラーメッセージ（注文ボタンの直前） */}
        {error && (
          <div className="mb-4 p-4 bg-error-light border border-error rounded-lg">
            <p className="error-text text-lg">{error}</p>
          </div>
        )}

        {/* ボタン */}
        <div className="space-y-4">
          {/* 注文ボタン（方式1と方式2） */}
          <div className="flex gap-4">
            <button
              onClick={handleOrder}
              disabled={ordering}
              className="btn-primary flex-1"
              id="orderButton1"
            >
              {ordering ? '注文処理中...' : '注文する（方式1）'}
            </button>
            <button
              onClick={handleOrder}
              disabled={ordering}
              className="btn-primary flex-1"
              id="orderButton2"
            >
              {ordering ? '注文処理中...' : '注文する（方式2）'}
            </button>
          </div>
          
          {/* 買い物を続けるリンク */}
          <div>
            <button
              onClick={() => navigate('/books')}
              className="text-primary-dark hover:text-primary-darker font-semibold transition-colors"
              id="continueLink"
            >
              買い物を続ける
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default OrderConfirmPage;


