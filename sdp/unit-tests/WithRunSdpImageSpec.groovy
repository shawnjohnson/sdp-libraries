/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package sdp

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class WithRunSdpImageSpec extends JenkinsPipelineSpecification {

  def WithRunSdpImage = null

  def testConfig = [:]

  String emptyConfigMsg = "SDP Image Config is empty in Pipeline Config, Caller Library/Image Config"
  String registryUndefinedMsg = "SDP Image Registry not defined in Pipeline Config, Caller Library/Image Config"
  public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

  def setup() {
    WithRunSdpImage = loadPipelineScriptForTest("./sdp/with_run_sdp_image.groovy")
  }

  def "If no value for config.images, throw error" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: null])
    when:
        WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("error")(emptyConfigMsg) >> {throw new DummyException("images error")}
      thrown(DummyException)
  }

  def "If config.images.empty, throw error" () {
    setup:
    WithRunSdpImage.getBinding().setVariable("config", [images: [:]])
    when:
      WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("error")(emptyConfigMsg) >> {throw new DummyException("images error")}
      thrown(DummyException)
  }

  def "If no value for config.images.registry, throw error" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: [repository: "repotest", cred: "testcred", docker_args: "testargs"]])
    when:
        WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("error")(WithRunSdpImage.getMissingRegistryMsg()) >> {throw new DummyException("images.registry error")}
      thrown(DummyException)
  }

  def "If no value for config.images.repository, default to \"sdp\"" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", cred: "testcred", docker_args: "testargs"]])
    when:
        WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("docker.image")("sdp/test-image") >> explicitlyMockPipelineVariable("Image")

  }

  def "If no value for config.images.cred, throw error" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "testrepo", docker_args: "testargs"]])
    when:
        WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("error")(WithRunSdpImage.getMissingCredentialMsg()) >> {throw new DummyException("images.cred error")}
      thrown(DummyException)
  }


  def "If no value for config.images.docker_args, default to empty string" () {
    setup:
    WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred"]])
    when:
      WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
    _ * getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    1 * getPipelineMock("Image.withRun")("",_, _ as Closure)
  }

  def "If no value for params.command, default to empty string" () {
    setup:
    WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred"]])
    when:
        WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
    _ * getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    1 * getPipelineMock("Image.withRun")(_,"", _ as Closure)
  }

  def "Login to the Docker registry specified in the pipeline config" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("docker.withRegistry")("testregistry", "testcred", _ as Closure)
  }

//  def "Ensure the image in the pipeline config" () {
//    setup:
//    WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
//    when:
//    WithRunSdpImage("test-image", {echo 'testing 123'})
//    then:
//    1 * getPipelineMock("docker.withRegistry")("testregistry", "testcred", _ as Closure)
//    1 * getPipelineMock("docker.image")("restrepo/test-image") >> explicitlyMockPipelineVariable("Image")
//  }

  def "Ensure the image is run w/ the given configuration docker args" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      WithRunSdpImage("test-image", {echo 'testing 123'})
    then:
    1 * getPipelineMock("Image.withRun")("testargs", "",_ as Closure)
  }

  def "use params.args for withRun args" () {
    setup:
    WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
    def args = "args string"
    when:
    WithRunSdpImage("test-image", [args: args], {echo 'testing 123'})
    then:
    _ * getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    1 * getPipelineMock("Image.withRun")(args, "", _ as Closure)
  }

  def "Ensure the closure's resolveStrategy is set to OWNER_FIRST, the default" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      def body = {echo 'testing 123'}
    when:
      WithRunSdpImage("test-image", body)
    then:
      body.resolveStrategy == Closure.OWNER_FIRST
  }

  def "Ensure the input 'command' argument is used in the withRun call" () {
    setup:
    WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
    getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    def body = {echo 'testing 123'}
    def command = "docker command"
    when:
    WithRunSdpImage("test-image", [command:command], body)
    then:
    1 * getPipelineMock("Image.withRun")("testargs", command, _ as Closure)
    1 * getPipelineMock('echo')('testing 123')
  }

  def "Ensure the input 'command' and 'args' arguments are used in the withRun call" () {
    setup:
    WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
    getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    def body = {echo 'testing 123'}
    def command = "docker command"
    def args = "args string"
    when:
    WithRunSdpImage("test-image", [args:args, command:command], body)
    then:
    1 * getPipelineMock("Image.withRun")(args, command, _ as Closure)
    1 * getPipelineMock('echo')('testing 123')
  }

  def "Execute the given closure within the given image" () {
    setup:
      WithRunSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      def body = {echo 'testing 123'}
    when:
      WithRunSdpImage("test-image", body)
    then:
    1 * getPipelineMock("Image.withRun")("testargs", _, _ as Closure)
    1 * getPipelineMock('echo')('testing 123')
  }

  def "Ensure outer call config is used instead of sdp config" () {
    setup:
    def sdpConfig = [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]]
    WithRunSdpImage.getBinding().setVariable("config", sdpConfig )
    getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    def contextConfig = null
    def body = { echo 'testing 123'; contextConfig = config}
    def outer = { WithRunSdpImage.call("test-image", body) }


    when:
    outer()
    then:
    1 * getPipelineMock('echo')('testing 123')
    contextConfig == testConfig
    contextConfig != sdpConfig
  }

  def "Ensure libraryConfig is used instead of sdp config" () {
    setup:
    def sdpConfig = [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]]
    def libraryConfig = [images: [registry: "libregistry", repository: "librepo", cred: "libcred", docker_args: "libargs"]]
    testConfig = libraryConfig
    WithRunSdpImage.getBinding().setVariable("config", sdpConfig )
    1 * getPipelineMock("docker.image")("librepo/test-image") >> explicitlyMockPipelineVariable("Image")
    def contextConfig = null
    def body = { echo 'testing 123'; contextConfig = config}
    def outer = { WithRunSdpImage.call("test-image", body) }


    when:
    outer()
    then:
    1 * getPipelineMock("docker.withRegistry")("libregistry", "libcred", _ as Closure)
    1 * getPipelineMock("Image.withRun")("libargs",_, _ as Closure)
    1 * getPipelineMock('echo')('testing 123')
    contextConfig == testConfig
    contextConfig != sdpConfig
  }

  def "Ensure libraryConfig'specific image is used instead of sdp config or library config" () {
    setup:
    def sdpConfig = [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]]
    def libraryConfig = [images: [registry: "libregistry", repository: "librepo", cred: "libcred", docker_args: "libargs", "test-image":[
            registry: "imageregistry", repository: "imagerepo", cred: "imagecred", docker_args: "imageargs"
    ]
    ]]
    testConfig = libraryConfig
    WithRunSdpImage.getBinding().setVariable("config", sdpConfig )
    1 * getPipelineMock("docker.image")("imagerepo/test-image") >> explicitlyMockPipelineVariable("Image")
    def contextConfig = null
    def body = { echo 'testing 123'; contextConfig = config}
    def outer = { WithRunSdpImage.call("test-image", body) }


    when:
    outer()
    then:
    1 * getPipelineMock("docker.withRegistry")("imageregistry", "imagecred", _ as Closure)
    1 * getPipelineMock("Image.withRun")("imageargs", _, _ as Closure)
    1 * getPipelineMock('echo')('testing 123')
    contextConfig == testConfig
    contextConfig != sdpConfig
  }

  def getConfig(){
    return testConfig
  }
}