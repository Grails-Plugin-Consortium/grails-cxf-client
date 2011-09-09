import com.grails.cxf.client.DynamicWebServiceClient
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler
import org.codehaus.groovy.grails.commons.TagLibArtefactHandler
import org.springframework.beans.factory.FactoryBeanNotInitializedException

class CxfClientGrailsPlugin {
    // the plugin version
    def version = "0.2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = "Christian Oestreich"
    def authorEmail = "acetrike@gmail.com"
    def developers = [
            [name: "Christian Oestreich", email: "acetrike@gmail.com"],
            [name: "Brett Borchardt", email: "bborchardt@gmail.com"]]
    def title = "Cxf Client - Support for CXF and JAXB Soap Clients"
    def description = '''\\
Used for easily integrating existing or new cxf/jaxb web service client code with soap services.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/cxf-client"

    def watchedResources = [
            "file:${getPluginLocation()}/grails-app/services/**/*Service.groovy",
            "file:${getPluginLocation()}/grails-app/controllers/**/*Controller.groovy",
            "file:${getPluginLocation()}/grails-app/taglib/**/*TagLib.groovy"
    ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {

        webServiceClientFactory(com.grails.cxf.client.WebServiceClientFactoryImpl)

        log.info "wiring up cxf-client beans"

        def cxfClientConfigMap = application.config?.cxf?.client ?: [:]

        cxfClientConfigMap.each { cxfClient ->
            def cxfClientName = cxfClient.key
            def client = application.config?.cxf?.client[cxfClientName]

            log.info "wiring up client for $cxfClientName [clientInterface=${client?.clientInterface} and serviceEndpointAddress=${client?.serviceEndpointAddress}]"

            //the call to the map returns a gstring as [:] not as a map literal (fix this?)
            if(!client?.clientInterface || (client.serviceEndpointAddress == "[:]")) {
                String errorMessage = "Web service client $cxfClientName cannot be created before setting the clientInterface=${client?.clientInterface} and serviceEndpointAddress=${client?.serviceEndpointAddress} properties"
                println errorMessage
                log.error errorMessage
                //throw new FactoryBeanNotInitializedException(errorMessage)
            }

            "${cxfClientName}"(DynamicWebServiceClient) {
                webServiceClientFactory = ref("webServiceClientFactory")
                clientInterface = client.clientInterface ?: ""
                serviceName = cxfClientName
                if(client?.secured) {
                    username = client?.username ?: ""
                    password = client?.password ?: ""
                }
                serviceEndpointAddress = client?.serviceEndpointAddress ?: ""
                secured = client?.secured ?: false
            }
        }

        log.info "completed mapping cxf-client beans"
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        if(!isBasePlugin()) {
            if(application.isArtefactOfType(ControllerArtefactHandler.TYPE, event.source)) {
                manager?.getGrailsPlugin("controllers")?.notifyOfEvent(event)
            } else if(application.isArtefactOfType(ServiceArtefactHandler.TYPE, event.source)) {
                manager?.getGrailsPlugin("services")?.notifyOfEvent(event)
            } else if(application.isArtefactOfType(TagLibArtefactHandler.TYPE, event.source)) {
                manager?.getGrailsPlugin("groovyPages")?.notifyOfEvent(event)
            }
        }
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    ConfigObject getBuildConfig() {
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().getClassLoader())
        return new ConfigSlurper().parse(classLoader.loadClass('BuildConfig'))
    }

    String getPluginLocation() {
        return getBuildConfig()?.grails?.plugin?.location?.'cxf-client'
    }
}
