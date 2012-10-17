package org.mokai.web.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author German Escobar
 */
public final class DefaultAdminPasswordStore implements AdminPasswordStore {
	
	private Logger log = LoggerFactory.getLogger(DefaultAdminPasswordStore.class);

	private static final String DEFAULT_PATH = "data/admin/.pwd";
	
	private String path = DEFAULT_PATH;
	
	@Override
	public final synchronized String getPassword() {
		File file = new File(path);
		
		if (file.exists()) {
			BufferedReader in = null;
			
			try {
				in = new BufferedReader(new FileReader(file));
				String encryptedPassword = in.readLine();
				
				return encryptedPassword;
			} catch (IOException e) {
				log.error("IOException retriving password: " + e.getMessage(), e);
			} finally {
				try { in.close(); } catch (Exception e) {}
			}
		}
		
		return null;
	}
	
	@Override
	public final synchronized boolean setPassword(String password) {
		String folder = path.substring(0, path.lastIndexOf('/') + 1);
		
		File fDir = new File(folder);
		fDir.mkdirs();
		
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new FileWriter(path));
			out.println(password);
			
			return true;
		} catch (Exception e) {
			log.error("Exception setting password: " + e.getMessage(), e);
		} finally {
			if (out != null) {
				try { out.close(); } catch (Exception e) {}
			}
		}
		
		return false;
	}
	
	public void setPasswordPath(String path) {
		this.path = path;
	}
}
