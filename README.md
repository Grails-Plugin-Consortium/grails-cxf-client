CXF CLIENT
======

* Introduction
* Wsdl2java Script
* Wsdl2java Manually
* Plugin Configuration
* Custom Security Interceptors
* Demo Project
* Issues
* Future Revisions

INTRODUCTION
---------------

There are a few different plugins for consuming SOAP web services with grails, but none currently deal with the issue of caching port references.  The ws-client plugin works, but its limitations are in how it creates and consumes the wsdl.  It relies on real time creation of proxy classes and services which can be very processor and memory (time) consuming with a large or complex service contract.  We need a way to speed up service invocation so this plugin was created to facilitate that need when consuming SOAP services using cxf.

The Cxf Client plugin will allow you to use existing (or new) apache cxf wsdl2java generated content and cache the port reference to speed up your soap service end point invocations through an easy configuration driven mechanism.

WSDL2JAVA SCRIPT
---------------

This plugin provides a convenient way to run wsdl2java as a grails run target in your project.  You must have apache cxf installed on your machine somewhere for this to work correctly.

I have mine installed in c:\apps\apache-cxf-2.4.2 so I will add the [installDir] config setting to my configuration node to tell the script where to find the apache cxf classes to put on the classpath.

```groovy
cxf {
    installDir = "C:/apps/apache-cxf-2.4.2" //only used for wsdl2java script target
    client {
        ...
    }
}
```

After I have done that I need to point the configured clients to a wsdl (either locally or remotely).  This is done by adding the [wsdl] node to the client config as following:

```groovy
cxf {
    installDir = "C:/apps/apache-cxf-2.4.2" //only used for wsdl2java script target
    client {
        simpleServiceClient {
            //used in wsdl2java
            wsdl = "docs/SimpleService.wsdl" //only used for wsdl2java script target
            namespace = "cxf.client.demo.simple"
            client = false //defaults to false
            binding = "grails-app/conf/bindings.xml"
            outputDir = "src/java"

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
                securityInterceptor = [text name of custom bean to use] //optional - defaults to wss4j interceptor
                wsdl = [location of the wsdl either locally relative to project home dir or a url] //optional - only used by wsdl2java script
                namespace = [package name to use for generated classes] //optional - uses packages from wsdl if not provided
                client = [true or false] //optional - used to tell wsdl2java to output sample clients, usually not needed - defaults to false
                binding = [Specifies JAXWS or JAXB binding files or XMLBeans context files] //optional
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
<tr><td>secured</td><td>If true will set the cxf client params to use username and password values using WSS4J. (default: false)</td><td>No</td></tr>
<tr><td>username</td><td>Username to pass along with request in wss4j interceptor when secured is true. (default: "")</td><td>No</td></tr>
<tr><td>password</td><td>Password to pass along with request in wss4j interceptor when secured is true. (default: "")</td><td>No</td></tr>
<tr><td>securityInterceptor</td><td>Provide a bean name as a string to wire in as an out interceptor for apache cxf.  If you provide a name for an interceptor, it will be implied that secured=true.  If you require the default wss4j interceptor you will not need to set this property, simply set the secured=true and the username and password properties.  If you set this to a value then the username and password fields will be ignored as it is expected that you will configure any required property injection in your resources.groovy file.  See below for examples (default: "")</td><td>No</td></tr>
</table>

Config items used by wsdl2java.

<table>
<tr><td><b>Property</b></td><td><b>Description</b></td><td>Required</b></td></tr>
<tr><td>wsdl</td><td>Location of the wsdl either locally relative to project home dir or a url. (default: "")</td><td>No</td></tr>
<tr><td>namespace</td><td>Specifies package names to use for the generated code. (default: "use wsdl provided schema")</td><td>No</td></tr>
<tr><td>client</td><td>Used to tell wsdl2java to output sample clients, usually not needed. (default: false)</td><td>No</td></tr>
<tr><td>binding</td><td>Password to pass along with request in wss4j interceptor when secured is true. (default: "")</td><td>No</td></tr>
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

NOTE: You should type the beans with the cxf port interface type so as to get intellisense auto-completion on the service methods. By simply using def you will not know what methods are available on the soap service without peaking into the wsdl or generated client port interface manually.

CUSTOM SECURITY INTERCEPTORS
---------------

This is rather complex exercise, but one that you can do as of version 1.2 of the plugin.

As a convenience to the user I created an interface to inherit from that allows you to customize the specifics of the interceptor without having to inherit all the contract methods for the cxf interceptors.  You simply have to inherit from SecurityInterceptor in the com.grails.cxf.client.security package.  Here is the custom interceptor I created for the demo project.

```groovy
package com.cxf.demo.security

import com.grails.cxf.client.CxfClientInterceptor
import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.UnsupportedCallbackException
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.ws.security.WSPasswordCallback
import org.apache.ws.security.handler.WSHandlerConstants

class CustomSecurityInterceptor implements SecurityInterceptor {

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

DEMO PROJECT
---------------

A demo project that includes both a sample service and usage of the cxf-client plugin can be found at

<https://www.github.com/ctoestreich/cxf-client-demo>

I have also included the full code on how to inject a custom security interceptor in the demo project.

ISSUES
---------------

Currently there is an issue with pointing to a secure endpoint and running the wsdl2java script.  If you get an error message like

    PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

You may need to put a cert into your [jdkhome]\jre\lib\security directory.  I will be working on getting this working working by adding the cert to the service end point configuration in an upcoming release.

Another solution is to get the wsdl from the web and copy into a local file.wsdl and change the config to point to a local file instead of the https endpoint for generation.


FUTURE REVISIONS
---------------

* Ability to dynamically reload endpoint url at runtime


LICENSE
---------------

Copyright 2011 Christian Oestreich

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.