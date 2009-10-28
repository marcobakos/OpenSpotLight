/*
 * OpenSpotLight - Open Source IT Governance Platform
 *  
 * Copyright (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA 
 * or third-party contributors as indicated by the @author tags or express 
 * copyright attribution statements applied by the authors.  All third-party 
 * contributions are distributed under license by CARAVELATECH CONSULTORIA E 
 * TECNOLOGIA EM INFORMATICA LTDA. 
 * 
 * This copyrighted material is made available to anyone wishing to use, modify, 
 * copy, or redistribute it subject to the terms and conditions of the GNU 
 * Lesser General Public License, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License  for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this distribution; if not, write to: 
 * Free Software Foundation, Inc. 
 * 51 Franklin Street, Fifth Floor 
 * Boston, MA  02110-1301  USA 
 * 
 *********************************************************************** 
 * OpenSpotLight - Plataforma de Governan�a de TI de C�digo Aberto 
 *
 * Direitos Autorais Reservados (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA 
 * EM INFORMATICA LTDA ou como contribuidores terceiros indicados pela etiqueta 
 * @author ou por expressa atribui��o de direito autoral declarada e atribu�da pelo autor.
 * Todas as contribui��es de terceiros est�o distribu�das sob licen�a da
 * CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA. 
 * 
 * Este programa � software livre; voc� pode redistribu�-lo e/ou modific�-lo sob os 
 * termos da Licen�a P�blica Geral Menor do GNU conforme publicada pela Free Software 
 * Foundation. 
 * 
 * Este programa � distribu�do na expectativa de que seja �til, por�m, SEM NENHUMA 
 * GARANTIA; nem mesmo a garantia impl�cita de COMERCIABILIDADE OU ADEQUA��O A UMA
 * FINALIDADE ESPEC�FICA. Consulte a Licen�a P�blica Geral Menor do GNU para mais detalhes.  
 * 
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral Menor do GNU junto com este
 * programa; se n�o, escreva para: 
 * Free Software Foundation, Inc. 
 * 51 Franklin Street, Fifth Floor 
 * Boston, MA  02110-1301  USA
 */

package org.openspotlight.federation.data.load;

import static java.text.MessageFormat.format;
import static java.util.Arrays.sort;
import static org.openspotlight.common.util.Arrays.andValues;
import static org.openspotlight.common.util.Arrays.map;
import static org.openspotlight.common.util.Arrays.ofKeys;
import static org.openspotlight.common.util.Assertions.checkCondition;
import static org.openspotlight.common.util.Assertions.checkNotEmpty;
import static org.openspotlight.common.util.Assertions.checkNotNull;
import static org.openspotlight.common.util.Conversion.convert;
import static org.openspotlight.common.util.Dates.dateFromString;
import static org.openspotlight.common.util.Dates.stringFromDate;
import static org.openspotlight.common.util.Exceptions.logAndReturn;
import static org.openspotlight.common.util.Exceptions.logAndReturnNew;
import static org.openspotlight.common.util.Exceptions.logAndThrow;
import static org.openspotlight.common.util.Exceptions.logAndThrowNew;
import static org.openspotlight.common.util.Serialization.readFromBase64;
import static org.openspotlight.common.util.Serialization.serializeToBase64;
import static org.openspotlight.federation.data.util.ConfigurationNodes.findAllNodesOfType;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;

import org.openspotlight.common.LazyType;
import org.openspotlight.common.SharedConstants;
import org.openspotlight.common.exception.ConfigurationException;
import org.openspotlight.federation.data.ConfigurationNode;
import org.openspotlight.federation.data.NoConfigurationYetException;
import org.openspotlight.federation.data.StaticMetadata;
import org.openspotlight.federation.data.InstanceMetadata.DataLoader;
import org.openspotlight.federation.data.InstanceMetadata.ItemChangeEvent;
import org.openspotlight.federation.data.InstanceMetadata.ItemChangeType;
import org.openspotlight.federation.data.impl.Artifact;
import org.openspotlight.federation.data.impl.Configuration;
import org.openspotlight.federation.data.util.ParentNumberComparator;
import org.openspotlight.jcr.provider.CommonJcrSupport;

/**
 * Configuration manager that stores and loads the configuration from a JcrRepository. LATER_TASK implement node property
 * 
 * @author Luiz Fernando Teston - feu.teston@caravelatech.com
 */
public class JcrSessionConfigurationManager implements ConfigurationManager {

    /**
     * Class to control the Lazy Loading for {@link JcrSessionConfigurationManager} when the loading is {@link LazyType#LAZY}.
     * 
     * @author Luiz Fernando Teston - feu.teston@caravelatech.com
     */
    private static class JcrDataLoader implements DataLoader {

        /**
         * Just a listener to create the load commands on demand.
         * 
         * @author feu
         */
        private static class CacheLoadingListener implements LoadingListener {

            /** The node cache. */
            private final Map<ConfigurationNode, LoadCommand> nodeCache;

            /**
             * Instantiates a new cache loading listener.
             * 
             * @param nodeCache the node cache
             */
            public CacheLoadingListener(
                                         final Map<ConfigurationNode, LoadCommand> nodeCache ) {
                this.nodeCache = nodeCache;
            }

