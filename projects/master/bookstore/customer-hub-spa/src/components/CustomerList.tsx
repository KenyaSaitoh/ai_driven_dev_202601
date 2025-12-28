import { useEffect, useState } from 'react';
import { Customer } from '../types';

interface EditFormData {
  customerName: string;
  email: string;
  birthday: string;
  address: string;
}

const CustomerList = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [formData, setFormData] = useState<EditFormData>({
    customerName: '',
    email: '',
    birthday: '',
    address: ''
  });
  const [saveError, setSaveError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [showToast, setShowToast] = useState<boolean>(false);

  useEffect(() => {
    fetchCustomers();
  }, []);

  const fetchCustomers = async () => {
    try {
      const response = await fetch('/berry-books-rest/customers/');
      if (!response.ok) {
        throw new Error('顧客データの取得に失敗しました');
      }
      const data = await response.json();
      setCustomers(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '不明なエラーが発生しました');
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (customer: Customer) => {
    setEditingCustomer(customer);
    setFormData({
      customerName: customer.customerName,
      email: customer.email,
      birthday: customer.birthday || '',
      address: customer.address
    });
    setSaveError(null);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingCustomer(null);
    setSaveError(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const validateForm = (): string | null => {
    // 顧客名のバリデーション
    if (!formData.customerName.trim()) {
      return '顧客名は必須です';
    }
    if (formData.customerName.length > 100) {
      return '顧客名は100文字以内で入力してください';
    }

    // メールアドレスのバリデーション
    if (!formData.email.trim()) {
      return 'メールアドレスは必須です';
    }
    if (formData.email.length > 254) {
      return 'メールアドレスは254文字以内で入力してください';
    }
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(formData.email)) {
      return 'メールアドレスの形式が正しくありません';
    }

    // 生年月日のバリデーション（任意項目）
    // バリデーションなし

    // 住所のバリデーション
    if (!formData.address.trim()) {
      return '住所は必須です';
    }
    if (formData.address.length > 200) {
      return '住所は200文字以内で入力してください';
    }

    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!editingCustomer) return;

    // フォームバリデーション
    const validationError = validateForm();
    if (validationError) {
      setSaveError(validationError);
      return;
    }

    setIsSaving(true);
    setSaveError(null);

    try {
      const response = await fetch(`/berry-books-rest/customers/${editingCustomer.customerId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        throw new Error('顧客情報の更新に失敗しました');
      }

      // 顧客リストを再取得
      await fetchCustomers();
      handleCloseModal();
      
      // トースト通知を表示
      setShowToast(true);
      setTimeout(() => {
        setShowToast(false);
      }, 3000);
    } catch (err) {
      setSaveError(err instanceof Error ? err.message : '保存中にエラーが発生しました');
    } finally {
      setIsSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <p>読み込み中...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error">
        <p>エラー: {error}</p>
      </div>
    );
  }

  return (
    <>
      <div className="customer-list-container">
        <h2>顧客一覧</h2>
        <table className="book-table">
        <thead>
          <tr>
            <th>顧客ID</th>
            <th>顧客名</th>
            <th>メールアドレス</th>
            <th>生年月日</th>
            <th>住所</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          {customers.map((customer) => (
            <tr key={customer.customerId}>
              <td>{customer.customerId}</td>
              <td>{customer.customerName}</td>
              <td>{customer.email}</td>
              <td>{customer.birthday ? new Date(customer.birthday).toLocaleDateString('ja-JP') : ''}</td>
              <td>{customer.address}</td>
              <td className="action-cell">
                <button 
                  className="edit-button"
                  onClick={() => handleEditClick(customer)}
                >
                  編集
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 編集モーダル */}
      {isModalOpen && editingCustomer && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>顧客情報の編集</h3>
              <button className="close-button" onClick={handleCloseModal}>×</button>
            </div>
            
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="customerName">顧客名 <span className="required">*</span></label>
                <input
                  type="text"
                  id="customerName"
                  name="customerName"
                  value={formData.customerName}
                  onChange={handleInputChange}
                  maxLength={100}
                  required
                />
                <span className="field-hint">最大100文字</span>
              </div>

              <div className="form-group">
                <label htmlFor="email">メールアドレス <span className="required">*</span></label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  maxLength={254}
                  required
                />
                <span className="field-hint">最大254文字</span>
              </div>

              <div className="form-group">
                <label htmlFor="birthday">生年月日</label>
                <input
                  type="date"
                  id="birthday"
                  name="birthday"
                  value={formData.birthday}
                  onChange={handleInputChange}
                />
              </div>

              <div className="form-group">
                <label htmlFor="address">住所 <span className="required">*</span></label>
                <input
                  type="text"
                  id="address"
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                  maxLength={200}
                  required
                />
                <span className="field-hint">最大200文字</span>
              </div>

              {saveError && (
                <div className="form-error">
                  <p>{saveError}</p>
                </div>
              )}

              <div className="form-actions">
                <button 
                  type="button" 
                  className="cancel-button"
                  onClick={handleCloseModal}
                  disabled={isSaving}
                >
                  キャンセル
                </button>
                <button 
                  type="submit" 
                  className="save-button"
                  disabled={isSaving}
                >
                  {isSaving ? '保存中...' : '保存'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      </div>

      {/* トースト通知 */}
      {showToast && (
        <div className="toast">
          <p>保存が完了しました</p>
        </div>
      )}
    </>
  );
};

export default CustomerList;

