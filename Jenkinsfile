pipeline {
    environment {
    registry = "wayapaychat-container-registry/waya-auth-service"
    registryCredential = 'DigitalOcean-registry-for-development'
    dockerImage = ''
    }
	parameters {
	    strings(name: 'FROM_BUILD' defaultValue: '', description: 'Build Source')
	}
    agent any

    tools {
        jdk 'jdk-11'
        maven 'mvn3.6.3'
    }

    stages {
        stage('compile') {
            steps {
               sh "mvn clean install"
            }
                
        }
	    
    stage('Building image') {
      steps{
        script {
          /*dockerImage = docker.build registry + ":$BUILD_NUMBER" */
	    dockerImage=docker.build registry + :latest
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
    stage ('Starting the deployment job') {
        build job: 'waya-2.0-auth-service-deploy-dev', 
		parameters: [[$class: 'StringParameterValue', name: 'FROM_BUILD', value: "${BUILD_NUMBER}"]
	        ]
    }	    
   stage('Remove Unused docker image') {
      steps{
        /* sh "docker rmi $registry:$BUILD_NUMBER" */
	   sh "docker rmi $registry:latest"
      }
    } 
     stage('Trig') {
       build job: 'waya-2.0-api-gateway-deploy-dev', propagate: true, wait: true
    }
 }

}
