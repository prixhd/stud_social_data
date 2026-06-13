-- Миграция существующих данных студентов в таблицу scholarship_orders
INSERT INTO scholarship_orders (
    student_id,
    scholarship_id,
    foundation_id,
    order_number,
    issuance_end_date,
    foundation_end_date,
    is_permanent,
    is_active,
    created_date
)
SELECT
    s.id,
    s.scholarship_id,
    s.foundation_id,
    s.order_number,
    s.issuance_end_date,
    s.foundation_end_date,
    COALESCE(s.is_permanent, 0),
    1, -- все существующие приказы активные
    COALESCE(s.created_date, CURRENT_DATE)
FROM students s
WHERE NOT EXISTS (
    SELECT 1 FROM scholarship_orders so WHERE so.student_id = s.id
);