/*
 * Copyright SoftInstigate srl. All Rights Reserved.
 *
 *
 * The copyright to the computer program(s) herein is the property of
 * SoftInstigate srl, Italy. The program(s) may be used and/or copied only
 * with the written permission of SoftInstigate srl or in accordance with the
 * terms and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied. This copyright notice must not be removed.
 */
package com.softinstigate.restheart.handlers.database;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import com.softinstigate.restheart.db.MongoDBClientSingleton;
import com.softinstigate.restheart.handlers.PipedHttpHandler;
import com.softinstigate.restheart.utils.ChannelReader;
import com.softinstigate.restheart.utils.HttpStatus;
import com.softinstigate.restheart.utils.RequestContext;
import com.softinstigate.restheart.utils.ResponseHelper;
import io.undertow.server.HttpServerExchange;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uji
 */
public class PutDBHandler extends PipedHttpHandler
{
    private static final MongoClient client = MongoDBClientSingleton.getInstance().getClient();

    private static final Logger logger = LoggerFactory.getLogger(PutDBHandler.class);

    /**
     * Creates a new instance of EntityResource
     */
    public PutDBHandler()
    {
        super(null);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange, RequestContext context) throws Exception
    {
        if (context.getDBName().isEmpty() || context.getDBName().startsWith("@"))
        {
            ResponseHelper.endExchangeWithError(exchange, HttpStatus.SC_NOT_ACCEPTABLE, new IllegalArgumentException("db name cannot be empty or start with @"));
            return;
        }

        String _content = ChannelReader.read(exchange.getRequestChannel());

        DBObject content;

        try
        {
            content = (DBObject) JSON.parse(_content);
        }
        catch (JSONParseException ex)
        {
            ResponseHelper.endExchangeWithError(exchange, HttpStatus.SC_NOT_ACCEPTABLE, ex);
            return;
        }
        
        // cannot PUT an array
        if (content instanceof BasicDBList)
        {
            ResponseHelper.endExchange(exchange, HttpStatus.SC_NOT_ACCEPTABLE);
            return;
        }

        boolean updating = client.getDatabaseNames().contains(context.getDBName());

        DB db = client.getDB(context.getDBName());

        DBCollection coll = db.getCollection("@metadata");

        BasicDBObject query = new BasicDBObject("_id", "@metadata");
        
        // apply new values
        
        String now = Instant.now().toString();
        
        if (content == null)
            content = new BasicDBObject();
        
        if (updating)
        {
            content.put("@lastupdated_on", now);
        }
        else
        {
            content.put("_id", "@metadata");
            content.put("@created_on", now);
        }
        
        coll.update(query, new BasicDBObject("$set", content), true, false);

        if (updating)
            ResponseHelper.endExchange(exchange, HttpStatus.SC_OK);
        else
            ResponseHelper.endExchange(exchange, HttpStatus.SC_CREATED);
    }
}