void call(){
  stage("Maven Unit test"){
    if( config.test_disable ){
      unstable("unit tests are disabled")
      return;
    }

    def result_msgr = config.test_fail_on_exception ? error : unstable

    def image = config.image ?: "maven:3.6-jdk-8"
    def output_ls = config.ls_output ?: false
    docker.image(image).inside{
      def test_results_file = config.test_report_path ?: "junit.xml"

      unstash "workspace"
      echo "running maven unit tests"

      timeout(time: 10, unit: 'MINUTES') {
        try {
          String testCmd = config.test_cmd ?: "mvn clean verify jacoco:report"
          sh "${testCmd}"

          if( output_ls ){
            sh "ls -R | grep jacoco"
          }

          stash name: config.stash?.name ?:"workspace",
                  includes: config.stash?.includes ?: "**",
                  excludes: config.stash?.excludes ?: "**/*Test.java",
                  useDefaultExcludes: false,
                  allowEmpty: true

          echo "archiving ${test_results_file}"

          archiveArtifacts allowEmptyArchive: true, artifacts: "${test_results_file}"
        } catch (any) {
          println "issue with maven unit tests"
          result_msgr("issue with maven unit tests: ${any}")
        }
      }
    }
  }
}