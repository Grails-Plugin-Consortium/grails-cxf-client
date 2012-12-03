log4j = {
    appenders {
        console name: 'stdout'
    }

    root {
        info('stdout')
    }

    error 'org.codehaus.groovy.grails',
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'

    info 'com.grails.cxf.client',
         'com.grails.cxf.client.security',
         'com.grails.cxf.client.exception'
}
