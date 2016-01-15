BUILD STATUS
==========
[![Build Status](https://travis-ci.org/Grails-Plugin-Consortium/grails-cxf-client.png?branch=master)](https://travis-ci.org/Grails-Plugin-Consortium/grails-cxf-client)

<a name="Top"></a>

CXF CLIENT
======

* <a href="#Introduction">Introduction</a>
* <a href="#Script">WsdlTojava Command</a>
* <a href="#Manually">WsdlTojava Manually</a>
* <a href="#Plugin">Plugin Configuration</a>
* <a href="#Mime">Mime Attachments</a>
* <a href="#Security">Custom Security Interceptors</a>
* <a href="#In">Custom In Interceptors</a>
* <a href="#Out">Custom Out Interceptors</a>
* <a href="#InFault">Custom In Fault Interceptors</a>
* <a href="#OutFault">Custom Out Fault Interceptors</a>
* <a href="#Custom">Custom Http Client Policy</a>
* <a href="#CustomAuth">Custom Authorization Policy</a>
* <a href="#Exceptions">Dealing With Exceptions</a>
* <a href="#Ssl">Setting Secure Socket Protocol</a>
* <a href="#Beans">Using Client Beans Anywhere</a>
* <a href="#Endpoints">Retrieving and Updating Endpoints</a>
* <a href="#console">Enabling Logging of SOAP Messages</a>
* <a href="#Demo">Demo Project</a>
* <a href="#Issues">Issues</a>
* <a href="#Change">Change Log</a>
* <a href="#Future">Future Revisions</a>
* <a href="#License">License</a>

<a name="Introduction"></a>
INTRODUCTION
---------------

There are a few different plugins for consuming SOAP web services with grails, but none currently deal with the issue of caching port references.  The ws-client plugin works, but its limitations are in how it creates and consumes the wsdl.  It relies on real time creation of proxy classes and services which can be very processor and memory (time) consuming with a large or complex service contract.  We need a way to speed up service invocation so this plugin was created to facilitate that need when consuming SOAP services using cxf.

The Cxf Client plugin will allow you to use existing (or new) apache cxf wsdl2java generated content and cache the port reference to speed up your soap service end point invocations through an easy configuration driven mechanism.

<p align="right"><a href="#Top">Top</a></p>
<a name="Script"></a>
WsdlToJava Command
---------------

This plugin provides a convenient way to run wsdl2java as a grails run target in your project.

You will need to put this plugin as a standard dependency AND a classpath dependency as follows

```groovy
buildscript {
    ext {
        grailsVersion = project.grailsVersion
    }
    repositories {
        mavenLocal()
        maven { url "https://repo.grails.org/grails/core" }
        maven { url "https://dl.bintray.com/ctoestreich/grails-plugins" } //Required until grails repo is fixed
    }
    dependencies {
        //other stuff
        classpath "org.grails.plugins:grails-cxf-client:3.0.3" //This line
    }
}

dependencies {
    //other stuff
    compile 'org.grails.plugins:grails-cxf-client:3.0.3' //This line
}
```

First point the configured clients to a wsdl (either locally or remotely).  This is done by adding the [wsdl] node to the client config as following:

```groovy
cxf {
    client {
        simpleServiceClient {
            //used in wsdl2java
            wsdl = "docs/SimpleService.wsdl" //only used for wsdl2java script target
            namespace = "cxf.client.demo.simple"
            client = false //defaults to false
            bindingFile = "grails-app/conf/bindings.xml"
            outputDir = "src/java"
            allowChunking = true //false

            //used for invoking service
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
```

You have the ability to provide wsdl2java custom args.  This is done through the wsdlArgs param.  You may provide this in list format or for a single param in string format.

```groovy
cxf {
    client {
        simpleServiceClient {
            //used in wsdl2java
            wsdl = "docs/SimpleService.wsdl" //only used for wsdl2java script target
            wsdlArgs = ['-autoNameResolution', '-validate']
            //wsdlArgs = '-autoNameResolution' //single param style
            namespace = "cxf.client.demo.simple"
            client = false //defaults to false
            bindingFile = "grails-app/conf/bindings.xml"
            outputDir = "src/java"

            //used for invoking service
            clientInterface = cxf.client.demo.simple.SimpleServicePortType
            serviceEndpointAddress = "${service.simple.url}"
        }
    }
}
```

You may need to use the interface rather than the port type to make your client work:

    ...
    clientInterface = cxf.client.demo.simple.SimpleInterface
    ...

In Grails 3 the default config has change dto YAML.  The following is an example of configured clients with interceptors and other features:

``` yaml
cxf:
    client:
        simpleServiceClient:
            wsdl: docs/SimpleService.wsdl
            wsdlArgs: -autoNameResolution
            clientInterface: cxf.client.demo.simple.SimpleServicePortType
            serviceEndpointAddress: http://localhost:8080/services/simple
            namespace: cxf.client.demo.simple
            httpClientPolicy: customHttpClientPolicy
        customSecureAuthorizationServiceClient:
            wsdl: docs/AuthorizationService.wsdl
            clientInterface: cxf.client.demo.authorization.AuthorizationServicePortType
            serviceEndpointAddress: http://localhost:8080/services/authorization
            namespace: cxf.client.demo.authorization
            authorizationPolicy: customAuthorizationPolicy
        simpleServiceInterceptorClient:
            wsdl: docs/SimpleService.wsdl #only used for wsdl2java script target
            clientInterface: cxf.client.demo.simple.SimpleServicePortType
            serviceEndpointAddress: http://localhost:8080/services/simple
            outInterceptors: customLoggingOutInterceptor #can use single item, comma separated list or groovy list
            inInterceptors:
              - customLoggingInInterceptor
              - verboseLoggingInInterceptor
            enableDefaultLoggingInterceptors: false
            namespace: cxf.client.demo.simple
        complexServiceClient:
            wsdl: docs/ComplexService.wsdl #only used for wsdl2java script target
            clientInterface: cxf.client.demo.complex.ComplexServicePortType
            serviceEndpointAddress: http://localhost:8080/services/complex
            namespace: cxf.client.demo.complex
            receiveTimeout: 120000 #2min
        insecureServiceClient:
            wsdl: docs/SecureService.wsdl #only used for wsdl2java script target
            namespace: cxf.client.demo.secure
            clientInterface: cxf.client.demo.secure.SecureServicePortType
            secured: false
            serviceEndpointAddress: http://localhost:8080/services/secure
        customSecureServiceClient:
            wsdl: docs/SecureService.wsdl
            namespace: cxf.client.demo.secure
            clientInterface: cxf.client.demo.secure.SecureServicePortType
            secured: true
            securityInterceptor: myCustomerSecurityOutInterceptor
            serviceEndpointAddress: http://localhost:8080/services/secure
        customSecureServiceOutClient:
            wsdl: docs/SecureService.wsdl #only used for wsdl2java script target
            namespace: cxf.client.demo.secure
            clientInterface: cxf.client.demo.secure.SecureServicePortType
            secured: true
            securityInterceptor: myCustomerSecurityOutInterceptor
            outInterceptors: customLoggingOutInterceptor #can use single item, comma separated list or groovy list
            inInterceptors:
              - customLoggingInInterceptor
              - verboseLoggingInInterceptor
            enableDefaultLoggingInterceptors: true #true by default (redundant)
            serviceEndpointAddress: http://localhost:8080/services/secure
        secureServiceClient:
            wsdl: docs/SecureService.wsdl #only used for wsdl2java script target
            namespace: cxf.client.demo.secure
            clientInterface: cxf.client.demo.secure.SecureServicePortType
            secured: true
            username: wsuser
            password: password
            serviceEndpointAddress: http://localhost:8080/services/secure
        stockQuoteClient:
            wsdl: http://www.webservicex.net/stockquote.asmx?WSDL
            clientInterface: net.webservicex.StockQuoteSoap
            serviceEndpointAddress: http://www.webservicex.net/stockquote.asmx
            receiveTimeout: 120000 #2min
            connectionTimeout: 120000 #2min
```

This references the following items that are setup in resources.groovy.

``` groovy
import com.cxf.demo.fault.out.interceptor.*
import com.cxf.demo.logging.CustomLoggingInInterceptor
import com.cxf.demo.logging.CustomLoggingOutInterceptor
import com.cxf.demo.logging.VerboseCustomLoggingInInterceptor
import com.cxf.demo.security.CustomSecurityInterceptor
import org.grails.cxf.client.security.DefaultSecurityOutInterceptor
import org.apache.cxf.configuration.security.AuthorizationPolicy
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import org.apache.cxf.transports.http.configuration.ConnectionType

// Place your Spring DSL code here
beans = {


	myCustomInterceptor(CustomSecurityInterceptor)

	myCustomerSecurityOutInterceptor(DefaultSecurityOutInterceptor){
		username = 'wsuser'
		password = 'password'
	}

	customLoggingInInterceptor(CustomLoggingInInterceptor) {
		name = "customLoggingInInterceptor"
	}

	customFaultOutInterceptorSetup(CustomFaultOutInterceptorSetup)
	customFaultOutInterceptorPreLogical(CustomFaultOutInterceptorPreLogical)
	customFaultOutInterceptorUserLogical(CustomFaultOutInterceptorUserLogical)
	customFaultOutInterceptorPostLogical(CustomFaultOutInterceptorPostLogical)
	customFaultOutInterceptorPrepareSend(CustomFaultOutInterceptorPrepareSend)
	customFaultOutInterceptorPreStream(CustomFaultOutInterceptorPreStream)
	customFaultOutInterceptorPreProtocol(CustomFaultOutInterceptorPreProtocol)
	customFaultOutInterceptorWrite(CustomFaultOutInterceptorWrite)
	customFaultOutInterceptorMarshal(CustomFaultOutInterceptorMarshal)
	customFaultOutInterceptorPreProtocol(CustomFaultOutInterceptorPreProtocol)
	customFaultOutInterceptorPostProtocol(CustomFaultOutInterceptorPostProtocol)
	customFaultOutInterceptorPreStream(CustomFaultOutInterceptorPreStream)
	customFaultOutInterceptorPostStream(CustomFaultOutInterceptorPostStream)
	customFaultOutInterceptorSend(CustomFaultOutInterceptorSend)

	verboseLoggingInInterceptor(VerboseCustomLoggingInInterceptor) {
		name = "verboseLoggingInInterceptor"
	}

	customLoggingOutInterceptor(CustomLoggingOutInterceptor) {
		name = "customLoggingOutInterceptor"
	}

	customHttpClientPolicy(HTTPClientPolicy) {
		connectionTimeout = 30000
		receiveTimeout = 60000
		allowChunking = false
		connection = ConnectionType.KEEP_ALIVE
	}

	customAuthorizationPolicy(AuthorizationPolicy) {
		userName = 'wsuser'
		password = 'secret'
		authorizationType = 'Basic'
	}
}
```

Note: The [wsdl] node is only used by the wsdl2java target and are not used in wiring the beans at runtime.

After adding the [wsdl] node I can now run the following grails command to generate the cxf/jaxb classes into the src/main/java directory of the project:

    grails wsdlTojava

The following will also work

    gradle wsdlToJava

<p align="right"><a href="#Top">Top</a></p>
<a name="Manually"></a>
WsdlToJava MANUALLY
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

<p align="right"><a href="#Top">Top</a></p>
<a name="Plugin"></a>
PLUGIN CONFIGURATION
----------------

To wire up the plugin simple install the plugin via:

    grails install-plugin cxf-client

or from the source code you could also package and install from a zip.

Once the plugin is installed and you have your jaxb objects and cxf client port interface in your path (lib or src), you need to add the following to the Config.groovy of your project:

    cxf:
        client:
            [beanName]:
                clientInterface: [package and name of wsdl2java -client generated port interface class]
                serviceEndpointAddress: [url for the service]
                username: [username] //optional - used when secured is true - currently wss4j interceptor
                password: [password] //optional - used when secured is true - currently wss4j interceptor
                securityInterceptor: [text name of custom bean to use] //optional - defaults to wss4j interceptor
                inInterceptors: [list of cxf in interceptors to add to the client] //optional - defaults to []
                outInterceptors: [list of cxf out interceptors to add to the client] //optional - defaults to []
                inFaultInterceptors: [list of cxf in fault interceptors to add to the client] //optional - defaults to []
                outFaultInterceptors: [list of cxf out fault interceptors to add to the client] //optional - defaults to []
                enableDefaultLoggingInterceptors: [turn on or off default in/out logging] //optional - defaults to true
                secured: [true or false] //optional - defaults to false
                connectionTimeout: [Number of milliseconds to wait for connection] //optional - Defaults to 60000 (use 0 to wait infinitely)
                receiveTimeout: [Number of milliseconds to wait to receive a response] //optional - Defaults to 30000 (use 0 to wait infinitely)
                allowChunking: [true or false] //optional - defaults to false
                contentType: [String value of http content type] - defaults to 'text/xml; charset=UTF8'
                connection: [Enum of ConnectionType (ConnectionType.CLOSE, ConectionType.KEEP_ALIVE) for type of connection] - defaults to ConnectionType.CLOSE
                httpClientPolicy: [text name of custom bean to use] //optional - defaults to null
                authorizationPolicy: [text name of custom bean to use] //optional - defaults to null
                proxyFactoryBindingId: [binding id uri if required] //optional - defaults to null
                mtomEnabled: [flag to enable mtom] //optional - defaults to false
                secureSocketProtocol: [socket protocol to use for secure service] //optional - defaults to null
                wsdlServiceName: [set to enable mime type mapping] //optional - defaults to null
                wsdlEndpointName: [may be needed for correct wsdl initialization] //optional - defaults to null
                requestContext: [Setting a Request Context Property on the Client Side] //optional - defaults to [:]
                tlsClientParameters: [conduit settings for secure services] //optional - defaults to [:]

                //wsdl config
                wsdl: [location of the wsdl either locally relative to project home dir or a url] //optional - only used by wsdl2java script
                wsdlArgs: [custom list of args to pass in seperated by space such as ["-autoNameResolution", "-validate"]] //optional - only used by wsdl2java script
                namespace: [package name to use for generated classes] //optional - uses packages from wsdl if not provided
                client: [true or false] //optional - used to tell wsdl2java to output sample clients, usually not needed - defaults to false
                bindingFile: [Specifies JAXWS or JAXB binding file or XMLBeans context file] //optional
                outputDir: [location to output generated files] //optional - defaults to src/java

Config used at runtime to invoke service.

<table>
<tr><td><b>Property</b></td><td><b>Description</b></td><td>Required</b></td></tr>
<tr><td>beanName</td><td>This can be any name you would like, but should be unique.  This will be the name of the bean the plugin will auto wire and that you will refer to the bean from your service/controller/etc.</td><td><b>Yes</b></td></tr>
<tr><td>clientInterface</td><td>Package name and object name of the wsdl2java generated port interface.</td><td><b>Yes</b></td></tr>
<tr><td>serviceEndpointAddress</td><td>Url of the service to call.  Can refer to env specific url as in belows example.</td><td><b>Yes</b></td></tr>
<tr><td>username</td><td>Username to pass along with request in wss4j interceptor when secured is true. (default: "")</td><td>No</td></tr>
<tr><td>password</td><td>Password to pass along with request in wss4j interceptor when secured is true. (default: "")</td><td>No</td></tr>
<tr><td>securityInterceptor</td><td>Provide a single bean name as a string to wire in as an out interceptor for apache cxf.  If you provide a name for an interceptor, it will be implied that secured=true.  If you require the default wss4j interceptor you will not need to set this property, simply set the secured=true and the username and password properties.  If you set this to a value then the username and password fields will be ignored as it is expected that you will configure any required property injection in your resources.groovy file.  You may also provide your custom security
interceptor in the outInterceptors property as well.  You would still be required to set secured=true.  This is here as a convenience to any existing configured clients that do not wish to switch to using the newer outInterceptors property.  See below for examples (default: "")</td><td>No</td></tr>
<tr><td>inInterceptors</td><td>Provide a bean name or list of bean names in "name", "name, name" or ["name","name"] format to wire in as an in interceptor for apache cxf.  If you set it is expected that you will configure the beans in the resources.groovy file.  See below for examples (default: [])</td><td>No</td></tr>
<tr><td>outInterceptors</td><td>Provide a bean name or list of bean names in "name", "name, name" or ["name","name"] format to wire in as an out interceptor for apache cxf.  If you set it is expected that you will configure the beans in the resources.groovy file.  See below for examples (default: [])</td><td>No</td></tr>
<tr><td>inFaultInterceptors</td><td>Provide a bean name or list of bean names in "name", "name, name" or ["name","name"] format to wire in as an in fault interceptor for apache cxf.  If you set it is expected that you will configure the beans in the resources.groovy file.  See below for examples (default: [])</td><td>No</td></tr>
<tr><td>outFaultInterceptors</td><td>Provide a bean name or list of bean names in "name", "name, name" or ["name","name"] format to wire in as an out fault interceptor for apache cxf.  If you set it is expected that you will configure the beans in the resources.groovy file.  See below for examples (default: [])</td><td>No</td></tr>
<tr><td>enableDefaultLoggingInterceptors</td><td>When set to true, default in and out logging interceptors will be added to the service.  If you require custom logging interceptors and wish to turn off the default loggers for any reason (security, custom, etc), set this property to false and provide your own in and out logging interceptors via the inInterceptors or outInterceptors properties.  You may also simply wish to disable logging of cxf (soap messages, etc) by setting this to false without providing your own interceptors.  (default: true)</td><td>No</td></tr>
<tr><td>connectionTimeout</td><td>Specifies the amount of time, in milliseconds, that the client will attempt to establish a connection before it times out. The default is 30000 (30 seconds). 0 specifies that the client will continue to attempt to open a connection indefinitely. (default: 30000)</td><td>No</td></tr>
<tr><td>receiveTimeout</td><td>Specifies the amount of time, in milliseconds, that the client will wait for a response before it times out. The default is 60000. 0 specifies that the client will wait indefinitely. (default: 60000)</td><td>No</td></tr>
<tr><td>secured</td><td>If true will set the cxf client params to use username and password values using WSS4J. (default: false)</td><td>No</td></tr>
<tr><td>allowChunking</td><td>If true will set the HTTPClientPolicy allowChunking for the clients proxy to true. (default: false)</td><td>No</td></tr>
<tr><td>contentType</td><td>Allows user to override the content type of the http policy default of 'text/xml; charset=UTF8'.  Might want to set to "application/soap+xml; charset=UTF-8" for example.</td><td>No</td></tr>
<tr><td>conection</td><td>Allows user to override the connection type of the http policy default of 'ConnectionType.CLOSE'.  Can attempt to reuse connections via ConnectionType.KEEP_ALIVE</td><td>No</td></tr>
<tr><td>httpClientPolicy</td><td>Instead of using the separate timeout, chunking, etc values you can create your own HTTPClientPolicy bean in resources.groovy and pass the name of the bean here. <B>This will override the connectionTimeout, receiveTimeout and allowChunking values.</b> (default: null)</td><td>No</td></tr>
<tr><td>authorizationPolicy</td><td>Name of a bean in resources.groovy of type AuthorizationPolicy that will be used in the httpConduit.</b> (default: null)</td><td>No</td></tr>
<tr><td>proxyFactoryBindingId</td><td>The URI, or ID, of the message binding for the endpoint to use. For SOAP the binding URI(ID) is specified by the JAX-WS specification. For other message bindings the URI is the namespace of the WSDL extensions used to specify the binding.  If you would like to change the binding (to use soap12 for example) set this value to "http://schemas.xmlsoap.org/wsdl/soap12/". (default: "")</td><td>No</td></tr>
<tr><td>mtomEnabled</td><td>SOAP Message Transmission Optimization Mechanism (MTOM) specifies an optimized method for sending binary data as part of a SOAP message. Unlike SOAP with Attachments, MTOM requires the use of XML-binary Optimized Packaging (XOP) packages for transmitting binary data. Using MTOM to send binary data does not require you to fully define the MIME Multipart/Related message as part of the SOAP binding. (default: false)</td><td>No</td></tr>
<tr><td>secureSocketProtocol</td><td>The Secure socket protocol to use for secure services.  This will be set on the cxf http object that is created for communication to the service.  If you don't specify, I believe that cxf will default to "TLS" when invoking https services endpoints.  Most common example are "SSL", "TLS" or "TLSv1".  (default: "")</td><td>No</td></tr>
<tr><td>wsdl</td><td>Location of the wsdl either locally or a url (must be available at runtime).  Will be passed into JaxWsProxyFactoryBean.  WSDL will be loaded to handle things that cannot be captured in Java classes via wsdl2java (like MIME attachments). Requires defining _wsdlServiceName_. (default: null)</td><td>No</td></tr>
<tr><td>wsdlServiceName</td><td>The QName of the service you will be accessing.  Will be passed into JaxWsProxyFactoryBean.  Only needed when using WSDL at run-time to handle things that cannot be captured in Java classes via wsdl2java. (example: '{http://my.xml.namespace/}TheNameOfMyWSDLServicePorts') (default: null)</td><td>No</td></tr>
<tr><td>wsdlEndpointName</td><td>The QName of the endpoint/port in the WSDL you will be accessing.  Will be passed into JaxWsProxyFactoryBean.  May be needed when using WSDL at run-time to handle things that cannot be captured in Java classes via wsdl2java. (example: '{http://my.xml.namespace/}TheNameOfMyWSDLServicePort') (default: null)</td><td>No</td></tr>
<tr><td>requestContext</td><td>Setting a Request Context Property on the Client Side. (default: [:])</td><td>No</td></tr>
<tr><td>tlsClientParameters</td><td>Configuration parameters for the secure conduit.  (default: [:])</td><td>No</td></tr>
</table>

Config items used by wsdl2java.

<table>
<tr><td><b>Property</b></td><td><b>Description</b></td><td>Required</b></td></tr>
<tr><td>wsdl</td><td>Location of the wsdl either locally relative to project home dir or a url. (default: "")</td><td>No</td></tr>
<tr><td>wsdlArgs</td><td>A custom list of args to pass in seperated by space such as ["-autoNameResolution","-validate"].  This can also be a single string value such as "-autoNameResolution", but when using multiple custom params you must specify each in a list ["-one val","-two","-three val"] due to limitations with ant. (default: "")</td><td>No</td></tr>
<tr><td>namespace</td><td>Specifies package names to use for the generated code. (default: "use wsdl provided schema")</td><td>No</td></tr>
<tr><td>client</td><td>Used to tell wsdl2java to output sample clients, usually not needed. (default: false)</td><td>No</td></tr>
<tr><td>bindingFile</td><td>Path of binding file to pass to wsdl2java. (default: "")</td><td>No</td></tr>
<tr><td>outputDir</td><td>Password to pass along with request in wss4j interceptor when secured is true. (default: "src/java")</td><td>No</td></tr>
</table>



You simply refer to your client beans from a controller/service/taglib like the following:

```groovy
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

_**NOTE:** You should type the beans with the cxf port interface type so as to get intellisense auto-completion on the service methods. By simply using def you will not know what methods are available on the soap service without peaking into the wsdl or generated client port interface manually._


<p align="right"><a href="#Top">Top</a></p>
<a name="Mime"></a>
MIME ATTACHMENTS
----------------
Functionality was recently added by Kyle Dickerson to support mime type attachements in a response.  To do this you will need to set both the _wsdl_ and _wsdlServiceName_ properties.  This is done so that cxf will be able to resolve correctly the attachment data against the wsdl.  If you fail to set these you may cause an IndexOutOfBounds thrown from cxf.  You may need to define _wsdlEndpointName_ as well.

<p align="right"><a href="#Top">Top</a></p>
<a name="Security"></a>
CUSTOM SECURITY INTERCEPTORS
---------------

As a convenience to the user I created an interface to inherit from that allows you to customize the specifics of the interceptor without having to inherit all the contract methods for the cxf interceptors.  You simply have to inherit from CxfClientInterceptor in the org.grails.cxf.client.security package.  Here is the custom interceptor I created for the demo project.

```groovy
package org.grails.cxf.client.security

import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.WSConstants
import org.apache.wss4j.dom.handler.WSHandlerConstants
import org.grails.cxf.client.CxfClientInterceptor
import org.grails.cxf.client.exception.CxfClientException

import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.UnsupportedCallbackException

class DefaultSecurityOutInterceptor implements CxfClientInterceptor {

    String password
    String username

    WSS4JOutInterceptor create() {
        if(!username?.trim() || !password) {
            throw new CxfClientException('Username and password are not configured for calling secure web services')
        }

        Map<String, Object> outProps = [:]
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN)
        outProps.put(WSHandlerConstants.USER, username)
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT)
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = callbacks[0]
                pc.password = password
            }
        })
        new WSS4JOutInterceptor(outProps)
    }
}

