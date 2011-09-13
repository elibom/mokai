package org.mokai.connector.smpp;

/**
 * Helper methods for the {@link SmppConnector}
 * 
 * @author German Escobar
 */
public class SmppUtil {

	/**
     * Converts a long value to a hex string.  E.g. 98765432101L to "16fee0e525".
     * 
     * Note: this method was copied from the Cloudhopper SMPP Project (https://github.com/twitter/cloudhopper-smpp)
     * 
     * @param value
     * @return
     */
    static String toMessageIdAsHexString(long value) {
        return String.format("%x", value);
    }

    /**
     * Converts a hex string to a long value.  E.g. "16fee0e525" to 98765432101L.
     * 
     * Note: this method was copied from the Cloudhopper SMPP Project (https://github.com/twitter/cloudhopper-smpp)
     * 
     * @param value
     * @return
     */
    static public long toMessageIdAsLong(String value) throws NumberFormatException {
        return Long.parseLong(value, 16);
    }
}
