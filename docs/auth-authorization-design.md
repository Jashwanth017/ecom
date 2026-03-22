# Step 3: Authentication and Authorization Design

## What this step is

This step defines how identity, login, JWT-based authentication, and role-based authorization will work in the marketplace MVP.

At this stage we are not writing Spring Security code yet. We are deciding the rules that the security code must implement later.

This step exists to answer these questions clearly:

- how a user logs in when the same email can exist for buyer and seller
- what account states are allowed to authenticate
- what account states are allowed to perform protected actions
- how buyer, seller, and admin permissions differ
- how the backend will enforce ownership and moderation rules

## Why this comes after schema design

Authentication depends on the identity schema already being settled.

For example:

- login query depends on `(email, role)` being unique in `users`
- seller approval checks depend on `seller_profiles.approval_status`
- ownership checks depend on seller and buyer profile relationships

If Step 2 is not stable, security design becomes guesswork.

## Why this comes before backend module implementation

Every protected endpoint depends on auth and authorization rules.

Without this step:

- controllers may expose the wrong routes publicly
- service methods may miss role checks
- seller approval may be checked in inconsistent places
- admin-only actions may not be clearly separated

Security rules must be decided before the endpoint layer is implemented.

## 1. Authentication model

The MVP should use stateless authentication with JWT.

Recommended flow:

1. user sends login request with `email`, `password`, and `role`
2. backend looks up the account in `users` using `(email, role)`
3. backend verifies password hash
4. backend verifies account is allowed to authenticate
5. backend issues JWT access token
6. frontend stores the token and sends it on protected API requests

Why JWT is appropriate here:

- simple for an MVP
- integrates well with Spring Security
- works cleanly with React frontend and REST APIs
- avoids server-side session storage complexity for now

## 2. Login identifier design

Login must use:

- `email`
- `password`
- `role`

Why this is required:

- the same email can exist once as `BUYER` and once as `SELLER`
- email alone is not enough to identify a unique account

This is better than trying to infer the role automatically because:

- automatic inference becomes ambiguous
- login UX becomes inconsistent
- backend error handling becomes messy

In the UI, role can be represented as:

- separate buyer login and seller login screens
- one login form with an account-type selector

For this project, separate login routes are usually cleaner:

- `/buyer/login`
- `/seller/login`
- `/admin/login`

The backend can still use one login endpoint if desired.

## 3. Recommended JWT contents

The JWT should contain only the claims needed for authorization decisions.

Recommended claims:

- `sub`: user id
- `email`
- `role`
- `status`

Optional claims:

- `sellerApprovalStatus` for seller accounts

Why keep JWT small:

- smaller token size
- less duplication
- less risk of stale business state inside the token

Important rule:

- the backend should not trust JWT claims alone for sensitive business decisions if current database state matters

Example:

- if a seller is banned after login, old tokens should not continue to authorize actions indefinitely

For that reason, protected requests should load the current user state from the database during authentication or authorization checks.

## 4. Recommended token strategy for MVP

For MVP, keep token strategy simple:

- use one access token
- short expiration, such as 15 to 60 minutes
- no refresh token in the very first version unless needed immediately

Why this is a good MVP choice:

- simpler implementation
- less moving parts
- good enough for a portfolio marketplace

Refresh tokens can be added later if needed.

## 5. Authentication success and failure rules

### Authentication succeeds only if:

- `(email, role)` account exists
- password is correct
- `users.status = ACTIVE`

### Authentication must fail if:

- account does not exist
- role is wrong for that email
- password is incorrect
- user is banned

### Seller-specific login behavior

Recommended policy:

- sellers with `approval_status = PENDING` may log in
- sellers with `approval_status = REJECTED` may log in
- unapproved sellers may access seller dashboard/profile pages
- unapproved sellers may not create active sellable products or participate in marketplace selling flow

Why this is better than blocking seller login entirely:

- seller can see onboarding status
- seller can read rejection reason
- seller can update store information later if you allow it

This keeps onboarding and moderation more transparent.

## 6. Authorization model

Authorization in this MVP has three layers:

### Layer 1. Role-based access

Examples:

- only `BUYER` can access cart and buyer order history routes
- only `SELLER` can access seller product management routes
- only `ADMIN` can access moderation routes

