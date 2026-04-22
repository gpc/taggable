package grails.plugins.taggable

import grails.core.GrailsApplication
import grails.util.GrailsNameUtils
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class TaggableService {

    GrailsApplication grailsApplication

    @Autowired
    @Qualifier('grailsDomainClassMappingContext')
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
     * Update the graph of known subclasses for each Taggable domain type.
     */
    def refreshDomainClasses() {
        mappingContext.persistentEntities.each { PersistentEntity persistentEntity ->
            Class<?> clazz = persistentEntity.javaClass
            if (Taggable.isAssignableFrom(clazz)) {
                def family = [GrailsNameUtils.getPropertyName(clazz)]
                family.addAll(
                        mappingContext.getChildEntities(persistentEntity).collect {
                            GrailsNameUtils.getPropertyName(it.javaClass)
                        }
                )
                domainClassFamilies[clazz.name] = family
            }
        }
    }
}
