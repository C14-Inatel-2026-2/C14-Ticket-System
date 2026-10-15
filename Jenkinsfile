pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean compile'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test jacoco:report'
            }

            post {
                always {
                    junit(
                        testResults: 'target/surefire-reports/*.xml',
                        allowEmptyResults: false
                    )

                    archiveArtifacts(
                        artifacts: 'target/site/jacoco/**',
                        allowEmptyArchive: false,
                        fingerprint: true
                    )

                    publishHTML(
                        target: [
                            reportDir: 'target/site/jacoco',
                            reportFiles: 'index.html',
                            reportName: 'JaCoCo Coverage Report',
                            reportTitles: 'Java Test Coverage',
                            keepAll: true,
                            alwaysLinkToLastBuild: true,
                            allowMissing: false
                        ]
                    )
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -DskipTests'
            }
        }

    }

    post {

        success {
            echo 'Build success.'
        }

        failure {
            echo 'Build failed.'
        }

    }
}