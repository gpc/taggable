package grails.plugins.taggable

/* Copyright 2006-2007 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import grails.plugins.taggable.*
import grails.util.*
import grails.plugins.*

/**
 * A plugin that adds a generic mechanism for tagging data 

 * @author Graeme Rocher
 */
class TaggableGrailsPlugin extends Plugin {
    def version = "1.1.0"
    def grailsVersion = "3.0.3 > *"
    def license = 'APACHE'
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails/plugins/taggable/Test*"
    ]

    def observe = ['hibernate']
    def developers = [
        [ name: "Graeme Rocher", email: "graeme.rocher@springsource.com" ]
    ]
    def title = "Taggable Plugin"
    def description = '''\
A plugin that adds a generic mechanism for tagging data.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/taggable"
    def issueManagement = [system: "JIRA", url: "http://jira.grails.org/browse/GPTAGGABLE"]
    def scm = [url: "https://github.com/gpc/grails-taggable"]
	def organization = [ name: "Grails Plugin Collective", url: "http://github.com/gpc" ]

    void doWithApplicationContext() {
        def tagService = applicationContext.taggableService
        tagService.refreshDomainClasses()
    }

    void onChange(Map<String, Object> event) {
        applicationContext.taggableService.refreshDomainClasses()
    }

}
