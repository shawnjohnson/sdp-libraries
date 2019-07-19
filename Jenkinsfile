parallel "Unit Test": {
  node{
    checkout scm 
    stage("Unit Test"){
      sh "make test docker" 
      archiveArtifacts "target/reports/tests/test/**" 
      junit "target/test-results/test/*.xml" 
    }
  }
}, "Compile Docs": {
  node{
    stage("Compile Docs"){
      checkout scm 
      sh "make docs" 
      archiveArtifacts "_build/html/**"
    }
  }
}
