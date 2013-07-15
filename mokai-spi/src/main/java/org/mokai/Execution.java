package org.mokai;

/**
 * Used by {@link Action} implementations to control the execution of the message (eg. stopping the message or
 * routing more messages). {@link Action} implementations need to define a field using the {@link Resource}
 * annotation.
 *
 * @author German Escobar
 */
public interface Execution {

	/**
	 * Stops the execution of the current message. Notice that this only affects the message that is being
	 * processed by the action. Others messages that you send using the {@link #route(Message)} method won't
	 * get affected.
	 */
	void stop();

	/**
	 * Routes a new message into the gateway. The message will continue the current flow starting with the
	 * next action (if exists), it won't pass through the previous actions.
	 *
	 * @param message the message to be routed.
	 * @throws Exception if something goes wrong.
	 */
	void route(Message message) throws Exception;

}
