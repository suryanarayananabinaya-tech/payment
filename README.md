# Payment Microservice – Cloud Native Spring Boot Application

Java | Spring Boot | REST API | JWT Security | Azure | Kubernetes | Docker | CI/CD | Design Patterns

---

# Overview

This repository contains a cloud-native Payment microservice built using Spring Boot and designed for deployment on Microsoft Azure.

The project demonstrates how modern backend services are developed using clean architecture, secure API authentication, containerization, cloud infrastructure, and automated CI/CD pipelines.

The service simulates a production-style payment processing backend and demonstrates how enterprise systems are structured, secured, and deployed.

---

# Architecture

Client Application  
↓  
Spring Boot REST API  
↓  
Controller Layer  
↓  
Service Layer  
↓  
Business Logic (Design Patterns)  
↓  
Repository Layer  
↓  
Database  

Deployment Flow

Developer  
↓  
Git Repository  
↓  
Azure DevOps CI/CD Pipeline  
↓  
Docker Image Build  
↓  
Azure Infrastructure Deployment (Bicep)  
↓  
Application Deployment to Azure App Service / Kubernetes

---

# Tech Stack

Backend

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT Authentication

Cloud

- Microsoft Azure
- Azure App Service

Containerization

- Docker

Orchestration

- Kubernetes

DevOps

- Azure DevOps Pipelines
- Infrastructure as Code (Bicep)

Monitoring

- Azure Application Insights
- Azure Log Analytics

---

# Security Architecture

This project implements JWT-based authentication and authorization using Spring Security.

Authentication Flow

Client Login Request  
↓  
AuthenticationController  
↓  
Spring Security Authentication Manager  
↓  
JWT Token Generated  
↓  
Client Stores Token  
↓  
Client Sends Token in Authorization Header  
↓  
JWTAuthenticationFilter Validates Token  
↓  
Access Granted to Protected APIs

Example Authorization Header

Authorization: Bearer <JWT_TOKEN>

---

# Security Components

SecurityConfig.java  
Configures Spring Security filters and endpoint protection.

JWTAuthenticationFilter.java  
Intercepts incoming requests and validates JWT tokens.

JwtConfig.java  
Defines JWT properties such as secret key and expiration.

CustomUserDetailsService.java  
Loads user details for authentication.

JWTUtil.java  
Utility class for generating and validating JWT tokens.

---

# API Endpoints

Authentication Endpoint

POST /auth/login

Request Example

{
  "username": "user",
  "password": "password"
}

Response Example

{
  "token": "JWT_TOKEN"
}

Protected Payment APIs

GET /payments  
POST /payments  

These endpoints require a valid JWT token.

---

# Project Structure

payment

src/main/java/com/example/payment

controller  
service  
repository  
entity  
dto  
exception  
model  
util  

Design Patterns

strategy  
factory  
decorator  
observer  
proxy  
template  

resources

application-local.properties  
application-dev.properties  

Infrastructure

infra/main.bicep  
infra/main.dev.parameters.json  

Kubernetes

k8s/deployment.yaml  
k8s/service.yaml  
k8s/namespace.yaml  
k8s/serviceaccount.yaml  
k8s/secretprovider.yaml  

DevOps

azure-pipelines-ci-cd.yaml  
azure-pipelines-infra.yaml  
azure-pipelines-identity.yaml  

Other Files

Dockerfile  
pom.xml  

---

# Design Patterns Implemented

This project demonstrates several enterprise design patterns commonly used in backend systems.

Strategy Pattern  
Used to select different payment processing algorithms.

Factory Pattern  
Creates payment processor objects dynamically.

Decorator Pattern  
Adds additional behaviors to payment processing logic.

Observer Pattern  
Handles event notifications for payment state changes.

Proxy Pattern  
Controls access to payment services.

Template Pattern  
Defines a template workflow for payment operations.

These patterns demonstrate clean architecture and extensible system design.

---

# Docker Container

The service is containerized using Docker.

Build the image

docker build -t payment-service .

Run the container

docker run -p 8080:8080 payment-service

---

# Kubernetes Deployment

Kubernetes manifests are located in the k8s directory.

Deployment includes

Namespace  
Deployment  
Service  
Service Account  
Secret Provider  

Deploy to Kubernetes

kubectl apply -f k8s/

---

# Infrastructure as Code (Azure)

Azure infrastructure is provisioned using Bicep templates.

Location

infra/main.bicep

Example deployment

az deployment sub create \
--location eastus \
--template-file infra/main.bicep

---

# Azure App Service Deployment

The application is deployed to Azure App Service for cloud hosting.

Deployment Flow

Code Commit  
↓  
Azure DevOps Pipeline  
↓  
Application Build  
↓  
Docker Image Creation  
↓  
Deployment to Azure App Service

Once deployed, the application can be accessed using the Azure App Service URL.

---

# Running Locally

Build the project

mvn clean install

Run the application

mvn spring-boot:run

Application will start at

http://localhost:8080

---

# Skills Demonstrated

This project demonstrates the following engineering skills

Java backend development  
Spring Boot REST API development  
JWT authentication and security using Spring Security  
Enterprise design patterns  
Docker containerization  
Kubernetes deployment  
Infrastructure as Code using Azure Bicep  
CI/CD automation using Azure DevOps  
Cloud-native application architecture  

---

# Why This Project Matters

Modern backend systems require scalable cloud infrastructure, automated deployments, and secure application architecture.

This project demonstrates how developers can combine backend development, cloud infrastructure, and DevOps automation to build production-ready systems.

---

# Future Improvements

Add Kafka-based event publishing for payment status updates  
Introduce asynchronous event-driven architecture  
Add distributed tracing  
Implement API rate limiting  
Integrate monitoring dashboards  
Add integration testing  

---

# Author

Abinaya Suryanarayanan

Java Backend Developer  
Spring Boot | Microservices | Azure | DevOps

GitHub  
https://github.com/suryanarayananabinaya-tech
