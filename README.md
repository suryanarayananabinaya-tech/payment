# Payment Microservice – Cloud Native Spring Boot Application

Java | Spring Boot | REST API | JWT Security | Azure | Kubernetes | Docker | CI/CD | Design Patterns

---

# Overview

This repository contains a **cloud-native Payment microservice built using Spring Boot** and designed for deployment on **Microsoft Azure**.

The project demonstrates how modern backend services are developed using **clean architecture, secure API authentication, containerization, cloud infrastructure, and automated CI/CD pipelines**.

The service is designed to simulate a **production-style payment processing backend** and demonstrates how enterprise systems are structured and deployed.

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

This project implements **JWT-based authentication and authorization using Spring Security**.

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
