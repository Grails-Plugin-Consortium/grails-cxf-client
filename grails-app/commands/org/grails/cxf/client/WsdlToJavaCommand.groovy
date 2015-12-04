package org.grails.cxf.client

import grails.core.GrailsApplication
import grails.dev.commands.ApplicationCommand
import grails.dev.commands.ExecutionContext
import org.apache.cxf.tools.wsdlto.WSDLToJava
import org.springframework.beans.factory.annotation.Autowired

class WsdlToJavaCommand implements ApplicationCommand {

	@Autowired
	GrailsApplication grailsApplication

	boolean handle(ExecutionContext ctx) {
		List<Map> wsdls = [[:]]
		grailsApplication.config.cxf?.client?.each {
			wsdls << [wsdl: it?.value?.wsdl, wsdlArgs: it?.value?.wsdlArgs, namespace: it?.value?.namespace, client: it?.value?.client ?: false, bindingFile: it?.value?.bindingFile, outputDir: it?.value?.outputDir ?: "src/main/java"]
		}

		wsdls.each { endpoint ->
			if (endpoint?.wsdl) {
				println "Generating java stubs from ${endpoint?.wsdl}"

				List<String> args = []
				args << "-verbose"
				if (endpoint?.client) args << "-client"
				if (endpoint?.namespace) {
					args << "-p"
					args << "${endpoint.namespace}"
				}
				if (endpoint?.bindingFile) {
					args << "-b"
					args << "${endpoint.bindingFile}"
				}
				//can handle a list of a string of single items
				if (endpoint?.wsdlArgs) {
					if (endpoint.wsdlArgs instanceof List) {
						endpoint?.wsdlArgs?.each {
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

			}
			return true
		}
	}
}
