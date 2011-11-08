package org.grails.taggable

import grails.test.TagLibUnitTestCase
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class TagsTagLibUnitTests extends TagLibUnitTestCase {
    def currentIdProperty = "id"

    void setUp() {
        super.setUp()

        def mockTags = mockFor(tagLib.class)
        mockTags.demand.link(0..100) { Map args, String body ->
            def controller = args.controller ?: "default"
            return "<a href='/${controller}/${args.action}/${args.params[currentIdProperty]}'>${body}</a>"
        }

        tagLib.grailsApplication = new Expando(config: new ConfigObject())
    }

    void testTagCloudWithNoActionAttribute() {
        def msg = shouldFail(GrailsTagException) {
            tagLib.tagCloud tags: [:]
        }

        assert msg == "Required attribute [action] is missing"
    }

    void testTagCloudWithNoTagsAttribute() {
        def msg = shouldFail(GrailsTagException) {
            tagLib.tagCloud action: "browseByTag"
        }

        assert msg == "Required attribute [tags] is missing"
    }

    void testTagCloudWithNoControllerAttribute() {
        tagLib.tagCloud tags: [hello: 1, world: 10, apple: 3, orange: 7], action: "byTag"

        def tagOutput = tagLib.out.toString()
        assert tagOutput == "<ol class=\"tagCloud\"><li class=\"smallest\">" + 
                "<a href='/default/byTag/hello'>hello</a></li>" +
                "<li class=\"largest\"><a href='/default/byTag/world'>world</a></li>" +
                "<li class=\"small\"><a href='/default/byTag/apple'>apple</a></li>" +
                "<li class=\"large\"><a href='/default/byTag/orange'>orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithLongCounts() {
        tagLib.tagCloud tags: [hello: 1L, world: 10L, apple: 3L, orange: 7L], action: "byTag"

        def tagOutput = tagLib.out.toString()
        assert tagOutput == "<ol class=\"tagCloud\"><li class=\"smallest\">" + 
                "<a href='/default/byTag/hello'>hello</a></li>" +
                "<li class=\"largest\"><a href='/default/byTag/world'>world</a></li>" +
                "<li class=\"small\"><a href='/default/byTag/apple'>apple</a></li>" +
                "<li class=\"large\"><a href='/default/byTag/orange'>orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithControllerAttribute() {
        tagLib.tagCloud tags: [hello: 1, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag"

        def tagOutput = tagLib.out.toString()
        assert tagOutput == "<ol class=\"tagCloud\"><li class=\"smallest\">" + 
                "<a href='/plugin/byTag/hello'>hello</a></li>" +
                "<li class=\"largest\"><a href='/plugin/byTag/world'>world</a></li>" +
                "<li class=\"small\"><a href='/plugin/byTag/apple'>apple</a></li>" +
                "<li class=\"large\"><a href='/plugin/byTag/orange'>orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithCustomIdProperty() {
        currentIdProperty = "tagName"
        tagLib.tagCloud tags: [hello: 1, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag", idProperty: "tagName"

        def tagOutput = tagLib.out.toString()
        assert tagOutput == "<ol class=\"tagCloud\"><li class=\"smallest\">" + 
                "<a href='/plugin/byTag/hello'>hello</a></li>" +
                "<li class=\"largest\"><a href='/plugin/byTag/world'>world</a></li>" +
                "<li class=\"small\"><a href='/plugin/byTag/apple'>apple</a></li>" +
                "<li class=\"large\"><a href='/plugin/byTag/orange'>orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithCustomCssClasses() {
        tagLib.grailsApplication.config.grails.taggable.css.classes = [ "one", "two", "three", "four" ]
        currentIdProperty = "tagName"
        tagLib.tagCloud tags: [hello: 2, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag", idProperty: "tagName"

        def tagOutput = tagLib.out.toString()
        assert tagOutput == "<ol class=\"tagCloud\"><li class=\"one\">" + 
                "<a href='/plugin/byTag/hello'>hello</a></li>" +
                "<li class=\"four\"><a href='/plugin/byTag/world'>world</a></li>" +
                "<li class=\"two\"><a href='/plugin/byTag/apple'>apple</a></li>" +
                "<li class=\"three\"><a href='/plugin/byTag/orange'>orange</a></li>" +
                "</ol>"
    }
}
