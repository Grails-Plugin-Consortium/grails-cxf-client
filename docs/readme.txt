You must be somewhat familiar with how to run wsdl2java.  Run something like the following to generate jaxb objects and a service proxy adapter
Replacing the paths with the something that is applicable for you.  Run this by either having wsdl2java in your path or from the root of the
project

wsdl2java -compile -client -d [outputpath] ComplexService.wsdl

here is my string

    C:\projects\cxf-client-demo\docs>c:\apps\apache-cxf-2.4.2\bin\wsdl2java -compile -client -d . -p cxf.client.demo.complex ComplexService.wsdl

I then jar up the files to a complex-service-cxf.jar

    C:\projects\cxf-client-demo\docs>jar -cvf complex-service-cxf.jar cxf

Put the jar into your project's lib dir (and generate any more jars you need).  In my case I need to create another for the Simple Service.

    C:\projects\cxf-client-demo\docs>c:\apps\apache-cxf-2.4.2\bin\wsdl2java -compile -client -d . -p cxf.client.demo.simple SimpleService.wsdl
    C:\projects\cxf-client-demo\docs>jar -cvf simple-service-cxf.jar cxf

These could be put in the same jar since the namespace I am using is different cxf.client.demo.complex and cxf.client.demo.simple.

NOTES:
   I am using the full path for objects in controller since I have source for original objects here
   which are named the same as the jaxb objects except the namespace.