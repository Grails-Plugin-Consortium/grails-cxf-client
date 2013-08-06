import com.grails.cxf.client.DynamicWebServiceClient
import com.grails.cxf.client.WebServiceClientFactoryImpl
import com.grails.cxf.client.exception.CxfClientException
import com.grails.cxf.client.security.DefaultSecurityOutInterceptor

class CxfClientGrailsPlugin {

    private final Long DEFAULT_CONNECTION_TIMEOUT = 30000
    private final Long DEFAULT_RECEIVE_TIMEOUT = 60000

    def version = "1.5.5"
    def grailsVersion = "1.3.0 > *"
    def pluginExcludes = [
            'grails-app/conf/codenarc.groovy',
            'grails-app/conf/codenarc.ruleset.all.groovy.txt',
            'grails-app/conf/DataSource.groovy',
            'grails-app/conf/UrlMappings.groovy',
            'src/groovy/test/**',
            'src/java/test/**',
            'src/java/net/**',
            'docs/**',
            'web-app/**',
            'codenarc.properties',
            "spock-0.6"
    ]

    def license = "APACHE"
    def author = "Grails Plugin Consortium"
    def authorEmail = "acetrike@gmail.com"
    def developers = [
            [name: "Christian Oestreich", email: "acetrike@gmail.com"],
            [name: "Brett Borchardt", email: "bborchardt@gmail.com"],
            [name: "Laura Helde", email: "laurahelde@gmail.com"],
            [name: "Kyle Dickerson", email: "kyle.dickerson@gmail.com"],
            [name: "Jordan Howe", email: "jordan.howe@gmail.com"]]
    def title = "Cxf Client - Support for Soap Service Endpoints"
    def description = '''\\
Used for easily calling soap web services.  Provides wsdl2java grails target to easily generate code into src/java from configured cxf clients.  Ability to dynamically update endpoint at runtime.
'''
    def documentation = "https://github.com/Grails-Plugin-Consortium/grails-cxf-client"
    def scm = [url: "https://github.com/Grails-Plugin-Consortium/grails-cxf-client"]

//    def watchedResources = [
//            "file:${pluginLocation}/grails-app/services/**/*Service.groovy",
//            "file:${pluginLocation}/grails-app/controllers/**/*Controller.groovy",
//            "file:${pluginLocation}/grails-app/taglib/**/*TagLib.groovy",
//            "file:${pluginLocation}/grails-app/taglib/**/*Job.groovy"
//    ]

    def doWithSpring = {

        webServiceClientFactory(WebServiceClientFactoryImpl)

        log.info "wiring up cxf-client beans"

        def cxfClientConfigMap = application.config?.cxf?.client ?: [:]

        cxfClientConfigMap.each { cxfClient ->
            configureCxfClientBeans.delegate = delegate
            configureCxfClientBeans(cxfClient)
        }

        log.info "completed mapping cxf-client beans"
    }

