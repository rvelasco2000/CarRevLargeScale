first of all you have to setup neo 4j 
1.insert the file cars_flat.csv in the import folder
insert this query:"//
//
MATCH (n) DETACH DELETE n;
//
// ---- Car indexes (help matching & guard-rails)
CREATE INDEX car_brand_idx IF NOT EXISTS FOR (c:Car) ON (c.car_brand);
CREATE INDEX car_model_idx IF NOT EXISTS FOR (c:Car) ON (c.car_model);
CREATE INDEX car_price_idx IF NOT EXISTS FOR (c:Car) ON (c.price_new);
CREATE INDEX car_disp_idx  IF NOT EXISTS FOR (c:Car) ON (c.engine_displacement);

// ---- Dimension tables uniqueness
CREATE CONSTRAINT brand_unique IF NOT EXISTS
FOR (b:Brand) REQUIRE b.name IS UNIQUE;

CREATE CONSTRAINT model_unique IF NOT EXISTS
FOR (m:Model) REQUIRE (m.name, m.brand) IS UNIQUE;

CREATE CONSTRAINT body_unique IF NOT EXISTS
FOR (b:BodyType) REQUIRE b.name IS UNIQUE;

CREATE CONSTRAINT fuel_unique IF NOT EXISTS
FOR (f:FuelType) REQUIRE f.name IS UNIQUE;

CREATE CONSTRAINT trans_unique IF NOT EXISTS
FOR (t:Transmission) REQUIRE t.name IS UNIQUE;

CREATE CONSTRAINT drive_unique IF NOT EXISTS
FOR (d:DriveWheels) REQUIRE d.name IS UNIQUE;

CREATE CONSTRAINT year_unique IF NOT EXISTS
FOR (y:Year) REQUIRE y.value IS UNIQUE;

// ---- Bin uniqueness
CREATE CONSTRAINT pricebin_unique IF NOT EXISTS
FOR (b:PriceBin) REQUIRE (b.min, b.max) IS UNIQUE;

