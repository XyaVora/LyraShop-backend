# LyraShop database snapshot

This directory is the review/import snapshot derived from the backend's
Flyway migrations V1-V34. Flyway migrations remain the source of truth for the
running application.

## Files

- `01_schema.sql`: drops and recreates `lyrashop_db`, then applies the tables,
  indexes, foreign keys, checks, and all structural changes. It contains no
  seed records.
- `02_seed_data.sql`: seed/demo records only, plus the data normalization that
  is required when those older records are loaded directly into the final
  schema.
- `lyrashop_db.sql`: the two files above combined for a one-file import into an
  empty MySQL 8.0+ inspection database.

Run `01_schema.sql` before `02_seed_data.sql`. Running `01_schema.sql` again
deletes the imported data and any Flyway history. These files are for a
disposable inspection database; do not run them against the local Docker
database managed by Flyway.

Do not baseline a schema-only import. Load `02_seed_data.sql` first, then use
baseline version 34 only when the backend intentionally points at this
Workbench database. Otherwise the V1-V34 data migrations are skipped and the
catalog remains empty.

Regenerate all snapshots after adding a migration:

```powershell
.\scripts\generate-db-snapshot.ps1
```

## Current inventory

The final schema contains 35 application tables:

| Area | Tables |
| --- | --- |
| Identity and security | `users`, `refresh_sessions`, `password_reset_tokens`, `email_verification_tokens` |
| Catalog | `categories`, `products`, `product_variants`, `product_images`, `reviews` |
| Cart and wishlist | `carts`, `cart_items`, `wishlist_items`, `wishlist_shares` |
| Orders and fulfillment | `orders`, `order_items`, `tracking_events`, `customer_return_requests`, `customer_return_items`, `customer_return_evidence`, `return_evidence_uploads`, `order_refunds` |
| Promotions | `promotions`, `promotion_products`, `vouchers`, `voucher_redemptions` |
| Customer profile | `shipping_addresses`, `payment_methods`, `search_history`, `newsletter_subscriptions`, `loyalty_accounts`, `loyalty_transactions` |
| Operations and content | `email_outbox`, `brand_settings`, `inventory_adjustments`, `admin_audit_logs` |

The supplied seed data creates 476 records:

| Table | Records |
| --- | ---: |
| `users` | 4 |
| `categories` | 4 |
| `products` | 56 |
| `product_variants` | 305 |
| `product_images` | 56 |
| `reviews` | 21 |
| `orders` | 3 |
| `order_items` | 4 |
| `carts` | 1 |
| `cart_items` | 1 |
| `promotions` | 1 |
| `promotion_products` | 16 |
| `vouchers` | 3 |
| `brand_settings` | 1 |

Tables not listed in the record summary intentionally start empty and are
filled by normal application activity.

## Compatibility fixes in the split data file

- Seeded `order_items` now include the mandatory `product_id` introduced by
  V14.
- Seeded orders receive the correct `subtotal_amount`, matching the V13
  backfill.
- Pending seeded orders receive `expires_at`, matching the V22 backfill.
- All V9-V34 tables, columns, constraints, indexes, and required settings
  missing from the old V1-V8 snapshot are included.
