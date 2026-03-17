# Payment Microservice – Cloud Native Spring Boot Application

Java | Spring Boot | Kafka | Resilience4j | Azure | Kubernetes | Docker | CI/CD | Design Patterns

---

# Overview

This repository contains a cloud-native Payment microservice built using Spring Boot and designed for deployment on Microsoft Azure.

It demonstrates modern backend engineering practices including:

- Secure REST APIs with JWT authentication
- Event-driven architecture using Kafka
- Transactional Outbox Pattern for reliable messaging
- Idempotent request handling
- Retry mechanisms for fault tolerance
- Circuit Breaker using Resilience4j
- Docker, Kubernetes, and CI/CD pipelines

The service simulates a production-style payment processing backend and demonstrates how enterprise systems are structured, secured, scalable, and fault-tolerant.

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


## High-Level Flow

- Client Application calls Spring Boot REST API
- JWT Authentication validates the user request
- Security Filter protects secured endpoints
- Controller Layer receives the request
- Service Layer executes business logic
- Idempotency Check prevents duplicate payment creation
- Repository Layer persists data to Azure SQL Database
- Transactional Outbox stores payment events reliably
- Kafka Producer publishes events to `payment.created`
- Kafka Consumer processes events asynchronously
- Audit or downstream processing is triggered
- Resilience4j Circuit Breaker protects downstream calls

## Cloud Deployment Flow

- Application packaged as Docker container
- Deployed to Azure App Service or Kubernetes
- CI/CD handled through Azure DevOps Pipelines

---

# Tech Stack

## Backend
- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT Authentication
- Spring Kafka
- Resilience4j

## Messaging
- Apache Kafka
- Transactional Outbox Pattern

## Cloud
- Microsoft Azure
- Azure App Service
- Azure SQL Database

## Containerization
- Docker

## Orchestration
- Kubernetes

## DevOps
- Azure DevOps Pipelines
- Infrastructure as Code (Bicep)

## Monitoring
- Azure Application Insights
- Azure Log Analytics

---

# Event-Driven and Reliability Features

## Kafka Event-Driven Architecture

The application publishes payment events asynchronously using Kafka.

### Flow
- Payment Request
- Save Payment
- Create Outbox Event
- Kafka Producer publishes to `payment.created`
- Kafka Consumer receives event
- Audit / downstream processing runs

## Transactional Outbox Pattern

- Payment and event are stored in the same database transaction
- Scheduler publishes pending events to Kafka
- Prevents dual-write issues between database and Kafka

## Idempotency

- Prevents duplicate payment creation
- Same transaction is processed only once
- Enables safe retry behavior

## Retry Handling

### Producer Retry
- Outbox scheduler retries failed Kafka publishes

### Consumer Retry
- Safe reprocessing supported using idempotent logic

## Circuit Breaker (Resilience4j)

- Prevents cascading failures
- Stops repeated failing calls
- Enables graceful recovery

---

# Security Architecture

This project implements JWT-based authentication and authorization using Spring Security.

## Authentication Flow

- Client sends login request
- AuthenticationController receives request
- Spring Security Authentication Manager validates credentials
- JWT token is generated
- Client stores token
- Client sends token in Authorization header
- JWTAuthenticationFilter validates token
- Access is granted to protected APIs

## Example Authorization Header

```text
Authorization: Bearer <JWT_TOKEN>
