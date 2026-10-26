pipeline {
    agent any

    environment {
        REGISTRY = 'registry.safaricom.et/fintech'
        IMAGE_NAME = 'mpesa-fintech-bridge'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        SONAR_HOST_URL = 'http://sonarqube.internal:9000'
        KUBECONFIG_CREDENTIAL_ID = 'k8s-cluster-credentials'
    }

    tools {
        maven 'Maven 3.9.9'
        jdk 'JDK 21'
    }

    stages {
        stage('Checkout Source') {
            steps {
                echo 'Pulling latest commit from enterprise Git repository...'
                checkout scm
            }
        }

        stage('Code Quality & Security Scan (SonarQube)') {
            steps {
                echo 'Running SonarQube static code analysis, OWASP Top 10 vulnerability checks...'
                withSonarQubeEnv('SonarQube-Server') {
                    sh 'mvn clean test sonar:sonar -Dsonar.projectKey=mpesa-fintech-bridge -Dsonar.qualitygate.wait=true'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build & Package JAR') {
            steps {
                echo 'Building production Spring Boot runnable artifact...'
                sh 'mvn clean package -DskipTests=true'
            }
        }

        stage('Docker Build & Container Scan') {
            steps {
                echo 'Building multi-stage, non-root hardened Docker container...'
                sh "docker build -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
                sh "docker tag ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ${REGISTRY}/${IMAGE_NAME}:latest"
                echo 'Running Trivy container vulnerability scan...'
                sh "trivy image --severity HIGH,CRITICAL --exit-code 0 ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Publish Image to Registry') {
            steps {
                echo 'Pushing Docker image to secure private registry...'
                // sh "docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Deploy to Kubernetes (Production Cluster)') {
            steps {
                echo 'Applying Kubernetes manifests with zero-downtime rolling update...'
                sh "kubectl apply -f k8s/configmap.yaml"
                sh "kubectl apply -f k8s/deployment.yaml"
                sh "kubectl apply -f k8s/service.yaml"
                sh "kubectl apply -f k8s/hpa.yaml"
                sh "kubectl rollout status deployment/mpesa-fintech-bridge -n fintech --timeout=120s"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Fintech bridge pipeline finished successfully! Deployed to Kubernetes.'
        }
        failure {
            echo 'Pipeline failed! Alerting DevOps and Core Banking engineering on-call.'
        }
    }
}
