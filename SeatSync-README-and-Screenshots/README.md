# SeatSync — Concurrent Ticket Booking System

SeatSync is a full-stack ticket booking application built with **React** and **Spring Boot**.

The main goal of SeatSync is to demonstrate how a real booking system can prevent **two users from booking the same seat at the same time**. Users can browse events, choose a show, select seats, temporarily hold them for five minutes, confirm a booking, and view their booking history.

The project also includes **JWT authentication, USER/ADMIN roles, PostgreSQL, JPA/Hibernate, database locking, Kafka events, and Docker-based infrastructure**.

> **Project status:** This README documents the current SeatSync application and uses the supplied project screenshots as sample screens.

---

## ✨ Features

### User features

- User registration and login
- JWT-based authentication
- USER and ADMIN roles
- Browse available events
- View event details
- Select a show date and time
- View seat availability
- Select seats
- Hold seats for **5 minutes**
- Confirm bookings
- View booking success details
- View previous bookings
- View profile information
- Logout

### Booking and concurrency features

- Temporary seat holds
- Five-minute hold countdown
- Backend-authoritative seat state
- Transactional booking flow
- PostgreSQL pessimistic locking to protect against concurrent booking
- Prevents double booking when multiple users try to book the same seat
- Server recalculates the booking amount during confirmation

### Admin features

- Admin authentication
- Manage events
- Manage shows
- View users
- View bookings
- Update booking status

### Backend features

- Spring Boot REST APIs
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT authentication
- BCrypt password hashing
- Global exception handling
- Scheduled cleanup of expired seat holds
- Kafka producer and consumer
- Docker support

---

## 🏗️ Architecture

SeatSync follows a layered Spring Boot REST architecture.

```text
                    React Frontend
                         │
                         │ REST / JSON
                         ▼
                ┌──────────────────┐
                │    Controller    │
                └────────┬─────────┘
                         ▼
                ┌──────────────────┐
                │     Service      │
                └────────┬─────────┘
                         ▼
                ┌──────────────────┐
                │    Repository    │
                └────────┬─────────┘
                         ▼
                ┌──────────────────┐
                │  JPA / Hibernate │
                └────────┬─────────┘
                         ▼
                ┌──────────────────┐
                │    PostgreSQL    │
                └──────────────────┘

                 Successful Booking
                         │
                         ▼
                       Kafka
                         │
                         ▼
                 Booking Consumer
```

---

## 🛠️ Technology Stack

### Frontend

- React
- Vite
- JavaScript
- React Router
- Axios
- CSS

### Backend

- Java 23
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- Maven
- JUnit / Mockito

### Database

- PostgreSQL

### Messaging

- Apache Kafka

### Development

- Git
- GitHub
- Docker
- Docker Compose
- IntelliJ IDEA
- Visual Studio Code
- Postman

---

## 📁 Project Structure

The repository contains both the frontend and backend.

```text
SeatSync-Booking-System/
│
├── src/                         # React frontend
│   ├── components/
│   ├── context/
│   ├── hooks/
│   ├── layouts/
│   ├── pages/
│   ├── services/
│   └── utils/
│
├── public/
├── package.json
├── package-lock.json
├── vite.config.js
├── .env.example
│
├── seatsync-backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │
│   ├── pom.xml
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── .gitignore
│
├── docs/
│   └── screenshots/
│       ├── home-events.png
│       ├── register.png
│       ├── event-details.png
│       ├── seat-selection-empty.png
│       ├── seat-selection-held.png
│       └── booking-success.png
│
└── README.md
```

### Files intentionally kept local

These should not be committed to GitHub:

```text
.env
.idea/
target/
node_modules/
```

An `.env.example` file can be committed with safe placeholder values.

---

# 💻 Run SeatSync on Another Laptop

## 1. Install the required software

Install:

- Git
- Node.js 18+
- Java 23
- Maven
- Docker Desktop

Verify:

```bash
git --version
node --version
npm --version
java -version
mvn -version
docker --version
```

Make sure Docker Desktop is running.

---

## 2. Clone the repository

