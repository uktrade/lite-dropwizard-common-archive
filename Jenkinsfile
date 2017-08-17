@Library('lite-jenkins-pipeline') _
def slackChannels = [started: ['#lite-jenkins'], successful: ['#lite-jenkins'], failed: ['#lite-builds', '#lite-jenkins']]
node('jdk8') {
 slackBuildNotifier.notifyBuild("STARTED", slackChannels)
  try {

    stage('Clean workspace'){
      deleteDir()
    }

    stage('Checkout files'){
     checkout scm
    }

    def testFailure = false

    stage("Gradle test: jersey-correlation-id") {
      dir("jersey-correlation-id") {  
        sh 'chmod 777 gradlew'
        try {
          sh "./gradlew test"
        }
        catch (e) {
          testFailure = true
        }
      }
    }

    stage("Gradle test: json-console-appender") {
      dir("json-console-appender") {
        sh 'chmod 777 gradlew'
        try {
          sh "./gradlew test"
        }
        catch (e) {
          testFailure = true
        }
      }
    }

    stage("Gradle test: readiness-metric") {
      dir("readiness-metric") {
        sh 'chmod 777 gradlew'
        try {
          sh "./gradlew test"
        }
        catch (e) {
          testFailure = true
        }
      }
    }

    stage("Gradle test: spire-client") {
      dir("spire-client") {
        sh 'chmod 777 gradlew'
        try {
          sh "./gradlew test"
        }
        catch (e) {
          testFailure = true
        }
      }
    }

    stage("Archive results") {
      step([$class: 'JUnitResultArchiver', testResults: '*/build/test-results//*.xml'])

      if (testFailure) {
        error("Test failures found, see the test reports for more details")
      }
    }

  } catch (e) {
    currentBuild.result = "FAILED"
    throw e
  }
  finally {
    slackBuildNotifier.notifyBuild(currentBuild.result, slackChannels)
  }
}