# Spring Boot CI/CD Demo — Jenkins Pipeline

A production-ready Spring Boot application with a fully configured Jenkins CI/CD pipeline covering **Build**, **Test**, and **Code Coverage** stages.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        JENKINS PIPELINE                          │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ Checkout │→ │ Compile  │→ │  Tests   │→ │   Package    │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘   │
│       │              │             │               │            │
│   Git Clone     Maven Build   JUnit + JaCoCo    JAR Archive    │
│                               Coverage Gate                     │
└──────────────────────────────────────────────────────────────────┘
         ↑
   GitHub Push / Poll SCM (every 5 min)
```

---

## Project Structure

```
springboot-cicd-demo/
├── Jenkinsfile                          ← Jenkins declarative pipeline
├── pom.xml                              ← Maven build + JaCoCo config
├── src/
│   ├── main/
│   │   ├── java/com/example/app/
│   │   │   ├── Application.java         ← Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── AppController.java   ← REST endpoints
│   │   │   ├── service/
│   │   │   │   └── GreetingService.java ← Business logic
│   │   │   └── model/
│   │   │       └── ApiResponse.java     ← Response model
│   │   └── resources/
│   │       └── application.properties   ← App config + Actuator
│   └── test/
│       └── java/com/example/app/
│           └── ApplicationTests.java    ← Unit + Integration tests
└── docs/
    └── JENKINS_SETUP.md                 ← Step-by-step Jenkins guide
```

---

## Pipeline Stages

| # | Stage                  | Description                                      |
|---|------------------------|--------------------------------------------------|
| 1 | **Checkout**           | Clones the repo, prints git log                  |
| 2 | **Environment Validation** | Verifies Java, Maven, disk, POM validity     |
| 3 | **Dependencies**       | Resolves Maven dependencies                      |
| 4 | **Compile**            | `mvn compile` — fails fast on syntax errors      |
| 5 | **Unit Tests**         | Runs unit tests, publishes JUnit XML             |
| 6 | **Integration Tests**  | Runs full `mvn verify`, publishes results        |
| 7 | **Code Coverage**      | Generates JaCoCo HTML report                     |
| 8 | **Package**            | `mvn package`, archives the JAR artifact         |
| 9 | **Quality Gate**       | Enforces ≥70% line coverage (unstable if below)  |

---

## REST API Endpoints

| Method | Endpoint            | Description             |
|--------|---------------------|-------------------------|
| GET    | `/api/health`       | Application health check |
| GET    | `/api/greet/{name}` | Greeting for a given name |
| GET    | `/api/version`      | App version info         |
| GET    | `/actuator/health`  | Spring Actuator health   |
| GET    | `/actuator/info`    | App metadata             |

---

## Quick Start

### Run locally

```bash
# Clone the repo
git clone https://github.com/<your-username>/<your-repo>.git
cd springboot-cicd-demo

# Build and test
mvn clean verify

# Run the app
mvn spring-boot:run
```

App starts on **http://localhost:8080**

### Run tests only

```bash
mvn test
```

### Generate coverage report

```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

---

## Requirements

| Tool  | Version |
|-------|---------|
| Java  | 17+     |
| Maven | 3.9+    |
| Jenkins | 2.426+ |

---

## Jenkins Setup

See **[docs/JENKINS_SETUP.md](docs/JENKINS_SETUP.md)** for the full step-by-step guide including plugin installation, tool configuration, job creation, and webhook setup.

---

## Code Coverage

JaCoCo is configured to enforce a **minimum 70% line coverage** threshold.

- Report location after build: `target/site/jacoco/index.html`
- Jenkins publishes the HTML report automatically via the JaCoCo plugin
- Builds below threshold are marked **UNSTABLE** (adjustable in `pom.xml`)

---

## License

MIT License — free to use, modify, and distribute.
