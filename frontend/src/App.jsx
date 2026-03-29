import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import AccountsPage from './pages/AccountsPage';
import TransferPage from './pages/TransferPage';
import StocksPage from './pages/StocksPage';
import StatementsPage from './pages/StatementsPage';
import AuditPage from './pages/AuditPage';

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useSelector((state) => state.auth);
  return isAuthenticated ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route index element={<Dashboard />} />
          <Route path="accounts" element={<AccountsPage />} />
          <Route path="transfer" element={<TransferPage />} />
          <Route path="stocks" element={<StocksPage />} />
          <Route path="statements" element={<StatementsPage />} />
          <Route path="audit" element={<AuditPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
