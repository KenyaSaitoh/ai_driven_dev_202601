import api from './api';
import { Workflow, WorkflowCreateRequest, WorkflowOperationRequest, CategoryMap, Publisher, Book } from '../types';

/**
 * ワークフローサービス
 */
export const workflowService = {
  /**
   * ワークフロー作成
   */
  createWorkflow: async (request: WorkflowCreateRequest): Promise<Workflow> => {
    const response = await api.post<Workflow>('/workflows', request);
    return response.data;
  },

  /**
   * ワークフロー一覧取得
   * @param state 状態（オプション）
   * @param workflowType ワークフロータイプ（オプション）
   */
  getWorkflows: async (state?: string, workflowType?: string): Promise<Workflow[]> => {
    const params: Record<string, string> = {};
    if (state) params.state = state;
    if (workflowType) params.workflowType = workflowType;
    
    const response = await api.get<Workflow[]>('/workflows', { params });
    return response.data;
  },

  /**
   * ワークフロー履歴取得
   * @param workflowId ワークフローID
   */
  getWorkflowHistory: async (workflowId: number): Promise<Workflow[]> => {
    const response = await api.get<Workflow[]>(`/workflows/${workflowId}`);
    return response.data;
  },

  /**
   * 申請
   * @param workflowId ワークフローID
   * @param request 操作リクエスト
   */
  applyWorkflow: async (workflowId: number, request: WorkflowOperationRequest): Promise<Workflow> => {
    const response = await api.post<Workflow>(`/workflows/${workflowId}/apply`, request);
    return response.data;
  },

  /**
   * 承認
   * @param workflowId ワークフローID
   * @param request 操作リクエスト
   */
  approveWorkflow: async (workflowId: number, request: WorkflowOperationRequest): Promise<Workflow> => {
    const response = await api.post<Workflow>(`/workflows/${workflowId}/approve`, request);
    return response.data;
  },

  /**
   * 差戻（却下）
   * @param workflowId ワークフローID
   * @param request 操作リクエスト
   */
  rejectWorkflow: async (workflowId: number, request: WorkflowOperationRequest): Promise<Workflow> => {
    const response = await api.post<Workflow>(`/workflows/${workflowId}/reject`, request);
    return response.data;
  },

  /**
   * カテゴリ一覧取得（Map形式）
   */
  getCategories: async (): Promise<CategoryMap> => {
    const response = await api.get<CategoryMap>('/categories');
    return response.data;
  },

  /**
   * 出版社一覧取得（仮実装：back-office-apiにエンドポイントがない場合は空配列）
   * TODO: バックエンドに/api/publishersエンドポイントを追加
   */
  getPublishers: async (): Promise<Publisher[]> => {
    try {
      const response = await api.get<Publisher[]>('/publishers');
      return response.data;
    } catch (error) {
      console.warn('Publishers endpoint not available, returning empty array');
      return [];
    }
  },

  /**
   * 書籍一覧取得
   */
  getBooks: async (): Promise<Book[]> => {
    const response = await api.get<Book[]>('/books');
    return response.data;
  },
};

