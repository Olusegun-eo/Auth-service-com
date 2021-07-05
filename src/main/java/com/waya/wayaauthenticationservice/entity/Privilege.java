package com.waya.wayaauthenticationservice.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "m_privilege")
@JsonIgnoreType
public class Privilege implements Serializable {
	
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "role_id",
            nullable = false,
            referencedColumnName = "id"
    )
    @JsonIgnore
    private Roles role;

    public Privilege(long id) {
        this.id =id;
    }

    public Privilege(){}
}
