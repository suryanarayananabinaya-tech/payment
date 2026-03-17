# Payment Microservice – Cloud Native Spring Boot Application

Java | Spring Boot | Kafka | Resilience4j | Azure | Kubernetes | Docker | CI/CD | Design Patterns

---

# Overview

This project is a cloud-native Payment microservice built using Spring Boot and deployed on Microsoft Azure.

It demonstrates how modern backend systems are designed using:

- Secure REST APIs with JWT authentication
- Event-driven architecture using Apache Kafka
- Transactional Outbox Pattern for reliable messaging
- Idempotent request handling to prevent duplicates
- Retry mechanisms for fault tolerance
- Circuit Breaker using Resilience4j
- Docker, Kubernetes, and CI/CD pipelines

The system simulates a production-grade payment processing backend with enterprise-level design, scalability, and resilience.

---

# Architecture

```mermaid
flowchart TD

A[Client Application] --> B[Spring Boot REST API]

B --> C[JWT Authentication]
C --> D[Security Filter]

D --> E[Controller Layer]
E --> F[Service Layer]

F --> G[Business Logic]
G --> H[Idempotency Check]
H --> I[(Azure SQL Database)]

F --> J[Transactional Outbox]
J --> K[Kafka Producer]
K --> L[(Kafka Topic: payment.created)]

L --> M[Kafka Consumer]
M --> N[Audit / Async Processing]

F --> O[Resilience4j Circuit Breaker]

Tech Stack
Backend

Java 21

Spring Boot 3

Spring Web

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

Containerization

Docker

Orchestration

Kubernetes

DevOps

Azure DevOps Pipelines

Infrastructure as Code (Bicep)

Monitoring

Azure Application Insights

Azure Log Analytics

Key Features
1. Kafka Event-Driven Architecture

Payment events are published asynchronously to Kafka

Topic: payment.created

Enables loose coupling between services

Supports scalable, asynchronous workflows

Event Flow

Payment API
↓
Save Payment (DB)
↓
Create Outbox Event
↓
Outbox Publisher
↓
Kafka Topic (payment.created)
↓
Kafka Consumer
↓
Audit / Downstream Processing

2. Transactional Outbox Pattern

Ensures reliable communication between database and Kafka.

How it works

Payment + Outbox event saved in same DB transaction

Scheduler publishes pending events to Kafka

Prevents dual-write issues

Benefits

Guaranteed event delivery

Fault-tolerant publishing

Supports recovery from failures

3. Idempotency

Prevents duplicate payment creation.

Why needed

Network retries

Client resubmissions

Distributed system failures

Behavior

Same transaction ID → processed once

Duplicate requests → ignored or return existing response

Benefits

Duplicate-safe processing

Data consistency

Reliable APIs

4. Retry Handling
Producer Retry

Outbox scheduler retries failed Kafka sends

Handles temporary broker failures

Consumer Retry

Safe reprocessing supported

Works with idempotency to avoid duplicates

Benefits

Improves reliability

Ensures eventual consistency

5. Circuit Breaker (Resilience4j)

Protects service from repeated failures.

Behavior

Monitors failure rate

Opens circuit on threshold breach

Blocks calls temporarily

Allows recovery after cooldown

Benefits

Prevents cascading failures

Improves system stability

Enables graceful degradation

Security Architecture

JWT-based authentication using Spring Security.

Authentication Flow

Client Request
↓
Authentication Controller
↓
Authentication Manager
↓
JWT Token Generated
↓
Client Stores Token
↓
Client Sends Token in Header
↓
JWT Filter Validates Token
↓
Access Granted

Example Header
Authorization: Bearer <JWT_TOKEN>
API Endpoints
Authentication

POST /auth/login

Request
{
  "username": "user",
  "password": "password"
}
Response
{
  "token": "JWT_TOKEN"
}
Payments (Protected)

GET /payments
POST /payments

Requires JWT authentication.

Kafka Event
Topic

payment.created

Event Payload
{
  "eventId": "uuid",
  "transactionId": "TXN-123",
  "amount": 500,
  "status": "CREATED"
}
Consumer Responsibilities

Audit logging

Async processing

Future integrations (notifications, reporting)

Project Structure
src/main/java/com/example/payment

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
util/
Design Patterns Used

Strategy Pattern

Factory Pattern

Decorator Pattern

Observer Pattern

Proxy Pattern

Template Pattern

Transactional Outbox Pattern

Docker Setup
Build Image
docker build -t payment-service .
Run Container
docker run -p 8080:8080 payment-service
Kubernetes Deployment
kubectl apply -f k8s/

Includes:

Deployment

Service

Namespace

Secrets

Azure Deployment
Flow

Code → Azure DevOps → Build → Docker → App Service / AKS

Local Setup
Run Application
mvn clean install
mvn spring-boot:run

App URL:

http://localhost:8080
Local Kafka Setup
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
Configuration Highlights
Kafka

bootstrap servers via environment variable

JSON serialization/deserialization

topic: payment.created

Circuit Breaker

Resilience4j configuration for fault tolerance

Idempotency

Prevent duplicate processing using transaction identifiers

Retry

Producer retry via outbox scheduler

Consumer safe retry using idempotent design

Monitoring

Azure Application Insights

Logs via Azure Log Stream

Kafka consumer logs for event tracing

Skills Demonstrated

Spring Boot Microservices

Kafka Event-Driven Systems

Transactional Outbox Pattern

Idempotent API Design

Retry and Failure Handling

Circuit Breaker (Resilience4j)

JWT Security

Docker & Kubernetes

Azure Cloud Deployment

CI/CD Pipelines

Why This Project Matters

Modern backend systems require:

scalability

fault tolerance

asynchronous communication

secure APIs

cloud-native deployment

This project demonstrates how to build a production-ready backend with these capabilities.

Future Enhancements

Dead Letter Topic (DLT)

Distributed tracing (OpenTelemetry)

Rate limiting

Monitoring dashboards

Integration testing

Multi-service event-driven architecture
