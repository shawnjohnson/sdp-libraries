def call(){
    def phase = "train_model"
    
    def phaseConfig = config[phase]
    if(!config[phase]){
        stage(phase){
            println "BoozFlow phase ${phase} not defined in pipeline configuration."
        }
        return 
    }else{
        boozflow(phaseConfig)
    }

}