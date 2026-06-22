# 🏥 Multispeciality Hospital — Full-Stack (Spring Boot + React)

A small but complete, role-based hospital management app for a fictional
**Multispeciality Hospital** in **Hyderabad, Telangana, India** — built end-to-end so
there are **no gaps** between frontend, API, and database.

- **Backend:** Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA / Hibernate
- **Frontend:** React 18 · React Router · Axios (Vite)
- **Database:** H2 (in-memory, for local learning) or PostgreSQL (Docker)
- **Auth:** stateless JWT · BCrypt password hashing · role-based access control

```
React (browser :5173)  ──HTTP/JSON──►  Spring Boot API (:8080)  ──JPA/SQL──►  DB
        ▲                                       │
        └────────── JWT in Authorization header ┘
```

---

## Roles

| Role          | Can do                                                                       |
|---------------|------------------------------------------------------------------------------|
| `ADMIN`       | Everything: create users, see/create **all** patients, intake & appointments |
| `RECEPTIONIST`| Register & list **all** patients, record intake, book appointments           |
| `DOCTOR`      | See & record intake for **only their own** patients; see **only their** schedule |

The whole project hinges on one field: `Patient.assigned_doctor_id`. That drives the
rule *"a doctor sees only their patients"* — **enforced on the server** (in
`PatientService`), never just hidden in the UI.

---

## Quick start

### Option A — Local (no Docker), H2 in-memory DB

You need JDK 21 and Node 20. **Two terminals:**

**1) Backend** (the included Maven wrapper downloads Maven automatically):
```bash
# from the project root
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"   # ensure JDK 21
./mvnw spring-boot:run
```
API on **http://localhost:8080** · Swagger UI on **http://localhost:8080/swagger-ui.html**

**2) Frontend:**
```bash
cd frontend
npm install
npm run dev
```
App on **http://localhost:5173**

> The `dev` profile uses an in-memory H2 database that is re-seeded on every start,
> so you always begin from a clean, known dataset.

### Option B — Docker (PostgreSQL, one command)

```bash
docker compose up --build        # Docker Desktop
# or, if you installed the standalone binary (Colima/Homebrew):
docker-compose up --build
```
Then open **http://localhost:5173**. This runs three containers: `db` (Postgres),
`backend` (Spring Boot, `postgres` profile), and `frontend` (Vite). Stop with
`docker-compose down` (add `-v` to also wipe the database volume).

---

## Demo logins (seeded automatically)

| Email                  | Password       | Role         | Name |
|------------------------|----------------|--------------|------|
| rakesh@hospital.in     | admin123       | ADMIN        | Rakesh Ramgiri |
| reception@hospital.in  | reception123   | RECEPTIONIST | Sunita Reddy |
| kamlesh@hospital.in    | doctor123      | DOCTOR (Cardiology) | Dr. Kamlesh Ramgiri |
| ravali@hospital.in     | doctor123      | DOCTOR (Neurology)  | Dr. Ravali Ramgiri |

> Fake data only — never put real patient information in a learning project.

**Try the access control:** log in as `kamlesh@hospital.in` — you'll see only your
patients. Log in as `rakesh@hospital.in` — you'll see all of them. As Dr. Kamlesh,
opening one of Dr. Ravali's patients returns **403**.

---

## API reference

All endpoints are under `/api`. Protected endpoints need
`Authorization: Bearer <token>`.

### Auth
| Method | Path                | Who          | Body / Notes |
|--------|---------------------|--------------|--------------|
| POST   | `/api/auth/login`   | public       | `{email, password}` → `{access_token, token_type, role, user_id}` |
| POST   | `/api/auth/register`| **ADMIN**    | `{name, email, password, role, specialization?}` → 201 user |
| GET    | `/api/auth/me`      | any logged-in| current user `{id, name, email, role}` |

### Patients
| Method | Path                       | Who                         | Notes |
|--------|----------------------------|-----------------------------|-------|
| POST   | `/api/patients`            | ADMIN, RECEPTIONIST         | create patient |
| GET    | `/api/patients?search=`    | any (role-filtered results) | admin/reception → all; doctor → own |
| GET    | `/api/patients/{id}`       | any (ownership-checked)     | 403 if a doctor requests another's patient; includes `intake_forms` |
| POST   | `/api/patients/{id}/intake`| ADMIN, RECEPTIONIST, DOCTOR | record a visit |

