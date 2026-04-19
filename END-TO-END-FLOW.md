# End-to-End Flow: Complete User Journey

## Overview

This document walks through the complete user journey from registration through to order confirmation. All IDs in this document are real-shaped examples — replace them with actual IDs returned from your running instance.

---

## Prerequisites

- Application running on `http://localhost:8080`
- MongoDB running with the `ecommerce` database
- AWS S3 configured for image uploads
- No seed data required — this flow creates everything from scratch

---

## Complete Flow (Step-by-Step)

### STEP 1: Register User

**Endpoint:** `POST /api/v1/users/register`

**Request Body:**
```json
{
  "name": "User Internet",
  "email": "user@example.com",
  "phone": "0821234567",
  "pwd": "SecurePass123"
}
```

**Expected Response:**
```json
{
  "status": "201 CREATED",
  "statusCode": 201,
  "message": "User registered successfully.",
  "timestamp": "2026-03-14T10:00:00.000Z",
  "data": {
    "id": "6754e01b188ed126b2443093",
    "name": "User Internet",
    "email": "user@example.com",
    "phone": "0821234567",
    "status": "AWAITING_CONFIRMATION"
  }
}
```

**What Happens:**
- User is created with `AWAITING_CONFIRMATION` status
- OTP is generated and sent to the phone number
- Account cannot be used until activated

> Save the `id` — this is your `userId` for all subsequent requests.

---

### STEP 2: Activate Account via OTP

**Endpoint:** `POST /api/v1/otp/validate`

**Request Body:**
```json
{
  "phoneNumber": "0821234567",
  "otp": "483920"
}
```

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "OTP validated successfully.",
  "timestamp": "2026-03-14T10:01:00.000Z",
  "data": {
    "phoneNumber": "0821234567",
    "valid": true
  }
}
```

**What Happens:**
- OTP is validated against the stored record
- On success, user status changes to `ACTIVE`
- OTP record is deleted
- Max 3 attempts before OTP is invalidated

**If OTP expired:**
```
POST /api/v1/otp/resend
{ "phoneNumber": "0821234567" }
```

---

### STEP 3: Login

**Endpoint:** `POST /api/v1/users/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Login successful.",
  "timestamp": "2026-03-14T10:02:00.000Z",
  "data": {
    "id": "6754e01b188ed126b2443093",
    "name": "User Internet",
    "email": "user@example.com",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "role": ["ROLE_USER"]
  }
}
```

> Add the `accessToken` as a Bearer token header on all subsequent authenticated requests:
> `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

---

### STEP 4: Add a Product (Admin)

**Endpoint:** `POST /api/v1/products/product`
**Content-Type:** `multipart/form-data`

| Field | Type | Value |
|---|---|---|
| `product` | String (JSON) | See below |
| `images` | File(s) | One or more image files |

**product JSON:**
```json
{
  "title": "Organic Fleece Hoodie",
  "description": "Lightweight hoodie made with organic fleece. Perfect for layering.",
  "category": "Clothing",
  "price": 49.99,
  "quantity": 25,
  "rate": "4.5"
}
```

**Expected Response:**
```json
{
  "status": "201 CREATED",
  "statusCode": 201,
  "message": "Product Added",
  "timestamp": "2026-03-14T10:03:00.000Z",
  "data": {
    "id": "6823a47985d0ae74b2d03534",
    "title": "Organic Fleece Hoodie",
    "description": "Lightweight hoodie made with organic fleece. Perfect for layering.",
    "category": "Clothing",
    "price": 49.99,
    "quantity": 25,
    "rate": "4.5",
    "imageUrls": [
      "https://your-bucket.s3.af-south-1.amazonaws.com/abc123_hoodie.jpg"
    ]
  }
}
```

> Add a second product for a richer cart:

```json
{
  "title": "Stretch Slim-Fit Jeans",
  "description": "Premium stretch denim with slim-fit cut.",
  "category": "Clothing",
  "price": 59.99,
  "quantity": 30,
  "rate": "4.3"
}
```

Returns ID: `6823a43685d0ae74b2d0352f`

---

### STEP 5: Add Product to Cart

**Endpoint:** `POST /api/v1/cart/add`

