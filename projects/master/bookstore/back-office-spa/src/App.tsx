import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import WorkflowListPage from './pages/WorkflowListPage';
import WorkflowDetailPage from './pages/WorkflowDetailPage';
import CreateWorkflowPage from './pages/CreateWorkflowPage';
import './styles/App.css';

const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          
          <Route
            path="/workflows"
            element={
              <ProtectedRoute>
                <WorkflowListPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/workflows/create"
            element={
              <ProtectedRoute>
                <CreateWorkflowPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/workflows/:workflowId"
            element={
              <ProtectedRoute>
                <WorkflowDetailPage />
              </ProtectedRoute>
            }
          />
          
          <Route path="/" element={<Navigate to="/workflows" replace />} />
          <Route path="*" element={<Navigate to="/workflows" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
};

export default App;

