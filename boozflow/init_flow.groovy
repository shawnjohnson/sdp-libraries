import org.boozallen.plugins.jte.hooks.Init

//@Init
def call(/* Map context */){
      // set the timestamped target directory
      // def globals = getAwsParameters("/${env.PROJECT_NAME}/infrastructure/globals", env.AWS_REGION)

      if( !env?.DATA_S3_BUCKET || !env?.DATA_TARGET_PREFIX || !env?.DATA_SOURCE_DIR ){
            error("parameters are not available in ${env.AWS_REGION} at: /${env.PROJECT_NAME}/infrastructure/globals")
      }

      def result = [:]
      result.BOOZFLOW_TARGET_DIR =  env?.DATA_TARGET_PREFIX + currentBuild.startTimeInMillis
      result.BOOZFLOW_S3_BUCKET  =  env?.DATA_S3_BUCKET
      result.BOOZFLOW_SOURCE_DIR =  env?.DATA_SOURCE_DIR

      return result
}
