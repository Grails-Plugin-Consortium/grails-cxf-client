<a name="Top"></a>

CXF CLIENT
======

* <a href="#Introduction">Introduction</a>
* <a href="#Script">Wsdl2java Script</a>
* <a href="#Manually">Wsdl2java Manually</a>
* <a href="#Plugin">Plugin Configuration</a>
* <a href="#Mime">Mime Attachments</a>
* <a href="#Security">Custom Security Interceptors</a>
* <a href="#In">Custom In Interceptors</a>
* <a href="#Out">Custom Out Interceptors</a>
* <a href="#Fault">Custom Out Fault Interceptors</a>
* <a href="#Custom">Custom Http Client Policy</a>
* <a href="#Exceptions">Dealing With Exceptions</a>
* <a href="#Beans">User Client Beans Anywhere</a>
* <a href="#Endpoints">Retrieving and Updating Endpoints</a>
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
WSDL2JAVA SCRIPT
---------------

This plugin provides a convenient way to run wsdl2java as a grails run target in your project.

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

Starting with version 1.2.4, you have the ability to provide wsdl2java custom args.  This is done through the wsdlArgs param.  You may provide this in list format or for a single param in string format.

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

Note: The [wsdl] node is only used by the wsdl2java target and are not used in wiring the beans at runtime.

After adding the [wsdl] node I can now run the following grails command to generate the cxf/jaxb classes into the src/java directory of the project:

    grails wsdl2java

Thanks to Stefan Armbruster for providing the starting script for this.

<p align="right"><a href="#Top">Top</a></p>
<a name="Manually"></a>
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

<p align="right"><a href="#Top">Top</a></p>
<a name="Plugin"></a>
PLUGIN CONFIGURATION
----------------

To wire up the plugin simple install the plugin via:

    grails install-plugin cxf-client

or from the source code you could also package and install from a zip.

