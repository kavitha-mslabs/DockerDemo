pipeline {
  agent any
  stages {
    stage('Bulid') {
      parallel {
        stage('Bulid') {
          steps {
            echo 'Building'
          }
        }

        stage('Test') {
          steps {
            echo 'Testing'
          }
        }

        stage('Deploy') {
          steps {
            echo 'Deploying'
          }
        }

      }
    }

  }
}