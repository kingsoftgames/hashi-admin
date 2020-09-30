#!/usr/bin/env groovy

pipeline {
    agent {
        label 'os:linux'
    }
    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(
            daysToKeepStr: '90',
            artifactNumToKeepStr: '10'
        ))
        ansiColor('xterm')
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Maven Package') {
            tools {
                jdk 'graalvm-ce-java11-20.2.0'
            }
            steps {
                sh './mvnw clean package -Pnative'
            }
        }
        stage('Archive') {
            steps {
                sh 'rm -vf *.tar.gz'
                dir('target') {
                    sh 'tar czf hashi-admin.tar.gz hashi-admin'
                }
                archiveMavenArtifact name: 'hashi-admin', glob: 'target/*.tar.gz', extension: 'tar.gz'
            }
        }
    }
}
