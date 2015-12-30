description "WSDL2JAVA Grails Script", "grails wsdl2java"

addStatus "Please use the wsdlToJava command via grails or gradle.  This has been deprecated in favor of the new command pattern."

//addStatus "Starting wsdl2java"
//def wsdls = [[:]]
//config.cxf.client.each {
//	wsdls << [wsdl: it?.value?.wsdl, wsdlArgs: it?.value?.wsdlArgs, namespace: it?.value?.namespace, client: it?.value?.client ?: false, bindingFile: it?.value?.bindingFile, outputDir: it?.value?.outputDir ?: "src/main/java"]
//}
//
//wsdls.each { endpoint ->
//	WsdlToJavaUtil.runWsdlToJava(endpoint)
//}

addStatus "Completed wsdl2java"