            /* (non-Javadoc)
             * @see org.openspotlight.federation.data.load.JcrSessionConfigurationManager.LoadingListener#loading(org.openspotlight.federation.data.ConfigurationNode, javax.jcr.Node)
             */
            public void loading( final ConfigurationNode configurationNode,
                                 final Node jcrNode ) {
                if (!this.nodeCache.containsKey(configurationNode)) {
                    this.nodeCache.put(configurationNode, new LoadCommand(configurationNode, jcrNode, this, this.nodeCache));
                }

            }
        }

        /**
         * This class should be a 'lazy command'. When it needs to load children or properties, it just load. If it's not needed,
         * it does nothing.
         * 
         * @author feu
         */
        private static class LoadCommand {

            /** The loading children. */
            private boolean                           loadingChildren   = false;

            /** The loading properties. */
            private boolean                           loadingProperties = false;

            /** The configuration node. */
            private final ConfigurationNode           configurationNode;

            /** The jcr node. */
            private final Node                        jcrNode;

            /** The properties loaded. */
            private boolean                           propertiesLoaded  = false;

            /** The children loaded. */
            private boolean                           childrenLoaded    = false;

            /** The listener. */
            private final LoadingListener             listener;

            /** The node cache. */
            final Map<ConfigurationNode, LoadCommand> nodeCache;

            /**
             * Instantiates a new load command.
             * 
             * @param configurationNode the configuration node
             * @param jcrNode the jcr node
             * @param listener the listener
             * @param nodeCache the node cache
             */
            LoadCommand(
                         final ConfigurationNode configurationNode, final Node jcrNode, final LoadingListener listener,
                         final Map<ConfigurationNode, LoadCommand> nodeCache ) {
                super();
                this.configurationNode = configurationNode;
                this.jcrNode = jcrNode;
                this.listener = listener;
                this.nodeCache = nodeCache;

            }

            /**
             * Internal load properties.
             * 
             * @throws RepositoryException the repository exception
             * @throws Exception the exception
             */
            private void internalLoadProperties() throws RepositoryException, Exception {
                final String[] propKeys = this.configurationNode.getInstanceMetadata().getStaticMetadata().propertyNames();
                final Class<?>[] propValues = this.configurationNode.getInstanceMetadata().getStaticMetadata().propertyTypes();
                final String keyPropertyName = this.configurationNode.getInstanceMetadata().getStaticMetadata().keyPropertyName();
                final Map<String, Class<?>> propertyTypes = map(ofKeys(propKeys), andValues(propValues));
                loadNodeProperties(this.jcrNode, this.configurationNode, propertyTypes, keyPropertyName);
            }

            /**
             * Load children.
             * 
             * @throws Exception the exception
             */
            public void loadChildren() throws Exception {
                if (this.childrenLoaded || this.loadingChildren) {
                    return;
                }
                this.loadingChildren = true;
                this.loadParent();
                loadChildrenNodes(this.jcrNode, this.configurationNode, LazyType.LAZY, this.listener);

                this.loadingChildren = false;
                this.childrenLoaded = true;
            }

            /**
             * Load parent.
             * 
             * @throws Exception the exception
             */
            private void loadParent() throws Exception {
                final ConfigurationNode parent = this.configurationNode.getInstanceMetadata().getDefaultParent();

                if (parent != null) {
                    final LoadCommand parentCommand = this.nodeCache.get(parent);
                    parentCommand.loadChildren();
                }
            }

            /**
             * Load properties.
             * 
             * @throws Exception the exception
             */
            public void loadProperties() throws Exception {
                if (this.propertiesLoaded || this.loadingProperties) {
                    return;
                }
                this.loadingProperties = true;
                this.loadParent();

                this.internalLoadProperties();
                this.loadingProperties = false;

                this.propertiesLoaded = true;
            }

        }

        /** The session. */
        private final Session                             session;

        /** The node cache. */
        private final Map<ConfigurationNode, LoadCommand> nodeCache = new HashMap<ConfigurationNode, LoadCommand>();

        /** The listener. */
        private final LoadingListener                     listener;

        /**
         * Constructor to initialize the lazy loading for this two corresponding root nodes.
         * 
         * @param jcrRootNode the jcr root node
         * @param configurationRootNode the configuration root node
         * @param session the session
         * @throws RepositoryException the repository exception
         */
        public JcrDataLoader(
                              final Configuration configurationRootNode, final Node jcrRootNode, final Session session )
            throws RepositoryException {
            this.listener = new CacheLoadingListener(this.nodeCache);
            this.nodeCache.put(configurationRootNode, new LoadCommand(configurationRootNode, jcrRootNode, this.listener,
                                                                      this.nodeCache));
            this.session = session;

        }

        /**
         * Fill command cache.
         * 
         * @param configurationNode the configuration node
         * @throws Exception the exception
         */
        private void fillCommandCache( final ConfigurationNode configurationNode ) throws Exception {
            final List<ConfigurationNode> allParentNodes = new LinkedList<ConfigurationNode>();
            allParentNodes.add(configurationNode);
            ConfigurationNode parent = configurationNode.getInstanceMetadata().getDefaultParent();
            while (!this.nodeCache.containsKey(parent)) {
                allParentNodes.add(parent);
                parent = configurationNode.getInstanceMetadata().getDefaultParent();
            }
            Collections.reverse(allParentNodes);
            for (final ConfigurationNode node : allParentNodes) {
                final String xpath = XpathSupport.getCompleteXpathFor(node);
                final Node jcrNode = JcrSupport.findUnique(this.session, xpath);
                this.nodeCache.put(node, new LoadCommand(node, jcrNode, this.listener, this.nodeCache));
            }
        }

