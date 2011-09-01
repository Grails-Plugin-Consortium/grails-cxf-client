class CxfClientGrailsPlugin {
    // the plugin version
    def version = "0.11"
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
    def authorEmail = "acetrike@yahoo.com"
    def title = "Cxf Client Factory"
    def description = '''\\
Used for integrating existing cxf/jaxb services with grails projects.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/cxf-client"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        //todo: need to get this working with auto injection from the plugin
        webServiceClientFactory(com.grails.cxf.client.WebServiceClientFactoryImpl) { bean ->
            bean.autowire = true
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
