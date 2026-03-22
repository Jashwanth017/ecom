# E-Commerce Marketplace MVP Blueprint

## 1. Purpose

This document defines the first production-style foundation for the marketplace MVP.
It exists to lock the business rules before backend and frontend implementation begins.

The MVP goal is to support a realistic end-to-end marketplace flow:

- buyers can discover products and place orders
- sellers can register, get approved, manage products, and fulfill marketplace supply
- one super admin can moderate users and products

This is intentionally not an Amazon-scale design. It is a scoped, portfolio-worthy marketplace MVP designed to scale cleanly later.

## 2. MVP Scope

### In scope

#### Buyer

- register as buyer
- login as buyer
- browse all visible products
- search products
- filter products by category
- sort products
- view product details
- add item to cart
- update cart item quantity
- remove item from cart
- place order
- view own orders
- manage basic profile

#### Seller

- register as seller
- login as seller
- manage store profile
- add product
- edit product
- disable own product
- view own products
- update stock
- view orders related to own products

#### Admin

- seeded super admin account only
- no public admin registration
- admin login
- admin dashboard access
- view all users
- ban/unban buyers and sellers
- approve/reject seller accounts
- view all products
- block/unblock products
- monitor basic platform activity

### Out of scope for MVP

- online payment gateway integration
- reviews and ratings
- wishlist
- coupons and discounts
- returns and refunds
- shipment tracking
- seller-buyer chat
- notifications by email or SMS
- multiple seller staff accounts
- advanced analytics
- tax and invoicing complexity

## 3. Product Principles

- one identity system for all account types
- role-aware authentication
- moderation is a first-class concern
- important records are disabled by status, not hard-deleted
- seller-specific and buyer-specific data are separated from core auth data
- backend is the source of truth for access control and business rules

## 4. Roles

The system supports exactly three roles in the MVP:

- `BUYER`
- `SELLER`
- `ADMIN`

### Role intent

- `BUYER` purchases products and manages a personal shopping account
- `SELLER` manages a store and products and views order items related to owned products
- `ADMIN` moderates platform activity and has no public registration flow

## 5. Identity Model

The authentication model uses one shared `users` table.

Each account is role-specific. The same email address may appear once for `BUYER` and once for `SELLER`, but not more than once for the same role.

### Identity rules

- uniqueness rule is `(email, role)`
- `email` alone is not globally unique
- login must include `email + role + password`
- successful login returns the authenticated role so the frontend can redirect correctly
- admin account is created manually or seeded, never publicly registered

## 6. Status Design

Statuses must be explicit because moderation and access control are part of the MVP.

### 6.1 User account status

Recommended enum: `UserStatus`

- `ACTIVE`
- `BANNED`
- `REJECTED`

Notes:

- `ACTIVE` means account can authenticate and use allowed features
- `BANNED` means login and protected actions are blocked
- `REJECTED` is mainly useful for seller-side rejection state if stored at the account level

### 6.2 Seller approval status

Recommended enum: `SellerApprovalStatus`

- `PENDING`
- `APPROVED`
- `REJECTED`

Notes:

- keep seller approval separate from generic user account status
- this avoids mixing moderation logic with onboarding workflow
- seller can exist as a valid account while still being unapproved for marketplace selling

### 6.3 Product status

Recommended enum: `ProductStatus`

- `ACTIVE`
- `OUT_OF_STOCK`
- `DISABLED_BY_SELLER`
- `BLOCKED_BY_ADMIN`

Visibility rule:

- buyer-facing catalog only shows products allowed for sale
- blocked products remain in seller/admin views for audit and moderation clarity

### 6.4 Order status

Recommended enum: `OrderStatus`

- `PLACED`
- `CANCELLED`

This is enough for MVP. Shipping lifecycle can be added later.

## 7. Core Entities

The MVP requires these main entities.

### 7.1 User

Purpose: central authentication and authorization record.

Core fields:

- `id`
- `email`
- `password_hash`
- `role`
- `status`
- `created_at`
- `updated_at`

Rules:

- one row per account role
- `(email, role)` must be unique
- admin is stored here like other accounts but created only by internal setup

### 7.2 BuyerProfile

Purpose: buyer-specific personal data.

Core fields:

- `id`
- `user_id`
- `full_name`
- `phone`
- `address_line_1` optional for MVP
- `address_line_2` optional for MVP
- `city` optional for MVP
- `state` optional for MVP
- `postal_code` optional for MVP

Rules:

- one-to-one with `users` where role is `BUYER`

### 7.3 SellerProfile

Purpose: seller/store-specific data.

Core fields:

- `id`
- `user_id`
- `store_name`
- `store_description`
- `approval_status`
- `approved_at`
- `rejection_reason` optional

Rules:

- one-to-one with `users` where role is `SELLER`
- seller may log in while approval is pending, but selling permissions depend on business policy

### 7.4 Category

Purpose: product grouping for browse and filter features.

Core fields:

- `id`
- `name`
- `slug`

Rules:

