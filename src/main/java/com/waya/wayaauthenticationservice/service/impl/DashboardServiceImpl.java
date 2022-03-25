package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.service.DashboardService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository usersRepository;


    @Override
    public long countActiveUser() {
       return usersRepository.findByActiveUsers();
    }

    @Override
    public long countActiveUsersCooperate() {
        return usersRepository.findByActiveCooperateUsers();
    }

    @Override
    public long countInactiveUsers() {
        return usersRepository.findByInactiveUsers();
    }

    @Override
    public long countInactiveUsersCooperate() {
        return usersRepository.findByInactiveCooperateUsers();
    }
}
