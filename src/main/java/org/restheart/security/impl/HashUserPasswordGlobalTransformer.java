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
package org.restheart.security.impl;

import io.undertow.server.HttpServerExchange;
import org.bson.BsonDocument;
import org.restheart.handlers.RequestContext;
import org.restheart.metadata.transformers.GlobalTransformer;
import org.restheart.metadata.transformers.HashTransformer;
import org.restheart.metadata.transformers.RequestTransformer;
import org.restheart.security.RequestContextPredicate;

/**
 *
 * @author Andrea Di Cesare <andrea@softinstigate.com>
 */
public class HashUserPasswordGlobalTransformer extends GlobalTransformer {
    /**
     *
     * @param db name of the db to apply the transformer (if null, apply to all
     * dbs)
     * @param coll name of the collection to apply the transformer (mandatory)
     * @param args arguments to pass to HashTransformer
     * @param propertyNamePassword the name of the password property
     */
    public HashUserPasswordGlobalTransformer(
            String db,
            String coll,
            BsonDocument args,
            String propertyNamePassword) {
        super(
                new HashTransformer(),
                new RequestContextPredicate() {
            @Override
            public boolean resolve(HttpServerExchange hse, RequestContext context) {
                return (db == null
                        ? true
                        : db.equals(context.getDBName()))
                        && coll.equals(context.getCollectionName())
                        && (((context.isPut() || context.isPatch())
                        && context.isDocument())
                        || (context.isPost()
                        && (context.isCollection() || context.isBulkDocuments())));
            }
        },
                RequestTransformer.PHASE.REQUEST,
                null,
                args,
                null);
    }

}
