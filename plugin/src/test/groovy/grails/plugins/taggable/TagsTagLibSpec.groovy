package grails.plugins.taggable

import grails.testing.web.taglib.TagLibUnitTest
import org.grails.taglib.GrailsTagException
import spock.lang.Specification

class TagsTagLibSpec extends Specification implements TagLibUnitTest<TagsTagLib> {

	void setup() {
		webRequest.controllerName = 'default'
		config.setAt("grails.taggable.css.classes", null)
	}

	void "test that tagCloud fails when no action attribute specified"() {
		when:
			applyTemplate('<tags:tagCloud tags="${tags}"/>', [tags: [:]])
		then:
			Exception e = thrown(GrailsTagException)
			e.getMessage().endsWith( "Required attribute [action] is missing")
	}

	void "test that tagCloud fails when no tags attribute specified"() {
		when:
			applyTemplate('<tags:tagCloud action="${action}"/>', [action: "browseByTag"])
		then:
			Exception e = thrown(GrailsTagException)
			e.getMessage().endsWith("Required attribute [tags] must be a map of tag names to tag counts")
	}

	void "test that tagCloud uses current controller when no attr passed"() {
		when:
			def result = applyTemplate('<tags:tagCloud tags="${tags}" action="${action}"/>', [tags: [hello: 1, world: 10, apple: 3, orange: 7], action: "byTag"])
		then:
			result == "<ol class=\"tagCloud\"><li class=\"smallest\">" +
				"<a href=\"/default/byTag/hello\">hello</a></li>" +
				"<li class=\"largest\"><a href=\"/default/byTag/world\">world</a></li>" +
				"<li class=\"small\"><a href=\"/default/byTag/apple\">apple</a></li>" +
				"<li class=\"large\"><a href=\"/default/byTag/orange\">orange</a></li>" +
				"</ol>"
	}

	void "test tagCloud with long counts"() {
		when:
			def result = applyTemplate('<tags:tagCloud tags="${tags}" action="${action}"/>', [tags: [hello: 1L, world: 10L, apple: 3L, orange: 7L], action: "byTag"])
		then:
			result == "<ol class=\"tagCloud\"><li class=\"smallest\">" +
				"<a href=\"/default/byTag/hello\">hello</a></li>" +
				"<li class=\"largest\"><a href=\"/default/byTag/world\">world</a></li>" +
				"<li class=\"small\"><a href=\"/default/byTag/apple\">apple</a></li>" +
				"<li class=\"large\"><a href=\"/default/byTag/orange\">orange</a></li>" +
				"</ol>"
	}

	void "test tagCloud with controller attribute"() {
		when:
			def result = applyTemplate('<tags:tagCloud tags="${tags}" controller="${controller}" action="${action}"/>', [tags: [hello: 1, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag"])
		then:
			result == "<ol class=\"tagCloud\"><li class=\"smallest\">" +
				"<a href=\"/plugin/byTag/hello\">hello</a></li>" +
				"<li class=\"largest\"><a href=\"/plugin/byTag/world\">world</a></li>" +
				"<li class=\"small\"><a href=\"/plugin/byTag/apple\">apple</a></li>" +
				"<li class=\"large\"><a href=\"/plugin/byTag/orange\">orange</a></li>" +
				"</ol>"
	}

	void "test tagCloud with custom id property"() {
		when:
			def result = applyTemplate('<tags:tagCloud tags="${tags}" controller="${controller}" action="${action}" idProperty="${idProperty}"/>', [tags: [hello: 1, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag", idProperty: "tagName"])
		then:
			result == "<ol class=\"tagCloud\"><li class=\"smallest\">" +
				"<a href=\"/plugin/byTag?tagName=hello\">hello</a></li>" +
				"<li class=\"largest\"><a href=\"/plugin/byTag?tagName=world\">world</a></li>" +
				"<li class=\"small\"><a href=\"/plugin/byTag?tagName=apple\">apple</a></li>" +
				"<li class=\"large\"><a href=\"/plugin/byTag?tagName=orange\">orange</a></li>" +
				"</ol>"
	}

	void "test tagCloud with custom CSS classes"() {
		given:
			tagLib.grailsApplication.config.grails.taggable.css.classes = [
				"one",
				"two",
				"three",
				"four"
			]
		when:
			def result = applyTemplate('<tags:tagCloud tags="${tags}" controller="${controller}" action="${action}"/>', [tags: [hello: 2, world: 10, apple: 3, orange: 7], controller: "plugin", action: "byTag"])
		then:
			result == "<ol class=\"tagCloud\"><li class=\"one\">" +
				"<a href=\"/plugin/byTag/hello\">hello</a></li>" +
				"<li class=\"four\"><a href=\"/plugin/byTag/world\">world</a></li>" +
				"<li class=\"two\"><a href=\"/plugin/byTag/apple\">apple</a></li>" +
				"<li class=\"three\"><a href=\"/plugin/byTag/orange\">orange</a></li>" +
				"</ol>"
	}
}
