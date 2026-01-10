import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { workflowService } from '../services/workflowService';
import { WorkflowCreateRequest, Category, Publisher, Book } from '../types';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';

const CreateWorkflowPage: React.FC = () => {
  const [workflowType, setWorkflowType] = useState<string>('ADD_NEW_BOOK');
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
  const [loading, setLoading] = useState(false);
  const { employee } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    loadMasterData();
  }, []);

  const loadMasterData = async () => {
    try {
      const [categoriesData, publishersData, booksData] = await Promise.all([
        workflowService.getCategories(),
        workflowService.getPublishers(),
        workflowService.getBooks(),
      ]);
      
      setCategories(categoriesData);
      setPublishers(publishersData);
      setBooks(booksData);
    } catch (err) {
      console.error('Failed to load master data:', err);
    }
  };

  const validateAndCreateRequest = (): WorkflowCreateRequest | null => {
    if (!employee) {
      setError('ログインが必要です');
      return null;
    }

    const request: WorkflowCreateRequest = {
      workflowType,
      createdBy: employee.employeeId,
      applyReason,
    };

    // ワークフロータイプごとに必要なフィールドを設定
    switch (workflowType) {
      case 'ADD_NEW_BOOK':
        if (!bookName || !author || !price || !categoryId || !publisherId) {
          setError('すべての必須項目を入力してください');
          return null;
        }
        request.bookName = bookName;
        request.author = author;
        request.price = Number(price);
        request.imageUrl = imageUrl;
        request.categoryId = Number(categoryId);
        request.publisherId = Number(publisherId);
        break;

      case 'REMOVE_BOOK':
        if (!bookId) {
          setError('削除する書籍を選択してください');
          return null;
        }
        request.bookId = Number(bookId);
        break;

      case 'PRICE_TEMP_ADJUSTMENT':
        if (!bookId || !price || !startDate || !endDate) {
          setError('すべての必須項目を入力してください');
          return null;
        }
        
        // 日付バリデーション
        const today = new Date();
        today.setHours(0, 0, 0, 0); // 時刻をリセットして日付のみで比較
        
        const start = new Date(startDate);
        const end = new Date(endDate);
        
        if (start < today) {
          setError('開始日は今日以降の日付を指定してください');
          return null;
        }
        
        if (end < start) {
          setError('終了日は開始日以降の日付を指定してください');
          return null;
        }
        
        request.bookId = Number(bookId);
        request.price = Number(price);
        request.startDate = startDate;
        request.endDate = endDate;
        break;

      default:
        setError('不明なワークフロータイプです');
        return null;
    }

    return request;
  };

  const handleSave = async () => {
    setError('');
    setLoading(true);

    try {
      const request = validateAndCreateRequest();
      if (!request || !employee) {
        setLoading(false);
        return;
      }

      const createdWorkflow = await workflowService.createWorkflow(request);
      navigate(`/workflows/${createdWorkflow.workflowId}`);
    } catch (err: any) {
      console.error('Failed to create workflow:', err);
      setError(err.response?.data?.message || 'ワークフローの作成に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async () => {
    setError('');
    setLoading(true);

    try {
      const request = validateAndCreateRequest();
      if (!request || !employee) {
        setLoading(false);
        return;
      }

      // ワークフローを作成
      const createdWorkflow = await workflowService.createWorkflow(request);
      
      // 即座に申請
      await workflowService.applyWorkflow(createdWorkflow.workflowId, {
        operatedBy: employee.employeeId,
      });

      navigate(`/workflows/${createdWorkflow.workflowId}`);
    } catch (err: any) {
      console.error('Failed to create and apply workflow:', err);
      setError(err.response?.data?.message || 'ワークフローの作成・申請に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const renderFormFields = () => {
    switch (workflowType) {
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
                disabled={loading}
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
                disabled={loading}
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
                disabled={loading}
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
                disabled={loading}
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
                disabled={loading}
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
                disabled={loading}
              />
            </div>
          </>
        );

      case 'REMOVE_BOOK':
        return (
          <div className="md:col-span-2">
            <label htmlFor="bookId" className="block text-sm font-semibold text-gray-700 mb-2">
              削除する書籍 *
            </label>
            <select
              id="bookId"
              value={bookId}
              onChange={(e) => setBookId(e.target.value ? Number(e.target.value) : '')}
              className="form-input"
              required
              disabled={loading}
            >
              <option value="">選択してください</option>
              {books.map((book) => (
                <option key={book.bookId} value={book.bookId}>
                  {book.bookName} - {book.author}
                </option>
              ))}
            </select>
          </div>
        );

      case 'PRICE_TEMP_ADJUSTMENT':
        return (
          <>
            <div className="md:col-span-2">
              <label htmlFor="bookId" className="block text-sm font-semibold text-gray-700 mb-2">
                価格改定する書籍 *
              </label>
              <select
                id="bookId"
                value={bookId}
                onChange={(e) => setBookId(e.target.value ? Number(e.target.value) : '')}
                className="form-input"
                required
                disabled={loading}
              >
                <option value="">選択してください</option>
                {books.map((book) => (
                  <option key={book.bookId} value={book.bookId}>
                    {book.bookName} - 現在価格: ¥{book.price.toLocaleString()}
                  </option>
                ))}
              </select>
            </div>

            <div className="md:col-span-2">
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
                disabled={loading}
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
                disabled={loading}
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
                disabled={loading}
                min={startDate || new Date().toISOString().split('T')[0]}
              />
            </div>
          </>
        );

      default:
        return null;
    }
  };

  return (
    <Layout>
      <div className="space-y-6">
        <div className="page-header">
          <h1>新規ワークフロー作成</h1>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6 space-y-6">
          <div>
            <label htmlFor="workflowType" className="block text-sm font-semibold text-gray-700 mb-2">
              ワークフロータイプ *
            </label>
            <select
              id="workflowType"
              value={workflowType}
              onChange={(e) => setWorkflowType(e.target.value)}
              className="form-input"
              required
              disabled={loading}
            >
              <option value="ADD_NEW_BOOK">新規書籍の追加</option>
              <option value="REMOVE_BOOK">既存書籍の削除</option>
              <option value="PRICE_TEMP_ADJUSTMENT">価格の一時調整</option>
            </select>
          </div>

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
              disabled={loading}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="flex justify-end gap-3">
            <button 
              type="button"
              onClick={handleSave}
              className="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed" 
              disabled={loading}
            >
              {loading ? '保存中...' : '保存'}
            </button>
            <button 
              type="button"
              onClick={handleApply}
              className="btn-primary disabled:opacity-50 disabled:cursor-not-allowed" 
              disabled={loading}
            >
              {loading ? '申請中...' : '申請'}
            </button>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default CreateWorkflowPage;

