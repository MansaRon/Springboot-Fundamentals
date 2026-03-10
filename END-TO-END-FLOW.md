# End-to-End Flow: Complete User Journey

## Overview
This document walks through the **complete user journey** from adding products to cart through to viewing the order confirmation.

---

## Prerequisites

### Database Setup
Ensure you have:
- MongoDB running
- At least one user in the `users` collection
- Products in the `products` collection

### Sample User
```json
{
  "_id": ObjectId("6754e01b188ed126b2443093"),
  "name": "Michael Jordan",
  "email": "test@gmail.com",
  "phone": "0113459000"
}
```

### Sample Products
```json
{
  "_id": ObjectId("6823a47985d0ae74b2d03534"),
  "title": "Organic Fleece Hoodie",
  "description": "Lightweight hoodie made with organic fleece",
  "category": "Clothing",
  "price": 49.99,
  "quantity": 25,
  "imageUrl": "https://example.com/images/hoodie.jpg",
  "rate": "4.5"
}
```

---

## Complete Flow (Step-by-Step)

### **STEP 1: Add Product to Cart**

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
  "timestamp": "2025-03-15T10:00:00.000Z",
  "data": {
    "id": "65f8a9b123456789abcdef01",
    "userId": "6754e01b188ed126b2443093",
    "cartItems": [
      {
        "product": {
          "id": "6823a47985d0ae74b2d03534",
          "title": "Organic Fleece Hoodie",
          "price": 49.99
        },
        "quantity": 2,
        "productPrice": 99.98
      }
    ],
    "totalAmount": 99.98
  }
}
```

**What Happens:**
- Creates or updates user's cart
- Adds product with specified quantity
- Calculates cart total

---

### **STEP 2: Add Another Product (Optional)**

**Endpoint:** `POST /api/v1/cart/add`

**Request Body:**
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
  "status": "200 OK",
  "statusCode": 200,
  "message": "Item added to cart.",
  "timestamp": "2025-03-15T10:01:00.000Z",
  "data": {
    "cartItems": [
      {
        "product": {
          "title": "Organic Fleece Hoodie",
          "price": 49.99
        },
        "quantity": 2,
        "productPrice": 99.98
      },
      {
        "product": {
          "title": "Stretch Slim-Fit Jeans",
          "price": 59.99
        },
        "quantity": 3,
        "productPrice": 179.97
      }
    ],
    "totalAmount": 279.95
  }
}
```

---

### **STEP 3: View Cart**

**Endpoint:** `GET /api/v1/cart/{userId}`

**URL:** `GET /api/v1/cart/6754e01b188ed126b2443093`

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Cart retrieved.",
  "timestamp": "2025-03-15T10:02:00.000Z",
  "data": {
    "id": "65f8a9b123456789abcdef01",
    "cartItems": [
      {
        "product": {
          "id": "6823a47985d0ae74b2d03534",
          "title": "Organic Fleece Hoodie",
          "price": 49.99
        },
        "quantity": 2,
        "productPrice": 99.98
      },
      {
        "product": {
          "id": "6823a43685d0ae74b2d0352f",
          "title": "Stretch Slim-Fit Jeans",
          "price": 59.99
        },
        "quantity": 3,
        "productPrice": 179.97
      }
    ],
    "totalAmount": 279.95
  }
}
```

**What Happens:**
- Retrieves user's current cart
- Shows all items and totals

---

### **STEP 4: Proceed to Checkout**

**Endpoint:** `POST /api/v1/checkout/{userId}/initiate-checkout`

**URL:** `POST /api/v1/checkout/6754e01b188ed126b2443093/initiate-checkout`

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Checkout initiated.",
  "timestamp": "2025-03-15T10:05:00.000Z",
  "data": {
    "id": "65f8b1c234567890bcdef012",
    "userId": "6754e01b188ed126b2443093",
    "cartId": "65f8a9b123456789abcdef01",
    "items": [
      {
        "product": {
          "title": "Organic Fleece Hoodie",
          "price": 49.99
        },
        "quantity": 2,
        "productPrice": 99.98
      },
      {
        "product": {
          "title": "Stretch Slim-Fit Jeans",
          "price": 59.99
        },
        "quantity": 3,
        "productPrice": 179.97
      }
    ],
    "subtotal": 279.95,
    "discount": 27.99,
    "tax": 37.79,
    "totalAmount": 289.75,
    "status": "PENDING",
    "paymentMethod": "NOT_SELECTED",
    "currency": "ZAR"
  }
}
```

**What Happens:**
- Creates checkout from cart
- Calculates discount (10% if subtotal > R100)
- Calculates tax (15% VAT)
- Returns checkout ID for next step

**IMPORTANT:** Save the checkout ID (`65f8b1c234567890bcdef012`) for the next steps!