```

You have to make sure your create method returns an object that already inherits from the appropriate classes such as an WSS4JOutInterceptor as I used here.  It is technically possible for your interceptor to extend something like SoapHeaderInterceptor, you will just be responsible for overriding all the appropriate methods yourself.  You can see the <a href="http://www.technipelago.se/content/technipelago/blog/basic-authentication-grails-cxf">following example</a> on how to define a basic auth interceptor on the server side.
More specifically refer to <a href="http://chrisdail.com/download/BasicAuthAuthorizationInterceptor.java">this file</a> for sample code on create your own interceptor or to the <a href="https://github.com/Grails-Plugin-Consortium/grails-cxf-client-demo/blob/master/grails-app/conf/BootStrap.groovy">demo project file</a> that injects a server side interceptor. Perhaps the <B>best documentation</b> on writing a complex interceptor can be found at the <a href="http://cxf.apache.org/docs/interceptors.html">Apache CXF</a> site.

In the case of the above CustomSecurityInterceptor, you would then place the following in your projects resources.groovy.

```groovy
beans = {
	myCustomerSecurityOutInterceptor(DefaultSecurityOutInterceptor){
        username = 'wsuser'
        password = 'password'
    }
}
```

The last step to hooking up the custom interceptor is to define the securityInterceptor for the client config block.  The myCustomInterceptor bean can be hooked up by adding the line in the config below.

```yaml
cxf:
    client:
        customSecureServiceClient:
            wsdl: docs/SecureService.wsdl
            namespace: cxf.client.demo.secure
            clientInterface: cxf.client.demo.secure.SecureServicePortType
            secured: true
            securityInterceptor: myCustomerSecurityOutInterceptor
            serviceEndpointAddress: http://localhost:8080/services/secure
