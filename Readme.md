## 📦 Envision Microservices Monorepo

A production-ready, event-driven microservice architecture for order management, built with **Spring Boot**, **RabbitMQ**, **PostgreSQL**, and **Keycloak**. Deployed using **Docker**, CI/CD via **GitHub Actions**, and containerized images pushed to **GitHub Container Registry (GHCR)**.

---

## 🗂️ Project Structure

```
.
├── gateway-service/         # Spring Cloud Gateway
├── order-service/           # Manages orders, addresses, and events
├── product-service/         # Manages products and inventory
├── docker-compose.yml       # All services for local development
├── .github/workflows/       # GitHub Actions for CI/CD
└── README.md
```

---

## 🚀 Features

- ✅ **Event-Driven Architecture** with RabbitMQ
- 🔐 **OAuth2 Authentication** with Keycloak
- 🛍️ Order processing with stock validation
- 🗃️ Inventory reservations & failure tracking
- 📦 Multi-stage **Docker builds**
- 📄 OpenAPI Swagger docs via SpringDoc
- ✅ CI/CD with **GitHub Actions** + GHCR

---

## 🛠️ Tech Stack

| Component       | Tech                                           |
|----------------|------------------------------------------------|
| API Gateway     | Spring Cloud Gateway (WebFlux)                |
| Services        | Spring Boot (MVC + JPA + MapStruct + Liquibase) |
| Messaging       | RabbitMQ                                       |
| Authentication  | Keycloak (OAuth2 Resource Server)             |
| Persistence     | PostgreSQL                                     |
| Event Streaming | RabbitMQ Queues + Events (OrderPlaced, etc.)  |
| CI/CD           | GitHub Actions + GHCR                         |

---

## 📦 Services Overview

### 🧾 Order Service
- Manages orders, order items, addresses
- Persists order status history
- Emits and consumes inventory events

### 📦 Product Service
- Handles product catalog & stock
- Reserves inventory
- Emits `InventoryReserved` or `InventoryFailed`

### 🚪 Gateway Service
- Centralized entry point
- JWT auth via Keycloak
- Exposes Swagger UI

---

## 🧪 Local Development

### 🔧 Prerequisites

- Docker + Docker Compose
- Java 17
- Maven
- Keycloak + Realm JSON (optional)

### 🐳 Start All Services

```bash
docker-compose up --build
```

This will spin up:
- Order Service on `localhost:8081`
- Product Service on `localhost:8082`
- Gateway on `localhost:8080`
- RabbitMQ (management UI on `localhost:15672`)
- PostgreSQL
- Keycloak (optional, e.g., `localhost:8084`)

---

## 🐇 RabbitMQ Topology

| Exchange           | Queues                         | Events Consumed By        |
|--------------------|--------------------------------|---------------------------|
| `order.exchange`   | `order.placed.queue`           | Product Service           |
| `inventory.exchange` | `inventory.reserved.queue` <br> `inventory.failed.queue` | Order Service             |

---

## 🔐 Authentication

- All services use **OAuth2 JWT validation** via Keycloak.
- Tokens are passed via `Authorization: Bearer <token>`

---

## 📚 Swagger Documentation

| Service          | URL                                |
|------------------|-------------------------------------|
| Order Service    | `http://localhost:8081/swagger-ui.html` |
| Product Service  | `http://localhost:8082/swagger-ui.html` |
| Gateway Service  | `http://localhost:8080/swagger-ui.html` |

---

## 🚀 CI/CD: GitHub Actions + GHCR

On every push to `main`:
- Docker images are built per service using their `Dockerfile`
- Images are tagged and pushed to GHCR:
  - `ghcr.io/bikundo/order`
  - `ghcr.io/bikundo/product`
  - `ghcr.io/bikundo/gateway`

Workflow: `.github/workflows/docker-build-and-push.yml`

---

## 📁 Example API Usage

### 📦 Place an Order (via Gateway)
```http
POST /api/orders
Authorization: Bearer <JWT>

{
  "shippingAddressId": 1,
  "billingAddressId": 2,
  "items": [
    {
      "productId": 100,
      "productSku": "ABC123",
      "quantity": 2
    }
  ],
  "externalReference": "CLIENT-XYZ-0001",
  "contactEmail": "client@example.com",
  "contactPhone": "0722123456",
  "notes": "Handle with care"
}
```

---

## 🧹 Useful Commands

### Run one service locally:

```bash
cd order-service
mvn spring-boot:run
```

### Build Docker image for a service:

```bash
docker build -t ghcr.io/bikundo/order:latest order-service/
```

---

## 🧠 Roadmap Ideas

- [ ] Add retry/delay queues for failed orders
- [ ] Implement notification events (email, SMS)
- [ ] Add Prometheus + Grafana observability
- [ ] Add dynamic config via Spring Cloud Config