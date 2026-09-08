# Repository Layer Documentation

This document covers the repository declarations, query function descriptions, and implementation examples within the service layer for the `identity` module.

---

##  Repository Layer Overview

The Repository Layer in the `identity` module handles I/O operations and database communications with PostgreSQL using **Spring Data JPA**. All repositories extend `JpaRepository` and are fully integrated with *Soft Delete* mechanisms and *N+1 Query* optimizations.

---

##  Repositories & Query Details

### 1. `UsersRepository`
Manages user profile entity data (`Users`).

####  Query Methods & Descriptions
| Query Method | Description / Function |
| :--- | :--- |
| `findByIdWithCredentials(UUID id)` | Retrieves a `Users` entity along with its associated `Credentials` list using `LEFT JOIN FETCH` in a single database query. |
| `findByEmail(String email)` | Finds a `Users` profile using the email address registered in the associated `Credentials` table. |
| `existsByIdAndStatusTrue(UUID id)` | Checks if a user with the specified ID exists and has an active status (`status = true`). |

#### 💡 Service Implementation Examples
```java
// Example: Retrieving a user profile along with credentials
Users user = usersRepository.findByIdWithCredentials(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

// Example: Checking active user status
boolean isActive = usersRepository.existsByIdAndStatusTrue(userId);
```

---

### 2. `CredentialsRepository`
Manages authentication credentials, passwords, user roles, and device sessions (`Credentials`).

#### Query Methods & Descriptions
| Query Method | Description / Function |
| :--- | :--- |
| `findByEmail(String email)` | Retrieves credentials by email (used during standard authentication/login flows). |
| `findByEmailWithUsers(String email)` | Retrieves credentials while eagerly fetching the parent `Users` entity in a single query via `JOIN FETCH`. |
| `existsByEmail(String email)` | Checks if an email is already registered (used for registration validation). |
| `findByAccessToken(String token)` | Finds a credential record matching the active access token. |
| `findByUsersId(UUID userId)` | Retrieves credentials belonging to a specific user by User ID. |

#### 💡 Service Implementation Examples
```java
// Example: Validating email during registration
if (credentialsRepository.existsByEmail(request.getEmail())) {
    throw new BadRequestException("Email is already registered!");
}

// Example: User authentication
Credentials credentials = credentialsRepository.findByEmailWithUsers(request.getEmail())
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
```

---

### 3. `SessionsRepository`
Manages the JWT *Refresh Token* lifecycle and token rotation processes (`Sessions`).

#### Query Methods & Descriptions
| Query Method | Description / Function |
| :--- | :--- |
| `findByRefreshToken(String token)` | Retrieves a token record by refresh token string (used at the `/refresh-token` endpoint). |
| `findByCredentialId(UUID id)` | Retrieves the active refresh token bound to a specific credential. |
| `findByRefreshTokenWithDetails(String token)` | Fetches a refresh token while performing a `JOIN FETCH` on `Credentials` and `Users` for full authorization context. |
| `deleteByCredentialId(UUID id)` | Removes/invalidates refresh token records by Credential ID (used during logout). |

#### 💡 Service Implementation Examples
```java
// Example: Refresh Token flow
RotateToken tokenRecord = rotateTokenRepository.findByRefreshTokenWithDetails(oldRefreshToken)
        .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid or expired"));

// Example: Logout flow (invalidating session)
rotateTokenRepository.deleteByCredentialId(credentialsId);
```

---

### 4. `OtpRepository`
Manages temporary OTP hash codes, expiration timestamps, and verification attempt limits (`Otp`).

> ⚠️ **Note:** This repository **does not use Soft Delete** because the data is ephemeral.

#### 🛠️ Query Methods & Descriptions
| Query Method | Description / Function |
| :--- | :--- |
| `findLatestActiveOtp(UUID userId, String purpose, LocalDateTime now)` | Retrieves the latest active OTP for a user that hasn't been used (`isUsed = false`) and hasn't expired (`expiresAt > now`). |
| `deleteByUserIdAndPurpose(UUID userId, String purpose)` | Deletes old OTP records for a user under a specific purpose (e.g., `REGISTER`) before issuing a new one. |
| `deleteExpiredOrUsedOtps(LocalDateTime now)` | Deletes all expired or consumed OTP records (used for periodic cleanup/cron jobs). |

#### 💡 Service Implementation Examples
```java
// Example 1: Creating a new OTP (clearing previous codes first)
otpRepository.deleteByUserIdAndPurpose(user.getId(), "REGISTER");

Otp newOtp = new Otp();
newOtp.setUser(user);
newOtp.setOtpHash(passwordEncoder.encode(rawOtp));
newOtp.setPurpose("REGISTER");
otpRepository.save(newOtp);

// Example 2: Verifying an OTP
Otp activeOtp = otpRepository.findLatestActiveOtp(user.getId(), "REGISTER", LocalDateTime.now())
        .orElseThrow(() -> new BadRequestException("OTP not found or has expired"));

if (passwordEncoder.matches(inputOtp, activeOtp.getOtpHash())) {
    activeOtp.setIsUsed(true);
    otpRepository.save(activeOtp);
}
```

---

## Automated Features & Important Notes

1. **Automatic Soft Delete Filtering**
   The `Users`, `Credentials`, and `Sessions` entities utilize the `@SQLRestriction("deleted_at IS NULL")` annotation. Standard JPA methods such as `findAll()`, `findById()`, or derived query methods **automatically exclude soft-deleted records** from execution results.

2. **Preventing N+1 Queries**
   Always prefer custom queries using **`JOIN FETCH`** (e.g., `findByIdWithCredentials` or `findByRefreshTokenWithDetails`) whenever the service layer requires reading related ch