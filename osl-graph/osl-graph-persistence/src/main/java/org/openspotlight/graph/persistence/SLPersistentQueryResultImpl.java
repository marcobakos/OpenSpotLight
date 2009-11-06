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
package org.openspotlight.graph.persistence;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

/**
 * The Class SLPersistentQueryResultImpl.
 * 
 * @author Vitor Hugo Chagas
 */
public class SLPersistentQueryResultImpl implements SLPersistentQueryResult {

    /** The tree session. */
    private SLPersistentTreeSession treeSession;

    /** The query result. */
    private QueryResult             queryResult;

    /**
     * Instantiates a new sL persistent query result impl.
     * 
     * @param treeSession the tree session
     * @param queryResult the query result
     */
    public SLPersistentQueryResultImpl(
                                        SLPersistentTreeSession treeSession, QueryResult queryResult ) {
        this.treeSession = treeSession;
        this.queryResult = queryResult;
    }

    /** The persistent nodes. */
    private Collection<SLPersistentNode> persistentNodes;

    //@Override
    /* (non-Javadoc)
     * @see org.openspotlight.graph.persistence.SLPersistentQueryResult#getNodes()
     */
    public Collection<SLPersistentNode> getNodes() throws SLPersistentTreeSessionException {
        if (persistentNodes == null) {
            try {
                persistentNodes = new ArrayList<SLPersistentNode>();
                NodeIterator iter = queryResult.getNodes();
                persistentNodes = new ArrayList<SLPersistentNode>();
                while (iter.hasNext()) {
                    Node node = iter.nextNode();
                    String[] names = node.getPath().split("/");
                    SLPersistentNode persistentNode = null;
                    for (int i = 2; i < names.length; i++) {
                        if (names[i].trim().equals("")) continue;
                        if (persistentNode == null) {
                            persistentNode = treeSession.getRootNode();
                        } else {
                            persistentNode = persistentNode.getNode(names[i]);
                        }
                    }
                    persistentNodes.add(persistentNode);
                }
            } catch (Exception e) {
                throw new SLPersistentTreeSessionException("Error on attempt to retrieve nodes from persistent query result.", e);
            }
        }
        return persistentNodes;
    }

    /* (non-Javadoc)
     * @see org.openspotlight.graph.persistence.SLPersistentQueryResult#getRowCount()
     */
    public int getRowCount() throws SLPersistentTreeSessionException {
        try {
            return (int)queryResult.getNodes().getSize();
        } catch (RepositoryException e) {
            throw new SLPersistentTreeSessionException("Error on attempt to retrieve query result size.", e);
        }
    }
}
