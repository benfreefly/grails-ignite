package grails.plugins.ignite

class IgniteGrailsPlugin {
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain"
    ]

    def title = "Grails Ignite Plugin" // Headline display name of the plugin
    def author = "Dan Stieglitz"
    def authorEmail = "dstieglitz@stainlesscode.com"
    def description = '''\
A plugin for the Apache Ignite data grid framework.
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/dstieglitz/grails-ignite"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [[name: "Dan Stieglitz", email: "dstieglitz@stainlesscode.com"]]

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "GITHUB", url: "https://github.com/dstieglitz/grails-ignite/issues"]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/dstieglitz/grails-ignite"]

    //def dependsOn = ['hibernate4': '* > 4.3.8.1']

//    def loadAfter = ['logging', 'shiro', 'springSecurityCore', 'cors']
//
//    def loadBefore = ['hibernate', 'hibernate4']

//    def LOG = LoggerFactory.getLogger('grails.plugin.ignite.IgniteGrailsPlugin')

//    def getWebXmlFilterOrder() {
//        [IgniteWebSessionsFilter: FilterManager.CHAR_ENCODING_POSITION + 1]
//    }

    def doWithWebDescriptor = { xml ->
        def configuredGridName = application.config.getProperty("ignite.gridName", String, IgniteStartupHelper.DEFAULT_GRID_NAME)

        // FIXME no log.(anything) output from here
        //println "Web session clustering enabled in config? ${webSessionClusteringEnabled} for gridName=${configuredGridName}"

//        //
//        // FIXME this will be checked at BUILD time and therefore must be "true" if the filters are to be installed
//        //
//        if (webSessionClusteringEnabled) {
//            def listenerNode = xml.'listener'
//            listenerNode[listenerNode.size() - 1] + {
//                listener {
//                    'listener-class'('org.apache.ignite.startup.servlet.ServletContextListenerStartup')
//                }
//            }

        def contextParam = xml.'context-param'
        contextParam[contextParam.size() - 1] + {
            'filter' {
                'filter-name'('IgniteWebSessionsFilter')
                'filter-class'('grails.plugins.ignite.WebSessionFilter')
                'init-param' {
                    'param-name'('IgniteWebSessionsGridName')
                    'param-value'(configuredGridName)
                }
            }
        }

//        def filterMappingNode = xml.'filter-mapping'
//        filterMappingNode[filterMappingNode.size() - 1] + {
//            'filter-mapping' {
//                'filter-name'('IgniteWebSessionsFilter')
//                'url-pattern'('/*')
//            }
//        }

        contextParam[contextParam.size() - 1] + {
            'context-param' {
                'param-name'('IgniteWebSessionsCacheName')
                'param-value'(IgniteStartupHelper.IGNITE_WEB_SESSION_CACHE_NAME)
            }
        }
//        }

        int i = 0
        int shiroFilterIndex = -1
        xml.'filter-mapping'.each {
            if (it.'filter-name'.text().equalsIgnoreCase("shiroFilter")) {
                shiroFilterIndex = i
            }
            i++
        }

        def filter
        if (shiroFilterIndex < 0) {
            // shiro not installed
            filter = xml.'filter-mapping'.find { it.'filter-name'.text() == "charEncodingFilter" }
        } else {
            filter = xml.'filter-mapping'[shiroFilterIndex - 1]
        }

        filter + {
            'filter-mapping' {
                'filter-name'('IgniteWebSessionsFilter')
                'url-pattern'("/*")
                dispatcher('REQUEST')
                dispatcher('ERROR')
            }
        }
    }

    def doWithSpring = {
        if (application.config.getProperty("ignite.enabled", Boolean, false)) {
            grid(IgniteContextBridge)
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->

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

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
