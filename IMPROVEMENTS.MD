# TODO: Features & Improvements

## Current Status: Core E-Commerce Complete

---

## Features to Add

> 👑 = work that falls within the **Admin** role's scope.
> Fully completed features (e.g. customer Order Cancellation) have been removed from this list.

### High Priority

#### 1. Email Notifications — *mostly done*
- [x] Order confirmation email
- [x] Order status update emails (shipped, delivered)
- [x] Payment failure notification
- [x] Welcome email for new users
- [ ] Cart abandonment reminder
- **Tech:** Spring Mail, Thymeleaf templates
- **Status:** `EmailService` wired into order confirmation, status updates, payment failure and registration. Only cart-abandonment remains.

#### 2. Coupon/Promo Code System
- [ ] Create Coupon entity (code, discount %, expiry date)
- [ ] Apply coupon at checkout
- [ ] Validate coupon (active, not expired, usage limit)
- [ ] Track coupon usage per user
- [ ] 👑 Admin: Create/manage coupons
- **Estimate:** 6-8 hours

#### 3. Refund Processing 👑
- [ ] 👑 Admin initiates refund
- [ ] Partial vs full refund
- [ ] Restore inventory if applicable
- [ ] Track refund status
- [ ] Refund history in order details
- **Estimate:** 4-5 hours
- **Note:** Customer-side cancellation (with inventory restore + CANCELLED status) already exists; refunds are the admin-driven counterpart and are still outstanding.

#### 4. Product Reviews & Ratings — *partially done*
- [x] Customer can review products
- [x] Star rating (1-5)
- [x] Review text (create / edit / delete)
- [x] Display reviews on product page
- [ ] Restrict reviews to verified purchasers
- [ ] Review images
- [ ] Calculate & expose average product rating
- [ ] 👑 Admin moderation for reviews (hide/remove/approve)
- **Status:** Review CRUD endpoints exist (`USER`/`ADMIN`); no purchase check, no average aggregation, no admin moderation.

### Medium Priority

#### 5. Wishlist Feature — *partially done*
- [x] Add products to wishlist
- [x] Remove from wishlist
- [x] View wishlist
- [ ] Move wishlist item to cart
- [ ] Share wishlist (optional)
- **Status:** `WishlistAPI` supports add / get / delete. Move-to-cart and sharing outstanding.

#### 6. Product Search & Filtering — *partially done*
- [ ] Search by product name/description
- [x] Filter by category
- [x] Lookup by title
- [ ] Filter by price range
- [ ] Filter by rating
- [ ] Sort by price, popularity, newest
- **Tech:** MongoDB text search or Elasticsearch
- **Status:** Category and title endpoints exist; free-text search, price/rating filters and sorting outstanding.

#### 7. Order Invoice Generation
- [ ] Generate PDF invoice
- [ ] Include order details, items, totals
- [ ] Company branding/logo
- [ ] Download invoice from order page
- **Tech:** iText or Apache PDFBox
- **Estimate:** 4-5 hours

#### 8. Inventory Alerts 👑
- [ ] Low stock warning (< 5 units)
- [ ] Out of stock notification
- [ ] 👑 Admin email when stock low
- [ ] Prevent checkout if out of stock
- **Estimate:** 2-3 hours

#### 9. Multiple Shipping Addresses
- [ ] Save multiple addresses per user
- [ ] Set default shipping address
- [ ] Select address at checkout
- [ ] Address book management
- **Estimate:** 4-5 hours

### Low Priority

#### 10. Guest Checkout
- [ ] Checkout without account
- [ ] Collect email for order tracking
- [ ] Optional account creation after order
- [ ] Guest order tracking by email + order number
- **Estimate:** 5-6 hours

#### 11. Order Tracking Integration — *internal tracking done*
- [x] Internal tracking timeline (`/track` endpoint)
- [ ] Integrate with DHL/FedEx API
- [ ] Real-time tracking updates
- [ ] Display carrier tracking number
- [ ] Tracking link in order details
- **Status:** In-app order tracking exists; external carrier integration outstanding.

