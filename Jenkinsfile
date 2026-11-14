pipeline {

    agent any

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['staging', 'production', 'both'],
            description: 'Select the deployment environment'
        )
    }

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
            when {
                branch 'main'
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ghcr-credentials',
                        usernameVariable: 'GHCR_USER',
                        passwordVariable: 'GHCR_TOKEN'
                    )
                ]){
                    sh '''
                        set -eu
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

stage('Check Railway Staging Token') {
    steps {
        withCredentials([
                string(
                    credentialsId: 'railway-staging-token',
                    variable: 'RAILWAY_TOKEN'
                )
            ]) {
                sh '''
                    set -eu
                    set +x

                    docker run --rm \
                        -e RAILWAY_TOKEN \
                        node:22-bookworm-slim \
                        sh -ec '
                            npx -y @railway/cli@latest \
                                service status \
                                --service c14-tickets-api \
                                --environment staging
                        '
                '''
            }
        }
    }

        stage('Deploy to Railway - Staging'){
            when{
                allOf{
                    branch 'main'
                    expression {
                        params.DEPLOY_ENV in ['staging', 'both']
                    }
                }
            }

            steps{
                withCredentials([
                    string(
                        credentialsId: 'railway-staging-token',
                        variable: 'RAILWAY_TOKEN'
                    )
                ]){
                    sh '''
                        set -eu
                        set +x

                        docker run --rm \
                        -e RAILWAY_TOKEN \
                        -e IMAGE_TAG="$BUILD_NUMBER" \
                        node:22-bookworm-slim \
                        sh -ec '
                            npx -y @railway/cli@latest service source connect \
                            --image "ghcr.io/c14-inatel-2026-2/c14-ticket-system:$IMAGE_TAG" \
                            --service c14-tickets-api \
                            --environment staging
                        '
                    '''
                }
            }
        }
        stage('Deploy to Railway - Production') {
        when{
                allOf{
                    branch 'main'
                    expression {
                        params.DEPLOY_ENV in ['production', 'both']
                    }
                }
        }

            steps {
                withCredentials([
                    string(
                        credentialsId: 'railway-token',
                        variable: 'RAILWAY_TOKEN'
                    )
                ]) {
                    sh '''
                        set -eu
                        set +x

                        docker run --rm \
                            -e RAILWAY_TOKEN \
                            -e IMAGE_TAG="$BUILD_NUMBER" \
                            node:22-bookworm-slim \
                            sh -ec '
                            npx -y @railway/cli@latest service source connect \
                            --image "ghcr.io/c14-inatel-2026-2/c14-ticket-system:$IMAGE_TAG" \
                            --service c14-tickets-api \
                            --environment production
                        '
                    '''
                }
            }
    }

    }
}