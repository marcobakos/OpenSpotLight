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
import org.openspotlight.graph.manipulation.GraphReader;
import org.openspotlight.graph.query.Query.SortMode;

/**
 * The Class AbstractSLQuery. Basic implemenation for SLQueries (api or text).
 * 
 * @author porcelli
 */
public abstract class AbstractSLQuery {

    /** The session. */
    protected GraphReader session;

    /**
     * Instantiates a new sL query impl.
     * 
     * @param session the session
     * @param treeSession the tree session
     */
    public AbstractSLQuery(final GraphReader session) {
        this.session = session;
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
        throws SLInvalidQuerySyntaxException,
            InvalidQueryElementException, QueryException {
        return this.execute((String[]) null, SortMode.NOT_SORTED, false, null,
                null);
    }

    /**
     * Execute.
     * 
     * @param inputNodes the input nodes
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(final Collection<Node> inputNodes)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute(QuerySupport.getNodeIDs(inputNodes),
                SortMode.NOT_SORTED, false, null, null);
    }

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
    public QueryResult execute(final Collection<Node> inputNodes,
                               final Integer limit, final Integer offset)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute(QuerySupport.getNodeIDs(inputNodes),
                SortMode.NOT_SORTED, false, limit, offset);
    }

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
    public QueryResult execute(final Collection<Node> inputNodes,
                               final SortMode sortMode, final boolean showSLQL)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute(QuerySupport.getNodeIDs(inputNodes),
                SortMode.NOT_SORTED, false, null, null);
    }

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
    public QueryResult execute(final Collection<Node> inputNodes,
                               final SortMode sortMode, final boolean showSLQL,
                               final Integer limit, final Integer offset)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute(QuerySupport.getNodeIDs(inputNodes),
                SortMode.NOT_SORTED, false, limit, offset);
    }

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
    public QueryResult execute(final Integer limit, final Integer offset)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute((String[]) null, SortMode.NOT_SORTED, false, limit,
                offset);
    }

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
    public QueryResult execute(final SortMode sortMode, final boolean showSLQL)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute((String[]) null, sortMode, showSLQL, null, null);
    }

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
    public QueryResult execute(final SortMode sortMode,
                               final boolean showSLQL, final Integer limit, final Integer offset)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute((String[]) null, sortMode, showSLQL, limit, offset);
    }

    /**
     * Execute.
     * 
     * @param inputNodesIDs the input nodes i ds
     * @return the sL query result
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     * @throws InvalidQueryElementException the SL invalid query element exception
     * @throws QueryException the SL query exception
     */
    public QueryResult execute(final String[] inputNodesIDs)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute(inputNodesIDs, SortMode.NOT_SORTED, false, null,
                null);
    }

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
    public QueryResult execute(final String[] inputNodesIDs,
                               final Integer limit, final Integer offset)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute(inputNodesIDs, SortMode.NOT_SORTED, false, limit,
                offset);
    }

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
    public QueryResult execute(final String[] inputNodesIDs,
                               final SortMode sortMode, final boolean showSLQL)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException {
        return this.execute(inputNodesIDs, SortMode.NOT_SORTED, false, null,
                null);
    }

    public abstract QueryResult execute(String[] inputNodesIDs,
                                        SortMode sortMode, boolean showSLQL, Integer limit, Integer offset)
            throws SLInvalidQuerySyntaxException, InvalidQueryElementException,
            QueryException;

}