Once the plugin is installed and you have your jaxb objects and cxf client port interface in your path (lib or src), you need to add the following to the Config.groovy of your project:

    cxf {
        client {
            [beanName] {
                clientInterface = [package and name of wsdl2java -client generated port interface class]
                serviceEndpointAddress = [url for the service]
                username = [username] //optional - used when secured is true - currently wss4j interceptor
                password = [password] //optional - used when secured is true - currently wss4j interceptor
                securityInterceptor = [text name of custom bean to use] //optional - defaults to wss4j interceptor
                inInterceptors = [list of cxf in interceptors to add to the request] //optional - defaults to []
                outInterceptors = [list of cxf out interceptors to add to the request] //optional - defaults to []
                outFaultInterceptors = [list of cxf out fault interceptors to add to the request] //optional - defaults to []
                enableDefaultLoggingInterceptors = [turn on or off default in/out logging] //optional - defaults to true
                secured = [true or false] //optional - defaults to false
                connectionTimeout = [Number of milliseconds to wait for connection] //optional - Defaults to 60000 (use 0 to wait infinitely)
                receiveTimeout = [Number of milliseconds to wait to receive a response] //optional - Defaults to 30000 (use 0 to wait infinitely)
                allowChunking = [true or false] //optional - defaults to false
                httpClientPolicy = [text name of custom bean to use] //optional - defaults to null
                proxyFactoryBindingId = [binding id uri if required] //optional - defaults to null
                wsdlServiceName = [set to enable mime type mapping] //optional - defaults to null
                wsdlEndpointName = [may be needed for correct wsdl initialization] //optional - defaults to null

                //wsdl config
                wsdl = [location of the wsdl either locally relative to project home dir or a url] //optional - only used by wsdl2java script
                wsdlArgs = [custom list of args to pass in seperated by space such as ["-autoNameResolution", "-validate"]] //optional - only used by wsdl2java script
                namespace = [package name to use for generated classes] //optional - uses packages from wsdl if not provided
                client = [true or false] //optional - used to tell wsdl2java to output sample clients, usually not needed - defaults to false
                bindingFile = [Specifies JAXWS or JAXB binding file or XMLBeans context file] //optional
                outputDir = [location to output generated files] //optional - defaults to src/java
            }
        }
    }

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
<tr><td>outFaultInterceptors</td><td>Provide a bean name or list of bean names in "name", "name, name" or ["name","name"] format to wire in as an out fault interceptor for apache cxf.  If you set it is expected that you will configure the beans in the resources.groovy file.  See below for examples (default: [])</td><td>No</td></tr>
<tr><td>enableDefaultLoggingInterceptors</td><td>When set to true, default in and out logging interceptors will be added to the service.  If you require custom logging interceptors and wish to turn off the default loggers for any reason (security, custom, etc), set this property to false and provide your own in and out logging interceptors via the inInterceptors or outInterceptors properties.  You may also simply wish to disable logging of cxf (soap messages, etc) by setting this to false without providing your own interceptors.  (default: true)</td><td>No</td></tr>
<tr><td>connectionTimeout</td><td>Specifies the amount of time, in milliseconds, that the client will attempt to establish a connection before it times out. The default is 30000 (30 seconds). 0 specifies that the client will continue to attempt to open a connection indefinitely. (default: 30000)</td><td>No</td></tr>
<tr><td>receiveTimeout</td><td>Specifies the amount of time, in milliseconds, that the client will wait for a response before it times out. The default is 60000. 0 specifies that the client will wait indefinitely. (default: 60000)</td><td>No</td></tr>
<tr><td>secured</td><td>If true will set the cxf client params to use username and password values using WSS4J. (default: false)</td><td>No</td></tr>
<tr><td>allowChunking</td><td>If true will set the HTTPClientPolicy allowChunking for the clients proxy to true. (default: false)</td><td>No</td></tr>
<tr><td>httpClientPolicy</td><td>Instead of using the seperate timeout, chunking, etc values you can create your own HTTPClientPolicy bean in resources.groovy and pass the name of the bean here. <B>This will override the connectionTimeout, receiveTimeout and allowChunking values.</b> (default: null)</td><td>No</td></tr>
<tr><td>proxyFactoryBindingId</td><td>The URI, or ID, of the message binding for the endpoint to use. For SOAP the binding URI(ID) is specified by the JAX-WS specification. For other message bindings the URI is the namespace of the WSDL extensions used to specify the binding.  If you would like to change the binding (to use soap12 for example) set this value to "http://schemas.xmlsoap.org/wsdl/soap12/". (default: "")</td><td>No</td></tr>
<tr><td>wsdl</td><td>Location of the wsdl either locally or a url (must be available at runtime).  Will be passed into JaxWsProxyFactoryBean.  WSDL will be loaded to handle things that cannot be captured in Java classes via wsdl2java (like MIME attachments). Requires defining _wsdlServiceName_. (default: null)</td><td>No</td></tr>
<tr><td>wsdlServiceName</td><td>The QName of the service you will be accessing.  Will be passed into JaxWsProxyFactoryBean.  Only needed when using WSDL at run-time to handle things that cannot be captured in Java classes via wsdl2java. (example: '{http://my.xml.namespace/}TheNameOfMyWSDLServicePorts') (default: null)</td><td>No</td></tr>
<tr><td>wsdlEndpointName</td><td>The QName of the endpoint/port in the WSDL you will be accessing.  Will be passed into JaxWsProxyFactoryBean.  May be needed when using WSDL at run-time to handle things that cannot be captured in Java classes via wsdl2java. (example: '{http://my.xml.namespace/}TheNameOfMyWSDLServicePort') (default: null)</td><td>No</td></tr>
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

This is an example of a config file

```groovy
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
```

You them refer to your services from a controller/service/taglib like the following:

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

This is rather complex exercise, but one that you can do as of version 1.2 of the plugin.

As a convenience to the user I created an interface to inherit from that allows you to customize the specifics of the interceptor without having to inherit all the contract methods for the cxf interceptors.  You simply have to inherit from CxfClientInterceptor in the com.grails.cxf.client.security package.  Here is the custom interceptor I created for the demo project.

```groovy
package com.cxf.demo.security

import com.grails.cxf.client.CxfClientInterceptor
import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.UnsupportedCallbackException
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.ws.security.WSPasswordCallback
import org.apache.ws.security.handler.WSHandlerConstants

class CustomSecurityInterceptor implements CxfClientInterceptor {

    def pass
    def user

    WSS4JOutInterceptor create() {
        Map<String, Object> outProps = [:]
        outProps.put(WSHandlerConstants.ACTION, org.apache.ws.security.handler.WSHandlerConstants.USERNAME_TOKEN)
        outProps.put(WSHandlerConstants.USER, user)
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, org.apache.ws.security.WSConstants.PW_TEXT)
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0]
                pc.password = pass
            }
        })

        new WSS4JOutInterceptor(outProps)
    }
}
```

