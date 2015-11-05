import org.apache.cxf.tools.wsdlto.WSDLToJava

//import com.grails.cxf.client.command.WsdlToJavaCommand

description "WSDL2JAVA Grails Script", "grails wsdl2java"

addStatus "Starting wsdl2java"
def wsdls = [[:]]
config.cxf.client.each {
	wsdls << [wsdl: it?.value?.wsdl, wsdlArgs: it?.value?.wsdlArgs, namespace: it?.value?.namespace, client: it?.value?.client ?: false, bindingFile: it?.value?.bindingFile, outputDir: it?.value?.outputDir ?: "src/main/java"]
}

wsdls.each { endpoint ->
	if (endpoint?.wsdl) {
		addStatus "Generating java stubs from ${endpoint?.wsdl}"
//		ant.project.getBuildListeners().firstElement().setMessageOutputLevel(3)

		List<String> args = []
		args << "-verbose"
		args << "-bareMethods=GetQuote"
		args << "-autoNameResolution"
		args << "-allowElementReferences"
		if (endpoint?.client) args << "-client"
		if (endpoint?.namespace) {
			args << "-p"
			args << "${endpoint.namespace}"
		}
		if (config?.bindingFile) {
			args << "-b"
			args << "${endpoint.bindingFile}"
		}
		//can handle a list of a string of single items
		if (config?.wsdlArgs) {
			if (endpoint.wsdlArgs instanceof List) {
				config?.wsdlArgs?.each {
					args << "${it}"
				}
			} else {
				args << "${endpoint.wsdlArgs}"
			}
		}
		args << "-d"
		args << "${endpoint?.outputDir}"

		args << endpoint.wsdl

		WSDLToJava.main(args.toArray() as String[]);

//		new com.grails.cxf.client.command.WsdlToJavaCommand().handle(args)

//		ant.java(classpath: '.', classname: 'org.apache.cxf.tools.wsdlto.WSDLToJava') {
//			arg(value: "-verbose")
//			if (endpoint?.client) arg(value: "-client")
//			if (endpoint?.namespace) {
//				arg(value: "-p")
//				arg(value: "${endpoint.namespace}")
//			}
//			if (config?.bindingFile) {
//				arg(value: "-b")
//				arg(value: "${endpoint.bindingFile}")
//			}
//			arg(value: "-d")
//			arg(value: "${config?.outputDir}")
//			//can handle a list of a string of single items
//			if (config?.wsdlArgs) {
//				if (endpoint.wsdlArgs instanceof List) {
//					config?.wsdlArgs?.each {
//						arg(value: "${it}")
//					}
//				} else {
//					arg(value: "${endpoint.wsdlArgs}")
//				}
//			}
//			arg(value: endpoint.wsdl)
//		}
	}
}

addStatus "Completed wsdl2java"
