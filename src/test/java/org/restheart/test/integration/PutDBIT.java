/*
 * RESTHeart - the Web API for MongoDB
 * Copyright (C) SoftInstigate Srl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.restheart.test.integration;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import io.undertow.util.Headers;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import static org.junit.Assert.*;
import org.junit.Test;
import org.restheart.hal.Representation;
import static org.restheart.test.integration.HttpClientAbstactIT.adminExecutor;
import org.restheart.utils.HttpStatus;

/**
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
public class PutDBIT extends HttpClientAbstactIT {

    public PutDBIT() {
    }

    @Test
    public void testPutCollection() throws Exception {
        Response resp;

        // *** PUT tmpdb
        resp = adminExecutor.execute(Request.Put(dbTmpUri).bodyString("{a:1}", halCT).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));
        check("check put db", resp, HttpStatus.SC_CREATED);

        // try to put without etag forcing checkEtag
        resp = adminExecutor.execute(Request.Put(addCheckEtag(dbTmpUri)).bodyString("{a:1}", halCT).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));
        check("check put tmp db without etag forcing checkEtag", resp, HttpStatus.SC_CONFLICT);

        // try to put without etag
        resp = adminExecutor.execute(Request.Put(dbTmpUri).bodyString("{a:1}", halCT).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));
        check("check put tmp db without etag", resp, HttpStatus.SC_OK);

        // try to put with wrong etag
        resp = adminExecutor.execute(Request.Put(dbTmpUri).bodyString("{a:1}", halCT).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE).addHeader(Headers.IF_MATCH_STRING, "pippoetag"));
        check("check put tmp db with wrong etag forcing checkEtag", resp, HttpStatus.SC_PRECONDITION_FAILED);

        resp = adminExecutor.execute(Request.Get(dbTmpUri).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));

        JsonObject content = Json.parse(resp.returnContent().asString()).asObject();

        String etag = content.get("_etag").asObject().get("$oid").asString();

        // try to put with correct etag
        resp = adminExecutor.execute(Request.Put(dbTmpUri).bodyString("{b:2}", halCT).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE).addHeader(Headers.IF_MATCH_STRING, etag));
        check("check put tmp db with correct etag", resp, HttpStatus.SC_OK);

        resp = adminExecutor.execute(Request.Get(dbTmpUri).addHeader(Headers.CONTENT_TYPE_STRING, Representation.HAL_JSON_MEDIA_TYPE));

        content = Json.parse(resp.returnContent().asString()).asObject();
        assertNull("check put content", content.get("a"));
        assertNotNull("check put content", content.get("b"));
        assertTrue("check put content", content.get("b").asInt() == 2);
    }
}
