import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { workflowService } from '../services/workflowService';
import { Workflow } from '../types';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';
import '../styles/WorkflowListPage.css';

const WorkflowListPage: React.FC = () => {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [stateFilter, setStateFilter] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('');
  const { employee, isManager } = useAuth();

  useEffect(() => {
    loadWorkflows();
  }, [stateFilter, typeFilter]);

  const loadWorkflows = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await workflowService.getWorkflows(
        stateFilter || undefined,
        typeFilter || undefined
      );
      setWorkflows(data);
    } catch (err: any) {
      console.error('Failed to load workflows:', err);
      setError('ワークフローの読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async (workflowId: number) => {
    if (!employee || !window.confirm('申請してもよろしいですか？')) {
      return;
    }

    try {
      await workflowService.applyWorkflow(workflowId, {
        operatedBy: employee.employeeId,
      });
      alert('申請しました');
      loadWorkflows();
    } catch (err: any) {
      console.error('Failed to apply workflow:', err);
      alert(err.response?.data?.message || '申請に失敗しました');
    }
  };

  const handleApprove = async (workflowId: number) => {
    if (!employee || !window.confirm('承認してもよろしいですか？')) {
      return;
    }

    const reason = prompt('承認理由を入力してください（任意）:');

    try {
      await workflowService.approveWorkflow(workflowId, {
        operatedBy: employee.employeeId,
        operationReason: reason || undefined,
      });
      alert('承認しました');
      loadWorkflows();
    } catch (err: any) {
      console.error('Failed to approve workflow:', err);
      alert(err.response?.data?.message || '承認に失敗しました');
    }
  };

  const handleReject = async (workflowId: number) => {
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
      await workflowService.rejectWorkflow(workflowId, {
        operatedBy: employee.employeeId,
        operationReason: reason,
      });
      alert('差戻しました');
      loadWorkflows();
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

  const canApply = (workflow: Workflow): boolean => {
    // 作成済（CREATED）状態で、自分が作成者の場合のみ申請可能
    return workflow.state === 'CREATED' && 
           employee !== null && 
           workflow.operatedBy === employee.employeeId;
  };

  const canManage = (workflow: Workflow): boolean => {
    // 申請中（APPLIED）状態で、マネージャー以上で、同じ部署の場合のみ操作可能
    if (workflow.state !== 'APPLIED' || !employee || !isManager()) {
      return false;
    }
    // TODO: 作成者の部署IDを取得して、自分の部署と一致するか確認
    // 現在はworkflow.departmentIdが作成者の部署IDを指しているか不明
    // 暫定的にマネージャー以上であれば操作可能とする
    return true;
  };

  return (
    <Layout>
      <div className="workflow-list-container">
        <div className="page-header">
          <h1>ワークフロー一覧</h1>
          <Link to="/workflows/create" className="btn btn-primary">
            新規ワークフロー作成
          </Link>
        </div>

        <div className="filters">
          <div className="filter-group">
            <label>状態:</label>
            <select value={stateFilter} onChange={(e) => setStateFilter(e.target.value)}>
              <option value="">すべて</option>
              <option value="CREATED">作成済</option>
              <option value="APPLIED">申請中</option>
              <option value="APPROVED">承認済</option>
            </select>
          </div>

          <div className="filter-group">
            <label>種別:</label>
            <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
              <option value="">すべて</option>
              <option value="CREATE">新規作成</option>
              <option value="DELETE">削除</option>
              <option value="PRICE_TEMP_ADJUSTMENT">価格調整</option>
            </select>
          </div>
        </div>

        {loading && <div className="loading">読み込み中...</div>}
        {error && <div className="error-message">{error}</div>}

        {!loading && !error && (
          <table className="workflow-table">
            <thead>
              <tr>
                <th>ワークフローID</th>
                <th>種別</th>
                <th>状態</th>
                <th>書籍名</th>
                <th>操作者</th>
                <th>操作日時</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {workflows.length === 0 ? (
                <tr>
                  <td colSpan={7} className="no-data">該当するワークフローがありません</td>
                </tr>
              ) : (
                workflows.map((workflow) => (
                  <tr key={workflow.operationId}>
                    <td>{workflow.workflowId}</td>
                    <td>{getTypeLabel(workflow.workflowType)}</td>
                    <td>
                      <span className={`state-badge state-${workflow.state.toLowerCase()}`}>
                        {getStateLabel(workflow.state)}
                      </span>
                    </td>
                    <td>{workflow.bookName || `書籍ID: ${workflow.bookId}`}</td>
                    <td>{workflow.operatorName || workflow.operatorCode}</td>
                    <td>{new Date(workflow.operatedAt).toLocaleString('ja-JP')}</td>
                    <td>
                      <div className="action-buttons">
                        <Link 
                          to={`/workflows/${workflow.workflowId}`} 
                          className="btn btn-sm btn-secondary"
                        >
                          詳細
                        </Link>
                        
                        {canApply(workflow) && (
                          <button 
                            onClick={() => handleApply(workflow.workflowId)}
                            className="btn btn-sm btn-primary"
                          >
                            申請
                          </button>
                        )}

                        {canManage(workflow) && (
                          <>
                            <button 
                              onClick={() => handleApprove(workflow.workflowId)}
                              className="btn btn-sm btn-success"
                            >
                              承認
                            </button>
                            <button 
                              onClick={() => handleReject(workflow.workflowId)}
                              className="btn btn-sm btn-danger"
                            >
                              差戻
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
};

export default WorkflowListPage;

