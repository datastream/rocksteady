/**
 **/
package com.admob.rocksteady.reactor;

import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.admob.rocksteady.util.MongodbInterface;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;


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
        if (retention.isEmpty()) {
          gs = app + "." + name + "." + colo + "." + suffix;
        } else {
          gs = retention + "." + app + "." + name + "." + colo + "." + suffix;
        }

        if (gs == null) {
          logger.error("Null string detected");
        }

        logger.debug("graphite string:" + gs);

        // Send the data
        mongodbInterface.send(mongodbInterface.mongodbObject(gs, value));

      } catch (Exception e) {
        // logger.error("Problem with sending metric to graphite: " +
        // newEvent.toString());
      }

    }
  }

}
