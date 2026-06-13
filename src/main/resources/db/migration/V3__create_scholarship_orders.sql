-- Создание таблицы для истории приказов (MySQL синтаксис)
CREATE TABLE scholarship_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    scholarship_id BIGINT NOT NULL,
    foundation_id BIGINT NOT NULL,
    order_number VARCHAR(100) NOT NULL,
    issuance_end_date DATE NOT NULL,
    foundation_end_date DATE DEFAULT NULL,
    is_permanent TINYINT(1) DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_date DATE DEFAULT (CURRENT_DATE),

    CONSTRAINT fk_order_student FOREIGN KEY (student_id)
        REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_scholarship FOREIGN KEY (scholarship_id)
        REFERENCES scholarships(id),
    CONSTRAINT fk_order_foundation FOREIGN KEY (foundation_id)
        REFERENCES foundations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Индексы для быстрого поиска
CREATE INDEX idx_scholarship_orders_student ON scholarship_orders(student_id);
CREATE INDEX idx_scholarship_orders_active ON scholarship_orders(is_active);
CREATE INDEX idx_scholarship_orders_created ON scholarship_orders(created_date);