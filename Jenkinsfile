pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh './gradlew clean build jar'
                sh 'pwd'
                sh 'ls'
                sh 'docker info'
                sh 'docker build -t graph .'
                sh 'docker tag graph 54.184.253.228:5000/graph:${BUILD_NUMBER}'
                sh 'docker push 54.184.253.228:5000/graph'
                sh 'docker images'
            }
        }
        stage('deploy') {
            steps {
                sh 'packer build -var 'aws_access_key=AKIAJGSHQ7KZQBQQDKOA' -var 'aws_secret_key=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' -var 'jenkins_build_number=${BUILD_NUMBER}' ./infrastructure/packer/build.json'
            }
        }
    }
}