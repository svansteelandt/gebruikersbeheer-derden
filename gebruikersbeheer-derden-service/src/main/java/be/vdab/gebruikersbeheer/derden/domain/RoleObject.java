package be.vdab.gebruikersbeheer.derden.domain;


import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * RoleObject - Wrapper class for Role object of the TIM API&#46; <br>
 */
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoleObject implements Serializable, Comparable<RoleObject> {

	@EqualsAndHashCode.Include
	private Dn dn;
	private String roleName;
	private String roleDescription;
	private String vdabRoleName;
	private String vdabRoleDescription;
	private boolean hasRole;
	private boolean pending;
	private boolean changed;
	private boolean available;
	private boolean adminRole;
	private boolean dynamicRole;
	private boolean needsApproval;
	private Set<String> tags;
	private Set<Dn> owners;

	public String getVdabRoleName() {
		return vdabRoleName;
	}

	public void setVdabRoleName(String vdabRoleName) {
		this.vdabRoleName = vdabRoleName;
	}

	public String getVdabRoleDescription() {
		return vdabRoleDescription;
	}

	public void setVdabRoleDescription(String vdabRoleDescription) {
		this.vdabRoleDescription = vdabRoleDescription;
	}

	public boolean isAdminRole() {
		return adminRole;
	}

	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

	public boolean isDynamicRole() {
		return dynamicRole;
	}

	public void setDynamicRole(boolean dynamicRole) {
		this.dynamicRole = dynamicRole;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public boolean getHasRole() {
		return hasRole;
	}

	public void setHasRole(boolean hasRole) {
		if (this.hasRole != hasRole) {
			this.changed = true;
		}
		this.hasRole = hasRole;
	}

	public boolean isNeedsApproval() {
		return needsApproval;
	}

	public void setNeedsApproval(boolean needsApproval) {
		this.needsApproval = needsApproval;
	}

	public RoleObject() {
		this.available = true;
		this.adminRole = false;
		this.tags = new LinkedHashSet<>();
	}

	public RoleObject(Dn dn) {
		this.adminRole = false;
		this.available = true;
		this.dn = dn;
		this.tags = new LinkedHashSet<>();
	}

	/**
	 * copy constructor
	 */
	public RoleObject(RoleObject other) {
		this.dn = other.dn;
		this.roleName = other.roleName;
		this.roleDescription = other.roleName;
		this.vdabRoleName = other.vdabRoleName;
		this.vdabRoleDescription = other.vdabRoleDescription;
		this.hasRole = other.hasRole;
		this.pending = other.pending;
		this.changed = other.changed;
		this.available = other.available;
		this.adminRole = other.adminRole;
		this.needsApproval = other.needsApproval;
		this.dynamicRole = other.dynamicRole;
		this.tags = other.tags;
		this.owners = other.owners;
	}

	public Dn getDn() {
		return dn;
	}

	public void setDn(Dn dn) {
		this.dn = dn;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	public @NonNull Set<String> getTags() {
		return tags;
	}

	public String getGlobalId() {
		if (dn != null) {
			return dn.getGlobalId();
		} else {
			return null;
		}
	}

	public void setTags(Set<String> tags) {
		this.tags = tags != null ? new LinkedHashSet<>(tags) : new LinkedHashSet<>();
	}

	public void addTag(String tag) {
		if (tags == null) {
			tags = new HashSet<>();
		}
		this.tags.add(tag);
	}

	public Set<Dn> getOwners() {
		return owners;
	}

	public void setOwners(Set<Dn> owners) {
		this.owners = owners;
	}

	public void addOwner(Dn loggedInDn) {
		if (this.owners == null) {
			owners = new HashSet<>();
		}
		this.owners.add(loggedInDn);
	}

	@Override
	public int compareTo(@NonNull RoleObject roleObject) {
		return StringUtils.compare(vdabRoleName, roleObject.vdabRoleName);
	}

}
