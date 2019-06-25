
def call(){
    List options = ['github', 'github_enterprise']
    String impl = config.source_type

    return getBinding().getStep(impl)
}
