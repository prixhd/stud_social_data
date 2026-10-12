# Student Social Scholarship Management System

Веб-приложение для учёта социальных стипендий студентов, разработанное и внедрённое в Дагестанском государственном университете.

Проект начался как дипломная работа и после завершения разработки был внедрён в университете. Приложение используется сотрудниками для ведения базы студентов, контроля сроков, поиска, фильтрации, формирования статистики и экспорта данных.

## Production usage

- 3–4 сотрудника работают с системой
- 2000+ студентов в production-базе
- приложение заменило ручную работу с данными в Excel
- приложение поддерживается и дорабатывается после внедрения

Проект прошёл полный цикл: сбор требований → проектирование → разработка → внедрение → эксплуатация → исправление ошибок → оптимизация.

## Problem

До внедрения приложения учёт велся преимущественно в Excel.

Это создавало ограничения:

- отсутствие отдельных профилей студентов;
- неудобный поиск по большой базе;
- отсутствие многокритериальной фильтрации;
- ручной контроль сроков;
- отсутствие централизованной статистики;
- отсутствие единого интерфейса для работы с данными;
- необходимость вручную формировать и обрабатывать отчёты.

Приложение объединяет эти операции в одной системе.

## Features

### Students

- добавление, редактирование и удаление студентов;
- отдельный профиль студента;
- хранение факультета, курса и формы обучения;
- учёт вида и основания стипендии;
- контроль сроков действия основания.

### Search and filtering

Поддерживается поиск по:

- ФИО;
- номеру приказа.

Многокритериальная фильтрация через JPA Specification:

- факультет;
- вид стипендии;
- форма обучения;
- основание;
- курс.

Также поддерживается динамическая сортировка результатов.

### Server-side pagination

Список студентов использует server-side pagination через Spring Data `Pageable`.

Вместо загрузки всей базы приложение получает только необходимую страницу данных.

Пагинация работает совместно с фильтрацией и сортировкой, а также для списков истекающих и архивных записей.

### Scholarship order history

Для каждого студента поддерживается история приказов.

При активации приказа:

1. предыдущие активные приказы деактивируются;
2. выбранный приказ становится активным;
3. текущие данные студента синхронизируются с активным приказом.

История реализована отдельной сущностью `ScholarshipOrder` со связью `Student 1:N ScholarshipOrder`.

### Status tracking

Система определяет:

- актуальных студентов;
- студентов с истекающим основанием;
- студентов с истёкшим основанием;
- бессрочные основания.

### Excel export

Экспорт данных в XLSX реализован через Apache POI.

Поддерживается:

- экспорт отфильтрованной выборки;
- экспорт выбранных студентов;
- валидация объёма выгрузки;
- измерение длительности экспорта через Micrometer.

## Architecture

Приложение построено по многослойной архитектуре:

```text
Browser
   |
Spring MVC Controllers
   |
Service Layer
   |
Spring Data JPA
   |
Hibernate
   |
MySQL
```

Основные слои приложения:

```text
controller
service
dao
model
dto
config
util
```

Для доступа к данным используются Spring Data JPA, Hibernate, `JpaRepository`, `JpaSpecificationExecutor` и JPQL.

## Tech stack

### Backend

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Spring Security
- Hibernate
- Bean Validation


### Database

- MySQL
- Flyway
- SQL
- database indexes and constraints
- Redis

### Frontend

- Thymeleaf
- HTML
- CSS
- Bootstrap
- JavaScript

### Monitoring

- Spring Boot Actuator
- Micrometer
- Prometheus

### Testing and tools

- JUnit 5
- Mockito
- Apache POI
- Maven
- Git

## Database

Основные сущности:

```text
Student
Faculty
StudyForm
Scholarship
Foundation
ScholarshipOrder
```

Основные связи:

```text
Faculty      1 ---- N Student
StudyForm    1 ---- N Student
Scholarship  1 ---- N Student
Foundation   1 ---- N Student

Student      1 ---- N ScholarshipOrder
```

В схеме используются:

- foreign keys;
- indexes;
- check constraints;
- Flyway migrations.

При добавлении истории приказов существующие данные студентов были перенесены в новую таблицу `scholarship_orders` через Flyway migration.

## Performance

Производительность проверялась локально на 2505 синтетических записях — объёме, сопоставимом с production-базой.

Измерения проводились через `curl` на одинаковом наборе данных после прогрева приложения.

### Server-side pagination

До оптимизации страница `/students/show` загружала и рендерила все 2505 записей.

После оптимизации используется Spring Data `Pageable` по 50 записей на страницу.

| Metric | Before | After |
|---|---:|---:|
| Records per page | 2505 | 50 |
| Median HTTP response time | ~154 ms | ~25.6 ms |
| HTML response size | ~5.52 MiB | ~130 KiB |

