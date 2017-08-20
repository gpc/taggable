package grails.plugins.taggable

import static org.junit.Assert.*
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import grails.transaction.*
import org.junit.Before

@TestMixin(IntegrationTestMixin)
@Rollback
class TaggableTests {
    @Before
    void resetConfig() {
        Tag.preserveCaseForTesting = false
    }
    
    void testAddTagMethodCaseInsensitive() {
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.addTag("Groovy")
		  .addTag("grails")
		
		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
		
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )
    }
    
    void testAddTagMethodCasePreserving() {
        Tag.preserveCaseForTesting = true
        
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.addTag("Groovy")
		  .addTag("grails")
		
		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
		
		assertEquals 2, links.size()
		assertEquals( ['Groovy', 'grails'], links.tag.name )

        // adding a second, even if preserving case in DB it should still not add it as already has such a tag
		td.addTag("groovy")
		
		links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
		
		assertEquals 3, links.size()
		assertEquals( ['Groovy', 'grails', 'groovy'], links.tag.name )
    }
    
    void testAddTagsMethod() {
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.addTags(["groovy","grails"])
		
		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
		
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )
    }

	void testRemoveTagMethod() {
		def td = new TestDomain(name:"foo")
		td.save()
		
		td.addTag("groovy")
		  .addTag("grails")
		
		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
		
		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )
		
		td.removeTag("groovy")
		
		links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
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
		
		td.tags = ["groovy", null, "grails", '']

		def links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')

		assertEquals 2, links.size()
		assertEquals( ['groovy', 'grails'], links.tag.name )	
		assertEquals( ['groovy', 'grails'], td.tags )			
		
		td.tags = ["foo", "bar"]			
		
		links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')

		// Grails 3.3.0 - fails links.size() returns 4 
		// foo, bar, groovy, grails
		// Grails 3.1.8 - links.size() returns 2
		assertEquals 2, links.size() 

		assertEquals( ['foo', 'bar'].sort(true), links.tag.name.sort(true) )	
		assertEquals( ['foo', 'bar'].sort(true), td.tags.sort(true) )		
		
		td.tags = []			
		
		links = TagLink.findAllWhere(tagRef:td.id, type:'testDomain')
		assertEquals 0, links.size()
		assertEquals( [], links.tag.name )	
		assertEquals( [], td.tags )					
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
	
	void testFindAllByTagPolymorphic() {
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


		def results = TestDomain.findAllByTag("groovy")
		
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

		assertEquals( ['gradle','grails','griffon','groovy'].sort(true), TestDomain.allTags.sort(true) )
		assertEquals 4, TestDomain.totalTags

		assertEquals( ['gradle','grails','groovy'].sort(true), TestDescendent.allTags.sort(true) )
		assertEquals 3, TestDescendent.totalTags
	}
	
	void testParseTags() {
		def td = new TestDomain(name:"foo")
		   			.save()
		
		td.parseTags("groovy,grails,griffon")

		assertEquals( ['grails','griffon','groovy'], TestDomain.allTags )				
	}
	
	void testParseTagsWithDelimiter() {
		def td = new TestDomain(name:"foo")
		   			.save()
		
		td.parseTags("groovy grails griffon", " ")

		assertEquals( ['grails','griffon','groovy'], TestDomain.allTags )				
		
	}
	

}
