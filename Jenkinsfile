node{
  checkout scm 
  stage("Unit Test"){
    sh "make test docker" 
    archiveArtifacts "target/reports/tests/test/**" 
    junit "target/test-results/test/*.xml" 
  }
}
