void call(phaseConfig, phase = ""){
    // parallel building 
    if(phaseConfig.modules){
        phaseConfig.modules.each{ module ->
            stage(phase){
                def phases = [:]
                module.each{ directory ->
                    phases[directory] = {
                        try{
                            node{
                                unstash "workspace"

                                // TODO: check if ${directory} exists? 
                                // for now, just create while testing..

                                // sh "mkdir -p ${directory}"
                                // aws region where bucket exists
                                String region = env.AWS_REGION

                                def params = init_flow()
                                String sourceDir = params.BOOZFLOW_SOURCE_DIR
                                String targetDir = params.BOOZFLOW_TARGET_DIR
                                String s3Bucket = params.BOOZFLOW_S3_BUCKET

                                dir(directory){
                                    // build something 
                                    sh "bash build_image.sh"

                                    def response = assumeRole()

                                    sh(script: """
                                        set -x; 
                                        bash ./run_container.sh  \
                                        ${response.AccessKeyId} \
                                        ${response.SecretAccessKey} \
                                        ${response.Token} \
                                        ${s3Bucket} \
                                        ${sourceDir} \
                                        ${targetDir}
                                    """, label: "executing ${directory} command")


                                }
                            } 
                        } catch(any){
                            println "Ran into issue running boozflow phase ${directory}"
                            unstable(any.getMessage())
                        }
                    } 
                }
                parallel phases
            }
        }            
    }
}