```

<p align="right"><a href="#Top">Top</a></p>
<a name="In"></a>
CUSTOM IN INTERCEPTORS
---------------

You can wire in your own custom in interceptors by adding the property inInterceptors to the configured client.  In this example I have chosen to wire in my own in logging interceptors and have disabled the default logging interceptors by setting enableDefaultLoggingInterceptors = false.

In the resources.groovy define our bean wiring.

```groovy
customLoggingInInterceptor(CustomLoggingInInterceptor) {
    name = "customLoggingInInterceptor"
}

verboseLoggingInInterceptor(VerboseCustomLoggingInInterceptor) {
    name = "verboseLoggingInInterceptor"
}

 customLoggingOutInterceptor(CustomLoggingOutInterceptor) {
    name = "customLoggingOutInterceptor"
}
```

In the Config.groovy cxf { client { ... } } block define a webservice client and provide the interceptor bean name(s).

```yaml
cxf:
  client:
      simpleServiceInterceptorClient:
          wsdl: docs/SimpleService.wsdl #only used for wsdl2java script target
          clientInterface: cxf.client.demo.simple.SimpleServicePortType
          serviceEndpointAddress: http://localhost:8080/services/simple
          outInterceptors: customLoggingOutInterceptor #can use single item, comma separated list or groovy list
          inInterceptors:
            - customLoggingInInterceptor
            - verboseLoggingInInterceptor
          enableDefaultLoggingInterceptors: false
          namespace: cxf.client.demo.simple

