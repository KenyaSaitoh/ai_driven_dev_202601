import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { workflowService } from '../services/workflowService';
import { Workflow } from '../types';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';
import '../styles/WorkflowDetailPage.css';

const WorkflowDetailPage: React.FC = () => {
  const { workflowId } = useParams<{ workflowId: string }>();
  const [history, setHistory] = useState<Workflow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
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

  const handleApply = async () => {
    if (!employee || !window.confirm('申請してもよろしいですか？')) {
      return;
    }

    try {
      await workflowService.applyWorkflow(Number(workflowId), {
        operatedBy: employee.employeeId,
      });
      alert('申請しました');
      loadWorkflowHistory();
    } catch (err: any) {
      console.error('Failed to apply workflow:', err);
      alert(err.response?.data?.message || '申請に失敗しました');
    }
  };

  const handleApprove = async () => {
    if (!employee || !window.confirm('承認してもよろしいですか？')) {
      return;
    }

    const reason = prompt('承認理由を入力してください（任意）:');

    try {
      await workflowService.approveWorkflow(Number(workflowId), {
        operatedBy: employee.employeeId,
        operationReason: reason || undefined,
      });
      alert('承認しました');
      loadWorkflowHistory();
    } catch (err: any) {
      console.error('Failed to approve workflow:', err);
      alert(err.response?.data?.message || '承認に失敗しました');
    }
  };

  const handleReject = async () => {
    if (!employee) {
      return;
    }

    const reason = prompt('差戻理由を入力してください（必須）:');
    if (!reason) {
      alert('差戻理由は必須です');
      return;
    }

    if (!window.confirm('差戻してもよろしいですか？')) {
      return;
    }

    try {
      await workflowService.rejectWorkflow(Number(workflowId), {
        operatedBy: employee.employeeId,
        operationReason: reason,
      });
      alert('差戻しました');
      loadWorkflowHistory();
    } catch (err: any) {
      console.error('Failed to reject workflow:', err);
      alert(err.response?.data?.message || '差戻に失敗しました');
    }
  };

  const getStateLabel = (state: string): string => {
    switch (state) {
      case 'CREATED': return '作成済';
      case 'APPLIED': return '申請中';
      case 'APPROVED': return '承認済';
      default: return state;
    }
  };

  const getTypeLabel = (type: string): string => {
    switch (type) {
      case 'CREATE': return '新規作成';
      case 'DELETE': return '削除';
      case 'PRICE_TEMP_ADJUSTMENT': return '価格調整';
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

  const canApply = (): boolean => {
    return latestWorkflow.state === 'CREATED' && 
           employee !== null && 
           latestWorkflow.operatedBy === employee.employeeId;
  };

  const canManage = (): boolean => {
    if (latestWorkflow.state !== 'APPLIED' || !employee || !isManager()) {
      return false;
    }
    return true;
  };

  return (
    <Layout>
      <div className="workflow-detail-container">
        <div className="page-header">
          <h1>ワークフロー詳細</h1>
          <button onClick={() => navigate('/workflows')} className="btn btn-secondary">
            一覧に戻る
          </button>
        </div>

        <div className="workflow-info-card">
          <h2>基本情報</h2>
          <table className="info-table">
            <tbody>
              <tr>
                <th>ワークフローID:</th>
                <td>{latestWorkflow.workflowId}</td>
              </tr>
              <tr>
                <th>種別:</th>
                <td>{getTypeLabel(latestWorkflow.workflowType)}</td>
              </tr>
              <tr>
                <th>現在の状態:</th>
                <td>
                  <span className={`state-badge state-${latestWorkflow.state.toLowerCase()}`}>
                    {getStateLabel(latestWorkflow.state)}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>

          <h3 className="detail-section-title">ワークフロー内容</h3>
          <table className="info-table">
            <tbody>
              {latestWorkflow.bookId && (
                <tr>
                  <th>書籍ID:</th>
                  <td>{latestWorkflow.bookId}</td>
                </tr>
              )}
              {latestWorkflow.bookName && (
                <tr>
                  <th>書籍名:</th>
                  <td>{latestWorkflow.bookName}</td>
                </tr>
              )}
              {latestWorkflow.author && (
                <tr>
                  <th>著者:</th>
                  <td>{latestWorkflow.author}</td>
                </tr>
              )}
              {latestWorkflow.price !== undefined && (
                <tr>
                  <th>価格:</th>
                  <td>¥{latestWorkflow.price.toLocaleString()}</td>
                </tr>
              )}
              {latestWorkflow.categoryName && (
                <tr>
                  <th>カテゴリ:</th>
                  <td>{latestWorkflow.categoryName}</td>
                </tr>
              )}
              {latestWorkflow.publisherName && (
                <tr>
                  <th>出版社:</th>
                  <td>{latestWorkflow.publisherName}</td>
                </tr>
              )}
              {latestWorkflow.startDate && (
                <tr>
                  <th>開始日:</th>
                  <td>{latestWorkflow.startDate}</td>
                </tr>
              )}
              {latestWorkflow.endDate && (
                <tr>
                  <th>終了日:</th>
                  <td>{latestWorkflow.endDate}</td>
                </tr>
              )}
              {latestWorkflow.applyReason && (
                <tr>
                  <th>申請理由:</th>
                  <td>{latestWorkflow.applyReason}</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {(canApply() || canManage()) && (
          <div className="action-panel">
            {canApply() && (
              <button onClick={handleApply} className="btn btn-primary">
                申請
              </button>
            )}
            {canManage() && (
              <>
                <button onClick={handleApprove} className="btn btn-success">
                  承認
                </button>
                <button onClick={handleReject} className="btn btn-danger">
                  差戻
                </button>
              </>
            )}
          </div>
        )}

        <div className="workflow-history-card">
          <h2>操作履歴</h2>
          <table className="history-table">
            <thead>
              <tr>
                <th>操作ID</th>
                <th>操作種別</th>
                <th>状態</th>
                <th>操作者</th>
                <th>操作日時</th>
                <th>操作理由</th>
              </tr>
            </thead>
            <tbody>
              {history.map((item) => (
                <tr key={item.operationId}>
                  <td>{item.operationId}</td>
                  <td>{getOperationTypeLabel(item.operationType)}</td>
                  <td>
                    <span className={`state-badge state-${item.state.toLowerCase()}`}>
                      {getStateLabel(item.state)}
                    </span>
                  </td>
                  <td>
                    {item.operatorName || item.operatorCode} 
                    {item.departmentName && ` (${item.departmentName})`}
                  </td>
                  <td>{new Date(item.operatedAt).toLocaleString('ja-JP')}</td>
                  <td>{item.operationReason || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </Layout>
  );
};

export default WorkflowDetailPage;

