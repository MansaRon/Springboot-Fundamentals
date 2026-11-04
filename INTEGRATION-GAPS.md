# Backend Gaps — Frontend Integration

Tracking doc for FE↔BE contract gaps surfaced during integration sprints. Each
open item needs a backend fix (or an explicit "frontend-only" resolution)
before the full end-to-end flow can be verified.

Legend: `[ ]` open · `[x]` done · `n/a` no backend change required.

---

## Cart Integration

### Open

- [ ] **Gap 4 — Guest cart + merge-on-login endpoint**
  - **What:** Cart state is keyed by `userId`, so logged-out visitors can't
    have a server cart, and there's no way to carry a guest's local items into
    their API cart on login. Guest browsing with a cart and a merge-on-login
    flow are both unimplemented.
  - **Fix:** Add `POST /api/v1/cart/{userId}/merge` accepting a list of
    `{ productId, quantity }`. For each entry, reuse add-to-cart logic (sum
    quantities if the product is already present; otherwise add a new line),
    apply stock validation + `updateTotal()`, and return the merged `CartDTO`
    in one atomic call. Conflict rule to confirm: default is "sum quantities".
  - **Depends on:** guest-cart storage decision on the frontend (localStorage)
    or a server-side guest-session cookie (see auth investigation — deferred).
  - **Status:** deferred pending decision; not yet implemented.

- [ ] **CartDTO tax field**
  - **What:** `CartDTO` exposes `totalPrice` but not a cart-level tax, so the UI
    can't read a backend-computed VAT figure for the whole cart (per-item `tax`
    is now populated at 15%, but there's no aggregate). See inline TODO in
    `CartDTO.java`.
  - **Fix:** Add a `tax` (and optionally `subtotal`) field to `CartDTO`,
    populated from the sum of line-item VAT / `ValueUtil.vat(subtotal)` in
    `CartMapper.toDTO`, so the DTO mirrors what the UI displays.

### Resolved / no action (cart integration sprint)

- [x] **Gap 1 — clearCart endpoint** — added `DELETE /api/v1/cart/{userId}/clear`
  + `clearCart(ObjectId)` service overload (restores stock, evicts cache).
- [x] **Gap 2 — CartDTO.totalPrice** — added field + mapped `cart.getTotalPrice()`.
- [x] **Gap 3 — per-item tax / VAT alignment** — single `ValueUtil.VAT_RATE = 0.15`
  source of truth; cart line items + checkout now both compute 15% VAT.
- n/a **Gap 5 — decrease-to-0 uses PATCH not DELETE** — backend already removes
  the item when `newQuantity <= 0`; frontend-only semantics preference.
- n/a **Gap 6 — confirmation uses local product catalog** — order-detail
  endpoints already exist (`getOrderByOrderNumber`, `getOrderByIdAndCustomerId`,
  `getOrderById`); frontend just needs to consume them.

> Cart gaps 1–3 are implemented on the integration branch (not yet merged).

---

## Checkout Integration

These gaps were identified during the checkout integration sprint. All six are
resolved on the integration branch (not yet merged); see the migration note below.

- [x] **Gap 1 — `AddressDTO` missing contact fields**
  - **What:** `AddressDTO` only had `streetAddress`, `city`, `state`, `country`,
    `postalCode`. The frontend shipping form also captures `fullName`,
    `phoneNumber`, `email`, and `apartment`. These were silently dropped during
    mapping.
  - **Done:** Added `fullName`, `phoneNumber`, `email`, `apartment` to both
    `AddressDTO` *and* the `Address` model (the model lacked them too), and mapped
    them both ways in `CheckoutMapper`. New fields are left unvalidated (no
    `@NotBlank`) so existing callers keep working — tighten once the FE always sends them.

- [x] **Gap 2 — `DeliverMethod` enum doesn't match frontend options**
  - **What:** Frontend had `standard`, `express`, `overnight`; backend enum was
    `DHL`, `FedEx`, `Express`, `FREE`, with pricing hidden in `OrderServiceImpl`.
  - **Done:** Reworked `DeliverMethod` to `STANDARD`, `EXPRESS`, `OVERNIGHT` (+ `FREE`
    as the internal default), each carrying `label`, `price` (R0 / R50 / R150) and
    `estimatedDays` — the enum is now the single source of truth for pricing. Added
    `GET /api/v1/checkout/delivery-options` (`CheckoutService.getDeliveryOptions`,
    excludes `FREE`) so the FE renders real values.

- [x] **Gap 3 — `PaymentMethod` enum missing EFT**
  - **What:** `PaymentMethod` had `CREDIT_CARD`, `CASH_ON_DELIVERY`, `NOT_SELECTED`;
    selecting EFT on the FE mapped to `NOT_SELECTED`.
  - **Done:** Added `EFT` to `PaymentMethod`.

- [x] **Gap 4 — No separate billing address step in frontend**
  - **What:** `CheckoutDTO.billingAddress` was `@NotNull` but the FE has no billing form.
  - **Done:** Made `billingAddress` optional (dropped `@NotNull`); `updateCheckout`
    defaults it to the shipping address when none is supplied.

- [x] **Gap 5 — `EFT` payment method has no backend support for payment flow**
  - **What:** EFT needs a bank-transfer reference flow the backend didn't expose.
  - **Done:** EFT is handled manually — `PaymentServiceImpl` returns
    `success=true, status=PENDING` with a generated transfer reference + bank-details
    message; the order is placed `PENDING` (awaiting payment). Automated
    reconciliation (webhook) is deliberately **out of scope** — see follow-up below.