        /**
         * Gets the load command for.
         * 
         * @param configurationNode the configuration node
         * @return the load command for
         * @throws Exception the exception
         */
        private LoadCommand getLoadCommandFor( final ConfigurationNode configurationNode ) throws Exception {
            if (this.nodeCache.containsKey(configurationNode)) {
                return this.nodeCache.get(configurationNode);
            }
            this.fillCommandCache(configurationNode);
            return this.nodeCache.get(configurationNode);

        }

        /* (non-Javadoc)
         * @see org.openspotlight.federation.data.InstanceMetadata.DataLoader#loadChildren(org.openspotlight.federation.data.ConfigurationNode)
         */
        public void loadChildren( final ConfigurationNode targetNode ) {
            try {
                this.getLoadCommandFor(targetNode).loadChildren();
            } catch (final Exception e) {
                throw logAndReturnNew(e, ConfigurationException.class);
            }
        }

        /* (non-Javadoc)
         * @see org.openspotlight.federation.data.InstanceMetadata.DataLoader#loadProperties(org.openspotlight.federation.data.ConfigurationNode)
         */
        public void loadProperties( final ConfigurationNode targetNode ) {
            try {
                this.getLoadCommandFor(targetNode).loadProperties();
            } catch (final Exception e) {
                throw logAndReturnNew(e, ConfigurationException.class);
            }

        }

    }

    /**
     * Class with some helper methods to use on Jcr stuff.
     * 
     * @author Luiz Fernando Teston - feu.teston@caravelatech.com
     */
    private static class JcrSupport {

        /**
         * Find all nodes by using a xpath query.
         * 
         * @param session the session
         * @param xpath the xpath
         * @return a node iterator
         * @throws Exception the exception
         */
        public static NodeIterator findAll( final Session session,
                                            final String xpath ) throws Exception {
            final Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
            final QueryResult result = query.execute();
            final NodeIterator nodes = result.getNodes();
            return nodes;
        }

        /**
         * Find a node using a xpath query.
         * 
         * @param session the session
         * @param xpath the xpath
         * @return null when no items found, or the jcr item instead
         * @throws Exception if more than one item was found, or if anything wrong happened
         */
        public static Node findUnique( final Session session,
                                       final String xpath ) throws Exception {
            final Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
            final QueryResult result = query.execute();
            final NodeIterator nodes = result.getNodes();
            Node foundNode = null;
            if (nodes.hasNext()) {
                foundNode = nodes.nextNode();
            } else {
                return null;
            }
            if (nodes.hasNext()) {
                logAndThrow(new IllegalStateException("XPath with more than one result: " + xpath)); //$NON-NLS-1$
            }
            return foundNode;
        }
    }

    /**
     * The listener interface for receiving loading events. The class that is interested in processing a loading event implements
     * this interface, and the object created with that class is registered with a component using the component's
     * <code>addLoadingListener<code> method. When
     * the loading event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see LoadingEvent
     */
    private static interface LoadingListener {

        /**
         * Loading.
         * 
         * @param configurationNode the configuration node
         * @param jcrNode the jcr node
         */
        public void loading( ConfigurationNode configurationNode,
                             Node jcrNode );
    }

    /**
     * This helper class can create xpath to find the {@link Node Jcr Node} corresponding to a unique {@link ConfigurationNode}.
     * 
     * @author Luiz Fernando Teston - feu.teston@caravelatech.com
     */
    private static class XpathSupport {

        /**
         * Fill the string buffer at the beginning using the node data.
         * 
         * @param xpath the xpath
         * @param node the node
         * @throws Exception on conversion errors
         */
        private static void fillXpathFromNode( final StringBuilder xpath,
                                               final ConfigurationNode node ) throws Exception {
            final String nodePath = classHelper.getNameFromNodeClass(node.getClass());
            final StaticMetadata metadata = node.getInstanceMetadata().getStaticMetadata();
            final String keyPropertyName = metadata.keyPropertyName();
            final String keyPropertyValueAsString = convert(node.getInstanceMetadata().getKeyPropertyValue(), String.class);
            if ("".equals(keyPropertyName) || keyPropertyName == null) { //$NON-NLS-1$
                xpath.insert(0, format("/{0}", nodePath));//$NON-NLS-1$
            } else {
                xpath.insert(0, format("/{0}[@osl:{1}=''{2}'']", //$NON-NLS-1$
                                       nodePath, keyPropertyName, keyPropertyValueAsString));
            }

        }

        /**
         * Gets the complete xpath for.
         * 
         * @param node the node
         * @return a complete filled xpath based on node data and also its parent's data.
         * @throws Exception the exception
         */
        static String getCompleteXpathFor( final ConfigurationNode node ) throws Exception {
            ConfigurationNode c = node;
            final StringBuilder xpath = new StringBuilder();
            while (c != null) {
                fillXpathFromNode(xpath, c);
                c = c.getInstanceMetadata().getDefaultParent();
            }
            xpath.insert(0, '/');
            return xpath.toString();

        }
    }

    /** The Constant classHelper. */
    static final NodeClassHelper             classHelper    = new NodeClassHelper();

    /** The Constant NS_DESCRIPTION. */
    private static final String              NS_DESCRIPTION = "www.openspotlight.org";  //$NON-NLS-1$

