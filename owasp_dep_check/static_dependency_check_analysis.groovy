/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call() {
  node {
    unstash "workspace"
    stage ('Static Dependency Security Scan') {

      //The folder that is scanned; it should meet the requirements of the tool's scanners
      def scan_target = config.scan_target ? "/src/${config.scan_target}" : "/src"

      //The folders ()& their contents) that are ignored by the scan are passed in with the "--exclude" flag to the tool
      if ( ! config.exclude_dirs ) { //exclude_dirs is a comma-separated list of *directories* to ignore
        echo "No dir(s) excluded."
        exclude_opt = ""
      } else {
        echo "Excluding dir(s): ${config.exclude_dirs}"
        exclude_opt = config.exclude_dirs.replace(',','/**\' --exclude \'') //need an additional flag for each dir to ignore
        exclude_opt = "--exclude \'${exclude_opt}/**\'"
        echo "Exclude command: $exclude_opt"
      }

      // Vulnerabilities are scored 0-10, w/ 10 being most severe. This threshold needs to be >0
      // For any threshold greater than 10, the pipeline will not fail due to any detected vulnerability
      def cvss_threshold = (config.cvss_threshold == "pass") ? "11" :
                           (config.cvss_threshold ==~ /^\d+$/) ? config.cvss_threshold :
                           {error "CVSS Threshold is not properly defined in Pipeline Config"}()

      def image_version = config.image_version ?: "latest"

      def report_format = config.report_format ?: "ALL"

      //Check for "missing node_modules" corner case
      if( ( fileExists("${scan_target}/package.json") || fileExists("${scan_target}/package-lock.json") ) && ! fileExists("${scan_target}/node_modules") ){
        echo "package.json or package-lock.json detected, but not node_modules. Running \"npm install\" at least once"
        docker.image("node:latest").inside{
          sh "npm install"
        }
      }

      def data_dir = "owasp_logs/data"
      def report_dir = "owasp_logs/reports"

      sh """
      if [ ! -d "$data_dir" ]; then
        echo "Initially creating persistent directories"
        mkdir -p \$(pwd)/${data_dir}
        chmod -R 777 \$(pwd)/${data_dir}
        mkdir -p \$(pwd)/${report_dir}
        chmod -R 777 \$(pwd)/${report_dir}
      fi
      """
      
      sh """
        docker run -v \$(pwd):/src -v \$(pwd)/${data_dir}:/usr/share/dependency-check/data -v \$(pwd)/${report_dir}:/report owasp/dependency-check --scan ${scan_target} --format "${report_format}" --project "OWASP_dependency_check" --out /report
      """
      
      stash "workspace" 
      archiveArtifacts allowEmptyArchive: true, artifacts: "${report_dir}/**"
      
    
    }
  }
}
