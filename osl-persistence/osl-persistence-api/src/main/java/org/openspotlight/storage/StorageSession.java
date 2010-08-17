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


package org.openspotlight.storage;

import org.openspotlight.storage.Criteria.CriteriaBuilder;
import org.openspotlight.storage.domain.Link;
import org.openspotlight.storage.domain.Node;
import org.openspotlight.storage.domain.NodeFactory;
import org.openspotlight.storage.domain.key.NodeKey;

/**
 * This class is an abstraction of a current state of storage session. The implementation classes must not store any kind of
 * connection state. This implementation must not be shared between threads.
 */
public interface StorageSession {

    public RepositoryPath getRepositoryPath();

    PartitionMethods withPartition(Partition partition);

    interface PartitionMethods extends NodeFactory {

        Iterable<String> getAllNodeTypes();

        NodeKeyBuilder createKey(String nodeType);

        Iterable<Node> findByCriteria(Criteria criteria);

        Iterable<Node> findByType(String nodeType);

        Node findUniqueByCriteria(Criteria criteria);

        public CriteriaBuilder createCriteria();

        NodeBuilder createWithType(String nodeType);

        NodeKey createNewSimpleKey(String... nodePaths);

        Node createNewSimpleNode(String... nodePaths);

    }

    Node findNodeByStringId(String idAsString);

    void removeNode(Node stNodeEntry);

    static enum FlushMode {
        AUTO,
        EXPLICIT
    }

    FlushMode getFlushMode();

    interface NodeKeyBuilder {

        NodeKeyBuilder withSimpleKey(String keyName,
                                      String value);

        NodeKeyBuilder withParent(Partition partition,
                                       String nodeType);

        NodeKeyBuilder withParent(String parentId);

        NodeKey andCreate();

    }

    void discardTransient();

    void flushTransient();

    Link addLink(Node origin,
                         Node destiny,
                         String name);

    void removeLink(Node origin,
                     Node destiny,
                     String name);

    void removeLink(Link link);

    Iterable<Link> findLinks(Node origin);

    Iterable<Link> findLinks(Node origin,
                                     String type);

    Link getLink(Node origin,
                         Node destiny,
                         String name);

    Iterable<Link> findLinks(Node origin,
                                     Node destiny);

}
