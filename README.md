# 🛠️ NIVE API Platform Base (Multi-Module)
<p align="right">
  <a href="README.ko.md">🇰🇷 한국어</a> |
  <strong>🇺🇸English</strong>
</p>

Spring Boot–based **API Platform Base Project**.  
This project goes beyond implementing a single service and provides  
a **multi-module architecture where authentication, authorization, logging, exception handling, and architectural rules are predefined**,  
allowing teams to start developing domain APIs immediately.

> ❗ This is **not** a framework that replaces Spring.  
> ❗ This is **not** a generic SaaS boilerplate.
>
> ✅ The goal is to provide a **practical API platform base refined through real operational experience**.

---

## 🎯 Project Goals

- Proactively solve **recurring platform-level problems** in API development
- Provide a structure where **domain developers do not need to design architecture from scratch**
- Enforce **clear roles and responsibilities at compile time** via multi-module separation
- Support evolution from a single service to **multi-service, batch, and event-driven architectures**

---

## 🧩 Tech Stack

- **Language**: OpenJDK 17
- **Framework**: Spring Boot 3.4.x
- **Persistence**: JPA + QueryDSL (with 일부 Native Queries)
- **Architecture**: REST API + Multi-Module
- **Security**: JWT-based Stateless Authentication
- **External APIs**: MailGun, Cloudflare Turnstile, NHN SMS, Google Chat Bot

---

## 🧱 Multi-Module Structure Overview

```bash
nive-platform
├── nive-common        # Shared contracts / Pure Java (no Spring dependency)
├── nive-domain        # Domain models + Repositories
├── nive-application   # UseCases / Queries / Adapters (Web, Batch...)
└── nive-web           # Spring Boot configuration / Security / Filters
```

> 📐 **Architecture Documentation**
>
> This project is built on a clearly defined multi-module architecture.
>
> To understand **why each module exists, what it owns, and how requests flow**,  
> please refer to the central architecture index:
>
> 👉 **[Architecture Overview](ARCHITECTURE.md)**
---

### Dependency Direction

```
common
  ↑
domain
  ↑
application
  ↑
web
```

- Lower modules must not depend on higher modules
- Only one-way dependencies are allowed
- Violations are blocked at compile time

---

### Why does the application layer own Controllers / Batch / Schedule?

In this project, the `application` layer is treated as  
**the central layer where actual system behaviors occur**.

HTTP requests, batch jobs, and scheduled executions are all considered  
different entry points that trigger the same UseCases.

This structure provides the following benefits:

- Centralizes business execution logic
- Prevents behavior from being scattered across Spring technical layers (e.g. Web)
- Preserves a UseCase-centric clean architecture mindset

---

### Why UseCases instead of Services?

Rather than using large, stateful Service classes,  
this project adopts a **UseCase-based structure split by individual actions**.

This approach prevents:

- Fat Service classes
- Classes with unclear responsibilities (violations of the Single Responsibility Principle)
- High coupling between unrelated functionalities

Each UseCase clearly represents a single business action,  
making the system easier to extend and reason about.

Depending on the nature of the domain or the team’s development style,  
the traditional **Controller – Service – Repository** structure may still be used.

The application structure proposed by this platform is  
a **recommended pattern designed for extensibility and maintainability**,  
and can be adjusted flexibly as needed.

---

## 📦 Module Responsibilities

### 🔹 nive-common
- Pure Java module with no dependency on Spring, Web, or JPA
- Common exception contracts (`AbstractRestException`)
- Unified API response format (`ApiResponseBody`)
- Policy Enums, Validators, and Annotations

### 🔹 nive-domain
- Domain Entities and Repositories
- Core business rules
- No dependency on web or application modules

### 🔹 nive-application
- UseCases (action-oriented business logic)
- Queries and DTOs
- Web Adapters (Controllers)
- Port interface definitions

### 🔹 nive-web
- Spring Boot configuration
- Security, Filters, and Interceptors
- application.yml management
- Concrete Adapter implementations for external configurations

---

## 🔌 Port–Adapter Strategy

- The application layer does not know where configurations come from
- Required values are requested via Ports (interfaces)
- Actual implementations are provided by Adapters in the web module

> Similar to service / serviceImpl,  
> but the primary goal is **protecting module boundaries**, not implementation swapping

---

## 🔐 Authentication / Authorization / Security

- JWT-based stateless authentication
- Filters handle authentication
- Interceptors handle role-based authorization
- EntryPoint and ControllerAdvice are separated

---

## ⚠️ Exception & Response Policy

This platform enforces a **single API response contract**  
across all layers and execution paths.

- `BusinessRestException`: Base business exception used in domain and UseCase flows
- `JwtAuthenticationException`: Exceptions occurring in authentication filters and security flows
- `JwtValidationException`: Token validation exceptions occurring inside UseCases

All exceptions raised in the application layer extend `AbstractRestException`,  
ensuring a consistent response structure.

Actual API responses are generated via `ApiResponseBody`,  
so both success and failure cases always share the same response format.

```json
{
  "code": "SUCCESS",
  "message": "Request processed successfully.",
  "data": {}
}
```

---

## 🚀 Getting Started with API Development

1. Add a domain (nive-domain)
2. Implement UseCases (nive-application)
3. Create Controllers (Adapters)
4. Configure settings only in the web module
5. Unified responses and exceptions are applied automatically

---

## 📌 What This Project Is Not

- A replacement for Spring ❌
- A generic SaaS template ❌
- A one-size-fits-all architecture ❌

## 📌 What This Project Is

- A **production-ready API platform base**
- A structure scalable at company or team level
- A personal architectural reference

---

## 🧰 Built-in Platform Features

This platform provides commonly required **platform-level features**  
for API-based services, allowing teams to focus on domain logic from day one.

### Authentication & Authorization
- JWT-based stateless authentication
- Role-based access control for user and admin scopes
- Security pipeline based on Filters and Interceptors

### User & Identity
- Base user domain model
- SMS / Email verification flows
- Redis TTL–based verification expiration and rate limiting

### Exception & Response
- Unified API response contract
- Domain-driven exception hierarchy
- Consistent validation and error-handling policies across layers

### Operations & Observability
- Health check endpoints for cloud environments
- Structured logging with MDC
- Request/response tracing and access logs
- Admin activity auditing via custom annotations
- Infrastructure-friendly default configurations

---

## 📚 Architecture Reference

This repository includes a complete set of architecture documents
describing the platform’s structure, responsibilities, and design decisions.

- [Architecture Overview](ARCHITECTURE_OVERVIEW.md)
- [Architecture Decisions](DECISIONS.md.md)

These documents serve as both:
- a reference implementation, and
- an architectural guide for future extensions.

## 📄 License

MIT License