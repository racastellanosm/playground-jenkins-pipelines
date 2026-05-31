pipeline {
    agent { label 'arm64' }
    environment {
        GIT_URL = 'https://github.com/racastellanosm/playground-for-playwirght-framework'
        GIT_CREDENTIALS_ID = 'github-token'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[credentialsId: env.GIT_CREDENTIALS_ID, url: env.GIT_URL]],
                    extensions: [
                        // Set Max Depth (Shallow Clone to 1 commit)
                        [$class: 'CloneOption', depth: 1, noTags: true, shallow: true]
                    ]
                ])
            }
        }
        stage('Build Dependencies') {
            steps {
                sh 'echo Installing dependencies'
                sh 'make build'
            }
        }
        stage('Run Tests') {
            steps {
                sh 'echo Running tests'
                sh 'make run-tests'
                
                // publish html reports
                publishHTML(
                    target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: "reports",
                        reportFiles: 'index.html',
                        reportName: 'Playwright Report',
                        includes: '**/*',
                        escapeUnderscores: true
                    ]
                )
            }
        }
    }
}
