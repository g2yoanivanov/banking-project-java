# Banking App - DevOps Project 🚀

![CI/CD Pipeline](https://github.com/g2yoan/banking-project-java/actions/workflows/pipeline.yml/badge.svg)
![Docker Pulls](https://img.shields.io/docker/pulls/g2yoan/banking-app)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

A robust Client-Server Banking Application transformed into a modern, containerized, and secure microservice using DevOps best practices. This project demonstrates the implementation of **CI/CD**, **Containerization**, **Infrastructure as Code (Kubernetes)**, and a **Deep Dive into Security (DevSecOps)**.

---

## 📑 Table of Contents
- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Deep Dive: Security (DevSecOps)](#-deep-dive-security-devsecops)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Infrastructure (Kubernetes)](#-infrastructure-kubernetes)
- [How to Run](#-how-to-run)

---

## Project Overview

The core application is a Java-based banking system where multiple clients can connect to a central server via TCP sockets to perform transactions (Login, Check Balance, Deposit).

**Key Technologies:**
* **Language:** Java 17 (Maven)
* **Containerization:** Docker & Docker Hub
* **Orchestration:** Kubernetes (Local Cluster / Minikube)
* **Automation:** GitHub Actions
* **Security:** Trivy (Vulnerability Scanner)
* **Base Image:** Eclipse Temurin (Alpine Linux)

---

## Architecture

1.  **Client:** Java console application connecting via Sockets.
2.  **Server:** Containerized Java application running in a Kubernetes Pod.
3.  **Network:** The server is exposed via a **NodePort Service (30007)**, forwarding traffic to the container port **6262**.

---

## 🔒 Deep Dive: Security (DevSecOps)

The main focus of this project is the **"Shift-Left Security"** approach, ensuring that no vulnerable code reaches production.

### 1. Security Gate Implementation
We integrated **Trivy Vulnerability Scanner** into the GitHub Actions pipeline.
* **Policy:** The build is configured to **FAIL** (`exit-code: 1`) if any `CRITICAL` or `HIGH` severity vulnerabilities are detected.
* **Scan Target:** The Docker image (`g2yoan/banking-app:test`) is scanned before being pushed to the registry.

### 2. The Vulnerability & The Fix
* **Detection:** During development, the pipeline blocked a build due to High-severity CVEs (e.g., `CVE-2025-64720`) found in the base Linux libraries (`libpng`, `apk-tools`).
* **Remediation:** We patched the `Dockerfile` by adding an upgrade command to the lightweight Alpine base image:
    ```dockerfile
    RUN apk update && apk upgrade --no-cache
    ```
* **Result:** The pipeline is now Green (Passing) and the production image is secure.

---

## ⚙️ CI/CD Pipeline

We use **GitHub Actions** to automate the software delivery lifecycle.

**Workflow Steps:**
1.  **Checkout Code:** Pulls the latest code from the repository.
2.  **Java Setup:** Installs JDK 17 and caches Maven dependencies.
3.  **Build & Test:** Compiles the code (`mvn clean package`).
4.  **Docker Build (Test):** Builds a temporary image for scanning.
5.  **Security Scan:** Runs Trivy. If vulnerabilities are found -> **STOP**.
6.  **Push to Registry:** If secure, tags the image and pushes it to [Docker Hub](https://hub.docker.com/r/g2yoan/banking-app).

---

## ⚓ Infrastructure (Kubernetes)

The application is deployed using declarative **Infrastructure as Code (IaC)** YAML files.

### Deployment (`k8s/deployment.yaml`)
* **Replicas:** 2 (High Availability).
* **Resources:** Limits CPU and RAM to prevent leaks.
* **Strategy:** Rolling Updates.

### Service (`k8s/service.yaml`)
* **Type:** NodePort.
* **Port:** Exposes the application on port **30007** for external access.

---

## 🚀 How to Run

### Prerequisites
* Docker Desktop (with Kubernetes enabled).
* Java 17 (for the Client).

### 1. Deploy to Kubernetes
Open a terminal in the project root and run:

```bash
# Apply configurations
kubectl apply -f k8s/

# Verify deployment
kubectl get all
