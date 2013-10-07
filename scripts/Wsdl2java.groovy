import org.codehaus.gant.GantState

includeTargets << grailsScript("_GrailsSettings")
includeTargets << grailsScript('_GrailsPackage')
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsClasspath")

printMessage = { String message -> event('StatusUpdate', [message]) }
finished = {String message -> event('StatusFinal', [message])}
errorMessage = { String message -> event('StatusError', [message]) }

target(wsdl2java: '''generate java class stubs out wsdl files.
This target needs to be run only upon changes in the upstream API, since it's artefacts are kept under version control in src/java
''') {

    depends(compile, createConfig, parseArguments, classpath)
//    depends(createConfig, parseArguments, classpath)

    printMessage "Starting wsdl2java"
    def wsdls = [[:]]
    config.cxf.client.each {
        wsdls << [wsdl: it?.value?.wsdl, wsdlArgs: it?.value?.wsdlArgs, namespace: it?.value?.namespace, client: it?.value?.client ?: false, bindingFile: it?.value?.bindingFile, outputDir: it?.value?.outputDir ?: "src/java"]
    }

    wsdls.each { config ->
        if(config?.wsdl) {
            printMessage "Generating java stubs from ${config?.wsdl}"
            ant.logger.setMessageOutputLevel(GantState.NORMAL)
            ant.java(classname: 'org.apache.cxf.tools.wsdlto.WSDLToJava') {
                arg(value: "-verbose")
                if(config?.client) arg(value: "-client")
                if(config?.namespace) {
                    arg(value: "-p")
                    arg(value: "${config.namespace}")
                }
                if(config?.bindingFile) {
                    arg(value: "-b")
                    arg(value: "${config.bindingFile}")
                }
                arg(value: "-d")
                arg(value: "${config?.outputDir}")
                //can handle a list of a string of single items
                if(config?.wsdlArgs) {
                    if(config.wsdlArgs instanceof List) {
                        config?.wsdlArgs?.each {
                            arg(value: "${it}")
                        }
                    } else {
                        arg(value: "${config.wsdlArgs}")
                    }
                }
                //arg(value: "src/java/META-INF/jax-ws-catalog.xml")
                arg(value: config.wsdl)
            }
        }
    }

    finished "Completed wsdl2java"
}

setDefaultTarget(wsdl2java)
