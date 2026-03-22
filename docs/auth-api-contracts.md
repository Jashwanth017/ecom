# Auth API Contracts for MVP

## Purpose

This document defines the minimal auth-related API contracts for the marketplace MVP.

These are design contracts, not final code.

## 1. Buyer registration

`POST /api/v1/auth/register/buyer`

Request:

```json
{
  "email": "buyer@example.com",
  "password": "StrongPassword123!",
  "fullName": "Buyer Name",
  "phone": "9876543210"
}
```

Behavior:

- creates `users` row with role `BUYER`
- creates `buyer_profiles` row
- rejects if `(email, BUYER)` already exists

## 2. Seller registration

`POST /api/v1/auth/register/seller`

Request:

```json
{
  "email": "seller@example.com",
  "password": "StrongPassword123!",
  "storeName": "Acme Store",
  "storeDescription": "General marketplace seller"
}
```

Behavior:

- creates `users` row with role `SELLER`
- creates `seller_profiles` row with `approval_status = PENDING`
- rejects if `(email, SELLER)` already exists

## 3. Login

`POST /api/v1/auth/login`

Request:

```json
{
  "email": "seller@example.com",
  "password": "StrongPassword123!",
  "role": "SELLER"
}
```

Response:

```json
{
  "accessToken": "jwt-token-here",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 12,
    "email": "seller@example.com",
    "role": "SELLER",
    "status": "ACTIVE"
  },
  "seller": {
    "approvalStatus": "PENDING"
  },
  "redirectTo": "/seller/dashboard"
}
```

Notes:

- `seller` block can be omitted for non-seller accounts
- `redirectTo` is optional, but useful for frontend simplicity

## 4. Current user profile

`GET /api/v1/auth/me`

Behavior:

- returns currently authenticated user identity info
- can include profile summary useful for frontend session bootstrap

Example response:

```json
{
  "id": 12,
  "email": "seller@example.com",
  "role": "SELLER",
  "status": "ACTIVE",
  "sellerApprovalStatus": "PENDING"
}
```

## 5. Admin seed account

Admin should not have a public registration endpoint.

Admin must be created by:

- startup seed logic
- SQL seed file
- manual database insert for local development

## 6. Authentication error examples

Invalid credentials:

```json
{
  "message": "Invalid email, role, or password"
}
```

Banned account:

```json
{
  "message": "This account is banned"
}
```

Seller not approved for an action:

```json
{
  "message": "Seller account is not approved for this action"
}
```
