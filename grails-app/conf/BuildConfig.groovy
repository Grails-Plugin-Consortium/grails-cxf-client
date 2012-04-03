grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile('org.apache.cxf:cxf-rt-frontend-jaxws:2.5.2') {
            excludes 'spring-web'
        }
        compile('org.apache.cxf:cxf-rt-frontend-jaxrs:2.5.2') {
            excludes 'xmlbeans', 'spring-web', 'spring-core'
        }
        compile('org.apache.ws.security:wss4j:1.6.4')
        compile('org.apache.cxf:cxf-rt-ws-security:2.5.2') {
            excludes 'spring-web'
        }
        // runtime 'mysql:mysql-connector-java:5.1.13'
    }

     plugins {
        test (":spock:0.6-SNAPSHOT") {
            export = false
        }
        test (":code-coverage:1.2.5") {
            export = false
        }
        test (":codenarc:0.17") {
            export = false
        }
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
