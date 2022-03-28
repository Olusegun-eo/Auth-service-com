package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.FraudAction;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.FraudType;
import com.waya.wayaauthenticationservice.pojo.fraud.FraudEventRequestDTO;
import com.waya.wayaauthenticationservice.pojo.fraud.FraudIDPojo;
import com.waya.wayaauthenticationservice.proxy.FraudProxy;
import com.waya.wayaauthenticationservice.repository.FraudActionRepository;
import com.waya.wayaauthenticationservice.service.FraudService;
import com.waya.wayaauthenticationservice.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.waya.wayaauthenticationservice.enums.FraudType.PASSWORD_ERROR;
import static com.waya.wayaauthenticationservice.enums.FraudType.PIN_ERROR;

@Service
@AllArgsConstructor
@Slf4j
public class FraudServiceImpl implements FraudService {

    private final UserService userService;
    private final FraudActionRepository fraudActionRepository;
    private final FraudProxy fraudProxy;

    @Override
    public void actionOnInvalidPassword(Users user) {
        try {
            onFailureAction(user, PASSWORD_ERROR);
        } catch (Exception e) {
            log.error("An error has occurred :: {}", e.getMessage());
        }
    }

    @Override
    public void actionOnSignInSuccess(Users user) {
        try {
            onSuccessAction(user, PASSWORD_ERROR);
        } catch (Exception e) {
            log.error("An error has occurred :: {}", e.getMessage());
        }
    }

    @Override
    public void actionOnPinValidateSuccess(Users user) {
        try {
            onSuccessAction(user, PIN_ERROR);
        } catch (Exception e) {
            log.error("An error has occurred :: {}", e.getMessage());
        }
    }

    @Override
    public void actionOnInvalidPin(Users user) {
        try {
            onFailureAction(user, PIN_ERROR);
        } catch (Exception e) {
            log.error("An error has occurred :: {}", e.getMessage());
        }
    }

    private void onFailureAction(Users user, FraudType fraudType) {
        if (user != null) {
            FraudAction fraudAction = fraudActionRepository.findActionByUserId
                    (fraudType.name(), user.getId()).orElse(null);
            if (fraudAction == null) {
                fraudAction = new FraudAction();
                fraudAction.setAttempts(1);
                fraudAction.setUser(user);
                fraudAction.setFraudType(fraudType);
            } else {
                fraudAction.setAttempts(fraudAction.getAttempts() + 1);
            }
            //Save for Tracking
            fraudActionRepository.saveAndFlush(fraudAction);

            //Report Event
            FraudEventRequestDTO pojo = new FraudEventRequestDTO(fraudType.name(), user.getId());
            fraudProxy.reportFraudEvent(pojo);
        }
    }

    private void onSuccessAction(Users user, FraudType fraudType) {
        if (user != null) {
            FraudAction fraudAction = fraudActionRepository.findActionByUserId
                    (fraudType.name(), user.getId()).orElse(null);
            if (fraudAction != null) {
                fraudAction.setDeleted(true);
                fraudActionRepository.saveAndFlush(fraudAction);
            }
        }
    }

    @Override
    public ResponseEntity<?> findUserById(Long id, String apiKey) {
        return this.userService.validateServiceUserCall(id, apiKey);
    }

    @Override
    public ResponseEntity<?> toggleUserLock(FraudIDPojo pojo) {
        return this.userService.toggleLock(pojo.getUserId());
    }

    @Override
    public ResponseEntity<?> toggleUserActivation(FraudIDPojo pojo) {
        return this.userService.toggleActivation(pojo.getUserId());
    }

    @Override
    public ResponseEntity<?> closeAccount(FraudIDPojo pojo) {
        if(Objects.isNull(pojo.getAdminId())){
            return ResponseEntity.badRequest().body("Agent/Admin Id has to be passed for account Closure");
        }
        return ResponseEntity.ok("Account Closure Implementation yet to be done");
    }

}
