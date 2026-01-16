import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { workflowService } from '../services/workflowService';
import { Workflow } from '../types';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';

const WorkflowDetailPage: React.FC = () => {
  const { workflowId } = useParams<{ workflowId: string }>();
  const [history, setHistory] = useState<Workflow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showApproveModal, setShowApproveModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [operationReason, setOperationReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { employee, isManager } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (workflowId) {
      loadWorkflowHistory();
    }
  }, [workflowId]);

  const loadWorkflowHistory = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await workflowService.getWorkflowHistory(Number(workflowId));
      setHistory(data);
    } catch (err: any) {
      console.error('Failed to load workflow history:', err);
      setError('ワークフロー履歴の読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleApproveClick = () => {
    setOperationReason('');
    setError('');
    setShowApproveModal(true);
  };

  const handleRejectClick = () => {
    setOperationReason('');
    setError('');
    setShowRejectModal(true);
  };

  const handleApproveConfirm = async () => {
    if (!employee) {
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      await workflowService.approveWorkflow(Number(workflowId), {
        operatedBy: employee.employeeId,
        operationReason: operationReason || undefined,
      });
      setShowApproveModal(false);
      setOperationReason('');
      await loadWorkflowHistory();
    } catch (err: any) {
      console.error('Failed to approve workflow:', err);
      setError(err.response?.data?.message || '承認に失敗しました');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRejectConfirm = async () => {
    if (!employee) {
      return;
    }

    if (!operationReason.trim()) {
      setError('差戻理由は必須です');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      await workflowService.rejectWorkflow(Number(workflowId), {
        operatedBy: employee.employeeId,
        operationReason: operationReason,
      });
      setShowRejectModal(false);
      setOperationReason('');
      await loadWorkflowHistory();
    } catch (err: any) {
      console.error('Failed to reject workflow:', err);
      setError(err.response?.data?.message || '差戻に失敗しました');
    } finally {
      setSubmitting(false);
    }
  };

  const getStateLabel = (state: string): string => {
    switch (state) {
      case 'NEW': // 後方互換性のため
      case 'CREATED': return '作成済み';
      case 'APPLIED': return '申請中';
      case 'APPROVED': return '承認済み';
      default: return state;
    }
  };

  const getDisplayStateLabel = (state: string): string => {
    if (state === 'APPLIED' && employee) {
      // 申請者（CREATE操作を行った人）を探す
      const creator = history.find(item => item.operationType === 'CREATE');
      if (creator && creator.operatedBy === employee.employeeId) {
        return '申請中';  // 自分が申請した場合
      } else {
        return '承認待ち';  // 他人が申請した場合
      }
    }
    return getStateLabel(state);
  };

  const getStateBadgeClass = (state: string): string => {
    if (state === 'CREATED') {
      return 'bg-blue-100 text-blue-800';
    } else if (state === 'APPLIED') {
      // 申請者（CREATE操作を行った人）を探す
      const creator = history.find(item => item.operationType === 'CREATE');
      if (employee && creator && creator.operatedBy === employee.employeeId) {
        return 'bg-yellow-100 text-yellow-800';  // 申請中（自分）
      } else {
        return 'bg-green-100 text-green-800';  // 承認待ち（他人）
      }
    } else if (state === 'APPROVED') {
      return 'bg-gray-300 text-gray-800';  // 承認済み（濃いグレー）
    }
    return 'bg-gray-300 text-gray-800';
  };

  const getTypeLabel = (type: string): string => {
    switch (type) {
      case 'ADD_NEW_BOOK': return '新規書籍の追加';
      case 'REMOVE_BOOK': return '既存書籍の削除';
      case 'ADJUST_BOOK_PRICE': return '書籍価格の改定';
      default: return type;
    }
  };

  const getOperationTypeLabel = (operationType: string): string => {
    switch (operationType) {
      case 'CREATE': return '作成';
      case 'APPLY': return '申請';
      case 'APPROVE': return '承認';
      case 'REJECT': return '差戻';
      default: return operationType;
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="loading">読み込み中...</div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="error-message">{error}</div>
      </Layout>
    );
  }

  if (history.length === 0) {
    return (
      <Layout>
        <div className="error-message">ワークフローが見つかりません</div>
      </Layout>
    );
  }

  const latestWorkflow = history[history.length - 1];

  const canEdit = (): boolean => {
    return latestWorkflow.state === 'CREATED' && 
           employee !== null && 
           latestWorkflow.operatedBy === employee.employeeId;
  };

  const canManage = (): boolean => {
    if (latestWorkflow.state !== 'APPLIED' || !employee || !isManager()) {
      return false;
    }
    
    // 自分自身が作成したワークフローは承認・差戻不可
    const creator = history.find(item => item.operationType === 'CREATE');
    if (creator && creator.operatedBy === employee.employeeId) {
      return false;
    }
    
    // 自分自身が既に承認または差戻を行っている場合は不可
    const hasAlreadyManaged = history.some(item => 
      item.operatedBy === employee.employeeId && 
      (item.operationType === 'APPROVE' || item.operationType === 'REJECT')
    );
    
    if (hasAlreadyManaged) {
      return false;
    }
    
    return true;
  };

  return (
    <Layout>
      <div className="space-y-6">
        <div className="page-header">
          <h1>ワークフロー詳細</h1>
          <button onClick={() => navigate('/workflows')} className="btn-secondary">
            一覧に戻る
          </button>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">基本情報</h2>
          <table className="w-full">
            <tbody>
              <tr className="border-b border-gray-200">
                <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50 w-48">ワークフローID</td>
                <td className="py-2 px-4 text-gray-900">{latestWorkflow.workflowId}</td>
              </tr>
              <tr className="border-b border-gray-200">
                <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">種別</td>
                <td className="py-2 px-4 text-gray-900">{getTypeLabel(latestWorkflow.workflowType)}</td>
              </tr>
              <tr className="border-b border-gray-200">
                <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">現在の状態</td>
                <td className="py-2 px-4">
                  <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStateBadgeClass(latestWorkflow.state)}`}>
                    {getDisplayStateLabel(latestWorkflow.state)}
                  </span>
                </td>
              </tr>
              {latestWorkflow.bookId && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">書籍ID</td>
                  <td className="py-2 px-4 text-gray-900">{latestWorkflow.bookId}</td>
                </tr>
              )}
              {latestWorkflow.bookName && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">書籍名</td>
                  <td className="py-2 px-4 text-gray-900">{latestWorkflow.bookName}</td>
                </tr>
              )}
              {latestWorkflow.author && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">著者</td>
                  <td className="py-2 px-4 text-gray-900">{latestWorkflow.author}</td>
                </tr>
              )}
              {latestWorkflow.price !== undefined && latestWorkflow.price !== null && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">
                    {latestWorkflow.workflowType === 'ADJUST_BOOK_PRICE' && latestWorkflow.originalPrice !== undefined && latestWorkflow.originalPrice !== null ? '価格' : '価格'}
                  </td>
                  <td className="py-2 px-4 text-gray-900">
                    {latestWorkflow.workflowType === 'ADJUST_BOOK_PRICE' && latestWorkflow.originalPrice !== undefined && latestWorkflow.originalPrice !== null ? (
                      <>
                        ¥{latestWorkflow.originalPrice.toLocaleString()}
                        <span className="mx-2 text-gray-500">→</span>
                        <span className="font-bold text-red-600">¥{latestWorkflow.price.toLocaleString()}</span>
                      </>
                    ) : (
                      `¥${latestWorkflow.price.toLocaleString()}`
                    )}
                  </td>
                </tr>
              )}
              {latestWorkflow.categoryName && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">カテゴリ</td>
                  <td className="py-2 px-4 text-gray-900">{latestWorkflow.categoryName}</td>
                </tr>
              )}
              {latestWorkflow.publisherName && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">出版社</td>
                  <td className="py-2 px-4 text-gray-900">{latestWorkflow.publisherName}</td>
                </tr>
              )}
              {latestWorkflow.startDate && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">開始日</td>
                  <td className="py-2 px-4 text-gray-900">{latestWorkflow.startDate}</td>
                </tr>
              )}
              {latestWorkflow.endDate && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50">終了日</td>
                  <td className="py-2 px-4 text-gray-900">{latestWorkflow.endDate}</td>
                </tr>
              )}
              {latestWorkflow.applyReason && (
                <tr className="border-b border-gray-200">
                  <td className="py-2 px-4 font-semibold text-gray-700 bg-gray-50 align-top">申請理由</td>
                  <td className="py-2 px-4 text-gray-900 whitespace-pre-wrap">{latestWorkflow.applyReason}</td>
                </tr>
              )}
            </tbody>
          </table>

          {(canEdit() || canManage()) && (
            <div className="mt-6 flex justify-end gap-3">
              {canEdit() && (
                <button 
                  onClick={() => navigate(`/workflows/${workflowId}/edit`)} 
                  className="btn-primary" 
                  disabled={submitting}
                >
                  編集
                </button>
              )}
              {canManage() && (
                <>
                  <button onClick={handleRejectClick} className="btn-secondary" disabled={submitting}>
                    差戻
                  </button>
                  <button onClick={handleApproveClick} className="btn-primary" disabled={submitting}>
                    承認
                  </button>
                </>
              )}
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">操作履歴</h2>
          <div className="overflow-x-auto">
            <table className="table-primary">
              <thead>
                <tr>
                  <th className="w-24">操作ID</th>
                  <th className="w-28">操作種別</th>
                  <th className="w-28">状態</th>
                  <th className="w-48">操作者</th>
                  <th className="w-44">操作日時</th>
                  <th className="w-96">操作理由</th>
                </tr>
              </thead>
              <tbody>
                {history.map((item) => (
                  <tr key={item.operationId}>
                    <td>{item.operationId}</td>
                    <td>{getOperationTypeLabel(item.operationType)}</td>
                    <td>
                      <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${
                        item.state === 'CREATED' ? 'bg-blue-100 text-blue-800' :
                        item.state === 'APPLIED' ? 'bg-yellow-100 text-yellow-800' :
                        item.state === 'APPROVED' ? 'bg-green-100 text-green-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {getStateLabel(item.state)}
                      </span>
                    </td>
                    <td>
                      {item.operatorName || item.operatorCode} 
                      {item.departmentName && `（${item.departmentName}）`}
                    </td>
                    <td>{new Date(item.operatedAt).toLocaleString('ja-JP')}</td>
                    <td className="whitespace-normal break-words max-w-96">{item.operationReason || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* 承認モーダル */}
      {showApproveModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
            <div className="p-6">
              <h3 className="text-xl font-bold text-gray-900 mb-4">承認確認</h3>
              <p className="text-gray-700 mb-4">このワークフローを承認しますか？</p>
              
              <div className="mb-4">
                <label htmlFor="approveReason" className="block text-sm font-semibold text-gray-700 mb-2">
                  承認理由（任意）
                </label>
                <textarea
                  id="approveReason"
                  value={operationReason}
                  onChange={(e) => setOperationReason(e.target.value)}
                  className="form-input"
                  rows={3}
                  placeholder="承認理由を入力してください"
                  disabled={submitting}
                />
              </div>

              <div className="flex justify-end gap-3">
                <button
                  onClick={() => {
                    setShowApproveModal(false);
                    setError('');
                  }}
                  className="btn-secondary-sm w-28"
                  disabled={submitting}
                >
                  キャンセル
                </button>
                <button
                  onClick={handleApproveConfirm}
                  className="btn-primary-sm w-28"
                  disabled={submitting}
                >
                  {submitting ? '処理中...' : '承認'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 差戻モーダル */}
      {showRejectModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
            <div className="p-6">
              <h3 className="text-xl font-bold text-gray-900 mb-4">差戻確認</h3>
              <p className="text-gray-700 mb-4">このワークフローを差戻しますか？</p>
              
              <div className="mb-4">
                <label htmlFor="rejectReason" className="block text-sm font-semibold text-gray-700 mb-2">
                  差戻理由 *
                </label>
                <textarea
                  id="rejectReason"
                  value={operationReason}
                  onChange={(e) => setOperationReason(e.target.value)}
                  className="form-input"
                  rows={3}
                  placeholder="差戻理由を入力してください（必須）"
                  disabled={submitting}
                  required
                />
              </div>

              <div className="flex justify-end gap-3">
                <button
                  onClick={() => {
                    setShowRejectModal(false);
                    setError('');
                  }}
                  className="btn-secondary-sm w-28"
                  disabled={submitting}
                >
                  キャンセル
                </button>
                <button
                  onClick={handleRejectConfirm}
                  className="btn-secondary-sm w-28"
                  disabled={submitting}
                >
                  {submitting ? '処理中...' : '差戻'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};

export default WorkflowDetailPage;

