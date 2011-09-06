CXF CLIENT

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. Introduction
2. Wsdl2java
3. Plugin Configuration
4. Future Revisions

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. INTRODUCTION

There are a few different plugins for consuming SOAP web services with grails, but none currently deal with the issue of caching port
references.  The ws-client plugin works, but its limitations are in how it creates and consumes the wsdl.  It relies on realtime
creation of proxy classes and services which can be very time consuming in a large or complex service.  We need a way to speed up service
invocation so this plugin was born.

The Cxf Client plugin will allow you to use existing (or new) cxf wsdl2java generated content and cache the port reference and speed up
your soap service end point invocations.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

2. WSDL2JAVA

If you already have a wsdl2java generated object graph and client proxy you can skip this section.

You must be somewhat familiar with how to run wsdl2java.  Run something like the following command to generate jaxb objects and a service proxy adapter
Replacing the paths with the something that is applicable for you.  Run this by either having wsdl2java in your path or from the bin of the apache cxf
project

wsdl2java -compile -client -d [output path] [path to wsdl]

here is my string (I have my wsdl manually saved to a wsdl file in the current working dir)

    C:\projects\cxf-client-demo\docs>c:\apps\apache-cxf-2.4.2\bin\wsdl2java -compile -client -d . -p cxf.client.demo.complex ComplexService.wsdl

I then jar up the files to a complex-service-cxf.jar

    C:\projects\cxf-client-demo\docs>jar -cvf complex-service-cxf.jar cxf

Put the jar into your project's lib dir (and generate any more jars you need).  In my case I need to create another for the Simple Service.

    C:\projects\cxf-client-demo\docs>c:\apps\apache-cxf-2.4.2\bin\wsdl2java -compile -client -d . -p cxf.client.demo.simple SimpleService.wsdl
    C:\projects\cxf-client-demo\docs>jar -cvf simple-service-cxf.jar cxf

Note: These could be put in the same jar since the namespace I am using is different cxf.client.demo.complex and cxf.client.demo.simple.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

3. PLUGIN CONFIGURATION

To wire up the plugin simple install the plugin via:

    grails install-plugin cxf-client

or from the source code you could also package and install from a zip.

Once the plugin is installed and you have your jaxb objects and cxf client port interface in your path (lib or src), you need to add the following
to the Config.groovy of your project:

cxf {
    client {
        [beanName] {
            clientInterface = [package and name of wsdl2java -client generated port interface class]
            serviceEndpointAddress = [url for the service]
            secured = [true or false] //optional - defaults to false
            securedName = [some name] //optional - defaults to beanName above
        }
    }
}

beanName                - This can be any name you would like, but should be unique.  This will be the name of the bean the plugin will auto wire.  Required.
clientInterface         - Package name and object name of the wsdl2java -client generated port interface.  Required.
serviceEndpointAddress  - Url of the service to call.  Can refer to env specific url as in belows example.  Required.
secured                 - If true will look for system level properties named [serviceName]Username and [serviceName]Password and set
                            the cxf client params to those values using WSS4J.
securedName             - Name of the service.  Will default to bean name, but can be customized to be shared across service beans for global configuration
                            of secured username and password.  eg. You might only want to set a single security username and password that is shared across
                            services called globalServiceUsername and globalServicePassword.  You would then use "globalService" in all your service configurations
                            and they would all refer to the same username and password configuration information.

This is an example of a config file

//**********************************************************************************************
// IMPORTANT - these must be set externally to env if you want to refer to them later for use
// via cxf.  You can also simply hardcode the url in the cxf section and NOT refer to a variable
// as well.
service.simple.url = ""
service.complex.url = ""

// set per-environment service url
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
        service.simple.url = "${grails.serverURL}/services/simple"
        service.complex.url = "${grails.serverURL}/services/complex"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
        service.simple.url = "${grails.serverURL}/services/simple"
        service.complex.url = "${grails.serverURL}/services/complex"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
        service.simple.url = "${grails.serverURL}/services/simple"
        service.complex.url = "${grails.serverURL}/services/complex"
    }
}

cxf {
    client {
        simpleServiceClient {
            clientInterface = cxf.client.demo.simple.SimpleServicePortType
            serviceEndpointAddress = "${service.simple.url}"
        }

        complexServiceClient {
            clientInterface = cxf.client.demo.complex.ComplexServicePortType
            serviceEndpointAddress = "${service.complex.url}"
        }
    }
}
//**********************************************************************************************

You them refer to your services from a controller/service/taglib like the following:

class DemoController {
    SimpleServicePortType simpleServiceClient
    ComplexServicePortType complexServiceClient

    def simpleServiceDemo = {
        SimpleRequest request = new SimpleRequest(age: 100, name: "Bob")
        SimpleResponse response = simpleServiceClient.simpleMethod(request)

        render(view: '/index', model: [simpleRequest: request, simpleResponse: response])
    }
}

NOTE: You should type the beans with the cxf port interface type so as to get intellisense auto-completion on the service methods.
By simply using def you will not know what methods are available on the soap service without peaking into the wsdl or generated
client port interface manually.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

4. FUTURE REVISIONS

- Ability to dynamically reload endpoint url at runtime
- More integration with soap header security



