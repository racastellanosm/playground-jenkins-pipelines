pipeline {
    agent { label 'arm64' } // default agent for all steps
    environment {
        // Docker registry credentials (set in Jenkins credentials or env vars)
        DOCKER_REGISTRY_CREDENTIALS = credentials('docker-hub-token')  // set this as a secret in Jenkins credentials
        // Github credentials id (set in Jenkins credentials or env vars)
        GITHUB_CREDENTIALS_ID = 'github-token'
        // Github repository URL (set in Jenkins credentials or env vars)
        GIT_URL = 'https://github.com/equationlabs/eqlabs-playground-php-slim-messenger'
        // Image name (set in Jenkins credentials or env vars)
        IMAGE_NAME = "rcastellanosm/eqlabs-playground-php-slim-messenger"
    }
    stages {
        stage('Setup') {
            steps {
                // Checkout public repository from Github using credentials already defined in the Jenkins credentials store
                // clone only main branch.
                echo "Cloning ${env.GIT_URL} to ${env.WORKSPACE}"
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[credentialsId: env.GITHUB_CREDENTIALS_ID, url: env.GIT_URL]],
                    extensions: [
                        // Set Max Depth (Shallow Clone to 1 commit)
                        [$class: 'CloneOption', depth: 1, noTags: true, shallow: true]
                    ]
                ])

                // Docker login – assumes credentials are stored as environment variables
                dockerLogin(DOCKER_REGISTRY_CREDENTIALS_USR, DOCKER_REGISTRY_CREDENTIALS_PSW)
                // stash contents for AMD64 step to avoid re-cloning 
                stash includes: '**/*', name: 'application-source'
            }
        }
        stage('Dependencies') {
            steps {
                // Build project dependencies – adjust command to your build system
                sh 'echo building project dependencies'
            }
        }
        stage('Security & Quality') {
            parallel {
                stage('Security') {
                    steps {
                        sh 'echo running security tests'
                    }
                }
                stage('Quality') {
                    steps {
                        sh 'echo running quality tests'
                    }
                }
            }
        }
        stage('Tests') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'echo running unit tests'
                    }
                }
                stage('Integration Tests') {
                    steps {
                        sh 'echo running integration tests'
                    }
                }
                stage('Functional Tests') {
                    steps {
                        sh 'echo running functional tests'
                    }
                }
            }
        }
        stage('Build & Push Docker Images') {
            parallel {
                stage('AMD Image') {
                    agent { label 'amd64' }
                    steps {
                        // unstash contents for AMD64 step saved in 'Setup' stage
                        unstash 'application-source'
                        // docker login - assumes credentials are set as environment variables
                        dockerLogin(DOCKER_REGISTRY_CREDENTIALS_USR, DOCKER_REGISTRY_CREDENTIALS_PSW)
                        // build image 
                        sh 'echo building image ${IMAGE_NAME}:amd64'
                        sh 'docker build -t ${IMAGE_NAME}:amd64 -f Dockerfile .'
                        
                        sh 'echo pushing image ${IMAGE_NAME}:amd64'
                        sh 'docker push ${IMAGE_NAME}:amd64'
                    }
                }
                stage('ARM Image') {
                    steps {
                        sh 'echo building image ${IMAGE_NAME}:arm64'
                        sh 'docker build -t ${IMAGE_NAME}:arm64 -f Dockerfile .'
                        
                        sh 'echo pushing image ${IMAGE_NAME}:arm64'
                        sh 'docker push ${IMAGE_NAME}:arm64'
                    }
                }
            }
        }
        stage('Create Docker Multi-Architecture Manifest') {
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
