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

package org.openspotlight.storage.engine;

import java.util.Set;

import org.openspotlight.common.Disposable;
import org.openspotlight.storage.NodeCriteria;
import org.openspotlight.storage.Partition;
import org.openspotlight.storage.domain.Property;
import org.openspotlight.storage.domain.PropertyContainer;
import org.openspotlight.storage.domain.StorageLink;
import org.openspotlight.storage.domain.StorageNode;

public interface StorageEngineBind<R> extends Disposable {

    R createLinkReferenceIfNecessary(Partition partition, StorageLink entry);

    R createNodeReferenceIfNecessary(Partition partition, StorageNode entry);

    void flushNewItem(R reference, Partition partition, StorageNode entry)
        throws Exception;

    void flushRemovedItem(Partition partition, StorageNode entry)
        throws Exception;

    void flushRemovedLink(Partition partition, StorageLink link)
        throws Exception;

    void handleNewLink(Partition partition, StorageNode origin, StorageLink link)
        throws Exception;

    Iterable<StorageNode> findByCriteria(Partition partition, NodeCriteria criteria)
        throws Exception;

    Iterable<StorageNode> findByType(Partition partition, String nodeType)
        throws Exception;

    Iterable<StorageLink> findLinks(Partition partition, StorageNode origin, StorageNode destiny, String type)
        throws Exception;

    void flushSimpleProperty(R reference, Partition partition, Property dirtyProperty)
        throws Exception;

    Iterable<String> getAllNodeTypes(Partition partition)
        throws Exception;

    StorageNode getNode(String key)
        throws Exception;

    Iterable<StorageNode> getChildren(Partition partition, StorageNode StorageNode)
        throws Exception;

    Iterable<StorageNode> getChildrenByType(Partition partition, StorageNode StorageNode, String type)
        throws Exception;

    StorageNode getParent(Partition partition, StorageNode StorageNode)
        throws Exception;

    Set<Property> loadProperties(R reference, Partition partition, PropertyContainer StorageNode)
        throws Exception;

    byte[] getPropertyValue(Partition partition, Property stProperty)
        throws Exception;

    void savePartitions(Partition... partitions)
        throws Exception;

}