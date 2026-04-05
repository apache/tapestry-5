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
                        values 'jdk_11_latest', 'jdk_21_latest'
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
        fixed    { sendMail('FIXED') }
        unstable { sendMail('UNSTABLE') }
        failure  { sendMail('FAILURE') }
        aborted  { sendMail('ABORTED') }
    }
}

// MAIL NOTIFICATIONS

def getChangeLog() {
    def log = ""
    def changeSets = currentBuild.changeSets

    if (changeSets.isEmpty()) {
        return "No changes recorded (Manual build or no new commits)."
    }

    for (int i = 0; i < changeSets.size(); i++) {
        def entries = changeSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            log += "[${entry.author}] ${entry.msg}\n"
        }
    }
    return log
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
\${TEST_COUNTS, var="total"} Tests: \${TEST_COUNTS, var="pass"} Passed, \${TEST_COUNTS, var="fail"} Failed, \${TEST_COUNTS, var="skipped"} Skipped

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
