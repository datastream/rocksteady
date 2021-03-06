/**
 **/
package com.admob.rocksteady.reactor;

import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.admob.rocksteady.util.MongodbInterface;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.PropertyAccessException;


/**
 *
 * @author Implement Esper UpdateListener to be used when event is triggered.
 *         This is testing the base form.
 *
 */
public class Mongodb implements UpdateListener {
    private static final Logger logger = LoggerFactory.getLogger(Mongodb.class);

    private String type;
    private String tag;

    private String suffix;

    @Autowired
    private MongodbInterface mongodbInterface;


    public void setType(String type) {
	this.type = type;
    }

    public void setTag(String tag) {
	this.tag = tag;
    }

    public String getTag() {
	return tag;
    }


    public String getSuffix() {
	return suffix;
    }

    public void setSuffix(String suffix) {
	this.suffix = suffix;
    }

    /**
     * Handle the triggered event
     *
     * @param newEvents the new events in the window
     * @param oldEvents the old events in the window
     */
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {

	if (newEvents == null) {
	    return;
	}
	for (EventBean newEvent : newEvents) {
	    String retention;
	    String app;
	    String name;
	    String colo;
	    String value;
	    String hostname;
	    String timestamp;
	    try {
		retention = newEvent.get("retention").toString();
		app = newEvent.get("app").toString();
		// get name
		if (type == null) {
		    name = newEvent.get("name").toString();
		} else {
		    name = new String(type);
		}

		try {
		    hostname = newEvent.get("hostname").toString();
		} catch (Exception e) {
		    hostname = new String("");
		}

		colo = newEvent.get("colo").toString();

		value = newEvent.get("value").toString();

		try {
		    timestamp = newEvent.get("timestamp").toString();
		} catch (Exception e) {
		    timestamp = "";
		}

		if (retention.isEmpty() || (retention == null)) {
		    retention = new String("");
		}
		if (suffix == null) {
		    suffix = new String("");
		}

		logger.debug("mogodb string: " + retention + "." + app + "." + name + "." + colo + "." + hostname  + "." + suffix + " " + value + " ");

		// Send the data
		mongodbInterface.send(mongodbInterface.mongodbObject(retention, app, name, colo, hostname, suffix, value, timestamp));
	    } catch (Exception e) {
		logger.error("Problem with sending metric to mongodb: " +
			     e.toString());
	    }
	}
    }
}
