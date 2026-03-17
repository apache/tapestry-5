pipeline {
    agent {
        node {
            label 'ubuntu'
        }
    }

    environment {
        GRADLE_OPTS  = '-Dci=true -Dfile.encoding=UTF-8'
    }

    tools {
        jdk 'jdk_11_latest'
    }

    options {
        timeout(time: 2, unit: 'HOURS')
        skipStagesAfterUnstable()
    }

    stages {

        // -- 01: Clean + Assemble ---------------------------------------------

        stage('Assemble') {
            steps {
                sh './gradlew clean assemble'
            }
        }

        // -- 02: Test: Check + jQuery + RequireJS Enabled ---------------------

        stage('Test: Check + jQuery + RequireJS Enabled') {
            steps {
                sh './gradlew check'
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

        // -- 03: Test Variants ------------------------------------------------

        stage('Test: Prototype + RequireJS Disabled') {
            steps {
                sh './gradlew tapestry-core:testWithPrototypeAndRequireJsDisabled'
            }
            post {
                always {
                     sh '''
                        if [ -d "tapestry-core/build/test-results/testWithPrototypeAndRequireJsDisabled" ]; then
                            find tapestry-core/build/test-results/testWithPrototypeAndRequireJsDisabled -name "*.xml" -type f -exec sed -i 's/classname="/classname="PrototypeAndRequireJsDisabled./g' {} +
                        fi
                    '''

                    junit(
                        testResults: 'tapestry-core/build/test-results/testWithPrototypeAndRequireJsDisabled/**/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('Test: jQuery + RequireJS Disabled') {
            steps {
                sh './gradlew tapestry-core:testWithJqueryAndRequireJsDisabled'
            }
            post {
                always {
                   sh '''
                        if [ -d "tapestry-core/build/test-results/testWithJqueryAndRequireJsDisabled" ]; then
                            find tapestry-core/build/test-results/testWithJqueryAndRequireJsDisabled -name "*.xml" -type f -exec sed -i 's/classname="/classname="JqueryAndRequireJsDisabled./g' {} +
                        fi
                    '''

                    junit(
                        testResults: 'tapestry-core/build/test-results/testWithJqueryAndRequireJsDisabled/**/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('Test: Prototype + RequireJS Enabled') {
            steps {
                sh './gradlew tapestry-core:testWithPrototypeAndRequireJsEnabled'
            }
            post {
                always {
                    sh '''
                        if [ -d "tapestry-core/build/test-results/testWithPrototypeAndRequireJsEnabled" ]; then
                            find tapestry-core/build/test-results/testWithPrototypeAndRequireJsEnabled -name "*.xml" -type f -exec sed -i 's/classname="/classname="PrototypeAndRequireJsEnabled./g' {} +
                        fi
                    '''

                    junit(
                        testResults: 'tapestry-core/build/test-results/testWithPrototypeAndRequireJsEnabled/**/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        // -- 04: Coverage (JaCoCo) --------------------------------------------

        stage('Coverage') {
            steps {
                sh './gradlew combinedJacocoReport'
            }
            post {
                always {
                    recordCoverage(
                        tools: [[
                            parser:  'JACOCO',
                            pattern: '**/build/reports/jacoco/jacoco.xml'
                        ]],
                        id:   'jacoco',
                        name: 'JaCoCo Coverage',
                        sourceDirectories: [[path: 'glob:**/src/main/java']],
                        sourceCodeRetention: 'LAST_BUILD'
                    )
                }
            }
        }

        // -- 05: Archive Artifacts --------------------------------------------

        stage('Archive') {
            steps {
                archiveArtifacts(
                    artifacts: '**/build/reports/**/*, **/build/test-results/**/*',
                    allowEmptyArchive: true
                )
            }
        }

        // -- 06: Aggregate Javadoc --------------------------------------------

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
