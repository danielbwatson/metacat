/*
 * Copyright 2016 Netflix, Inc.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.metacat.main.search

import com.netflix.metacat.common.MetacatRequestContext
import com.netflix.metacat.common.MetacatRequestContext
import com.netflix.metacat.common.dto.TableDto
import com.netflix.metacat.common.json.MetacatJsonLocator
import com.netflix.metacat.common.util.DataProvider
import com.netflix.metacat.main.services.search.ElasticSearchDoc
import spock.lang.Unroll

import static com.netflix.metacat.main.services.search.ElasticSearchDoc.Type

/**
 * Created by amajumdar on 8/17/15.
 */
class ElasticSearchUtilSpec extends BaseEsSpec{

    @Unroll
    def "Test save for #id"(){
        given:
        def table = DataProvider.getTable(catalogName, databaseName, tableName, "amajumdar", "s3:/a/b")
        es.save(Type.table.name(), id, es.toJsonString(id, table, metacatContext, false))
        def result = (TableDto)es.get(Type.table.name(),id).getDto()
        expect:
        id==result.getName().toString()
        where:
        catalogName     | databaseName  | tableName     | id
        'prodhive'      | 'amajumdar'   | 'part'        | 'prodhive/amajumdar/part'
        'prodhive'      | 'amajumdar'   | 'part'        | 'prodhive/amajumdar/part'
    }

    @Unroll
    def "Test saves for #id"(){
        given:
        def table = DataProvider.getTable(catalogName, databaseName, tableName, "amajumdar", "s3:/a/b/c")
        es.save(Type.table.name(),[new ElasticSearchDoc(table.name.toString(), table, metacatContext.userName, false)])
        def result = (TableDto)es.get(Type.table.name(),id).getDto()
        expect:
        id==result.getName().toString()
        where:
        catalogName     | databaseName  | tableName     | id
        'prodhive'      | 'amajumdar'   | 'part_test'   | 'prodhive/amajumdar/part_test'
    }

    @Unroll
    def "Test delete for #id"(){
        given:
        def table = DataProvider.getTable(catalogName, databaseName, tableName, "amajumdar", "s3:/a/b")
        es.save(Type.table.name(), id, es.toJsonString(id, table, metacatContext, false))
        softDelete?es.softDelete(Type.table.name(), id, metacatContext):es.delete(Type.table.name(), id)
        def result =  es.get(Type.table.name(),id)
        expect:
        if( softDelete){
            id==((TableDto)result.getDto()).getName().toString()
        } else {
            result == null
        }
        where:
        catalogName     | databaseName  | tableName     | id                        | softDelete
        'prodhive'      | 'amajumdar'   | 'part'        | 'prodhive/amajumdar/part' | false
        'prodhive'      | 'amajumdar'   | 'part'        | 'prodhive/amajumdar/part' | true
    }

    @Unroll
    def "Test deletes for #id"(){
        given:
        def table = DataProvider.getTable(catalogName, databaseName, tableName, "amajumdar", "s3:/a/b")
        es.save(Type.table.name(), id, es.toJsonString(id, table, metacatContext, false))
        softDelete?es.softDelete(Type.table.name(), [id], metacatContext):es.delete(Type.table.name(), [id])
        def result = es.get(Type.table.name(),id)
        expect:
        if( softDelete){
            id==((TableDto)result.getDto()).getName().toString()
        } else {
            result == null
        }
        where:
        catalogName     | databaseName  | tableName     | id                        | softDelete
        'prodhive'      | 'amajumdar'   | 'part'        | 'prodhive/amajumdar/part' | false
        'prodhive'      | 'amajumdar'   | 'part'        | 'prodhive/amajumdar/part' | true
    }

    @Unroll
    def "Test updates for #id"(){
        given:
        def table = DataProvider.getTable(catalogName, databaseName, tableName, metacatContext.getUserName(), uri)
        es.save(Type.table.name(), id, es.toJsonString(id, table, metacatContext, false))
        es.updates(Type.table.name(), [id], new MetacatRequestContext("testUpdate", null, null, null, null), MetacatJsonLocator.INSTANCE.parseJsonObject('{"dataMetadata": {"metrics":{"count":10}}}'))
        def result = es.get(Type.table.name(),id)
        es.refresh()
        def resultByUri = es.getTableIdsByUri(Type.table.name(), uri)
        expect:
        result != null
        result.getUser()=="testUpdate"
        ((TableDto)result.getDto()).getDataMetadata()!=null
        resultByUri!=null
        resultByUri.size()==1
        resultByUri[0]==id
        where:
        catalogName     | databaseName  | tableName     | id                        | uri
        'prodhive'      | 'amajumdar'   | 'part'        | 'prodhive/amajumdar/part' | 's3:/a/b'
    }

    @Unroll
    def "Test deletes for #type"(){
        given:
        def tables = DataProvider.getTables(catalogName, databaseName, tableName, "amajumdar", "s3:/a/b", noOfTables)
        def docs = tables.collect{
            String userName = it.getAudit() != null ? it.getAudit().getCreatedBy()
                    : "admin";
            return new ElasticSearchDoc(it.getName().toString(), it, userName, false, null)
        }
        es.save(Type.table.name(), docs)
        es.refresh()
        es.delete( new MetacatRequestContext("testUpdate", null, null, null, null), Type.table.name(), softDelete)
        where:
        catalogName     | databaseName  | tableName     | noOfTables     | softDelete
        'prodhive'      | 'amajumdar'   | 'part'        | 10             | false
        'prodhive'      | 'amajumdar'   | 'part'        | 0              | false
        'prodhive'      | 'amajumdar'   | 'part'        | 1000           | false
        'prodhive'      | 'amajumdar'   | 'part'        | 10             | true
        'prodhive'      | 'amajumdar'   | 'part'        | 0              | true
    }
}
