# Step 4: Backend Module and API Design

## What this step is

This step turns the product rules, schema design, and security design into a backend structure that can actually be implemented in Spring Boot.

The goal of this step is to define:

- which backend modules exist
- what each module is responsible for
- how controllers, services, repositories, and DTOs should be separated
- which APIs belong to which module
- where validation and authorization logic should live

This is the last major design step before starting actual Spring Boot project setup.

## Why this comes before coding controllers and services

If backend boundaries are not designed first, teams often end up with:

- bloated controllers
- service methods that know too much about unrelated modules
- duplicated validation logic
- weak ownership checks
- endpoints organized by screens instead of business responsibilities

This step prevents that by making responsibilities explicit before implementation starts.

## Why this approach is better than building page-by-page APIs

A common beginner mistake is to build APIs based on frontend screens:

- "make dashboard API"
- "make seller page API"
- "make product page API"

That usually produces poor boundaries because the backend should be organized by domain capability, not by UI screen.

This module-first approach is better because:

- services stay cohesive
- APIs remain reusable
- business rules have a clear home
- later frontend changes do not force backend redesign

## 1. Recommended Spring Boot package structure

Recommended base package example:

`com.marketplace`

Recommended top-level package layout:

- `config`
- `security`
- `common`
- `auth`
- `user`
- `catalog`
- `cart`
- `order`
- `admin`

### Purpose of shared packages

#### `config`

Holds application configuration classes.

Examples:

- CORS config
- Jackson config
- OpenAPI config later if added

#### `security`

Holds security-layer implementation.

Examples:

- JWT filter
- token service
- custom user principal
- security config

#### `common`

Holds cross-cutting concerns.

Examples:

- global exception handling
- common API response models if used
- enums shared across modules
- pagination helpers

## 2. Module breakdown

### `auth` module

Purpose:

- registration
- login
- current authenticated identity

Main responsibilities:

- register buyer
- register seller
- authenticate by `email + role + password`
- issue JWT
- return current session user summary

Should contain:

- `AuthController`
- `AuthService`
- registration and login DTOs
- password handling logic

Should not contain:

- full buyer profile management
- seller product management
- admin moderation logic

### `user` module

Purpose:

- buyer profile management
- seller profile management
- user-facing account retrieval

Main responsibilities:

- get/update buyer profile
- get/update seller profile
- return seller approval state where useful

Should contain:

- `BuyerProfileController`
- `SellerProfileController`
- services for profile management
- profile DTOs

Should not contain:

- login
- catalog browsing
- admin moderation

### `catalog` module

Purpose:

- public product discovery
- seller product management

This module can be split later, but for MVP it is acceptable to keep both public catalog and seller product management under one domain because both operate on `Product`.

Main responsibilities:

- public product listing
- search, filter, and sort
- product detail
- seller create/update/disable product
- seller stock updates
- seller own product listing

Should contain:

- `CatalogController` for public read APIs
- `SellerProductController` for seller product management
- `ProductService`
- `CategoryService`
- product DTOs

Important rule:

- public product queries and seller product management are different use cases even if they share entities

### `cart` module

Purpose:

- buyer cart lifecycle

Main responsibilities:

- get current cart
- add item
- update item quantity
- remove item

Should contain:

- `CartController`
- `CartService`
- cart DTOs

### `order` module

Purpose:

- order placement and order visibility

Main responsibilities:

- place order from cart
- buyer order history
- seller order-item visibility

Should contain:

- `BuyerOrderController`
- `SellerOrderController`
- `OrderService`
- order DTOs

### `admin` module

Purpose:

- platform moderation
- basic operational visibility

Main responsibilities:

- list users
- approve/reject sellers
- ban/unban users
- list products
- block/unblock products
- basic dashboard summary

Should contain:

- `AdminUserController`
- `AdminProductController`
- `AdminDashboardController`
- admin services and DTOs

## 3. Layer responsibilities

### Controllers

Controllers should do only these things:

- receive HTTP requests
- validate request bodies and params
- call service methods
- return DTO responses

Controllers should not:

- contain business rules
- write repository queries directly
- decide ownership rules inline

