pipeline {
  agent none
  stages {
    stage('Tests') {
        parallel {
            stage('Unit Tests') {
                agent { label 'arm64' }
                steps {
                    sh 'echo running unit tests'
                }
            }
            stage('Integration Tests') {
                agent { label 'arm64' }
                steps {
                    sh 'echo running integration tests'
                }
            }
            stage('Functional Tests') {
                agent { label 'arm64' }
                steps {
                    sh 'echo running functional tests'
                }
            }
        }
    }
    stage('E2E Tests - Optional') {
      agent none
      steps {
        script {
            try {
                timeout(time: 1, unit: 'MINUTES') {
                    input message: "Do you want to run the E2E tests?", ok: "Submit"
                }
                
                node('arm64') {
                    echo "Running E2E tests..."
                    sh '''
                        echo 'E2E tests started'
                        echo 'E2E tests finished'
                    '''
                }
            } catch (err) {
                echo "Skipping optional E2E tests. Moving to Build stage..."
            }
        }
      }
    }
    stage('Build Docker Images') {
        agent { label 'arm64' }
        steps {
            sh 'echo building docker images'
        }
    }
    stage('Publish to Docker Registry') {
        agent { label 'arm64' }
        steps {
            sh 'echo pushing docker images'
        }
    }
  }
}