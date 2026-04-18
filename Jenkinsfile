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
                                script {
                                    // Define a clean, unique destination for this cell
                                    def cellArtifactPath = "build/matrix-artifacts/${JDK_VERSION}"

                                    sh """
                                        # Remove any old artifacts from previous runs on this same node
                                        rm -rf -- "${cellArtifactPath}"
                                        mkdir -p -- "${cellArtifactPath}"

                                        # Find all test results and reports, but EXCLUDE our own destination.
                                        # We use -mindepth to ensure we don't pick up the root accidentally.
                                        find . -mindepth 2 \\( -path '*/build/reports' -o -path '*/build/test-results' \\) \
                                            -not -path '*/matrix-artifacts/*' -type d | while read -r src; do
                                            # Create a path-based destination name to avoid collisions
                                            rel_src=\${src#./}
                                            dest_dir="${cellArtifactPath}/\${rel_src}"
                                            mkdir -p -- "\$dest_dir"
                                            cp -r -- "\$src/." "\$dest_dir/"
                                        done
                                    """

                                    // Point JUnit ONLY to the namespaced folder for this specific matrix cell
                                    junit(
                                        testResults: "${cellArtifactPath}/**/test-results/**/*.xml",
                                        allowEmptyResults: true
                                    )

                                    // Archive only the namespaced artifacts
                                    archiveArtifacts(
                                        artifacts: "${cellArtifactPath}/**/*",
                                        allowEmptyArchive: true
                                    )
                                }
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
