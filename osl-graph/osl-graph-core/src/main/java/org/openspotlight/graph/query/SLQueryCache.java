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
package org.openspotlight.graph.query;

import java.util.Collection;
import java.util.List;

import org.openspotlight.common.exception.SLException;
import org.openspotlight.graph.SLGraphSessionException;
import org.openspotlight.graph.SLNodeNotFoundException;
import org.openspotlight.graph.persistence.SLPersistentTreeSessionException;
import org.openspotlight.graph.query.SLQuery.SortMode;

/**
 * The Interface SLQueryCache.
 * 
 * @author porcelli
 */
public interface SLQueryCache {

    /**
     * Builds a unique query id.
     * 
     * @param selects the selects
     * @param collatorStrength the collator strength
     * @param inputNodesIDs the input nodes i ds
     * @param sortMode the sort mode
     * @param limit the limit
     * @param offset the offset
     * @return the string
     * @throws SLException the SL exception
     */
    public abstract String buildQueryId( final List<SLSelect> selects,
                                         final Integer collatorStrength,
                                         final String[] inputNodesIDs,
                                         final SortMode sortMode,
                                         final Integer limit,
                                         final Integer offset ) throws SLException;

    /**
     * Gets the cache content. Returns null if not found.
     * 
     * @param queryId the query id
     * @return the cache
     * @throws SLPersistentTreeSessionException the SL persistent tree session exception
     * @throws SLNodeNotFoundException the SL node not found exception
     * @throws SLGraphSessionException the SL graph session exception
     */
    public abstract SLQueryResult getCache( final String queryId )
        throws SLPersistentTreeSessionException, SLNodeNotFoundException, SLGraphSessionException;

    /**
     * Adds content to the cache.
     * 
     * @param queryId the query id
     * @param nodes the nodes
     * @throws SLPersistentTreeSessionException the SL persistent tree session exception
     * @throws SLNodeNotFoundException the SL node not found exception
     * @throws SLGraphSessionException the SL graph session exception
     */
    public abstract void add2Cache( final String queryId,
                                    final Collection<PNodeWrapper> nodes )
        throws SLPersistentTreeSessionException, SLNodeNotFoundException, SLGraphSessionException;

}