package org.mokai.web.admin;

/**
 * Retrieves and saves the adminstrator password in a persistent mechanism.
 *
 * @author German Escobar
 */
public interface AdminPasswordStore {

    /**
     * Retrieves the password as it was saved in the store.
     *
     * @return the saved password.
     */
    String getPassword();

    /**
     * Saves the password in the store. It is advised to encrypt it first before saving.
     *
     * @param password the password to be saved.
     * @return true if the password was successfully saved, false otherwise.
     */
    boolean setPassword(String password);
}
