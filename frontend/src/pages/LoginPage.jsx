import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { loginStart, loginSuccess, loginFailure } from '../features/auth/authSlice';
import axios from 'axios';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleLogin = async (e) => {
    e.preventDefault();
    dispatch(loginStart());
    try {
      const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:9090';
      const response = await axios.post(
        `${keycloakUrl}/realms/vaultcore/protocol/openid-connect/token`,
        new URLSearchParams({
          grant_type: 'password',
          client_id: 'vaultcore-app',
          username,
          password,
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
      );

      const { access_token, refresh_token } = response.data;
      
      // IDENTITY SYNC: Fetch internal user profile from backend
      // VITE_BACKEND_URL is the bare server base (no /api/v1)
      const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
      const profileResponse = await axios.get(`${backendUrl}/api/v1/users/me`, {
        headers: { Authorization: `Bearer ${access_token}` }
      });

      const internalUser = profileResponse.data;

      dispatch(loginSuccess({
        token: access_token,
        refreshToken: refresh_token,
        user: {
          id: internalUser.id,           // Internal DB UUID
          keycloakId: internalUser.id,   // Also map to id for compatibility
          username: internalUser.username,
          email: internalUser.email,
          fullName: internalUser.fullName,
          role: internalUser.role,
        },
      }));
      navigate('/');
    } catch (err) {
      console.error('Login error:', err);
      const msg = err.response?.data?.error_description
        || err.response?.data?.message
        || err.message
        || 'Invalid credentials or server unavailable';
      dispatch(loginFailure(msg));
    }
  };

  return (
    <div className="login-page">
      <div className="login-bg">
        <div className="login-bg-gradient"></div>
      </div>
      <div className="login-container">
        <div className="login-card">
          <div className="login-header">
            <div className="login-logo">
              <span className="material-symbols-outlined logo-icon">shield</span>
            </div>
            <h1>VaultCore</h1>
            <p className="login-subtitle">Secure access to your institutional ledger.</p>
          </div>

          <form onSubmit={handleLogin} className="login-form">
            {error && <div className="error-message">{error}</div>}
            <div className="form-group">
              <label htmlFor="username">Node Identifier</label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Username or ID"
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">Security Protocol</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Secure Password"
                required
              />
            </div>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'AUTHENTICATING...' : 'AUTHORIZE SESSION'}
            </button>
          </form>

          <div className="login-footer">
            <p>Non-registered node? <span className="signup-link" onClick={() => navigate('/register')}>Request Access</span></p>
          </div>
        </div>
      </div>
    </div>

  );
}
