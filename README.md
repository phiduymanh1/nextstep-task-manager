# 🚀 NextStep Task Manager

A full-stack task management system inspired by Trello, supporting workspace-based collaboration, role-based access control, and scalable architecture.

---

## 🏗️ System Architecture

Frontend (Vercel)
        ↓
Backend API (Render)
        ↓
MySQL Database (Aiven)
Redis Cache (Upstash)

---

## 🧠 Core Workflow

Workspace → Board → List → Card

---

## ⚙️ Tech Stack

### 🖥 Backend
- Java 21 + Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- Flyway Migration
- MySQL (Aiven Cloud)
- Redis (Upstash)
- Swagger / OpenAPI
- MapStruct
- Cloudinary (file upload)
- Email service (SMTP)

### 🎨 Frontend
- React 19 + Vite
- TypeScript
- React Router
- Axios
- React Hook Form + Zod
- Framer Motion
- DnD Kit (drag & drop)

---

## 🌐 Deployment

- Frontend: Vercel  
- Backend: Render  
- Database: Aiven MySQL (Free Tier)  
- Cache: Upstash Redis  

---

## 🗄 Database Design

### Core Modules
- Users & Auth
- Workspace Management
- Boards & Lists
- Cards (Tasks)
- Members & Roles
- Labels & Tags
- Attachments
- Comments
- Activity Logs
- Notifications

---

## 🧩 Features

### Authentication & Authorization
- JWT Authentication
- Role-based access:
  - OWNER
  - ADMIN
  - MEMBER
  - GUEST

---

### Workspace Management
- Multi-workspace system
- Invite & manage members
- Role-based permissions

---

### Board System
- Multiple boards per workspace
- Drag & drop lists/cards
- Star favorite boards

---

### Task Management
- Cards with:
  - Title & description
  - Due date
  - Checklists
  - Labels
  - Attachments
  - Comments
- Assign users to tasks

---

### Activity Tracking
- Full activity log
- JSON metadata support

---

### Notifications
- Task assignment alerts
- Mentions
- Activity updates

---

## 🗄 Database (Aiven MySQL)

Connection format:
mysql://user:pass@host:port/db

Spring Boot format:
jdbc:mysql://host:port/db?sslMode=REQUIRED

---

## ⚡ Redis (Upstash)

Used for:
- Cache layer
- Performance optimization
- Session/token handling

---

## 🚀 Run Project Locally

### Backend
```bash
mvn clean install
mvn spring-boot:run
```

### Frontend
```bash
npm install
npm run dev
```

---

## 🔐 Environment Variables

### Backend
```
SPRING_PROFILES_ACTIVE=
ALLOWED_ORIGINS=
DB_URL=
DB_USERNAME=
DB_PASSWORD=
REDIS_HOST=
REDIS_PORT=
JWT_SECRET=
NVD_API_KEY=
MAIL_PASS=
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
SSL_ENABLED=
KEY_STORE_PASS=
DB_URL_PROD=jdbc:
DB_USERNAME_PROD=
DB_PASSWORD_PROD=
REDIS_HOST_PROD=
REDIS_PASSWORD_PROD=
```

### Frontend
```
VITE_API_URL=
```

---

## 📌 Key Highlights

- ENUM role-based system
- Drag & drop with DECIMAL positioning
- JSON metadata support
- Soft delete strategy
- SEO-friendly slugs
- Scalable relational database design

---

## 👨‍💻 Author

- Name: Phí Duy Mạnh
- Project: NextStep Task Manager
```
