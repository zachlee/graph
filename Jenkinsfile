pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh './gradlew clean build jar'
            }
        }
        stage('build and deploy container') {
            steps {
                sh 'docker info'
                sh 'docker build -t graph-repository .'
                sh 'docker tag graph-repository 438205058935.dkr.ecr.us-west-2.amazonaws.com/graph-repository:latest'
                sh 'docker push 438205058935.dkr.ecr.us-west-2.amazonaws.com/graph-repository:latest'
                sh 'docker images'
            }
        }
    }
}