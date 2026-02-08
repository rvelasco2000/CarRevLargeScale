## This is Large Scale project

### Sales recompute
See `README-sales-recompute.md`.

### Analytics API

Get top viewed and top reviewed car between two dates (admin):

```bash
curl -X POST "http://localhost:8080/api/admin/analytics/carTopBetweenDates" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "startDate": "2026-02-08",
    "endDate": "2026-02-09"
  }'
```

Get auth token (login):

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "YOUR_ADMIN_PASSWORD"
  }'
```
