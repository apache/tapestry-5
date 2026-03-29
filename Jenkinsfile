pipeline {
    agent {
        node {
            label 'ubuntu'
        }
    }

    environment {
        GRADLE_OPTS  = '-Dci=true -Dfile.encoding=UTF-8 -Dselenium.wait.timeout=30'
    }

    tools {
        jdk 'jdk_11_latest'
    }

    options {
        timeout(time: 3, unit: 'HOURS')
        skipStagesAfterUnstable()
    }

    stages {

        // -- 01: Clean + Assemble ---------------------------------------------

        stage('Assemble') {
            steps {
                sh './gradlew clean assemble'
            }
        }

        // -- 02: Test: Check - Unit + integration tests (jQuery + RequireJS) --

        stage('Test: Check') {
            steps {
                sh './gradlew check --continue'
            }
            post {
                always {
                    junit(
                        testResults: '**/build/test-results/test/**/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        // -- 03: Coverage (JaCoCo) --------------------------------------------

//       stage('Coverage') {
//           steps {
//               sh './gradlew combinedJacocoReport'
//           }
//           post {
//               always {
//                   jacoco(
//                       execPattern:   '**/build/jacoco/*.exec',
//                       classPattern:  '**/build/classes/java/main',
//                       sourcePattern: '**/src/main/java'
//                   )
//               }
//           }
//       }

        // -- 04: Archive Artifacts --------------------------------------------

        stage('Archive') {
            steps {
                archiveArtifacts(
                    artifacts: '**/build/reports/**/*, **/build/test-results/**/*',
                    allowEmptyArchive: true
                )
            }
        }

        // -- 05: Aggregate Javadoc --------------------------------------------

        stage('Aggregate Javadoc') {
            steps {
                sh './gradlew aggregateJavadoc'
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir:   'build/documentation/javadocs',
                        reportFiles: 'index.html',
                        reportName:  'Aggregate Javadoc',
                        keepAll:     true
                    ])
                }
            }
        }
    }

    post {
        failure {
            emailext (
                to: "${env.MAIL_NOTIFICATION}",
                subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                         <p>Check console output at &quot;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&quot;</p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }

        unstable {
            emailext (
                to: "${env.MAIL_NOTIFICATION}",
                subject: "UNSTABLE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>UNSTABLE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                         <p>Check console output at &quot;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&quot;</p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
    }
}
