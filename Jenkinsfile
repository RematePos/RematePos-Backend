pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = credentials('docker-registry-url')
        DOCKER_CREDENTIALS = credentials('docker-registry-credentials')
        SONAR_HOST_URL = credentials('sonar-host-url')
        SONAR_LOGIN = credentials('sonar-token')
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        MAVEN_SETTINGS = '/var/jenkins_home/settings.xml'
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'qa', 'release', 'main'], description: 'Target environment')
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip unit tests')
        booleanParam(name: 'DEPLOY', defaultValue: true, description: 'Deploy after build')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    env.GIT_COMMIT_SHORT = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    sh '''
                        echo "===== GIT INFO ====="
                        echo "Branch: ${GIT_BRANCH}"
                        echo "Commit: ${GIT_COMMIT_SHORT}"
                        echo "Tag: ${DOCKER_IMAGE_TAG}"
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh '''
                        echo "===== BUILDING PROJECT ====="
                        ./mvnw clean package \
                            -s ${MAVEN_SETTINGS} \
                            -DskipTests=${SKIP_TESTS} \
                            -Dmaven.test.skip=${SKIP_TESTS} \
                            -U
                    '''
                }
            }
        }

        stage('Unit Tests') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                script {
                    sh '''
                        echo "===== RUNNING UNIT TESTS ====="
                        ./mvnw test \
                            -s ${MAVEN_SETTINGS} \
                            -Dmaven.test.failure.ignore=true
                    '''
                }
                junit '**/target/surefire-reports/TEST-*.xml'
            }
        }

        stage('Code Quality') {
            when {
                expression { GIT_BRANCH == 'develop' || GIT_BRANCH == 'main' }
            }
            steps {
                script {
                    sh '''
                        echo "===== SONARQUBE ANALYSIS ====="
                        ./mvnw sonar:sonar \
                            -Dsonar.projectKey=microservice-pos \
                            -Dsonar.projectName="Microservice POS" \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_LOGIN} \
                            -Dsonar.qualitygate.wait=true \
                            -Dsonar.qualitygate.timeout=300
                    '''
                }
            }
        }

        stage('Build Docker Images') {
            when {
                expression { params.DEPLOY }
            }
            steps {
                script {
                    sh '''
                        echo "===== BUILDING DOCKER IMAGES ====="
                        docker login -u _json_key --password-stdin ${DOCKER_REGISTRY} < ${DOCKER_CREDENTIALS}

                        # Build config-server
                        docker build -t ${DOCKER_REGISTRY}/microservice-pos/config-server:${DOCKER_IMAGE_TAG} config-server/
                        docker tag ${DOCKER_REGISTRY}/microservice-pos/config-server:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/microservice-pos/config-server:latest

                        # Build discovery-server
                        docker build -t ${DOCKER_REGISTRY}/microservice-pos/discovery-server:${DOCKER_IMAGE_TAG} discovery-server/
                        docker tag ${DOCKER_REGISTRY}/microservice-pos/discovery-server:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/microservice-pos/discovery-server:latest

                        # Build customer-microservice
                        docker build -t ${DOCKER_REGISTRY}/microservice-pos/customer-microservice:${DOCKER_IMAGE_TAG} microservices/customer-microservice/
                        docker tag ${DOCKER_REGISTRY}/microservice-pos/customer-microservice:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/microservice-pos/customer-microservice:latest

                        # Build product-microservice
                        docker build -t ${DOCKER_REGISTRY}/microservice-pos/product-microservice:${DOCKER_IMAGE_TAG} microservices/product-microservice/
                        docker tag ${DOCKER_REGISTRY}/microservice-pos/product-microservice:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/microservice-pos/product-microservice:latest

                        # Build cart-microservice
                        docker build -t ${DOCKER_REGISTRY}/microservice-pos/cart-microservice:${DOCKER_IMAGE_TAG} microservices/cart-microservice/
                        docker tag ${DOCKER_REGISTRY}/microservice-pos/cart-microservice:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/microservice-pos/cart-microservice:latest
                    '''
                }
            }
        }

        stage('Push Docker Images') {
            when {
                expression { params.DEPLOY }
            }
            steps {
                script {
                    sh '''
                        echo "===== PUSHING DOCKER IMAGES ====="
                        docker push ${DOCKER_REGISTRY}/microservice-pos/config-server:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/microservice-pos/config-server:latest
                        docker push ${DOCKER_REGISTRY}/microservice-pos/discovery-server:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/microservice-pos/discovery-server:latest
                        docker push ${DOCKER_REGISTRY}/microservice-pos/customer-microservice:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/microservice-pos/customer-microservice:latest
                        docker push ${DOCKER_REGISTRY}/microservice-pos/product-microservice:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/microservice-pos/product-microservice:latest
                        docker push ${DOCKER_REGISTRY}/microservice-pos/cart-microservice:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/microservice-pos/cart-microservice:latest

                        docker logout ${DOCKER_REGISTRY}
                    '''
                }
            }
        }

        stage('Deploy to QA') {
            when {
                expression { GIT_BRANCH == 'develop' && params.DEPLOY }
            }
            steps {
                script {
                    sh '''
                        echo "===== DEPLOYING TO QA ====="
                        docker compose -p pos-qa \
                          -f infra/docker/compose/docker-compose.yml \
                          -f infra/docker/compose/docker-compose.qa.yml \
                          --env-file infra/docker/env/.env.qa pull
                        docker compose -p pos-qa \
                          -f infra/docker/compose/docker-compose.yml \
                          -f infra/docker/compose/docker-compose.qa.yml \
                          --env-file infra/docker/env/.env.qa up -d

                        sleep 10
                        bash scripts/smoke-endpoints.sh qa
                        echo "✅ QA deployment successful"
                    '''
                }
            }
        }

        stage('Deploy to Release') {
            when {
                expression { GIT_BRANCH == 'release' && params.DEPLOY }
            }
            steps {
                input(message: 'Deploy to Release environment?', ok: 'Deploy')
                script {
                    sh '''
                        echo "===== DEPLOYING TO RELEASE ====="
                        docker compose -p pos-release \
                          -f infra/docker/compose/docker-compose.yml \
                          -f infra/docker/compose/docker-compose.release.yml \
                          --env-file infra/docker/env/.env.release pull
                        docker compose -p pos-release \
                          -f infra/docker/compose/docker-compose.yml \
                          -f infra/docker/compose/docker-compose.release.yml \
                          --env-file infra/docker/env/.env.release up -d

                        sleep 10
                        bash scripts/smoke-endpoints.sh release
                        echo "✅ Release deployment successful"
                    '''
                }
            }
        }

        stage('Deploy to Main') {
            when {
                expression { GIT_BRANCH == 'main' && params.DEPLOY }
            }
            steps {
                input(message: 'Deploy to PRODUCTION?', ok: 'Deploy', submitter: 'admins')
                script {
                    sh '''
                        echo "===== DEPLOYING TO PRODUCTION ====="
                        docker compose -p pos-main \
                          -f infra/docker/compose/docker-compose.yml \
                          -f infra/docker/compose/docker-compose.main.yml \
                          --env-file infra/docker/env/.env.main pull
                        docker compose -p pos-main \
                          -f infra/docker/compose/docker-compose.yml \
                          -f infra/docker/compose/docker-compose.main.yml \
                          --env-file infra/docker/env/.env.main up -d

                        sleep 15
                        bash scripts/smoke-endpoints.sh main
                        echo "✅ Production deployment successful"
                    '''
                }
            }
        }
    }

    post {
        always {
            // Clean workspace
            cleanWs()

            // Archive test results
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'

            // Archive coverage reports
            publishHTML([
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ])
        }

        success {
            script {
                sh '''
                    echo "✅ BUILD SUCCESS"
                    echo "Image: ${DOCKER_IMAGE_TAG}"
                    echo "Branch: ${GIT_BRANCH}"
                '''
            }
        }

        failure {
            script {
                sh '''
                    echo "❌ BUILD FAILED"
                    echo "Check logs for details"
                '''
            }
        }
    }
}