### Appointments
| Method | Path                              | Who                         | Notes |
|--------|-----------------------------------|-----------------------------|-------|
| POST   | `/api/appointments`               | ADMIN, RECEPTIONIST         | `{patient_id, doctor_id, scheduled_at, reason}` |
| GET    | `/api/appointments`               | any (role-filtered)         | paginated; `?page=&size=&patientId=`; doctor → only their schedule |
| PATCH  | `/api/appointments/{id}/status`   | ADMIN, RECEPTIONIST, own DOCTOR | `{status: SCHEDULED\|COMPLETED\|CANCELLED}` |

### Doctors
| Method | Path           | Who           | Notes |
|--------|----------------|---------------|-------|
| GET    | `/api/doctors` | any logged-in | for the assignment dropdown |

### Pagination
List endpoints (`/api/patients`, `/api/appointments`) accept `?page=` (0-based) and
`?size=` and return an envelope:
```jsonc
{ "content": [ ... ], "page": 0, "size": 10,
  "total_elements": 14, "total_pages": 2, "first": true, "last": false }
```

### Standard error shape
```jsonc
401 → { "detail": "Could not validate credentials" }
403 → { "detail": "Insufficient permissions" }          // or "Not authorized to view this patient"
404 → { "detail": "Patient not found" }
422 → { "detail": [ { "loc": "body.age", "msg": "age must be 0 or greater" } ] }
```

---

## How it works, layer by layer

### 1. Authentication (JWT)
1. `POST /api/auth/login` → `AuthService` asks Spring Security's `AuthenticationManager`
   to verify the email + BCrypt-hashed password.
2. On success, `JwtService` issues a signed JWT (subject = email, claim = role).
3. The frontend stores it (`localStorage`) and the Axios **request interceptor**
   (`frontend/src/api/client.js`) attaches it to every call.
4. On each request, `JwtAuthFilter` verifies the signature, loads the user, and marks
   the request authenticated.

### 2. Authorization (roles)
- **Coarse gates** (which role may hit an endpoint) use `@PreAuthorize` on controllers,
  enabled by `@EnableMethodSecurity` in `SecurityConfig`.
- **Fine-grained data rules** (which *records* you may see) live in `PatientService`:
  admins/receptionists get all patients; a doctor's query is filtered to their
  `assigned_doctor_id`, and per-record access throws 403 if a doctor reaches for
  someone else's patient.

### 3. Data layer
JPA entities (`model/`) ↔ tables, via Spring Data repositories (`repo/`). Hibernate's
`ddl-auto: update` auto-creates the schema from the entities (fine for learning; use
Flyway/Liquibase migrations for real projects).

### 4. Frontend
React Router pages under `frontend/src/pages/`. `AuthContext` holds the session;
`ProtectedRoute` redirects based on login/role (UX only — the backend is the real
gatekeeper).

---

## Project structure

```
Hospital/
├── pom.xml                       # backend build (Spring Boot)
├── mvnw / .mvn/                  # Maven wrapper (no system Maven needed)
├── Dockerfile                    # backend image
├── docker-compose.yml            # db + backend + frontend
├── src/
│   ├── main/java/com/hospital/
│   │   ├── HospitalApplication.java
│   │   ├── model/      # User, Doctor, Patient, IntakeForm, Role
│   │   ├── repo/       # Spring Data JPA repositories
│   │   ├── dto/        # request/response shapes (the API contract)
│   │   ├── security/   # JwtService, JwtAuthFilter, AppUserDetails, UserDetailsService
│   │   ├── config/     # SecurityConfig, OpenApiConfig, DataSeeder, 401/403 handlers
│   │   ├── service/    # AuthService, PatientService, DoctorService (business logic)
│   │   └── web/        # AuthController, PatientController, DoctorController, error handler
│   ├── main/resources/application.yml   # dev (H2) + postgres profiles
│   └── test/java/com/hospital/AccessControlTest.java
└── frontend/
    └── src/
        ├── api/client.js         # axios + token interceptors
        ├── context/AuthContext.jsx
        ├── components/           # Navbar, ProtectedRoute
        └── pages/                # Login, Dashboard, PatientList, PatientDetail, NewPatient, Register
```

---

## Tests

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
./mvnw test
```
`AccessControlTest` boots the real app on a random port and verifies the core rules:
admin sees all, doctor sees only their own, doctor gets 403 on another's patient,
unauthenticated → 401, doctor cannot register users.

---

## Notes & next steps

- **Security:** set a strong `JWT_SECRET` env var in any real deployment.
- The API contract returns the full `intake_forms` list on patient detail (a patient
  can have multiple visits) rather than a single embedded intake.
- Natural extensions: appointments, pagination, refresh tokens, DB migrations,
  audit logging. Intentionally out of scope to keep this "minor".
```
