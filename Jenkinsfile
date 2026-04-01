pipeline {
    agent any

    // ─────────────────────────────────────────
    //  Global environment variables
    // ─────────────────────────────────────────
    environment {
        APP_NAME        = 'springboot-cicd-demo'
        JAVA_VERSION    = '17'
        MAVEN_OPTS      = '-Xmx512m -XX:+TieredCompilation -XX:TieredStopAtLevel=1'
        BUILD_TIMESTAMP = sh(script: "date '+%Y%m%d-%H%M%S'", returnStdout: true).trim()
    }

    // ─────────────────────────────────────────
    //  Tool configuration (match Jenkins setup)
    // ─────────────────────────────────────────
    tools {
        maven 'Maven-3.9'     // Name configured in Jenkins → Manage Jenkins → Tools
        jdk   'JDK-17'        // Name configured in Jenkins → Manage Jenkins → Tools
    }

    // ─────────────────────────────────────────
    //  Pipeline options
    // ─────────────────────────────────────────
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    // ─────────────────────────────────────────
    //  Trigger: poll SCM every 5 minutes
    // ─────────────────────────────────────────
    triggers {
        pollSCM('H/5 * * * *')
    }

    // ═════════════════════════════════════════
    //  STAGES
    // ═════════════════════════════════════════
    stages {

        // ── 1. Checkout ────────────────────────
        stage('Checkout') {
            steps {
                echo "╔══════════════════════════════════╗"
                echo "║  Checking out source code        ║"
                echo "╚══════════════════════════════════╝"
                checkout scm
                sh 'git log --oneline -5'
                echo "Branch   : ${env.BRANCH_NAME}"
                echo "Commit   : ${env.GIT_COMMIT?.take(8)}"
                echo "Build    : #${env.BUILD_NUMBER}"
            }
        }

        // ── 2. Environment Validation ──────────
        stage('Environment Validation') {
            steps {
                echo "Validating build environment..."
                sh '''
                    echo "=== Java Version ==="
                    java -version

                    echo "=== Maven Version ==="
                    mvn --version

                    echo "=== Disk Space ==="
                    df -h .

                    echo "=== pom.xml sanity check ==="
                    mvn help:effective-pom -q && echo "POM is valid"
                '''
            }
        }

        // ── 3. Dependency Resolution ───────────
        stage('Dependencies') {
            steps {
                echo "Resolving Maven dependencies..."
                sh 'mvn dependency:resolve -q'
            }
            post {
                success { echo "Dependencies resolved successfully." }
                failure { echo "Dependency resolution failed. Check pom.xml or Maven settings." }
            }
        }

        // ── 4. Compile ─────────────────────────
        stage('Compile') {
            steps {
                echo "Compiling source code..."
                sh 'mvn compile -B'
            }
            post {
                success { echo "Compilation successful." }
                failure {
                    echo "Compilation failed. Archiving logs..."
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                }
            }
        }

        // ── 5. Unit Tests ──────────────────────
        stage('Unit Tests') {
            steps {
                echo "Running unit tests..."
                sh '''
                    mvn test -B \
                        -Dtest="**/ApplicationTests" \
                        -Dsurefire.failIfNoSpecifiedTests=false
                '''
            }
            post {
                always {
                    // Publish JUnit test results
                    junit testResults: 'target/surefire-reports/**/*.xml',
                          allowEmptyResults: true

                    echo "Unit test results published."
                }
                failure {
                    echo "Unit tests FAILED. Review reports above."
                }
            }
        }

        // ── 6. Integration Tests ───────────────
        stage('Integration Tests') {
            steps {
                echo "Running integration tests..."
                sh '''
                    mvn verify -B \
                        -Dsurefire.failIfNoSpecifiedTests=false \
                        -DskipUnitTests=false
                '''
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/**/*.xml',
                          allowEmptyResults: true
                }
                failure {
                    echo "Integration tests FAILED."
                }
            }
        }

        // ── 7. Code Coverage ───────────────────
        stage('Code Coverage') {
            steps {
                echo "Generating JaCoCo coverage report..."
                sh 'mvn jacoco:report -B'
            }
            post {
                always {
                    // Publish JaCoCo HTML report (requires JaCoCo plugin in Jenkins)
                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : 'target/site/jacoco',
                        reportFiles          : 'index.html',
                        reportName           : 'JaCoCo Coverage Report'
                    ])
                    echo "Coverage report published."
                }
            }
        }

        // ── 8. Package ─────────────────────────
        stage('Package') {
            steps {
                echo "Packaging application as JAR..."
                sh 'mvn package -B -DskipTests'
                sh 'ls -lh target/*.jar'
            }
            post {
                success {
                    // Archive the final JAR
                    archiveArtifacts artifacts: 'target/*.jar',
                                     fingerprint: true,
                                     allowEmptyArchive: false

                    echo "JAR archived: ${APP_NAME}-${BUILD_TIMESTAMP}.jar"
                }
            }
        }

        // ── 9. Build Quality Gate ──────────────
        stage('Quality Gate') {
            steps {
                echo "Checking build quality gates..."
                script {
                    // Read JaCoCo XML to check coverage threshold
                    def coverageFile = 'target/site/jacoco/jacoco.xml'
                    if (fileExists(coverageFile)) {
                        def xml       = readFile(coverageFile)
                        def covered   = (xml =~ /type="LINE".*?covered="(\d+)"/)
                        def missed    = (xml =~ /type="LINE".*?missed="(\d+)"/)
                        if (covered && missed) {
                            def coveredLines = covered[0][1].toInteger()
                            def missedLines  = missed[0][1].toInteger()
                            def total        = coveredLines + missedLines
                            def pct          = total > 0 ? (coveredLines * 100 / total) : 0
                            echo "Line coverage: ${pct.round(1)}% (${coveredLines}/${total} lines)"
                            if (pct < 70) {
                                unstable("Coverage ${pct.round(1)}% is below the 70% threshold.")
                            }
                        }
                    } else {
                        echo "Coverage file not found — skipping gate."
                    }
                }
            }
        }

    } // end stages

    // ═════════════════════════════════════════
    //  POST-PIPELINE ACTIONS
    // ═════════════════════════════════════════
    post {
        always {
            echo "Pipeline finished. Build #${env.BUILD_NUMBER} — ${currentBuild.currentResult}"
            cleanWs()
        }
        success {
            echo """
╔══════════════════════════════════════════════╗
║  ✅  BUILD SUCCESSFUL                        ║
║  App     : ${APP_NAME}
║  Build   : #${env.BUILD_NUMBER}
║  Branch  : ${env.BRANCH_NAME}
╚══════════════════════════════════════════════╝
            """
        }
        failure {
            echo """
╔══════════════════════════════════════════════╗
║  ❌  BUILD FAILED                            ║
║  App     : ${APP_NAME}
║  Build   : #${env.BUILD_NUMBER}
║  Branch  : ${env.BRANCH_NAME}
╚══════════════════════════════════════════════╝
            """
            // Uncomment below to send email notifications:
            // mail to: 'team@example.com',
            //      subject: "BUILD FAILED: ${APP_NAME} #${env.BUILD_NUMBER}",
            //      body: "Check: ${env.BUILD_URL}"
        }
        unstable {
            echo "⚠️  Build UNSTABLE — quality gate not met. Review coverage report."
        }
    }
}
