# Backend API Map for MVP

## Purpose

This document provides the first full API map for the marketplace MVP.

These are design endpoints, not final implementation details.

## Auth APIs

### Public

`POST /api/v1/auth/register/buyer`

- register buyer account

`POST /api/v1/auth/register/seller`

- register seller account

`POST /api/v1/auth/login`

- login by `email + password + role`

### Protected

`GET /api/v1/auth/me`

- return current authenticated user summary

## Buyer APIs

### Buyer profile

`GET /api/v1/buyer/profile`

- return current buyer profile

`PUT /api/v1/buyer/profile`

- update current buyer profile

### Cart

`GET /api/v1/buyer/cart`

- return current cart with items and totals

`POST /api/v1/buyer/cart/items`

- add item to cart

`PUT /api/v1/buyer/cart/items/{cartItemId}`

- update cart item quantity

`DELETE /api/v1/buyer/cart/items/{cartItemId}`

- remove cart item

### Orders

`POST /api/v1/buyer/orders`

- place order from current cart

`GET /api/v1/buyer/orders`

- list buyer's own orders

`GET /api/v1/buyer/orders/{orderId}`

- return buyer's own order detail

## Seller APIs

### Seller profile

`GET /api/v1/seller/profile`

- return current seller/store profile

`PUT /api/v1/seller/profile`

- update current seller/store profile

### Seller products

`POST /api/v1/seller/products`

- create product

`GET /api/v1/seller/products`

- list current seller's products

`GET /api/v1/seller/products/{productId}`

- return one owned product

`PUT /api/v1/seller/products/{productId}`

- update owned product

`PATCH /api/v1/seller/products/{productId}/disable`

- disable owned product

`PATCH /api/v1/seller/products/{productId}/stock`

- update stock

### Seller order visibility

`GET /api/v1/seller/orders/items`

- list order items related to current seller

`GET /api/v1/seller/orders/items/{orderItemId}`

- return one seller-visible order item

## Public catalog APIs

### Public product browsing

`GET /api/v1/catalog/products`

- list buyer-visible products
- supports search, category filter, and sort

Suggested query params:

- `search`
- `category`
- `sort`
- `page`
- `size`

`GET /api/v1/catalog/products/{productId}`

- return one buyer-visible product detail

### Categories

`GET /api/v1/catalog/categories`

- list categories for filter UI

## Admin APIs

### Admin dashboard

`GET /api/v1/admin/dashboard/summary`

- return basic counts for users, sellers, products, and orders

### User moderation

`GET /api/v1/admin/users`

- list all users

Suggested query params:

- `role`
- `status`
- `page`
- `size`

`GET /api/v1/admin/users/{userId}`

- return one user summary

`PATCH /api/v1/admin/users/{userId}/ban`

- ban user

`PATCH /api/v1/admin/users/{userId}/unban`

- unban user

### Seller approval

`GET /api/v1/admin/sellers/pending`

- list pending seller accounts

`PATCH /api/v1/admin/sellers/{sellerProfileId}/approve`

- approve seller

`PATCH /api/v1/admin/sellers/{sellerProfileId}/reject`

- reject seller

### Product moderation

`GET /api/v1/admin/products`

- list all products across platform

Suggested query params:

- `status`
- `sellerId`
- `page`
- `size`

`GET /api/v1/admin/products/{productId}`

- return one product for admin view

`PATCH /api/v1/admin/products/{productId}/block`

- block product

`PATCH /api/v1/admin/products/{productId}/unblock`

- unblock product

## DTO direction

Recommended request DTO examples:

- `BuyerRegistrationRequest`
- `SellerRegistrationRequest`
- `LoginRequest`
- `UpdateBuyerProfileRequest`
- `UpdateSellerProfileRequest`
- `CreateProductRequest`
- `UpdateProductRequest`
- `UpdateStockRequest`
- `AddCartItemRequest`
- `UpdateCartItemRequest`
- `PlaceOrderRequest`
- `RejectSellerRequest`

Recommended response DTO examples:

- `AuthResponse`
- `CurrentUserResponse`
- `BuyerProfileResponse`
- `SellerProfileResponse`
- `ProductListItemResponse`
- `ProductDetailResponse`
- `SellerProductResponse`
- `CartResponse`
- `OrderSummaryResponse`
- `OrderDetailResponse`
- `AdminUserResponse`
- `AdminDashboardSummaryResponse`

## Query behavior notes

### Catalog listing

The public catalog endpoint should support:

- text search on product name
- category filtering
- sorting by price or newest
- pagination

### Seller product list

The seller product list should support:

- filter by status
- pagination

### Admin list endpoints

Admin list endpoints should support:

- pagination by default
- filters for operational use

## Endpoint design principles used here

- public and protected APIs are separated clearly
- buyer, seller, and admin routes are separated by responsibility
- seller product management is isolated from public catalog browsing
- moderation routes are explicit instead of hidden inside generic update APIs
