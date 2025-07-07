/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 *
 * Copyright (c) 2008 - 2011 LSC Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of the LSC Project nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *                  ==LICENSE NOTICE==
 *
 *               (c) 2008 - 2025 LSC Project
 ****************************************************************************
 */
package org.lsc.plugins.connectors.executable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.lsc.LscDatasets;
import org.lsc.SimpleSynchronize;
import org.lsc.beans.IBean;
import org.lsc.configuration.JaxbXmlConfigurationHelper;
import org.lsc.configuration.LdapConnectionType;
import org.lsc.configuration.LscConfiguration;
import org.lsc.exception.LscConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.jndi.JndiServices;
import org.lsc.jndi.SimpleJndiSrcService;
import org.lsc.service.IService;
import org.lsc.utils.directory.LDAP;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

public class Ldap2ExecutableLdifSyncTest extends TestCase {

    private static final String SOURCE_LDAP_CONNECTION = "executable-ldap-src-conn";

    private final String TASK_NAME = "ldap2executableLdifTestTask";
	private final String DN_ADD_SRC = "uid=0001,ou=ldap2executableLdifTestTaskSrc,ou=Test Data,dc=lsc-project,dc=org";
	private final String DN_ADD_DST = "uid=0001,ou=ldap2executableLdifTestTaskDst,ou=Test Data,dc=lsc-project,dc=org";

	private JndiServices srcJndiServices;

	@BeforeClass
	public void setUp() throws LscConfigurationException {
        LscConfiguration.loadFromInstance(new JaxbXmlConfigurationHelper().getConfiguration(this.getClass().getClassLoader().getResource("etc/" + JaxbXmlConfigurationHelper.LSC_CONF_XML).getPath(), System.getenv()));
        reloadJndiConnections();
	}

	private void reloadJndiConnections() {
		srcJndiServices = JndiServices.getInstance((LdapConnectionType)LscConfiguration.getConnection(SOURCE_LDAP_CONNECTION));
	}

	public final void testSyncLdap2Ldap() throws Exception {

		// make sure the contents of the directory are as we expect to begin with

		// check presence of source entry
		assertTrue(srcJndiServices.exists(DN_ADD_SRC));
		assertFalse(srcJndiServices.exists(DN_ADD_DST));
		checkAttributeValue(DN_ADD_SRC, "cn", "CN0001");
		checkAttributeValue(DN_ADD_SRC, "sn", "SN0001");
		checkAttributeValue(DN_ADD_SRC, "mail", "0001@domain.com");
		checkAttributeValue(DN_ADD_SRC, "uid", "0001");

		// perform the sync
		String outputFirstRun = launchSyncCleanTask(TASK_NAME, true, false);

		// check the results of the synchronization
		reloadJndiConnections();
		checkSyncResultsFirstPass();

		// sync again to confirm convergence
		String outputSecondRun = launchSyncCleanTask(TASK_NAME, true, false);
		// Ensure there is no modification detected
		assertTrue(outputSecondRun.contains("All entries: 1, to modify entries: 0, successfully modified entries: 0, errors: 0"));

		reloadJndiConnections();

	}

	private final void checkSyncResultsFirstPass() throws Exception {
		List<String> attributeValues = null;

		assertTrue(srcJndiServices.exists(DN_ADD_DST));
		checkAttributeValue(DN_ADD_DST, "cn", "CN0001");
		checkAttributeValue(DN_ADD_DST, "sn", "SN0001");
		checkAttributeValue(DN_ADD_DST, "mail", "ok@domain.net");
		checkAttributeValue(DN_ADD_DST, "uid", "0001");
	}

	private static String launchSyncCleanTask(String taskName, boolean doSync,
					boolean doClean) throws Exception {
		// initialize required stuff
		SimpleSynchronize sync = new SimpleSynchronize();
		List<String> syncType = new ArrayList<String>();
		List<String> cleanType = new ArrayList<String>();
		sync.setThreads(1);

                // Change system output stream to a variable
                PrintStream orgStream = System.out; // save original stream
                ByteArrayOutputStream lscOutput = new ByteArrayOutputStream();
                System.setOut(new PrintStream(lscOutput));

		if (doSync) {
			syncType.add(taskName);
		}

		if (doClean) {
			cleanType.add(taskName);
		}

		boolean ret = sync.launch(new ArrayList<String>(), syncType, cleanType);
		assertTrue(ret);

                // Restore standard output stream
                System.setOut(orgStream);

		return lscOutput.toString();
	}

	private void checkAttributeIsEmpty(String dn, String attributeName)
					throws NamingException {
		SearchResult sr = srcJndiServices.readEntry(dn, false);
		assertNull(sr.getAttributes().get(attributeName));
	}

	private void checkAttributeValue(String dn, String attributeName, String value) throws NamingException {
		SearchResult sr = srcJndiServices.readEntry(dn, false);
		Attribute at = sr.getAttributes().get(attributeName);
		assertNotNull(at);
		assertEquals(1, at.size());

		String realValue = (String) at.get();
		assertTrue(realValue.equals(value));
	}

	private String getAttributeFirstValue(String dn, String attributeName) throws NamingException {
		Map<String, LscDatasets> res = srcJndiServices.getAttrsList(dn, "(objectClass=*)", 0, Arrays.asList(new String[] {"*", "+"}));
		Map.Entry<String,LscDatasets> entry = res.entrySet().iterator().next();

		assertNotNull(entry);
		assertNotNull(entry.getValue());
		String val = entry.getValue().getStringValueAttribute(attributeName);
		assertNotNull(val);

		return val;
	}

	private void checkAttributeValues(String dn, String attributeName, List<String> expectedValues) throws NamingException {
		SearchResult sr = srcJndiServices.readEntry(dn, false);
		Attribute at = sr.getAttributes().get(attributeName);
		if (expectedValues.size() > 0) {
			assertNotNull(at);
		} else {
			if (at == null) {
				assertEquals(0, expectedValues.size());
				return;
			}
		}
		assertEquals(expectedValues.size(), at.size());

		// check that each value matches one on one
		for (String expectedValue : expectedValues) {
			assertTrue(at.contains(expectedValue));
		}
		for (int i = 0; i < at.size(); i++) {
			assertTrue(expectedValues.contains((String)at.get(i)));
		}
	}
}
