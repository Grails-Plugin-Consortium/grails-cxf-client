package grails.cxf.client.g3

import com.grails.cxf.client.DynamicWebServiceClient
import com.grails.cxf.client.WebServiceClientFactoryImpl
import com.grails.cxf.client.exception.CxfClientException
import com.grails.cxf.client.security.DefaultSecurityOutInterceptor
import grails.plugins.*

class GrailsCxfClientG3GrailsPlugin extends Plugin {

    private final Long DEFAULT_CONNECTION_TIMEOUT = 30000
    private final Long DEFAULT_RECEIVE_TIMEOUT = 60000

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            'grails-app/conf/codenarc.groovy',
            'grails-app/conf/config.properties',
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

    Closure doWithSpring() { {->
        webServiceClientFactory(WebServiceClientFactoryImpl)

        log.info "wiring up cxf-client beans"

        def cxfClientConfigMap = application.config?.cxf?.client ?: [:]

        cxfClientConfigMap.each { cxfClient ->
            configureCxfClientBeans.delegate = delegate
            configureCxfClientBeans(cxfClient)
        }

        log.info "completed mapping cxf-client beans"
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
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
            if(client?.authorizationPolicy){
                authorizationPolicy = ref("${client.authorizationPolicy}")
            }
            proxyFactoryBindingId = client?.proxyFactoryBindingId ?: ""
            secureSocketProtocol = client?.secureSocketProtocol ?: "" //should be one of the constants in CxfClientConstants, but doesn't have to be
            requestContext = client?.requestContext ?: [:]
            tlsClientParameters = client?.tlsClientParameters ?: [:]
        }
    }

    def validateTimeouts = {cxfClientName, timeoutName, timeoutValue ->
        try {
            if(Integer.parseInt((timeoutValue ?: 0) as String) < 0) {
                throw new CxfClientException("Configured value for ${cxfClientName} ${timeoutName} must be >= 0 if provided. Value provided was(${timeoutValue})")
            }
        } catch (Exception e) {
            throw new CxfClientException("Configured value for ${cxfClientName} ${timeoutName} (${timeoutValue}) caused the exception ${e.message}.")
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
}
