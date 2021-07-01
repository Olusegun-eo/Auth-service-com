package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;
<<<<<<< HEAD
import java.util.Set;
=======
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Data;
>>>>>>> 5b32112750c7ea61ccac03db912e4eef40653d63

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Data
@Table(name = "m_roles")
public class Roles implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;
	
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

	public Roles(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Roles(Integer id) {
		this.id = id;

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
		} else if (!name.equals(other.name))
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