You have to make sure your create method returns an object that already inherits from the appropriate classes such as an WSS4JOutInterceptor as I used here.  It is technically possible for your interceptor to extend something like SoapHeaderInterceptor, you will just be responsible for overriding all the appropriate methods yourself.  You can see the <a href="http://www.technipelago.se/content/technipelago/blog/basic-authentication-grails-cxf">following example</a> on how to define a basic auth interceptor on the server side.
More specifically refer to <a href="http://chrisdail.com/download/BasicAuthAuthorizationInterceptor.java">this file</a> for sample code on create your own interceptor or to the <a href="https://github.com/ctoestreich/cxf-client-demo/blob/master/grails-app/conf/BootStrap.groovy">demo project file</a> that injects a server side interceptor. Perhaps the <B>best documentation</b> on writing a complex interceptor can be found at the <a href="http://cxf.apache.org/docs/interceptors.html">Apache CXF</a> site.

In the case of the above CustomSecurityInterceptor, you would then place the following in your projects resources.groovy.

```groovy
beans = {
    myCustomInterceptor(com.cxf.demo.security.CustomSecurityInterceptor){
        user = "wsuser"
        pass = "secret"
    }
}
```

The last step to hooking up the custom interceptor is to define the securityInterceptor for the client config block.  The myCustomInterceptor bean can be hooked up by adding the line in the config below.

```groovy
cxf {
    client {
        customSecureServiceClient {
            wsdl = "docs/SecureService.wsdl" //only used for wsdl2java script target
            namespace = "cxf.client.demo.secure"
            clientInterface = cxf.client.demo.secure.SecureServicePortType
            //secured = true //implied when you define a value for securityInterceptor
            securityInterceptor = 'myCustomInterceptor'
            serviceEndpointAddress = "${service.secure.url}"
        }
    }
}
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

```groovy
simpleServiceInterceptorClient {
    wsdl = "docs/SimpleService.wsdl" //only used for wsdl2java script target
    clientInterface = cxf.client.demo.simple.SimpleServicePortType
    serviceEndpointAddress = "${service.simple.url}"
    inInterceptors = ['customLoggingInInterceptor', 'verboseLoggingInInterceptor'] //can use comma separated list or groovy list
    enableDefaultLoggingInterceptors = false
    namespace = "cxf.client.demo.simple"
}
```

Here is the code for my customLoggingInInterceptor (verboseLoggingInInterceptor is almost identical)

```groovy
package com.cxf.demo.logging

import org.apache.cxf.common.injection.NoJSR250Annotations
import org.apache.cxf.interceptor.AbstractLoggingInterceptor
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.message.Message
import org.apache.cxf.phase.Phase

@NoJSR250Annotations
public class CustomLoggingInInterceptor extends AbstractLoggingInterceptor {

    def name

    public CustomLoggingInInterceptor() {
        super(Phase.RECEIVE);
        log "Creating the custom interceptor bean"
    }

    public void handleMessage(Message message) throws Fault {
        log "$name :: I AM IN CUSTOM IN LOGGER!!!!!!!"
    }
}
```

_**NOTE:** In your constructor you will need to be mindful what Phase you set your interceptor for.  Please see the docs at <http://cxf.apache.org/docs/interceptors.html>_

You will need to set the logging level in the log4j config section to enable the logging

```groovy
info 'com.grails.cxf.client'
info 'org.apache.cxf.interceptor'
info 'blah.blah.blah' //whatever package your custom interceptors are in
//debug 'org.apache.cxf.interceptor' //choose appropriate level
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Out"></a>
CUSTOM OUT INTERCEPTORS
---------------
You can wire in your own custom out interceptors by adding the property outInterceptors to the configured client.  In this example I have chosen to wire in my own out logging interceptors.

```groovy
simpleServiceInterceptorClient {
    wsdl = "docs/SimpleService.wsdl" //only used for wsdl2java script target
    clientInterface = cxf.client.demo.simple.SimpleServicePortType
    serviceEndpointAddress = "${service.simple.url}"
    outInterceptors = 'customLoggingOutInterceptor' //can use comma separated list or groovy list
    namespace = "cxf.client.demo.simple"
}
```

Here is the code for my customLoggingOutInterceptor

```groovy
package com.cxf.demo.logging

import org.apache.cxf.common.injection.NoJSR250Annotations
import org.apache.cxf.interceptor.AbstractLoggingInterceptor
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.message.Message
import org.apache.cxf.phase.Phase

@NoJSR250Annotations
public class CustomLoggingOutInterceptor extends AbstractLoggingInterceptor {

    def name

    public CustomLoggingOutInterceptor() {
        super(Phase.WRITE);
        log "Creating the custom interceptor bean"
    }

    public void handleMessage(Message message) throws Fault {
        log "$name :: I AM IN CUSTOM OUT LOGGER!!!!!!!"
    }
}
```

