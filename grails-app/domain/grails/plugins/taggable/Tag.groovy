/* Copyright 2006-2007 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.taggable

import grails.util.Holders

/**
 * A tag entity used to store the tag names
 * 
 * @author Graeme Rocher
 */
class Tag implements Serializable{

    static transients = ['caseSensitive']

    static Boolean preserveCaseForTesting = null
    static @Lazy boolean preserveCaseFromConfig = { Holders.config.grails.taggable.preserve.case instanceof ConfigObject ? false :
    Holders.config.grails.taggable.preserve.case.toString().toBoolean() }
    static boolean getPreserveCase() {
        return (preserveCaseForTesting != null) ? preserveCaseForTesting : preserveCaseFromConfig
    }

    String name

    void setName(String name) {
        this.@name = Tag.preserveCase ? name : name.toLowerCase()
    }

    String toString() {
        name
    }

    static searchable = { only = ['name'] }

    static constraints = {
        name blank:false, unique:true
    }

    static mapping = {
        cache 'read-write'

        def config = Holders.config
        if(config.grails.taggable.tag.table) {
            table config.grails.taggable.tag.table.toString()
        }
        else {
            table "tags"
        }

        if (config.grails.taggable.tag.autoImport instanceof Boolean) {
            autoImport(config.grails.taggable.tag.autoImport)
        } else {
            autoImport false
        }
    }
}