---

### **STEP 5: Update Checkout (Add Addresses & Payment Method)**

**Endpoint:** `PUT /api/v1/checkout/{userId}`

**URL:** `PUT /api/v1/checkout/6754e01b188ed126b2443093`

**Request Body:**
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
  "paymentMethod": "CARD",
  "shippingMethod": "DHL"
}
```

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Checkout updated.",
  "timestamp": "2025-03-15T10:10:00.000Z",
  "data": {
    "id": "65f8b1c234567890bcdef012",
    "subtotal": 279.95,
    "discount": 27.99,
    "tax": 37.79,
    "totalAmount": 289.75,
    "paymentMethod": "CARD",
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
    "shippingMethod": "DHL",
    "status": "PENDING"
  }
}
```

**What Happens:**
- Updates checkout with shipping/billing addresses
- Sets payment method to CARD
- Sets shipping method to DHL

---

### **STEP 6: Confirm Order (Place Order)**

**Endpoint:** `POST /api/v1/checkout/{checkoutId}/confirm`

**URL:** `POST /api/v1/checkout/65f8b1c234567890bcdef012/confirm`

**No Request Body Needed**

**Expected Response (Success - 70% chance):**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Order placed successfully!",
  "timestamp": "2025-03-15T10:15:00.000Z",
  "data": {
    "id": "65f8b2d345678901cdef0123",
    "orderNumber": "ORD-20250315-A7B9C2",
    "orderStatus": "CONFIRMED",
    "customerInfo": {
      "id": "6754e01b188ed126b2443093",
      "name": "Michael Jordan",
      "email": "test@gmail.com"
    },
    "orderItems": [
      {
        "product": {
          "title": "Organic Fleece Hoodie",
          "price": 49.99
        },
        "quantity": 2,
        "unitPrice": 49.99,
        "totalPrice": 99.98
      },
      {
        "product": {
          "title": "Stretch Slim-Fit Jeans",
          "price": 59.99
        },
        "quantity": 3,
        "unitPrice": 59.99,
        "totalPrice": 179.97
      }
    ],
    "paymentDetails": {
      "transactionId": "TXN-20250315-8F4E2A",
      "paymentStatus": "COMPLETED",
      "paymentMethod": "CARD",
      "paymentDate": "2025-03-15T10:15:00"
    },
    "shippingAddress": {
      "streetAddress": "51 Frank Ocean Street",
      "city": "Johannesburg",
      "state": "Gauteng",
      "country": "South Africa",
      "postalCode": "2003"
    },
    "subtotal": 279.95,
    "discount": 27.99,
    "tax": 37.79,
    "shippingCost": 15.99,
    "totalAmount": 305.74,
    "orderDate": "2025-03-15T10:15:00",
    "estimatedDeliveryDate": "2025-03-20T10:15:00",
    "currency": "ZAR"
  }
}
```

**Expected Response (Failure - 30% chance):**
```json
{
  "status": "402 PAYMENT_REQUIRED",
  "statusCode": 402,
  "message": "Card declined",
  "timestamp": "2025-03-15T10:15:00.000Z",
  "error": {
    "code": "PAYMENT_FAILED",
    "details": "Card declined"
  }
}
```

**What Happens (Success):**
1. Validates checkout (addresses, inventory, pricing)
2. Simulates payment processing (500-2000ms delay)
3. Payment succeeds (70% probability)
4. Creates Order with unique order number
5. Generates transaction ID
6. Reduces inventory for each product
7. Clears the shopping cart
8. Updates checkout status to COMPLETED

**What Happens (Failure):**
1. Validates checkout
2. Simulates payment processing
3. Payment fails (30% probability)
4. Checkout status set to FAILED
5. Cart remains unchanged
6. Can retry by calling confirm again

---

### **STEP 7: View Order Confirmation**

**Endpoint:** `GET /api/v1/orders/{orderId}`

**URL:** `GET /api/v1/orders/65f8b2d345678901cdef0123`

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Order retrieved successfully.",
  "timestamp": "2025-03-15T10:20:00.000Z",
  "data": {
    "id": "65f8b2d345678901cdef0123",
    "orderNumber": "ORD-20250315-A7B9C2",
    "orderStatus": "CONFIRMED",
    "orderDate": "2025-03-15T10:15:00",
    "customerInfo": {
      "name": "Michael Jordan",
      "email": "test@gmail.com",
      "phone": "0113459000"
    },
    "orderItems": [
      {
        "product": {
          "title": "Organic Fleece Hoodie",
          "imageUrl": "https://example.com/images/hoodie.jpg"
        },
        "quantity": 2,
        "unitPrice": 49.99,
        "totalPrice": 99.98
      },
      {
        "product": {
          "title": "Stretch Slim-Fit Jeans",
          "imageUrl": "https://example.com/images/jeans.jpg"
        },
        "quantity": 3,
        "unitPrice": 59.99,
        "totalPrice": 179.97
      }
    ],
    "paymentDetails": {
      "transactionId": "TXN-20250315-8F4E2A",
      "paymentStatus": "COMPLETED",
      "paymentMethod": "CARD"
    },
    "shippingAddress": {
      "streetAddress": "51 Frank Ocean Street",
      "city": "Johannesburg",
      "state": "Gauteng",
      "country": "South Africa",
      "postalCode": "2003"
    },
    "totalAmount": 305.74,
    "estimatedDeliveryDate": "2025-03-20T10:15:00",
    "statusHistory": [
      {
        "status": "CONFIRMED",
        "timestamp": "2025-03-15T10:15:00",
        "notes": "Order created"
      }
    ]
  }
}
```

