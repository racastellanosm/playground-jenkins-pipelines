import groovy.io.FileType

def pipelinesDir = new File('/var/jenkins_home/pipelines')
if (pipelinesDir.exists()) {
    pipelinesDir.eachFileMatch(FileType.FILES, ~/.*\.groovy/) { file ->
        // Use the filename without extension as the job name
        def jobName = file.name.take(file.name.lastIndexOf('.'))
        
        pipelineJob(jobName) {
            description("Programmatic pipeline generated from ${file.name}")
            definition {
                cps {
                    // Load the contents of the pipeline script directly
                    script(file.text)
                    sandbox(true)
                }
            }
        }
        println "Created/Updated pipeline job: ${jobName}"
    }
} else {
    println "Pipelines directory not found at /var/jenkins_home/pipelines"
}
