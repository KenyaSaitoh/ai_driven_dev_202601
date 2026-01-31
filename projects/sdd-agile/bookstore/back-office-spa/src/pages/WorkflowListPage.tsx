import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { workflowService } from '../services/workflowService';
import { Workflow } from '../types';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';

const WorkflowListPage: React.FC = () => {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [stateFilter, setStateFilter] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('');
  const { employee } = useAuth();

  useEffect(() => {
    loadWorkflows();
  }, [stateFilter, typeFilter]);

  const loadWorkflows = async () => {
    setLoading(true);
    setError('');
    try {
      // サーバー側のフィルター値を決定
      let serverStateFilter: string | undefined;
      if (stateFilter === 'APPLIED_SELF' || stateFilter === 'APPLIED_OTHERS') {
        serverStateFilter = 'APPLIED';
      } else if (stateFilter) {
        serverStateFilter = stateFilter;
      }

      const data = await workflowService.getWorkflows(
        serverStateFilter,
        typeFilter || undefined,
        employee?.employeeId
      );

      // クライアント側でさらにフィルタリング
      let filteredData = data;
      if (stateFilter === 'APPLIED_SELF' && employee) {
        filteredData = data.filter(w => w.state === 'APPLIED' && w.operatedBy === employee.employeeId);
      } else if (stateFilter === 'APPLIED_OTHERS' && employee) {
        filteredData = data.filter(w => w.state === 'APPLIED' && w.operatedBy !== employee.employeeId);
      }

      setWorkflows(filteredData);
    } catch (err: any) {
      console.error('Failed to load workflows:', err);
      setError('ワークフローの読み込みに失敗しました');
    } finally {
      setLoading(false);
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

  const getDisplayStateLabel = (workflow: Workflow): string => {
    if (workflow.state === 'APPLIED' && employee) {
      // APPLIED状態の場合、operatedByが自分なら「申請中」、他人なら「承認待ち」
      if (workflow.operatedBy === employee.employeeId) {
        return '申請中';  // 自分が申請した場合
      } else {
        return '承認待ち';  // 他人が申請した場合
      }
    }
    return getStateLabel(workflow.state);
  };

  const getStateBadgeClass = (workflow: Workflow): string => {
    if (workflow.state === 'CREATED') {
      return 'bg-blue-100 text-blue-800';
    } else if (workflow.state === 'APPLIED') {
      // 自分が申請した場合は黄色、他人が申請した場合（承認待ち）は緑
      if (employee && workflow.operatedBy === employee.employeeId) {
        return 'bg-yellow-100 text-yellow-800';  // 申請中
      } else {
        return 'bg-green-100 text-green-800';  // 承認待ち
      }
    } else if (workflow.state === 'APPROVED') {
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

  const canEdit = (workflow: Workflow): boolean => {
    return workflow.state === 'CREATED' && 
           employee !== null && 
           workflow.operatedBy === employee.employeeId;
  };


  return (
    <Layout>
      <div className="space-y-6">
        <div className="page-header">
          <h1>ワークフロー一覧</h1>
        </div>

        <div className="bg-white p-4 rounded-lg shadow-md flex gap-4">
          <div className="flex-1">
            <label className="block text-sm font-semibold text-gray-700 mb-2">状態:</label>
            <select 
              value={stateFilter} 
              onChange={(e) => setStateFilter(e.target.value)}
              className="form-input"
            >
              <option value="">すべて</option>
              <option value="CREATED">作成済み</option>
              <option value="APPLIED_SELF">申請中</option>
              <option value="APPLIED_OTHERS">承認待ち</option>
              <option value="APPROVED">承認済み</option>
            </select>
          </div>

          <div className="flex-1">
            <label className="block text-sm font-semibold text-gray-700 mb-2">種別:</label>
            <select 
              value={typeFilter} 
              onChange={(e) => setTypeFilter(e.target.value)}
              className="form-input"
            >
              <option value="">すべて</option>
              <option value="ADD_NEW_BOOK">新規書籍の追加</option>
              <option value="REMOVE_BOOK">既存書籍の削除</option>
              <option value="ADJUST_BOOK_PRICE">書籍価格の改定</option>
            </select>
          </div>
        </div>

        {loading && <div className="loading">読み込み中...</div>}
        {error && <div className="error-message">{error}</div>}

        {!loading && !error && (
          <div className="bg-white rounded-lg shadow-table overflow-hidden">
            <table className="table-primary">
              <thead>
                <tr>
                  <th className="w-16">ID</th>
                  <th>種別</th>
                  <th>状態</th>
                  <th>書籍名</th>
                  <th>操作者</th>
                  <th>操作日時</th>
                  <th className="w-48">操作</th>
                </tr>
              </thead>
              <tbody>
                {workflows.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center py-8 text-gray-600">
                      該当するワークフローがありません
                    </td>
                  </tr>
                ) : (
                  workflows.map((workflow) => (
                    <tr key={workflow.operationId}>
                      <td>{workflow.workflowId}</td>
                      <td>{getTypeLabel(workflow.workflowType)}</td>
                      <td>
                        <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStateBadgeClass(workflow)}`}>
                          {getDisplayStateLabel(workflow)}
                        </span>
                      </td>
                      <td>{workflow.bookName || `書籍ID: ${workflow.bookId}`}</td>
                      <td>
                        {workflow.operatorName || workflow.operatorCode}
                        {workflow.departmentName && `（${workflow.departmentName}）`}
                      </td>
                      <td>{new Date(workflow.operatedAt).toLocaleString('ja-JP')}</td>
                      <td>
                        <div className="flex gap-2 flex-wrap">
                          <Link 
                            to={`/workflows/${workflow.workflowId}`} 
                            className="btn-secondary-sm"
                          >
                            参照
                          </Link>
                          {canEdit(workflow) && (
                            <Link 
                              to={`/workflows/${workflow.workflowId}/edit`} 
                              className="btn-secondary-sm"
                            >
                              編集
                            </Link>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default WorkflowListPage;

