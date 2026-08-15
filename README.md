# 🍔 SafeBite — Deep Supply Chain & Allergen Tracer

**SafeBite** is a graph-based food supply-chain and allergen tracing application built to identify hidden allergens across deeply nested ingredients, suppliers, and processing facilities.

It demonstrates how **Graph Databases (Neo4j / CognoDB)** can simplify highly connected data and multi-hop traversal compared to traditional relational approaches.

### 🌐 Live Demo

* **Frontend:** https://safebite-rontend.onrender.com/
* **Backend API:** https://safebite-backend-lri4.onrender.com/api/safebite

---

## 🧠 Problem

A menu item may contain an ingredient that contains another ingredient several levels deep:

```text
Burger
  ↓
Secret Sauce
  ↓
Base Paste
  ↓
Peanuts
```

A simple ingredient lookup would miss the peanut.

SafeBite models these dependencies as a graph and uses **Cypher variable-length paths** to detect hidden allergens and trace their source.

---

## 🕸️ Graph Model

### Nodes

* `MenuItem`
* `Ingredient`
* `Allergen`
* `Supplier`
* `Facility`

### Relationships

```text
(MenuItem)-[:CONTAINS]->(Ingredient)
(Ingredient)-[:CONTAINS]->(Ingredient)
(Ingredient)-[:HAS_ALLERGEN]->(Allergen)
(Ingredient)-[:SOURCED_FROM]->(Supplier)
(Supplier)-[:OPERATES_IN]->(Facility)
```

Example:

```text
MenuItem
   ↓ CONTAINS
Ingredient
   ↓ CONTAINS
Ingredient
   ↓ HAS_ALLERGEN
Allergen
```

---

## ✨ Features

* 🔎 Deep allergen detection
* 🧬 Multi-level ingredient tracing
* 🚨 Unsafe menu detection
* 🥗 Safe alternative recommendations
* 🏭 Facility impact analysis
* 🕸️ Variable-depth graph traversal
* 🐳 Dockerized backend
* ☁️ Render deployment
* 🧪 Backend service-level testing

---

## 📡 API Endpoints

Base URL:

```text
/api/safebite
```

| Endpoint                                      | Purpose                                |
| --------------------------------------------- | -------------------------------------- |
| `GET /menu`                                   | Get all menu items                     |
| `GET /allergens`                              | Get available allergens                |
| `GET /menu/{itemName}/ingredients`            | Get direct ingredients                 |
| `GET /trace?item=&allergen=`                  | Trace an allergen path                 |
| `GET /unsafe?allergens=`                      | Find unsafe menu items                 |
| `GET /safe-alternatives?category=&allergens=` | Find safe alternatives                 |
| `GET /facility-impact?facility=`              | Find menu items affected by a facility |

### Example Cypher

```cypher
MATCH path =
(m:MenuItem)-[:CONTAINS*1..4]->(i:Ingredient)
-[:HAS_ALLERGEN]->(a:Allergen)
WHERE a.name IN $allergens
RETURN DISTINCT m, path;
```

This allows allergens to be detected across **1–4 levels of nested ingredients**.

---

## 🏗️ Architecture

```text
React + Vite
     │
     │ REST API
     ▼
Spring Boot
     │
     │ Neo4j Java Driver
     ▼
Neo4j / CognoDB
```

---

## 🛠️ Tech Stack

**Backend**

* Java 21
* Spring Boot
* Spring Web
* Neo4j Java Driver
* Maven

**Frontend**

* React
* Vite
* Tailwind CSS
* Lucide React

**Database**

* Neo4j / CognoDB
* Cypher

**Deployment**

* Docker
* Render

---

## 🚀 Local Setup

### Prerequisites

* JDK 21+
* Maven 3.9+
* Node.js 18+
* npm
* Neo4j / CognoDB

### 1. Clone

```bash
git clone https://github.com/your-username/safebite.git
cd safebite
```

### 2. Configure Database

Set:

```text
NEO4J_URI
NEO4J_USERNAME
NEO4J_PASSWORD
PORT
```

Example:

```bash
export NEO4J_URI="bolt+s://your-instance-id.databases.neo4j.io"
export NEO4J_USERNAME="neo4j"
export NEO4J_PASSWORD="your-password"
export PORT=8080
```

### 3. Run Backend

```bash
cd backend
mvn clean spring-boot:run
```

Backend:

```text
http://localhost:8080
```

The application automatically runs the seed data through `SeedDataRunner`.

### 4. Run Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

## 🐳 Docker

Build:

```bash
cd backend
docker build -t safebite-backend .
```

Run:

```bash
docker run -p 8080:8080 \
  -e NEO4J_URI="bolt+s://your-instance-id.databases.neo4j.io" \
  -e NEO4J_USERNAME="neo4j" \
  -e NEO4J_PASSWORD="your-password" \
  safebite-backend
```

---

## 🧪 Testing

Testing was performed at the **backend service layer**, covering core business logic and graph-based operations such as:

* Allergen tracing
* Unsafe menu detection
* Safe alternatives
* Ingredient traversal
* Facility impact logic

API, frontend, and end-to-end automated tests are not currently included.

---

## 📁 Project Structure

```text
safebite/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

---

## 🔮 Future Improvements

* Authentication & role-based access
* Batch-level recall tracking
* Real-time contamination alerts
* QR-based allergen information
* Supply-chain analytics dashboard
* AI-powered risk explanations

---

## 👨‍💻 Author

**Jaswanth Siva Sai Sontineni**

Computer Science & Engineering

**Technologies:** Java • Spring Boot • React • Neo4j • Docker • Cypher
