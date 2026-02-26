// =============================================================================
// Pipeline de Entrega Continua (CD) - Sistema de Facturación Electrónica
// =============================================================================
// Herramienta: Jenkins (Declarative Pipeline)
//
// Estrategia de ramas:
//   dev  → Despliegue automático en entorno de Desarrollo
//   uat  → Despliegue automático en entorno UAT/Staging
//   prd  → Despliegue en Producción con aprobación manual
//
// Stages:
//   1. Clonar Repositorio
//   2. Determinar Entorno
//   3. Construir Imágenes Docker (paralelo)
//   4. Publicar Imágenes en DockerHub
//   5. Desplegar (condicional por rama)
// =============================================================================

pipeline {
    agent any

    // -------------------------------------------------------------------------
    // Variables de entorno globales del pipeline
    // -------------------------------------------------------------------------
    environment {
        DOCKER_REGISTRY   = 'docker.io'
        IMAGE_PREFIX      = 'invoice-system'
        IMAGE_TAG         = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'latest'}"
        DOCKER_CREDENTIALS = credentials('dockerhub-credentials')
    }

    // -------------------------------------------------------------------------
    // Opciones de comportamiento del pipeline
    // -------------------------------------------------------------------------
    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    // =========================================================================
    // STAGES DEL PIPELINE
    // =========================================================================
    stages {

        // ---------------------------------------------------------------------
        // Stage 1: Clonar el repositorio
        // ---------------------------------------------------------------------
        stage('Clonar Repositorio') {
            steps {
                echo "========================================"
                echo " Clonando repositorio..."
                echo " Rama: ${env.BRANCH_NAME}"
                echo " Build #${env.BUILD_NUMBER}"
                echo "========================================"
                checkout scm
                sh 'git log --oneline -5'
            }
        }

        // ---------------------------------------------------------------------
        // Stage 2: Determinar el entorno de despliegue según la rama
        // ---------------------------------------------------------------------
        stage('Determinar Entorno') {
            steps {
                script {
                    switch (env.BRANCH_NAME) {
                        case 'dev':
                            env.DEPLOY_ENV     = 'development'
                            env.ENV_LABEL      = 'DEV'
                            env.NAMESPACE      = 'invoice-dev'
                            break
                        case 'uat':
                            env.DEPLOY_ENV     = 'staging'
                            env.ENV_LABEL      = 'UAT'
                            env.NAMESPACE      = 'invoice-uat'
                            break
                        case 'prd':
                            env.DEPLOY_ENV     = 'production'
                            env.ENV_LABEL      = 'PRD'
                            env.NAMESPACE      = 'invoice-prd'
                            break
                        default:
                            env.DEPLOY_ENV     = 'unknown'
                            env.ENV_LABEL      = 'UNKNOWN'
                            env.NAMESPACE      = 'invoice-dev'
                    }
                    echo "Entorno destino: ${env.ENV_LABEL} (${env.DEPLOY_ENV})"
                    echo "Namespace K8s: ${env.NAMESPACE}"
                    echo "Image Tag: ${env.IMAGE_TAG}"
                }
            }
        }

        // ---------------------------------------------------------------------
        // Stage 3: Construir las 4 imágenes Docker en paralelo
        // ---------------------------------------------------------------------
        stage('Construir Imágenes Docker') {
            parallel {

                stage('Build: Orchestrator (Java)') {
                    steps {
                        dir('orchestrator-service') {
                            sh """
                                echo "Building Orchestrator Service..."
                                docker build \
                                    --build-arg BUILD_NUMBER=${env.BUILD_NUMBER} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-orchestrator:${IMAGE_TAG} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-orchestrator:${env.ENV_LABEL.toLowerCase()}-latest \
                                    .
                            """
                        }
                    }
                }

                stage('Build: PDF Service (Python)') {
                    steps {
                        dir('pdf-service') {
                            sh """
                                echo "Building PDF Service..."
                                docker build \
                                    --build-arg BUILD_NUMBER=${env.BUILD_NUMBER} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-pdf:${IMAGE_TAG} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-pdf:${env.ENV_LABEL.toLowerCase()}-latest \
                                    .
                            """
                        }
                    }
                }

                stage('Build: Notification Service (Node.js)') {
                    steps {
                        dir('notification-service') {
                            sh """
                                echo "Building Notification Service..."
                                docker build \
                                    --build-arg BUILD_NUMBER=${env.BUILD_NUMBER} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-notification:${IMAGE_TAG} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-notification:${env.ENV_LABEL.toLowerCase()}-latest \
                                    .
                            """
                        }
                    }
                }

                stage('Build: Frontend (React)') {
                    steps {
                        dir('frontend') {
                            sh """
                                echo "Building Frontend..."
                                docker build \
                                    --build-arg BUILD_NUMBER=${env.BUILD_NUMBER} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-frontend:${IMAGE_TAG} \
                                    -t ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-frontend:${env.ENV_LABEL.toLowerCase()}-latest \
                                    .
                            """
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // Stage 4: Publicar imágenes en DockerHub
        // ---------------------------------------------------------------------
        stage('Publicar Imágenes en DockerHub') {
            steps {
                sh "echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"

                sh """
                    echo "Publicando imágenes con tag: ${IMAGE_TAG}"

                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-orchestrator:${IMAGE_TAG}
                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-orchestrator:${env.ENV_LABEL.toLowerCase()}-latest

                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-pdf:${IMAGE_TAG}
                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-pdf:${env.ENV_LABEL.toLowerCase()}-latest

                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-notification:${IMAGE_TAG}
                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-notification:${env.ENV_LABEL.toLowerCase()}-latest

                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-frontend:${IMAGE_TAG}
                    docker push ${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-frontend:${env.ENV_LABEL.toLowerCase()}-latest

                    echo "✅ Imágenes publicadas exitosamente"
                """
            }
        }

        // ---------------------------------------------------------------------
        // Stage 5a: Despliegue en Desarrollo (rama dev)
        // ---------------------------------------------------------------------
        stage('Desplegar en Desarrollo') {
            when {
                branch 'dev'
            }
            steps {
                echo "========================================"
                echo " Desplegando en DEV con Docker Compose"
                echo "========================================"
                sh """
                    export IMAGE_TAG=${IMAGE_TAG}
                    export DOCKER_USER=${DOCKER_CREDENTIALS_USR}
                    docker-compose -f docker-compose.yml pull
                    docker-compose -f docker-compose.yml up -d --force-recreate
                    docker-compose -f docker-compose.yml ps
                """
                echo "✅ Desplegado en Desarrollo"
            }
        }

        // ---------------------------------------------------------------------
        // Stage 5b: Despliegue en UAT/Staging (rama uat)
        // ---------------------------------------------------------------------
        stage('Desplegar en UAT') {
            when {
                branch 'uat'
            }
            steps {
                echo "========================================"
                echo " Desplegando en UAT/Staging (Kubernetes)"
                echo "========================================"
                sh """
                    echo "kubectl set image deployment/orchestrator orchestrator=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-orchestrator:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl set image deployment/pdf-service pdf-service=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-pdf:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl set image deployment/notification notification=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-notification:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl set image deployment/frontend frontend=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-frontend:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl rollout status deployment -n ${NAMESPACE}"
                """
                // En un entorno real, reemplazar los echo por los comandos kubectl reales
                echo "✅ Desplegado en UAT"
            }
        }

        // ---------------------------------------------------------------------
        // Stage 5c: Despliegue en Producción (rama prd) — requiere aprobación
        // ---------------------------------------------------------------------
        stage('Aprobación para Producción') {
            when {
                branch 'prd'
            }
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    input(
                        message: "¿Confirmar despliegue en PRODUCCIÓN? (rama: ${env.BRANCH_NAME}, tag: ${IMAGE_TAG})",
                        ok: 'Sí, desplegar en producción',
                        submitter: 'admin,devops-lead'
                    )
                }
            }
        }

        stage('Desplegar en Producción') {
            when {
                branch 'prd'
            }
            steps {
                echo "========================================"
                echo " Desplegando en PRODUCCIÓN (Kubernetes)"
                echo "========================================"
                sh """
                    echo "kubectl set image deployment/orchestrator orchestrator=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-orchestrator:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl set image deployment/pdf-service pdf-service=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-pdf:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl set image deployment/notification notification=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-notification:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl set image deployment/frontend frontend=${DOCKER_CREDENTIALS_USR}/${IMAGE_PREFIX}-frontend:${IMAGE_TAG} -n ${NAMESPACE}"
                    echo "kubectl rollout status deployment -n ${NAMESPACE} --timeout=5m"
                """
                // En un entorno real, reemplazar los echo por los comandos kubectl reales
                echo "✅ Desplegado en Producción"
            }
        }
    }

    // =========================================================================
    // POST-PIPELINE: Acciones al finalizar (éxito, fallo, siempre)
    // =========================================================================
    post {
        success {
            echo "=============================================="
            echo " Pipeline CD completado exitosamente"
            echo " Rama   : ${env.BRANCH_NAME}"
            echo " Entorno: ${env.DEPLOY_ENV}"
            echo " Tag    : ${env.IMAGE_TAG}"
            echo "=============================================="
        }
        failure {
            echo "❌ Pipeline CD falló en rama: ${env.BRANCH_NAME}"
            echo "   Revisar logs para diagnóstico"
        }
        always {
            sh "docker logout ${DOCKER_REGISTRY} || true"
            cleanWs()
        }
    }
}
