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
package org.openspotlight.domain;

import static org.openspotlight.common.util.Arrays.andOf;
import static org.openspotlight.common.util.Arrays.of;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.openspotlight.common.util.Equals;
import org.openspotlight.common.util.HashCodes;
import org.openspotlight.federation.domain.artifact.ArtifactSource;
import org.openspotlight.persist.annotation.KeyProperty;
import org.openspotlight.persist.annotation.Name;
import org.openspotlight.persist.annotation.SimpleNodeType;

// TODO: Auto-generated Javadoc

/**
 * The Class Repository.
 */
@Name("repository")
public class Repository implements SimpleNodeType, Serializable {

    public static Builder newRepositoryNamed(String name) {
        return new Builder(name);
    }

    public static class Builder {

        private ArtifactSourceMapping currentMapping;

        private Builder(String name) {
            repository = new Repository();
            repository.setName(name);
            repository.setActive(true);

        }

        private Repository repository;

        private Group currentGroup;

        public Repository andCreate(){
            return repository;
        }

        public Builder withGroup(String groupName) {
            currentGroup = new Group();
            currentGroup.setActive(true);
            currentGroup.setName(groupName);
            repository.getGroups().add(currentGroup);
            return this;
        }

        public Builder withSubGroup(String groupName) {

            Group group = new Group();
            group.setActive(true);
            group.setName(groupName);
            currentGroup.getGroups().add(currentGroup);
            group.setGroup(currentGroup);
            currentGroup = group;
            return this;
        }

        public Builder withArtifactSource(String artifactSourceName, String rootFolder, String initialLookup) {
            ArtifactSource source = new ArtifactSource();
            source.setActive(true);
            source.setName(artifactSourceName);
            source.setInitialLookup(initialLookup);
            source.setRootFolder(rootFolder);
            ArtifactSourceMapping mapping = new ArtifactSourceMapping();
            mapping.getIncludeds().add("**/*");
            source.getMappings().add(mapping);
            this.currentGroup.getArtifactSources().add(source);
            this.currentMapping = mapping;
            return this;
        }

        public Builder withBundleConfig(String name, Class<? extends Callable<Void>>... taskType) {
            BundleConfig bundleProcessorType = new BundleConfig();
            bundleProcessorType.setActive(true);
            bundleProcessorType.setName(name);
            for (Class<? extends Callable<Void>> t : taskType)
                bundleProcessorType.getTasks().add(t);
            currentMapping.getBundleConfig().add(bundleProcessorType);
            return this;
        }


    }

    public static interface GroupVisitor {
        public void visitGroup(Group group);
    }

    private static final long serialVersionUID = -8278810189446649901L;


    /**
     * The name.
     */
    private String name;

    /**
     * The groups.
     */
    private Set<Group> groups = new HashSet<Group>();

    /**
     * The active.
     */
    private boolean active;

    private volatile transient int hashCode;

    public void acceptGroupVisitor(final GroupVisitor visitor) {
        for (final Group entry : getGroups()) {
            entry.acceptVisitor(visitor);
        }
    }

    public boolean equals(final Object o) {
        if (!(o instanceof Repository)) {
            return false;
        }
        final Repository that = (Repository) o;
        return Equals.eachEquality(of(name), andOf(that.name));
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public Set<Group> getGroups() {
        return groups;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @KeyProperty
    public String getName() {
        return name;
    }

    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = HashCodes.hashOf(name);
            hashCode = result;
        }
        return result;
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active.
     *
     * @param active the new active
     */
    public void setActive(final boolean active) {
        this.active = active;
    }


    public void setGroups(final Set<Group> groups) {
        this.groups = groups;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
	public void setName(final String name) {
		this.name = name;
	}

}