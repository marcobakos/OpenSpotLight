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

import java.io.Serializable;

import org.openspotlight.common.util.AbstractFactory;
import org.openspotlight.graph.persistence.SLPersistentNode;
import org.openspotlight.graph.persistence.SLPersistentProperty;
import org.openspotlight.graph.persistence.SLPersistentTreeSession;
import org.openspotlight.jcr.provider.JcrConnectionDescriptor;
import org.openspotlight.security.authz.PolicyEnforcement;
import org.openspotlight.security.idm.AuthenticatedUser;

/**
 * A factory for creating SLGraph objects.
 */
public abstract class SLGraphFactory extends AbstractFactory {

    /**
     * Creates a new SLGraph object.
     * 
     * @param descriptor the descriptor
     * @return the SL graph
     * @throws SLGraphFactoryException the SL graph factory exception
     * @throws SLInvalidCredentialsException the SL invalid credentials exception
     */
    public abstract SLGraph createGraph( final JcrConnectionDescriptor descriptor )
        throws SLGraphFactoryException, SLInvalidCredentialException;

    /**
     * Creates the graph session.
     * 
     * @param treeSession the tree session
     * @param user the user
     * @param policyEnforcement the policy enforcement
     * @return the SL graph session
     * @throws SLGraphFactoryException the SL graph factory exception
     * @throws SLInvalidCredentialsException the SL invalid credentials exception
     */
    abstract SLGraphSession createGraphSession( SLPersistentTreeSession treeSession,
                                                PolicyEnforcement policyEnforcement,
                                                AuthenticatedUser user )
        throws SLGraphFactoryException, SLInvalidCredentialException;

    /**
     * Creates a new SLGraph object.
     * 
     * @param clazz the clazz
     * @param context the context
     * @param parent the parent
     * @param persistentNode the persistent node
     * @return the T
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    abstract <T extends SLNode> T createNode( Class<T> clazz,
                                              SLContext context,
                                              SLNode parent,
                                              SLPersistentNode persistentNode ) throws SLGraphFactoryException;

    /**
     * Creates a new SLGraph object.
     * 
     * @param context the context
     * @param parent the parent
     * @param persistentNode the persistent node
     * @param eventPoster the event poster
     * @return the SL node
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    abstract SLNode createNode( SLContext context,
                                SLNode parent,
                                SLPersistentNode persistentNode,
                                SLGraphSessionEventPoster eventPoster ) throws SLGraphFactoryException;

    /**
     * Creates a new SLGraph object.
     * 
     * @param context the context
     * @param persistentNode the persistent node
     * @param eventPoster the event poster
     * @return the SL node
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    abstract SLNode createNode( SLContext context,
                                SLPersistentNode persistentNode,
                                SLGraphSessionEventPoster eventPoster ) throws SLGraphFactoryException;

    /**
     * Creates a new SLGraph object.
     * 
     * @param node the node
     * @param persistentProperty the persistent property
     * @param eventPoster the event poster
     * @return the SL node property< v>
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    abstract <V extends Serializable> SLNodeProperty<V> createProperty( SLNode node,
                                                                        SLPersistentProperty<V> persistentProperty,
                                                                        SLGraphSessionEventPoster eventPoster )
        throws SLGraphFactoryException;

    /**
     * Gets the context impl class.
     * 
     * @return the context impl class
     * @throws SLGraphFactoryException the SL graph factory exception
     */
    abstract Class<? extends SLContext> getContextImplClass() throws SLGraphFactoryException;

}
