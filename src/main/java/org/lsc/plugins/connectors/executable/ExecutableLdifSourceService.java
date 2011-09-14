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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.apache.commons.codec.binary.Base64;
import org.lsc.LscAttributes;
import org.lsc.beans.IBean;
import org.lsc.configuration.objects.Task;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.service.IService;
import org.lsc.utils.output.LdifLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a generic but configurable implementation to read data to
 * any referential which can be scripted
 * 
 * It just requires 2 scripts to :
 * <ul>
 * <li>list data</li>
 * <li>get a piece of data</li>
 * </ul>
 * 
 * @author Sebastien Bahloul &lt;seb@lsc-project.org&gt;
 */
public class ExecutableLdifSourceService implements IService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableLdifSourceService.class);

	private static final String DEBUG_PREFIX = "DEBUG: ";
	private static final String INFO_PREFIX = "INFO: ";
	private static final String WARN_PREFIX = "WARN: ";
	private static final String ERROR_PREFIX = "ERROR: ";
	private static final String VARS_PREFIX = "vars";

	private String listScript;
	private String getScript;
	private Class<IBean> beanClass;
	private Runtime rt;
	private Properties globalEnvironmentVariables;

	@SuppressWarnings("unchecked")
	@Deprecated
	public ExecutableLdifSourceService(Properties props, String beanClassName) throws LscServiceConfigurationException {
		rt = Runtime.getRuntime();
		try {
			globalEnvironmentVariables = org.lsc.Configuration.getPropertiesSubset(props, VARS_PREFIX);
			listScript = (String) props.get("listScript");
			getScript = (String) props.get("getScript");

			beanClass = (Class<IBean>) Class.forName(beanClassName);
		} catch (ClassNotFoundException e) {
			throw new LscServiceConfigurationException(e);
		}
	}

	public ExecutableLdifSourceService(Task task) throws LscServiceConfigurationException {
		this((ExecutableLdifSourceServiceConfiguration)task.getSourceService(), task.getBean());
	}

	@SuppressWarnings("unchecked")
	public ExecutableLdifSourceService(ExecutableLdifSourceServiceConfiguration elsc, String beanClassName) throws LscServiceConfigurationException {
		rt = Runtime.getRuntime();
		ExecutableLdifSourceServiceConnectionConfiguration elssc = (ExecutableLdifSourceServiceConnectionConfiguration)elsc.getConnection();

		listScript = elssc.getListScript();
		getScript = elssc.getGetScript();
		globalEnvironmentVariables = new Properties();
		globalEnvironmentVariables.putAll(elsc.getVars());
		try {
			beanClass = (Class<IBean>) Class.forName(beanClassName);
		} catch (ClassNotFoundException e) {
			throw new LscServiceConfigurationException(e);
		}
	}
	
	/**
	 * The simple object getter according to its identifier.
	 * 
	 * @param pivotName Name of the entry to be returned, which is the name returned by {@link #getListPivots()}
	 *            (used for display only)
	 * @param pivotAttributes Map of attribute names and values, which is the data identifier in the
	 *            source such as returned by {@link #getListPivots()}. It must identify a unique entry in the
	 *            source.
	 * @return The bean, or null if not found
	 * @throws LscServiceException May throw a {@link LscServiceException} if there is any error with LDIF conversion
	 */
	public IBean getBean(String pivotName, LscAttributes pivotAttributes, boolean fromSameService) throws LscServiceException {
		String output = executeWithReturn(getParameters(getScript, pivotName), getEnv(), toLdif(pivotAttributes));
		Collection<IBean> entries = fromLdif(output);
		if (entries.size() != 1) {
			LOGGER.error("Entries count: {}", entries.size());
			return null;
		}
		return entries.iterator().next();
	}

	/**
	 * Returns a list of all the objects' identifiers.
	 *
	 * @return Map of all entries names that are returned by the directory with an associated map of
	 *         attribute names and values (never null)
	 * @throws LscServiceException 
	 */
	public Map<String, LscAttributes> getListPivots() throws LscServiceException  {
		try {
			Map<String, LscAttributes> map = null;
			String output = executeWithReturn(getParameters(listScript), getEnv(), "");
			Collection<IBean> beans = fromLdif(output);
			if (beans != null) {
				map = new HashMap<String, LscAttributes>();
				for (IBean bean : beans) {
					LscAttributes attributes = new LscAttributes();
					for (String id : bean.getAttributesNames()) {
						Attribute attribute = bean.getAttributeById(id);
						//TODO: handle multi value attributes pivot
						attributes.getAttributes().put(id, attribute.get());
					}
					map.put(bean.getDistinguishedName(), attributes);
				}
			}
			return map;
		} catch (NamingException ne) {
			throw new LscServiceException("Error while converting LscAttributes to map: " + ne.toString(), ne);
		}
	}

	public int execute(String[] runtime, String[] env, String input) {
		StringBuffer datas = new StringBuffer();
		return execute(runtime, env, input, datas);
	}

	public String executeWithReturn(String[] runtime, String[] env, String input) {
		StringBuffer datas = new StringBuffer();
		execute(runtime, env, input, datas);
		return datas.toString();
	}

	private int execute(String[] runtime, String[] env, String input, StringBuffer datas) {
		StringBuffer messages = new StringBuffer();
		Process p = null;
		try {
			if (LOGGER.isDebugEnabled()) {
				StringBuilder parametersStr = new StringBuilder();
				for (String parameter : runtime) {
					parametersStr.append(parameter).append(" ");
				}
				LOGGER.debug("Lauching '{}'", parametersStr.toString());
			}
			p = rt.exec(runtime, env);

			LOGGER.debug("Writing to STDIN {}", input);

			OutputStream outputStream = p.getOutputStream();
			outputStream.write(input.getBytes());
			outputStream.flush();
			outputStream.close();

			//TODO: need to check for max time
			LOGGER.debug("Waiting for command to stop ... ");

			p.waitFor();
		} catch (IOException e) {
			// Encountered an error while reading data from output
			LOGGER.error("Encountered an I/O exception while writing data to script {}", runtime);
			LOGGER.debug(e.toString(), e);
		} catch (InterruptedException e) {
			// Encountered an interruption
			LOGGER.error("Script {} interrupted", runtime);
			LOGGER.debug(e.toString(), e);
		}

		byte[] data = new byte[65535];
		try {
			while (p.getInputStream() != null && p.getInputStream().read(data) > 0) {
				datas.append(new String(data));
			}
		} catch (IOException e) {
			// Failing to read the complete string causes null return
			LOGGER.error("Fail to read complete data from script output stream: {}", runtime);
			LOGGER.debug(e.toString(), e);
		}

		byte[] message = new byte[65535];
		try {
			while (p.getErrorStream().read(message) > 0) {
				messages.append(new String(message));
			}
		} catch (IOException e) {
			// Failing to read the complete string causes null return
			LOGGER.error("Fail to read complete messages from script stderr stream: {}", runtime);
			LOGGER.debug(e.toString(), e);
		}
		
		if (p.exitValue() != 0) {
			// A non zero value causes null return
			LOGGER.error("Non zero exit code for runtime: {}, exit code={}", runtime[0], p.exitValue());
			displayByLevel(messages.toString());
		} else {
			LOGGER.debug("Messages dump on stderr by script: ");
			displayByLevel(messages.toString());
		}
		return p.exitValue();
	}

	/**
	 * Parse returned messages to send them to the correct log level
	 * Messages must be prefixed by "DEBUG: ", "INFO: ", "WARN: ", "ERROR: "
	 * Default level is WARN. 
	 * @param messages the returned messages
	 */
	private void displayByLevel(String messages) {
		StringTokenizer lines = new StringTokenizer(messages, "\n");
		while (lines.hasMoreTokens()) {
			String line = lines.nextToken();
			String message = (line.contains(": ") ? line.substring(line.indexOf(": ") + 2) : line);
			if (line.startsWith(DEBUG_PREFIX)) {
				LOGGER.debug(message);
			} else if (line.startsWith(INFO_PREFIX)) {
				LOGGER.info(message);
			} else if (line.startsWith(WARN_PREFIX)) {
				LOGGER.warn(message);
			} else if (line.startsWith(ERROR_PREFIX)) {
				LOGGER.error(message);
			} else {
				// Default to WARN level
				LOGGER.warn(line);
			}
		}
	}

	protected String[] getEnv(String... args) {
		String[] envVars = new String[args.length + globalEnvironmentVariables.size()];
		int i = 0;
		for (String parameter : args) {
			envVars[i++] = parameter;
		}
		for (Object parameterName : globalEnvironmentVariables.keySet()) {
			envVars[i++] = (String) parameterName + "=" + (String) globalEnvironmentVariables.get(parameterName);
		}
		return envVars;
	}

	protected String[] getParameters(String... args) {
		String[] parameters = new String[args.length];
		int i = 0;
		for (String parameter : args) {
			parameters[i++] = parameter;
		}
		return parameters;
	}

	private Collection<IBean> fromLdif(String output) {
		ArrayList<IBean> beans = new ArrayList<IBean>();
		try {
			IBean bean = null;
			StringTokenizer sTok = new StringTokenizer(output, "\n", true);
			while (sTok.hasMoreTokens()) {
				bean = beanClass.newInstance();
				String entryStr = "";
				while (sTok.hasMoreTokens()) {
					String line = sTok.nextToken();
					entryStr += line;
					if (entryStr.endsWith("\n\n")) {
						break;
					}
				}
				if (entryStr.trim().length() > 0) {
					updateBean(bean, entryStr);
					beans.add(bean);
				}
			}
		} catch (InstantiationException e) {
			LOGGER.error("Bean class name: {}", beanClass.getName());
			LOGGER.debug(e.toString(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Bean class name: {}", beanClass.getName());
			LOGGER.debug(e.toString(), e);
		}

		return beans;
	}

	private void updateBean(IBean bean, String entryStr) {
		StringTokenizer sTok = new StringTokenizer(entryStr, "\n");
		String multiLineValue = null;
		String multiLineAttribute = null;
		boolean base64 = false;
		while (sTok.hasMoreTokens()) {
			String line = sTok.nextToken();
			if (multiLineValue != null && !line.startsWith(" ")) {
				// End of multi line value
				if (base64) {
					String attributeValue = new String(Base64.decodeBase64(multiLineValue.getBytes()));
					updateBeanAttributeValue(bean, multiLineAttribute, attributeValue);
				} else {
					updateBeanAttributeValue(bean, multiLineAttribute, multiLineValue);
				}
				multiLineValue = null;
			}
			if (multiLineValue != null && line.startsWith(" ")) {
				multiLineValue += line.substring(1);
			} else if (line.contains(":: ")) {
				multiLineAttribute = line.substring(0, line.indexOf(":"));
				multiLineValue = line.substring(line.indexOf(":: ") + 3);
				base64 = true;
			} else if (line.contains(": ")) {
				multiLineAttribute = line.substring(0, line.indexOf(":"));
				multiLineValue = line.substring(line.indexOf(": ") + 2);
				base64 = false;
			} else if (line.trim().length() == 0) {
				break;
			} else {
				// TODO
				LOGGER.error("Got something strange : '{}'. Please consider checking as this data may be either an incorrect format or an error !", line);
			}
		}
		if (multiLineValue != null) {
			if (base64) {
				String attributeValue = new String(Base64.decodeBase64(multiLineValue.getBytes()));
				updateBeanAttributeValue(bean, multiLineAttribute, attributeValue);
			} else {
				updateBeanAttributeValue(bean, multiLineAttribute, multiLineValue);
			}
		}
	}

	private void updateBeanAttributeValue(IBean bean,
					String attributeName, String attributeValue) {
		if (attributeName.equals("dn")) {
			bean.setDistinguishedName(attributeValue);
		} else {
			if (bean.getAttributeById(attributeName) != null) {
				Attribute attr = bean.getAttributeById(attributeName);
				attr.add(attributeValue);
				bean.setAttribute(attr);
			} else {
				bean.setAttribute(new BasicAttribute(attributeName, attributeValue));
			}
			if (bean.getDistinguishedName() == null) {
				bean.setDistinguishedName(attributeValue);
			}
		}
	}

	private String toLdif(LscAttributes attributes) throws LscServiceException {
		StringBuilder sb = new StringBuilder();
		for (String attributeName : attributes.getAttributes().keySet()) {
			try {
				LdifLayout.printAttributeToStringBuffer(sb, attributeName, (List<Object>) attributes.getListValueAttribute(attributeName));
			} catch (NamingException e) {
				throw new LscServiceException("Error while converting LscAttributes to LDIF: " + e.toString(), e);
			}
		}
		return sb.toString();
	}
}
