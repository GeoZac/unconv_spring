pipeline {
    agent any

    triggers {
            pollSCM('* * * * *')
    }

    environment {
        APPLICATION_NAME = 'spring'
    }

    stages {
        stage('Build') {
            steps {
                sh './mvnw clean verify'
            }
        }
    }
}
