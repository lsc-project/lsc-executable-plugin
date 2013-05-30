package org.lsc.plugins.connectors.executable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.lsc.LscDatasets;
import org.lsc.beans.IBean;
import org.lsc.configuration.ConnectionType;
import org.lsc.exception.LscServiceException;
import org.lsc.service.IService;
import org.lsc.utils.output.LdifLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractExecutableLdifService implements IService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExecutableLdifService.class);

    private static final String DEBUG_PREFIX = "DEBUG: ";
    private static final String INFO_PREFIX = "INFO: ";
    private static final String WARN_PREFIX = "WARN: ";
    private static final String ERROR_PREFIX = "ERROR: ";

    protected String listScript;
    protected String getScript;
    protected Class<IBean> beanClass;
    protected static Properties globalEnvironmentVariables;
    
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
    public IBean getBean(String pivotName, LscDatasets pivotAttributes, boolean fromSameService) throws LscServiceException {
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
    public Map<String, LscDatasets> getListPivots() throws LscServiceException  {
        Map<String, LscDatasets> map = null;
        String output = executeWithReturn(getParameters(listScript), getEnv(), "");
        Collection<IBean> beans = fromLdif(output);
        if (beans != null) {
            map = new HashMap<String, LscDatasets>();
            for (IBean bean : beans) {
                map.put(bean.getMainIdentifier(), bean.datasets());
            }
        }
        return map;
    }

    public static int execute(String[] runtime, String[] env, String input) {
        StringBuffer datas = new StringBuffer();
        return execute(runtime, env, input, datas);
    }

    public static String executeWithReturn(String[] runtime, String[] env, String input) {
        StringBuffer datas = new StringBuffer();
        execute(runtime, env, input, datas);
        return datas.toString();
    }

    private static int execute(String[] runtime, String[] env, String input, StringBuffer datas) {
        StringBuffer messages = new StringBuffer();
        Process p = null;
        Runtime rt = Runtime.getRuntime();
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
    private static void displayByLevel(String messages) {
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

    protected static String[] getEnv(String... args) {
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

    public static String[] getParameters(String... args) {
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
            bean.setMainIdentifier(attributeValue);
        } else {
            if (bean.getDatasetById(attributeName) != null) {
                bean.getDatasetById(attributeName).add(attributeValue);
            } else {
                bean.datasets().put(attributeName, Collections.singleton(attributeValue));
            }
            if (bean.getMainIdentifier() == null) {
                bean.setMainIdentifier(attributeValue);
            }
        }
    }

    private String toLdif(LscDatasets attributes) throws LscServiceException {
        StringBuilder sb = new StringBuilder();
        for (String attributeName : attributes.getDatasets().keySet()) {
            try {
                LdifLayout.printAttributeToStringBuffer(sb, attributeName, (List<Object>) attributes.getListValueAttribute(attributeName));
            } catch (NamingException e) {
                throw new LscServiceException("Error while converting LscAttributes to LDIF: " + e.toString(), e);
            }
        }
        return sb.toString();
    }

    @Override
    public Collection<Class<? extends ConnectionType>> getSupportedConnectionType() {
        Collection<Class<? extends ConnectionType>> list = new ArrayList<Class<? extends ConnectionType>>();
        return list;
    }
}
