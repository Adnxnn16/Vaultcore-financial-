# VaultCore Financial

VaultCore Financial is an enterprise-grade Neo-Banking Core system, designed to handle high transaction volumes with sub-50ms latency while ensuring 100% ACID compliance and banking-level security.

## Prerequisites
Before you begin, ensure you have the following installed on your local machine:
- **Docker & Docker Compose** (for running databases and Keycloak)
- **Java 21** (for compiling and running the backend)
- **Apache Maven** (or you can use the included `mvnw` wrapper)
- **Node.js (v18+) & npm** (for the React frontend)

> ⚠️ **IMPORTANT: Ensure Docker Desktop (or the Docker daemon) is open and running on your machine before executing any of the setup commands below.**

---

## 🚀 Option 1: One-Click Full Docker Setup (Easiest)

If you simply want to run the entire project without setting up the local development environment:

1. Open a terminal in the root directory (`VAULTCORE FINANCIAL`).
2. Run the following command:
   ```bash
   docker-compose up -d --build
   ```
3. Docker will build the backend, build the frontend, and start the Postgres, Redis, and Keycloak instances.
4. Access the application at **http://localhost:3000**

*(Note: Keycloak may take ~30 seconds to fully initialize its database on the first run. If the backend fails to connect immediately, wait 30 seconds and run `docker-compose restart backend`)*

---

## 💻 Option 2: Local Development Setup (Recommended for Evaluation)

Follow these steps to run the infrastructure in Docker, but compile and run the Frontend and Backend locally.

### Step 1: Start the Infrastructure (Database, Redis, & Auth)
Open a terminal in the root directory and start PostgreSQL, Redis, and Keycloak:
```bash
docker-compose up -d postgres redis keycloak
```
*Wait about 30-40 seconds for Keycloak to fully initialize its database.*

### Step 2: Start the Backend (Spring Boot)
1. Open a new terminal and navigate to the `backend` folder:
   ```bash
   cd backend
   ```
2. Compile and run the application:
   ```bash
   mvn spring-boot:run
   ```
3. The backend will start on **http://localhost:8080**.

### Step 3: Start the Frontend (React)
1. Open a new terminal and navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
2. Install the dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. The frontend will start on **http://localhost:5173** (or the port specified in your terminal, often `3000` or `5173`).

---

## 🧪 Running the Tests

To verify the integrity of the core banking logic, you can run the comprehensive backend test suite.
1. Navigate to the `backend` folder.
2. Run the tests:
   ```bash
   mvn clean test
   ```
*This will execute all unit and integration tests (including the Double-Entry Ledger, MFA Verification, and Fraud Detection tests).*

---

## 🔗 Important URLs & Credentials

Once the system is up and running, you can access the following services:

| Service | URL | Credentials |
|---------|-----|-------------|
| **VaultCore Web App** | [http://localhost:3000](http://localhost:3000) (or `5173`) | **User:** `john.doe` <br> **Pass:** `Password123` <br> *(Or register a new user via UI)* |
| **Backend API Docs (Swagger)** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | N/A |
| **Keycloak Admin Console** | [http://localhost:9090](http://localhost:9090) | **User:** `admin` <br> **Pass:** `admin` |

---

## 🛑 Stopping the Application

To completely shut down the project when you are finished:

1. **Stop the Frontend & Backend**: If you are running them manually in your terminal, simply go to their respective terminal windows and press `Ctrl + C`.
2. **Stop the Infrastructure (Docker)**: Open a terminal in the root directory and run:
   ```bash
   docker-compose down
   ```
   *(This will safely stop and clean up Keycloak, PostgreSQL, and Redis)*

---

## ⚙️ Core Features
1. **Double-Entry Ledger Architecture**: Strictly enforces immutable transaction states and 100% ACID compliance.
2. **Advanced Security**: Keycloak integration for OAuth2/OIDC, plus Spring Security for RBAC.
3. **Fraud & Risk Mitigation**: Transactions exceeding thresholds trigger MFA Challenges (2FA) backed by Redis.
4. **Modern UI/UX**: Built with React and TailwindCSS using a premium, high-density professional design system.

---

## 📸 Screenshots

### User Login
![User Login](screenshots/1.User%20Login.png)

### User Dashboard
![User Dashboard](screenshots/2.User%20Dashboard.png)

### My Account
![My Account](screenshots/3.My%20Account.png)

### Money Transfer
![Money Transfer](screenshots/4.Money%20Transfer.png)

### Stock Portfolio
![Stock Portfolio](screenshots/5.Stock%20Portfolio.png)

### Account Statements
![Account Statements](screenshots/6.Account%20Statments.png)
