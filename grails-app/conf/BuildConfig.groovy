grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
        // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
        //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

        // configure settings for the test-app JVM, uses the daemon by default
        test: false, //[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
        // configure settings for the run-app JVM
        run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
        // configure settings for the run-war JVM
        war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
        // configure settings for the Console UI JVM
        console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {

    String cxfVersion = '2.6.6'

    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve true // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }

    dependencies {

        compile('commons-cli:commons-cli:1.2')

        compile("org.apache.cxf:cxf-tools-wsdlto-core:${cxfVersion}") {
            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
        }

        compile("org.apache.cxf:cxf-tools-wsdlto-frontend-jaxws:${cxfVersion}") {
            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
        }

        compile("org.apache.cxf:cxf-tools-wsdlto-databinding-jaxb:${cxfVersion}") {
            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
        }

        compile("org.apache.cxf:cxf-rt-frontend-jaxws:${cxfVersion}") {
            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
        }

        compile("org.apache.cxf:cxf-rt-frontend-jaxrs:${cxfVersion}") {
            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
        }

        compile('org.apache.ws.security:wss4j:1.6.7'){
            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis',
                     'junit', 'log4j', 'slf4j', 'slf4j-log4j12','slf4j-api', 'slf4j-jdk14'

        }

        compile("org.apache.cxf:cxf-rt-ws-security:${cxfVersion}"){
            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis',
                     'ehcache', 'easymock', 'ehcache-core',
                     'log4j', 'slf4j', 'slf4j-log4j12','slf4j-api', 'slf4j-jdk14'
        }


        /* Some Testing Help **************************************************/
//        test("org.codehaus.geb:geb-spock:${gebVersion}") {
//            export = false
//        }
//
//        test('org.seleniumhq.selenium:selenium-htmlunit-driver:2.25.0') {
//            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
//            export = false
//        }
//
//        test('org.seleniumhq.selenium:selenium-chrome-driver:2.20.0') {
//            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
//            export = false
//        }
//
//        test('org.seleniumhq.selenium:selenium-firefox-driver:2.20.0') {
//            excludes 'xmlbeans', 'spring-web', 'spring-core', 'xml-apis'
//            export = false
//        }
    }

    plugins {
        //remove this before committing.  Only used to release...not test.
        // This still an issue?!?
        // http://grails.1312388.n4.nabble.com/Geb-and-Release-plugin-httpclient-conflicts-td4295238.html
        build(':release:3.0.1', ':rest-client-builder:2.0.1') {
            export = false
        }

//        runtime(":geb:${gebVersion}") {
//            export = false
//        }

        test(":code-coverage:1.2.5") {
            export = false
        }

        test(":codenarc:0.21") {
            export = false
        }

//        runtime(":wslite:0.7.1.0") {
//            export = false
//        }
    }
}

coverage {
    xml = true
    exclusions = ["**/*Tests*"]
}

codenarc {
    processTestUnit = false
    processTestIntegration = false
    propertiesFile = 'codenarc.properties'
    ruleSetFiles = "file:grails-app/conf/codenarc.groovy"
    reports = {
        CxfClientReport('xml') {                    // The report name "MyXmlReport" is user-defined; Report type is 'xml'
            outputFile = 'target/codenarc.xml'      // Set the 'outputFile' property of the (XML) Report
            title = 'CXF Client Plugin'             // Set the 'title' property of the (XML) Report
        }
    }
}
