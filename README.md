# Payment Microservice – Cloud Native Spring Boot Application

Java | Spring Boot | Kafka | Resilience4j | Azure | Kubernetes | Docker | CI/CD | Design Patterns

---

# Overview

This project is a cloud-native Payment microservice built using Spring Boot and deployed on Microsoft Azure.

It demonstrates how modern backend systems are designed using:

- REST APIs with secure JWT authentication
- Event-driven architecture using Kafka
- Transactional Outbox Pattern for reliability
- Idempotent request handling
- Retry mechanisms for fault tolerance
- Circuit Breaker using Resilience4j
- Docker, Kubernetes, and CI/CD pipelines

The system simulates a production-grade payment processing backend with enterprise-level design and resilience patterns.

---

# Architecture

```mermaid
flowchart TD

A[Client] --> B[Spring Boot API]

B --> C[JWT Authentication]
C --> D[Security Filter]

D --> E[Controller]
E --> F[Service Layer]

F --> G[Business Logic]
G --> H[Idempotency Check]
H --> I[(Azure SQL DB)]

F --> J[Outbox Table]
J --> K[Kafka Producer]
K --> L[(Kafka Topic: payment.created)]

L --> M[Kafka Consumer]
M --> N[Audit / Async Processing]

F --> O[Resilience4j Circuit Breaker]
Tech Stack
Backend

Java 21

Spring Boot 3

Spring Data JPA

Spring Security (JWT)

Spring Kafka

Resilience4j

Messaging

Apache Kafka

Transactional Outbox Pattern

Cloud

Microsoft Azure

Azure App Service

Azure SQL Database

DevOps & Infra

Docker

Kubernetes

Azure DevOps Pipelines

Bicep (Infrastructure as Code)

Key Features
1. Kafka Event-Driven Architecture

Payment events are published asynchronously to Kafka

Topic: payment.created

Decouples payment processing from downstream services

2. Transactional Outbox Pattern

Payment + event stored in same DB transaction

Scheduled publisher pushes events to Kafka

Ensures no data loss and avoids dual-write issues

3. Idempotency

Prevents duplicate payment creation

Same request processed only once

Handles retries and network failures safely

4. Retry Handling
Producer

Outbox scheduler retries failed Kafka publishes

Consumer

Safe reprocessing enabled (idempotent design)

5. Circuit Breaker (Resilience4j)

Protects service from repeated downstream failures

Prevents cascading failures

Enables graceful degradation

API Endpoints
Authentication

POST /auth/login

{
  "username": "user",
  "password": "password"
}
Payments (Protected)

GET /payments
POST /payments

Requires:

Authorization: Bearer <JWT_TOKEN>
Kafka Event Example
{
  "eventId": "uuid",
  "transactionId": "TXN-123",
  "amount": 500,
  "status": "CREATED"
}
Project Structure
controller/
service/
repository/
entity/
dto/
event/
messaging/
outbox/
config/
security/
Deployment
Docker
docker build -t payment-service .
docker run -p 8080:8080 payment-service
Kubernetes
kubectl apply -f k8s/
Azure Deployment Flow

Code → Azure DevOps → Build → Docker → App Service / Kubernetes

Local Setup
mvn clean install
mvn spring-boot:run

App runs at:

http://localhost:8080
Kafka Local Setup (Docker)
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
Skills Demonstrated

Spring Boot Microservices

Kafka Event-Driven Architecture

Transactional Outbox Pattern

Idempotency & Retry Handling

Resilience4j Circuit Breaker

JWT Security

Docker & Kubernetes

Azure Cloud Deployment

CI/CD Pipelines

Why This Project Matters

Demonstrates how to build production-ready backend systems with:

scalability

fault tolerance

event-driven design

cloud-native deployment

Author

Abinaya Suryanarayanan
Java Backend Developer
Spring Boot | Kafka | Azure | Microservices

GitHub:
https://github.com/suryanarayananabinaya-tech
