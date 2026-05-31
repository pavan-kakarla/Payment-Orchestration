# Payment-Orchestration (Java Spring Boot)

This workspace now contains a minimal Spring Boot (Java 11) implementation of the payment orchestration demo. It implements:

### Functional Requirements
- **Create Payment API**: Initiates transactions and handles them asynchronously.
- **Fetch Payment API**: Provides real-time status and detailed attempt history for any transaction.
- **Intelligent Routing**: Automatically routes payments based on method (e.g., CARD → Provider A, UPI → Provider B).
- **Automated Retries**: Implements exponential backoff for transient provider errors to increase success rates.
- **Failover Strategy**: Automatically switches to a secondary provider if the primary gateway is unavailable or exhausts retries.
- **Content-Based Idempotency**: Uses SHA-256 fingerprinting of the request payload to prevent duplicate charges within a 5-minute window.
- **Detailed Auditing**: Captures every provider interaction (request/response/status) in the `provider_attempts` table.

### Non-Functional Requirements
- **High Availability**: Achieved through multi-gateway failover and retry logic.
- **Scalability**: Asynchronous background processing ensures the API remains responsive under high load.
- **Data Integrity**: Uses PostgreSQL for transactional consistency and Redis for high-performance idempotency checks.
- **Observability**: Detailed logging of provider responses and status transitions for easy troubleshooting.
- **Extensibility**: Decoupled architecture allows for easy addition of new payment providers and routing rules.

## Setup and Running

### 1. Prerequisites & Infrastructure

Ensure you have Docker installed. If not, download it here:
- **macOS**: [Docker Desktop](https://docs.docker.com/desktop/mac/install/)
- **Windows**: [Docker Desktop](https://docs.docker.com/desktop/windows/install/)

Start the required infrastructure (PostgreSQL and Redis) using the provided compose file:

```bash
docker compose up -d
```

### 2. Build & Run the Application

**Requirements:** Java 11, Maven

```bash
mvn clean package
mvn spring-boot:run
```

Server listens on http://localhost:8080

## API Documentation & Interaction (Swagger UI)

The system is integrated with Swagger (OpenAPI) to provide an interactive UI for exploring and testing the endpoints.

**URL:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### How to use:
1. **Create Payment**:
   - Locate the `POST /payments` endpoint.
   - Click **Try it out**.
   - Use the pre-populated JSON body. Note that the system uses **Content-Based Idempotency**, so sending the exact same payload within 5 minutes will return the existing transaction rather than creating a new one.
   - Execute and observe the `201 Created` or `202 Accepted` response.

2. **Fetch Payment**:
   - Locate the `GET /payments/{paymentId}` endpoint.
   - Click **Try it out**, enter the `paymentId` returned from the Create API, and click **Execute**.
   - This will show the current status and the full history of provider attempts (retries/failovers).

## Database Schema

The system uses two primary tables to manage the payment lifecycle:

- **`payments`**: The master record. Tracks the `paymentId`, total `amount`, `currency`, and the final `status` (SUCCESS, FAILED, PROCESSING).
- **`provider_attempts`**: The audit log. Every time the orchestration engine calls a provider (including retries and failovers), a record is created here. It stores the `providerName`, `attemptNo`, the raw `response` from the gateway, and the `status` of that specific attempt.

Notes
- Persistence is now PostgreSQL (configured via environment variables). A `docker-compose.yml` is included to run Postgres and Redis.
- Providers are simulated (randomized responses) to demonstrate retry and failover behavior.
- This is a minimal demo for learning/testing; production code should include validation, security, proper error handling, metrics, and hardened idempotency stores (e.g., Redis with TTL).

Download Docker:
Docker Desktop (macOS): https://docs.docker.com/desktop/mac/install/
Docker Desktop (Windows): https://docs.docker.com/desktop/windows/install/

Dockerized Postgres + Redis

I added a `docker-compose.yml` that brings up Postgres and Redis. To start the containers:

```bash
docker compose up -d
```
## Debugging & Data Inspection

### Inspect PostgreSQL
To check the persisted payments and orchestration attempts:
```bash
docker exec -it $(docker ps -qf "name=postgres") psql -U payments_user -d payments

-- Useful queries:
SELECT * FROM payments;
SELECT * FROM attempts ORDER BY created_at DESC;
```

### Inspect Redis (Idempotency Store)
To check active idempotency locks and fingerprints (5-minute window):
```bash
docker exec -it $(docker ps -qf "name=redis") redis-cli

-- Useful commands:
KEYS *
GET <key_name>
TTL <key_name>
```
