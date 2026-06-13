-- Колонка is_indefinite уже существует, только обновляем constraint

-- Обновляем существующих студентов (на всякий случай)
UPDATE students
SET is_indefinite = 0
WHERE is_indefinite IS NULL;

-- Удаляем старый constraint (MySQL синтаксис)
ALTER TABLE students
DROP CHECK chk_foundation_date;

-- Добавляем новый constraint с учетом is_indefinite
ALTER TABLE students
ADD CONSTRAINT chk_foundation_date
CHECK (is_permanent = 1 OR is_indefinite = 1 OR foundation_end_date IS NOT NULL);