    def configureCxfClientBeans = {cxfClient ->
        def cxfClientName = cxfClient.key
        def client = application.config?.cxf?.client[cxfClientName]
        def inList = []
        def outList = []
        def outFaultList = []
        def inFaultList = []

        log.info "wiring up client for $cxfClientName [clientInterface=${client?.clientInterface} and serviceEndpointAddress=${client?.serviceEndpointAddress}]"

        //the call to the map returns a gstring as [:] not as a map literal (fix this?)
        if(!client?.clientInterface || (client.serviceEndpointAddress == "[:]")) {
            String errorMessage = "Web service client $cxfClientName cannot be created before setting the clientInterface=${client?.clientInterface} and serviceEndpointAddress=${client?.serviceEndpointAddress} properties"
            println errorMessage
            log.error errorMessage
        }

        if(client?.secured && !client?.securityInterceptor) {
            "securityInterceptor${cxfClientName}"(DefaultSecurityOutInterceptor) {
                username = client?.username ?: ""
                password = client?.password ?: ""
            }
        }

        addInterceptors.delegate = delegate
        addInterceptors(client?.inInterceptors, inList)
        addInterceptors(client?.outInterceptors, outList)
        addInterceptors(client?.inFaultInterceptors, inFaultList)
        addInterceptors(client?.outFaultInterceptors, outFaultList)

        def connectionTimeout = client?.connectionTimeout ?: ((client?.connectionTimeout == 0) ? client.connectionTimeout : DEFAULT_CONNECTION_TIMEOUT) //use the cxf defaults instead of 0
        def receiveTimeout = client?.receiveTimeout ?: ((client?.receiveTimeout == 0) ? client.receiveTimeout : DEFAULT_RECEIVE_TIMEOUT) //use the cxf defaults instead of 0

        validateTimeouts.delegate = delegate
        validateTimeouts(cxfClientName, 'connectionTimeout', connectionTimeout)
        validateTimeouts(cxfClientName, 'receiveTimeout', receiveTimeout)

        "${cxfClientName}"(DynamicWebServiceClient) {
            webServiceClientFactory = ref("webServiceClientFactory")
            if(client?.secured || client?.securityInterceptor) {
                if(client?.securityInterceptor) {
                    outList << ref("${client.securityInterceptor}")
                } else {
                    outList << ref("securityInterceptor${cxfClientName}")
                }
            }
            //both of these are used for mime attachments only atm.
            if(client?.wsdlServiceName){
                wsdlURL = client?.wsdl ?: null
                wsdlServiceName = client?.wsdlServiceName ?: null
            }
            if(client?.wsdlEndpointName){
                wsdlEndpointName = client?.wsdlEndpointName ?: null
            }
            inInterceptors = inList
            outInterceptors = outList
            inFaultInterceptors = inFaultList
            outFaultInterceptors = outFaultList
            clientInterface = client.clientInterface ?: ""
            serviceName = cxfClientName
            serviceEndpointAddress = client?.serviceEndpointAddress ?: ""
            enableDefaultLoggingInterceptors = (client?.enableDefaultLoggingInterceptors?.toString() ?: "true") != "false"
            clientPolicyMap = [connectionTimeout: connectionTimeout,
                    receiveTimeout: receiveTimeout,
                    allowChunking: (client?.allowChunking) ?: false,
                    contentType: (client?.contentType) ?: 'text/xml; charset=UTF-8'
            ]
            if(client?.httpClientPolicy){
                httpClientPolicy = ref("${client.httpClientPolicy}")
            }
            proxyFactoryBindingId = client?.proxyFactoryBindingId ?: ""
            secureSocketProtocol = client?.secureSocketProtocol ?: "" //should be one of the constants in CxfClientConstants, but doesn't have to be
            requestContext = client?.requestContext ?: [:]
            tlsClientParameters = client?.tlsClientParameters ?: [:]
        }
    }

    def validateTimeouts = {cxfClientName, timeoutName, timeoutValue ->
        if(timeoutValue && timeoutValue < 0) {
            throw new CxfClientException("Configured value for ${cxfClientName} ${timeoutName} must be >= 0 if provided. Value provided was(${timeoutValue})")
        }
    }

    def addInterceptors = {clientInterceptors, interceptorList ->
        if(clientInterceptors) {
            if(clientInterceptors instanceof List) {
                clientInterceptors.each {
                    interceptorList << ref(it.trim())
                }
            } else {
                clientInterceptors.split(',').each {
                    interceptorList << ref(it.trim())
                }
            }
        }
    }

    //todo: add and test the onchange registration for this
    def onChange = { event ->
//        if (event.source) {
//            def serviceClass = application.addServiceClass(event.source)
//            def serviceName = "${serviceClass.propertyName}"
//            def beans = beans {
//                "$serviceName"(serviceClass.getClazz()) { bean ->
//                    bean.autowire =  true
//                }
//            }
//            if (event.ctx) {
//                event.ctx.registerBeanDefinition(
//                        serviceName,
//                        beans.getBeanDefinition(serviceName))
//            }
//        }
    }

    ConfigObject getBuildConfig() {
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().getClassLoader())
        return new ConfigSlurper().parse(classLoader.loadClass('BuildConfig'))
    }

    String getPluginLocation() {
        return buildConfig?.grails?.plugin?.location?.'cxf-client'
    }
}