```bash
git clone https://github.com/Sabarii27/SeatSync-Booking-System.git
cd SeatSync-Booking-System
```

After cloning, the same repository contains the React frontend and the `seatsync-backend` folder.

---

# 🎨 Start the Frontend

From the repository root:

```bash
npm install
```

Create a frontend `.env` file if required:

```env
VITE_API_URL=http://localhost:8080/api
```

Start Vite:

```bash
npm run dev
```

Normally the frontend runs at:

```text
http://localhost:5173
```

Keep this terminal running.

---

# ☕ Start the Backend

Open a second terminal:

```bash
cd SeatSync-Booking-System/seatsync-backend
```

The backend uses:

```text
Java 23
Spring Boot
PostgreSQL
Kafka
```

---

## 3. Start PostgreSQL and Kafka

With Docker Desktop running:

```bash
docker compose up -d postgres kafka
```

Check:

```bash
docker ps
```

The local development configuration uses:

```text
PostgreSQL → localhost:5433
Kafka      → localhost:9092
```

---

## 4. Start Spring Boot

Open `seatsync-backend` in IntelliJ IDEA and run:

```text
SeatSyncApplication.java
```

Or, if Maven is configured:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

The frontend communicates with:

```text
http://localhost:8080/api
```

---

# 🔐 Environment Variables

Do **not** commit your real `.env` file to GitHub.

If local configuration is required, create a new `.env` file on the new laptop.

Example development values:

```env
DB_HOST=localhost
DB_PORT=5433
DB_NAME=seatsync
DB_USERNAME=postgres
DB_PASSWORD=postgres

KAFKA_BOOTSTRAP_SERVERS=localhost:9092

JWT_SECRET=local-dev-only-change-me-this-secret-must-be-at-least-32-bytes

CORS_ORIGIN=http://localhost:5173

ADMIN_EMAIL=admin@seatsync.local
ADMIN_PASSWORD=Admin@123
```

> Never put real production passwords, JWT secrets, database credentials, API keys, or other secrets in GitHub.

---

# 🔄 Complete Startup Flow

### Terminal 1 — Docker

```bash
cd SeatSync-Booking-System/seatsync-backend
docker compose up -d postgres kafka
```

### Terminal 2 — Backend

Open `seatsync-backend` in IntelliJ IDEA and run:

```text
SeatSyncApplication.java
```

### Terminal 3 — Frontend

From the repository root:

```bash
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

---

# 🎟️ Typical User Flow

```text
Register
   ↓
Login
   ↓
Browse Events
   ↓
Select Event
   ↓
Select Show
   ↓
Select Seat
   ↓
Hold Seat for 5 Minutes
   ↓
Confirm Booking
   ↓
Booking Created
   ↓
Kafka Booking Event
   ↓
View My Bookings
```

---

# 🔒 How SeatSync Prevents Double Booking

This is the main technical feature of the project.

Imagine:

```text
User A → selects B5
User B → selects B5
```

Both users cannot receive the same seat.

The backend uses:

- `@Transactional`
- PostgreSQL row-level locking
- JPA pessimistic locking
- Backend seat-state validation

Conceptually:

```text
User A
   │
   ▼
Lock B5
   │
   ▼
Check B5
   │
   ▼
Book B5
   │
   ▼
Commit
```

At the same time:

```text
User B
   │
   ▼
Wait for B5 lock
   │
   ▼
Check B5 again
   │
   ▼
B5 is already BOOKED
   │
   ▼
Booking rejected
```

Therefore:

```text
Two users
    ↓
Same seat
    ↓
Only one successful booking
```

This is why the project is called:

> **SeatSync: two people, one seat, one winner.**

---

# ⏱️ Five-Minute Seat Hold

When a user selects an available seat:

```text
AVAILABLE
    ↓
HELD
    ↓
5-minute countdown
```

If the user confirms:

```text
HELD
  ↓
BOOKED
```

If the five minutes expire:

```text
HELD
  ↓
AVAILABLE
```

The backend is authoritative for the hold expiration. The frontend countdown is only a visual representation.

---

# 📨 Kafka Booking Event

After a successful booking:

```text
BookingService
      │
      ▼
