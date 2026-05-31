pipeline {
    agent { label 'arm64' } // default agent for all steps
    environment {
        // Docker registry credentials (set in Jenkins credentials or env vars)
        DOCKER_REGISTRY_CREDENTIALS = credentials('docker-hub-token')  // set this as a secret in Jenkins credentials
        IMAGE_NAME = "rcastellanosm/eqlabs-playground-php-slim-messenger"
    }
    stages {
        stage('Setup') {
            steps {
                // Checkout a public repository (replace with actual repo URL)
                // git url: 'https://github.com/example/example-repo.git', branch: 'main'
                // Docker login – assumes credentials are stored as environment variables
                dockerLogin(DOCKER_REGISTRY_CREDENTIALS_USR, DOCKER_REGISTRY_CREDENTIALS_PSW)
            }
        }
        stage('Build & Push Images') {
            parallel {
                stage('AMD Image') {
                    agent { label 'amd64' }
                    steps {
                        dir('/home/jenkins/agent/test-context') {
                            dockerLogin(DOCKER_REGISTRY_CREDENTIALS_USR, DOCKER_REGISTRY_CREDENTIALS_PSW)

                            sh 'echo building image ${IMAGE_NAME}:amd64'
                            sh 'docker build -t ${IMAGE_NAME}:amd64 -f Dockerfile .'
                            
                            sh 'echo pushing image ${IMAGE_NAME}:amd64'
                            sh 'docker push ${IMAGE_NAME}:amd64'
                        }
                    }
                }
                stage('ARM Image') {
                    agent { label 'arm64' }
                    steps {
                        dir('/home/jenkins/agent/test-context') {
                            sh 'echo building image ${IMAGE_NAME}:arm64'
                            sh 'docker build -t ${IMAGE_NAME}:arm64 -f Dockerfile .'
                            
                            sh 'echo pushing image ${IMAGE_NAME}:arm64'
                            sh 'docker push ${IMAGE_NAME}:arm64'
                        }
                    }
                }
            }
        }
        stage('Create Manifest') {
            steps {
                // Create multi‑arch manifest and push it
                sh 'docker manifest create --amend ${IMAGE_NAME}:latest ${IMAGE_NAME}:amd64 ${IMAGE_NAME}:arm64'
                // inspect the newly created manifest 
                sh 'docker manifest inspect ${IMAGE_NAME}:latest'
                // push the manifest
                sh 'docker manifest push ${IMAGE_NAME}:latest'
                // finishing stage
                sh 'echo Manifest created'
            }
        }
    }
    post {
        always {
            // Cleanup happens per-node; nothing to do globally with agent none
            echo 'Pipeline complete for multi architecture image building'
        }
    }
}

def dockerLogin(username, token) {
    sh "echo ${token} | docker login -u ${username} --password-stdin"
}
