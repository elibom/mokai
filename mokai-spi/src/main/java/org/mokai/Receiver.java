package org.mokai;

/**
 * <p>Marker interface that is implemented by the connectors capable of 
 * receiving messages from applications or external systems. Receivers 
 * use the {@link MessageProducer} interface to route the messages inside the 
 * framework.</p>
 * 
 * <p>When a receiver is added to the {@link org.mokai.RoutingEngine}, it is wrapped
 * into a {@link org.mokai.ReceiverService} which also contains the post-receiving
 * actions.</p>
 * 
 * @author German Escobar
 */
public interface Receiver {

}
