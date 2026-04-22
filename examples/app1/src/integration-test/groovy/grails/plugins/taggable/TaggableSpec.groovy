package grails.plugins.taggable

import spock.lang.Specification

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class TaggableSpec extends Specification {

    void setup() {
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
        links.size() == 2
        links.tag.name == ['groovy', 'grails']
    }

    void testAddTagMethodCasePreserving() {
        given:
        Tag.preserveCaseForTesting = true

        def td = new TestDomain(name: "foo")
        td.save()

        when:
        td.addTag("Groovy")
                .addTag("grails")

        def links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')
        then:
        links.size() == 2
        links.tag.name == ['Groovy', 'grails']

        when:
        // adding a second, even if preserving case in DB it should still not add it as already has such a tag
        td.addTag("groovy")
        and:
        links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

        then:
        links.size() == 3
        links.tag.name == ['Groovy', 'grails', 'groovy']
    }

    void testAddTagsMethod() {
        given:
        def td = new TestDomain(name: "foo")
        td.save()

        when:
        td.addTags(["groovy", "grails"])
        and:
        def links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

        then:
        links.size() == 2
        links.tag.name == ['groovy', 'grails']
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
        links.size() == 2
        links.tag.name == ['groovy', 'grails']

        when:
        td.removeTag("groovy")
        and:
        links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

        then:
        links.size() == 1
        links.tag.name == ['grails']
    }

    void testGetTagsMethod() {
        given:
        def td = new TestDomain(name: "foo")
        td.save()

        when:
        td.addTag("groovy")
                .addTag("grails")
        td.save(flush: true)

        and:
        TestDomain.withSession { session -> session.clear() }
        td = TestDomain.findByName("foo")

        then:
        td.tags == ['groovy', 'grails']
    }

    void testSetTagsMethod() {
        given:
        def td = new TestDomain(name: "foo")
        td.save()

        when:
        td.tags = ["groovy", null, "grails", '']
        and:
        def links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

        then:
        links.size() == 2
        links.tag.name == ['groovy', 'grails']
        td.tags == ['groovy', 'grails']

        when:
        td.tags = ["foo", "bar"]
        and:
        links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

        then:
        links.size() == 2
        links.tag.name.sort(true) == ['foo', 'bar'].sort(true)
        td.tags.sort(true) == ['foo', 'bar'].sort(true)

        when:
        td.tags = []
        and:
        links = TagLink.findAllWhere(tagRef: td.id, type: 'testDomain')

        then:
        links.size() == 0
        links.tag.name == []
        td.tags == []
    }

    void testFindAllByTag() {
        given:
        new TestDomain(name: "foo")
                .save()
                .addTag("groovy")
                .addTag("grails")
                .addTag("griffon")
        new TestDomain(name: "bar")
                .save()
                .addTag("groovy")
                .addTag("grails")

        when:
        def results = TestDomain.findAllByTag("groovy")

        then:
        results.size() == 2
        results[0] instanceof TestDomain
        TestDomain.findAllByTag("groovy").size() == 2
        TestDomain.findAllByTag("grails").size() == 2
        TestDomain.findAllByTag("griffon").size() == 1
        TestDomain.findAllByTag("nothing").size() == 0
        TestDomain.findAllByTag(null).size() == 0
    }

    void testFindAllByTagPolymorphic() {
        given:
        new TestDomain(name: "foo")
                .save()
                .addTag("groovy")
                .addTag("grails")
                .addTag("griffon")
        new TestDescendent(name: "bar", other: 'bla')
                .save()
                .addTag("groovy")
                .addTag("grails")
                .addTag("gradle")

        when:
        def results = TestDomain.findAllByTag("groovy")

        then:
        results.size() == 2
        results[0] instanceof TestDomain
        TestDomain.findAllByTag("groovy").size() == 2
        TestDescendent.findAllByTag("groovy").size() == 1
        TestDomain.findAllByTag("grails").size() == 2
        TestDescendent.findAllByTag("grails").size() == 1
        TestDomain.findAllByTag("gradle").size() == 1
        TestDescendent.findAllByTag("gradle").size() == 1
        TestDomain.findAllByTag("griffon").size() == 1
        TestDomain.findAllByTag("nothing").size() == 0
        TestDomain.findAllByTag(null).size() == 0
    }

    void testCountByTag() {
        given:
        new TestDomain(name: "foo")
                .save()
                .addTag("groovy")
                .addTag("grails")
                .addTag("griffon")
        new TestDomain(name: "bar")
                .save()
                .addTag("groovy")
                .addTag("grails")
        new TestDescendent(name: "bla", other: 'zzzz')
                .save()
                .addTag("groovy")
                .addTag("grails")
                .addTag("gradle")

        expect:
        TestDomain.countByTag("groovy") == 3
        TestDescendent.countByTag("groovy") == 1
        TestDomain.countByTag("griffon") == 1
        TestDescendent.countByTag("griffon") == 0
        TestDomain.countByTag("gradle") == 1
        TestDescendent.countByTag("gradle") == 1
        TestDomain.countByTag("rubbish") == 0
        TestDomain.countByTag(null) == 0
    }

    void testAllTags() {
        given:
        new TestDomain(name: "foo")
                .save()
                .addTag("groovy")
                .addTag("grails")
                .addTag("griffon")
        new TestDomain(name: "bar")
                .save()
                .addTag("groovy")
                .addTag("grails")
        new TestDescendent(name: "bla", other: 'zzzz')
                .save()
                .addTag("groovy")
                .addTag("grails")
                .addTag("gradle")

        expect:
        TestDomain.allTags.sort(true) == ['gradle', 'grails', 'griffon', 'groovy'].sort(true)
        TestDomain.totalTags == 4
        TestDescendent.allTags.sort(true) == ['gradle', 'grails', 'groovy'].sort(true)
        TestDescendent.totalTags == 3
    }

    void testParseTags() {
        given:
        def td = new TestDomain(name: "foo")
                .save()

        when:
        td.parseTags("groovy,grails,griffon")

        then:
        TestDomain.allTags == ['grails', 'griffon', 'groovy']
    }

    void testParseTagsWithDelimiter() {
        given:
        def td = new TestDomain(name: "foo")
                .save()

        when:
        td.parseTags("groovy grails griffon", " ")

        then:
        TestDomain.allTags == ['grails', 'griffon', 'groovy']
    }
}
