pipeline {
    environment {
    registry = " registry.digitalocean.com/wayapaychat-container-registry/waya-auth-service"
    registryCredential = 'DigitalOcean-registry-for-development'
    dockerImage = ''
  }
    agent any

    tools {
        jdk 'jdk-11'
        maven 'mvn3.6.3'
    }

    stages {
	    stage('Cloning Git') {
      steps {
        git 'https://github.com/WAYA-MULTI-LINK/WAYA-PAY-CHAT-2.0-AUTH-SERVICE.git'
      }
        stage('compile') {
            steps {
               sh "mvn clean install"
            }
                
        }
	    
    stage('Building image') {
      steps{
        script {
          dockerImage = docker.build registry + ":$BUILD_NUMBER"
        }
      }
    }
    stage('Deploy Image') {
      steps{
         script {
            docker.withRegistry( 'https://registry.digitalocean.com/wayapaychat-container-registry', registryCredential ) {
            dockerImage.push()
          }
        }
      }
    }
    stage('Remove Unused docker image') {
      steps{
        sh "docker rmi $registry:$BUILD_NUMBER"
      }
    }
  }
}
