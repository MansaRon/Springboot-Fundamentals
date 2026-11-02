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
