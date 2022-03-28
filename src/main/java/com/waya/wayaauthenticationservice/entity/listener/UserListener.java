package com.waya.wayaauthenticationservice.entity.listener;

import com.waya.wayaauthenticationservice.SpringApplicationContext;
import com.waya.wayaauthenticationservice.entity.UserHistory;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.Action;

import javax.persistence.EntityManager;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.transaction.Transactional;

import static com.waya.wayaauthenticationservice.enums.Action.*;
import static javax.transaction.Transactional.TxType.MANDATORY;

public class UserListener {

    @PrePersist
    public void prePersist(Users target) {
        perform(target, INSERTED);
    }

    @PreUpdate
    public void preUpdate(Users target) {
        perform(target, UPDATED);
    }

    @PreRemove
    public void preRemove(Users target) {
        perform(target, DELETED);
    }

    @Transactional(MANDATORY)
    private void perform(Users target, Action action) {
        EntityManager entityManager = SpringApplicationContext.getBean(EntityManager.class);
        if(entityManager != null)
            entityManager.persist(new UserHistory(target, action));
    }
}