---

### **STEP 8: View Order History**

**Endpoint:** `GET /api/v1/orders/user/{userId}`

**URL:** `GET /api/v1/orders/user/6754e01b188ed126b2443093`

**Expected Response:**
```json
{
  "status": "200 OK",
  "statusCode": 200,
  "message": "Retrieved 1 orders.",
  "timestamp": "2025-03-15T10:25:00.000Z",
  "data": [
    {
      "id": "65f8b2d345678901cdef0123",
      "orderNumber": "ORD-20250315-A7B9C2",
      "orderStatus": "CONFIRMED",
      "orderDate": "2025-03-15T10:15:00",
      "totalAmount": 305.74,
      "paymentStatus": "COMPLETED"
    }
  ]
}
```

---

### **STEP 9: Track Order by Order Number**

**Endpoint:** `GET /api/v1/orders/number/{orderNumber}`

**URL:** `GET /api/v1/orders/number/ORD-20250315-A7B9C2`

**Expected Response:**
Same as Step 7 (Get order by ID)

---

## Alternative Flow: Payment Failure & Retry

### Scenario: Payment Fails (30% chance)

**Step 6 Response (Failure):**
```json
{
  "status": "402 PAYMENT_REQUIRED",
  "statusCode": 402,
  "message": "Insufficient funds",
  "timestamp": "2025-03-15T10:15:00.000Z"
}
```

**What to do:**
1. Display error to user
2. Keep checkout active
3. User can retry by calling confirm again:
   ```
   POST /api/v1/checkout/65f8b1c234567890bcdef012/confirm
   ```
4. New payment attempt (another 70/30 chance)

---

## Data State After Complete Flow

### Before Flow:
```
Cart: 2 items (Hoodie x2, Jeans x3)
Checkouts: 0
Orders: 0
Product Inventory:
  - Hoodie: 25 units
  - Jeans: 30 units
```

### After Successful Flow:
```
Cart: Empty (cleared after order)
Checkouts: 1 (status: COMPLETED)
Orders: 1 (status: CONFIRMED)
Product Inventory:
  - Hoodie: 23 units (reduced by 2)
  - Jeans: 27 units (reduced by 3)
```

---

## Important Notes

### IDs to Track:
1. **User ID:** `6754e01b188ed126b2443093` (from prerequisites)
2. **Cart ID:** Returned in Step 1
3. **Checkout ID:** Returned in Step 4 (use this for Step 6!)
4. **Order ID:** Returned in Step 6
5. **Order Number:** Returned in Step 6 (for tracking)

### Payment Method Options:
- `CARD` - Simulated card payment (70% success)
- `CASH_ON_DELIVERY` - Always succeeds, payment pending

### Shipping Method Options:
- `DHL` - R15.99
- `FedEx` - R12.99
- `Express` - R10.99
- `FREE` - R0.00

---

## Success Criteria

After completing this flow, verify:
- [ ] Cart is created with items
- [ ] Cart totals are correct
- [ ] Checkout is created from cart
- [ ] Discount applied (10% if subtotal > R100)
- [ ] Tax calculated (15% VAT)
- [ ] Addresses saved to checkout
- [ ] Payment simulated (may need retry if failed)
- [ ] Order created with unique order number
- [ ] Transaction ID generated
- [ ] Inventory reduced
- [ ] Cart cleared
- [ ] Order retrievable by ID and order number
- [ ] Order appears in user's order history

---

## Troubleshooting

### "Checkout not found"
- Make sure you're using the checkout ID from Step 4, not the cart ID

### "Cannot confirm checkout. Current status: COMPLETED"
- Checkout already confirmed, create a new one

### "Insufficient inventory"
- Check product quantities in database

### Payment keeps failing
- Normal! 30% failure rate. Keep retrying.

### Cart not clearing
- Check if order was actually created
- Verify confirmCheckout completed successfully
