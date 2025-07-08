-- Создание таблицы факультетов
CREATE TABLE IF NOT EXISTS faculties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Создание таблицы форм обучения
CREATE TABLE IF NOT EXISTS study_forms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Создание таблицы стипендий
CREATE TABLE IF NOT EXISTS scholarships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Создание таблицы оснований
CREATE TABLE IF NOT EXISTS foundations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Создание таблицы студентов
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    course INT NOT NULL CHECK (course >= 1 AND course <= 6),
    faculty_id BIGINT NOT NULL,
    study_form_id BIGINT NOT NULL,
    scholarship_id BIGINT NOT NULL,
    foundation_id BIGINT NOT NULL,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    issuance_end_date DATE NOT NULL,
    foundation_end_date DATE,
    is_permanent BOOLEAN DEFAULT FALSE,
    created_date DATE DEFAULT (CURRENT_DATE),
    updated_date DATE,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_student_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id),
    CONSTRAINT fk_student_study_form FOREIGN KEY (study_form_id) REFERENCES study_forms(id),
    CONSTRAINT fk_student_scholarship FOREIGN KEY (scholarship_id) REFERENCES scholarships(id),
    CONSTRAINT fk_student_foundation FOREIGN KEY (foundation_id) REFERENCES foundations(id),

    INDEX idx_student_faculty (faculty_id),
    INDEX idx_student_course (course),
    INDEX idx_student_order_number (order_number),
    INDEX idx_student_issuance_end_date (issuance_end_date),
    INDEX idx_student_foundation_end_date (foundation_end_date),
    INDEX idx_student_created_date (created_date),
    INDEX idx_student_name (last_name, first_name)
);

-- Добавляем проверочные ограничения
ALTER TABLE students
ADD CONSTRAINT chk_foundation_date
CHECK (is_permanent = TRUE OR foundation_end_date IS NOT NULL);

ALTER TABLE students
ADD CONSTRAINT chk_issuance_date_future
CHECK (issuance_end_date >= created_date);