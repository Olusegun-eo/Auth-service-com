package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.LoginHistory;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.pojo.others.LoginHistoryPojo;
import com.waya.wayaauthenticationservice.repository.LoginHistoryRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.LoginHistoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LoginHistoryServiceImpl implements LoginHistoryService {

    @Autowired
    LoginHistoryRepository loginHistoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticatedUserFacade authenticatedUserFacade;

    @Override
    public ResponseEntity<?> saveHistory(LoginHistoryPojo loginHistoryPojo) {
        Users user = userRepository.findById(false, loginHistoryPojo.getUserId()).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        LoginHistory loginHistory = new ModelMapper().map(loginHistoryPojo, LoginHistory.class);
        loginHistory.setUser(user);
        loginHistoryRepository.save(loginHistory);
        return new ResponseEntity<>(new SuccessResponse("Result Saved", loginHistory), HttpStatus.CREATED);

    }

    @Override
    public ResponseEntity<?> getHistoryByUserId(long userId) {
        Users user = userRepository.findById(false, userId).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        List<LoginHistory> loginHistory = loginHistoryRepository.findByUser(user);
        return new ResponseEntity<>(new SuccessResponse("Result Fetched", loginHistory), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getHistoryByUser() {
        Users user = authenticatedUserFacade.getUser();
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        List<LoginHistory> loginHistory = loginHistoryRepository.findByUser(user);
        return new ResponseEntity<>(new SuccessResponse("Result Fetched", loginHistory), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> getLastHistoryByUserId(long userId) {
        Users user = userRepository.findById(false, userId).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        LoginHistory loginHistory = loginHistoryRepository.findTop1ByUserOrderByLoginDateDesc(user);
        return new ResponseEntity<>(new SuccessResponse("Result Fetched", loginHistory), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> getMYLastHistory() {
        Users user = authenticatedUserFacade.getUser();
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }
        LoginHistory loginHistory = loginHistoryRepository.findTop1ByUserOrderByLoginDateDesc(user);
        return new ResponseEntity<>(new SuccessResponse("Result Fetched", loginHistory), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllHistoryByAdmin() {
        List<LoginHistory> loginHistory = loginHistoryRepository.findAll();
        return new ResponseEntity<>(new SuccessResponse("Result Fetched", loginHistory), HttpStatus.OK);

    }


}