Database transaction commits
      │
      ▼
Kafka Producer
      │
      ▼
booking-events topic
      │
      ▼
Kafka Consumer
```

The consumer can receive/process the booking event for downstream booking-related processing.

---

# 🗄️ Main Database Relationships

```text
User
 │
 └── Booking
       │
       └── BookingSeat
               │
               └── Seat

Event
 │
 └── Show
       │
       └── Seat
```

Main entities:

- User
- Event
- Show
- Seat
- Booking
- BookingSeat

---

# 🌐 Main API Areas

The frontend communicates with backend REST APIs under areas such as:

```text
/api/auth
/api/profile
/api/events
/api/shows
/api/bookings
/api/admin
```

Authentication uses a JWT Bearer token:

```text
Authorization: Bearer <JWT_TOKEN>
```

---

# 🖥️ Application Screens

The screenshots below are stored in:

```text
docs/screenshots/
```

### Home / Events

![SeatSync Home](docs/screenshots/home-events.png)

### Registration

![SeatSync Registration](docs/screenshots/register.png)

### Event Details and Showtimes

![SeatSync Event Details](docs/screenshots/event-details.png)

### Seat Selection

![SeatSync Seat Selection](docs/screenshots/seat-selection-empty.png)

### Seat Held for Five Minutes

![SeatSync Held Seat](docs/screenshots/seat-selection-held.png)

### Booking Confirmation

![SeatSync Booking Success](docs/screenshots/booking-success.png)

---

# 📸 How the Screenshots Work on Another Laptop

The screenshots are stored inside the Git repository:

```text
docs/screenshots/
```

For example:

```text
docs/screenshots/home-events.png
```

The README references them using relative paths:

```markdown
![SeatSync Home](docs/screenshots/home-events.png)
```

When another developer clones the repository:

```bash
git clone https://github.com/Sabarii27/SeatSync-Booking-System.git
```

Git downloads the screenshot files too.

Therefore, **you do not need to manually copy the screenshots on another laptop**.

---

# 🧪 Frontend Commands

| Command | Purpose |
| --- | --- |
| `npm install` | Install frontend dependencies |
| `npm run dev` | Start Vite development server |
| `npm run build` | Create production build |
| `npm run preview` | Preview production build |

---

# 🐳 Docker Commands

Start PostgreSQL and Kafka:

```bash
docker compose up -d postgres kafka
```

Check containers:

```bash
docker ps
```

Stop containers:

```bash
docker compose stop
```

Stop and remove containers:

```bash
docker compose down
```

Remove containers and PostgreSQL volume/data:

```bash
docker compose down -v
```

> Use `docker compose down -v` carefully because it removes the database volume.

---

# 🐛 Troubleshooting

### Frontend does not start

```bash
npm install
npm run dev
```

Check Node and npm:

```bash
node --version
npm --version
```

### Events are not loading

Check that:

1. PostgreSQL is running.
2. Kafka is running.
3. Spring Boot is running.
4. Backend is available on port `8080`.
5. `VITE_API_URL` points to `http://localhost:8080/api`.

### PostgreSQL connection error

```bash
docker ps
```

Then:

```bash
docker compose up -d postgres
```

### Kafka connection error

```bash
docker compose up -d kafka
```

Then restart Spring Boot.

### Port 5173 is already in use

Use the URL printed by:

```bash
npm run dev
```

### Port 8080 is already in use

Stop the application using port `8080`, or change the Spring Boot server port in `application.yml`.

---

# 🧹 Git Files That Stay Local

These are intentionally ignored:

```text
.env
.idea/
target/
node_modules/
```

The repository can contain:

```text
.env.example
```

with safe placeholder values.

---

# 👨‍💻 Author

**Sabarinathan M**

B.Tech Computer Science and Engineering

GitHub: https://github.com/Sabarii27/

LinkedIn: https://www.linkedin.com/in/sabari27/

---

# 📄 License

This project is intended as a personal learning and portfolio project.