```

Here is the code for my customLoggingInInterceptor (verboseLoggingInInterceptor is almost identical)

```groovy
package com.cxf.demo.logging

import org.apache.cxf.common.injection.NoJSR250Annotations
import org.apache.cxf.common.logging.LogUtils
import org.apache.cxf.interceptor.AbstractLoggingInterceptor
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.message.Message
import org.apache.cxf.phase.Phase

import java.util.logging.Logger

/**
 *
 */
@NoJSR250Annotations
public class CustomLoggingInInterceptor extends AbstractLoggingInterceptor {

	private static final Logger LOG = LogUtils.getLogger(CustomLoggingInInterceptor)
	def name
	//SimpleServicePortType simpleServiceClient

	public CustomLoggingInInterceptor() {
		super(Phase.RECEIVE);
		log LOG, "Creating the custom interceptor bean"
	}

	public void handleMessage(Message message) throws Fault {
		//get another web service bean here by name and call it
//        SimpleServicePortType simpleServiceClient = ApplicationHolder.application.mainContext.getBean("simpleServiceClient")
//        log LOG, "status is " + simpleServiceClient.simpleMethod1(new cxf.client.demo.simple.SimpleRequest(age: 30, name: 'Test')).status
		log LOG, "$name :: I AM IN CUSTOM IN LOGGER!!!!!!!"
	}

