pipeline {
    agent none

    environment {
        GRADLE_OPTS = '-Dci=true -Dfile.encoding=UTF-8 -Dselenium.wait.timeout=30'
    }

    options {
        timeout(time: 90, unit: 'MINUTES')
        skipStagesAfterUnstable()
    }

    stages {
        stage('Matrix') {
            matrix {
                axes {
                    axis {
                        name   'JDK_VERSION'
                        values 'jdk_1.8_latest', 'jdk_21_latest'
                    }
                }

                agent { node { label 'ubuntu' } }

                tools {
                    jdk "${JDK_VERSION}"
                }

                stages {

                    // -- 01: Clean + Assemble ---------------------------------------------

                    stage('Assemble') {
                        steps {
                            sh './gradlew clean assemble'
                        }
                    }

                    // -- 02: Test + Coverage ----------------------------------------------

                    stage('Test: Check') {
                        steps {
                            // This will run unit tests and the default integration variant (jQuery + RequireJS).
                            // We include the coverage report in this step, as modifying the test results with
                            // sed would trigger a test rerun if it would be its own stage.
                            sh './gradlew check combinedJacocoReport --continue'
                        }
                        post {
                            always {
                                // Prefix the JUnit classnames so the Test UI shows which JDK ran which test
                                sh """
                                    find . -path '*/build/test-results/test/*.xml' -exec \
                                        sed -i 's/classname="/classname="${JDK_VERSION}./g' {} +
                                """

                                junit(
                                    testResults: '**/build/test-results/test/**/*.xml',
                                    allowEmptyResults: true
                                )

                                // Copy reports into a JDK-named folder to avoid overwriting between matrix cells
                                sh """
                                    find . \\( -path '*/build/reports' -o -path '*/build/test-results' \\) -type d | while IFS= read -r src; do
                                        dest="matrix-artifacts/${JDK_VERSION}/\${src#./}"
                                        mkdir -p -- "\$dest"
                                        cp -r -- "\$src/." "\$dest/"
                                    done
                                """

                                archiveArtifacts(
                                    artifacts: "matrix-artifacts/${JDK_VERSION}/**/*",
                                    allowEmptyArchive: true
                                )
                            }
                        }
                    }
                }
            }
        }

        // -- 03: JavaDoc Generation -------------------------------------------

        stage('Aggregate Javadoc') {
            agent { node { label 'ubuntu' } }
            tools {
                jdk 'jdk_21_latest'
            }
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