    /** The Constant propertyHelper. */
    private static final PropertyEntryHelper propertyHelper = new PropertyEntryHelper();

    /**
     * Reads an property on jcr node.
     * 
     * @param jcrNode the jcr node
     * @param propertyName the property name
     * @param propertyClass the property class
     * @return the property
     * @throws Exception the exception
     */
    @SuppressWarnings( {"boxing", "unchecked"} )
    private static <T> T getProperty( final Node jcrNode,
                                      final String propertyName,
                                      final Class<?> propertyClass ) throws Exception {
        Property jcrProperty = null;
        Object value = null;
        try {
            jcrProperty = jcrNode.getProperty(propertyName);
        } catch (final Exception e) {
            throw logAndReturn(e);
        }
        if (Boolean.class.equals(propertyClass)) {
            value = jcrProperty.getBoolean();
        } else if (Calendar.class.equals(propertyClass)) {
            value = jcrProperty.getDate();
        } else if (Double.class.equals(propertyClass)) {
            value = jcrProperty.getDouble();
        } else if (Long.class.equals(propertyClass)) {
            value = jcrProperty.getLong();
        } else if (String.class.equals(propertyClass)) {
            value = jcrProperty.getString();
        } else if (Integer.class.equals(propertyClass)) {
            value = (int)jcrProperty.getLong();
        } else if (Byte.class.equals(propertyClass)) {
            value = (byte)jcrProperty.getLong();
        } else if (Float.class.equals(propertyClass)) {
            value = (float)jcrProperty.getDouble();
        } else if (Date.class.equals(propertyClass)) {
            if (jcrProperty.getString() != null) {
                value = dateFromString(jcrProperty.getString());
            }
        } else if (propertyClass.isEnum()) {
            final String propertyAsString = jcrProperty.getString();
            if (propertyAsString == null) {
                return null;
            }
            final Field[] flds = propertyClass.getDeclaredFields();
            for (final Field f : flds) {
                if (f.isEnumConstant()) {
                    if (f.getName().equals(propertyAsString)) {
                        value = f.get(null);
                        break;
                    }
                }
            }
        } else if (InputStream.class.isAssignableFrom(propertyClass)) {
            value = jcrProperty.getStream();
        } else if (Serializable.class.isAssignableFrom(propertyClass)) {
            final String valueAsString = jcrProperty.getString();
            if (valueAsString != null) {
                value = readFromBase64(valueAsString);
            }
        } else {
            throw new IllegalStateException(format("Invalid class for property {0} : {1}", propertyName, //$NON-NLS-1$
                                                   propertyClass));
        }

        return (T)value;
    }

    /**
     * Loads the newly created node and also it's properties and it's children.
     * 
     * @param jcrNode the jcr node
     * @param configurationNode the configuration node
     * @param staticMetadata the static metadata
     * @throws Exception the exception
     */
    private static void loadChildrenAndProperties( final Node jcrNode,
                                                   final ConfigurationNode configurationNode,
                                                   final StaticMetadata staticMetadata ) throws Exception {
        final String[] propKeys = configurationNode.getInstanceMetadata().getStaticMetadata().propertyNames();
        final Class<?>[] propValues = configurationNode.getInstanceMetadata().getStaticMetadata().propertyTypes();
        final Map<String, Class<?>> propertyTypes = map(ofKeys(propKeys), andValues(propValues));
        loadNodeProperties(jcrNode, configurationNode, propertyTypes, staticMetadata.keyPropertyName());
        loadChildrenNodes(jcrNode, configurationNode, LazyType.EAGER, null);
    }

    /**
     * Load children nodes.
     * 
     * @param jcrNode the jcr node
     * @param parentNode the parent node
     * @param lazyType the lazy type
     * @param listener the listener
     * @throws PathNotFoundException the path not found exception
     * @throws RepositoryException the repository exception
     * @throws ConfigurationException the configuration exception
     * @throws Exception the exception
     */
    static void loadChildrenNodes( final Node jcrNode,
                                   final ConfigurationNode parentNode,
                                   final LazyType lazyType,
                                   final LoadingListener listener )
        throws PathNotFoundException, RepositoryException, ConfigurationException, Exception {
        NodeIterator children;
        try {
            children = jcrNode.getNodes();
        } catch (final PathNotFoundException e) {
            children = null;
            // Thats ok, just didn't find any nodes
        }
        if (children == null) {
            return;
        }
        while (children.hasNext()) {
            final Node jcrChild = children.nextNode();
            if (!jcrChild.getName().startsWith("osl:")) { //$NON-NLS-1$
                continue;
            }
            final Class<ConfigurationNode> nodeClass = classHelper.getNodeClassFromName(jcrChild.getName());
            final StaticMetadata staticMetadata = classHelper.getStaticMetadataFromClass(nodeClass);
            final ConfigurationNode newNode = loadOneChild(parentNode, jcrChild, staticMetadata, nodeClass);
            if (listener != null) {
                listener.loading(newNode, jcrChild);
            }
            if (LazyType.EAGER.equals(lazyType)) {
                loadChildrenAndProperties(jcrChild, newNode, staticMetadata);
            }

        }

    }

