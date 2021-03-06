/**
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
 * OpenSpotLight - Plataforma de Governança de TI de Código Aberto
 *
 * Direitos Autorais Reservados (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA
 * EM INFORMATICA LTDA ou como contribuidores terceiros indicados pela etiqueta
 * @author ou por expressa atribuição de direito autoral declarada e atribuída pelo autor.
 * Todas as contribuições de terceiros estão distribuídas sob licença da
 * CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA.
 *
 * Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo sob os
 * termos da Licença Pública Geral Menor do GNU conforme publicada pela Free Software
 * Foundation.
 *
 * Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA
 * GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU ADEQUAÇÃO A UMA
 * FINALIDADE ESPECÍFICA. Consulte a Licença Pública Geral Menor do GNU para mais detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral Menor do GNU junto com este
 * programa; se não, escreva para:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */

package org.openspotlight.graph.query;

import java.util.Collection;

import org.openspotlight.graph.Node;
import org.openspotlight.graph.exception.SLInvalidQuerySyntaxException;

/**
 * The Interface SLQuery.
 * 
 * @author Vitor Hugo Chagas
 */
public interface Query {

    /**
     * The Enum SortMode.
     * 
     * @author Vitor Hugo Chagas
     */
    public static enum SortMode {

        /** The NOT sorted mode. */
        NOT_SORTED,

        /** The SORTED mode. */
        SORTED
    }

    /**
     * Execute.
     * 
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute()
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodes the input nodes
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(Collection<Node> inputNodes)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodes the input nodes
     * @param limit the limit
     * @param offset the offset
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(Collection<Node> inputNodes,
                                  Integer limit,
                                  Integer offset)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodes the input nodes
     * @param sortMode the sort mode
     * @param showSLQL the show slql
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(Collection<Node> inputNodes,
                                  SortMode sortMode,
                                  boolean showSLQL)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodes the input nodes
     * @param sortMode the sort mode
     * @param showSLQL the show slql
     * @param limit the limit
     * @param offset the offset
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(Collection<Node> inputNodes,
                                  SortMode sortMode,
                                  boolean showSLQL,
                                  Integer limit,
                                  Integer offset)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param limit the limit
     * @param offset the offset
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(Integer limit,
                                  Integer offset)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param sortMode the sort mode
     * @param showSLQL the show slql
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(SortMode sortMode,
                                  boolean showSLQL)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param sortMode the sort mode
     * @param showSLQL the show slql
     * @param limit the limit
     * @param offset the offset
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(SortMode sortMode,
                                  boolean showSLQL,
                                  Integer limit,
                                  Integer offset)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodesIDs the input nodes i ds
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(String[] inputNodesIDs)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodesIDs the input nodes i ds
     * @param limit the limit
     * @param offset the offset
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(String[] inputNodesIDs,
                                  Integer limit,
                                  Integer offset)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodesIDs the input nodes i ds
     * @param sortMode the sort mode
     * @param showSLQL the show slql
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(String[] inputNodesIDs,
                                  SortMode sortMode,
                                  boolean showSLQL)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

    /**
     * Execute.
     * 
     * @param inputNodesIDs the input nodes i ds
     * @param sortMode the sort mode
     * @param showSLQL the show slql
     * @param limit the limit
     * @param offset the offset
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(String[] inputNodesIDs,
                                  SortMode sortMode,
                                  boolean showSLQL,
                                  Integer limit,
                                  Integer offset)
        throws InvalidQuerySyntaxException, InvalidQueryElementException, QueryException;

}
