# Zentro

E-commerce backend built with Spring Boot 3.2 and Java 21.

## Tech Stack

- **Java 21** + **Spring Boot 3.2**
- **PostgreSQL** with Spring Data JPA
- **Spring Security** with JWT authentication
- **Cloudflare R2** for file storage (S3-compatible)
- **Resend** for transactional emails
- **Razorpay** for payments (integration ready)

## Project Structure

```
backend/src/main/java/com/zentro/
├── common/
│   ├── config/          # Security, CORS configuration
│   ├── dto/             # ApiResponse, ErrorResponse, PageResponse
│   ├── exception/       # Global exception handler + custom exceptions
│   ├── security/        # JWT filter, token provider, UserPrincipal
│   ├── service/         # R2 storage service
│   ├── util/            # Constants, PublicIdGenerator
│   └── validator/       # Custom validators (username)
├── config/              # Environment configuration
└── feature/
    ├── auth/            # Authentication (signup, login, OTP, password reset)
    ├── brand/           # Brand management
    ├── category/        # Category hierarchy
    └── user/            # User profiles, addresses
```

Each feature follows the same structure: `controller/`, `dto/`, `entity/`, `repository/`, `service/`.

## Authentication Flow

The auth system uses a dual-token approach:

**Access Token** (1 hour) - Sent in Authorization header for API requests
**Refresh Token** (7 days) - Stored hashed in DB, used to get new access tokens

### Signup Process

1. User submits email, password, name
2. Account created with `emailVerified = false`
3. 6-digit OTP sent via email (valid for 5 minutes)
4. User verifies OTP → account activated → JWT tokens returned

### Password Reset

1. User requests reset → OTP sent to email
2. OTP verified → temporary token issued (5 min expiry)
3. Temporary token + new password submitted → password updated

### Security Measures

- OTP rate limiting: max 3 requests per hour
- Account lockout after 10 failed OTP attempts (1 hour)
- Passwords hashed with BCrypt (strength 12)
- Refresh tokens stored as SHA-256 hashes
- Public IDs exposed in API (internal DB IDs hidden)

## API Endpoints

### Auth (`/api/v1/auth`)

| Method | Endpoint            | Description                                 |
| ------ | ------------------- | ------------------------------------------- |
| POST   | `/signup`           | Register new user                           |
| POST   | `/admin/signup`     | Register admin (requires secret key header) |
| POST   | `/login`            | Login with email/password                   |
| POST   | `/verify-email`     | Verify email with OTP                       |
| POST   | `/resend-otp`       | Resend verification OTP                     |
| POST   | `/forgot-password`  | Request password reset OTP                  |
| POST   | `/verify-reset-otp` | Verify reset OTP, get temp token            |
| POST   | `/reset-password`   | Reset password with temp token              |
| POST   | `/refresh`          | Refresh access token                        |
| POST   | `/logout`           | Invalidate refresh token                    |

### Users (`/api/v1/users`)

| Method | Endpoint           | Description                       |
| ------ | ------------------ | --------------------------------- |
| GET    | `/profile`         | Get current user profile          |
| PUT    | `/profile`         | Update profile                    |
| PUT    | `/username`        | Update username (30-day cooldown) |
| PUT    | `/profile/picture` | Upload profile picture            |
| DELETE | `/profile/picture` | Remove profile picture            |
| DELETE | `/profile`         | Soft delete account               |

### Brands (`/api/v1/brands`)

| Method | Endpoint      | Description         |
| ------ | ------------- | ------------------- |
| GET    | `/`           | List all brands     |
| GET    | `/{publicId}` | Get brand by ID     |
| GET    | `/featured`   | Get featured brands |

### Admin - Brands (`/api/v1/admin/brands`)

| Method | Endpoint      | Description  |
| ------ | ------------- | ------------ |
| POST   | `/`           | Create brand |
| PUT    | `/{publicId}` | Update brand |
| DELETE | `/{publicId}` | Delete brand |

## Key Implementation Details

### Public ID Pattern

Internal database IDs are never exposed. Each entity has a `publicId` field with a prefix:

- Users: `USR_abc123...`
- Admins: `ADM_abc123...`
- Categories: `CAT_abc123...`
- Brands: `BRD_abc123...`

### Soft Delete with Recovery

When users delete their account:

- `isDeleted` flag set to `true`
- `deletedAt` timestamp recorded
- Account locked for 100 years (effectively disabled)
- User can restore by logging in within 30 days
- After 30 days, data is anonymized

### File Upload (R2 Storage)

- Max file size: 5MB
- Allowed types: JPEG, PNG, GIF
- Files stored with UUID names
- Uses AWS CRT async client (bypasses Java SSL issues)

### Email Templates

HTML emails for:

- Email verification OTP
- Password reset OTP
- Welcome email (after verification)
- Password reset confirmation

### Category Hierarchy

Two-level structure:

- Root categories (`parent = null`)
- Subcategories (reference parent)
- Self-referencing JPA relationship

## Exception Handling

Global exception handler returns consistent responses:

```json
{
  "success": false,
  "message": "Email already exists",
  "error": "DUPLICATE_RESOURCE",
  "status": 409,
  "path": "/api/v1/auth/signup",
  "timestamp": "2025-01-15T10:30:00"
}
```

Custom exceptions:

- `ResourceNotFoundException` (404)
- `DuplicateResourceException` (409)
- `UnauthorizedException` (401)
- `BadRequestException` (400)
- `ValidationException` (400)
- `RateLimitExceededException` (429)
- `AccountLockedException` (401)

## Configuration

Environment variables needed:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/zentro
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=yourpassword

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_ACCESS_EXPIRATION=3600000      # 1 hour
JWT_REFRESH_EXPIRATION=604800000   # 7 days

# Email (Resend)
RESEND_API_KEY=re_xxxxx
RESEND_FROM_EMAIL=noreply@yourdomain.com
RESEND_FROM_NAME=Zentro

# Storage (Cloudflare R2)
R2_ACCOUNT_ID=xxxxx
R2_ACCESS_KEY_ID=xxxxx
R2_SECRET_ACCESS_KEY=xxxxx
R2_BUCKET_NAME=zentro
R2_PUBLIC_URL=https://pub-xxx.r2.dev
R2_ENDPOINT=https://xxx.r2.cloudflarestorage.com

# Admin
ADMIN_SECRET_KEY=your-admin-secret
```

## Running Locally

```bash
cd backend
./mvnw spring-boot:run
```

Server starts at `http://localhost:8080`

## Database

Using PostgreSQL with:

- HikariCP connection pool (5-10 connections)
- JPA auditing for `createdAt`, `updatedAt`
- Hibernate auto DDL update mode
- Indexed columns for performance (email, username, public_id)

## What's Next

The codebase has TODOs for:

- Product entity and catalog
- Shopping cart
- Wishlist
- Orders and checkout
- Product reviews
- Razorpay payment processing
