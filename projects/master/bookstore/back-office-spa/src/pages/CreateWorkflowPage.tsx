import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { workflowService } from '../services/workflowService';
import { WorkflowCreateRequest, Category, Publisher, Book } from '../types';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/Layout';
import '../styles/CreateWorkflowPage.css';

const CreateWorkflowPage: React.FC = () => {
  const [workflowType, setWorkflowType] = useState<string>('CREATE');
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
      const [categoriesMap, publishersData, booksData] = await Promise.all([
        workflowService.getCategories(),
        workflowService.getPublishers(),
        workflowService.getBooks(),
      ]);
      
      // CategoryMapをCategory配列に変換
      const categoriesArray: Category[] = Object.entries(categoriesMap).map(
        ([categoryName, categoryId]) => ({
          categoryId,
          categoryName,
        })
      );
      
      setCategories(categoriesArray);
      setPublishers(publishersData);
      setBooks(booksData);
    } catch (err) {
      console.error('Failed to load master data:', err);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!employee) {
      setError('ログインが必要です');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const request: WorkflowCreateRequest = {
        workflowType,
        createdBy: employee.employeeId,
        applyReason,
      };

      // ワークフロータイプごとに必要なフィールドを設定
      switch (workflowType) {
        case 'CREATE':
          if (!bookName || !author || !price || !categoryId || !publisherId) {
            setError('すべての必須項目を入力してください');
            setLoading(false);
            return;
          }
          request.bookName = bookName;
          request.author = author;
          request.price = Number(price);
          request.imageUrl = imageUrl;
          request.categoryId = Number(categoryId);
          request.publisherId = Number(publisherId);
          break;

        case 'DELETE':
          if (!bookId) {
            setError('削除する書籍を選択してください');
            setLoading(false);
            return;
          }
          request.bookId = Number(bookId);
          break;

        case 'PRICE_TEMP_ADJUSTMENT':
          if (!bookId || !price || !startDate || !endDate) {
            setError('すべての必須項目を入力してください');
            setLoading(false);
            return;
          }
          request.bookId = Number(bookId);
          request.price = Number(price);
          request.startDate = startDate;
          request.endDate = endDate;
          break;

        default:
          setError('不明なワークフロータイプです');
          setLoading(false);
          return;
      }

      const createdWorkflow = await workflowService.createWorkflow(request);
      alert(`ワークフローを作成しました (ID: ${createdWorkflow.workflowId})`);
      navigate(`/workflows/${createdWorkflow.workflowId}`);
    } catch (err: any) {
      console.error('Failed to create workflow:', err);
      setError(err.response?.data?.message || 'ワークフローの作成に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const renderFormFields = () => {
    switch (workflowType) {
      case 'CREATE':
        return (
          <>
            <div className="form-group">
              <label htmlFor="bookName">書籍名 *</label>
              <input
                type="text"
                id="bookName"
                value={bookName}
                onChange={(e) => setBookName(e.target.value)}
                required
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="author">著者 *</label>
              <input
                type="text"
                id="author"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                required
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="price">価格 *</label>
              <input
                type="number"
                id="price"
                value={price}
                onChange={(e) => setPrice(e.target.value ? Number(e.target.value) : '')}
                required
                min="0"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="categoryId">カテゴリ *</label>
              <select
                id="categoryId"
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : '')}
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

            <div className="form-group">
              <label htmlFor="publisherId">出版社 *</label>
              <select
                id="publisherId"
                value={publisherId}
                onChange={(e) => setPublisherId(e.target.value ? Number(e.target.value) : '')}
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

            <div className="form-group">
              <label htmlFor="imageUrl">画像URL</label>
              <input
                type="text"
                id="imageUrl"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                disabled={loading}
              />
            </div>
          </>
        );

      case 'DELETE':
        return (
          <div className="form-group">
            <label htmlFor="bookId">削除する書籍 *</label>
            <select
              id="bookId"
              value={bookId}
              onChange={(e) => setBookId(e.target.value ? Number(e.target.value) : '')}
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
            <div className="form-group">
              <label htmlFor="bookId">価格調整する書籍 *</label>
              <select
                id="bookId"
                value={bookId}
                onChange={(e) => setBookId(e.target.value ? Number(e.target.value) : '')}
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

            <div className="form-group">
              <label htmlFor="price">調整後の価格 *</label>
              <input
                type="number"
                id="price"
                value={price}
                onChange={(e) => setPrice(e.target.value ? Number(e.target.value) : '')}
                required
                min="0"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="startDate">開始日 *</label>
              <input
                type="date"
                id="startDate"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="endDate">終了日 *</label>
              <input
                type="date"
                id="endDate"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
                disabled={loading}
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
      <div className="create-workflow-container">
        <div className="page-header">
          <h1>新規ワークフロー作成</h1>
          <button onClick={() => navigate('/workflows')} className="btn btn-secondary">
            キャンセル
          </button>
        </div>

        <form onSubmit={handleSubmit} className="create-workflow-form">
          <div className="form-group">
            <label htmlFor="workflowType">ワークフロータイプ *</label>
            <select
              id="workflowType"
              value={workflowType}
              onChange={(e) => setWorkflowType(e.target.value)}
              required
              disabled={loading}
            >
              <option value="CREATE">新規作成</option>
              <option value="DELETE">削除</option>
              <option value="PRICE_TEMP_ADJUSTMENT">価格調整</option>
            </select>
          </div>

          {renderFormFields()}

          <div className="form-group">
            <label htmlFor="applyReason">申請理由</label>
            <textarea
              id="applyReason"
              value={applyReason}
              onChange={(e) => setApplyReason(e.target.value)}
              rows={4}
              disabled={loading}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? '作成中...' : 'ワークフローを作成'}
          </button>
        </form>
      </div>
    </Layout>
  );
};

export default CreateWorkflowPage;

