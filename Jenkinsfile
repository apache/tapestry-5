pipeline {
    agent none

    environment {
        GRADLE_OPTS = '-Dci=true -Dfile.encoding=UTF-8 -Dselenium.wait.timeout=60'
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
                        values 'jdk_11_latest', 'jdk_21_latest'
                    }
                }

                agent {
                    node {
                        label 'ubuntu'
                        // Use cell-based workspaces.
                        // This shouldn't be needed, but we had some weird matrix issues.
                        customWorkspace "workspace/${env.JOB_NAME}-${JDK_VERSION}"
                    }
                }

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
                            // This will run unit tests (JUnit & TestNG) and the default integration variant (jQuery + RequireJS).
                            sh './gradlew check combinedJacocoReport --continue'
                        }
                        post {
                            always {
                                // JUnit namespaces automatically on matrix cells
                                junit(
                                    testResults: '**/build/test-results/**/*.xml',
                                    allowEmptyResults: true
                                )

                                // archiveArtifacts has no per-cell namespacing in matrix builds,
                                // so we copy into a JDK-labelled folder first to avoid cells
                                // overwriting each other's reports in the artifact store.
                                sh """
                                    find . \\( -path '*/build/reports' -o -path '*/build/test-results' \\) \
                                        -not -path '*/matrix-artifacts/*' -type d | while read -r src; do
                                        dest="build/matrix-artifacts/${JDK_VERSION}/\${src#./}"
                                        mkdir -p -- "\$dest"
                                        cp -r -- "\$src/." "\$dest/"
                                    done
                                """

                                archiveArtifacts(
                                    artifacts: "build/matrix-artifacts/${JDK_VERSION}/**/*",
                                    allowEmptyArchive: true,
                                    fingerprint: false
                                )
                            }
                        }
                    }

                    // -- 03: JavaDoc Generation -------------------------------------------

                    stage('Aggregate Javadoc') {
                        when {
                            expression { JDK_VERSION == 'jdk_21_latest' }
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
            }
        }
    }

    post {
        fixed    { sendMail('FIXED') }
        unstable { sendMail('UNSTABLE') }
        failure  { sendMail('FAILURE') }
        aborted  { sendMail('ABORTED') }
    }
}

// MAIL NOTIFICATIONS

def getChangeLog() {
    def changeSets = currentBuild.changeSets
    if (changeSets.isEmpty()) {
        return "No changes recorded (Manual build or no new commits)."
    }
    return changeSets
        .collectMany { it.items as List }
        .collect { "[${it.author}] ${it.msg}" }
        .join('\n')
}

def sendMail(buildStatus) {
    emailext (
        subject: "[${buildStatus}] ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
        body: """
STATUS: ${buildStatus}
Build URL: ${env.BUILD_URL}
Duration: ${currentBuild.durationString.replace(' and counting', '')}

-----------------------------------------------------------
CHANGES
-----------------------------------------------------------
${getChangeLog()}

-----------------------------------------------------------
TEST RESULTS
-----------------------------------------------------------
\${TEST_COUNTS, var="total"} Tests: \${TEST_COUNTS, var="pass"} Passed, \${TEST_COUNTS, var="fail"} Failed, \${TEST_COUNTS, var="skip"} Skipped

-----------------------------------------------------------
FAILED TESTS (if any)
-----------------------------------------------------------
\${FAILED_TESTS, maxTests=10, showStack=false}
        """,
        recipientProviders: [
            developers(), // People who have commits in this build
            requestor()   // The person who manually triggered the build
        ]
    )
}
