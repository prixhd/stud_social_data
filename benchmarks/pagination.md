# Server-side pagination benchmark

Date: 2026-08-18

Dataset:
- 2505 synthetic student records
- MySQL
- local environment

Method:
- endpoint: GET /students/show
- 5 warm-up requests
- 10 measured requests
- curl time_total
- authenticated HTTP session

Before:
- all 2505 records rendered
- median: ~154 ms
- average: ~166 ms
- response size: 5,782,915 bytes (~5.52 MiB)

After:
- Page<Student>, 50 records per page
- median: ~25.6 ms
- average: ~33.0 ms
- response size: 132,796 bytes (~129.7 KiB)

Result:
- median response time: ~6x lower
- HTML response size: ~43.5x smaller