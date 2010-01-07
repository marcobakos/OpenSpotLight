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
package org.openspotlight.bundle.db.processor;

import static org.openspotlight.bundle.db.processor.DbProcessorHelper.createParentNodes;

import java.util.Set;

import org.openspotlight.bundle.db.processor.DbProcessorHelper.TableParentVo;
import org.openspotlight.bundle.db.processor.wrapped.WrappedTypeFactory;
import org.openspotlight.common.util.Collections;
import org.openspotlight.federation.context.ExecutionContext;
import org.openspotlight.federation.domain.artifact.db.ConstraintArtifact;
import org.openspotlight.federation.domain.artifact.db.DatabaseCustomArtifact;
import org.openspotlight.federation.domain.artifact.db.TableArtifact;
import org.openspotlight.federation.processing.ArtifactChanges;
import org.openspotlight.federation.processing.ArtifactsToBeProcessed;
import org.openspotlight.federation.processing.BundleProcessorGlobalPhase;
import org.openspotlight.federation.processing.CurrentProcessorContext;
import org.openspotlight.federation.processing.SaveBehavior;
import org.openspotlight.graph.SLNode;

public class DbArtifactGlobalProcessor implements
		BundleProcessorGlobalPhase<DatabaseCustomArtifact> {

	public void didFinishProcessing(
			final ArtifactChanges<DatabaseCustomArtifact> changes) {

	}

	@SuppressWarnings("unchecked")
	public Set<Class<? extends DatabaseCustomArtifact>> getArtifactTypes() {
		return Collections.setOf(TableArtifact.class, ConstraintArtifact.class);
	}

	public SaveBehavior getSaveBehavior() {
		return SaveBehavior.PER_PROCESSING;
	}

	public void selectArtifactsToBeProcessed(
			final CurrentProcessorContext currentContext,
			final ExecutionContext context,
			final ArtifactChanges<DatabaseCustomArtifact> changes,
			final ArtifactsToBeProcessed<DatabaseCustomArtifact> toBeReturned)
			throws Exception {
		for (final DatabaseCustomArtifact artifact : changes
				.getExcludedArtifacts()) {
			if (artifact instanceof TableArtifact) {
				final TableArtifact tableArtifact = (TableArtifact) artifact;
				final DbWrappedType wrappedType = WrappedTypeFactory.INSTANCE
						.createByType(tableArtifact.getDatabaseType());
				final TableParentVo parent = createParentNodes(wrappedType,
						tableArtifact, currentContext, context);
				final SLNode tableNode = parent.tableParent
						.getNode(tableArtifact.getTableName());
				if (tableNode != null) {
					tableNode.remove();
				}
			}
		}

	}

}
