import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { CartProvider } from './contexts/CartContext';
import PrivateRoute from './components/PrivateRoute';

// Pages
import LoginPage from './pages/LoginPage';
import BookListPage from './pages/BookListPage';
import BookSearchPage from './pages/BookSearchPage';
import CartPage from './pages/CartPage';
import CartAddedPage from './pages/CartAddedPage';
import OrderConfirmPage from './pages/OrderConfirmPage';
import OrderSuccessPage from './pages/OrderSuccessPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import OrderDetailPage from './pages/OrderDetailPage';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Routes>
            {/* Public Route */}
            <Route path="/" element={<LoginPage />} />

            {/* Private Routes */}
            <Route
              path="/books"
              element={
                <PrivateRoute>
                  <BookListPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/books/search"
              element={
                <PrivateRoute>
                  <BookSearchPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/cart"
              element={
                <PrivateRoute>
                  <CartPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/cart/added"
              element={
                <PrivateRoute>
                  <CartAddedPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/orders/confirm"
              element={
                <PrivateRoute>
                  <OrderConfirmPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/orders/success"
              element={
                <PrivateRoute>
                  <OrderSuccessPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/orders/history"
              element={
                <PrivateRoute>
                  <OrderHistoryPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/orders/:tranId"
              element={
                <PrivateRoute>
                  <OrderDetailPage />
                </PrivateRoute>
              }
            />

            {/* Fallback Route */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;

