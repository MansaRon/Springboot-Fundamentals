# TODO: Features & Improvements

## Current Status: Core E-Commerce Complete ✅

### Completed in Current Session
-  OTP flow (generate, validate, resend, scheduled cleanup)
-  Product module refactored (AWS S3 image storage, Image collection removed)
-  Cart service refactored (ModelMapper, stock delta fix, builder pattern)
-  Checkout service refactored (CheckoutMapper removed, clearCart delegated)
-  Order service refactored (OrderMapper removed, statusHistory bug fixed)
-  Payment service refactored (PaymentStatus enum, validatePaymentRequest wired)
-  Manual mappers (CartMapper, CheckoutMapper, OrderMapper) removed
-  @RequiredArgsConstructor enforced across all services
-  NOT_FOUND vs BAD_REQUEST HTTP status codes corrected
-  isOnlinePaymentMethod logic fixed (CREDIT_CARD only)
-  Fix the DTO returned for confirm user API
-  Add schema for categories for products, making it clear to distinguish
-  Add schema for roles for users
-  Add pagination to the wishlist API

---

## Features to Add

### High Priority

#### 1. Email Notifications
- [ ] Order confirmation email
- [ ] Order status update emails (shipped, delivered)
- [ ] Payment failure notification
- [ ] Welcome email after OTP activation
- **Tech:** Spring Mail + Thymeleaf templates
- **Estimate:** 4–6 hours

#### 2. Global Exception Handler
- [ ] `@RestControllerAdvice` to catch all exceptions centrally
- [ ] Map `ClientException`, `ProductException`, `CartException`, `CheckoutException`, `OrderException`, `OTPException`, `PaymentException` to consistent `GlobalApiResponse` error shape
- [ ] Remove any remaining try-catch in service layer
- [ ] Log all exceptions with correlation ID
- **Priority:** High — needed before unit tests
- **Estimate:** 3–4 hours

#### 3. Coupon / Promo Code System
- [ ] `Coupon` entity (code, discount %, expiry, usage limit)
- [ ] Apply coupon at checkout
- [ ] Validate coupon (active, not expired, not exceeded)
- [ ] Track coupon usage per user
- [ ] Admin: create / deactivate coupons
- **Estimate:** 6–8 hours

#### 4. Order Cancellation (Customer)
- [ ] Cancel `PENDING` or `CONFIRMED` orders only
- [ ] Restore inventory on cancellation
- [ ] Add cancellation reason to status history
- [ ] Prevent cancellation after `SHIPPED`
- **Estimate:** 3–4 hours

#### 5. Refund Processing
- [ ] Admin initiates refund on `DELIVERED` orders
- [ ] Partial vs full refund support
- [ ] Restore inventory if applicable
- [ ] Track refund status in `PaymentDetails`
- **Estimate:** 4–5 hours

#### 6. Product Reviews & Ratings
- [ ] Customer can review purchased products only
- [ ] Star rating (1–5) + review text
- [ ] Calculate and update average product rating
- [ ] Admin moderation (approve/reject)
- **Estimate:** 6–8 hours

### Medium Priority

#### 7. Product Search & Filtering
- [ ] Full-text search by title/description
- [ ] Filter by category, price range, rating
- [ ] Sort by price, newest, popularity
- **Tech:** MongoDB text indexes
- **Estimate:** 5–6 hours

#### 8. Order Invoice Generation
- [ ] PDF invoice with order details, items, totals, branding
- [ ] Download from order endpoint
- **Tech:** iText or Apache PDFBox
- **Estimate:** 4–5 hours

#### 9. Inventory Alerts
- [ ] Low stock warning threshold (< 5 units)
- [ ] Admin notification when stock is critical
- [ ] Block checkout if stock hits zero during validation
- **Estimate:** 2–3 hours

#### 10. Multiple Shipping Addresses
- [ ] Save address book per user
- [ ] Set default address
- [ ] Select saved address at checkout
- **Estimate:** 4–5 hours

