# Payment Microservice – Cloud Native Spring Boot Application

Java | Spring Boot | Kafka | Resilience4j | Azure | Kubernetes | Docker | CI/CD | Design Patterns

---

# Overview

This project is a cloud-native Payment microservice built using Spring Boot and deployed on Microsoft Azure.

It demonstrates modern backend engineering practices including:

- Secure REST APIs with JWT authentication
- Event-driven architecture using Kafka
- Transactional Outbox Pattern for reliable messaging
- Idempotent request handling
- Retry mechanisms for fault tolerance
- Circuit Breaker using Resilience4j
- Docker, Kubernetes, and CI/CD pipelines

The system simulates a production-grade payment processing backend with enterprise-level scalability and resilience.

---

# Architecture

```mermaid
flowchart TD

    A["Client Application"] --> B["Spring Boot REST API"]

    B --> C["JWT Authentication"]
    C --> D["Security Filter"]

    D --> E["Controller Layer"]
    E --> F["Service Layer"]

    F --> G["Business Logic"]
    G --> H["Idempotency Check"]
    H --> I["Azure SQL Database"]

    F --> J["Transactional Outbox"]
    J --> K["Kafka Producer"]
    K --> L["Kafka Topic: payment.created"]

    L --> M["Kafka Consumer"]
    M --> N["Audit / Async Processing"]

    F --> O["Resilience4j Circuit Breaker"]