#### 12. Loyalty Points System
- [ ] Earn points on purchases
- [ ] Redeem points for discounts
- [ ] Points expiration
- [ ] Points history
- [ ] Different tiers (Bronze, Silver, Gold)
- **Estimate:** 8-10 hours

#### 13. Product Recommendations
- [ ] "Customers also bought" feature
- [ ] Personalized recommendations
- [ ] Recently viewed products
- [ ] Similar products
- **Tech:** Basic algorithm or ML integration
- **Estimate:** 10-12 hours

#### 14. Advanced Analytics Dashboard 👑
- [x] Order counts by status (`/order/admin/statistics`)
- [ ] 👑 Sales charts (daily, weekly, monthly)
- [ ] 👑 Revenue tracking
- [ ] 👑 Top selling products
- [ ] 👑 Customer analytics
- [ ] 👑 Conversion rate tracking
- **Tech:** Chart.js or similar
- **Status:** Basic status-count statistics exist; revenue, trends, top-sellers and customer analytics outstanding. Entirely admin-facing.

---

## Frontend-Expected Admin Endpoints (Backend Contract)

Endpoints the admin frontend branches expect. Check each box when the backend
route is implemented **and** verified against the frontend integration.
Paths below are the frontend's expected shape; where a backend equivalent already
exists under a different path, it's noted so we can align naming.

**Branching / PR workflow:** each numbered build item below is its own PR, opened
against the **`feat/admin`** feature branch. Once all are merged and the feature
branch is green, `feat/admin` is merged into `master`. Build in the listed order —
each item depends only on items above it (or on the flagged prerequisites).

### Already available (align frontend to these)
- [x] `GET /admin/orders` — exists as **`GET /order/admin/all`** (ADMIN-only, all users).
      ⚠️ No pagination yet; returns the full list. Also `GET /order/admin/status/{status}`.
- [x] Review create/edit/delete — exists as **`POST|PATCH|DELETE /products/{productId}/reviews/{userId}`**
      (owner-scoped: a user manages their own review).

### To build server-side — ordered build / PR plan

One PR per item, against `feat/admin`, in this order.

#### Tier 1 — standalone, low effort (unblocks frontend lists)

- [x] **PR 1 · `GET /admin/users`** — ✅ **done & merged** (`feat/admin-user-list`). Paginated,
      ADMIN-only `GET /api/v1/admin/users` via new `AdminUserAPI`/`AdminUserAPIImpl`,
      `UserListDTO` (no password/token/OTP), `GetAllUsersDTO` wrapper, `UserService.getAllUsers(...)`.
      Returns an empty page (not 400) when no users.
      *Depends on:* nothing.
- [x] **PR 2 · Paginate admin order list** — ✅ **done & merged** (`feat/PR-2/order-admin-all`).
      `GET /order/admin/all` now takes `pageNumber/pageSize/sortBy/sortOrder` and returns
      `OrderDTOAllApiResource` (`GetAllOrdersDTO` wrapper). Default sort `totalAmount` (a real field).
      ⚠️ Response shape changed: data is now `{ orders: [...], pageNo, ... }` — frontend reads `data.orders`.
      *Depends on:* nothing. Establishes the pagination pattern reused later.
- [x] **PR 3 · `POST /admin/users/:id/reset-password`** — ✅ **done** (`feat/PR-3/admin-reset-password`).
      ADMIN-only `POST /api/v1/admin/users/{id}/reset-password` on `AdminUserAPIImpl`;
      `UserService.adminResetPassword(userId)` generates a policy-compliant temp password and
      emails it via new `EmailService.sendPasswordResetEmail(...)` (+ `password-reset.html`).
      Rejects users still `AWAITING_CONFIRMATION`.
      **Hardened flow:** (1) **force-change** — sets `User.mustChangePassword`; login returns the flag
      and self-service `update-password` clears it; (2) **no lockout** — the email is sent *synchronously
      before* the password is persisted, so a mail failure aborts with 503 and leaves the old password intact;
      (3) **expiry** — `User.passwordResetAt` + `app.security.temp-password-ttl-hours` (default 24) causes
      login to reject an expired temp password.
      *Note:* distinct from the self-service `POST /auth/update-password` (which needs the current password).
      *Depends on:* nothing (reuses existing `EmailService`).

