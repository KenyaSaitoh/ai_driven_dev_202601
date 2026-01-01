import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { workflowService } from '../services/workflowService';
import { WorkflowUpdateRequest, Category, Publisher, Book, Workflow } from '../types';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';

const EditWorkflowPage: React.FC = () => {
  const { workflowId } = useParams<{ workflowId: string }>();
  const [workflow, setWorkflow] = useState<Workflow | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [publishers, setPublishers] = useState<Publisher[]>([]);
  const [books, setBooks] = useState<Book[]>([]);
  
  // フォームフィールド
  const [bookId, setBookId] = useState<number | ''>('');
  const [bookName, setBookName] = useState('');
  const [author, setAuthor] = useState('');
  const [price, setPrice] = useState<number | ''>('');
  const [imageUrl, setImageUrl] = useState('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [publisherId, setPublisherId] = useState<number | ''>('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [applyReason, setApplyReason] = useState('');
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const { employee } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    loadWorkflowAndMasterData();
  }, [workflowId]);

  const loadWorkflowAndMasterData = async () => {
    setLoading(true);
    try {
      const [workflowHistory, categoriesData, publishersData, booksData] = await Promise.all([
        workflowService.getWorkflowHistory(Number(workflowId)),
        workflowService.getCategories(),
        workflowService.getPublishers(),
        workflowService.getBooks(),
      ]);
      
      if (workflowHistory.length === 0) {
        setError('ワークフローが見つかりません');
        return;
      }

      const latestWorkflow = workflowHistory[workflowHistory.length - 1];
      
      // 作成済み状態かつ作成者本人のみ編集可能
      if (latestWorkflow.state !== 'CREATED' || 
          !employee || 
          latestWorkflow.operatedBy !== employee.employeeId) {
        setError('このワークフローは編集できません');
        return;
      }

      setWorkflow(latestWorkflow);
      setCategories(categoriesData);
      setPublishers(publishersData);
      setBooks(booksData);

      // フォームに既存データを設定
      if (latestWorkflow.bookId) setBookId(latestWorkflow.bookId);
      if (latestWorkflow.bookName) setBookName(latestWorkflow.bookName);
      if (latestWorkflow.author) setAuthor(latestWorkflow.author);
      if (latestWorkflow.price) setPrice(latestWorkflow.price);
      if (latestWorkflow.imageUrl) setImageUrl(latestWorkflow.imageUrl);
      if (latestWorkflow.categoryId) setCategoryId(latestWorkflow.categoryId);
      if (latestWorkflow.publisherId) setPublisherId(latestWorkflow.publisherId);
      if (latestWorkflow.startDate) setStartDate(latestWorkflow.startDate);
      if (latestWorkflow.endDate) setEndDate(latestWorkflow.endDate);
      if (latestWorkflow.applyReason) setApplyReason(latestWorkflow.applyReason);
    } catch (err) {
      console.error('Failed to load workflow:', err);
      setError('ワークフローの読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!employee || !workflow) {
      return;
    }

    setError('');
    setSubmitting(true);

    try {
      const request: WorkflowUpdateRequest = {
        updatedBy: employee.employeeId,
        applyReason,
      };

      // ワークフロータイプごとに必要なフィールドを設定
      switch (workflow.workflowType) {
        case 'ADD_NEW_BOOK':
          if (!bookName || !author || !price || !categoryId || !publisherId) {
            setError('すべての必須項目を入力してください');
            setSubmitting(false);
            return;
          }
          request.bookName = bookName;
          request.author = author;
          request.price = Number(price);
          request.imageUrl = imageUrl;
          request.categoryId = Number(categoryId);
          request.publisherId = Number(publisherId);
          break;

        case 'ADJUST_BOOK_PRICE':
          if (!price || !startDate || !endDate) {
            setError('すべての必須項目を入力してください');
            setSubmitting(false);
            return;
          }
          
          // 日付バリデーション
          const today = new Date();
          today.setHours(0, 0, 0, 0); // 時刻をリセットして日付のみで比較
          
          const start = new Date(startDate);
          const end = new Date(endDate);
          
          if (start < today) {
            setError('開始日は今日以降の日付を指定してください');
            setSubmitting(false);
            return;
          }
          
          if (end < start) {
            setError('終了日は開始日以降の日付を指定してください');
            setSubmitting(false);
            return;
          }
          
          request.price = Number(price);
          request.startDate = startDate;
          request.endDate = endDate;
          break;
      }

      await workflowService.updateWorkflow(Number(workflowId), request);
      navigate(`/workflows/${workflowId}`);
    } catch (err: any) {
      console.error('Failed to update workflow:', err);
      setError(err.response?.data?.message || 'ワークフローの更新に失敗しました');
    } finally {
      setSubmitting(false);
    }
  };

  const handleApply = async () => {
    if (!employee || !workflow) {
      return;
    }

    setError('');
    setSubmitting(true);

    try {
      await workflowService.applyWorkflow(Number(workflowId), {
        operatedBy: employee.employeeId,
      });
      navigate(`/workflows/${workflowId}`);
    } catch (err: any) {
      console.error('Failed to apply workflow:', err);
      setError(err.response?.data?.message || '申請に失敗しました');
    } finally {
      setSubmitting(false);
    }
  };

  const renderFormFields = () => {
    if (!workflow) return null;

    switch (workflow.workflowType) {
      case 'ADD_NEW_BOOK':
        return (
          <>
            <div>
              <label htmlFor="bookName" className="block text-sm font-semibold text-gray-700 mb-2">
                書籍名 *
              </label>
              <input
                type="text"
                id="bookName"
                value={bookName}
                onChange={(e) => setBookName(e.target.value)}
                className="form-input"
                required
                disabled={submitting}
              />
            </div>

            <div>
              <label htmlFor="author" className="block text-sm font-semibold text-gray-700 mb-2">
                著者 *
              </label>
              <input
                type="text"
                id="author"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                className="form-input"
                required
                disabled={submitting}
              />
            </div>

            <div>
              <label htmlFor="price" className="block text-sm font-semibold text-gray-700 mb-2">
                価格 *
              </label>
              <input
                type="number"
                id="price"
                value={price}
                onChange={(e) => setPrice(e.target.value ? Number(e.target.value) : '')}
                className="form-input"
                required
                min="0"
                disabled={submitting}
              />
            </div>

            <div>
              <label htmlFor="categoryId" className="block text-sm font-semibold text-gray-700 mb-2">
                カテゴリ *
              </label>
              <select
                id="categoryId"
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : '')}
                className="form-input"
                required
                disabled={submitting}
              >
                <option value="">選択してください</option>
                {categories.map((category) => (
                  <option key={category.categoryId} value={category.categoryId}>
                    {category.categoryName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="publisherId" className="block text-sm font-semibold text-gray-700 mb-2">
                出版社 *
              </label>
              <select
                id="publisherId"
                value={publisherId}
                onChange={(e) => setPublisherId(e.target.value ? Number(e.target.value) : '')}
                className="form-input"
                required
                disabled={submitting}
              >
                <option value="">選択してください</option>
                {publishers.map((publisher) => (
                  <option key={publisher.publisherId} value={publisher.publisherId}>
                    {publisher.publisherName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="imageUrl" className="block text-sm font-semibold text-gray-700 mb-2">
                画像URL
              </label>
              <input
                type="text"
                id="imageUrl"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                className="form-input"
                disabled={submitting}
              />
            </div>
          </>
        );

      case 'REMOVE_BOOK':
        return (
          <div className="md:col-span-2 bg-gray-50 p-4 rounded-lg">
            <p className="text-gray-700">
              <span className="font-semibold">削除対象書籍ID:</span> {workflow.bookId}
            </p>
            {workflow.bookName && (
              <p className="text-gray-700">
                <span className="font-semibold">書籍名:</span> {workflow.bookName}
              </p>
            )}
            <p className="text-sm text-gray-600 mt-2">
              ※ 削除ワークフローの書籍IDは変更できません
            </p>
          </div>
        );

      case 'ADJUST_BOOK_PRICE':
        return (
          <>
            <div className="md:col-span-2 bg-gray-50 p-4 rounded-lg">
              <p className="text-gray-700">
                <span className="font-semibold">対象書籍ID:</span> {workflow.bookId}
              </p>
              {workflow.bookName && (
                <p className="text-gray-700">
                  <span className="font-semibold">書籍名:</span> {workflow.bookName}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="price" className="block text-sm font-semibold text-gray-700 mb-2">
                改定後の価格 *
              </label>
              <input
                type="number"
                id="price"
                value={price}
                onChange={(e) => setPrice(e.target.value ? Number(e.target.value) : '')}
                className="form-input"
                required
                min="0"
                disabled={submitting}
              />
            </div>

            <div>
              <label htmlFor="startDate" className="block text-sm font-semibold text-gray-700 mb-2">
                開始日 *
              </label>
              <input
                type="date"
                id="startDate"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="form-input"
                required
                disabled={submitting}
                min={new Date().toISOString().split('T')[0]}
              />
            </div>

            <div>
              <label htmlFor="endDate" className="block text-sm font-semibold text-gray-700 mb-2">
                終了日 *
              </label>
              <input
                type="date"
                id="endDate"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="form-input"
                required
                disabled={submitting}
                min={startDate || new Date().toISOString().split('T')[0]}
              />
            </div>
          </>
        );

      default:
        return null;
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="loading">読み込み中...</div>
      </Layout>
    );
  }

  if (error && !workflow) {
    return (
      <Layout>
        <div className="space-y-6">
          <div className="page-header">
            <h1>ワークフロー編集</h1>
            <button onClick={() => navigate('/workflows')} className="btn-secondary">
              一覧に戻る
            </button>
          </div>
          <div className="error-message">{error}</div>
        </div>
      </Layout>
    );
  }

  const getTypeLabel = (type: string): string => {
    switch (type) {
      case 'ADD_NEW_BOOK': return '新規書籍の追加';
      case 'REMOVE_BOOK': return '既存書籍の削除';
      case 'ADJUST_BOOK_PRICE': return '書籍価格の改定';
      default: return type;
    }
  };

  return (
    <Layout>
      <div className="space-y-6">
        <div className="page-header">
          <h1>ワークフロー編集</h1>
          <button onClick={() => navigate(`/workflows/${workflowId}`)} className="btn-secondary">
            キャンセル
          </button>
        </div>

        {error && (
          <div className="error-message">{error}</div>
        )}

        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="mb-4 space-y-3">
            <p className="text-sm text-gray-600">
              <span className="font-semibold">ワークフローID:</span> {workflow?.workflowId}
            </p>
            <p className="text-sm text-gray-600">
              <span className="font-semibold">種別:</span> {workflow && getTypeLabel(workflow.workflowType)}
            </p>
          </div>

          <form onSubmit={handleSave} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {renderFormFields()}
            </div>

            <div>
              <label htmlFor="applyReason" className="block text-sm font-semibold text-gray-700 mb-2">
                申請理由
              </label>
              <textarea
                id="applyReason"
                value={applyReason}
                onChange={(e) => setApplyReason(e.target.value)}
                rows={4}
                className="form-input"
                disabled={submitting}
              />
            </div>

            <div className="flex justify-end gap-3">
              <button 
                type="submit" 
                className="btn-secondary" 
                disabled={submitting}
              >
                {submitting ? '保存中...' : '保存'}
              </button>
              <button 
                type="button"
                onClick={handleApply}
                className="btn-primary" 
                disabled={submitting}
              >
                {submitting ? '申請中...' : '申請'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  );
};

export default EditWorkflowPage;