	@Override
	protected Logger getLogger() {
		LOG
	}
}
```

_**NOTE:** In your constructor you will need to be mindful what Phase you set your interceptor for.  Please see the docs at <http://cxf.apache.org/docs/interceptors.html>_

You will need to set the logging level in the log4j config section to enable the logging

```groovy
info 'org.grails.cxf.client'
info 'org.apache.cxf.interceptor'
info 'blah.blah.blah' //whatever package your custom interceptors are in
//debug 'org.apache.cxf.interceptor' //choose appropriate level
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Out"></a>
CUSTOM OUT INTERCEPTORS
---------------
You can wire in your own custom out interceptors by adding the property outInterceptors to the configured client.  In this example I have chosen to wire in my own out logging interceptors.

```yaml
cxf:
    client:
        simpleServiceInterceptorClient:
            wsdl: docs/SimpleService.wsdl #only used for wsdl2java script target
            clientInterface: cxf.client.demo.simple.SimpleServicePortType
            serviceEndpointAddress: http://localhost:8080/services/simple
            outInterceptors: customLoggingOutInterceptor #can use single item, comma separated list or groovy list
            inInterceptors:
              - customLoggingInInterceptor
              - verboseLoggingInInterceptor
            enableDefaultLoggingInterceptors: false
            namespace: cxf.client.demo.simple

```