CREATE CONSTRAINT dispbin_unique IF NOT EXISTS
FOR (b:DispBin) REQUIRE (b.min, b.max) IS UNIQUE;
//
LOAD CSV WITH HEADERS FROM "file:///cars_flat.csv" AS row
CALL {
  WITH row
  WITH
    toLower(trim(row.car_brand)) AS brand,
    toLower(trim(row.car_model)) AS model,
    toLower(trim(row.body_type)) AS body,
    toLower(trim(row.drive_wheels)) AS drive,
    toLower(trim(row.transmission_type)) AS trans,
    toLower(trim(row.fuel_type)) AS fuel,
    trim(row.car_name) AS car_name,

    CASE WHEN row.production_year = "" THEN NULL ELSE toInteger(row.production_year) END AS year,
    CASE WHEN row.engine_displacement = "" THEN NULL ELSE toFloat(row.engine_displacement) END AS disp,
    CASE WHEN row.number_of_cylinders = "" THEN NULL ELSE toInteger(row.number_of_cylinders) END AS cyl,
    CASE WHEN row.horse_power = "" THEN NULL ELSE toInteger(row.horse_power) END AS hp,
    CASE WHEN row.seat_capacity = "" THEN NULL ELSE toInteger(row.seat_capacity) END AS seats,
    CASE WHEN row.price_new = "" THEN NULL ELSE toFloat(row.price_new) END AS price

  MERGE (c:Car {
    car_name: car_name,
    car_brand: brand,
    car_model: model,
    production_year: year,
    engine_displacement: disp,
    fuel_type: fuel,
    transmission_type: trans
  })
  SET
    c.body_type = body,
    c.drive_wheels = drive,
    c.number_of_cylinders = cyl,
    c.horse_power = hp,
    c.seat_capacity = seats,
    c.price_new = price
} IN TRANSACTIONS OF 1000 ROWS;
//
MATCH (c:Car)
CALL {
  WITH c
  WITH c,
    toLower(trim(c.car_brand)) AS brand,
    toLower(trim(c.car_model)) AS model,
    toLower(trim(c.body_type)) AS body,
    toLower(trim(c.drive_wheels)) AS drive,
    toLower(trim(c.transmission_type)) AS trans,
    toLower(trim(c.fuel_type)) AS fuel,
    c.production_year AS year

  // Brand
  FOREACH (_ IN CASE WHEN brand IS NULL OR brand = "" THEN [] ELSE [1] END |
    MERGE (b:Brand {name: brand})
    MERGE (c)-[:OF_BRAND]->(b)
  )

  // Model
  FOREACH (_ IN CASE WHEN model IS NULL OR model = "" OR brand IS NULL OR brand = "" THEN [] ELSE [1] END |
    MERGE (m:Model {name: model, brand: brand})
    MERGE (c)-[:OF_MODEL]->(m)
  )

  // Body
  FOREACH (_ IN CASE WHEN body IS NULL OR body = "" THEN [] ELSE [1] END |
    MERGE (bt:BodyType {name: body})
    MERGE (c)-[:HAS_BODY_TYPE]->(bt)
  )

  // Drive
  FOREACH (_ IN CASE WHEN drive IS NULL OR drive = "" THEN [] ELSE [1] END |
    MERGE (dw:DriveWheels {name: drive})
    MERGE (c)-[:HAS_DRIVE]->(dw)
  )

  // Transmission
  FOREACH (_ IN CASE WHEN trans IS NULL OR trans = "" THEN [] ELSE [1] END |
    MERGE (tr:Transmission {name: trans})
    MERGE (c)-[:HAS_TRANSMISSION]->(tr)
  )

  // Fuel
  FOREACH (_ IN CASE WHEN fuel IS NULL OR fuel = "" THEN [] ELSE [1] END |
    MERGE (ft:FuelType {name: fuel})
    MERGE (c)-[:USES_FUEL]->(ft)
  )

  // Year
  FOREACH (_ IN CASE WHEN year IS NULL THEN [] ELSE [1] END |
    MERGE (y:Year {value: year})
    MERGE (c)-[:OF_YEAR]->(y)
  )
} IN TRANSACTIONS OF 2000 ROWS;
//
UNWIND [
  {min: 0,      max: 10000,    name:"<10k"},
  {min: 10000,  max: 20000,    name:"10–20k"},
  {min: 20000,  max: 30000,    name:"20–30k"},
  {min: 30000,  max: 45000,    name:"30–45k"},
  {min: 45000,  max: 60000,    name:"45–60k"},
  {min: 60000,  max: 80000,    name:"60–80k"},
  {min: 80000,  max: 120000,   name:"80–120k"},
  {min: 120000, max: 1e18,     name:">=120k"}
] AS p
MERGE (b:PriceBin {min: p.min, max: p.max})
SET b.name = p.name;
//
WITH range(1, 12) AS i
UNWIND i AS k
WITH (k-1)*0.5 + 0.5 AS minL, k*0.5 + 0.5 AS maxL
MERGE (b:DispBin {min: minL, max: maxL})
SET b.name = toString(minL) + "–" + toString(maxL) + "L";
//
// delete old links in batch
MATCH (c:Car)-[old:HAS_PRICE_BIN]->(:PriceBin)
CALL {
  WITH old
  DELETE old
} IN TRANSACTIONS OF 5000 ROWS;

// rebuild links in batch
MATCH (c:Car)
WHERE c.price_new IS NOT NULL
CALL {
  WITH c
  MATCH (b:PriceBin)
  WHERE c.price_new >= b.min AND c.price_new < b.max
  MERGE (c)-[:HAS_PRICE_BIN]->(b)
} IN TRANSACTIONS OF 2000 ROWS;
//
// delete old links in batch
MATCH (c:Car)-[old:HAS_DISPLACEMENT_BIN]->(:DispBin)
CALL {
  WITH old
  DELETE old
} IN TRANSACTIONS OF 5000 ROWS;

// rebuild links in batch
MATCH (c:Car)
WHERE c.engine_displacement IS NOT NULL
CALL {
  WITH c
  MATCH (b:DispBin)
  WHERE c.engine_displacement >= b.min AND c.engine_displacement < b.max
  MERGE (c)-[:HAS_DISPLACEMENT_BIN]->(b)
} IN TRANSACTIONS OF 2000 ROWS;
//"



