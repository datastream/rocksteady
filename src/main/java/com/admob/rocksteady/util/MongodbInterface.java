/**
 **/
package com.admob.rocksteady.util;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import java.util.*;
import java.io.*;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongodbInterface {

    /**
     * The log instance.
     */


    private static final Logger LOG = LoggerFactory.getLogger(MongodbInterface.class);

    private Boolean enableSend = true;

    /**
     * The server name hosting Mongodb.
     */
    private final String mongodbServer;

    /**
     * The port Mongodb is listening on.
     */
    private final short mongodbPort;

    private final String user;
    private final char[] passwd;
    private final String dbname;

    private Mongo mongo;

    /**
     * The number of milliseconds between each stats update.
     */
    private int updateInterval = 5 * 60 * 1000; // 5 minute default

    /**
     * The timer that runs jobs at the <code>updateInterval</code>.
     */
    private Timer jobTimer;


    public MongodbInterface(String mongodbServer, short mongodbPort, String user, char[] passwd, String dbname) {
	this.mongodbServer = mongodbServer;
	this.mongodbPort = mongodbPort;
	this.user = user;
	this.passwd = passwd;
	this.dbname = dbname;
    }

    public MongodbInterface(String mongodbServer, String dbname) {
	this(mongodbServer, (short)27017, null, null, dbname);
    }

    public void setInterval(int seconds) {
	this.updateInterval = seconds * 1000; // Convert to ms
    }


    /**
     * Forms the input string to Mongodb to record all the current data.
     *
     * @return A BasicDBObject that can be sent to the Mongodb collection.
     */
    public BasicDBObject mongodbObject(String key, String value) {

	// Current UNIX timestamp.
	long timestamp = new Date().getTime() / 1000; // seconds since midnight Jan
	// 1, 1970
	BasicDBObject obj = new BasicDBObject();
	obj.put("key", key);
	obj.put("value", value);
	obj.put("timestamp", timestamp);
	return obj;
    }

    private void connect() {
	if (this.mongo == null) {
	    try {
		this.mongo = new Mongo(this.mongodbServer, this.mongodbPort);
	    } catch (UnknownHostException e) {
		LOG.error("cant connect Host" + e.toString());
		this.mongo = null;
	    } catch (MongoException e) {
		LOG.error("mongodb connect error" + e.toString());
		this.mongo = null;
	    }
	    LOG.trace("Connect Mongodb instance.");
	}
    }

    /**
     * Sends a mongodb object to Mongodb.
     *
     * @param input is all the statistics to record in Mongodb's format. See
     *        <code>mongodbString</code>.
     */
    public void send(BasicDBObject input) {
	LOG.trace("Going to record statistics with Mongodb.");
	BasicDBObject data;

	data = input;

	if (enableSend) {
	    // Write the data to the socket.
	    try {
		// Make sure we're connected to Mongodb.
		connect();

		// Write the data as sequence of 1-byte characters. Can't just do
		// our.writeUTF because
		// it prefixes UTF encoding characters that we don't want.
		if (this.mongo != null) {
		    DB db = this.mongo.getDB( this.dbname );
		    if(!db.authenticate(user, passwd)) {
			LOG.error("wrong passwd or user");
		    }
		    DBCollection coll = db.getCollection("matrics");
		    coll.insert(data);
		}
		LOG.trace("Recorded statistics with Mongodb.");
	    } catch (MongoException e) {
		LOG.warn("Problem sending statistics to Mongodb.  Will save the data and try again.");
		LOG.debug("Problem sending to Mongodb", e);
		if (this.mongo != null) {
		    this.mongo.close();
		}
	    }
	}
    }

    public void setEnableSend(Boolean enableSend) {
	this.enableSend = enableSend;
    }

    public Boolean getEnableSend() {
	return enableSend;
    }

}
