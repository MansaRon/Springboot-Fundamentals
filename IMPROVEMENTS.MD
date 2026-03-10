# TODO: Features & Improvements

## Current Status: Core E-Commerce Complete

---

## Features to Add

### High Priority

#### 1. Email Notifications
- [ ] Order confirmation email
- [ ] Order status update emails (shipped, delivered)
- [ ] Payment failure notification
- [ ] Cart abandonment reminder
- [ ] Welcome email for new users
- **Tech:** Spring Mail, Thymeleaf templates
- **Estimate:** 4-6 hours

#### 2. Coupon/Promo Code System
- [ ] Create Coupon entity (code, discount %, expiry date)
- [ ] Apply coupon at checkout
- [ ] Validate coupon (active, not expired, usage limit)
- [ ] Track coupon usage per user
- [ ] Admin: Create/manage coupons
- **Estimate:** 6-8 hours

#### 3. Order Cancellation (Customer)
- [ ] Allow customer to cancel pending orders
- [ ] Restore inventory on cancellation
- [ ] Add cancellation reason
- [ ] Update order status to CANCELLED
- [ ] Prevent cancellation after shipping
- **Estimate:** 3-4 hours

#### 4. Refund Processing
- [ ] Admin initiates refund
- [ ] Partial vs full refund
- [ ] Restore inventory if applicable
- [ ] Track refund status
- [ ] Refund history in order details
- **Estimate:** 4-5 hours

#### 5. Product Reviews & Ratings
- [ ] Customer can review purchased products
- [ ] Star rating (1-5)
- [ ] Review text and images
- [ ] Calculate average product rating
- [ ] Display reviews on product page
- [ ] Admin moderation for reviews
- **Estimate:** 6-8 hours

### Medium Priority

#### 6. Wishlist Feature 
- [ ] Add products to wishlist
- [ ] Remove from wishlist
- [ ] View wishlist
- [ ] Move wishlist item to cart
- [ ] Share wishlist (optional)
- **Estimate:** 4-5 hours

#### 7. Product Search & Filtering
- [ ] Search by product name/description
- [ ] Filter by category
- [ ] Filter by price range
- [ ] Filter by rating
- [ ] Sort by price, popularity, newest
- **Tech:** MongoDB text search or Elasticsearch
- **Estimate:** 5-6 hours

#### 8. Order Invoice Generation
- [ ] Generate PDF invoice
- [ ] Include order details, items, totals
- [ ] Company branding/logo
- [ ] Download invoice from order page
- **Tech:** iText or Apache PDFBox
- **Estimate:** 4-5 hours

#### 9. Inventory Alerts
- [ ] Low stock warning (< 5 units)
- [ ] Out of stock notification
- [ ] Admin email when stock low
- [ ] Prevent checkout if out of stock
- **Estimate:** 2-3 hours

#### 10. Multiple Shipping Addresses
- [ ] Save multiple addresses per user
- [ ] Set default shipping address
- [ ] Select address at checkout
- [ ] Address book management
- **Estimate:** 4-5 hours

### Low Priority

#### 11. Guest Checkout
- [ ] Checkout without account
- [ ] Collect email for order tracking
- [ ] Optional account creation after order
- [ ] Guest order tracking by email + order number
- **Estimate:** 5-6 hours

#### 12. Order Tracking Integration
- [ ] Integrate with DHL/FedEx API
- [ ] Real-time tracking updates
- [ ] Display tracking number
- [ ] Tracking link in order details
- **Estimate:** 8-10 hours

#### 13. Loyalty Points System
- [ ] Earn points on purchases
- [ ] Redeem points for discounts
- [ ] Points expiration
- [ ] Points history
- [ ] Different tiers (Bronze, Silver, Gold)
- **Estimate:** 8-10 hours

#### 14. Product Recommendations
- [ ] "Customers also bought" feature
- [ ] Personalized recommendations
- [ ] Recently viewed products
- [ ] Similar products
- **Tech:** Basic algorithm or ML integration
- **Estimate:** 10-12 hours

#### 15. Advanced Analytics Dashboard
- [ ] Sales charts (daily, weekly, monthly)
- [ ] Revenue tracking
- [ ] Top selling products
- [ ] Customer analytics
- [ ] Conversion rate tracking
- **Tech:** Chart.js or similar
- **Estimate:** 8-10 hours

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
- [ ] Cache user cart (Redis)
- [ ] Cache frequently accessed data
- [ ] Cache invalidation strategy
- **Tech:** Spring Cache + Redis
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
