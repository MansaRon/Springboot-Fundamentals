# 🛒 E-Commerce Backend API

A full-featured e-commerce REST API built with **Spring Boot** and **MongoDB**, supporting product management, cart operations, checkout flows, order management, OTP-based authentication, and simulated payment processing.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Database | MongoDB |
| ODM | Spring Data MongoDB |
| Auth | Spring Security + JWT |
| Image Storage | AWS S3 (SDK v2) |
| Mapping | ModelMapper |
| Boilerplate | Lombok |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB (local or Atlas)
- AWS account with an S3 bucket (for image uploads)

---

## Running Locally

### 1. Clone the repository

```bash
git clone https://github.com/your-username/ecommerce-backend.git
cd ecommerce-backend
```

### 2. Configure environment

Create `src/main/resources/application.yml` (or copy from `application.yml.example`):

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/ecommerce

aws:
  s3:
    bucket-name: your-bucket-name
    region: af-south-1
    access-key: YOUR_ACCESS_KEY
    secret-key: YOUR_SECRET_KEY

jwt:
  secret: your-jwt-secret
  expiration: 86400000
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The API will start at `http://localhost:8080`.

---

## API Overview

| Module | Base Path |
|---|---|
| Auth / Users | `/api/v1/users` |
| OTP | `/api/v1/otp` |
| Products | `/api/v1/products` |
| Cart | `/api/v1/cart` |
| Checkout | `/api/v1/checkout` |
| Orders | `/api/v1/orders` |

---

## End-to-End Flow

### Prerequisites

Ensure MongoDB is running and you have registered and activated a user. You'll need a user ID and at least one product in the database before running the cart → checkout → order flow.

---

### Step 1 — Register User

**POST** `/api/v1/users/register`

```json
{
  "name": "User Internet",
  "email": "user@example.com",
  "phone": "0821234567",
  "pwd": "SecurePass123"
}
```

User is created with status `AWAITING_CONFIRMATION`. An OTP is sent to the phone number.

---

### Step 2 — Activate Account via OTP

**POST** `/api/v1/otp/validate`

```json
{
  "phoneNumber": "0821234567",
  "otp": "483920"
}
```

On success, account status changes to `ACTIVE`.

---

### Step 3 — Login

**POST** `/api/v1/users/login`

```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

Returns `accessToken` and `refreshToken`. Use the `accessToken` as a Bearer token on subsequent requests.

---

### Step 4 — Add a Product (Admin)

**POST** `/api/v1/products/product` — `multipart/form-data`

| Field | Value |
|---|---|
| `product` | JSON string (see below) |
| `images` | One or more image files |

```json
{
  "title": "Organic Fleece Hoodie",
  "description": "Lightweight hoodie made with organic fleece",
  "category": "Clothing",
  "price": 49.99,
  "quantity": 25,
  "rate": "4.5"
}
```

Images are uploaded to S3. The response includes `imageUrls` with the public S3 URLs.

---

### Step 5 — Add Product to Cart

**POST** `/api/v1/cart/add`

```json
{
  "userId": "6754e01b188ed126b2443093",
  "productId": "6823a47985d0ae74b2d03534",
  "quantity": 2
}
```

Creates or updates the cart. Reduces product stock by the requested quantity.

---

### Step 6 — Add Another Product to Cart

**POST** `/api/v1/cart/add`

```json
{
  "userId": "6754e01b188ed126b2443093",
  "productId": "6823a43685d0ae74b2d0352f",
  "quantity": 3
}
```

---

### Step 7 — View Cart

**GET** `/api/v1/cart/6754e01b188ed126b2443093`

Returns all cart items, quantities, and the running total.

---

### Step 8 — Initiate Checkout

**POST** `/api/v1/checkout/6754e01b188ed126b2443093/initiate-checkout`

No request body needed. Calculates subtotal, applies 10% tax, and creates a `PENDING` checkout. Returns the `checkoutId` — save this for the confirm step.

```json
{
  "subtotal": 279.95,
  "tax": 27.995,
  "totalAmount": 307.945,
  "status": "PENDING",
  "paymentMethod": "NOT_SELECTED"
}
```

---

### Step 9 — Update Checkout (Addresses + Payment)

**PUT** `/api/v1/checkout/6754e01b188ed126b2443093`

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

Payment method options: `CREDIT_CARD`, `CASH_ON_DELIVERY`
Shipping method options: `DHL` (R15.99), `FedEx` (R12.99), `Express` (R10.99), `FREE` (R0.00)

---

### Step 10 — Confirm Order

**POST** `/api/v1/checkout/{checkoutId}/confirm`

No request body. Runs full validation, simulates payment (70% success rate), creates the order, reduces inventory, and clears the cart.

**On success:**
```json
{
  "orderNumber": "ORD-20260314-483920",
  "orderStatus": "CONFIRMED",
  "paymentDetails": {
    "transactionId": "TXN-20260314-8F4E2A",
    "paymentStatus": "COMPLETED"
  },
  "totalAmount": 323.935
}
```

**On payment failure (30% chance):** Returns `402 PAYMENT_REQUIRED`. Checkout status is set to `FAILED`. Retry by calling confirm again.

---

### Step 11 — View Order

**GET** `/api/v1/orders/{orderId}`

Returns full order details including items, payment info, shipping address, and status history.

---

### Step 12 — Track by Order Number

**GET** `/api/v1/orders/number/ORD-20260314-483920`

---

## Order Status Lifecycle

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → REFUNDED
                                                       ↘ CANCELLED
```

Cancelled and Refunded orders cannot have their status updated further.

---

## OTP Flow

| Endpoint | Purpose |
|---|---|
| `POST /api/v1/otp/generate` | Generate OTP for phone number |
| `POST /api/v1/otp/resend` | Resend / regenerate OTP |
| `POST /api/v1/otp/validate` | Validate OTP to activate account |

- OTPs expire after **5 minutes**
- Maximum **3 attempts** before OTP is invalidated
- Expired OTPs are cleaned up automatically every hour

---

## Shipping Costs

| Method | Cost |
|---|---|
| DHL | R15.99 |
| FedEx | R12.99 |
| Express | R10.99 |
| FREE | R0.00 |

---

## Environment Variables

| Variable | Description |
|---|---|
| `MONGO_URI` | MongoDB connection string |
| `AWS_BUCKET_NAME` | S3 bucket name |
| `AWS_REGION` | AWS region (e.g. `af-south-1`) |
| `AWS_ACCESS_KEY` | IAM access key |
| `AWS_SECRET_KEY` | IAM secret key |
| `JWT_SECRET` | JWT signing secret |
| `JWT_EXPIRATION` | Token expiry in milliseconds |

---

## Roadmap

See [TODO.md](./TODO.md) for the full feature backlog and improvement tracker.

### Next Up
- Unit tests (JUnit 5 + Mockito)
- Docker + docker-compose setup
- CI/CD pipeline (GitHub Actions)
- Global exception handler (`@RestControllerAdvice`)
- Swagger / OpenAPI documentation
- Email notifications (Spring Mail)
- Coupon/promo code system

---

## Known Issues

- **Inventory race condition** — no reservation lock during concurrent checkouts
- **Order number collision** — low probability but possible with random generation
- **No soft delete** — hard deletes on products can affect order history integrity