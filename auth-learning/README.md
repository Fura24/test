# Auth Learning Project

A step-by-step login system with proper auth, security and RBAC.
Stack: Java (Spring Boot) · PostgreSQL / Supabase · Next.js + React · Vercel · Docker Compose.

## Roadmap

| Step | Topic | Status |
|------|-------|--------|
| 1 | Docker Compose, Spring Boot skeleton, DB schema (users/roles), Flyway | done |
| 2 | Register + login: BCrypt, validation, JWT access tokens | this step |
| 3 | JWT filter, refresh tokens, logout/revocation | |
| 4 | RBAC: `ROLE_USER` / `ROLE_ADMIN`, `@PreAuthorize`, protected endpoints | |
| 5 | Security hardening: rate limiting, lockout, CORS, headers, secrets | |
| 6 | Switch DB to Supabase (managed Postgres) | |
| 7 | Next.js frontend: login UI, protected routes, httpOnly cookies | |
| 8 | Deploy: Vercel (frontend) + container host for the Java API | |

## Run step 1

```bash
cp .env.example .env        # then edit DB_PASSWORD
docker compose up --build
```

Verify:

```bash
curl http://localhost:8080/actuator/health      # {"status":"UP"}
curl -i http://localhost:8080/anything          # 401 (everything else is locked by default)

docker exec -it auth-db psql -U authuser -d authdb -c '\dt'
docker exec -it auth-db psql -U authuser -d authdb -c 'select * from roles;'
```

Reset the database: `docker compose down -v`.

## What to learn in this step

- **Deny by default.** `SecurityConfig` makes every route require auth except the health check. You open routes deliberately, never the reverse.
- **Flyway owns the schema.** Hibernate is set to `validate`, so it never silently alters tables. Schema changes are versioned SQL files (`V2__...sql`).
- **Never store passwords.** Only a BCrypt hash goes in `password_hash`.
- **RBAC shape.** `users` <-> `user_roles` <-> `roles`. A user can hold many roles; permissions are checked against role names.
- **Case-insensitive unique email** via an index on `lower(email)`.
- **Container hygiene.** Multi-stage build, non-root runtime user, secrets in `.env` (git-ignored), not in the image.

## Step 2: register and login

Add a `JWT_SECRET` line to your `.env` (see `.env.example` for how to generate one), then:

```bash
docker compose up --build
```

The API is on port 8081. In PowerShell:

```powershell
# register
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/register -ContentType "application/json" -Body '{"email":"me@example.com","password":"a-long-password-123"}'

# login (returns accessToken)
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/login -ContentType "application/json" -Body '{"email":"me@example.com","password":"a-long-password-123"}'
```

Paste the token into https://jwt.io to read its payload (sub, email, roles, exp).

Things to try: register the same email twice (409), a short password (400), a wrong password (401),
then look at the stored hash: `select email, password_hash from users;`

Request flow: `AuthController` (HTTP) -> `AuthService` (rules) -> `UserRepository` (database),
and `JwtService` signs the token. Step 3 adds the filter that checks tokens on incoming requests.

## Supabase note (step 6)

Supabase is Postgres, so the Java code doesn't change. You only swap `DB_URL`, `DB_USER`, `DB_PASSWORD`
to Supabase's connection string. We are using Supabase purely as the database and doing auth in Java
on purpose, since that's the backend you want to learn. (Supabase Auth exists, but it would replace the part you're studying.)
