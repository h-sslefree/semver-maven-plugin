node {
  def mvnHome
  def versionTag = gitTag;
  def gitRemoteUser = 'jenkins';
  def gitRemoteUrl = 'git.bicat.com/scm/build/semver-maven-plugin';
  def artifactId = "semver-maven-plugin";
  stage('Preparation') {
    // Clean workspace
    step([$class: 'WsCleanup', cleanWhenFailure: false])
    // Get some code from git.bicat.com
    checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: 'refs/tags/${gitTag}']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'Jenkins-GIT', url: 'http://' + gitRemoteUser + '@' + gitRemoteUrl + '.git']]]
    // Get the Maven tool.
    mvnHome = tool 'MAVEN'
    // Set buildname
    currentBuild.displayName = versionTag;
  }
  stage('Build') {
    // Run the maven build
    sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean package"
  }
  stage('Test') {
    echo 'Gather testresults and push them to sonar';
    //junit '**/target/surefire-reports/TEST-*.xml'
  }
  stage('Release') {
    echo 'Deploy on release repo';
    // deploy it in the staging repo
    sh "'${mvnHome}/bin/mvn' deploy"
    // push changelog to doc.bicat.com
    sh "/usr/share/local/jenkins/copy_changelog.sh " + artifactId;
    // email everbody with new changelog
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
      Build-team''', subject: 'New version of ${JOB_NAME} - ' + versionTag, to: 'sido@bicat.com, rudie@bicat.com, arnold@bicat.com, mike@bicat.com'
    currentBuild.result = 'SUCCESS';
  }
}
