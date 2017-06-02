pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        parallel(
          "build": {
            sh './gradlew clean build'
            
          },
          "": {
            sh './gradlew test'
            
          }
        )
      }
    }
  }
}