3.for testing the algoritm insert this parameters(obviusly you can replace/add/remove the car),is stable with <= 5 car:"
//
:param clicks => [
{
car_brand: "jeep",
    car_model: "compass",
    car_name: "jeep compass",
    production_year: 2011,
    body_type: "suv",
    drive_wheels: "fwd",
    engine_displacement: 2.4,
    number_of_cylinders: 4,
    transmission_type: "automatic",
    horse_power: 180,
    fuel_type: "petrol",
    seat_capacity: 5,
    price_new: 40128
  },{
car_brand: "bmw",
    car_model: "x5",
    car_name: "bmw x5",
    production_year: 2011,
    body_type: "suv",
    drive_wheels: "awd",
    engine_displacement: 3.0,
    number_of_cylinders: 6,
    transmission_type: "automatic",
    horse_power: 225,
    fuel_type: "diesel",
    seat_capacity: 5,
    price_new: 42864
}
];

:param pricePct => 0.20;
:param dispDelta => 0.4;
:param k => 20;

//

"
4.This is the main query :"
UNWIND $clicks AS click

 MATCH ( u : User { username : $username }) -[: HAS_VISITED ] - >( click : Car )
WITH DISTINCT click
MATCH ( seed : Car { mongo_id : click . mongo_id })
MATCH ( seed ) -[: HAS_BODY_TYPE |: HAS_DRIVE |: HAS_TRANSMISSION |
: USES_FUEL |: OF_BRAND |: OF_MODEL |
: HAS_PRICE_BIN |: HAS_DISPLACEMENT_BIN ] - >( feat )
MATCH ( cand : Car ) -[: HAS_BODY_TYPE |: HAS_DRIVE |: HAS_TRANSMISSION |
: USES_FUEL |: OF_BRAND |: OF_MODEL |
: HAS_PRICE_BIN |: HAS_DISPLACEMENT_BIN ] - >( feat )
WHERE cand <> seed
AND toLower ( coalesce ( cand . car_name , ’ ’) )
<> toLower ( coalesce ( seed . car_name , ’ ’) )
AND ( click . price_new IS NULL OR
( cand . price_new IS NOT NULL AND
cand . price_new >= click . price_new * (1 - $pricePct ) AND
cand . price_new <= click . price_new * (1 + $pricePct ) ) )
AND ( click . engine_displacement IS NULL OR
( cand . engine_displacement IS NOT NULL AND
cand . engine_displacement >= click . engine_displacement -
$dispDelta AND
cand . engine_displacement <= click . engine_displacement +
$dispDelta ) )
WITH
97
cand ,
count ( DISTINCT feat ) AS shared_features ,
avg ( CASE
WHEN click . price_new IS NULL OR cand . price_new IS NULL
THEN NULL
ELSE abs ( cand . price_new - click . price_new )
END ) AS avg_price_diff ,
avg ( CASE
WHEN click . engine_displacement IS NULL
OR cand . engine_displacement IS NULL
THEN NULL
ELSE abs ( cand . engine_displacement - click . engine_displacement )
END ) AS avg_disp_diff
ORDER BY shared_features DESC ,
avg_price_diff ASC ,
avg_disp_diff ASC
WITH
cand . car_brand AS brand ,
cand . car_model AS model ,
head ( collect ( cand ) ) AS rep ,
head ( collect ( shared_features ) ) AS shared_features ,
head ( collect ( avg_price_diff ) ) AS avg_price_diff ,
head ( collect ( avg_disp_diff ) ) AS avg_disp_diff
RETURN
rep . car_name AS car_name ,
rep . car_brand AS car_brand ,
rep . car_model AS car_model ,
rep . mongo_id AS mongo_id ,
rep . production_year AS production_year ,
rep . price_new AS price_new ,
rep . engine_displacement AS engine_displacement ,
shared_features AS shared_features ,
avg_price_diff AS avg_price_diff ,
avg_disp_diff AS avg_disp_diff
ORDER BY shared_features DESC ,
avg_price_diff ASC ,
avg_disp_diff ASC
LIMIT $k

//

"

