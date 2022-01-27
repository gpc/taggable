package grails.plugins.taggable

import grails.core.GrailsApplication
import grails.core.GrailsDomainClass
import grails.util.GrailsNameUtils
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class TaggableService {

    GrailsApplication grailsApplication

    @Autowired
    @Qualifier("grailsDomainClassMappingContext")
    MappingContext mappingContext

    def domainClassFamilies = [:]

    def getTagCounts(type) {
        def tagCounts = [:]
        TagLink.withCriteria {
            eq('type', type)
            projections {
                groupProperty('tag')
                count('tagRef')
            }
        }.each {
            def tagName = it[0].name
            def count = it[1]
            tagCounts[tagName] = tagCounts[tagName] ? (tagCounts[tagName] + count) : count
        }
        return tagCounts
    }
    
    /**
     * Update the graph of known subclasses
     *
     * Example:
     * [
     *  WcmContent: [
     *      WcmBlog,
     *      WcmHTMLContent,
     *      WcmComment
     *   ]
     *  WcmBlog: [],
     *  WcmHTMLContent: [WcmRichContent],
     *  WcmRichContent: [],
     *  WcmStatus: []
     * ]
     */
    def refreshDomainClasses() {
        grailsApplication.domainClasses.each {GrailsDomainClass artefact ->
            PersistentEntity persistentEntity = mappingContext.getPersistentEntity(artefact.clazz.name)
            if (Taggable.class.isAssignableFrom(artefact.clazz)) {
                domainClassFamilies[artefact.clazz.name] = [GrailsNameUtils.getPropertyName(artefact.clazz)]
                // Add class and all subclasses 
                domainClassFamilies[artefact.clazz.name].addAll(mappingContext.getChildEntities(persistentEntity).collect {GrailsNameUtils.getPropertyName(it.javaClass)})
            }
        }
    }
}