Результат:

- медианное время HTTP-ответа уменьшилось примерно в 6 раз;
- размер HTML-ответа уменьшился примерно в 43 раза.

Метод измерения:

- 5 warm-up запросов;
- 10 измеряемых запросов;
- авторизованная HTTP-сессия;
- `curl time_total`.

Подробные результаты:

`benchmarks/pagination.md`

### Dashboard statistics

Изначально dashboard загружал всю коллекцию `Student` и рассчитывал статистику через Java Streams.

До оптимизации:

```text
MySQL
   |
2505 Student entities
   |
Java Streams
   |
Statistics
```

После оптимизации:

```text
MySQL
   |
COUNT + SUM(CASE ...)
   |
Aggregated values
```

Расчёт статистики был перенесён в агрегирующий SQL-запрос.

| Metric | Before | After |
|---|---:|---:|
| Median HTTP response time | ~26.9 ms | ~13.0 ms |

Медианное время ответа уменьшилось примерно в 2.1 раза.

Подробные результаты:

`benchmarks/dashboard-performance.md`

## Monitoring

Приложение использует Spring Boot Actuator, Micrometer и Prometheus.

Добавлены прикладные метрики:

```text
students.export.count
students.created.count
students.updated.count
students.deleted.count
students.export.duration
```

Также доступны стандартные метрики Spring Boot для JVM, HTTP и datasource.

В production публично доступен только health endpoint. Остальные management endpoints требуют аутентификации.

## Security

Для ограничения доступа используется Spring Security.

Реализовано:

- form-based authentication;
- BCrypt password encoding;
- CSRF protection;
- защита изменяющих HTTP-запросов;
- ограничение доступа к Actuator endpoints;
- credentials передаются через environment variables и не хранятся непосредственно в Java-коде.

## Testing

Бизнес-логика покрыта unit-тестами на JUnit 5 и Mockito.

Текущий test suite:

```text
Tests run: 28
Failures: 0
Errors: 0
```

Проверяются в том числе:

- создание студента;
- создание первоначального приказа;
- валидация данных;
- обновление и удаление студента;
- передача параметров пагинации в repository;
- создание приказа;
- деактивация предыдущего приказа;
- активация выбранного приказа;
- синхронизация активного приказа с данными студента;
- обработка отсутствующих сущностей.

Запуск всех тестов:

```bash
./mvnw clean test
```

## Requirements

Для локального запуска необходимы:

- Java 17
- MySQL
- Git

Maven отдельно устанавливать не требуется — в репозитории используется Maven Wrapper.

## Configuration

Для подключения к БД и входа в приложение используются environment variables.

```bash
export DB_USERNAME='root'
export DB_PASSWORD='your-database-password'

export APP_ADMIN_USERNAME='admin'
export APP_ADMIN_PASSWORD='your-application-password'
```

При необходимости можно переопределить URL базы:

```bash
export DB_URL='jdbc:mysql://localhost:3306/studdata'
```

Не добавляйте реальные пароли или другие секреты в Git.

## Running locally

### 1. Clone repository

```bash
git clone https://github.com/prixhd/stud_social_data.git
cd stud_social_data
```

### 2. Check Java

```bash
java -version
```

Проект использует Java 17.

### 3. Create database

```sql
CREATE DATABASE studdata;
```

### 4. Configure environment

```bash
export DB_USERNAME='root'
export DB_PASSWORD='your-database-password'

export APP_ADMIN_USERNAME='admin'
export APP_ADMIN_PASSWORD='your-application-password'
```

### 5. Run tests

```bash
./mvnw clean test
```

### 6. Start application

```bash
./mvnw spring-boot:run
```

После запуска приложение доступно по адресу:

```text
http://localhost:8080
```

Flyway автоматически применяет необходимые миграции при запуске.

## Project structure

```text
src/
├── main/
│   ├── java/com/example/studdata/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dao/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── service/
│   │   └── util/
│   │
│   └── resources/
│       ├── db/migration/
│       ├── static/
│       ├── templates/
│       └── application.yml
│
└── test/
    └── java/com/example/studdata/
```

## Development

После первоначального внедрения приложение продолжило развиваться и поддерживаться.

В ходе эксплуатации и дальнейшей разработки были добавлены:

- многокритериальная фильтрация;
- server-side pagination;
- история приказов;
- Flyway migrations;
- агрегированная статистика;
- Excel export;
- monitoring;
- unit-тесты;
- дополнительные security-настройки.

При поиске проблем использовались application logs, stack trace, SQL-логи и воспроизведение ошибок локально. Для проверки оптимизаций проводились воспроизводимые HTTP-замеры на синтетическом наборе данных.

## Author
 
Абдулманап Аминов

GitHub: https://github.com/prixhd
