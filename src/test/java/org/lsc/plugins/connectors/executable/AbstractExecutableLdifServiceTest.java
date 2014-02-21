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
 *               (c) 2008 - 2011 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.plugins.connectors.executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.lsc.beans.IBean;

import junit.framework.TestCase;

public class AbstractExecutableLdifServiceTest extends TestCase {

	@Test
	public void testFromLdifOneEntry() throws Exception {
		TestExecutableLdifService executableLdifService = new TestExecutableLdifService();
		
		String ldif = "dn: uid=uniqueId, dc=do\n"
				+ " main\n"
				+ "singleAttribute: singleValue\n"
				+ "multiAttribute: firstValue\n"
				+ "multiAttribute: secondValue\n";
		Collection<IBean> entries = executableLdifService.fromLdif(ldif);
		
		assertEquals(1, entries.size());
		
		IBean entry = entries.iterator().next();
		
		assertEquals("uid=uniqueId, dc=domain", entry.getMainIdentifier());
		assertEquals("singleValue", entry.getDatasetFirstValueById("singleAttribute"));
		
		assertEquals(2, entry.getDatasetById("multiAttribute").size());
		String[] multiValues = { "firstValue", "secondValue" } ;
		HashSet<String> multiValuesSet = new HashSet<String>(Arrays.asList(multiValues));
		assertEquals(multiValuesSet, entry.getDatasetById("multiAttribute"));
	}
	
	private class TestExecutableLdifService extends AbstractExecutableLdifService {
		@SuppressWarnings("unchecked")
		public TestExecutableLdifService() throws Exception {
			beanClass = (Class<IBean>) Class.forName("org.lsc.beans.SimpleBean");
		}
	}

}