#### 11. Pagination for Order Endpoints
- [ ] Paginate `getAllOrders()`
- [ ] Paginate `getUserOrders()`
- [ ] Consistent with existing product pagination pattern
- **Estimate:** 2–3 hours

### Low Priority

#### 12. Guest Checkout
- [ ] Checkout without account
- [ ] Track guest order by email + order number
- **Estimate:** 5–6 hours

#### 13. Real Payment Integration
- [ ] Replace simulated payment with Stripe or PayFast
- [ ] Webhook handler for async payment confirmation
- **Estimate:** 8–10 hours

#### 14. Loyalty Points System
- [ ] Earn points on purchases
- [ ] Redeem points for discounts
- [ ] Bronze / Silver / Gold tiers
- **Estimate:** 8–10 hours

#### 15. Product Recommendations
- [ ] "Customers also bought" based on order history
- [ ] Recently viewed products
- **Estimate:** 10–12 hours

---

## Code Improvements & Refactoring

### High Priority

#### 1. Input Validation Enhancement
- [ ] Ensure `@Valid` on all controller request bodies
- [ ] Custom validator for `ObjectId` format (replace manual regex checks)
- [ ] Validate price is positive, quantity is within range
- [ ] Phone number format validation on registration
- **Estimate:** 3–4 hours

#### 2. API Security with @PreAuthorize
- [ ] Enable `@EnableMethodSecurity` in security config
- [ ] Admin-only: product create/update/delete, deleteAllProducts, updateOrderStatus
- [ ] User + Admin: cart, checkout, user orders
- [ ] Ownership check: users can only access their own cart/orders
  ```java
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
  ```
- **Estimate:** 3–4 hours

#### 3. Swagger / OpenAPI Documentation
- [ ] Add `springdoc-openapi` dependency
- [ ] Document all endpoints with request/response examples
- [ ] Add auth header documentation
- [ ] Accessible at `/swagger-ui.html`
- **Tech:** Springdoc OpenAPI
- **Estimate:** 4–5 hours

#### 4. Soft Delete
- [ ] Add `deleted` boolean and `deletedAt` timestamp to `Entity`
- [ ] Override repository queries to filter deleted records
- [ ] Affects: `Product`, `User`, `Order`
- **Estimate:** 3–4 hours

### Medium Priority

#### 5. Caching Strategy
- [ ] Cache product catalog (frequently read, rarely written)
- [ ] Cache user cart in Redis for performance
- [ ] Cache invalidation on product update/delete
- **Tech:** Spring Cache + Redis
- **Estimate:** 5–6 hours

#### 6. Rate Limiting
- [ ] Limit OTP generation requests (prevent SMS abuse)
- [ ] Limit login attempts
- [ ] General API rate limit per user
- **Tech:** Bucket4j
- **Estimate:** 4–5 hours

#### 7. Structured Logging
- [ ] JSON log format for production
- [ ] Correlation ID per request (MDC)
- [ ] Sensitive data masking (OTP, passwords, card hints)
- [ ] Environment-specific log levels
- **Estimate:** 3–4 hours

