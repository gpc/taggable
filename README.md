Taggable Grails Plugin
======================

The [Taggable plugin](http://grails.org/plugin/taggable) that adds a generic mechanism for tagging data.

Taggable Plugin
---------------
This plugin provides an alternative to the Acts as Taggable hosted at grails.org and with the following features.

Classes can be made taggable by implementing the [org.grails.taggable.Taggable](/src/groovy/org/grails/taggable/Taggable.groovy) interface
* Method chaining can be used to add tags
* The table name the domain classes use is customizable
* Utilizes extensive caching to improve performance
* Property use of packages to avoid domain conflicts

Requirements
------------
Grails Version: 1.1 and above JDK: 1.5 and above

Installation
------------

Add this dependency to `BuildConfig.groovy`
```groovy
grails.project.dependency.resolution = {
  plugins {
    compile ":taggable:1.0.1"
  }
}
```

For old Grails 1.x
```
grails install-plugin taggable
```
By default the plugin will force all tags to lower case. If you want to preserve the case of tags, you must specify the following in `Config.groovy`:

```groovy
grails.taggable.preserve.case = true
```

This will preserve the supplied case of tags when adding and removing them. Eg adding tags "grails" and "Grails" will result in two tags on the object. The finder methods for locating objects by tag still require the exact case of the tag you want to find - each tag is treated as discrete.


Usage
-----

Implement the `Taggable` interface:

```groovy
import org.grails.taggable.*
class Vehicle implements Taggable {
}
```

Add some tags:

```groovy
def v = Vehicle.get(1)
v.addTag("red")
 .addTag("sporty")
 .addTag("expensive")
// Alternatively
v.setTags(['red', 'sporty', 'expensive'])
// Or
v.addTags(['electric', 'hybrid'])
```

Query:
```groovy
def v = Vehicle.get(1)
println v.tags
def vehicles = Vehicle.findAllByTag("sporty") // Also takes params eg [max:5]
def count = Vehicle.countByTag("sporty")

assert 3 == Vehicle.totalTags
assert ['expensive', 'red','sporty'] == Vehicle.allTags

// Find all cars with tag "electric", where the instances also 
def teslaElectricCars = Vehicle.findAllByTagWithCriteria('electric') {
    eq('manufacturer', 'Tesla Motors')
}

// Find all the tags for this class, using the supplied params and criteria that operate on the TAGS
def fiveCoolTags = Vehicle.findAllTagsWithCriteria( [max:5]) {
    ilike('name', '%cool%')
}
```

Query with HQL:
```groovy
//NOTE: Tag and TagLink aren't GORM domain objects, but they are both mapped and available for HQL
//Find Vehicles by tag, but group on model
String findByTagHQL = """
   SELECT vehicle
   FROM Vehicle vehicle
            ,TagLink tagLink
   WHERE vehicle.id = tagLink.tagRef
   AND tagLink.type = 'Vehicle'
   AND tagLink.tag.name IN (:tags)
   GROUP BY vehicle.model.name
   ORDER BY vehicle.model.name
"""
List tags = ["SUV", "gas-guzzler", "tank", "boat"]
def gasGuzzlers = Vehicle.executeQuery(findByTagHQL, [tags: tags])


//List all tags since there isn't a Tag domain object
String listAllHQL = """
   SELECT tag
   FROM Tag tag
   ORDER BY tag.name
"""
def allTags = Vehicle.executeQuery(listAllHQL)

Tag parsing:

def tags = "red,sporty,expensive"
def v = Vehicle.get(1)
v.parseTags(tags)
assert ['expensive', 'red','sporty'] == v.tags

tags = "red/sporty/expensive"

v.parseTags(tags, "/")
assert ['expensive', 'red','sporty'] == v.tags
```

Configuration options
---------------------

### Customized Table names
You can change the table names used for the `Tag` and `TagLink` classes in `Config.groovy`:

```groovy
grails.taggable.tag.table="MY_TAGS"
grails.taggable.tagLink.table="MY_TAG_LINKS"
```

### Turn hibernate autoImport back on
Since version 1.0 snapshots, hibernate autoImport is turned off in the mapping DSL to avoid clashes with existing domain classes called `Tag` or `TagLink`. If you really need to revert to the old behaviour you can do so with this `Config.groovy`:

```groovy
grails.taggable.tag.autoImport=true
grails.taggable.tagLink.autoImport=true
```
