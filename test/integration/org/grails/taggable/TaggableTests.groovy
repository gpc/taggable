package org.grails.taggable

import grails.test.*

class TaggableTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testAddTagMethod() {
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.addTag("groovy")
		  .addTag("grails")
		
		def links = TagLink.findAllWhere(tagRef:td.id, tagClass:td.class.name)
		
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )
    }

	void testRemoveTagMethod() {
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.addTag("groovy")
		  .addTag("grails")
		
		def links = TagLink.findAllWhere(tagRef:td.id, tagClass:td.class.name)
		
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )
		
		td.removeTag("groovy")
		
		links = TagLink.findAllWhere(tagRef:td.id, tagClass:td.class.name)
		assertEquals 1, links.size()
		assertEquals( ['grails'], links.tag.name )
		
	}

	void testGetTagsMethod() {
		
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.addTag("groovy")
		  .addTag("grails")
		
		td.save(flush:true)
		
		TestDomain.withSession { session -> session.clear() }		
		
		td = TestDomain.findByName("foo")
		
     	assertEquals( ['groovy', 'grails'], td.tags )
	}
	
	void testSetTagsMethod() {
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.tags = ["groovy", "grails"]
		
		def links = TagLink.findAllWhere(tagRef:td.id, tagClass:td.class.name)
		
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )	
		assertEquals( ['groovy', 'grails'], td.tags )			
		
		td.tags = ["foo", "bar"]			
		
		links = TagLink.findAllWhere(tagRef:td.id, tagClass:td.class.name)
		assertEquals 2, links.size()
		assertEquals( ['foo', 'bar'], links.tag.name )	
		assertEquals( ['foo', 'bar'], td.tags )			
		
	}
	
	void testFindAllByTag() {
		new TestDomain(name:"foo")
		   .save()		
		  .addTag("groovy")
		  .addTag("grails")
		  .addTag("griffon")		
		new TestDomain(name:"bar")
		   .save()		
		  .addTag("groovy")
		  .addTag("grails")


		def results = TestDomain.findAllByTag("groovy")
		
		assertEquals 2, results.size()
		assertTrue results[0] instanceof TestDomain
		
		assertEquals 2, TestDomain.findAllByTag("groovy").size()			
		assertEquals 2, TestDomain.findAllByTag("grails").size()					
		assertEquals 1, TestDomain.findAllByTag("griffon").size()							
		assertEquals 0, TestDomain.findAllByTag("nothing").size()							
		assertEquals 0, TestDomain.findAllByTag(null).size()											
		
	}
	

	void testCountByTag() {
		
		new TestDomain(name:"foo")
		   .save()		
		  .addTag("groovy")
		  .addTag("grails")
		  .addTag("griffon")		
		new TestDomain(name:"bar")
		   .save()		
		  .addTag("groovy")
		  .addTag("grails")


		assertEquals 2, TestDomain.countByTag("groovy")
		assertEquals 1, TestDomain.countByTag("griffon")		
		assertEquals 0, TestDomain.countByTag("rubish")		
		assertEquals 0, TestDomain.countByTag(null)				
		
	}
}
