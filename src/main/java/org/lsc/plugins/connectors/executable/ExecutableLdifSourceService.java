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

import java.util.Properties;

import org.lsc.beans.IBean;
import org.lsc.configuration.KeysValuesMap.Entry;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.plugins.connectors.executable.generated.ExecutableLdifSourceServiceSettings;

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
public class ExecutableLdifSourceService extends AbstractExecutableLdifService {

	@SuppressWarnings("unchecked")
    public ExecutableLdifSourceService(final TaskType task) throws LscServiceConfigurationException {
        try {
            if (task.getPluginSourceService().getAny() == null || task.getPluginSourceService().getAny().size() != 1 || !(task.getPluginSourceService().getAny().get(0) instanceof ExecutableLdifSourceServiceSettings)) {
                throw new LscServiceConfigurationException("Unable to identify the executable LDIF source service configuration " + "inside the plugin source node of the task: " + task.getName());
            }
            ExecutableLdifSourceServiceSettings serviceSettings = (ExecutableLdifSourceServiceSettings) task.getPluginSourceService().getAny().get(0);
            
            listScript = serviceSettings.getListScript();
            getScript = serviceSettings.getGetScript();
            
            globalEnvironmentVariables = new Properties();
            if(serviceSettings.getVariables() != null) {
                for(Entry entry : serviceSettings.getVariables().getEntry()) {
                    globalEnvironmentVariables.put(entry.getKey(), entry.getValue());
                }
            }
            interpretor = serviceSettings.getInterpretor();
            
            beanClass = (Class<IBean>) Class.forName(task.getBean());
        } catch (ClassNotFoundException e) {
            throw new LscServiceConfigurationException(e);
        }
	}
}
