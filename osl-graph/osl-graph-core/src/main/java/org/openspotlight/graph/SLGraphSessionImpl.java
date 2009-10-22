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
package org.openspotlight.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.openspotlight.common.exception.SLException;
import org.openspotlight.graph.annotation.SLLinkAttribute;
import org.openspotlight.graph.listeners.SLCollatorListener;
import org.openspotlight.graph.listeners.SLLinkCountListener;
import org.openspotlight.graph.listeners.SLMetadataListener;
import org.openspotlight.graph.listeners.SLObjectMarkListener;
import org.openspotlight.graph.listeners.SLTransientObjectListener;
import org.openspotlight.graph.persistence.SLPersistentNode;
import org.openspotlight.graph.persistence.SLPersistentNodeNotFoundException;
import org.openspotlight.graph.persistence.SLPersistentProperty;
import org.openspotlight.graph.persistence.SLPersistentQuery;
import org.openspotlight.graph.persistence.SLPersistentQueryResult;
import org.openspotlight.graph.persistence.SLPersistentTreeSession;
import org.openspotlight.graph.persistence.SLPersistentTreeSessionException;
import org.openspotlight.graph.query.SLInvalidQuerySyntaxException;
import org.openspotlight.graph.query.SLQueryApi;
import org.openspotlight.graph.query.SLQueryApiImpl;
import org.openspotlight.graph.query.SLQueryCache;
import org.openspotlight.graph.query.SLQueryCacheImpl;
import org.openspotlight.graph.query.SLQueryText;
import org.openspotlight.graph.query.SLQueryTextImpl;
import org.openspotlight.graph.query.SLQueryTextInternal;
import org.openspotlight.graph.query.parser.SLQueryTextInternalBuilder;
import org.openspotlight.graph.util.ProxyUtil;

/**
 * The Class SLGraphSessionImpl.
 * 
 * @author Vitor Hugo Chagas
 */
public class SLGraphSessionImpl implements SLGraphSession {

    /** The tree session. */
    private final SLPersistentTreeSession   treeSession;

    /** The event poster. */
    private final SLGraphSessionEventPoster eventPoster;

    /** The encoder. */
    private SLEncoder                       encoder;

    /** The encoder factory. */
    private final SLEncoderFactory          encoderFactory;

    /** The slql query builder. */
    private SLQueryTextInternalBuilder      queryBuilder;

    /** The slql query cache. */
    private SLQueryCache                    queryCache;

    /**
     * Instantiates a new sL graph session impl.
     * 
     * @param treeSession the tree session
     */
    public SLGraphSessionImpl(
                               final SLPersistentTreeSession treeSession ) {
        this.treeSession = treeSession;
        final Collection<SLGraphSessionEventListener> listeners = new ArrayList<SLGraphSessionEventListener>();
        listeners.add(new SLObjectMarkListener());
        listeners.add(new SLTransientObjectListener());
        listeners.add(new SLLinkCountListener());
        listeners.add(new SLCollatorListener());
        listeners.add(new SLMetadataListener());
        this.eventPoster = new SLGraphSessionEventPosterImpl(listeners);
        this.encoderFactory = new SLEncoderFactoryImpl();
        this.encoder = this.encoderFactory.getUUIDEncoder();
        this.queryBuilder = new SLQueryTextInternalBuilder();
        this.queryCache = new SLQueryCacheImpl(this.treeSession, this);
    }

    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphSession#addLink(java.lang.Class, org.openspotlight.graph.SLNode, org.openspotlight.graph.SLNode, boolean)
     */
    public <L extends SLLink> L addLink( final Class<L> linkClass,
                                         final SLNode source,
                                         final SLNode target,
                                         final boolean bidirecional ) throws SLGraphSessionException {
        return this.addLink(linkClass, source, target, bidirecional, SLPersistenceMode.NORMAL);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#addLink(java.lang.Class,
     * org.openspotlight.graph.SLNode, org.openspotlight.graph.SLNode, boolean)
     */
    public <L extends SLLink> L addLink( final Class<L> linkClass,
                                         final SLNode source,
                                         final SLNode target,
                                         final boolean bidirecional,
                                         final SLPersistenceMode persistenceMode ) throws SLGraphSessionException {

        try {

            SLPersistentNode linkNode = null;

            boolean changedToBidirectional = false;
            final boolean allowsMultiple = this.allowsMultiple(linkClass);
            final boolean allowsChangeToBidirecional = this.allowsChangeToBidirecional(linkClass);

            if (allowsMultiple && allowsChangeToBidirecional) {
                throw new SLGraphSessionException("ALLOWS_CHANGE_TO_BIDIRECTIONAL and ALLOWS_MULTIPLE attributes are not supported at once.");
            }

            final SLPersistentNode pairKeyNode = this.getPairKeyNode(linkClass, source, target);
            final int direction = this.getDirection(source, target, bidirecional);

            boolean newLink = false;

            if (allowsMultiple) {
                newLink = true;
            } else {

                linkNode = this.getLinkNodeByDirection(pairKeyNode, direction);

                if (linkNode == null) {
                    if (allowsChangeToBidirecional) {
                        linkNode = this.findUniqueLinkNode(pairKeyNode);
                        if (linkNode == null) {
                            newLink = true;
                        } else {
                            final SLPersistentProperty<Integer> directionProp = linkNode.getProperty(Integer.class, SLConsts.PROPERTY_NAME_DIRECTION);
                            if (directionProp.getValue() != SLConsts.DIRECTION_BOTH) {
                                directionProp.setValue(SLConsts.DIRECTION_BOTH);
                                changedToBidirectional = true;
                            }
                        }
                    } else {
                        newLink = true;
                    }
                }
            }

            if (newLink) {
                linkNode = this.addLinkNode(pairKeyNode, linkClass, source, target, direction);
            }

            final SLLink link = new SLLinkImpl(this, linkNode, this.eventPoster);
            final L linkProxy = ProxyUtil.createLinkProxy(linkClass, link);
            SLLinkEvent event = new SLLinkEvent(SLLinkEvent.TYPE_LINK_ADDED, linkProxy, linkNode, persistenceMode);
            event.setSource(source);
            event.setTarget(target);
            event.setNewLink(newLink);
            event.setChangedToBidirectional(changedToBidirectional);
            this.eventPoster.post(event);
            return linkProxy;
        } catch (final SLException e) {
            throw new SLGraphSessionException("Error on attempt to add link.", e);
        }
    }

