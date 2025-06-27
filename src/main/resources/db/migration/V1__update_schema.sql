ALTER TABLE students ADD COLUMN is_permanent BOOLEAN DEFAULT FALSE;

 INSERT INTO foundations (name) VALUES ('Военнослужащий');

UPDATE scholarships SET name = 'Социальная стипендия' WHERE name = 'Социальная';
UPDATE scholarships SET name = 'Социальная стипендия в повышенном размере' WHERE name = 'В повышенном размере';