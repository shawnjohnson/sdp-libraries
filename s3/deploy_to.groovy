import static groovy.json.JsonOutput.*

void call(app_env){
   stage("Deploy to ${app_env.short_name}"){
      // aws region where bucket exists
      String region = env.AWS_REGION ?:
                      { error "must define AWS region where s3 cluster is location in s3 library." }()

      // determine bucket we're deploying to.
      String bucket = app_env.S3_BUCKET
      String oldBucket
      Boolean firstEnv
      if(app_env.short_name.equals("Dev")){
          firstEnv = true
      } else if(app_env.short_name.equals("Test")){
          firstEnv = false
          oldBucket = getBinding().getVariable("Dev").S3_BUCKET
      } else if(app_env.short_name.equals("Prod")){
          firstEnv = false
          oldBucket = getBinding().getVariable("Test").S3_BUCKET
      } else {
          error "application environment ${app_env.short_name} not recognized."
      }

      // target folder within S3
      // String targetFolder = app_env.s3?.targetFolder ?:
      //                       config.targetFolder ?:
      //                       { error "must define target folder within s3 bucket for upload for s3 library."}()

      // working directory from within the workspace from which to apply the include/exclude patterns for upload
      String workingDir = app_env.s3?.workingDir ?:
                          config.workingDir ?:
                          { error "must define the working directory for upload for the s3 library." }()

      // include pattern within workingDir to upload
      String includePattern = app_env.s3?.includePattern ?:
                              config.includePattern ?: "**/*"

      // exclude pattern within workingDir to NOT upload
      String excludePattern = app_env.s3?.excludePattern ?:
                              config.excludePattern ?: null

      // path within workingDir to place the env json file
      String envFilePath = config.envFilePath ?: "assets/env.json"

      inside_sdp_image "aws", {
          withAWS(region: region){
              assumeRole()
              unstash "workspace"

              dir(workingDir){

                  // add a file to the assets directory.
                  def envConfig =[
                          AWS_IDENTITY_POOL_ID: env.COGNITO_USER_POOL_ID ,
                          AWS_IDENTITY_POOL_WEB_CLIENT_ID: env.COGNITO_USER_POOL_CLIENT_ID ,
                          COGNITO_REGION: env.AWS_REGION ,
                          BASE_API_URL: app_env.BASE_API_URL
                  ]
                  String jsonFile = prettyPrint(toJson(envConfig))
                  sh "rm -f ${envFilePath}"
                  writeFile file: envFilePath, text: jsonFile


                  // empty bucket first to be sure new files cached..
                   sh "aws s3 rm s3://${bucket} --recursive"
                  // upload to s3
                  if( firstEnv ) {
                      sh "aws s3 sync . s3://${bucket} --include '${includePattern}' --exclude '${excludePattern}' --delete --acl public-read"
                  } else {
                      sh "aws s3 sync s3://${oldBucket} s3://${bucket} --include '${includePattern}' --exclude '${envFilePath}' --exclude '${excludePattern}' --delete --acl public-read"
                      sh "aws s3 sync . s3://${bucket} --include '${envFilePath}' --exclude '${excludePattern}' --delete --acl public-read"
                  }

              }

          }
      }
   }
}