#### 8. Async Processing
- [ ] Async email sending (don't block request thread)
- [ ] Async inventory update after order creation
- **Tech:** Spring `@Async`
- **Estimate:** 3–4 hours

### Low Priority

#### 9. Health Checks & Monitoring
- [ ] Spring Actuator endpoints
- [ ] MongoDB health indicator
- [ ] S3 connectivity check
- [ ] Prometheus metrics export
- **Estimate:** 4–5 hours

---

## Known Issues / Tech Debt

### Critical

#### 1. Inventory Race Condition
- **Issue:** Multiple users can checkout the same last unit simultaneously before stock is reduced
- **Current:** No inventory reservation — stock reduced only after payment confirms
- **Solution:** Pessimistic locking or inventory reservation at checkout creation
- **Priority:** High
- **Estimate:** 4–5 hours

#### 2. Price Validation Mismatch
- **Issue:** Cart price can become stale if a product price changes between add-to-cart and checkout
- **Current Solution:** `recalculateTotals()` re-runs before payment
- **Better Solution:** Snapshot product price at cart-add time, show price change warning if it drifts
- **Priority:** High
- **Estimate:** 2–3 hours

### Medium

#### 3. Order Number Collision
- **Issue:** `ORD-yyyyMMdd-XXXXXX` with random 6-digit suffix has a non-zero collision probability under load
- **Solution:** Add uniqueness check on save, or switch to UUID-based order numbers
- **Priority:** Medium
- **Estimate:** 1–2 hours

#### 4. No Soft Delete
- **Issue:** Deleting a product that exists in order history breaks order item references
- **Solution:** Soft delete on `Product` and `User`
- **Priority:** Medium
- **Estimate:** 3–4 hours

#### 5. CartItems Not an Entity
- **Issue:** `CartItems` is embedded in `Cart` without its own `@Id`, making individual item updates depend on list index position rather than ID
- **Solution:** Add `@Id` to `CartItems` or use a separate collection
- **Priority:** Medium
- **Estimate:** 2–3 hours

---

## Infrastructure & Deployment

### High Priority

#### 1. Docker Containerization
- [ ] `Dockerfile` with multi-stage build (build → runtime)
- [ ] `docker-compose.yml` with app + MongoDB
- [ ] Environment variable injection via `.env`
- **Estimate:** 2–3 hours

#### 2. CI/CD Pipeline (GitHub Actions)
- [ ] On PR: run tests, fail if coverage drops
- [ ] On merge to main: build Docker image, push to registry
- [ ] On tag: deploy to staging
- **Estimate:** 4–5 hours

#### 3. Environment Configuration
- [ ] Separate `application-dev.yml`, `application-prod.yml`
- [ ] All secrets via environment variables (no hardcoded values)
- [ ] MongoDB URI, JWT secret, AWS keys from env
- **Estimate:** 2–3 hours

### Medium Priority

#### 4. Kubernetes Deployment
- [ ] `Deployment`, `Service`, `ConfigMap` manifests
- [ ] Horizontal Pod Autoscaling on CPU
- [ ] Liveness and readiness probes
- **Estimate:** 6–8 hours

---

## Testing Roadmap

### Phase 1 — Unit Tests (Next)
- [ ] `OTPServiceImpl` — generate, validate, resend, cleanup
- [ ] `CartServiceImpl` — add, update, delete, stock logic
- [ ] `CheckoutServiceImpl` — create, update, confirm, cancel
- [ ] `CheckoutValidationServiceImpl` — all validation branches
- [ ] `OrderServiceImpl` — create, status transitions
- [ ] `PaymentServiceImpl` — success/failure paths
- [ ] `ProductServiceImpl` — CRUD, pagination
- **Target:** 80% coverage
- **Tech:** JUnit 5 + Mockito

### Phase 2 — Integration Tests
- [ ] Full cart → checkout → order flow with embedded MongoDB
- [ ] S3 upload mock (LocalStack or Mockito)
- [ ] OTP generation and validation cycle
- **Tech:** Spring Boot Test + Testcontainers

### Phase 3 — E2E Tests
- [ ] Full flow via REST Assured or Postman/Newman
- [ ] Run in CI pipeline against a test environment

---

## Roadmap Summary

| Phase | Focus | Status |
|---|---|---|
| 1 — Foundation | Cart, Checkout, Orders, Payment simulation | ✅ Complete |
| 2 — Hardening | Unit tests, Global exception handler, @PreAuthorize, Validation | 🔄 Next |
| 3 — Docker & CI | Dockerfile, docker-compose, GitHub Actions pipeline | ⏳ Planned |
| 4 — Features | Email, Coupons, Order cancellation, Reviews | ⏳ Planned |
| 5 — Production | Real payment, Caching, Rate limiting, Monitoring | ⏳ Future |