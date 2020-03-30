package com.microsoft.windowsazure.messaging;

import android.content.Context;

import androidx.test.filters.SmallTest;

import java.net.URI;
import java.util.UUID;
import org.junit.Test;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SmallTest
public class NotificationHubTests {
	@Test
	public void testCreateNotificationHub() {
		String nhName = "myHub";
		String cs = ConnectionString.createUsingSharedAccessKeyWithListenAccess(URI.create("http://myUrl.com"), "secret123");
		Context context = getInstrumentation().getTargetContext();
		NotificationHub nh = new NotificationHub(nhName, cs, context);

		assertEquals(nh.getNotificationHubPath(), nhName);
		assertEquals(nh.getConnectionString(), cs);
	}

	@Test
	public void testCreateNotificationHubWithInvalidValues() {
		String nhName = "myHub";
		String cs = ConnectionString.createUsingSharedAccessKeyWithListenAccess(URI.create("http://myUrl.com"), "secret123");
		Context context = getInstrumentation().getTargetContext();

		try {
			new NotificationHub(null, cs, context);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		}

		try {
			new NotificationHub(nhName, null, context);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		}

		try {
			new NotificationHub(nhName, UUID.randomUUID().toString(), context);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		}

		try {
			new NotificationHub(nhName, cs, null);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testRegisterWithInvalidValues() {
		String nhName = "myHub";
		String cs = ConnectionString.createUsingSharedAccessKeyWithListenAccess(URI.create("http://myUrl.com"), "secret123");
		Context context = getInstrumentation().getTargetContext();
		NotificationHub nh = new NotificationHub(nhName, cs, context);

		String[] tags = { "myTag_1", "myTag_2" };

		try {
			nh.register(null, tags);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
		}
	}

	@Test
	public void testRegisterTemplateWithInvalidValues() {
		String nhName = "myHub";
		String cs = ConnectionString.createUsingSharedAccessKeyWithListenAccess(URI.create("http://myUrl.com"), "secret123");
		Context context = getInstrumentation().getTargetContext();
		NotificationHub nh = new NotificationHub(nhName, cs, context);

		String fcmId = "123456";
		String templateName = "myTemplate";
		String template = "{\"my_int\": 1, \"my_string\": \"1\" }";
		String[] tags = { "myTag_1", "myTag_2" };

		try {
			nh.registerTemplate(null, templateName, template, tags);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
		}

		try {
			nh.registerTemplate(fcmId, null, template, tags);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
		}

		try {
			nh.registerTemplate(fcmId, templateName, null, tags);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
		}
	}

	@Test
	public void testUnregisterTemplateWithInvalidValues() {
		String nhName = "myHub";
		String cs = ConnectionString.createUsingSharedAccessKeyWithListenAccess(URI.create("http://myUrl.com"), "secret123");
		Context context = getInstrumentation().getTargetContext();
		NotificationHub nh = new NotificationHub(nhName, cs, context);

		try {
			nh.unregisterTemplate(null);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
		}
	}

	@Test
	public void testUnregisterAllWithInvalidValues() {
		String nhName = "myHub";
		String cs = ConnectionString.createUsingSharedAccessKeyWithListenAccess(URI.create("http://myUrl.com"), "secret123");
		Context context = getInstrumentation().getTargetContext();
		NotificationHub nh = new NotificationHub(nhName, cs, context);

		try {
			nh.unregisterAll(null);

			fail("invalid parameters");
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
		}
	}

}
