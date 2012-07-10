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
	    try {
		String name = newEvent.get("name").toString();
		String value = newEvent.get("value").toString();
		String colo = newEvent.get("colo").toString();
		String retention = newEvent.get("retention").toString();
		String app = newEvent.get("app").toString();
		String gs;

		if (app.equals("cpu")) {
		    String[] splitName = name.split("\\.");
		    StringBuffer sb = new StringBuffer();
		    for (int i = 1; i < splitName.length; i++) {
			if (sb.length() > 0) {
			    sb.append(".");
			}
			sb.append(splitName[i]);
		    }
		    name = sb.toString();
		}
		if ( (type != null) && (type.equals("uniq_host"))) {
		    String hostname = newEvent.get("hostname").toString();
		    gs = app + "." + name + "." + colo + "." + hostname;
		} else {
		    gs = app + "." + name + "." + colo;
		}

		if (!retention.isEmpty()) {
		    gs = retention + "." + gs;
		}

		if ((suffix != null)&&(suffix.length() != 0)) {
		    gs = gs + "." + suffix;
		}

		if (gs == null) {
		    logger.error("Null string detected");
		}

		logger.debug("mogodb string: " + gs + value);

		// Send the data
		mongodbInterface.send(mongodbInterface.mongodbObject(gs, value));

	    } catch (Exception e) {
		logger.error("Problem with sending metric to mongodb: " +
			     e.toString());
	    }
	}
    }
}
