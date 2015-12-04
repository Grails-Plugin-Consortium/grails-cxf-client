package org.grails.cxf.client.util

import org.apache.cxf.tools.wsdlto.WSDLToJava

public class WsdlToJavaUtil {
	public static void runWsdlToJava(Map endpoint) {
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
	}
}