#### Tier 2 — small model/logic additions, reuse existing flows

- [x] **PR 4 · Review moderation** — ✅ **done** (`feat/PR-4/review-moderation`).
      Added `Rating.hidden` (+ `RatingDTO.hidden`); public `getProductReviews` now filters hidden
      reviews out. New ADMIN-only `AdminReviewAPI`/`AdminReviewAPIImpl` under
      `api/v1/admin/products/{productId}/reviews`: `GET` (all, incl. hidden),
      `PATCH /{userId}/hide`, `PATCH /{userId}/approve`, `DELETE /{userId}`.
      Service gained `getAllProductReviews` + `setReviewVisibility` (cache-evicting).
      *Depends on:* Rating model change.
      *Note:* the pre-existing owner-scoped `/products/{productId}/reviews/{userId}` routes are
      `USER||ADMIN` but don't verify the caller owns the review — a separate authz gap worth a follow-up.
- [ ] **PR 5 · `GET /admin/categories` (+ CRUD)** — managed category resource.
      Categories are currently a hardcoded `enum Category`, not a managed resource.
      **Decide first:** (a) reuse the lookup pattern (`AdminLookupAPI`:
      `POST|PUT|DELETE /admin/lookup`) — low effort; or (b) new `Category` collection + migrate
      `Product.category` to a reference — higher effort, touches product filtering.
      *Depends on:* option (b) only — product-model changes.

#### Tier 3 — touches order money/state logic

- [ ] **PR 6 · `PATCH /admin/orders/:id/pricing`** — admin price-adjustment on an order
      (only `PUT /order/{orderId}/status` exists today). Recalculate subtotal/total, record the
      adjustment, notify customer. Resolve the "price validation mismatch" tech-debt here.
      *Depends on:* order totals logic.
- [ ] **PR 7 · `DELETE /admin/orders/stale`** — bulk delete of stale orders
      (no order-delete of any kind exists today). Define "stale" (e.g. `PENDING` older than N days).
      *Prerequisite:* implement **soft-delete** first (hard `deleteMany` risks referential integrity).
      *Depends on:* soft-delete decision.

#### Tier 4 — aggregation-heavy / most integrated (do last)

- [ ] **PR 8 · `GET /admin/stats`** *(expand existing)* — dedicated aggregation endpoint.
      `GET /order/admin/statistics` returns status counts only; needs revenue, sales trends,
      top-sellers, and user/customer metrics via MongoDB aggregation.
      *Depends on:* orders (exist); more accurate after PR 6.
- [ ] **PR 9 · `POST /admin/orders/:id/rebook`** — rebook/clone an existing order:
      new checkout/order, re-validate inventory, re-price at current prices, new order number,
      `PENDING` status, confirmation email.
      *Depends on:* order-creation internals, inventory, pricing (PR 6), email flow. Build last.