**Request Body:**
```json
{
  "userId": "6754e01b188ed126b2443093",
  "productId": "6823a47985d0ae74b2d03534",
  "quantity": 2
}
```

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Item added to cart.",
  "timestamp": "2026-03-14T10:05:00.000Z",
  "data": {
    "cartItems": [
      {
        "product": {
          "id": "6823a47985d0ae74b2d03534",
          "title": "Organic Fleece Hoodie",
          "price": 49.99,
          "quantity": 23
        },
        "quantity": 2,
        "productPrice": 99.98
      }
    ],
    "totalPrice": 99.98
  }
}
```

**What Happens:**
- Cart is created for the user if one doesn't exist
- Product stock is reduced by the requested quantity (25 → 23)
- If item already in cart, quantity is accumulated

---

### STEP 6: Add Second Product to Cart

**Endpoint:** `POST /api/v1/cart/add`

```json
{
  "userId": "6754e01b188ed126b2443093",
  "productId": "6823a43685d0ae74b2d0352f",
  "quantity": 3
}
```

**Expected Response:**
```json
{
  "data": {
    "cartItems": [
      {
        "product": { "title": "Organic Fleece Hoodie", "price": 49.99 },
        "quantity": 2,
        "productPrice": 99.98
      },
      {
        "product": { "title": "Stretch Slim-Fit Jeans", "price": 59.99 },
        "quantity": 3,
        "productPrice": 179.97
      }
    ],
    "totalPrice": 279.95
  }
}
```

---

### STEP 7: View Cart

**Endpoint:** `GET /api/v1/cart/6754e01b188ed126b2443093`

Returns current cart state with all items and total.

---

### STEP 8: Initiate Checkout

**Endpoint:** `POST /api/v1/checkout/6754e01b188ed126b2443093/initiate-checkout`

No request body needed.

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Checkout initiated.",
  "timestamp": "2026-03-14T10:10:00.000Z",
  "data": {
    "id": "65f8b1c234567890bcdef012",
    "cartId": "65f8a9b123456789abcdef01",
    "items": [
      {
        "product": { "title": "Organic Fleece Hoodie", "price": 49.99 },
        "quantity": 2,
        "productPrice": 99.98
      },
      {
        "product": { "title": "Stretch Slim-Fit Jeans", "price": 59.99 },
        "quantity": 3,
        "productPrice": 179.97
      }
    ],
    "subtotal": 279.95,
    "discount": 0.0,
    "tax": 27.99,
    "totalAmount": 307.94,
    "status": "PENDING",
    "paymentMethod": "NOT_SELECTED",
    "currency": "ZAR"
  }
}
```

**What Happens:**
- Checkout is created from the active cart
- Totals are calculated: subtotal → tax (10%) → total
- If a `PENDING` checkout already exists for this cart, it is returned instead of creating a duplicate

> **Save the checkout `id`** — you need it for the confirm step.

---

### STEP 9: Update Checkout (Addresses + Payment + Shipping)

**Endpoint:** `PUT /api/v1/checkout/6754e01b188ed126b2443093`

```json
{
  "shippingAddress": {
    "streetAddress": "51 Frank Ocean Street",
    "city": "Johannesburg",
    "state": "Gauteng",
    "country": "South Africa",
    "postalCode": "2003"
  },
  "billingAddress": {
    "streetAddress": "51 Frank Ocean Street",
    "city": "Johannesburg",
    "state": "Gauteng",
    "country": "South Africa",
    "postalCode": "2003"
  },
  "paymentMethod": "CREDIT_CARD",
  "shippingMethod": "DHL"
}
```

**Payment Method Options:**
- `CREDIT_CARD` — simulated card payment (70% success)
- `CASH_ON_DELIVERY` — always succeeds, order stays `PENDING` until delivery

**Shipping Method Options:**
| Method | Cost |
|---|---|
| `DHL` | R15.99 |
| `FedEx` | R12.99 |
| `Express` | R10.99 |
| `FREE` | R0.00 |

---

### STEP 10: Confirm Order

**Endpoint:** `POST /api/v1/checkout/65f8b1c234567890bcdef012/confirm`

No request body needed.

