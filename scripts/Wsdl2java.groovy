includeTargets << grailsScript("_GrailsSettings")
includeTargets << grailsScript('_GrailsPackage')
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("Init")

target(main: '''generate java class stubs out wsdl files.
This target needs to be run only upon changes in the upstream API, since it's artefacts are kept under version control in src/java
''') {

    depends(compile, createConfig, parseArguments)

    if(!config?.cxf?.installDir){
        echo "ERROR: You must set the config property for cxf.installDir to the root dir of your apache cxf install"
        echo "eg: cxf { installDir = \"c:/apache-cxf-2.4.2\" } }"
        echo "Please correct this and try again"
        return
    }

    echo "starting wsdl2java"
    def wsdls = []
    def cxflib = "${config.cxf.installDir}/lib"
    config.cxf.client.each {
        wsdls << it?.value?.wsdl
    }

    if(!new File(cxflib).exists()){
        echo "ERROR: You must set the config property for cxf.installDir to the root dir of your apache cxf install"
        echo "eg: cxf { installDir = \"c:/apache-cxf-2.4.2\" } }"
        echo "Your dir ${cxflib} does not appear to exist"
        echo "Please correct this and try again"
        return
    }

    path(id: "classpath") {
        fileset(dir: cxflib)
    }

    wsdls.each { wsdl ->
        echo "generating java stubs from $wsdl"

        if(wsdl) {
            java(fork: true, classpathref: "classpath", classname: "org.apache.cxf.tools.wsdlto.WSDLToJava") {
                arg(value: "-verbose")
                arg(value: "-client")
                //arg(value: "-b")
                //arg(value: "grails-app/conf/bindings.xml")
                arg(value: "-d")
                arg(value: "src/java")
                //arg(value: "-catalog")
                //arg(value: "src/java/META-INF/jax-ws-catalog.xml")
                arg(value: wsdl)
            }
        }
    }

    echo "completed wsdl2java"
}

setDefaultTarget(main)

