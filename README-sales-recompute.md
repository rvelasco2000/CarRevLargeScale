# ProductYear Recompute (Sales Aggregation)

This feature rebuilds each car's `productYear` array by aggregating the `sales` collection based on the sale IDs stored in `cars.sales[]`.

## How it works
- External process inserts a `Sale` document in MongoDB.
- External process pushes that `sale_id` into the target car's `sales[]` array.
- Spring Boot recomputes `productYear` by loading all sale IDs per car and calculating:
  - `Average_used_milage`
  - `Average_used_price`
  - `Sales_count`

## Manual API (admin only)
Endpoint:
```
POST /api/admin/cars/recompute-product-year
```

Steps:
1) Register admin user (once):
```
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@test.com","password":"admin"}'
```

2) Promote user to admin in Mongo:
```
use <your_db_name>
db.users.updateOne({ username: "admin" }, { $set: { isAdmin: true } })
```

3) Login to get token:
```
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

4) Call recompute:
```
curl -X POST http://localhost:8080/api/admin/cars/recompute-product-year \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Response:
```
{"updatedCars": 3}
```

## Scheduled run
The scheduler reads from:
```
car.sales.recompute-cron
```

Default (weekly, Monday at 00:00):
```
car.sales.recompute-cron=0 0 0 * * MON
```

Example (every 2 minutes):
```
car.sales.recompute-cron=0 */2 * * * *
```

Restart the application after changing this value.

## Logs
Each scheduled run logs:
```
Scheduled productYear recompute completed; updatedCars=...
```
