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
import org.grails.taggable.*

/**
 * A plugin that adds a generic mechanism for tagging data 

 * @author Graeme Rocher
 */
class TaggableGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [hibernate:"1.1 > *"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
			"grails-app/domain/org/grails/taggable/TestDomain.groovy"
    ]

    def author = "Graeme Rocher"
    def authorEmail = "graeme.rocher@springsource.com"
    def title = "Taggable Plugin"
    def description = '''\\
A plugin that adds a generic mechanism for tagging data
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/Taggable+Plugin"

    def doWithSpring = {
		for(domainClass in application.domainClasses) {
			if(Taggable.class.isAssignableFrom(domainClass.clazz)) {
				domainClass.clazz.metaClass {
					addTag { String name ->
						if(delegate.id == null) throw new TagException("You need to save the domain instance before tagging it")
						def tag = Tag.findByName(name, [cache:true]) ?: new Tag(name:name).save()
						if(!tag) throw new TagException("Value [$name] is not a valid tag")
						
						def criteria = TagLink.createCriteria()
						def instance = delegate
						def link = criteria.get {
							eq 'tag', tag
							eq 'tagRef', instance.id
							eq 'tagClass', instance.class.name
							cache true
						}
						
						if(!link) {
							link = new TagLink(tag:tag, tagRef:delegate.id, tagClass:delegate.class.name).save()
						}
						return delegate // for method chaining
					}
					
					addTags { names ->
					    names.each { delegate.addTag it }
					}
					
					getTags {->
						delegate.id ? getTagLinks(delegate).tag.name : []
					}
					
					removeTag { String name ->
						if(delegate.id == null) throw new TagException("You need to save the domain instance before tagging it")
						
						def criteria = TagLink.createCriteria()
						def instance = delegate
						def link = criteria.get {
							tag {
								eq 'name', name
							}
							eq 'tagRef', instance.id
							eq 'tagClass', instance.class.name
							cache true
						}						
						link?.delete()
						return delegate
					}
										
					setTags { List tags ->
						getTagLinks(delegate)*.delete()
						for(tag in tags) {
							addTag(tag?.toString())
						}
					}
					
					'static' {
						
						countByTag { String tag ->
							def identifiers = TaggableGrailsPlugin.getTagReferences(tag, delegate.name)
							if(identifiers) {
								def criteria = createCriteria()
								criteria.get {
									projections {
										rowCount()
									}
									inList 'id', identifiers
									cache true
								}
							}
							else {
								return 0								
							}
						}
						
						findAllByTag { String name ->
							def identifiers = TaggableGrailsPlugin.getTagReferences(name, delegate.name)
							if(identifiers) {
								withCriteria {
									inList 'id', identifiers
									cache true
								}
							}
							else {
								return Collections.EMPTY_LIST								
							}
						}
						

					}
				}
			}
		}
    }

	private getTagLinks(obj) {
		TagLink.findAllByTagRefAndTagClass(obj.id, obj.class.name, [cache:true])		
	}
	
	static getTagReferences(String tagName, String className) {
		if(tagName) {
			TagLink.withCriteria {
				projections {
					property 'tagRef'
				}
				tag {
					eq 'name', tagName							
				}				
				eq 'tagClass', className
				cache true
			}
			
		}	
		else {
			return Collections.EMPTY_LIST
		}	
	}

}
