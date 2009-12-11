package org.openspotlight.federation.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Session;

import org.openspotlight.common.LazyType;
import org.openspotlight.common.SharedConstants;
import org.openspotlight.common.exception.SLRuntimeException;
import org.openspotlight.common.util.Arrays;
import org.openspotlight.common.util.Equals;
import org.openspotlight.common.util.Exceptions;
import org.openspotlight.common.util.HashCodes;
import org.openspotlight.federation.domain.Group;
import org.openspotlight.federation.domain.Repository;
import org.openspotlight.persist.annotation.KeyProperty;
import org.openspotlight.persist.annotation.ParentProperty;
import org.openspotlight.persist.annotation.SimpleNodeType;
import org.openspotlight.persist.support.SimplePersistSupport;
import org.openspotlight.persist.util.SimpleNodeTypeVisitorSupport;

public class GroupSupport {

	public static class GroupDifferences implements SimpleNodeType,
			Serializable {

		private String repositoryName;

		private RepositoryGroupDifferences repositoryGroupDifferences;

		/**
		 * 
		 */
		private static final long serialVersionUID = 6595595697385787637L;

		private final Set<String> addedGroups = new HashSet<String>();

		private final Set<String> removedGroups = new HashSet<String>();

		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof GroupDifferences)) {
				return false;
			}
			final GroupDifferences that = (GroupDifferences) obj;
			return Equals
					.eachEquality(Arrays.of(getRepositoryName(),
							getRepositoryGroupDifferences()), Arrays.andOf(that
							.getRepositoryName(), that
							.getRepositoryGroupDifferences()));
		}

		public Set<String> getAddedGroups() {
			return addedGroups;
		}

		public Set<String> getRemovedGroups() {
			return removedGroups;
		}

		@ParentProperty
		public RepositoryGroupDifferences getRepositoryGroupDifferences() {
			return repositoryGroupDifferences;
		}

		@KeyProperty
		public String getRepositoryName() {
			return repositoryName;
		}

		@Override
		public int hashCode() {
			return HashCodes.hashOf(getClass(), repositoryName);
		}

		public void setRepositoryGroupDifferences(
				final RepositoryGroupDifferences repositoryGroupDifferences) {
			this.repositoryGroupDifferences = repositoryGroupDifferences;
		}

		public void setRepositoryName(final String repositoryName) {
			this.repositoryName = repositoryName;
		}

	}

	public static class RepositoryGroupDifferences implements SimpleNodeType,
			Serializable {
		/**
	 * 
	 */
		private static final long serialVersionUID = -7604798465423650188L;
		private Map<String, GroupDifferences> differencesByRepository = new HashMap<String, GroupDifferences>();

		@Override
		public boolean equals(final Object obj) {
			return obj instanceof RepositoryGroupDifferences;
		}

		public Map<String, GroupDifferences> getDifferencesByRepository() {
			return differencesByRepository;
		}

		@Override
		public int hashCode() {
			return HashCodes.hashOf(getClass());
		}

		public void setDifferencesByRepository(
				final Map<String, GroupDifferences> differencesByRepository) {
			this.differencesByRepository = differencesByRepository;
		}

	}

	private static String ROOT_NODE = SharedConstants.DEFAULT_JCR_ROOT_NAME
			+ "/differences";

	private static GroupDifferences createDifferences(
			final RepositoryGroupDifferences differences,
			final Repository newOne, final Set<Group> newGroups,
			final Set<Group> oldGroups) {
		GroupDifferences groupDifferences = differences
				.getDifferencesByRepository().get(newOne.getName());
		if (groupDifferences == null) {
			groupDifferences = new GroupDifferences();
			groupDifferences.setRepositoryGroupDifferences(differences);
			groupDifferences.setRepositoryName(newOne.getName());
			differences.getDifferencesByRepository().put(newOne.getName(),
					groupDifferences);
		}
		groupDifferences = findChangesOnNewGroups(groupDifferences, oldGroups,
				newGroups);
		return groupDifferences;
	}

	public static Set<Group> findAllGroups(final Repository repository) {
		final Set<Group> groups = new HashSet<Group>();
		final AggregateVisitor<Group> visitor = new AggregateVisitor<Group>(
				groups);
		SimpleNodeTypeVisitorSupport.<Group> acceptVisitorOn(Group.class,
				repository, visitor);
		return groups;
	}

	private static GroupDifferences findChangesOnNewGroups(
			final GroupDifferences differences, final Set<Group> oldGroups,
			final Set<Group> newOnes) {

		for (final Group newOne : newOnes) {
			if (!oldGroups.contains(newOne)) {
				differences.getAddedGroups().add(newOne.getUniqueName());
			}
		}
		for (final Group oldOne : oldGroups) {
			if (!newOnes.contains(oldOne)) {
				differences.getRemovedGroups().add(oldOne.getUniqueName());
			}
		}
		return differences;
	}

	public static RepositoryGroupDifferences findDifferencesOnAllRepositories(
			final RepositoryGroupDifferences differences,
			final Set<Repository> oldRepositories,
			final Set<Repository> newRepositories) {
		final Set<Repository> copyOfOldOnes = new HashSet<Repository>(
				oldRepositories);
		loopingOnNewOnes: for (final Repository newOne : newRepositories) {
			copyOfOldOnes.remove(newOne);
			final Set<Group> newGroups = findAllGroups(newOne);
			for (final Repository oldOne : oldRepositories) {
				if (oldOne.equals(newOne)) {
					final Set<Group> oldGroups = findAllGroups(oldOne);
					createDifferences(differences, newOne, newGroups, oldGroups);
					continue loopingOnNewOnes;
				}
			}
			createDifferences(differences, newOne, newGroups, Collections
					.<Group> emptySet());
		}
		for (final Repository excluded : copyOfOldOnes) {
			final Set<Group> oldGroups = findAllGroups(excluded);
			createDifferences(differences, excluded, Collections
					.<Group> emptySet(), oldGroups);
		}
		return differences;
	}

	public static RepositoryGroupDifferences getDifferences(
			final Session session) {
		final Set<RepositoryGroupDifferences> result = SimplePersistSupport
				.findNodesByProperties(ROOT_NODE, session,
						RepositoryGroupDifferences.class, LazyType.EAGER,
						new String[0], new Object[0]);
		if (result.size() > 0) {
			return result.iterator().next();
		}
		return null;

	}

	public static void saveDifferences(final Session session,
			final RepositoryGroupDifferences differences) {
		try {
			SimplePersistSupport.convertBeanToJcr(ROOT_NODE, session,
					differences);
			session.save();
		} catch (final Exception e) {
			throw Exceptions.logAndReturnNew(e, SLRuntimeException.class);
		}
	}

	private GroupSupport() {
	}

}