**Note:** Since the out interceptor is in PRE_STREAM phase (but PRE_STREAM phase is removed in MESSAGE mode), you have to configure out interceptors to be run at write phase.

**Note:** In your constructor you will need to be mindful what Phase you set your interceptor for.  Please see the docs at <http://cxf.apache.org/docs/interceptors.html>

You will need to set the logging level in the log4j config section to enable the logging

```groovy
info 'com.grails.cxf.client'
info 'org.apache.cxf.interceptor'
info 'blah.blah.blah' //whatever package your custom interceptors are in
//debug 'org.apache.cxf.interceptor' //choose appropriate level
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Fault"></a>
CUSTOM OUT FAULT INTERCEPTORS
---------------

You can wire in your own custom out fault interceptors by adding the property outFaultInterceptors to the configured client.  Example coming soon, but should be similar to the earlier two examples.

You will need to set the logging level in the log4j config section to enable the logging

```groovy
info 'com.grails.cxf.client'
info 'org.apache.cxf.interceptor'
info 'blah.blah.blah' //whatever package your custom interceptors are in
//debug 'org.apache.cxf.interceptor' //choose appropriate level
```

<p align="right"><a href="#Top">Top</a></p>
<a name="Custom"></a>
CUSTOM HTTP CLIENT POLICY
---------------

If you simply need to set the connectionTimeout, receiveTimeout, or allowChunking you may use the three provided params to accomplish this.  If you require more fine grained control of the HTTPClientPolicy you can create a custom bean in the resources.groovy and tell your cxf client to use it via the code below.

_Note: A configured httpClientPolicy will take precedence over the connectionTimeout, receiveTimeout and allowChunking. Setting all four params in the config will cause the httpClientPolicy to be used and the others ignored._

resources.groovy

```groovy
beans = {
    customHttpClientPolicy(HTTPClientPolicy){
        connectionTimeout = 30000
        receiveTimeout = 60000
        allowChunking = false
        autoRedirect = false
    }
}
```

Config.groovy

```groovy
cxf {
    client {
        simpleServiceClient {
            clientInterface = cxf.client.demo.simple.SimpleServicePortType
            serviceEndpointAddress = "${service.simple.url}"
            httpClientPolicy = 'customHttpClientPolicy'
        }
}
```

Note: If you incorrectly refer to your new beans name (spelling, etc) you will get an exception such as `...Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'blahblah' is defined` error.

<p align="right"><a href="#Exceptions">Top</a></p>
<a name="Beans"></a>
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

In additiona, any general SOAPFaultException thrown can be caught as well.

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
<a name="Beans"></a>
USING CLIENT BEANS ANYWHERE
---------------
If you require useage of the web service clients you can access them anywhere by accessing them by name.  The name of the bean will match the name of the configured client in your Config.groovy.

Here is one example that could be called from a class in src/groovy

```groovy
cxf {
    client {
        simpleServiceClient {
            ...
        }
    }
}
```

```groovy
SimpleServicePortType simpleServiceClient = ApplicationHolder.application.mainContext.getBean("simpleServiceClient")
```

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
<a name="Demo"></a>
DEMO PROJECT
---------------

A demo project that includes both a sample service and usage of the cxf-client plugin can be found at <https://www.github.com/ctoestreich/cxf-client-demo>

I have also included the full code on how to inject a custom security interceptor in the demo project.

<p align="right"><a href="#Top">Top</a></p>
<a name="Issues"></a>
ISSUES
---------------

To submit an issue please use <https://github.com/ctoestreich/cxf-client/issues>.

Currently there is an issue with pointing to a secure endpoint and running the wsdl2java script.  If you get an error message like

    PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

You may need to put a cert into your [jdkhome]\jre\lib\security directory.  I will be working on getting this working working by adding the cert to the service end point configuration in an upcoming release.

Another solution is to get the wsdl from the web and copy into a local file.wsdl and change the config to point to a local file instead of the https endpoint for generation.

<p align="right"><a href="#Top">Top</a></p>
<a name="Change"></a>
CHANGE LOG
---------------
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
    * Ability to specify custom HTTPClientPolicy bean to use for the client (see [demo project][https://github.com/ctoestreich/cxf-client-demo/blob/master/grails-app/conf/spring/resources.groovy] for more details)

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