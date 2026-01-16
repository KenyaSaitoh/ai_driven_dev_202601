import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { OrderHistoryResponse } from '../types';
import { orderService } from '../services/orderService';

const OrderHistoryPage: React.FC = () => {
  const [orders, setOrders] = useState<OrderHistoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadOrderHistory();
  }, []);

  const loadOrderHistory = async () => {
    try {
      setLoading(true);
      const data = await orderService.getOrderHistory();
      setOrders(data);
    } catch (err: any) {
      setError('注文履歴の読み込みに失敗しました');
      console.error('Failed to load order history:', err);
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
          <button onClick={loadOrderHistory} className="btn-primary mt-4">
            再読み込み
          </button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <h2 className="text-4xl font-bold text-primary-darker text-center mb-6">
        注文履歴
      </h2>
      <hr className="mb-8 border-t-2 border-primary opacity-50" />

      {orders.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600 text-lg mb-6">注文履歴がありません</p>
          <Link to="/books" className="btn-primary inline-block">
            書籍一覧へ
          </Link>
        </div>
      ) : (
        <table className="table-primary">
          <thead>
            <tr>
              <th className="w-32">注文日</th>
              <th className="w-28">注文ID</th>
              <th>書籍名</th>
              <th className="w-48">出版社名</th>
              <th className="w-32 text-right">価格</th>
              <th className="w-24 text-center">個数</th>
              <th className="w-32"></th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order, index) => (
              <tr key={`${order.orderTranId}-${order.orderDetailId}-${index}`}>
                <td className="px-4 py-3">{order.orderDate}</td>
                <td className="px-4 py-3">{order.orderTranId}</td>
                <td className="px-4 py-3">{order.bookName}</td>
                <td className="px-4 py-3">{order.publisherName}</td>
                <td className="px-4 py-3 text-right">{order.price.toLocaleString()}</td>
                <td className="px-4 py-3 text-center">{order.count}</td>
                <td className="px-4 py-3 text-center">
                  <Link
                    to={`/orders/${order.orderTranId}`}
                    className="text-primary-dark hover:text-primary-darker font-semibold transition-colors"
                  >
                    詳細
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Layout>
  );
};

export default OrderHistoryPage;

