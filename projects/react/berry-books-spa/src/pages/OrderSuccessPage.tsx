import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import Layout from '../components/Layout';
import { OrderResponse } from '../types';

const OrderSuccessPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const orderResponse = location.state?.orderResponse as OrderResponse | undefined;

  useEffect(() => {
    // orderResponseが存在しない場合は書籍一覧にリダイレクト
    if (!orderResponse) {
      navigate('/books', { replace: true });
    }
  }, [orderResponse, navigate]);

  if (!orderResponse) {
    return null;
  }

  const getSettlementTypeName = (type: number): string => {
    switch (type) {
      case 1: return '銀行振り込み';
      case 2: return 'クレジットカード';
      case 3: return '着払い';
      default: return '';
    }
  };

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        {/* 成功メッセージ（コンパクト版） */}
        <div className="bg-primary text-white rounded-lg p-4 mb-6 shadow-lg">
          <div className="flex items-center justify-center gap-3">
            <div className="w-12 h-12 flex-shrink-0">
              <svg
                className="w-12 h-12"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div>
              <h2 className="text-2xl font-bold">注文が成功しました</h2>
              <p className="text-sm mt-1">ご注文ありがとうございます</p>
            </div>
          </div>
        </div>

        {/* 注文情報 */}
        <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
          <div className="mb-6">
            <h3 className="text-xl font-bold text-primary-darker mb-3">注文情報</h3>
            <div className="grid grid-cols-2 gap-4 mb-3">
              <div className="text-gray-600">注文ID</div>
              <div className="text-base font-bold text-primary">{orderResponse.orderTranId}</div>
            </div>
            <div className="grid grid-cols-2 gap-4 mb-3">
              <div className="text-gray-600">合計金額</div>
              <div className="text-base font-bold text-primary">
                ¥{orderResponse.totalPrice.toLocaleString()}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4 mb-3">
              <div className="text-gray-600">配送料</div>
              <div className="text-base text-gray-800">
                ¥{orderResponse.deliveryPrice.toLocaleString()}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4 mb-3">
              <div className="text-gray-600">配送先住所</div>
              <div className="text-base text-gray-800">{orderResponse.deliveryAddress}</div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="text-gray-600">決済方法</div>
              <div className="text-base text-gray-800">
                {getSettlementTypeName(orderResponse.settlementType)}
              </div>
            </div>
          </div>

          {/* 注文明細 */}
          <div>
            <h3 className="text-xl font-bold text-primary-darker mb-3">注文明細</h3>
            <table className="w-full border border-gray-300">
              <thead className="bg-accent-light">
                <tr>
                  <th className="px-4 py-2 text-left border-b border-gray-300">書籍名</th>
                  <th className="px-4 py-2 text-center border-b border-gray-300 w-24">注文数</th>
                  <th className="px-4 py-2 text-right border-b border-gray-300 w-32">価格</th>
                </tr>
              </thead>
              <tbody>
                {orderResponse.orderDetails.map((detail) => (
                  <tr key={detail.orderDetailId} className="border-b border-gray-200">
                    <td className="px-4 py-2">{detail.bookName}</td>
                    <td className="px-4 py-2 text-center">{detail.count}</td>
                    <td className="px-4 py-2 text-right">¥{detail.price.toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* ナビゲーションボタン */}
        <div className="flex flex-col sm:flex-row gap-4">
          <button
            onClick={() => navigate('/orders/history')}
            className="btn-primary flex-1"
          >
            注文履歴を表示する
          </button>
          <button
            onClick={() => navigate('/books')}
            className="btn-primary flex-1"
          >
            書籍の選択ページへ
          </button>
        </div>
      </div>
    </Layout>
  );
};

export default OrderSuccessPage;


