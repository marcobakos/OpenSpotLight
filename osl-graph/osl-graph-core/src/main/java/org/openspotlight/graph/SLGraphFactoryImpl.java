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

import static org.openspotlight.common.util.Exceptions.logAndReturnNew;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.openspotlight.common.exception.AbstractFactoryException;
import org.openspotlight.common.exception.ConfigurationException;
import org.openspotlight.common.util.AbstractFactory;
import org.openspotlight.graph.persistence.SLPersistentNode;
import org.openspotlight.graph.persistence.SLPersistentProperty;
import org.openspotlight.graph.persistence.SLPersistentTree;
import org.openspotlight.graph.persistence.SLPersistentTreeFactory;
import org.openspotlight.graph.persistence.SLPersistentTreeSession;
import org.openspotlight.jcr.provider.JcrConnectionDescriptor;
import org.openspotlight.jcr.provider.JcrConnectionProvider;

/**
 * The Class SLGraphFactoryImpl.
 * 
 * @author Vitor Hugo Chagas
 */
public class SLGraphFactoryImpl extends SLGraphFactory {

    public static interface SLGraphClosingListener {
        public void graphClosed( SLGraph desc );
    }

    private class SLGraphClosingListenerImpl implements SLGraphClosingListener {

        public void graphClosed( final SLGraph desc ) {
            JcrConnectionDescriptor data = null;
            for (final Entry<JcrConnectionDescriptor, SLGraph> entry : SLGraphFactoryImpl.this.cache.entrySet()) {
                if (entry.getValue().equals(desc)) {
                    data = entry.getKey();
                    break;
                }
            }
            synchronized (SLGraphFactoryImpl.this.cache) {
                if (data != null) {
                    SLGraphFactoryImpl.this.cache.remove(data);
                }

            }

        }

    }

    private SLGraph                                     graph;

    private final Map<JcrConnectionDescriptor, SLGraph> cache = new ConcurrentHashMap<JcrConnectionDescriptor, SLGraph>();

    @Override
    public synchronized SLGraph createGraph( final JcrConnectionDescriptor descriptor ) throws SLGraphFactoryException {
        SLGraph cached = this.cache.get(descriptor);
        if (cached == null) {
            try {
                SLPersistentTreeFactory factory;
                factory = AbstractFactory.getDefaultInstance(SLPersistentTreeFactory.class);
                final JcrConnectionProvider provider = JcrConnectionProvider.createFromData(descriptor);
                provider.openRepositoryAndCleanIfItIsTemporary();//this is necessary only because test issues. DO NOT REMOVE THIS LINE!
                final SLPersistentTree tree = factory.createPersistentTree(descriptor);
                cached = new SLGraphImpl(tree, new SLGraphClosingListenerImpl());
                this.cache.put(descriptor, cached);
            } catch (final AbstractFactoryException e) {
                throw logAndReturnNew(e, ConfigurationException.class);
            }

        }
        return cached;
    }

    //@Override
    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphFactory#createGraphSession(org.openspotlight.graph.persistence.SLPersistentTreeSession)
     */
    /**
     * Creates the graph session.
     * 
     * @param treeSession the tree session
     * @return the sL graph session
     */
    @Override
    SLGraphSession createGraphSession( final SLPersistentTreeSession treeSession ) {
        return new SLGraphSessionImpl(treeSession);
    }

    //@Override
    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphFactory#createNode(java.lang.Class, org.openspotlight.graph.SLContext, org.openspotlight.graph.SLNode, org.openspotlight.graph.persistence.SLPersistentNode)
     */
    /**
     * Creates the node.
     * 
     * @param clazz the clazz
     * @param context the context
     * @param parent the parent
     * @param persistentNode the persistent node
     * @return the t
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    @Override
    <T extends SLNode> T createNode( final Class<T> clazz,
                                     final SLContext context,
                                     final SLNode parent,
                                     final SLPersistentNode persistentNode ) throws SLGraphFactoryException {
        try {
            final Constructor<T> constructor = clazz.getConstructor(SLContext.class, SLNode.class, SLPersistentNode.class);
            return constructor.newInstance(context, parent, persistentNode);
        } catch (final Exception e) {
            throw new SLGraphFactoryException("Couldn't instantiate node type " + clazz.getName(), e);
        }
    }

    //@Override
    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphFactory#createNode(org.openspotlight.graph.SLContext, org.openspotlight.graph.SLNode, org.openspotlight.graph.persistence.SLPersistentNode, org.openspotlight.graph.SLGraphSessionEventPoster)
     */
    /**
     * Creates the node.
     * 
     * @param context the context
     * @param parent the parent
     * @param persistentNode the persistent node
     * @param eventPoster the event poster
     * @return the sL node
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    @Override
    SLNode createNode( final SLContext context,
                       final SLNode parent,
                       final SLPersistentNode persistentNode,
                       final SLGraphSessionEventPoster eventPoster ) throws SLGraphFactoryException {
        return new SLNodeImpl(context, parent, persistentNode, eventPoster);
    }

    //@Override
    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphFactory#createNode(org.openspotlight.graph.SLContext, org.openspotlight.graph.persistence.SLPersistentNode, org.openspotlight.graph.SLGraphSessionEventPoster)
     */
    /**
     * Creates the node.
     * 
     * @param context the context
     * @param persistentNode the persistent node
     * @param eventPoster the event poster
     * @return the sL node
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    @Override
    SLNode createNode( final SLContext context,
                       final SLPersistentNode persistentNode,
                       final SLGraphSessionEventPoster eventPoster ) throws SLGraphFactoryException {
        return new SLNodeImpl(context, null, persistentNode, eventPoster);
    }

    //@Override
    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphFactory#createProperty(org.openspotlight.graph.SLNode, org.openspotlight.graph.persistence.SLPersistentProperty)
     */
    /**
     * Creates the property.
     * 
     * @param node the node
     * @param persistentProperty the persistent property
     * @return the sL node property< v>
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    @Override
    <V extends Serializable> SLNodeProperty<V> createProperty( final SLNode node,
                                                               final SLPersistentProperty<V> persistentProperty,
                                                               final SLGraphSessionEventPoster eventPoster )
        throws SLGraphFactoryException {
        return new SLNodePropertyImpl<V>(node, persistentProperty, eventPoster);
    }

    //@Override
    /* (non-Javadoc)
     * @see org.openspotlight.graph.SLGraphFactory#getContextImplClass()
     */
    /**
     * Gets the context impl class.
     * 
     * @return the context impl class
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    @Override
    Class<? extends SLContext> getContextImplClass() throws SLGraphFactoryException {
        return SLContextImpl.class;
    }
}
