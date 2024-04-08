pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps{
                git branch: 'main',
                    url: 'https://github.com/Sergistan/producer.git'
                }
        }
       stage('Build docker image') {
            steps{
                 sh 'docker build -t KafkaProducer-for-SMA .'
                }
         }
    }
}