    /**
     * Adds the link node.
     * 
     * @param pairKeyNode the pair key node
     * @param linkType the link type
     * @param source the source
     * @param target the target
     * @param direction the direction
     * @return the sL persistent node
     * @throws SLPersistentTreeSessionException the SL persistent tree session exception
     * @throws SLGraphSessionException the SL graph session exception
     */
    @SuppressWarnings( "unchecked" )
    private SLPersistentNode addLinkNode( final SLPersistentNode pairKeyNode,
                                          Class<? extends SLLink> linkType,
                                          SLNode source,
                                          SLNode target,
                                          final int direction ) throws SLPersistentTreeSessionException, SLGraphSessionException {
        final long linkCount = this.incLinkCount(pairKeyNode);
        final String name = SLCommonSupport.getLinkIndexNodeName(linkCount);
        final SLPersistentNode linkNode = pairKeyNode.addNode(name);
        Class<? extends SLNode> sourceType = (Class<? extends SLNode>)source.getClass().getInterfaces()[0];
        Class<? extends SLNode> targetType = (Class<? extends SLNode>)target.getClass().getInterfaces()[0];
        SLCommonSupport.setInternalStringProperty(linkNode, SLConsts.PROPERTY_NAME_SOURCE_ID, source.getID());
        SLCommonSupport.setInternalStringProperty(linkNode, SLConsts.PROPERTY_NAME_TARGET_ID, target.getID());
        SLCommonSupport.setInternalIntegerProperty(linkNode, SLConsts.PROPERTY_NAME_LINK_TYPE_HASH, linkType.getName().hashCode());
        SLCommonSupport.setInternalIntegerProperty(linkNode, SLConsts.PROPERTY_NAME_SOURCE_TYPE_HASH, sourceType.getName().hashCode());
        SLCommonSupport.setInternalIntegerProperty(linkNode, SLConsts.PROPERTY_NAME_TARGET_TYPE_HASH, targetType.getName().hashCode());
        linkNode.setProperty(Long.class, SLConsts.PROPERTY_NAME_LINK_COUNT, linkCount);
        linkNode.setProperty(Integer.class, SLConsts.PROPERTY_NAME_DIRECTION, direction);
        return linkNode;
    }

    /**
     * Allows change to bidirecional.
     * 
     * @param linkTypeClass the link type class
     * @return true, if successful
     */
    private boolean allowsChangeToBidirecional( final Class<? extends SLLink> linkTypeClass ) {
        final SLLinkAttribute attribute = linkTypeClass.getAnnotation(SLLinkAttribute.class);
        return (attribute != null) && (Arrays.binarySearch(attribute.value(), SLLinkAttribute.ALLOWS_CHANGE_TO_BIDIRECTIONAL) > -1);
    }

