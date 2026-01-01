import api from './api';
import { Workflow, WorkflowCreateRequest, WorkflowOperationRequest, WorkflowUpdateRequest, Category, Publisher, Book } from '../types';

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
   * ワークフロー更新（一時保存）
   * @param workflowId ワークフローID
   * @param request 更新リクエスト
   */
  updateWorkflow: async (workflowId: number, request: WorkflowUpdateRequest): Promise<Workflow> => {
    const response = await api.put<Workflow>(`/workflows/${workflowId}`, request);
    return response.data;
  },

  /**
   * ワークフロー一覧取得
   * @param state 状態（オプション）
   * @param workflowType ワークフロータイプ（オプション）
   * @param employeeId ログイン中の社員ID（CREATED状態のフィルタリング用）
   */
  getWorkflows: async (state?: string, workflowType?: string, employeeId?: number): Promise<Workflow[]> => {
    const params: Record<string, string | number> = {};
    if (state) params.state = state;
    if (workflowType) params.workflowType = workflowType;
    if (employeeId) params.employeeId = employeeId;
    
    const response = await api.get<Workflow[]>('/workflows', { params });
    return response.data;
  },

  /**
   * ワークフロー履歴取得
   * @param workflowId ワークフローID
   */
  getWorkflowHistory: async (workflowId: number): Promise<Workflow[]> => {
    const response = await api.get<Workflow[]>(`/workflows/${workflowId}/history`);
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
   * カテゴリ一覧取得
   */
  getCategories: async (): Promise<Category[]> => {
    const response = await api.get<Category[]>('/categories');
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

