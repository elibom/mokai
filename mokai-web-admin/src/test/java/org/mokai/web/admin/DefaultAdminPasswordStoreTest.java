package org.mokai.web.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultAdminPasswordStoreTest {

	@BeforeMethod
	public void setup() {
		String path = "src/test/resources/.pwd";

		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void shouldRetrievePassword() throws Exception {
		DefaultAdminPasswordStore passwordStore = new DefaultAdminPasswordStore();
		passwordStore.setPasswordPath("src/test/resources/.test");

		String password = passwordStore.getPassword();
		Assert.assertEquals(password, "german");
	}

	@Test
	public void shouldSavePassword() throws Exception {
		String path = "src/test/resources/.pwd";

		DefaultAdminPasswordStore passwordStore = new DefaultAdminPasswordStore();
		passwordStore.setPasswordPath(path);

		boolean saved = passwordStore.setPassword("test");
		Assert.assertTrue(saved);

		File file = new File(path);
		Assert.assertTrue(file.exists());

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String password = in.readLine();

			Assert.assertEquals(password, "test");
		} finally {
			try { in.close(); } catch (Exception e) {}
		}
	}
}