**Expected Response (Success — 70% chance):**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Order placed successfully.",
  "timestamp": "2026-03-14T10:15:00.000Z",
  "data": {
    "id": "65f8b2d345678901cdef0123",
    "orderNumber": "ORD-20260314-483920",
    "orderStatus": "CONFIRMED",
    "customerInfo": {
      "name": "User Internet",
      "email": "user@example.com",
      "phone": "0821234567"
    },
    "orderItems": [
      {
        "quantity": 2,
        "unitPrice": 49.99,
        "totalPrice": 99.98,
        "discount": 0.0,
        "tax": 0.0
      },
      {
        "quantity": 3,
        "unitPrice": 59.99,
        "totalPrice": 179.97,
        "discount": 0.0,
        "tax": 0.0
      }
    ],
    "paymentDetails": {
      "transactionId": "TXN-20260314-8F4E2A",
      "paymentStatus": "COMPLETED",
      "paymentMethod": "CREDIT_CARD",
      "paymentDate": "2026-03-14T10:15:00"
    },
    "shippingAddress": {
      "streetAddress": "51 Frank Ocean Street",
      "city": "Johannesburg",
      "state": "Gauteng",
      "country": "South Africa",
      "postalCode": "2003"
    },
    "subtotal": 279.95,
    "discount": 0.0,
    "tax": 27.99,
    "shippingCost": 15.99,
    "totalAmount": 323.93
  }
}
```

**Expected Response (Failure — 30% chance):**
```json
{
  "status": "402 PAYMENT_REQUIRED",
  "statusCode": 402,
  "message": "Card declined",
  "timestamp": "2026-03-14T10:15:00.000Z"
}
```

**What Happens on Success:**
1. Checkout is validated (addresses, stock levels, pricing)
2. Payment is simulated with a 500–2000ms delay
3. Order is created with a unique order number (`ORD-yyyyMMdd-XXXXXX`)
4. Transaction ID is generated
5. Inventory is reduced for each product
6. Cart is cleared
7. Checkout status → `COMPLETED`

**What Happens on Failure:**
1. Checkout status → `FAILED`
2. Cart remains unchanged
3. Retry by calling confirm again — a new payment attempt is made

---

### STEP 11: View Order

**Endpoint:** `GET /api/v1/orders/65f8b2d345678901cdef0123`

Returns full order including items, payment details, shipping address, and status history.

---

### STEP 12: Track by Order Number

**Endpoint:** `GET /api/v1/orders/number/ORD-20260314-483920`

Same response as above. Useful for customer-facing tracking.

---

### STEP 13: View Order History

**Endpoint:** `GET /api/v1/orders/user/6754e01b188ed126b2443093`

Returns all orders for the user.

---

## Alternative Flow: Update or Remove Cart Items

### Update Item Quantity

**PATCH** `/api/v1/cart/update`

```json
{
  "userId": "6754e01b188ed126b2443093",
  "productId": "6823a47985d0ae74b2d03534",
  "newQuantity": 1
}
```

- If `newQuantity <= 0`, the item is removed from the cart
- Stock is adjusted accordingly (delta between old and new quantity)

### Remove Item from Cart

**DELETE** `/api/v1/cart/remove`

```json
{
  "userId": "6754e01b188ed126b2443093",
  "productId": "6823a47985d0ae74b2d03534"
}
```

Stock is fully restored for the removed item.

---

## Data State After Successful Flow

### Before:
```
Users:    1 (AWAITING_CONFIRMATION)
Cart:     empty
Checkout: none
Orders:   none
Hoodie:   25 units
Jeans:    30 units
```

### After:
```
Users:    1 (ACTIVE)
Cart:     empty (cleared after order)
Checkout: 1 (COMPLETED)
Orders:   1 (CONFIRMED)
Hoodie:   23 units (reduced by 2)
Jeans:    27 units (reduced by 3)
```

---

## Order Status Lifecycle

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                                                  ↓
                                              REFUNDED

Any status before SHIPPED → CANCELLED
```

Admin endpoint to update status:

**PATCH** `/api/v1/orders/{orderId}/status`

```json
{
  "status": "PROCESSING",
  "notes": "Order picked and packed."
}
```

---

## IDs to Track Per Session

| Item | Where It Comes From |
|---|---|
| `userId` | Step 1 (register) |
| `productId` | Step 4 (add product) |
| `cartId` | Step 5 (add to cart) |
| `checkoutId` | Step 8 (initiate checkout) |
| `orderId` | Step 10 (confirm order) |
| `orderNumber` | Step 10 (confirm order) |

---

## Troubleshooting

| Error | Cause | Fix |
|---|---|---|
| `No OTP found` | OTP expired or never generated | Call `/otp/resend` |
| `User is already active` | Trying to activate an already active account | Proceed to login |
| `Cannot checkout with an empty cart` | Cart has no items | Add items first |
| `Cannot confirm checkout. Current status: COMPLETED` | Checkout already used | Initiate a new checkout |
| `Insufficient stock` | Product quantity too low | Reduce quantity or restock |
| `Payment keeps failing` | 30% failure rate by design | Retry confirm |
| `No pending checkouts found` | Trying to delete a checkout that doesn't exist | Check checkout status |