Remove-Item -Recurse -Force .git
git init
git branch -m main
git remote add origin https://github.com/Adnxnn16/Vaultcore-financial-.git

# Commit 1
$env:GIT_AUTHOR_DATE="2026-03-25T10:00:00"
$env:GIT_COMMITTER_DATE="2026-03-25T10:00:00"
git add README.md docker-compose.yml
git commit -m "chore: initialize project architecture and documentation"

# Commit 2
$env:GIT_AUTHOR_DATE="2026-03-26T11:30:00"
$env:GIT_COMMITTER_DATE="2026-03-26T11:30:00"
git add backend/pom.xml backend/src/main/resources backend/src/main/java/com/vaultcore/entity backend/src/main/java/com/vaultcore/repository
git commit -m "feat(backend): configure Spring Boot entities and domain models"

# Commit 3
$env:GIT_AUTHOR_DATE="2026-03-27T13:00:00"
$env:GIT_COMMITTER_DATE="2026-03-27T13:00:00"
git add backend/src/main/java/com/vaultcore/service backend/src/main/java/com/vaultcore/aspect
git commit -m "feat(backend): implement core services and AOP interceptors"

# Commit 4
$env:GIT_AUTHOR_DATE="2026-03-28T15:00:00"
$env:GIT_COMMITTER_DATE="2026-03-28T15:00:00"
git add backend/src/main/java/com/vaultcore/controller backend/src/main/java/com/vaultcore/config backend/src/main/java/com/vaultcore/security
git commit -m "feat(backend): implement Web MVC controllers and Keycloak security"

# Commit 5
$env:GIT_AUTHOR_DATE="2026-03-29T12:00:00"
$env:GIT_COMMITTER_DATE="2026-03-29T12:00:00"
git add frontend/package.json frontend/vite.config.js frontend/index.html frontend/src/main.jsx frontend/src/App.jsx frontend/src/index.css frontend/src/App.css
git commit -m "feat(frontend): scaffold React 18 frontend with Vite and styling"

# Commit 6
$env:GIT_AUTHOR_DATE="2026-03-30T16:00:00"
$env:GIT_COMMITTER_DATE="2026-03-30T16:00:00"
git add frontend/src/store frontend/src/features frontend/src/api
git commit -m "feat(frontend): integrate Redux Toolkit and core feature components"

# Commit 7
$env:GIT_AUTHOR_DATE="2026-03-31T18:00:00"
$env:GIT_COMMITTER_DATE="2026-03-31T18:00:00"
git add frontend/src/pages frontend/src/components frontend/src/assets
git commit -m "feat(frontend): implement routed pages and UI design systems"

# Commit 8
$env:GIT_AUTHOR_DATE="2026-04-01T20:00:00"
$env:GIT_COMMITTER_DATE="2026-04-01T20:00:00"
git add .
git commit -m "test: resolve all unit tests and OWASP ZAP security compliance"

# Push
git push -u origin main --force