- category names/slugs should be unique
- categories are manageable by admin later if needed, but static seed data is acceptable in MVP

### 7.5 Product

Purpose: seller-owned catalog item.

Core fields:

- `id`
- `seller_id`
- `category_id`
- `name`
- `description`
- `price`
- `stock_quantity`
- `status`
- `image_url` optional for MVP
- `created_at`
- `updated_at`

Rules:

- every product belongs to one seller
- seller can edit only own products
- admin can block/unblock any product
- blocked products cannot appear in buyer catalog

### 7.6 Cart

Purpose: one active shopping cart per buyer.

Core fields:

- `id`
- `buyer_id`
- `created_at`
- `updated_at`

Rules:

- one active cart per buyer

### 7.7 CartItem

Purpose: items inside a buyer cart.

Core fields:

- `id`
- `cart_id`
- `product_id`
- `quantity`

Rules:

- one cart item per `(cart_id, product_id)`
- quantity must be positive
- backend must validate stock and product visibility before order placement

### 7.8 Order

Purpose: immutable purchase transaction header for a buyer.

Core fields:

- `id`
- `buyer_id`
- `status`
- `total_amount`
- `placed_at`

Rules:

- order is created from cart snapshot at placement time
- totals should be stored on the order, not recalculated from current product price later

### 7.9 OrderItem

Purpose: line items within an order.

Core fields:

- `id`
- `order_id`
- `product_id`
- `seller_id`
- `product_name_snapshot`
- `product_price_snapshot`
- `quantity`
- `line_total`

Rules:

- seller visibility should be based on `seller_id`
- historical data must not break if product details change later

## 8. Core Business Rules

These rules are the source of truth for the MVP.

### Identity and registration

- a person may create one buyer account and one seller account using the same email
- the same email cannot register twice for the same role
- buyer registration creates `User(BUYER)` and `BuyerProfile`
- seller registration creates `User(SELLER)` and `SellerProfile`
- admin has no public registration route

### Login and session

- login request must include role or account type
- login succeeds only if password matches and account status allows access
- banned accounts must not receive valid authenticated access
- frontend redirects after login based on returned role

### Seller approval

- newly registered sellers start in `PENDING`
- seller cannot fully operate marketplace-selling features until approved
- admin can mark seller as `APPROVED` or `REJECTED`

### Product ownership and visibility

- only approved active sellers may offer active products for buyer purchase
- seller can manage only own products
- admin can block any product
- blocked products are hidden from buyers but remain visible to seller/admin with moderation status

### Cart and order rules

- buyers can manage only their own cart
- buyers can place orders only for currently purchasable products
- stock must be checked during order placement
- order items store product snapshots to preserve history
- sellers can view only order items related to their own products

### Moderation rules

- admin can ban/unban buyer and seller accounts
- banning does not hard-delete historical data
- admin can block/unblock products
- moderation actions should preserve auditability

## 9. Backend Module Boundaries

These are the backend modules that should exist in the MVP.

### Auth module

Responsibilities:

- registration
- login
- JWT issuance
- password hashing
- current-user resolution

### User/Profile module

Responsibilities:

- buyer profile retrieval and update
- seller store profile retrieval and update
- user status checks

### Catalog module

Responsibilities:

- product listing
- search
- category filtering
- sorting
- product detail

### Seller Product module

Responsibilities:

- add product
- edit product
- disable own product
- update stock
- list own products

### Cart module

Responsibilities:

- create or fetch active cart
- add item
- update quantity
- remove item
- return cart summary

### Order module

Responsibilities:

- place order
- buyer order history
- seller order-item visibility

### Admin module

Responsibilities:

- list users
- approve/reject sellers
- ban/unban users
- list products
- block/unblock products
- basic platform overview counts

## 10. Frontend Application Areas

The React app should be split by responsibility, not by random pages.

### Public area

- landing/home
- product listing
- product detail
- buyer login/register
- seller login/register

### Buyer area

- buyer profile
- cart
- order history

### Seller area

- seller dashboard
- seller profile
- product management
- stock management
- seller order view

### Admin area

- admin login
- admin dashboard
- user moderation
- seller approvals
- product moderation

## 11. Access Control Summary

Minimum access-control rules:

- only `BUYER` can manage buyer cart and buyer order history
- only `SELLER` can manage own seller profile and own products
- only approved sellers can make products sellable
- only `ADMIN` can moderate users and products
- backend must verify ownership on every seller product action
- backend must verify role on every protected endpoint

## 12. Non-Functional MVP Expectations

Even at MVP level, the implementation should aim for:

- clear package/module boundaries
- DTO-based request/response design
- server-side validation
- status-driven moderation instead of deletion
- audit-friendly data retention
- simple but scalable schema decisions

## 13. What This Blueprint Enables Next

After this document is accepted, the next implementation step becomes precise:

- convert roles and statuses into enums
- design relational schema and constraints
- define entity relationships
- define authentication contract
- define API surface for buyer, seller, and admin modules

Without this document, later steps are guesswork.