Why:

- controllers should stay thin
- business logic belongs in services

### Services

Services are the main home of business rules.

Services should handle:

- business validation
- state transitions
- ownership checks
- approval checks
- ban and moderation rule enforcement
- orchestration across repositories

Examples:

- cart service validates stock before updating quantity
- order service converts cart into order and order items
- seller product service checks ownership and approval state

### Repositories

Repositories should handle:

- database access
- query methods
- fetch strategies needed by services

Repositories should not:

- enforce business policy
- build controller responses directly

### DTOs

DTOs should be used for:

- request payloads
- response payloads

Why DTOs are important:

- avoid exposing JPA entities directly
- keep API shape independent of internal persistence shape
- make validation and versioning easier later

## 4. Validation strategy

Validation should happen in two layers.

### Layer 1. Request validation

Use Bean Validation for:

- required fields
- email format
- password length
- positive quantity
- non-blank product name

This belongs in DTOs.

### Layer 2. Business validation

Use services for:

- duplicate account checks
- seller approval rules
- product ownership rules
- stock availability checks
- account status rules

Why two layers:

- syntax validation and business validation are not the same thing

## 5. Authorization placement

Authorization should also happen in layers.

### Security configuration level

Handle:

- public vs protected routes
- role-based route restrictions

### Service level

Handle:

- ownership checks
- seller approval checks
- product moderation rules
- order visibility checks

Why:

- route-level security alone is not enough for marketplace rules

## 6. Recommended endpoint grouping

The backend should group APIs by business responsibility.

Recommended root:

`/api/v1`

Main groups:

- `/api/v1/auth`
- `/api/v1/buyer`
- `/api/v1/seller`
- `/api/v1/catalog`
- `/api/v1/admin`

This structure is better than random endpoint naming because it keeps role boundaries obvious.

## 7. Cross-module interaction rules

Modules will interact, but responsibilities should remain clear.

Examples:

- `AuthService` can create `User` and profile records
- `OrderService` can read cart and product data
- `AdminService` can update user and product states
- `CatalogService` should not know how JWT tokens work

This is important because cross-module dependency is normal, but leakage of responsibility is not.

## 8. Recommended service responsibilities by use case

### Registration use case

Primary module: `auth`

Responsibilities:

- validate uniqueness by `(email, role)`
- hash password
- create `users`
- create matching profile row

### Login use case

Primary module: `auth`

Responsibilities:

- lookup account by `(email, role)`
- verify password
- check account status
- issue JWT

### Browse catalog use case

Primary module: `catalog`

Responsibilities:

- return only buyer-visible products
- apply search, category filter, and sort

### Seller manage product use case

Primary module: `catalog`

Responsibilities:

- verify seller role
- verify seller ownership
- verify seller approval when activating product

### Cart use case

Primary module: `cart`

Responsibilities:

- ensure buyer owns cart
- validate product purchasability
- validate quantity rules

### Place order use case

Primary module: `order`

Responsibilities:

- load cart
- validate each line item
- snapshot product data
- create order and order items
- update stock

### Admin moderation use case

Primary module: `admin`

Responsibilities:

- list and moderate users
- list and moderate products
- change seller approval state

## 9. Error handling design

Use one global exception-handling strategy.

Recommended error categories:

- validation errors
- authentication errors
- authorization errors
- not found errors
- state conflict errors

Why:

- consistent API behavior
- easier frontend integration
- cleaner controller code

## 10. API response design direction

For MVP, either of these is acceptable:

- plain JSON objects per endpoint
- a light standard wrapper with fields like `message`, `data`, `timestamp`

Recommendation:

- keep success responses simple
- standardize error responses more strictly than success wrappers

Why:

- over-designed response wrappers add noise early

## 11. What this step avoids

This step avoids:

- controller-heavy architecture
- mixing auth, profile, catalog, cart, and moderation logic together
- leaking entity models directly into APIs
- random endpoint naming
- inconsistent validation and authorization placement

## 12. What this step enables next

After this step, the project is ready for technical setup:

- create Spring Boot project
- add dependencies
- create package structure
- start implementing entities, repositories, and security