    /**
     * Load node properties.
     * 
     * @param jcrNode the jcr node
     * @param configurationNode the configuration node
     * @param propertyTypes the property types
     * @param keyPropertyName the key property name
     * @throws RepositoryException the repository exception
     * @throws ConfigurationException the configuration exception
     * @throws Exception the exception
     */
    static void loadNodeProperties( final Node jcrNode,
                                    final ConfigurationNode configurationNode,
                                    final Map<String, Class<?>> propertyTypes,
                                    final String keyPropertyName ) throws RepositoryException, ConfigurationException, Exception {
        configurationNode.getInstanceMetadata().setSavedUniqueId(jcrNode.getUUID());
        for (final Map.Entry<String, Class<?>> entry : propertyTypes.entrySet()) {
            if (keyPropertyName != null && keyPropertyName.equals(entry.getKey())) {
                continue;
            }
            if (entry.getKey().length() == 0) {
                continue;
            }
            try {
                jcrNode.getProperty(DEFAULT_OSL_PREFIX + ":" + entry.getKey());
            } catch (final PathNotFoundException e) {
                continue;
                //that's okay. There's no such property
            }
            if (Serializable.class.isAssignableFrom(entry.getValue())) {
                final Serializable value = (Serializable)getProperty(jcrNode, DEFAULT_OSL_PREFIX + ":" + entry.getKey(),
                                                                     entry.getValue());
                configurationNode.getInstanceMetadata().setPropertyIgnoringListener(entry.getKey(), value);
            } else if (InputStream.class.isAssignableFrom(entry.getValue())) {
                final InputStream value = (InputStream)getProperty(jcrNode, DEFAULT_OSL_PREFIX + ":" + entry.getKey(),
                                                                   entry.getValue());
                configurationNode.getInstanceMetadata().setStreamProperty(entry.getKey(), value);
            }

        }
    }

    /**
     * Load one child.
     * 
     * @param parentNode the parent node
     * @param jcrChild the jcr child
     * @param staticMetadataForChild the static metadata for child
     * @param childNodeClass the child node class
     * @return the configuration node
     * @throws Exception the exception
     */
    private static ConfigurationNode loadOneChild( final ConfigurationNode parentNode,
                                                   final Node jcrChild,
                                                   final StaticMetadata staticMetadataForChild,
                                                   final Class<? extends ConfigurationNode> childNodeClass ) throws Exception {
        final Serializable keyValue = getProperty(jcrChild, "osl:" //$NON-NLS-1$
                                                            + staticMetadataForChild.keyPropertyName(),
                                                  staticMetadataForChild.keyPropertyType());
        final String childNodeClassName = jcrChild.getName();
        final ConfigurationNode newNode = classHelper.createInstance(keyValue, parentNode, childNodeClassName);
        if (Artifact.class.isAssignableFrom(childNodeClass)) {
            newNode.getInstanceMetadata().setPropertyIgnoringListener(Artifact.KeyProperties.UUID.toString(), jcrChild.getUUID());
        }
        newNode.getInstanceMetadata().setSavedUniqueId(jcrChild.getUUID());
        return newNode;
    }

    /** JCR session. */
    private final Session session;

    /**
     * Constructor. It's mandatory that the session is valid during object liveness.
     * 
     * @param session valid session
     * @throws ConfigurationException the configuration exception
     */
    public JcrSessionConfigurationManager(
                                           final Session session ) throws ConfigurationException {
        checkNotNull("session", session); //$NON-NLS-1$
        checkCondition("session", session.isLive()); //$NON-NLS-1$
        this.session = session;
        this.initDataInsideSession();
    }

    /* (non-Javadoc)
     * @see org.openspotlight.federation.data.load.ConfigurationManager#closeResources()
     */
    public void closeResources() {
        this.session.logout();
    }

    /**
     * Creates the.
     * 
     * @param parentNode the parent node
     * @param nodePath the node path
     * @param keyPropertyName the key property name
     * @param keyPropertyValue the key property value
     * @param keyPropertyType the key property type
     * @return the node
     * @throws Exception the exception
     */
    private Node create( final Node parentNode,
                         final String nodePath,
                         final String keyPropertyName,
                         final Serializable keyPropertyValue,
                         final Class<? extends Serializable> keyPropertyType ) throws Exception {
        checkNotNull("parentNode", parentNode); //$NON-NLS-1$
        checkNotEmpty("nodePath", nodePath); //$NON-NLS-1$
        final Node newNode = parentNode.addNode(nodePath);
        if (keyPropertyName != null && !"".equals(keyPropertyName)) { //$NON-NLS-1$
            this.setProperty(newNode, "osl:" + keyPropertyName, //$NON-NLS-1$
                             keyPropertyType, keyPropertyValue);
        }
        newNode.addMixin("mix:referenceable"); //$NON-NLS-1$
        if (nodePath.equals("osl:configuration")) { //$NON-NLS-1$
            newNode.addMixin("mix:versionable"); //$NON-NLS-1$
        }
        return newNode;

    }

