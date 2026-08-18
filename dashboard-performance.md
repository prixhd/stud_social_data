# Dashboard statistics optimization

Date: 2026-08-18

## Dataset

2505 synthetic student records in MySQL.

## Before

Implementation:
- loaded all Student entities from the database;
- calculated total/active/expired/expiring statistics in Java using Streams.

Benchmark:
- 5 warm-up requests;
- 10 measured requests;
- curl `time_total`;
- authenticated HTTP session.

Results:
- median: ~26.9 ms
- average: ~30.8 ms
- response size: 8491 bytes

## After

Implementation:
- statistics calculated in MySQL;
- single aggregate query using COUNT and SUM(CASE ...);
- no full Student collection materialized for dashboard statistics.

Results:
- median: ~13.0 ms
- average: ~13.5 ms
- response size: 8491 bytes

## Result

Median response time decreased by approximately 2.1x (~52%).