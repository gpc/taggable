package grails.plugins.taggable

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

@Integration
@Rollback
class TaggableSpec extends Specification {

	def setup() {
		Tag.preserveCaseForTesting = false
	}

	void testAddTagMethodCaseInsensitive() {
		given:
		def td = new TestDomain(name: "foo")
		td.save()

		when:
		td.addTag("Groovy")
				.addTag("grails")
		and:
		def links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

		then:
		assertEquals 2, links.size()
		assertEquals(['groovy', 'grails'], links.tag.name)
	}

	void testAddTagMethodCasePreserving() {
		given:
		Tag.preserveCaseForTesting = true

		def td = new TestDomain(name:"foo")
		td.save()

		when:
		td.addTag("Groovy")
				.addTag("grails")

		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
		then:
		assertEquals 2, links.size()
		assertEquals( ['Groovy', 'grails'], links.tag.name )

		when:
		// adding a second, even if preserving case in DB it should still not add it as already has such a tag
		td.addTag("groovy")
		and:
		links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')

		then:
		assertEquals 3, links.size()
		assertEquals( ['Groovy', 'grails', 'groovy'], links.tag.name )
	}

	void testAddTagsMethod() {
		given:
		def td = new TestDomain(name:"foo")
		td.save()

		when:
		td.addTags(["groovy","grails"])
		and:
		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')

		then:
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )
	}

	void testRemoveTagMethod() {
		given:
		def td = new TestDomain(name: "foo")
		td.save()

		when:
		td.addTag("groovy")
				.addTag("grails")
		and:
		def links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

		then:
		assertEquals 2, links.size()
		assertEquals(['groovy', 'grails'], links.tag.name)

		when:
		td.removeTag("groovy")
		and:
		links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

		then:
		assertEquals 1, links.size()
		assertEquals(['grails'], links.tag.name)
	}

	void testGetTagsMethod() {
		given:
		def td = new TestDomain(name:"foo")
		td.save()

		when:
		td.addTag("groovy")
				.addTag("grails")
		td.save(flush:true)

		and:
		TestDomain.withSession { session -> session.clear() }
		td = TestDomain.findByName("foo")

		then:
		assertEquals( ['groovy', 'grails'], td.tags )
	}

	void testSetTagsMethod() {
		given:
		def td = new TestDomain(name:"foo")
		td.save()

		when:
		td.tags = ["groovy", null, "grails", '']
		and:
		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')

		then:
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )
		assertEquals( ['groovy', 'grails'], td.tags )

		when:
		td.tags = ["foo", "bar"]
		and:
		links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')

		then:
		assertEquals 2, links.size()
		assertEquals( ['foo', 'bar'].sort(true), links.tag.name.sort(true) )
		assertEquals( ['foo', 'bar'].sort(true), td.tags.sort(true) )

		when:
		td.tags = []
		and:
		links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')

		then:
		assertEquals 0, links.size()
		assertEquals( [], links.tag.name )
		assertEquals( [], td.tags )
	}

	void testFindAllByTag() {
		given:
		new TestDomain(name:"foo")
				.save()
				.addTag("groovy")
				.addTag("grails")
				.addTag("griffon")
		new TestDomain(name:"bar")
				.save()
				.addTag("groovy")
				.addTag("grails")

		when:
		def results = TestDomain.findAllByTag("groovy")

		then:
		assertEquals 2, results.size()
		assertTrue results[0] instanceof TestDomain

		assertEquals 2, TestDomain.findAllByTag("groovy").size()
		assertEquals 2, TestDomain.findAllByTag("grails").size()
		assertEquals 1, TestDomain.findAllByTag("griffon").size()
		assertEquals 0, TestDomain.findAllByTag("nothing").size()
		assertEquals 0, TestDomain.findAllByTag(null).size()

	}

	void testFindAllByTagPolymorphic() {
		given:
		new TestDomain(name:"foo")
				.save()
				.addTag("groovy")
				.addTag("grails")
				.addTag("griffon")
		new TestDescendent(name:"bar", other:'bla')
				.save()
				.addTag("groovy")
				.addTag("grails")
				.addTag("gradle")

		when:
		def results = TestDomain.findAllByTag("groovy")

		then:
		assertEquals 2, results.size()
		assertTrue results[0] instanceof TestDomain

		assertEquals 2, TestDomain.findAllByTag("groovy").size()
		assertEquals 1, TestDescendent.findAllByTag("groovy").size()

		assertEquals 2, TestDomain.findAllByTag("grails").size()
		assertEquals 1, TestDescendent.findAllByTag("grails").size()

		assertEquals 1, TestDomain.findAllByTag("gradle").size()
		assertEquals 1, TestDescendent.findAllByTag("gradle").size()

		assertEquals 1, TestDomain.findAllByTag("griffon").size()
		assertEquals 0, TestDomain.findAllByTag("nothing").size()
		assertEquals 0, TestDomain.findAllByTag(null).size()

	}

	void testCountByTag() {
		given:
		new TestDomain(name:"foo")
				.save()
				.addTag("groovy")
				.addTag("grails")
				.addTag("griffon")
		new TestDomain(name:"bar")
				.save()
				.addTag("groovy")
				.addTag("grails")
		new TestDescendent(name:"bla", other:'zzzz')
				.save()
				.addTag("groovy")
				.addTag("grails")
				.addTag("gradle")

		expect:
		assertEquals 3, TestDomain.countByTag("groovy")
		assertEquals 1, TestDescendent.countByTag("groovy")

		assertEquals 1, TestDomain.countByTag("griffon")
		assertEquals 0, TestDescendent.countByTag("griffon")

		assertEquals 1, TestDomain.countByTag("gradle")
		assertEquals 1, TestDescendent.countByTag("gradle")

		assertEquals 0, TestDomain.countByTag("rubbish")
		assertEquals 0, TestDomain.countByTag(null)

	}

	void testAllTags() {
		given:
		new TestDomain(name:"foo")
				.save()
				.addTag("groovy")
				.addTag("grails")
				.addTag("griffon")
		new TestDomain(name:"bar")
				.save()
				.addTag("groovy")
				.addTag("grails")
		new TestDescendent(name:"bla", other:'zzzz')
				.save()
				.addTag("groovy")
				.addTag("grails")
				.addTag("gradle")

		expect:
		assertEquals( ['gradle','grails','griffon','groovy'].sort(true), TestDomain.allTags.sort(true) )
		assertEquals 4, TestDomain.totalTags

		assertEquals( ['gradle','grails','groovy'].sort(true), TestDescendent.allTags.sort(true) )
		assertEquals 3, TestDescendent.totalTags
	}

	void testParseTags() {
		given:
		def td = new TestDomain(name:"foo")
				.save()

		when:
		td.parseTags("groovy,grails,griffon")

		then:
		assertEquals( ['grails','griffon','groovy'], TestDomain.allTags )
	}

	void testParseTagsWithDelimiter() {
		given:
		def td = new TestDomain(name:"foo")
				.save()

		when:
		td.parseTags("groovy grails griffon", " ")

		then:
		assertEquals( ['grails','griffon','groovy'], TestDomain.allTags )

	}
}