    /**
     * Method to create nodes on jcr only when necessary.
     * 
     * @param parentJcrNode the parent jcr node
     * @param currentNode the current node
     * @param nodePath the node path
     * @param keyPropertyName the key property name
     * @param keyPropertyValue the key property value
     * @param keyPropertyType the key property type
     * @return the node
     * @throws ConfigurationException the configuration exception
     */
    private Node createIfDontExists( final Node parentJcrNode,
                                     final ConfigurationNode currentNode,
                                     final String nodePath,
                                     final String keyPropertyName,
                                     final Serializable keyPropertyValue,
                                     final Class<? extends Serializable> keyPropertyType ) throws ConfigurationException {
        checkNotNull("parentJcrNode", parentJcrNode); //$NON-NLS-1$
        checkNotEmpty("nodePath", nodePath); //$NON-NLS-1$
        try {
            try {
                final String xpath = XpathSupport.getCompleteXpathFor(currentNode);
                Node foundNode = JcrSupport.findUnique(this.session, xpath);
                if (foundNode == null) {
                    foundNode = this.create(parentJcrNode, nodePath, keyPropertyName, keyPropertyValue, keyPropertyType);

                }
                return foundNode;
            } catch (final PathNotFoundException e) {
                final Node newNode = this.create(parentJcrNode, nodePath, keyPropertyName, keyPropertyValue, keyPropertyType);

                return newNode;
            }

        } catch (final Exception e) {
            throw logAndReturnNew(e, ConfigurationException.class);
        }
    }

