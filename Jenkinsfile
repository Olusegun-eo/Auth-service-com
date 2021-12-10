pipeline {
    agent any
    environment {
        AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
        AWS_DEFAULT_REGION = 'eu-west-2'
        CLUSTER_NAME = 'WAYA-PROD-ENV-k8s'
        REGISTRY = '863852973330.dkr.ecr.eu-west-2.amazonaws.com'
        REPO_NAME = 'auth-service'
        SERVICE_NAME = 'auth-service'
    }


    stages{
        stage("build") {
            steps{
                script {
                    sh 'mvn clean install package -DskipTests'
                    echo 'Build with Maven'
                }
            }   
        }
        stage("Image Build") {
            steps{
                script {
                    dockerImage = docker.build "${REGISTRY}/${REPO_NAME}:${SERVICE_NAME}"
                }
            }   
        }
        stage("ECR") {
            steps{
                script {
                    sh "aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin ${REGISTRY}"
                }
            }   
        }

        stage("pushing to ECR") {
            steps{
                script {
                    sh "docker push ${REGISTRY}/${REPO_NAME}:${SERVICE_NAME}"
                }
            }   
        }
        stage("Deploy to EKS cluster") {
            steps{
                script {
                    sh '''
                        aws eks --region $AWS_DEFAULT_REGION update-kubeconfig --name $CLUSTER_NAME
                        kubectl apply -f staging.yaml --namespace=staging
                        '''
                }

            }
        }   
    }
}