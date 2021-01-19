package com.waya.wayaauthenticationservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class Roles implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 50)
    @Column(name = "name")
    private String name;
    private String description;
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private Set<Privilege> permissions;
    @ManyToMany(mappedBy = "rolesList")
    @JsonIgnore
    private List<Users> usersList;

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