Here is the code for my customLoggingOutInterceptor

```groovy
package com.cxf.demo.logging

import org.apache.cxf.common.injection.NoJSR250Annotations
import org.apache.cxf.common.logging.LogUtils
import org.apache.cxf.interceptor.AbstractLoggingInterceptor
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.message.Message
import org.apache.cxf.phase.Phase

import java.util.logging.Logger

/**
 *
 */
@NoJSR250Annotations
public class CustomLoggingOutInterceptor extends AbstractLoggingInterceptor {

	private static final Logger LOG = LogUtils.getLogger(CustomLoggingOutInterceptor)
	def name

	public CustomLoggingOutInterceptor() {
		super(Phase.WRITE);
		log LOG, "Creating the custom interceptor bean"
	}

	public void handleMessage(Message message) throws Fault {
		log LOG, "$name :: I AM IN CUSTOM OUT LOGGER!!!!!!!"
	}

	@Override
	protected Logger getLogger() {
		LOG
	}
}
```

**Note:** Since the out interceptor is in PRE_STREAM phase (but PRE_STREAM phase is removed in MESSAGE mode), you have to configure out interceptors to be run at write phase.

**Note:** In your constructor you will need to be mindful what Phase you set your interceptor for.  Please see the docs at <http://cxf.apache.org/docs/interceptors.html>

You will need to set the logging level in the log4j config section to enable the logging

```groovy
info 'org.grails.cxf.client'
info 'org.apache.cxf.interceptor'
info 'blah.blah.blah' //whatever package your custom interceptors are in
//debug 'org.apache.cxf.interceptor' //choose appropriate level
```

<p align="right"><a href="#Top">Top</a></p>
<a name="InFault"></a>
CUSTOM IN FAULT INTERCEPTORS
---------------

You can wire in your own custom in fault interceptors by adding the property inFaultInterceptors to the configured client.  Example coming soon, but should be similar to the earlier two examples.

You will need to set the logging level in the log4j config section to enable the logging

```groovy
info 'org.grails.cxf.client'
info 'org.apache.cxf.interceptor'
info 'blah.blah.blah' //whatever package your custom interceptors are in
//debug 'org.apache.cxf.interceptor' //choose appropriate level
```


<p align="right"><a href="#Top">Top</a></p>
<a name="OutFault"></a>
CUSTOM OUT FAULT INTERCEPTORS
---------------

You can wire in your own custom out fault interceptors by adding the property outFaultInterceptors to the configured client.  Example coming soon, but should be similar to the earlier two examples.

You will need to set the logging level in the log4j config section to enable the logging

```groovy
info 'org.grails.cxf.client'
info 'org.apache.cxf.interceptor'
info 'blah.blah.blah' //whatever package your custom interceptors are in
//debug 'org.apache.cxf.interceptor' //choose appropriate level
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Custom"></a>
CUSTOM HTTP CLIENT POLICY
---------------

If you simply need to set the connectionTimeout, receiveTimeout, or allowChunking you may use the three provided params to accomplish this.  If you require more fine grained control of the HTTPClientPolicy you can create a custom bean in the resources.groovy and tell your cxf client to use it via the code below.

_Note: A configured httpClientPolicy will take precedence over the connection, connectionTimeout, receiveTimeout and allowChunking. Setting all four params in the config will cause the httpClientPolicy to be used and the others ignored._

resources.groovy

```groovy
beans = {
    customHttpClientPolicy(HTTPClientPolicy){
        connectionTimeout = 30000
        receiveTimeout = 60000
        allowChunking = false
        autoRedirect = false
        connection = org.apache.cxf.transports.http.configuration.ConnectionType.KEEP_ALIVE
    }
}
```

Config.groovy

```yaml
cxf:
    client:
        simpleServiceClient:
            wsdl: docs/SimpleService.wsdl
            wsdlArgs: -autoNameResolution
            clientInterface: cxf.client.demo.simple.SimpleServicePortType
            serviceEndpointAddress: http://localhost:8080/services/simple
            namespace: cxf.client.demo.simple
            httpClientPolicy: customHttpClientPolicy

