import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Layout from './components/Layout';

const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const AccountsPage = lazy(() => import('./pages/AccountsPage'));
const TransferPage = lazy(() => import('./pages/TransferPage'));
const StocksPage = lazy(() => import('./pages/StocksPage'));
const StatementsPage = lazy(() => import('./pages/StatementsPage'));
const AuditPage = lazy(() => import('./pages/AuditPage'));

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useSelector((state) => state.auth);
  return isAuthenticated ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<div style={{ padding: '24px' }}>Loading...</div>}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<Dashboard />} />
            <Route path="accounts" element={<AccountsPage />} />
            <Route path="transfer" element={<TransferPage />} />
            <Route path="stocks" element={<StocksPage />} />
            <Route path="statements" element={<StatementsPage />} />
            <Route path="audit" element={<AuditPage />} />
          </Route>
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}

export default App;