    /**
     * Allows multiple.
     * 
     * @param linkTypeClass the link type class
     * @return true, if successful
     */
    private boolean allowsMultiple( final Class<? extends SLLink> linkTypeClass ) {
        final SLLinkAttribute attribute = linkTypeClass.getAnnotation(SLLinkAttribute.class);
        return (attribute != null) && (Arrays.binarySearch(attribute.value(), SLLinkAttribute.ALLOWS_MULTIPLE) > -1);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#clear()
     */
    public void clear() throws SLGraphSessionException {
        try {
            this.eventPoster.sessionCleaned();
            this.treeSession.clear();
        } catch (final SLPersistentTreeSessionException e) {
            throw new SLGraphSessionException("Error on attempt to clear OSL repository.", e);
        }
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#close()
     */
    public void close() {
        this.treeSession.close();
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#createContext(java.lang.Long)
     */
    public SLContext createContext( final String id ) throws SLContextAlreadyExistsException, SLGraphSessionException {
        SLContext context = null;
        try {
            final SLPersistentNode contextsPersistentNode = SLCommonSupport.getContextsPersistentNode(this.treeSession);
            if (contextsPersistentNode.getNode("" + id) == null) {
                final SLPersistentNode contextRootPersistentNode = contextsPersistentNode.addNode("" + id);
                context = new SLContextImpl(this, contextRootPersistentNode, this.eventPoster);
            } else {
                throw new SLContextAlreadyExistsException(id);
            }
        } catch (final SLPersistentTreeSessionException e) {
            throw new SLGraphSessionException("Error on attempt to create context node.", e);
        }
        return context;
    }

    /**
     * Filter nodes from links.
     * 
     * @param links the links
     * @param node the node
     * @param nodeClass the node class
     * @param returnSubTypes the return sub types
     * @return the set< n>
     * @throws SLGraphSessionException the SL graph session exception
     */
    private <N extends SLNode> Set<N> filterNodesFromLinks( final Collection<? extends SLLink> links,
                                                            final SLNode node,
                                                            final Class<N> nodeClass,
                                                            final boolean returnSubTypes ) throws SLGraphSessionException {
        final Set<N> nodes = new HashSet<N>();
        for (final SLLink link : links) {
            if (node == null) {
                final SLNode side1 = link.getSides()[0];
                final SLNode side2 = link.getSides()[1];
                if (this.nodeOfType(side1, nodeClass, returnSubTypes)) {
                    nodes.add(nodeClass.cast(side1));
                }
                if (this.nodeOfType(side2, nodeClass, returnSubTypes)) {
                    nodes.add(nodeClass.cast(side2));
                }
            } else {
                final SLNode otherSide = link.getOtherSide(node);
                if (this.nodeOfType(otherSide, nodeClass, returnSubTypes)) {
                    nodes.add(nodeClass.cast(otherSide));
                }
            }
        }
        return nodes;
    }

    /**
     * Find unique link node.
     * 
     * @param pairKeyNode the pair key node
     * @return the sL persistent node
     * @throws SLPersistentTreeSessionException the SL persistent tree session exception
     */
    private SLPersistentNode findUniqueLinkNode( final SLPersistentNode pairKeyNode ) throws SLPersistentTreeSessionException {
        return pairKeyNode.getNodes().isEmpty() ? null : pairKeyNode.getNodes().iterator().next();
    }

    /**
     * Gets the a node.
     * 
     * @param source the source
     * @param target the target
     * @return the a node
     * @throws SLException the SL exception
     */
    private SLNode getANode( final SLNode source,
                             final SLNode target ) throws SLException {
        return source.getID().compareTo(target.getID()) < 0 ? source : target;
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getBidirectionalLinks(java.lang
     * .Class, org.openspotlight.graph.SLNode, org.openspotlight.graph.SLNode)
     */
    public <L extends SLLink> Collection<L> getBidirectionalLinks( final Class<L> linkClass,
                                                                   final SLNode side1,
                                                                   final SLNode side2 ) throws SLGraphSessionException {
        return this.getLinks(linkClass, side1, side2, SLLink.DIRECTION_BI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @seeorg.openspotlight.graph.SLGraphSession#getBidirectionalLinks(org.
     * openspotlight.graph.SLNode, org.openspotlight.graph.SLNode)
     */
    public Collection<SLLink> getBidirectionalLinks( final SLNode side1,
                                                     final SLNode side2 ) throws SLGraphSessionException {
        return this.getLinks(side1, side2, SLLink.DIRECTION_BI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getBidirectionalLinksBySide(java
     * .lang.Class, org.openspotlight.graph.SLNode)
     */
    public <L extends SLLink> Collection<L> getBidirectionalLinksBySide( final Class<L> linkClass,
                                                                         final SLNode side ) throws SLGraphSessionException {
        return this.getLinks(linkClass, side, null, SLLink.DIRECTION_BI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getBidirectionalLinksBySide(org
     * .openspotlight.graph.SLNode)
     */
    public Collection<SLLink> getBidirectionalLinksBySide( final SLNode side ) throws SLGraphSessionException {
        return this.getLinks(side, null, SLLink.DIRECTION_BI);
    }

    /**
     * Gets the b node.
     * 
     * @param source the source
     * @param target the target
     * @return the b node
     * @throws SLException the SL exception
     */
    private SLNode getBNode( final SLNode source,
                             final SLNode target ) throws SLException {
        return source.getID().compareTo(target.getID()) < 0 ? target : source;
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#getContext(java.lang.Long)
     */
    public SLContext getContext( final String id ) throws SLGraphSessionException {
        try {
            SLContext context = null;
            final SLPersistentNode contextsPersistentNode = SLCommonSupport.getContextsPersistentNode(this.treeSession);
            final SLPersistentNode contextRootPersistentNode = contextsPersistentNode.getNode("" + id);
            if (contextRootPersistentNode != null) {
                context = new SLContextImpl(this, contextRootPersistentNode, this.eventPoster);
            }
            return context;
        } catch (final SLPersistentTreeSessionException e) {
            throw new SLGraphSessionException("Error on attempt retrieve context node.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#getDefaultEncoder()
     */
    public SLEncoder getDefaultEncoder() throws SLGraphSessionException {
        return this.encoder;
    }

    /**
     * Gets the direction.
     * 
     * @param source the source
     * @param target the target
     * @param bidirecional the bidirecional
     * @return the direction
     * @throws SLException the SL exception
     */
    private int getDirection( final SLNode source,
                              final SLNode target,
                              final boolean bidirecional ) throws SLException {
        if (bidirecional) {
            return SLConsts.DIRECTION_BOTH;
        } else {
            return this.getANode(source, target).equals(source) ? SLConsts.DIRECTION_AB : SLConsts.DIRECTION_BA;
        }
    }

    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphSession#getEncoderFactory()
     */
    public SLEncoderFactory getEncoderFactory() throws SLGraphSessionException {
        return this.encoderFactory;
    }

    /**
     * Gets the link classes.
     * 
     * @return the link classes
     * @throws SLGraphSessionException the SL graph session exception
     */
    @SuppressWarnings( "unchecked" )
    private Collection<Class<? extends SLLink>> getLinkClasses() throws SLGraphSessionException {
        try {
            final Collection<Class<? extends SLLink>> linkClasses = new ArrayList<Class<? extends SLLink>>();
            final SLPersistentQuery query = this.treeSession.createQuery("//osl/links/*", SLPersistentQuery.TYPE_XPATH);
            final SLPersistentQueryResult result = query.execute();
            final Collection<SLPersistentNode> linkClassNodes = result.getNodes();
            for (final SLPersistentNode linkClassNode : linkClassNodes) {
                final Class<? extends SLLink> linkClass = (Class<? extends SLLink>)Class.forName(linkClassNode.getName());
                linkClasses.add(linkClass);
            }
            return linkClasses;
        } catch (final Exception e) {
            throw new SLGraphSessionException("Error on attempt to retrieve link classes.", e);
        }
    }

    /**
     * Gets the link node by direction.
     * 
     * @param pairKeyNode the pair key node
     * @param direction the direction
     * @return the link node by direction
     * @throws SLPersistentTreeSessionException the SL persistent tree session exception
     */
    private SLPersistentNode getLinkNodeByDirection( final SLPersistentNode pairKeyNode,
                                                     final int direction ) throws SLPersistentTreeSessionException {
        SLPersistentNode linkNode = null;
        for (final SLPersistentNode node : pairKeyNode.getNodes()) {
            final SLPersistentProperty<Long> directionProp = node.getProperty(Long.class, SLConsts.PROPERTY_NAME_DIRECTION);
            if (directionProp.getValue() == direction) {
                linkNode = node;
            }
        }
        return linkNode;
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#getLinks(java.lang.Class,
     * org.openspotlight.graph.SLNode, org.openspotlight.graph.SLNode)
     */
    public <L extends SLLink> Collection<L> getLinks( final Class<L> linkClass,
                                                      final SLNode source,
                                                      final SLNode target ) throws SLGraphSessionException {
        return this.getLinks(linkClass, source, target, SLLink.DIRECTION_ANY);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#getLinks(java.lang.Class,
     * org.openspotlight.graph.SLNode, org.openspotlight.graph.SLNode, int)
     */
    public <L extends SLLink> Collection<L> getLinks( final Class<L> linkClass,
                                                      final SLNode source,
                                                      final SLNode target,
                                                      final int direction ) throws SLGraphSessionException {

        try {

            final Collection<L> links = new TreeSet<L>();

            // query format:
            // //osl/links/JavaClassToJavaMethod/*[(@a=sourceID or @b=sourceID)
            // and (@a=targetID or @b=targetID)]/*[@direction=AB or @direcionBA]
            // order by @linkCount ascending

            final StringBuilder statement = new StringBuilder();
            statement.append("//osl/links/").append(linkClass.getName()).append("/*");

            if ((source != null) || (target != null)) {

                statement.append('[');

                // filter source ...
                if (source != null) {
                    statement.append('(').append('@').append(SLConsts.PROPERTY_NAME_A_NODE_ID).append("='").append(source.getID()).append("'").append(" or ").append('@').append(SLConsts.PROPERTY_NAME_B_NODE_ID).append("='").append(source.getID()).append("'").append(')');
                }

                if ((source != null) && (target != null)) {
                    statement.append(" and ");
                }

                // filter target ...
                if (target != null) {
                    statement.append('(').append('@').append(SLConsts.PROPERTY_NAME_A_NODE_ID).append("='").append(target.getID()).append("'").append(" or ").append('@').append(SLConsts.PROPERTY_NAME_B_NODE_ID).append("='").append(target.getID()).append("'").append(')');
                }

                statement.append(']');
            }

            statement.append("/*");

            final StringBuilder directionFilter = new StringBuilder();
            if ((direction == (direction | SLLink.DIRECTION_UNI)) || (direction == (direction | SLLink.DIRECTION_UNI_REVERSAL))) {
                directionFilter.append('@').append(SLConsts.PROPERTY_NAME_DIRECTION).append('=').append(SLConsts.DIRECTION_AB).append(" or @").append(SLConsts.PROPERTY_NAME_DIRECTION).append('=').append(SLConsts.DIRECTION_BA);
            }
            if (direction == (direction | SLLink.DIRECTION_BI)) {
                if (directionFilter.length() > 0) {
                    directionFilter.append(" or ");
                }
                directionFilter.append('@').append(SLConsts.PROPERTY_NAME_DIRECTION).append('=').append(SLConsts.DIRECTION_BOTH);
            }

            // add direction filter ...
            statement.append('[').append(directionFilter).append(']');

            // order by link count (the order the links are added by the user)
            // ...
            statement.append(" order by @").append(SLConsts.PROPERTY_NAME_LINK_COUNT).append(" ascending");

            // execute query ...
            final SLPersistentQuery query = this.treeSession.createQuery(statement.toString(), SLPersistentQuery.TYPE_XPATH);
            final SLPersistentQueryResult result = query.execute();
            final Collection<SLPersistentNode> linkNodes = result.getNodes();

            for (final SLPersistentNode linkNode : linkNodes) {

                final SLPersistentNode pairKeyNode = linkNode.getParent();

                final SLPersistentProperty<String> aNodeIDProp = pairKeyNode.getProperty(String.class, SLConsts.PROPERTY_NAME_A_NODE_ID);
                final SLPersistentProperty<String> bNodeIDProp = pairKeyNode.getProperty(String.class, SLConsts.PROPERTY_NAME_B_NODE_ID);
                final SLPersistentProperty<Integer> directionProp = linkNode.getProperty(Integer.class, SLConsts.PROPERTY_NAME_DIRECTION);

                final SLNode aNode = this.getNodeByID(aNodeIDProp.getValue());
                final SLNode bNode = this.getNodeByID(bNodeIDProp.getValue());

                boolean status = false;

                if ((source == null) && (target == null)) {
                    if ((directionProp.getValue() == SLConsts.DIRECTION_AB) || (directionProp.getValue() == SLConsts.DIRECTION_BA)) {
                        status = direction == (direction | SLLink.DIRECTION_UNI);
                    } else {
                        status = direction == (direction | SLLink.DIRECTION_BI);
                    }
                } else {

                    if (directionProp.getValue() == SLConsts.DIRECTION_BOTH) {
                        status = direction == (direction | SLLink.DIRECTION_BI);
                    } else {

                        SLNode s = null;
                        SLNode t = null;

                        if (directionProp.getValue() == SLConsts.DIRECTION_AB) {
                            s = aNode;
                            t = bNode;
                        } else if (directionProp.getValue() == SLConsts.DIRECTION_BA) {
                            s = bNode;
                            t = aNode;
                        }

                        status = ((direction == (direction | SLLink.DIRECTION_UNI)) && (s.equals(source) || t.equals(target))) || ((direction == (direction | SLLink.DIRECTION_UNI_REVERSAL)) && (s.equals(target) || t.equals(source)));
                    }
                }

                if (status) {
                    final SLLink link = new SLLinkImpl(this, linkNode, this.eventPoster);
                    links.add(ProxyUtil.createLinkProxy(linkClass, link));
                }
            }

            return links;
        } catch (final SLPersistentTreeSessionException e) {
            throw new SLGraphSessionException("Error on attempt to retrieve links.", e);
        }
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getLinks(org.openspotlight.graph
     * .SLNode, org.openspotlight.graph.SLNode)
     */
    public Collection<SLLink> getLinks( final SLNode source,
                                        final SLNode target ) throws SLGraphSessionException {
        return this.getLinks(source, target, SLLink.DIRECTION_ANY);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getLinks(org.openspotlight.graph
     * .SLNode, org.openspotlight.graph.SLNode, int)
     */
    public Collection<SLLink> getLinks( final SLNode source,
                                        final SLNode target,
                                        final int directionType ) throws SLGraphSessionException {
        final Collection<SLLink> links = new ArrayList<SLLink>();
        final Collection<Class<? extends SLLink>> linkClasses = this.getLinkClasses();
        for (final Class<? extends SLLink> linkClass : linkClasses) {
            links.addAll(this.getLinks(linkClass, source, target, directionType));
        }
        return links;
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#getMetadata()
     */
    public SLMetadata getMetadata() {
        return new SLMetadataImpl(this.treeSession);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#getNodeByID(java.lang.String)
     */
    public SLNode getNodeByID( final String id ) throws SLNodeNotFoundException, SLGraphSessionException {
        final int INDEX_CONTEXT_ID = 2;
        try {
            SLNode node = null;
            final SLPersistentNode pNode = this.treeSession.getNodeByID(id);
            final String[] names = pNode.getPath().substring(1).split("/");
            final String contextID = names[INDEX_CONTEXT_ID];
            final SLContext context = this.getContext(contextID);
            final SLEncoder fakeEncoder = this.getEncoderFactory().getFakeEncoder();
            for (int i = INDEX_CONTEXT_ID + 1; i < names.length; i++) {
                if (node == null) {
                    SLNode rootNode = context.getRootNode();
                    node = context.getRootNode().getNode(SLNode.class, names[i], fakeEncoder);
                    Class<? extends SLNode> nodeType = getNodeType(rootNode);
                    node = ProxyUtil.createNodeProxy(nodeType, node);
                } else {
                    node = node.getNode(SLNode.class, names[i], fakeEncoder);
                    Class<? extends SLNode> nodeType = getNodeType(node);
                    node = ProxyUtil.createNodeProxy(nodeType, node);
                }
            }
            return node;
        } catch (final SLPersistentNodeNotFoundException e) {
            throw new SLNodeNotFoundException(id, e);
        } catch (final SLException e) {
            throw new SLGraphSessionException("Error on attempt to retrieve node by id.", e);
        }
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(java.lang.Class)
     */
    public Collection<SLNode> getNodesByLink( final Class<? extends SLLink> linkClass ) throws SLGraphSessionException {
        return this.getNodesByLink(linkClass, null);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(java.lang.Class,
     * org.openspotlight.graph.SLNode)
     */
    public Collection<SLNode> getNodesByLink( final Class<? extends SLLink> linkClass,
                                              final SLNode node ) throws SLGraphSessionException {
        return this.getNodesByLink(linkClass, node, SLLink.DIRECTION_UNI | SLLink.DIRECTION_BI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(java.lang.Class,
     * org.openspotlight.graph.SLNode, java.lang.Class, boolean)
     */
    public <N extends SLNode> Collection<N> getNodesByLink( final Class<? extends SLLink> linkClass,
                                                            final SLNode node,
                                                            final Class<N> nodeClass,
                                                            final boolean returnSubTypes ) throws SLGraphSessionException {
        return this.getNodesByLink(linkClass, node, nodeClass, returnSubTypes, SLLink.DIRECTION_UNI | SLLink.DIRECTION_BI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(java.lang.Class,
     * org.openspotlight.graph.SLNode, java.lang.Class, boolean, int)
     */
    public <N extends SLNode> Collection<N> getNodesByLink( final Class<? extends SLLink> linkClass,
                                                            final SLNode node,
                                                            final Class<N> nodeClass,
                                                            final boolean returnSubTypes,
                                                            final int direction ) throws SLGraphSessionException {
        try {
            final Collection<? extends SLLink> links = this.getLinks(linkClass, node, null, direction);
            return this.filterNodesFromLinks(links, node, nodeClass, returnSubTypes);
        } catch (final SLGraphSessionException e) {
            throw new SLGraphSessionException("Error on attempt to retrieve nodes by link.", e);
        }
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(java.lang.Class,
     * org.openspotlight.graph.SLNode, int)
     */
    public Collection<SLNode> getNodesByLink( final Class<? extends SLLink> linkClass,
                                              final SLNode node,
                                              final int direction ) throws SLGraphSessionException {
        return this.getNodesByLink(linkClass, node, SLNode.class, true, direction);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(org.openspotlight
     * .graph.SLNode)
     */
    public Collection<SLNode> getNodesByLink( final SLNode node ) throws SLGraphSessionException {
        return this.getNodesByLink(node, SLLink.DIRECTION_UNI | SLLink.DIRECTION_BI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(org.openspotlight
     * .graph.SLNode, java.lang.Class, boolean)
     */
    public <N extends SLNode> Collection<N> getNodesByLink( final SLNode node,
                                                            final Class<N> nodeClass,
                                                            final boolean returnSubTypes ) throws SLGraphSessionException {
        return this.getNodesByLink(node, nodeClass, returnSubTypes, SLLink.DIRECTION_UNI | SLLink.DIRECTION_BI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(org.openspotlight
     * .graph.SLNode, java.lang.Class, boolean, int)
     */
    public <N extends SLNode> Collection<N> getNodesByLink( final SLNode node,
                                                            final Class<N> nodeClass,
                                                            final boolean returnSubTypes,
                                                            final int direction ) throws SLGraphSessionException {
        try {
            final Collection<? extends SLLink> links = this.getLinks(node, null, direction);
            return this.filterNodesFromLinks(links, node, nodeClass, returnSubTypes);
        } catch (final SLGraphSessionException e) {
            throw new SLGraphSessionException("Error on attempt to retrieve nodes by link.", e);
        }
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByLink(org.openspotlight
     * .graph.SLNode, int)
     */
    public Collection<SLNode> getNodesByLink( final SLNode node,
                                              final int direction ) throws SLGraphSessionException {
        return this.getNodesByLink(node, SLNode.class, true, direction);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getNodesByPredicate(org.openspotlight
     * .graph.SLNodePredicate)
     */
    public Collection<SLNode> getNodesByPredicate( final SLNodePredicate predicate ) throws SLGraphSessionException {
        try {
            final Collection<SLNode> nodes = new ArrayList<SLNode>();
            final SLPersistentQuery query = this.treeSession.createQuery("//osl/contexts/*//descendant::node()", SLPersistentQuery.TYPE_XPATH);
            final SLPersistentQueryResult result = query.execute();
            final Collection<SLPersistentNode> pNodes = result.getNodes();
            for (final SLPersistentNode pNode : pNodes) {
                final SLNode node = this.getNodeByID(pNode.getID());
                if ((node != null) && predicate.evaluate(node)) {
                    nodes.add(node);
                }
            }
            return nodes;
        } catch (final SLException e) {
            throw new SLGraphSessionException("Error on attempt to retrieve nodes by predicate.", e);
        }
    }

    /**
     * Gets the pair key node.
     * 
     * @param linkClass the link class
     * @param source the source
     * @param target the target
     * @return the pair key node
     * @throws SLException the SL exception
     */
    private SLPersistentNode getPairKeyNode( final Class<? extends SLLink> linkClass,
                                             final SLNode source,
                                             final SLNode target ) throws SLException {

        final SLNode a = this.getANode(source, target);
        final SLNode b = this.getBNode(source, target);
        final String aIDName = SLCommonSupport.getNameInIDForm(a);
        final String bIDName = SLCommonSupport.getNameInIDForm(b);

        final StringBuilder pairKey = new StringBuilder();
        pairKey.append(aIDName).append('.').append(bIDName);
        final SLPersistentNode linkClassNode = SLCommonSupport.getLinkClassNode(this.treeSession, linkClass);
        SLPersistentNode pairKeyNode = linkClassNode.getNode(pairKey.toString());

        if (pairKeyNode == null) {
            pairKeyNode = linkClassNode.addNode(pairKey.toString());
            pairKeyNode.setProperty(String.class, SLConsts.PROPERTY_NAME_A_NODE_ID, a.getID());
            pairKeyNode.setProperty(String.class, SLConsts.PROPERTY_NAME_B_NODE_ID, b.getID());
            pairKeyNode.setProperty(Long.class, SLConsts.PROPERTY_NAME_LINK_COUNT, 0L);
        }

        return pairKeyNode;
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getUnidirectionalLinks(java.lang
     * .Class, org.openspotlight.graph.SLNode, org.openspotlight.graph.SLNode)
     */
    public <L extends SLLink> Collection<L> getUnidirectionalLinks( final Class<L> linkClass,
                                                                    final SLNode source,
                                                                    final SLNode target ) throws SLGraphSessionException {
        return this.getLinks(linkClass, source, target, SLLink.DIRECTION_UNI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @seeorg.openspotlight.graph.SLGraphSession#getUnidirectionalLinks(org.
     * openspotlight.graph.SLNode, org.openspotlight.graph.SLNode)
     */
    public Collection<SLLink> getUnidirectionalLinks( final SLNode source,
                                                      final SLNode target ) throws SLGraphSessionException {
        return this.getLinks(source, target, SLLink.DIRECTION_UNI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getUnidirectionalLinksBySource
     * (java.lang.Class, org.openspotlight.graph.SLNode)
     */
    public <L extends SLLink> Collection<L> getUnidirectionalLinksBySource( final Class<L> linkClass,
                                                                            final SLNode source ) throws SLGraphSessionException {
        return this.getLinks(linkClass, source, null, SLLink.DIRECTION_UNI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getUnidirectionalLinksBySource
     * (org.openspotlight.graph.SLNode)
     */
    public Collection<SLLink> getUnidirectionalLinksBySource( final SLNode source ) throws SLGraphSessionException {
        return this.getLinks(source, null, SLLink.DIRECTION_UNI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getUnidirectionalLinksByTarget
     * (java.lang.Class, org.openspotlight.graph.SLNode)
     */
    public <L extends SLLink> Collection<L> getUnidirectionalLinksByTarget( final Class<L> linkClass,
                                                                            final SLNode target ) throws SLGraphSessionException {
        return this.getLinks(linkClass, null, target, SLLink.DIRECTION_UNI);
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#getUnidirectionalLinksByTarget
     * (org.openspotlight.graph.SLNode)
     */
    public Collection<SLLink> getUnidirectionalLinksByTarget( final SLNode target ) throws SLGraphSessionException {
        return this.getLinks(null, target, SLLink.DIRECTION_UNI);
    }

    /**
     * Inc link count.
     * 
     * @param linkKeyPairNode the link key pair node
     * @return the long
     * @throws SLPersistentTreeSessionException the SL persistent tree session exception
     */
    private long incLinkCount( final SLPersistentNode linkKeyPairNode ) throws SLPersistentTreeSessionException {
        final SLPersistentProperty<Long> linkCountProp = linkKeyPairNode.getProperty(Long.class, SLConsts.PROPERTY_NAME_LINK_COUNT);
        final long linkCount = linkCountProp.getValue() + 1;
        linkCountProp.setValue(linkCount);
        return linkCount;
    }

    /**
     * Node of type.
     * 
     * @param node the node
     * @param nodeClass the node class
     * @param returnSubTypes the return sub types
     * @return true, if successful
     */
    private boolean nodeOfType( final SLNode node,
                                final Class<? extends SLNode> nodeClass,
                                final boolean returnSubTypes ) {
        return (returnSubTypes && nodeClass.isAssignableFrom(node.getClass().getInterfaces()[0])) || (!returnSubTypes && nodeClass.equals(node.getClass().getInterfaces()[0]));
    }

    // @Override
    /*
     * (non-Javadoc)
     * 
     * @see org.openspotlight.graph.SLGraphSession#save()
     */
    public void save() throws SLGraphSessionException {
        try {
            this.eventPoster.post(new SLGraphSessionEvent(SLGraphSessionEvent.TYPE_BEFORE_SAVE, this));
            this.treeSession.save();
        } catch (final SLException e) {
            throw new SLGraphSessionException("Error on attempt to save the session.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.graph.SLGraphSession#setDefaultEncoder(org.openspotlight
     * .graph.SLEncoder)
     */
    public void setDefaultEncoder( final SLEncoder encoder ) throws SLGraphSessionException {
        this.encoder = encoder;
    }

    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphSession#createQuery()
     */
    public SLQueryApi createQueryApi() throws SLGraphSessionException {
        return new SLQueryApiImpl(this, treeSession, queryCache);
    }

    /* (non-Javadoc)
    * @see org.openspotlight.graph.SLGraphSession#createQuery()
    */
    public SLQueryText createQueryText( String slqlInput ) throws SLGraphSessionException, SLInvalidQuerySyntaxException {
        SLQueryTextInternal query = queryBuilder.build(slqlInput);
        return new SLQueryTextImpl(this, treeSession, query);
    }

    /**
     * Gets the node type.
     * 
     * @param node the node
     * @return the node type
     * @throws SLPersistentTreeSessionException the SL persistent tree session exception
     */
    private Class<? extends SLNode> getNodeType( SLNode node ) throws SLPersistentTreeSessionException {
        SLPersistentNode pNode = SLCommonSupport.getPNode(node);
        String typeName = SLCommonSupport.getInternalPropertyAsString(pNode, SLConsts.PROPERTY_NAME_TYPE);
        return getNodeType(typeName);
    }

    /**
     * Gets the node type.
     * 
     * @param typeName the type name
     * @return the node type
     */
    @SuppressWarnings( "unchecked" )
    private Class<? extends SLNode> getNodeType( String typeName ) {
        Class<? extends SLNode> nodeType = null;
        if (typeName != null) {
            try {
                return (Class<? extends SLNode>)Class.forName(typeName);
            } catch (ClassNotFoundException e) {
            }
        }
        return nodeType == null ? SLNode.class : nodeType;
    }

}