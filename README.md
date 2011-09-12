CXF CLIENT
======

* Introduction
* Wsdl2java Script
* Wsdl2java Manually
* Plugin Configuration
* Demo Project
* Future Revisions

INTRODUCTION
---------------

There are a few different plugins for consuming SOAP web services with grails, but none currently deal with the issue of caching port references.  The ws-client plugin works, but its limitations are in how it creates and consumes the wsdl.  It relies on real time creation of proxy classes and services which can be very processor and memory (time) consuming with a large or complex service contract.  We need a way to speed up service invocation so this plugin was created to facilitate that need when consuming SOAP services using cxf.

The Cxf Client plugin will allow you to use existing (or new) apache cxf wsdl2java generated content and cache the port reference to speed up your soap service end point invocations through an easy configuration driven mechanism.

WSDL2JAVA SCRIPT
---------------

This plugin provides a convenient way to run wsdl2java as a grails run target in your project.  You must have apache cxf installed on your machine somewhere for this to work correctly.

I have mine installed in c:\apps\apache-cxf-2.4.2 so I will add the [installDir] config setting to my configuration node to tell the script where to get grab the cxf classes to put on the classpath.

    cxf {
        installDir = "C:/apps/apache-cxf-2.4.2" //only used for wsdl2java script target
        client {
            ...
        }
    }

After I have done that I need to point the configured clients to a wsdl (either locally or remotely).  This is done by adding the [wsdl] node to the client config as following:

    cxf {
        installDir = "C:/apps/apache-cxf-2.4.2" //only used for wsdl2java script target
        client {
            simpleServiceClient {
                wsdl = "docs/SimpleService.wsdl" //only used for wsdl2java script target
                clientInterface = cxf.client.demo.simple.SimpleServicePortType
                serviceEndpointAddress = "${service.simple.url}"
            }

            //Another example real service to use against wsd2java script
            stockQuoteClient {
                wsdl = "http://www.webservicex.net/stockquote.asmx?WSDL"
                clientInterface = net.webservicex.StockQuoteSoap
                serviceEndpointAddress = "http://www.webservicex.net/stockquote.asmx"
            }
        }
    }

Note: The [installDir] and [wsdl] nodes are only used by the wsdl2java target and are not used in wiring the beans at runtime.

After adding both [installDir] and [wsdl] nodes I can now run the following grails command to generate the cxf/jaxb classes into the src/java directory of the project:

    grails wsdl2java

Thanks to Stefan Armbruster for providing the starting script for this.

WSDL2JAVA MANUALLY
----------------

If you already have a wsdl2java generated object graph and client proxy you can skip this section.

You must be somewhat familiar with how to run wsdl2java.  Run something like the following command to generate jaxb objects and a service proxy adapter Replacing the paths with the something that is applicable for you.  Run this by either having wsdl2java in your path or from the bin of the apache cxf project

    wsdl2java -compile -client -d [output path] [path to wsdl]

here is my string (I have my wsdl manually saved to a wsdl file in the current working dir)

    C:\projects\cxf-client-demo\docs>c:\apps\apache-cxf-2.4.2\bin\wsdl2java -compile -client -d . -p cxf.client.demo.complex ComplexService.wsdl

I then jar up the files to a complex-service-cxf.jar

    C:\projects\cxf-client-demo\docs>jar -cvf complex-service-cxf.jar cxf

Put the jar into your project's lib dir (and generate any more jars you need).  In my case I need to create another for the Simple Service.

    C:\projects\cxf-client-demo\docs>c:\apps\apache-cxf-2.4.2\bin\wsdl2java -compile -client -d . -p cxf.client.demo.simple SimpleService.wsdl
    C:\projects\cxf-client-demo\docs>jar -cvf simple-service-cxf.jar cxf

Note: These could be put in the same jar since the namespace I am using is different cxf.client.demo.complex and cxf.client.demo.simple.

PLUGIN CONFIGURATION
----------------

To wire up the plugin simple install the plugin via:

    grails install-plugin cxf-client

or from the source code you could also package and install from a zip.

Once the plugin is installed and you have your jaxb objects and cxf client port interface in your path (lib or src), you need to add the following to the Config.groovy of your project:

    cxf {
        installDir = [install dir for apache cxf]
        client {
            [beanName] {
                clientInterface = [package and name of wsdl2java -client generated port interface class]
                serviceEndpointAddress = [url for the service]
                secured = [true or false] //optional - defaults to false
                username = [username] //optional - used when secured is true - currently wss4j interceptor
                password = [password] //optional - used when secured is true - currently wss4j interceptor
                wsdl = [location of the wsdl either locally relative to project home dir or a url] //optional - only used by wsdl2java script
            }
        }
    }

<table>
<tr><td>Property</td><td>Description</td><td>Required</td></tr>
<tr><td>beanName</td><td>This can be any name you would like, but should be unique.  This will be the name of the bean the plugin will auto wire and that you will refer to the bean from your service/controller/etc.</td><td><b>Yes</b></td></tr>
<tr><td>clientInterface</td><td>Package name and object name of the wsdl2java generated port interface.</td><td>Yes</td></tr>
<tr><td>serviceEndpointAddress</td><td>Url of the service to call.  Can refer to env specific url as in belows example.</td><td>Yes</td></tr>
<tr><td>secured</td><td>If true will set the cxf client params to use username and password values using WSS4J.</td><td>No</td></tr>
<tr><td>username</td><td>Username to pass along with request in wss4j interceptor when secured is true.</td><td>No</td></tr>
<tr><td>password</td><td>Password to pass along with request in wss4j interceptor when secured is true.</td><td>No</td></tr>
</table>

This is an example of a config file

    //**********************************************************************************************
    // IMPORTANT - these must be set externally to env if you want to refer to them later for use
    // via cxf.  You can also simply hard code the url in the cxf section and NOT refer to a variable
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

```java
class DemoController {
    SimpleServicePortType simpleServiceClient
    ComplexServicePortType complexServiceClient

    def simpleServiceDemo = {
        SimpleRequest request = new SimpleRequest(age: 100, name: "Bob")
        SimpleResponse response = simpleServiceClient.simpleMethod(request)

        render(view: '/index', model: [simpleRequest: request, simpleResponse: response])
    }
}
```

NOTE: You should type the beans with the cxf port interface type so as to get intellisense auto-completion on the service methods. By simply using def you will not know what methods are available on the soap service without peaking into the wsdl or generated client port interface manually.

DEMO PROJECT
---------------

A demo project that includes both a sample service and usage of the cxf-client plugin can be found at

<https://www.github.com/ctoestreich/cxf-client-demo>

FUTURE REVISIONS
---------------

* Ability to dynamically reload endpoint url at runtime
* More integration with soap header security
    * Ability to configure/inject custom security interceptor
