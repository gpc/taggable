grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits "global"
    log "warn"

    repositories {        
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
    }
    plugins {
        build(":release:3.1.1", ":rest-client-builder:2.1.1") {
            export = false
        }

        runtime ":hibernate:3.6.10.19", {
            export = false
        }
    }
}
