# Car Search API

## Endpoint
- `GET /api/cars`

## Query params (all optional)
- `carName`
- `carBrand`
- `carModel`
- `bodyType`
- `engineDisplacement`
- `numberOfCylinders`
- `page` (1-based, default 1)
- `size` (default 20)

## Sorting
- Always by `views` descending.

## Response
- `items`: list of cars (summary fields)
- `page`, `size`, `totalElements`, `totalPages`

## Test Car Search API

Example request:
```
curl -X GET "http://localhost:8080/api/cars?carBrand=Toyota&bodyType=SUV&page=1&size=10" \
  -H "Accept: application/json"
```

Example response (shape):
```
{
  "items": [
    {
      "id": "64f1a0...",
      "carName": "RAV4",
      "carBrand": "Toyota",
      "carModel": "RAV4",
      "bodyType": "SUV",
      "engineDisplacement": 2000,
      "numberOfCylinders": 4,
      "views": 1203
    }
  ],
  "page": 1,
  "size": 10,
  "totalElements": 245,
  "totalPages": 25
}
```