    /**
     * Fill result for each item.
     * 
     * @param root the root
     * @param node the node
     * @param nodeType the node type
     * @param result the result
     * @throws RepositoryException the repository exception
     * @throws ItemNotFoundException the item not found exception
     * @throws AccessDeniedException the access denied exception
     * @throws ConfigurationException the configuration exception
     * @throws Exception the exception
     */
    private <T> void fillResultForEachItem( final ConfigurationNode root,
                                            final Node node,
                                            final Class<T> nodeType,
                                            final Set<T> result )
        throws RepositoryException, ItemNotFoundException, AccessDeniedException, ConfigurationException, Exception {
        final Stack<Node> nodeStack = new Stack<Node>();
        Node n = node;
        while (n.getDepth() != 1) {
            nodeStack.push(n);
            n = n.getParent();
        }
        ConfigurationNode lastParent = root;
        lookingForChildrenInsideJcr: while (!nodeStack.isEmpty()) {
            n = nodeStack.pop();
            final String name = n.getName();
            if (!name.startsWith(DEFAULT_OSL_PREFIX)) {
                continue lookingForChildrenInsideJcr;
            }
            final Class<? extends ConfigurationNode> childType = classHelper.getNodeClassFromName(name);
            if (childType.equals(root.getClass())) {
                continue lookingForChildrenInsideJcr;
            }
            final StaticMetadata childStaticMetadata = classHelper.getStaticMetadataFromClass(childType);
            final String childKeyPropertyName = DEFAULT_OSL_PREFIX + ":" //$NON-NLS-1$
                                                + childStaticMetadata.keyPropertyName();
            final Class<? extends Serializable> propertyClass = childStaticMetadata.keyPropertyType();
            final Serializable keyProperty = getProperty(n, childKeyPropertyName, propertyClass);
            final Set<? extends ConfigurationNode> children = findAllNodesOfType(lastParent, childType);
            lookingForParent: for (final ConfigurationNode child : children) {
                if (child.getInstanceMetadata().getKeyPropertyValue().equals(keyProperty)) {
                    lastParent = child;
                    break lookingForParent;
                }
            }
            if (nodeStack.size() == 0) {
                result.add(nodeType.cast(lastParent));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T extends ConfigurationNode, K extends Serializable> T findNodeByUuidAndVersion( final ConfigurationNode root,
                                                                                             final Class<T> nodeType,
                                                                                             final String uuid,
                                                                                             final String version )
        throws ConfigurationException {
        try {
            final Set<T> result = new HashSet<T>();
            final Node node = this.session.getNodeByUUID(uuid);
            this.fillResultForEachItem(root, node, nodeType, result);
            if (result.size() > 0) {
                return result.iterator().next();
            }
            return null;

        } catch (final Exception e) {
            throw logAndReturnNew(e, ConfigurationException.class);
        }

    }

    /**
     * {@inheritDoc}
     */
    public <T extends ConfigurationNode, K extends Serializable> Set<T> findNodesByKey( final ConfigurationNode root,
                                                                                        final Class<T> nodeType,
                                                                                        final K key )
        throws ConfigurationException {
        try {
            checkNotNull("root", root); //$NON-NLS-1$
            checkNotNull("nodeType", nodeType); //$NON-NLS-1$
            checkNotNull("key", key); //$NON-NLS-1$
            checkCondition("sessionAlive", this.session.isLive()); //$NON-NLS-1$
            final Set<T> result = new HashSet<T>();
            final StaticMetadata metadata = nodeType.getAnnotation(StaticMetadata.class);
            final String keyPropertyName = metadata.keyPropertyName();

            final String nodePath = classHelper.getNameFromNodeClass(nodeType);
            final String pathToFind = format("//*/{0}[@osl:{1}=''{2}'']", //$NON-NLS-1$
                                             nodePath, keyPropertyName, key);
            final NodeIterator nodes = JcrSupport.findAll(this.session, pathToFind);
            while (nodes.hasNext()) {
                final Node n = nodes.nextNode();
                this.fillResultForEachItem(root, n, nodeType, result);
            }
            return result;
        } catch (final Exception e) {
            throw logAndReturnNew(e, ConfigurationException.class);
        }
    }

    /**
     * Just create the "osl" prefix if that one doesn't exists, and after that created the node "osl:configuration" if that
     * doesn't exists.
     * 
     * @throws ConfigurationException the configuration exception
     */
    private void initDataInsideSession() throws ConfigurationException {
        try {
            final NamespaceRegistry namespaceRegistry = this.session.getWorkspace().getNamespaceRegistry();
            if (!this.prefixExists(namespaceRegistry)) {
                namespaceRegistry.registerNamespace(DEFAULT_OSL_PREFIX, NS_DESCRIPTION);
            }
        } catch (final Exception e) {
            logAndThrowNew(e, ConfigurationException.class);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws NoConfigurationYetException
     */
    public Configuration load( final LazyType lazyType ) throws ConfigurationException, NoConfigurationYetException {
        checkNotNull("lazyType", lazyType); //$NON-NLS-1$
        checkCondition("sessionAlive", this.session.isLive()); //$NON-NLS-1$
        final String defaultRootNode = classHelper.getNameFromNodeClass(Configuration.class);
        Node rootJcrNode;
        try {

            rootJcrNode = this.session.getRootNode().getNode(SharedConstants.DEFAULT_JCR_ROOT_NAME).getNode(defaultRootNode);
        } catch (final PathNotFoundException e) {
            throw logAndReturnNew(e, NoConfigurationYetException.class);
        } catch (final Exception e) {
            throw logAndReturnNew(e, ConfigurationException.class);

        }

        try {
            final Configuration rootNode = new Configuration();
            if (LazyType.EAGER.equals(lazyType)) {
                loadChildrenAndProperties(rootJcrNode, rootNode, rootNode.getInstanceMetadata().getStaticMetadata());
                this.setUuidData(rootJcrNode, rootNode);

            } else if (LazyType.LAZY.equals(lazyType)) {
                final JcrDataLoader dataLoader = new JcrDataLoader(rootNode, rootJcrNode, this.session);
                rootNode.getInstanceMetadata().getSharedData().setDataLoader(dataLoader);
            } else {
                logAndThrow(new IllegalArgumentException("Invalid lazyType")); //$NON-NLS-1$
            }

            rootNode.getInstanceMetadata().getSharedData().markAsSaved();
            rootNode.getInstanceMetadata().setSavedUniqueId(rootJcrNode.getUUID());
            return rootNode;
        } catch (final Exception e) {
            throw logAndReturnNew(e, ConfigurationException.class);
        }
    }

    /**
     * Verify if the prefix "osl" exists.
     * 
     * @param namespaceRegistry the namespace registry
     * @return true if exists
     * @throws RepositoryException the repository exception
     */
    private boolean prefixExists( final NamespaceRegistry namespaceRegistry ) throws RepositoryException {
        final String[] prefixes = namespaceRegistry.getPrefixes();
        boolean hasFound = false;
        for (final String prefix : prefixes) {
            if (DEFAULT_OSL_PREFIX.equals(prefix)) {
                hasFound = true;
                break;
            }
        }
        return hasFound;
    }

    /**
     * Removes the node.
     * 
     * @param oldItem the old item
     * @throws Exception the exception
     */
    private void removeNode( final ConfigurationNode oldItem ) throws Exception {
        assert oldItem != null;
        final String pathToFind = XpathSupport.getCompleteXpathFor(oldItem);
        final Node node = JcrSupport.findUnique(this.session, pathToFind);
        node.remove();
    }

    /**
     * {@inheritDoc}
     */
    public void save( final Configuration configuration ) throws ConfigurationException {
        checkNotNull("group", configuration); //$NON-NLS-1$
        checkCondition("sessionAlive", this.session.isLive()); //$NON-NLS-1$

        try {

            final String[] repositoryNames = configuration.getRepositoryNames().toArray(new String[0]);

            CommonJcrSupport.createRepositoryNodes(this.session, repositoryNames);

            final Set<ConfigurationNode> dirtyNodes = configuration.getInstanceMetadata().getSharedData().getDirtyNodes();

            final ConfigurationNode[] dirtyNodesAsArray = dirtyNodes.toArray(new ConfigurationNode[0]);

            sort(dirtyNodesAsArray, new ParentNumberComparator());

            final Map<ConfigurationNode, Node> alreadySaved = new HashMap<ConfigurationNode, Node>();
            for (final ConfigurationNode node : dirtyNodesAsArray) {
                if (alreadySaved.containsKey(node)) {
                    continue;
                }
                Node parentJcrNode = null;
                if (node.getInstanceMetadata().getDefaultParent() == null) {
                    try {
                        parentJcrNode = this.session.getRootNode().getNode(SharedConstants.DEFAULT_JCR_ROOT_NAME);
                    } catch (final PathNotFoundException e) {
                        parentJcrNode = this.session.getRootNode().addNode(SharedConstants.DEFAULT_JCR_ROOT_NAME);
                    }
                } else if (node.getInstanceMetadata().getDefaultParent() != null) {
                    if (dirtyNodes.contains(node.getInstanceMetadata().getDefaultParent())) {

                        parentJcrNode = alreadySaved.get(node.getInstanceMetadata().getDefaultParent());
                        if (parentJcrNode == null) {
                            logAndThrow(new IllegalStateException("Dirty node without dirty parent")); //$NON-NLS-1$
                        }
                    } else {
                        final String pathToFind = XpathSupport.getCompleteXpathFor(node.getInstanceMetadata().getDefaultParent());
                        parentJcrNode = JcrSupport.findUnique(this.session, pathToFind);
                        if (parentJcrNode == null) {
                            logAndThrow(new IllegalStateException("Dirty node without parent")); //$NON-NLS-1$
                        }
                    }
                }
                if (parentJcrNode == null) {
                    logAndThrow(new IllegalStateException("Parent for dirty node not found")); //$NON-NLS-1$
                }

                final StaticMetadata metadata = node.getInstanceMetadata().getStaticMetadata();

                final String nodePath = JcrSessionConfigurationManager.classHelper.getNameFromNodeClass(node.getClass());

                final Node newJcrNode = this.createIfDontExists(parentJcrNode, node, nodePath, metadata.keyPropertyName(),
                                                                node.getInstanceMetadata().getKeyPropertyValue(),
                                                                metadata.keyPropertyType());
                if (node instanceof Artifact) {
                    final Artifact a = (Artifact)node;
                    a.getInstanceMetadata().setPropertyIgnoringListener(Artifact.KeyProperties.UUID.name(), newJcrNode.getUUID());
                }
                node.getInstanceMetadata().setSavedUniqueId(newJcrNode.getUUID());
                alreadySaved.put(node, newJcrNode);
                this.saveProperties(node, newJcrNode);
            }

            final List<ItemChangeEvent<ConfigurationNode>> lastChanges = configuration.getInstanceMetadata().getSharedData().getNodeChangesSinceLastSave();

            for (final ItemChangeEvent<ConfigurationNode> event : lastChanges) {
                if (ItemChangeType.EXCLUDED.equals(event.getType())) {
                    this.removeNode(event.getOldItem());
                }
            }
            this.session.save();

            final String configurationPath = classHelper.getNameFromNodeClass(Configuration.class);
            final Node configurationJcrNode = this.session.getRootNode().getNode(SharedConstants.DEFAULT_JCR_ROOT_NAME).getNode(
                                                                                                                                configurationPath);

            final Version version = configurationJcrNode.checkin();
            configurationJcrNode.checkout();
            final String versionName = version.getName();
            final Set<Artifact> artifacts = findAllNodesOfType(configuration, Artifact.class);
            for (final Artifact a : artifacts) {
                a.getInstanceMetadata().setPropertyIgnoringListener(Artifact.KeyProperties.version.toString(), versionName);
            }
            configuration.getInstanceMetadata().getSharedData().markAsSaved();
        } catch (final Exception e) {
            logAndThrowNew(e, ConfigurationException.class);
        }
    }

    /**
     * Save properties.
     * 
     * @param configurationNode the configuration node
     * @param innerNewJcrNode the inner new jcr node
     * @throws Exception the exception
     */
    private void saveProperties( final ConfigurationNode configurationNode,
                                 final Node innerNewJcrNode ) throws Exception {
        final Map<String, Object> properties = configurationNode.getInstanceMetadata().getProperties();
        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            final Object value = entry.getValue();
            final Class<?> clazz = value != null ? value.getClass() : null;
            this.setProperty(innerNewJcrNode, DEFAULT_OSL_PREFIX + ":" //$NON-NLS-1$
                                              + entry.getKey(), clazz, entry.getValue());
        }
    }

    /**
     * Sets an property on a jcr node.
     * 
     * @param jcrNode the jcr node
     * @param propertyName the property name
     * @param propertyClass the property class
     * @param value the value
     * @throws Exception the exception
     */
    @SuppressWarnings( "boxing" )
    private void setProperty( final Node jcrNode,
                              final String propertyName,
                              final Class<?> propertyClass,
                              final Object value ) throws Exception {
        if (value == null) {
            jcrNode.setProperty(propertyName, (String)null);
        } else if (Boolean.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (Boolean)value);
        } else if (Calendar.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (Calendar)value);
        } else if (Double.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (Double)value);
        } else if (Long.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (Long)value);
        } else if (String.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (String)value);
        } else if (Integer.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (Integer)value);
        } else if (Byte.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (Byte)value);
        } else if (Float.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, (Float)value);
        } else if (Date.class.equals(propertyClass)) {
            jcrNode.setProperty(propertyName, stringFromDate((Date)value));
        } else if (propertyClass.isEnum()) {
            jcrNode.setProperty(propertyName, ((Enum<?>)value).name());
        } else if (InputStream.class.isAssignableFrom(propertyClass)) {
            jcrNode.setProperty(propertyName, (InputStream)value);
        } else if (Serializable.class.isAssignableFrom(propertyClass)) {
            final String valueAsString = serializeToBase64((Serializable)value);
            jcrNode.setProperty(propertyName, valueAsString);
        } else {
            throw new IllegalStateException(format("Invalid class for property {0} : {1}", propertyName, //$NON-NLS-1$
                                                   propertyClass));
        }
    }

    /**
     * Sets the uuid data.
     * 
     * @param rootJcrNode the root jcr node
     * @param rootNode the root node
     * @throws RepositoryException the repository exception
     * @throws UnsupportedRepositoryOperationException the unsupported repository operation exception
     */
    private void setUuidData( final Node rootJcrNode,
                              final Configuration rootNode ) throws RepositoryException, UnsupportedRepositoryOperationException {
        final Set<Artifact> artifacts = findAllNodesOfType(rootNode, Artifact.class);
        final String versionName = rootJcrNode.getBaseVersion().getName();
        for (final Artifact a : artifacts) {
            a.getInstanceMetadata().setPropertyIgnoringListener(Artifact.KeyProperties.version.toString(), versionName);
        }
    }
}
