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
      // Decode JWT payload for user info
      const payload = JSON.parse(atob(access_token.split('.')[1]));

      dispatch(loginSuccess({
        token: access_token,
        refreshToken: refresh_token,
        user: {
          id: payload.sub,
          username: payload.preferred_username,
          email: payload.email,
          fullName: payload.name || payload.preferred_username,
          role: payload.realm_access?.roles?.includes('ADMIN') ? 'ADMIN' : 'USER',
        },
      }));
      navigate('/');
    } catch (err) {
      dispatch(loginFailure(err.response?.data?.error_description || 'Invalid credentials'));
    }
  };

  const handleDemoLogin = (demoUser, demoPass) => {
    setUsername(demoUser);
    setPassword(demoPass);
  };

  return (
    <div className="login-page">
      <div className="login-bg">
        <div className="login-bg-gradient"></div>
        <div className="login-bg-pattern"></div>
      </div>
      <div className="login-container">
        <div className="login-card">
          <div className="login-header">
            <div className="login-logo">
              <span className="logo-icon large">◆</span>
            </div>
            <h1>VaultCore Financial</h1>
            <p className="login-subtitle">Neo Banking Core System</p>
          </div>

          <form onSubmit={handleLogin} className="login-form">
            {error && <div className="error-message">{error}</div>}
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
              />
            </div>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <div className="demo-credentials">
            <p className="demo-title">Demo Accounts</p>
            <div className="demo-buttons">
              <button className="demo-btn" onClick={() => handleDemoLogin('john.doe', 'password123')}>
                👤 John Doe (User)
              </button>
              <button className="demo-btn" onClick={() => handleDemoLogin('admin', 'admin123')}>
                🛡️ Admin
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