### Layer 2. Ownership-based access

Examples:

- a seller can edit only products they own
- a buyer can view only their own orders
- a buyer can modify only their own cart

### Layer 3. Status-based access

Examples:

- banned users cannot access protected business routes
- unapproved sellers cannot activate sellable inventory
- blocked products cannot appear in buyer catalog

A real marketplace needs all three layers, not just role checks.

## 7. Route protection design

### Public routes

- buyer registration
- seller registration
- buyer login
- seller login
- admin login
- product listing
- product detail
- category listing if exposed

### Buyer-only routes

- get current buyer profile
- update current buyer profile
- get own cart
- add item to cart
- update cart item quantity
- remove cart item
- place order
- get own orders

### Seller-only routes

- get seller profile
- update seller profile
- create product
- update own product
- disable own product
- update stock
- list own products
- view own order items

### Admin-only routes

- list all users
- ban/unban user
- approve/reject seller
- list all products
- block/unblock product
- platform summary endpoints

## 8. Authorization rules by actor

### Buyer rules

- can browse public catalog without login
- must be authenticated as `BUYER` to use cart or orders
- cannot access seller or admin routes
- cannot view or modify other buyers' records

### Seller rules

- must be authenticated as `SELLER` to access seller routes
- cannot manage products owned by other sellers
- can log in even if pending approval, based on recommended policy
- cannot sell through active marketplace flow until approved
- cannot access admin routes

### Admin rules

- must be authenticated as `ADMIN`
- admin account is seeded manually
- no public admin registration
- can moderate users and products across the system

## 9. Approval and ban enforcement design

These rules must be enforced in backend service logic, not only in controllers.

### Ban enforcement

If `users.status = BANNED`:

- login must fail
- existing access must be rejected on protected requests

Why:

- a ban should take effect consistently

### Seller approval enforcement

If seller approval status is `PENDING` or `REJECTED`:

- seller may access account-facing seller pages, depending on product policy
- seller must not publish or maintain buyer-visible active products
- seller must not enter the live selling flow

A simple MVP rule is:

- seller may create or edit product records
- product cannot be `ACTIVE` unless seller is `APPROVED`

This is better than hiding everything because it allows a seller workspace while still protecting the marketplace.

### Product block enforcement

If product status is `BLOCKED_BY_ADMIN`:

- buyer catalog must not return it
- cart/order placement using that product must fail
- seller can still view it in seller dashboard with blocked status

## 10. Recommended security checks by module

### Auth module

- validate `(email, role)` login path
- reject banned users
- issue JWT with role-aware claims

### Buyer module

- require `BUYER`
- verify authenticated buyer owns the target profile/cart/order

### Seller module

- require `SELLER`
- verify authenticated seller owns target product/order item scope
- verify seller approval before enabling marketplace-selling actions

### Catalog module

- public list/detail endpoints must filter hidden products
- protected seller/admin views may expose more statuses

### Admin module

- require `ADMIN`
- reject all non-admin access

## 11. Error design

Security-related responses should be predictable.

Recommended patterns:

- `401 Unauthorized` for invalid or missing authentication
- `403 Forbidden` for authenticated user without permission
- `404 Not Found` for resources hidden due to ownership boundaries when appropriate
- `409 Conflict` for invalid state transitions such as trying to activate product before seller approval

Why this matters:

- predictable APIs make frontend handling cleaner
- status codes should reflect business meaning, not just technical failure

## 12. Spring Security implementation plan later

When coding begins, this design should map into:

- custom login request DTO with `email`, `password`, `role`
- `UserDetailsService` or equivalent user-loading strategy using `(email, role)`
- JWT utility/service
- security filter for token parsing
- route authorization configuration
- service-layer guards for seller approval and ownership

This is the implementation direction, but not the code yet.

## 13. What this step avoids

This design avoids:

- ambiguous login for duplicate emails across roles
- shallow role-only authorization
- security checks scattered randomly across controllers
- seller approval being treated as the same thing as banning
- admin registration accidentally being exposed

## 14. What this step enables next

After this step, the next implementation stage becomes clear:

- define backend modules and API contracts
- map routes to roles
- define request/response DTOs
- implement Spring Security against a stable auth model
