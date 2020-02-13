import groovy.json.JsonSlurper 

/*
    expected to be called like:
    inside_sdp_image("aws"){
        assumeRole()
    }
*/
def call(){

    // get response with secret key 
    String credURI = System.getenv("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI")
    String r = "curl http://169.254.170.2${credURI} ".execute().text
    println "response = ${r}"
    def response = readJSON(text: r)

    env.AWS_ACCESS_KEY_ID = response.AccessKeyId
    env.AWS_SECRET_ACCESS_KEY = response.SecretAccessKey
    env.AWS_SESSION_TOKEN = response.Token

    return response

}
