-- Скрипт для безопасного обновления базы данных
-- Выполнить ПЕРЕД заменой JAR файла

-- Создаем резервную копию (если MySQL поддерживает)
-- mysqldump -u root -p students_db > backup_before_update.sql

-- Проверяем, есть ли уже поле is_permanent
SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'students_db'
AND TABLE_NAME = 'students'
AND COLUMN_NAME = 'is_permanent';

-- Добавляем поле is_permanent, если его нет
ALTER TABLE students
ADD COLUMN IF NOT EXISTS is_permanent BOOLEAN DEFAULT FALSE;

-- Проверяем, есть ли уже 'Военнослужащий' в foundations
SELECT COUNT(*) FROM foundations WHERE name = 'Военнослужащий';

-- Добавляем 'Военнослужащий', если его нет
INSERT IGNORE INTO foundations (name) VALUES ('Военнослужащий');

-- Обновляем названия стипендий
UPDATE scholarships
SET name = 'Социальная стипендия'
WHERE name = 'Социальная';

UPDATE scholarships
SET name = 'Социальная стипендия в повышенном размере'
WHERE name = 'В повышенном размере';

-- Проверяем результат
SELECT 'Обновление завершено успешно' as status;
SELECT COUNT(*) as total_students FROM students;
SELECT name FROM foundations;
SELECT name FROM scholarships;