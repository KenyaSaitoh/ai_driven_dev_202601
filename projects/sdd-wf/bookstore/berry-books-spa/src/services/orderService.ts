import api from './api';
import { OrderRequest, OrderResponse, OrderHistoryResponse, OrderDetailResponse } from '../types';

export const orderService = {
  // 注文作成
  createOrder: async (data: OrderRequest): Promise<OrderResponse> => {
    const response = await api.post<OrderResponse>('/orders', data);
    return response.data;
  },

  // 注文履歴取得
  getOrderHistory: async (): Promise<OrderHistoryResponse[]> => {
    const response = await api.get<OrderHistoryResponse[]>('/orders/history');
    return response.data;
  },

  // 注文詳細取得
  getOrderDetail: async (tranId: number): Promise<OrderResponse> => {
    const response = await api.get<OrderResponse>(`/orders/${tranId}`);
    return response.data;
  },

  // 注文明細取得
  getOrderDetailItem: async (tranId: number, detailId: number): Promise<OrderDetailResponse> => {
    const response = await api.get<OrderDetailResponse>(`/orders/${tranId}/details/${detailId}`);
    return response.data;
  },
};

