@Validate
void call(Map context) {
  List options = ['github', 'github_enterprise']
  String impl = config.source_type

   options.contains(impl) ? null :
          { error "github.config.source_type: ${impl} is not a valid option; should be one of: ${options.join(", ")}" } ()
}