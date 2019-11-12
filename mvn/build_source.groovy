void call(){
  stage("build"){
    def image = config.image ?: "maven:3.6-jdk-8"
    def install_script = config.install_script ?: "install"
    def output_ls = config.ls_output ?: false
    def build_cmd = config.build_cmd ?: "mvn clean package -Dmaven.test.skip=true"
    docker.image(image).inside{
      unstash "workspace"
      sh "${build_cmd}"

      if( output_ls ){
        sh "ls ."
      }

      stash name: "workspace",
        includes: config.stash?.includes ?: "**",
        excludes: config.stash?.excludes ?: "**/*Test",
        useDefaultExcludes: false,
        allowEmpty: true
    }
  }
}
