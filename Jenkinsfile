pipeline {
    environment {
    registry = "wayapaychat-container-registry/waya-auth-service"
    registryCredential = 'DigitalOcean-registry-for-development'
    dockerImage = ''
    }
      /*	parameters {
	    strings(name: 'FROM_BUILD' defaultValue: '', description: 'Build Source')
	} */
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
	    
	stage('Code Quality Check via SonarQube') {

             steps {
                 script {
                     def scannerHome = tool 'Jenkins-sonar-scanner';
                     withSonarQubeEnv("Jenkins-sonar-scanner") {
                     sh "${tool("Jenkins-sonar-scanner")}/bin/sonar-scanner \
		     -Dsonar.projectName=waya-auth-service \
	             -Dsonar.projectKey=waya-auth-service \
	             -Dsonar.sources=/var/jenkins_home/workspace/waya-2.0-auth-service-dev \
		     -Dsonar.projectBaseDir=/var/jenkins_home/workspace/waya-2.0-auth-service-dev \
                     -Dsonar.sources=. \
		     -Dsonar.projectVersion=1.0 \
                     -Dsonar.language=java \
                     -Dsonar.java.binaries=/var/jenkins_home/workspace/waya-2.0-auth-service-dev/target/classes \
                     -Dsonar.sourceEncoding=UTF-8 \
                     -Dsonar.test.inclusions=/var/jenkins_home/workspace/waya-2.0-auth-service-dev/src \
		     -Dsonar.junit.reportsPath=/var/jenkins_home/workspace/waya-2.0-auth-service-dev/target/surefire-reports \
                     -Dsonar.surefire.reportsPath=/var/jenkins_home/workspace/waya-2.0-auth-service-dev/target/surefire-reports \
                     -Dsonar.jacoco.reportPath=/var/jenkins_home/workspace/waya-2.0-auth-service-dev/target/coverage-reports/jacoco-unit.exec \
                     -Dsonar.java.coveragePlugin=/var/jenkins_home/workspace/waya-2.0-auth-service-dev/target/jacoco  \
		     -Dsonar.host.url=https://sonarqube.waya-pay.com \
		     -Dsonar.verbose=true "
               }
           }
       }
   }

	    
    stage('Building image') {
      steps{
        script {
          /*dockerImage = docker.build registry + ":$BUILD_NUMBER" */
	    dockerImage=docker.build registry
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
	    steps {
                build job: 'waya-2.0-auth-service-deploy-dev', 
		parameters: [[$class: 'StringParameterValue', name: 'FROM_BUILD', value: "${BUILD_NUMBER}"]
	        ]
	    }	    
    }	  
       
   stage('Remove Unused docker image') {
      steps{
         /* sh "docker rmi $registry:$BUILD_NUMBER" */
	   sh "docker rmi $registry"
      }
    } 
    /* stage('Trig') {
       build job: 'waya-2.0-api-gateway-deploy-dev', propagate: true, wait: true
    } */
 }

}
