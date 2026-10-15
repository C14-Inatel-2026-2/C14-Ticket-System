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
        stage ('Build and Push Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ghcr-credentials',
                        usernameVariable: 'GHCR_USER',
                        passwordVariable: 'GHCR_TOKEN'
                    )
                ]){
                    sh '''
                        set +x
                        trap 'docker logout ghcr.io >/dev/null 2>&1 || true' EXIT
                        echo "$GHCR_TOKEN" | docker login ghcr.io \
                        -u "$GHCR_USER" --password-stdin

                        docker build \
                            -t ghcr.io/c14-inatel-2026-2/c14-ticket-system:${BUILD_NUMBER} \
                            -t ghcr.io/c14-inatel-2026-2/c14-ticket-system:latest .

                        docker push ghcr.io/c14-inatel-2026-2/c14-ticket-system:${BUILD_NUMBER}
                        docker push ghcr.io/c14-inatel-2026-2/c14-ticket-system:latest
                    '''
                }
            }
        }
        stage ('Deploy') {
            steps {
                sh '''
                    docker compose pull app
                    docker compose up -d --no-deps app
                '''
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