- [x] **Gap 6 — `shippingCost` not reflected in checkout total**
  - **What:** `CheckoutDTO.totalAmount` didn't include delivery price at the
    checkout stage; FE delivery prices weren't in the server total.
  - **Done:** Added `shippingCost` to `Checkout` + `CheckoutDTO`;
    `recalculateTotals` now computes `(subtotal − discount) + VAT + shippingCost`;
    `OrderServiceImpl` reuses `checkout.getShippingCost()` (no longer re-adds it —
    avoids double-counting); and `CheckoutValidationServiceImpl.validatePricing`
    was fixed to include `shippingCost` in its expected-total check.

### Migration note (Gap 2 — persisted enum rename)

`DeliverMethod` values `DHL` / `FedEx` / `Express` were removed. Any existing
MongoDB `checkout` / `order` documents storing those strings in `shippingMethod`
will **fail to deserialize** against the new enum. Before this reaches an
environment with live data, run a data migration (e.g. `DHL → OVERNIGHT`,
`Express → EXPRESS`, `FedEx → STANDARD`, legacy `FREE → STANDARD` or keep `FREE`)
or add a tolerant deserializer. No migration needed for fresh/empty databases.

### Open follow-ups (not in this sprint)

- [ ] **EFT reconciliation** — a webhook/endpoint to confirm a received bank
  transfer and move the order from `PENDING` to `CONFIRMED`. Currently manual.
- [ ] **VAT on shipping** — tax is charged on subtotal only; shipping is added
  post-tax. Revisit if shipping should be VAT-inclusive.

---

## Order Integration

Gaps found while wiring up the order views on the frontend. All resolved on the
integration branch (not yet merged).

- [x] **Gap 1 — `OrderMapper.toAddressDTO` dropped contact fields**
  - **Done:** Maps `fullName`, `phoneNumber`, `email`, `apartment` (the `Address`
    model already carried them from the checkout work).

- [x] **Gap 2 — `OrderMapper.mapToOrderItemsDTO` returned 0s + duplicate `setId`**
  - **Done:** Maps `unitPrice`, `discount`, `tax` (previously never set, always 0)
    and removed the duplicate `dto.setId(...)` call.

- [x] **Gap 3 — `GET /order/{orderId}` had no ownership enforcement**
  - **Done:** Split by audience instead of branching in the controller —
    `GET /order/{orderId}` is now **ADMIN-only** (`@PreAuthorize("hasRole('ADMIN')")`,
    plain one-liner body), and a regular USER fetches a single order via the existing
    `GET /order/{customerId}/{orderId}`, where ownership is enforced **declaratively**
    (`#customerId == principal.id`). Each endpoint's audience is commented in
    `OrderAPIImpl`. **Also fixed a related bug:** the service called
    `findByIdAndCustomerInfoId(customerId, orderId)` with arguments swapped vs the
    repo's `(orderId, customerId)`, so the ownership-scoped query (and the
    `/{customerId}/{orderId}` endpoint) never matched. Corrected the order.
  - **FE impact:** fetch a single order by id via `/order/{customerId}/{orderId}`,
    not `/order/{orderId}` (which is now admin-only).

- [x] **Gap 4 — `getUserOrders` ran the query twice**
  - **Done:** Fetches the list once and reuses it for both `data` and the count in
    the response message (was calling `getUserOrders(userId)` again just for `.size()`).

---

## Access Control (cross-cutting)

Policy going forward: **one audience per endpoint** — admin endpoints are
`hasRole('ADMIN')` only; customer endpoints are owner-scoped
(`#userId == authentication.principal.id`) only. A wrong-audience call returns
403 via `JwtAccessDeniedHandler`. This is already applied to the **Order** module.

- [ ] **Tighten Cart & Checkout authorization**
  - **What:** `CartAPIImpl` and `CheckoutAPIImpl` endpoints currently use
    `@PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.principal.id.toString()")`.
    The `hasRole('ADMIN') or` part was a **testing convenience** (so any logged-in
    user on hand could exercise the flow), **not** a deliberate authorization design.
  - **Fix:** Apply the same strict separation used in Order — customer-facing
    cart/checkout endpoints become owner-only (`#userId == principal.id`), and any
    genuinely admin operation moves to an explicit `/admin/...` endpoint.

- [ ] **Deliberate admin override / intervention capability**
  - **What:** Separate from the testing escape hatch above, admins legitimately need
    to act on a specific customer's cart/checkout/order when that customer is **stuck
    mid-flow** — e.g. a wedged checkout that won't confirm or cancel, an item that
    can't be removed, a cart that won't clear, or a payment stuck in `PENDING`.
  - **Fix:** Provide this through **explicit, purpose-built admin endpoints** that
    take the target `userId`/`orderId` (e.g. under `/admin/...`), rather than the
    implicit `hasRole('ADMIN') or owner` shortcut. That keeps the override
    intentional, clearly separated from customer self-service, and auditable
    (admin acting on behalf of a user should be logged). Examples to consider:
    admin force-cancel/reset a checkout, admin clear/adjust a user's cart,
    admin list a specific customer's orders (`GET /order/admin/user/{userId}`).
