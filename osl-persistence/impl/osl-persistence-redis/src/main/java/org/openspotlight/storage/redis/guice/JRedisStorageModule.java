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

package org.openspotlight.storage.redis.guice;

import java.util.Map;

import org.openspotlight.storage.DefaultPartitionFactory;
import org.openspotlight.storage.Partition;
import org.openspotlight.storage.PartitionFactory;
import org.openspotlight.storage.StorageSession;
import org.openspotlight.storage.StorageSessionProvider;
import org.openspotlight.storage.engine.StorageEngineBind;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * Created by User: feu - Date: Mar 23, 2010 - Time: 4:41:43 PM
 */
public class JRedisStorageModule extends AbstractModule {

    private final StorageSession.FlushMode           flushMode;

    private final Map<Partition, JRedisServerDetail> mappedServerConfig;

    private final PartitionFactory                   partitionFactory;

    public JRedisStorageModule(final StorageSession.FlushMode flushMode,
                               final Map<Partition, JRedisServerDetail> mappedServerConfig) {
        this.flushMode = flushMode;
        this.mappedServerConfig = mappedServerConfig;
        partitionFactory = new DefaultPartitionFactory();
    }

    public JRedisStorageModule(final StorageSession.FlushMode flushMode,
                               final Map<Partition, JRedisServerDetail> mappedServerConfig,
                               final PartitionFactory partitionFactory) {
        this.flushMode = flushMode;
        this.mappedServerConfig = mappedServerConfig;
        if (partitionFactory == null) {
            this.partitionFactory = new DefaultPartitionFactory();
        } else {
            this.partitionFactory = partitionFactory;
        }
    }

    @Override
    protected void configure() {
        bind(PartitionFactory.class).toInstance(partitionFactory);
        bind(StorageSession.class).toProvider(StorageSessionProvider.class);
        bind(StorageEngineBind.class).toProvider(JRedisStorageSessionProvider.class);
        bind(StorageSession.FlushMode.class).toInstance(flushMode);
        bind(new TypeLiteral<Map<Partition, JRedisServerDetail>>() {}).toInstance(mappedServerConfig);
        bind(JRedisFactory.class).to(JRedisFacoryImpl.class);
        bind(boolean.class).annotatedWith(StartRedisLocally.class).toInstance(true);
    }
}
