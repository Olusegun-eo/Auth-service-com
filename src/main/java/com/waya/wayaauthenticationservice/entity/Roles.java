package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "m_roles")
public class Roles implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Basic(optional = false)
	@Column(name = "id")
	private Long id;

	@Size(max = 50)
	@Column(name = "name", unique = true)
	private String name;

	private String description;

	@OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
	private Collection<Privilege> permissions;

//    @ManyToMany(mappedBy = "rolesList")
//    @JsonIgnore
//    private List<Users> usersList;

	public Roles() {
	}

	public Roles(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Roles(Long id) {
		this.id = id;

	}

	public Roles(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Roles other = (Roles) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equalsIgnoreCase(other.name))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
}
