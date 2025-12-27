import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { OrderResponse } from '../types';
import { orderService } from '../services/orderService';

const OrderDetailPage: React.FC = () => {
  const { tranId } = useParams<{ tranId: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    if (tranId) {
      const id = Number(tranId);
      if (isNaN(id)) {
        setError('無効な注文IDです');
        setLoading(false);
        return;
      }
      loadOrderDetail(id);
    } else {
      setError('注文IDが指定されていません');
      setLoading(false);
    }
  }, [tranId]);

  const loadOrderDetail = async (id: number) => {
    try {
      setLoading(true);
      const data = await orderService.getOrderDetail(id);
      setOrder(data);
    } catch (err: any) {
      setError('注文詳細の読み込みに失敗しました');
      console.error('Failed to load order detail:', err);
    } finally {
      setLoading(false);
    }
  };

  const getSettlementTypeLabel = (type: number): string => {
    const types: { [key: number]: string } = {
      1: 'クレジットカード',
      2: '代金引換',
      3: '銀行振込',
    };
    return types[type] || '不明';
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

  if (error || !order) {
    return (
      <Layout>
        <div className="text-center py-12">
          <p className="error-text text-lg">{error || '注文が見つかりませんでした'}</p>
          <button onClick={() => navigate('/orders/history')} className="btn-primary mt-4">
            注文履歴へ戻る
          </button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <h2 className="text-4xl font-bold text-primary-darker text-center mb-6">
        注文詳細
      </h2>
      <hr className="mb-8 border-t-2 border-primary opacity-50" />

      <div className="max-w-4xl mx-auto">
        {/* 注文情報 */}
        <div className="bg-accent-light p-6 rounded-lg mb-6">
          <h3 className="text-2xl font-bold text-primary-darker mb-4">注文情報</h3>
          <table className="w-full">
            <tbody>
              <tr className="border-b border-accent">
                <td className="py-3 pr-4 font-semibold text-primary-darker w-48">注文番号</td>
                <td className="py-3">{order.orderTranId}</td>
              </tr>
              <tr className="border-b border-accent">
                <td className="py-3 pr-4 font-semibold text-primary-darker">注文日</td>
                <td className="py-3">{order.orderDate}</td>
              </tr>
              <tr className="border-b border-accent">
                <td className="py-3 pr-4 font-semibold text-primary-darker">配送先住所</td>
                <td className="py-3">{order.deliveryAddress}</td>
              </tr>
              <tr className="border-b border-accent">
                <td className="py-3 pr-4 font-semibold text-primary-darker">決済方法</td>
                <td className="py-3">{getSettlementTypeLabel(order.settlementType)}</td>
              </tr>
              <tr className="border-b border-accent">
                <td className="py-3 pr-4 font-semibold text-primary-darker">商品合計</td>
                <td className="py-3 font-semibold">
                  ¥{(order.totalPrice - order.deliveryPrice).toLocaleString()}
                </td>
              </tr>
              <tr className="border-b border-accent">
                <td className="py-3 pr-4 font-semibold text-primary-darker">配送料</td>
                <td className="py-3 font-semibold">¥{order.deliveryPrice.toLocaleString()}</td>
              </tr>
              <tr>
                <td className="py-3 pr-4 font-bold text-primary-darker text-lg">合計金額</td>
                <td className="py-3 font-bold text-primary-darker text-xl">
                  ¥{order.totalPrice.toLocaleString()}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        {/* 注文明細 */}
        <div>
          <h3 className="text-2xl font-bold text-primary-darker mb-4">注文明細</h3>
          <table className="table-primary">
            <thead>
              <tr>
                <th>書籍名</th>
                <th className="w-48">出版社</th>
                <th className="w-32 text-right">単価</th>
                <th className="w-24 text-center">数量</th>
                <th className="w-32 text-right">小計</th>
              </tr>
            </thead>
            <tbody>
              {order.orderDetails.map((detail) => (
                <tr key={detail.orderDetailId}>
                  <td className="px-4 py-3">{detail.bookName}</td>
                  <td className="px-4 py-3">{detail.publisherName}</td>
                  <td className="px-4 py-3 text-right">¥{detail.price.toLocaleString()}</td>
                  <td className="px-4 py-3 text-center">{detail.count}</td>
                  <td className="px-4 py-3 text-right font-semibold">
                    ¥{(detail.price * detail.count).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-8 flex gap-4">
          <button onClick={() => navigate('/orders/history')} className="btn-primary">
            注文履歴へ戻る
          </button>
          <button onClick={() => navigate('/books')} className="btn-primary">
            書籍一覧へ
          </button>
        </div>
      </div>
    </Layout>
  );
};

export default OrderDetailPage;

