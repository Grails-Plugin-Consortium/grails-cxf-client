includeTargets << grailsScript("_GrailsSettings")
includeTargets << grailsScript('_GrailsPackage')
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("Init")

target(main: '''generate java class stubs out wsdl files.
This target needs to be run only upon changes in the upstream API, since it's artefacts are kept under version control in src/java
''') {

    depends(compile, createConfig, parseArguments)

    if(!config?.cxf?.installDir) {
        echo "ERROR: You must set the config property for cxf.installDir to the root dir of your apache cxf install"
        echo "eg: cxf { installDir = \"c:/apache-cxf-2.4.2\" } }"
        echo "Please correct this and try again"
        return
    }

    echo "starting wsdl2java"
    def wsdls = [[:]]
    def cxflib = "${config.cxf.installDir}/lib"
    config.cxf.client.each {
        wsdls << [wsdl: it?.value?.wsdl, wsdlArgs: it?.value?.wsdlArgs, namespace: it?.value?.namespace, client: it?.value?.client ?: false, bindingFile: it?.value?.bindingFile, outputDir: it?.value?.outputDir ?: "src/java"]
    }

    if(!new File(cxflib).exists()) {
        echo "ERROR: You must set the config property for cxf.installDir to the root dir of your apache cxf install"
        echo "eg: cxf { installDir = \"c:/apache-cxf-2.4.2\" } }"
        echo "Your dir ${cxflib} does not appear to exist"
        echo "Please correct this and try again"
        return
    }

    path(id: "classpath") {
        fileset(dir: cxflib)
    }

    wsdls.each { config ->
        if(config?.wsdl) {
            echo "generating java stubs from ${config?.wsdl}"
            java(fork: true, classpathref: "classpath", classname: "org.apache.cxf.tools.wsdlto.WSDLToJava") {
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

    echo "completed wsdl2java"
}

setDefaultTarget(main)

