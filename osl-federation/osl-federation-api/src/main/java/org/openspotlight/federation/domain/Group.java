package org.openspotlight.federation.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openspotlight.common.util.Arrays;
import org.openspotlight.common.util.Equals;
import org.openspotlight.common.util.HashCodes;
import org.openspotlight.federation.domain.Repository.GroupVisitor;
import org.openspotlight.persist.annotation.KeyProperty;
import org.openspotlight.persist.annotation.Name;
import org.openspotlight.persist.annotation.ParentProperty;
import org.openspotlight.persist.annotation.SimpleNodeType;
import org.openspotlight.persist.annotation.TransientProperty;

// TODO: Auto-generated Javadoc
/**
 * The Class Group.
 */
@Name( "group" )
public class Group implements SimpleNodeType, Serializable, Schedulable {

    private static final long         serialVersionUID = -722058711327567623L;

    private Set<Group>                groups           = new HashSet<Group>();

    /** The repository. */
    private Repository                repository;

    /** The type. */
    private String                    type;

    /** The name. */
    private String                    name;

    /** The active. */
    private boolean                   active;

    /** The group. */
    private Group                     group;

    private volatile int              hashCode;

    private Set<BundleProcessorType>  bundleTypes      = new HashSet<BundleProcessorType>();

    private final List<String>        cronInformation  = new ArrayList<String>();

    private volatile transient String uniqueName;

    public void acceptVisitor( final GroupVisitor visitor ) {
        visitor.visitGroup(this);
        for (final Group g : getGroups()) {
            g.acceptVisitor(visitor);
        }
    }

    public boolean equals( final Object o ) {
        if (!(o instanceof Group)) {
            return false;
        }
        final Group that = (Group)o;
        final boolean result = Equals.eachEquality(Arrays.of(group, repository,
                                                             name), Arrays.andOf(that.group, that.repository, that.name));
        return result;
    }

    public Set<BundleProcessorType> getBundleTypes() {
        return bundleTypes;
    }

    public List<String> getCronInformation() {
        return cronInformation;
    }

    /**
     * Gets the group.
     * 
     * @return the group
     */
    @ParentProperty
    public Group getGroup() {
        return group;
    }

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

    /**
     * Gets the repository.
     * 
     * @return the repository
     */
    @ParentProperty
    public Repository getRepository() {
        return repository;
    }

    @TransientProperty
    public Repository getRootRepository() {
        return repository != null ? repository : getGroup().getRootRepository();
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    public String getUniqueName() {
        String result = uniqueName;
        if (result == null) {
            result = (group != null ? group.getUniqueName() : repository
                                                                        .getName())
                     + "/" + getName();
            uniqueName = result;
        }
        return result;
    }

    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = HashCodes.hashOf(group, repository, name);
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
    public void setActive( final boolean active ) {
        this.active = active;
    }

    public void setBundleTypes( final Set<BundleProcessorType> bundleTypes ) {
        this.bundleTypes = bundleTypes;
    }

    /**
     * Sets the group.
     * 
     * @param group the new group
     */
    public void setGroup( final Group group ) {
        this.group = group;
    }

    public void setGroups( final Set<Group> groups ) {
        this.groups = groups;
    }

    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName( final String name ) {
        this.name = name;
    }

    /**
     * Sets the repository.
     * 
     * @param repository the new repository
     */
    public void setRepository( final Repository repository ) {
        this.repository = repository;
    }

    /**
     * Sets the type.
     * 
     * @param type the new type
     */
    public void setType( final String type ) {
        this.type = type;
    }

    public String toUniqueJobString() {
        return getUniqueName();
    }
}
