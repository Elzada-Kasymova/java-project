# 🧩 Microservices System: Users & Companies with Eureka Discovery

Этот проект реализует два микросервиса на Java 17 с использованием Spring Boot и Spring Cloud, взаимодействующие друг с другом через Eureka Discovery Service. Каждый сервис имеет собственную базу данных PostgreSQL, развернутую через Docker.

---

## 📦 Архитектура

```
[users-service] <-----> [Eureka] <-----> [company-service]
     |                        ↑                     |
     |                        ↓                     |
[PostgreSQL: userdb]     Discovery Server     [PostgreSQL: companydb]
```

- **users-service**: Управление пользователями, CRUD + получение компании по ID.
- **company-service**: Управление компаниями, CRUD + возвращает список сотрудников.
- **discovery-service (Eureka)**: Централизованная служба регистрации сервисов.

---

## 🚀 Технологии

- Java 17
- Spring Boot, Spring Data JPA
- Spring Cloud Eureka
- PostgreSQL (через Docker)
- Docker, Docker Compose
- Postman (для тестирования)

---

## 🛠️ Как запустить проект

### 📁 Структура проекта

```
java-project/
├── company-service/
│   └── Dockerfile
├── users-service/
│   └── Dockerfile
├── discovery-service/
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

### ⚙️ Шаги запуска

1. **Убедитесь, что Docker работает**  
   На Mac откройте Docker Desktop и дождитесь запуска.

2. **Соберите и запустите все сервисы**  
   Из корня проекта:
   ```bash
   docker-compose up --build
   ```

3. **Откройте в браузере:**
    - Eureka Dashboard: [http://localhost:8761](http://localhost:8761)
    - Users API: [http://localhost:8000/api/users](http://localhost:8000/api/users)
    - Companies API: [http://localhost:8090/api/company](http://localhost:8090/api/company)

---

## 📮 API Тестирование (Postman)

Коллекция Postman с примерами запросов доступна по ссылке:  
👉 [Открыть коллекцию в Postman](https://elzada.postman.co/workspace/Elzada~3c605b44-f931-4b46-88c9-869fdf18394d/collection/37646761-dae50584-a2b4-4cf4-96e6-a6a461ac225a?action=share&creator=37646761)

---

## 🗂️ Примеры API### 🔹 Users Service (`users-service`)

Хранит записи о пользователях:  
`id`, `firstName`, `lastName`, `phoneNumber`, `companyId`

📌 **Все API возвращают объект пользователя, включая данные его компании** (а не просто `companyId`).

#### Эндпоинты:

- `GET /api/users` — получить список всех пользователей с данными их компаний
- `GET /api/users/{id}` — получить одного пользователя по ID, включая компанию
- `POST /api/users` — создать нового пользователя (есть проверка, на существование компании)
- `PUT /api/users/{id}` — обновить данные пользователя
- `DELETE /api/users/{id}` — удалить пользователя

---

### 🔹 Company Service (`company-service`)

Хранит записи о компаниях:  
`id`, `name`, `budget`, `employeeIds`

📌 **Все API возвращают объект компании, включая список пользователей** (а не только `employeeIds`).

#### Эндпоинты:

- `GET /api/companies` — получить список всех компаний с их сотрудниками
- `GET /api/companies/{id}` — получить одну компанию по ID со списком её сотрудников
- `POST /api/companies` — создать новую компанию
- `PUT /api/companies/{id}` — обновить данные компании
- `DELETE /api/companies/{id}` — удалить компанию (удаляються и все пользователи этой компании)

## 👩‍💻 Автор
**Kasymova Elzada**  