```

Note: If you incorrectly refer to your new beans name (spelling, etc) you will get an exception such as `...Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'blahblah' is defined` error.

<p align="right"><a href="#Top">Top</a></p>
<a name="CustomAuth"></a>
CUSTOM AUTHORIZATION POLICY
---------------

If you simply need to set Authorization Policy you can create a custom bean in the resources.groovy and tell your cxf client to use it via the code below.
resources.groovy

```groovy
beans = {
    customAuthorizationPolicy(AuthorizationPolicy){
        userName = 'user'
        password = 'password'
    }
}
```

Config.groovy

```groovy
cxf:
    client:
        customSecureAuthorizationServiceClient:
            wsdl: docs/AuthorizationService.wsdl
            clientInterface: cxf.client.demo.authorization.AuthorizationServicePortType
            serviceEndpointAddress: http://localhost:8080/services/authorization
            namespace: cxf.client.demo.authorization
            authorizationPolicy: customAuthorizationPolicy
```

Note: If you incorrectly refer to your new beans name (spelling, etc) you will get an exception such as `...Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'blahblah' is defined` error.

<p align="right"><a href="#Top">Top</a></p>
<a name="Exceptions"></a>
DEALING WITH EXCEPTIONS
---------------
As of version 1.2.9 of the plugin, I have fixed the issue so your services with checked exceptions defined will not throw them as designed.  Given some service that throws an exception (ComplexContrivedException_Exception in our case) as follows:

```java
@WebResult(name = "return", targetNamespace = "")
@RequestWrapper(localName = "complexMethod3", targetNamespace = "http://demo.client.cxf/", className = "cxf.client.demo.complex.ComplexMethod3")
@WebMethod
@ResponseWrapper(localName = "complexMethod3Response", targetNamespace = "http://demo.client.cxf/", className = "cxf.client.demo.complex.ComplexMethod3Response")
public cxf.client.demo.complex.ComplexResponse complexMethod3(
    @WebParam(name = "request", targetNamespace = "")
    cxf.client.demo.complex.ComplexRequest request
) throws ComplexContrivedException_Exception;
```

the checked exceptions can now be caught in code and dealt with as desired:

```groovy
try {
    response1 = complexServiceClient.complexMethod3(request1)
} catch (ComplexContrivedException_Exception e) {
    log.error e
}
```

In addition, any general SOAPFaultException thrown can be caught as well.

```groovy
try {
    response1 = complexServiceClient.complexMethod3(request1)
} catch (ComplexContrivedException_Exception e) {
    log.error e
} catch (SOAPFaultException e) {
    log.error e
}
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Ssl"></a>
SETTING SECURE SOCKET PROTOCOL
---------------

**TODO: Not supported in grails 3 yet!**

If you would like to set the secure socket protocol for a secure service you can use the `CxfClientConstants` class to set the bean constructor.  The types provided are:

```groovy
public static final String SSL_PROTOCOL_TLSV1 = 'TLSv1'
public static final String SSL_PROTOCOL_SSLV3 = 'SSLv3'
```

You may also provide configuration for via the tlsClientParameters for the client.  Using this you can set any of the following:

disableCNCheck: [boolean]
sslCacheTimeout: [integer]
secureSocketProtocol: [CxfClientConstants.SSL_PROTOCOL_SSLV3]
useHttpsURLConnectionDefaultSslSocketFactory: [boolean]
useHttpsURLConnectionDefaultHostnameVerifier: [boolean]
cipherSuitesFilter.exclude: [List<String>]
cipherSuitesFilter.include: [List<String>]

This is done via the configuration block such as:

Config.groovy

```groovy
cxf {
    client {
        simpleServiceClient {
            ...
            tlsClientParameters = [
                disableCNCheck: true,
                sslCacheTimeout: 100,
                secureSocketProtocol: CxfClientConstants.SSL_PROTOCOL_SSLV3
                cipherSuitesFilter.include = ['.*_EXPORT_.*','.*_EXPORT1024_.*']
                cipherSuitesFilter.exclude = ['.*_DH_anon_.*']
            ]
        }
    }
}
```

Either may be used, but the secureSocketProtocol takes precedent for setting the protocol.  Both `secureSocketProtocol` and `tlsClientParameters` may be used in conjunction, but it is preferred if you want to set more than just the protocol to use the tlsClientParameters map.

Not all features for http conduit are supported.  You can read more about conduit settings at <https://cwiki.apache.org/confluence/display/CXF20DOC/TLS+Configuration>.

<p align="right"><a href="#Top">Top</a></p>
<a name="Beans"></a>
USING CLIENT BEANS ANYWHERE
---------------
If you require useage of the web service clients you can access them anywhere by accessing them by name.  The name of the bean will match the name of the configured client in your Config.groovy.

<p align="right"><a href="#Top">Top</a></p>
<a name="Endpoints"></a>
RETRIEVING AND UPDATING ENDPOINTS
---------------
The service endpoint address for any given service can be retrieved and updated at runtime using the WebserviceClientFactory interface.

I have synchronized the access to update address method to ensure thread safe access to the underlying service map.  The method signature is as follows:

```groovy
@Synchronized void updateServiceEndpointAddress(String serviceName, String serviceEndpointAddress) throws UpdateServiceEndpointException
```

To retrieve an endpoint via code:

```groovy
WebserviceClientFactory webserviceClientFactory

String endpointAddress = webserviceClientFactory.getServiceEndpointAddress('simpleServiceClient')
```

To update an endpoint:

```groovy
WebserviceClientFactory webserviceClientFactory

webserviceClientFactory.updateServiceEndpointAddress('simpleServiceClient', 'http://www.changeme.com/services/newURL')
```

If no service endpoint is found matching the `serviceName` or if an empty name is passed, an UpdateServiceEndpointException will be thrown.  This was done to give concrete feedback of an endpoint update failure.

