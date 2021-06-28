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

    public Roles(Integer id,String name) {
        this.id = id;
        this.name = name;
    }

    public Roles(Integer id) {
        this.id = id;

    }
}
