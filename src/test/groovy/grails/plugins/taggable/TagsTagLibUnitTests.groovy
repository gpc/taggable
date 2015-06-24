

package grails.plugins.taggable

import grails.test.mixin.TestFor
import static org.junit.Assert.*

import org.grails.taglib.GrailsTagException
import org.junit.Before


@TestFor(TagsTagLib)
class TagsTagLibUnitTests {
    @Before
    void before() {
        webRequest.controllerName = 'default'
        tagLib.grailsApplication.config.grails.taggable.css.classes = null
    }

    void testTagCloudWithNoActionAttribute() {
        def msg = shouldFail(GrailsTagException) {
            tagLib.tagCloud tags: [:]
        }

        assertEquals msg, "Required attribute [action] is missing"
    }

    void testTagCloudWithNoTagsAttribute() {
        def msg = shouldFail(GrailsTagException) { 
            tagLib.tagCloud action: "browseByTag" 
        }

        assertEquals msg, "Required attribute [tags] must be a map of tag names to tag counts"
    }

    void testTagCloudWithNoControllerAttribute() {
        def result = tagLib.tagCloud(tags: [hello: 1, world: 10, apple: 3, orange: 7], action: "byTag")

        assertEquals result.toString(), "<ol class=\"tagCloud\"><li class=\"smallest\">" +
                "<a href=\"/default/byTag/hello\">hello</a></li>" +
                "<li class=\"largest\"><a href=\"/default/byTag/world\">world</a></li>" +
                "<li class=\"small\"><a href=\"/default/byTag/apple\">apple</a></li>" +
                "<li class=\"large\"><a href=\"/default/byTag/orange\">orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithLongCounts() {
        def result = tagLib.tagCloud(tags: [hello: 1L, world: 10L, apple: 3L, orange: 7L], action: "byTag")

        assertEquals result.toString(), "<ol class=\"tagCloud\"><li class=\"smallest\">" +
                "<a href=\"/default/byTag/hello\">hello</a></li>" +
                "<li class=\"largest\"><a href=\"/default/byTag/world\">world</a></li>" +
                "<li class=\"small\"><a href=\"/default/byTag/apple\">apple</a></li>" +
                "<li class=\"large\"><a href=\"/default/byTag/orange\">orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithControllerAttribute() {
        def result = tagLib.tagCloud(tags: [hello: 1, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag")

        assertEquals result.toString(), "<ol class=\"tagCloud\"><li class=\"smallest\">" +
                "<a href=\"/plugin/byTag/hello\">hello</a></li>" +
                "<li class=\"largest\"><a href=\"/plugin/byTag/world\">world</a></li>" +
                "<li class=\"small\"><a href=\"/plugin/byTag/apple\">apple</a></li>" +
                "<li class=\"large\"><a href=\"/plugin/byTag/orange\">orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithCustomIdProperty() {
        def result = tagLib.tagCloud(tags: [hello: 1, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag", idProperty: "tagName")

        assertEquals result.toString(), "<ol class=\"tagCloud\"><li class=\"smallest\">" +
                "<a href=\"/plugin/byTag?tagName=hello\">hello</a></li>" +
                "<li class=\"largest\"><a href=\"/plugin/byTag?tagName=world\">world</a></li>" +
                "<li class=\"small\"><a href=\"/plugin/byTag?tagName=apple\">apple</a></li>" +
                "<li class=\"large\"><a href=\"/plugin/byTag?tagName=orange\">orange</a></li>" +
                "</ol>"
    }

    void testTagCloudWithCustomCssClasses() {
        tagLib.grailsApplication.config.grails.taggable.css.classes = [
            "one",
            "two",
            "three",
            "four"
        ]
        def result = tagLib.tagCloud(tags: [hello: 2, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag")
        assertEquals result.toString(), "<ol class=\"tagCloud\"><li class=\"one\">" +
                "<a href=\"/plugin/byTag/hello\">hello</a></li>" +
                "<li class=\"four\"><a href=\"/plugin/byTag/world\">world</a></li>" +
                "<li class=\"two\"><a href=\"/plugin/byTag/apple\">apple</a></li>" +
                "<li class=\"three\"><a href=\"/plugin/byTag/orange\">orange</a></li>" +
                "</ol>"
    }
}
