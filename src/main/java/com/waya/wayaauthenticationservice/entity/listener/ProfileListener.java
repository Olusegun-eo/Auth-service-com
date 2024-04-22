package com.waya.wayaauthenticationservice.entity.listener;

import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ProfileHistory;
import com.waya.wayaauthenticationservice.enums.Action;

import javax.persistence.EntityManager;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.transaction.Transactional;

import static com.waya.wayaauthenticationservice.enums.Action.*;
import static javax.transaction.Transactional.TxType.MANDATORY;

public class ProfileListener {

    @PrePersist
    public void prePersist(Profile target) {
        perform(target, INSERTED);
    }

    @PreUpdate
    public void preUpdate(Profile target) {
        perform(target, UPDATED);
    }

    @PreRemove
    public void preRemove(Profile target) {
        perform(target, DELETED);
    }

    @Transactional(MANDATORY)
    public void perform(Profile target, Action action) {
        EntityManager entityManager = SpringApplicationContext.getBean(EntityManager.class);
        if(entityManager != null)
            entityManager.persist(new ProfileHistory(target, action));
    }
}
