# Payment Microservice – Spring Boot Cloud Native Application

Java | Spring Boot | Microservices | Azure | Kubernetes | Docker | CI/CD | Design Patterns

---

# Overview

This repository contains a cloud-native Payment microservice built using Spring Boot and designed for deployment in a modern cloud environment using Kubernetes and Microsoft Azure infrastructure.

The project demonstrates how enterprise backend services are built using clean architecture, design patterns, containerization, infrastructure as code, and automated CI/CD pipelines.

This project simulates a real production-ready payment processing service that can run locally or be deployed to cloud infrastructure.

---

# Architecture

Client Request
↓
REST API (Spring Boot)
↓
Controller Layer
↓
Service Layer
↓
Business Logic & Design Patterns
↓
Repository Layer
↓
Database

Deployment Architecture

Developer
↓
Git Repository
↓
Azure DevOps CI/CD Pipeline
↓
Docker Image Build
↓
Kubernetes Deployment
↓
Azure Infrastructure Provisioned via Bicep

---

# Tech Stack

Backend
- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA

Cloud
- Microsoft Azure

Containerization
- Docker

Orchestration
- Kubernetes

DevOps
- Azure DevOps Pipelines
- Infrastructure as Code (Bicep)

Monitoring
- Azure Application Insights
- Log Analytics

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
Used for selecting different payment processing algorithms.

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

# CI/CD Pipeline

The project includes Azure DevOps pipeline configurations for automated build and deployment.

Pipeline files

azure-pipelines-ci-cd.yaml  
azure-pipelines-infra.yaml  
azure-pipelines-identity.yaml  

Pipeline workflow

1 Build Spring Boot application  
2 Run tests  
3 Build Docker image  
4 Push image to container registry  
5 Deploy infrastructure using Bicep  
6 Deploy application to Kubernetes  

---

# Docker Container

The service is containerized using Docker.

Build Docker image

docker build -t payment-service .

Run container

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
Spring Boot microservices  
REST API design  
Enterprise design patterns  
Docker containerization  
Kubernetes deployment  
Infrastructure as Code using Azure Bicep  
CI/CD automation using Azure DevOps  
Cloud-native application architecture  

---

# Why This Project Matters

Modern backend systems require scalable cloud infrastructure, automated deployments, and clean application architecture.

This project demonstrates how developers can combine backend development, cloud infrastructure, and DevOps automation to build production-ready systems.

---

# Future Improvements

Add distributed tracing  
Implement event-driven architecture using Kafka  
Add API authentication using JWT  
Integrate monitoring dashboards  
Add integration testing  

---

# Author

Abinaya Suryanarayanan

Java Backend Developer  
Spring Boot | Microservices | Azure | DevOps

GitHub  
https://github.com/suryanarayananabinaya-tech