**Cross-cutting prerequisites:** the pagination pattern (PR 2 — reused by PR 1 & PR 8) and the
soft-delete decision (gates PR 7, makes PR 9's cancel/restore safer).

---

## Code Improvements & Refactoring

### High Priority

#### 1. Exception Handling Standardization 
- [ ] Create `@RestControllerAdvice` global exception handler
- [ ] Standardize error response format
- [ ] Add specific exception types (ProductNotFoundException, etc.)
- [ ] Log exceptions properly
- [ ] Return appropriate HTTP status codes
- **Current:** Some methods have try-catch, some don't
- **Estimate:** 3-4 hours

#### 2. Input Validation Enhancement
- [ ] Add `@Valid` to all request bodies
- [ ] Custom validators for complex fields
- [ ] Validate ObjectId format
- [ ] Validate email format
- [ ] Validate phone number format
- [ ] Price range validation
- **Current:** Basic validation exists
- **Estimate:** 3-4 hours

#### 3. DTO Mapping Consistency
- [ ] Use ModelMapper or MapStruct consistently
- [ ] Create utility class for common mappings
- [ ] Avoid manual mapping in controllers
- [ ] Create bidirectional mapping (Entity ↔ DTO)
- **Current:** Mix of manual mapping and ObjectMapper
- **Estimate:** 4-5 hours

#### 4. Service Layer Transaction Management
- [ ] Add `@Transactional` where needed
- [ ] Rollback on exceptions
- [ ] Optimize transaction boundaries
- [ ] Avoid nested transactions
- **Current:** Some methods have it, some don't
- **Estimate:** 2-3 hours

#### 5. API Versioning
- [ ] Implement proper API versioning (/api/v1, /api/v2)
- [ ] Deprecation strategy for old versions
- [ ] Version-specific DTOs if needed
- **Current:** Using /api/v1 but no versioning strategy
- **Estimate:** 2-3 hours

### Medium Priority

#### 6. Logging Improvements
- [ ] Structured logging (JSON format)
- [ ] Add request/response logging
- [ ] Log correlation IDs
- [ ] Different log levels per environment
- [ ] Sensitive data masking
- **Current:** Basic logging with @Slf4j
- **Estimate:** 3-4 hours

#### 7. Caching Strategy
- [ ] Cache product catalog
- [ ] Cache user cart (Caffeine)
- [ ] Cache frequently accessed data
- [ ] Cache invalidation strategy
- **Tech:** Spring Cache + Caffeine
- **Estimate:** 5-6 hours

#### 8. Rate Limiting
- [ ] Limit API requests per user
- [ ] Prevent brute force attacks
- [ ] Different limits for different endpoints
- **Tech:** Bucket4j or similar
- **Estimate:** 4-5 hours

#### 9. Database Indexing Optimization 
- [ ] Index frequently queried fields
- [ ] Compound indexes for common queries
- [ ] Monitor slow queries
- [ ] Query performance optimization
- **Current:** Basic indexes exist
- **Estimate:** 3-4 hours

#### 10. Pagination for List Endpoints
- [ ] Add pagination to `getAllOrders()`
- [ ] Add pagination to `getUserOrders()`
- [ ] Add pagination to product listing
- [ ] Configurable page size
- [ ] Sort options
- **Current:** Returns all results
- **Estimate:** 3-4 hours

### Low Priority

#### 11. API Documentation (Swagger/OpenAPI)
- [ ] Add Swagger UI
- [ ] Document all endpoints
- [ ] Add request/response examples
- [ ] Add authentication details
- **Tech:** Springdoc OpenAPI
- **Estimate:** 4-5 hours

#### 12. Security Enhancements
- [ ] JWT token refresh mechanism
- [ ] Role-based access control (RBAC)
- [ ] API key for third-party integrations
- [ ] CSRF protection
- [ ] CORS configuration
- **Current:** Basic authentication
- **Estimate:** 6-8 hours

#### 13. Async Processing
- [ ] Async email sending
- [ ] Async inventory updates
- [ ] Background job processing
- [ ] Queue for long-running tasks
- **Tech:** Spring @Async or message queue
- **Estimate:** 5-6 hours

#### 14. Health Checks & Monitoring
- [ ] Spring Actuator endpoints
- [ ] Database health check
- [ ] External service health checks
- [ ] Prometheus metrics
- [ ] Grafana dashboards
- **Estimate:** 4-5 hours

#### 15. Code Coverage Improvement
- [ ] Achieve 80%+ test coverage
- [ ] Integration tests for critical flows
- [ ] End-to-end tests
- [ ] Performance tests
- **Current:** No tests yet
- **Estimate:** 15-20 hours

---

## Known Issues / Tech Debt

### Critical

#### 1. Price Validation Mismatch Issue 
- **Issue:** Checkout subtotal can mismatch calculated subtotal if prices change
- **Current Solution:** Recalculate totals before payment
- **Better Solution:** Lock prices when checkout created, or show price change warning
- **Priority:** High
- **Estimate:** 2-3 hours

#### 2. Inventory Race Condition
- **Issue:** Multiple users can checkout same item simultaneously
- **Current:** No inventory reservation
- **Solution:** Implement inventory reservation or pessimistic locking
- **Priority:** High
- **Estimate:** 4-5 hours

### Medium

#### 3. No Soft Delete 
- **Issue:** Hard deletes can break referential integrity
- **Solution:** Implement soft delete pattern
- **Affected:** Products, Orders, Users
- **Priority:** Medium
- **Estimate:** 3-4 hours

#### 4. Order Number Collision (Low probability)
- **Issue:** Random generation might create duplicates
- **Current:** 6-char alphanumeric = 36^6 possibilities
- **Solution:** Add uniqueness check or use UUID
- **Priority:** Low
- **Estimate:** 1-2 hours

---

## Infrastructure & Deployment

### High Priority

#### 1. Docker Containerization 
- [ ] Create Dockerfile
- [ ] Create docker-compose.yml
- [ ] Multi-stage build for optimization
- [ ] Environment-specific configs
- **Estimate:** 2-3 hours

#### 2. CI/CD Pipeline 
- [ ] GitHub Actions or GitLab CI
- [ ] Automated tests on PR
- [ ] Automated build
- [ ] Automated deployment
- **Estimate:** 4-5 hours

#### 3. Environment Configuration 
- [ ] Dev, Staging, Production configs
- [ ] Environment variables
- [ ] Secrets management
- [ ] Database migration strategy
- **Estimate:** 3-4 hours

### Medium Priority

#### 4. Kubernetes Deployment 
- [ ] Create K8s manifests
- [ ] Deployment, Service, ConfigMap
- [ ] Horizontal Pod Autoscaling
- [ ] Health probes
- **Estimate:** 6-8 hours

#### 5. Monitoring & Logging Infrastructure 
- [ ] ELK Stack or similar
- [ ] Centralized logging
- [ ] Application monitoring
- [ ] Alert configuration
- **Estimate:** 8-10 hours

---

## Roadmap Suggestion

### Phase 1: Foundation (Completed)
- Core e-commerce flow
- Cart, Checkout, Orders
- Simulated payment
- Admin dashboard

### Phase 2: Essential Features (2-3 weeks)
- Email notifications
- Exception handling
- Input validation
- Coupon system
- Order cancellation

### Phase 3: Enhanced UX (2-3 weeks)
- Product reviews
- Wishlist
- Search & filtering
- Invoice generation
- Multiple addresses

### Phase 4: Production Ready (1-2 weeks)
- Unit tests
- Docker & CI/CD
- Caching
- Rate limiting
- API documentation

### Phase 5: Advanced Features (3-4 weeks)
- Real payment integration (Stripe/PayPal)
- Order tracking
- Loyalty points
- Advanced analytics
- Recommendations

---

## Metrics to Track

- [ ] Test coverage percentage
- [ ] API response times
- [ ] Error rates
- [ ] Conversion rate (cart → order)
- [ ] Payment success rate
- [ ] User retention
- [ ] Average order value

---

## Ideas for Later

- Mobile app (React Native/Flutter)
- PWA (Progressive Web App)
- Multi-language support
- Multi-currency support
- Social media login
- Live chat support
- Product bundles/packages
- Subscription products
- Gift cards
- Affiliate program

---

## Notes

- Prioritize based on business needs
- Re-evaluate priorities quarterly
- Track time spent vs estimated
- Update this file regularly
- Celebrate completed items!