<p align="right"><a href="#Top">Top</a></p>
<a name="console"></a>

ENABLING LOGGING OF SOAP MESSAGES
---------------

**Todo: Update for grails 3**

If you would like to view the raw soap in the console/log files add the follow:

JVM startup params:
```
-Dorg.apache.cxf.Logger=org.apache.cxf.common.logging.Log4jLogger
```

Logging config:
```groovy
log4j {
    ...
  info 'org.apache.cxf' //debug, etc
}
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Demo"></a>
DEMO PROJECT
---------------

A grails 3 demo project that includes both a sample service and usage of the cxf-client plugin can be found at <https://github.com/Grails-Plugin-Consortium/grails-cxf-client-demo>

I have also included the full code on how to inject a custom security interceptor in the demo project.

<p align="right"><a href="#Top">Top</a></p>
<a name="Issues"></a>
ISSUES
---------------

To submit an issue please use <https://github.com/Grails-Plugin-Consortium/grails-cxf-client/issues>.

Currently there is an issue with pointing to a secure endpoint and running the wsdl2java script.  If you get an error message like

    PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

You may need to put a cert into your [jdkhome]\jre\lib\security directory.  I will be working on getting this working working by adding the cert to the service end point configuration in an upcoming release.

Another solution is to get the wsdl from the web and copy into a local file.wsdl and change the config to point to a local file instead of the https endpoint for generation.

If your compile fails on the Client classes you may need to add

```
wsdlArgs = ['-autoNameResolution','-frontend','jaxws21']
```

to your service args.  The autoNameResolution to resolve duplicate or recursive entries in the wsdl and the frontend set to jaxws21 to force the generated classes to conform with 2.1 standards.

WSDL2JAVA

It appears there was a change that caused classpath issues with the ant wsdl2java tools.  If this fails to run you can upgrade to version 1.6.1+ of the plugin or add the following into your dependencies block of your project.

```
compile("${cxfGroup}:cxf-tools-wsdlto-core:${cxfVersion}") {
    excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
}

compile("${cxfGroup}:cxf-tools-wsdlto-frontend-jaxws:${cxfVersion}") {
    excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
}

compile("${cxfGroup}:cxf-tools-wsdlto-databinding-jaxb:${cxfVersion}") {
    excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
}
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Change"></a>
CHANGE LOG
---------------
* v 3.0.4
	* Adding support for ConnectionType (eg. KEEP_ALIVE and CLOSE) on the http connection

* v 3.0.1-3.0.3
    * Minor bug fixes
    * Rename plugin to remove the G3 from the name
    * Grails 3

* v 3.0.0
    * Moving to CXF 3.1.x
    * Grails 3

* v 2.1.1
    * Moving to WSS4j 2.0.3 as this is required (2+) for use with CXF 3+.


* v 2.1
    * Moving to CXF 3.0.4 in preparation for grails 3 move


* v 2.0.3
    * Fixing soap12 support
    * Adding mtomEnabled configuration flag


* v 2.0.2
	* Adding Fix for external properties timeout number format exception casting int->string


* v 2.0
	* Moving to cxf 2.6.6
	* Rabasing support for grails 2.2+
	* Update grails version
    * Removed spock plugin (now bundled with grails)


* v 1.6.2
	* Adding AuthorizationPolicy support for clients


* v 1.6.1
	* Fixing wsdl2java script which appears broken in 2.3+


* v 1.5.5
    * Removing compile from the wsdl2java script as a dependency.


* v 1.5.4
    * Adding tlsClientParameters to set disableCNCheck, sslCacheTimeout and secureSocketProtocol.


* v 1.5.1
    * Adding contentType param to allow different http client policy content types. See: [The Client Element](http://cxf.apache.org/docs/client-http-transport-including-ssl-support.html#ClientHTTPTransport%28includingSSLsupport%29-The{{client}}element)


* v 1.5.0
    * Adding requestContext param to inject onto the client port object. See: [Setting Connection Properties with Contexts](http://cxf.apache.org/docs/developing-a-consumer.html)


* v1.4.8
    * No logical code changes, code cleanup and removal of unused items - burtbeckwith


* v1.4.7
    * Fixing issue with scope for some testing plugins
    * Adding excludes for slf4j to a few dependencies


* v1.4.6
    * Removing some jar deps from plugin causing issue with other plugins


* v1.4.5
    * Reverted the use of @Commons to make app compatible with 1.3.0+
    * Added parameter for secureSocketProtocol to specify protocol.  Constants were added for this in CxfClientConstants class.


* v1.4.4
    * Adding inFaultInterceptor support


* v1.4.0
    * Updating the wsdl2java script to not require the installPath any longer
    * Updating cxf to version 2.6.1 to match the cxf plugin


* v1.3.0
    * Adding ability to update endpoint during runtime if needed - Thanks to Laura Helde for finalizing this work.
    * Adding reponse mime attachement support - Thanks to Kyle Dickerson for helping with this issue.


* v1.2.9
    * Adding better exception handling
    * Checked exceptions will bubble correctly
    * SOAPFault Exceptions will also bubble correctly
    * Fixed bug from config reader from 1.2.8


* v1.2.8
    * Ability to set proxyFactoryBindingId if you require different binding such as soap12


* v1.2.7
    * Ability to set allowChunking
    * Ability to specify custom HTTPClientPolicy bean to use for the client (see [demo project][https://github.com/Grails-Plugin-Consortium/grails-cxf-client-demo/blob/master/grails-app/conf/spring/resources.groovy] for more details)


* v1.2.6
    * Ability to set connectionTimeout and recieveTimeout for the client proxy


<p align="right"><a href="#Top">Top</a></p>
<a name="Future"></a>
FUTURE REVISIONS
---------------

Currently taking submissions for improvements.


<p align="right"><a href="#Top">Top</a></p>
<a name="License"></a>
LICENSE
---------------

Copyright 2012 Christian Oestreich

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.