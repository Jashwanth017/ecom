# Step 2: Database Schema Design

## What this step is

This step converts the MVP blueprint into a relational database design for MySQL.

The goal is not just to list tables. The goal is to decide:

- which tables must exist
- how they relate to each other
- which constraints protect business rules
- which fields should be stored now versus later
- which indexes the MVP needs to remain usable as data grows

This is the second step because the database is the structural backbone of the backend.

## Why this comes before Spring Boot code

Once the schema is wrong, everything above it becomes unstable:

- JPA entities become wrong
- service logic becomes awkward
- authentication queries become ambiguous
- admin moderation becomes difficult
- order history becomes unreliable

A clean schema makes later code simpler. A weak schema forces later code to compensate for design mistakes.

## Why this approach is better than "just start with entities"

Many people begin by writing JPA entities first and let the schema emerge from annotations.
That is convenient for demos, but it often creates poor production design because:

- table constraints are under-planned
- indexes are forgotten
- business uniqueness rules are not made explicit
- audit/history needs are ignored
- one-to-one and one-to-many boundaries become muddy

Designing the schema first is better because the database is the source of durable truth.

## Main schema decisions

### 1. One `users` table for shared identity

Why:

- one auth pipeline
- one password policy
- one moderation entry point
- one role field
- one `(email, role)` uniqueness rule

This is better than separate buyer and seller auth tables because login, admin control, and security stay centralized.

### 2. Separate `buyer_profiles` and `seller_profiles`

Why:

- buyer and seller data evolve differently
- seller onboarding/approval is not a buyer concern
- role-specific columns do not pollute the core identity table

This avoids sparse tables full of irrelevant nullable columns.

### 3. `seller_profiles.approval_status` separate from `users.status`

Why:

- account moderation and seller onboarding are different concerns
- a seller can be a valid user account but still not be approved to sell

This avoids forcing unrelated state transitions into a single enum.

### 4. Product rows belong to sellers

Why:

- ownership checks become simple
- seller dashboards can query owned inventory directly
- admin moderation can act on seller-linked supply

### 5. Orders are split into `orders` and `order_items`

Why:

- one buyer order can contain many products
- sellers need visibility only into their own items
- order items must preserve product snapshot data even if products change later

This is the standard real-world pattern. A single flat order table would break quickly.

### 6. Cart is modeled separately from orders

Why:

- cart is mutable
- order is transactional history

Mixing cart and order concepts creates brittle lifecycle logic.

## Table list

The MVP schema contains:

- `users`
- `buyer_profiles`
- `seller_profiles`
- `categories`
- `products`
- `carts`
- `cart_items`
- `orders`
- `order_items`

## Relationship summary

- one `users` row may have one `buyer_profiles` row
- one `users` row may have one `seller_profiles` row
- one seller profile owns many `products`
- one category has many `products`
- one buyer profile has one active `cart`
- one cart has many `cart_items`
- one buyer profile has many `orders`
- one order has many `order_items`
- each order item references the seller and product snapshot involved in purchase

## Constraint strategy

The schema must enforce these rules at database level where possible:

- `(email, role)` unique in `users`
- one profile per user in `buyer_profiles` and `seller_profiles`
- unique category slug
- one cart per buyer
- one cart line per product inside a cart
- stock and quantity cannot go below zero
- monetary values use decimal, not floating point

## Index strategy

The MVP does not need advanced indexing, but it does need the basics:

- `users(email, role)` for login lookup
- `products(category_id, status)` for buyer catalog queries
- `products(seller_id, status)` for seller product list
- `orders(buyer_id, placed_at)` for order history
- `order_items(seller_id, order_id)` for seller order visibility

These avoid obvious performance problems early without over-optimizing.

## Future-safe decisions in this schema

This design keeps expansion paths open for:

- payment transactions
- shipping addresses
- product image tables
- reviews
- coupons
- return workflows
- seller analytics

We are not building those now, but the schema leaves room for them without forcing a rewrite.

## Deliverables created in this step

This step adds:

- a schema explanation document
- an initial MySQL DDL file for the MVP

The DDL is the implementation artifact of this step.
