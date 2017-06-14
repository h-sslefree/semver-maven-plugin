node {
  def mvnHome
  def gitRemoteUser = 'jenkins';
  def gitRemoteUrl = 'github.com/sidohaakma/semver-maven-plugin';
  def artifactId = "semver-maven-plugin";
  stage('Preparation') {
    // Clean workspace
    step([$class: 'WsCleanup', cleanWhenFailure: false])
    // Get code from github.com
    checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: 'refs/tags/${gitTag}']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'jenkins-git', url: 'http://' + gitRemoteUser + '@' + gitRemoteUrl + '.git']]]
    // Get the Maven tool.
    mvnHome = tool 'MAVEN'
    // Set buildname
    currentBuild.displayName = versionTag;
  }
  stage('Test') {
    // Run junit tests
    sh "'${mvnHome}/bin/mvn' test"
    // Generate junit test report
    junit '**/target/surefire-reports/TEST-*.xml'
  }
  stage('Build') {
    // Run the maven build
    sh "'${mvnHome}/bin/mvn' package"
  }
  stage('Release') {
    echo "Deploy artifact: ${artifactId} on search.maven.org";
    // deploy it in the staging repo
    sh "'${mvnHome}/bin/mvn' deploy"
    emailext body: '''Hi everybody,
      <br>
      <br>
      There is a new version available of <i>${JOB_NAME}</i>.
      <br>
      <br>
      The new version is: <b>''' + versionTag + '''</b>.
      <br>
      <br>
      The Changelog is available on:
      <br>
      <br>
      <a target='_blank' href='http://''' + gitRemoteUrl + ''''>http://''' + gitRemoteUrl + '''</a>
      <br>
      <br>
      Kind regards,
      <br>
      <br>
      Build-team''', subject: 'New version of ${JOB_NAME} - ' + versionTag, to: 'sido@haakma.org'
    currentBuild.result = 'SUCCESS';
  }
}
