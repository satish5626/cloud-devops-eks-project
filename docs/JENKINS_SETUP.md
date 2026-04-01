# Jenkins Setup Guide

This document describes how to configure Jenkins to run the CI/CD pipeline for this project.

---

## Prerequisites

| Requirement     | Version  |
|-----------------|----------|
| Jenkins         | 2.426+   |
| Java (Jenkins)  | 17+      |
| Maven           | 3.9+     |
| Git             | 2.x      |

---

## Step 1 — Install Required Jenkins Plugins

Go to **Manage Jenkins → Plugins → Available plugins** and install:

- **Pipeline** (workflow-aggregator)
- **Git Plugin**
- **Maven Integration Plugin**
- **JUnit Plugin**
- **JaCoCo Plugin**
- **HTML Publisher Plugin**
- **Timestamper**
- **Workspace Cleanup Plugin**
- **Build Discarder (Log Rotator)**

Restart Jenkins after installation.

---

## Step 2 — Configure JDK and Maven

Go to **Manage Jenkins → Tools**:

### JDK
- Click **Add JDK**
- Name: `JDK-17`
- Install automatically from adoptium.net, or set `JAVA_HOME` manually

### Maven
- Click **Add Maven**
- Name: `Maven-3.9`
- Install automatically, or point to an existing Maven installation

> The names **must** match what is declared in the `Jenkinsfile` `tools {}` block.

---

## Step 3 — Create the Jenkins Pipeline Job

1. From the Jenkins dashboard, click **New Item**
2. Enter a name (e.g., `springboot-cicd-demo`)
3. Select **Pipeline** and click **OK**

### Configure the Pipeline

Under **Pipeline → Definition**, select **Pipeline script from SCM**:

| Field             | Value                                  |
|-------------------|----------------------------------------|
| SCM               | Git                                    |
| Repository URL    | `https://github.com/<your-username>/<your-repo>.git` |
| Credentials       | Add your GitHub credentials if private |
| Branch Specifier  | `*/main`                               |
| Script Path       | `Jenkinsfile`                          |

Click **Save**.

---

## Step 4 — Run the Pipeline

Click **Build Now** to trigger the first run.

The pipeline will execute these stages in order:

```
Checkout → Environment Validation → Dependencies →
Compile → Unit Tests → Integration Tests →
Code Coverage → Package → Quality Gate
```

---

## Step 5 — View Reports

After a successful build:

| Report          | Location in Jenkins UI                         |
|-----------------|------------------------------------------------|
| Test Results    | Build → Test Results                           |
| Coverage Report | Build → JaCoCo Coverage Report (HTML)          |
| Archived JAR    | Build → Build Artifacts → `target/*.jar`       |

---

## Webhook (Optional — Auto-trigger on Push)

To trigger builds automatically on every GitHub push:

1. In Jenkins job → **Configure → Build Triggers** → check **GitHub hook trigger for GITScm polling**
2. In GitHub repo → **Settings → Webhooks → Add webhook**
   - Payload URL: `http://<your-jenkins-url>/github-webhook/`
   - Content type: `application/json`
   - Events: **Just the push event**

---

## Environment Variables Used in Jenkinsfile

| Variable         | Description                      |
|------------------|----------------------------------|
| `APP_NAME`       | Application name label           |
| `JAVA_VERSION`   | Java version reference           |
| `MAVEN_OPTS`     | JVM flags for Maven              |
| `BUILD_TIMESTAMP`| Timestamp for artifact naming    |

---

## Quality Gate

The pipeline enforces a **minimum 70% line coverage** threshold via JaCoCo.  
Builds below this threshold are marked **UNSTABLE** (not failed).  
Adjust the threshold in `pom.xml` under the `jacoco-maven-plugin` configuration.
