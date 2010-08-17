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
package org.openspotlight.storage.domain.node;

import org.openspotlight.storage.Partition;
import org.openspotlight.storage.StringIDSupport;
import org.openspotlight.storage.domain.Link;
import org.openspotlight.storage.domain.Node;

public class LinkImpl extends PropertyContainerImpl implements
        Link {

    private static final long serialVersionUID = -3462836679437486046L;

    public LinkImpl(final String linkType, final Node origin,
                    final Node target, final boolean resetTimeout) {
        super(resetTimeout);
        this.linkType = linkType;
        this.origin = origin;
        this.target = target;
        originPartition = origin.getPartition();
        linkId = StringIDSupport.getLinkKeyAsString(originPartition,
                linkType, origin, target);
    }

    private final String    linkId;

    private final String    linkType;

    private final Node      origin;

    private final Node      target;

    private final Partition originPartition;

    @Override
    public String getLinkId() {
        return linkId;
    }

    @Override
    public String getLinkType() {
        return linkType;
    }

    @Override
    public Node getOrigin() {
        return origin;
    }

    @Override
    public Node getTarget() {
        return target;
    }

    @Override
    public String getKeyAsString() {
        return linkId;
    }

    @Override
    public Partition getPartition() {
        return originPartition;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) { return true; }
        if (!(o instanceof Link)) { return false; }
        return getKeyAsString().equals(((Link) o).getKeyAsString());
    }

    @Override
    public int hashCode() {
        return getKeyAsString().hashCode();
    }

}
