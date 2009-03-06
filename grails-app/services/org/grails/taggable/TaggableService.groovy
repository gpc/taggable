package org.grails.taggable

import grails.util.*

class TaggableService {
    
    def getTagCounts(type) {
        def tagCounts = [:]
        TagLink.withCriteria {
            eq('type', type)
            projections {
                groupProperty('tag')
                count('tagRef')
            }
        }.each {
            def (tagName, count) = it
            tagCounts[tagName] = tagCounts[tagName] ? (tagCounts[tagName] + count) : count
        }
        tagCounts
    }
    
}