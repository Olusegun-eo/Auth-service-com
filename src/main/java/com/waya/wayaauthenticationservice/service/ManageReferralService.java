package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.entity.ReferralBonusEarning;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.response.NewWalletResponse;
import com.waya.wayaauthenticationservice.response.ReferralBonusResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface ManageReferralService {
    ReferralBonus toggleReferralAmount(Long id) throws CustomException;
    ReferralBonusResponse createReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException;
    ReferralBonusResponse getReferralBonusResponse(ReferralBonus referralBonus);
    ReferralBonus findReferralBonus(String id) throws CustomException;
    ReferralBonus editReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException;

    Map<String, Object> getUsersWithTheirReferralsByPhoneNumber(String value, int page, int size);
    Map<String, Object> getUsersWithTheirReferrals(int page, int size);
    Map<String, Object> getUserWithoutReferralCode(int page, int size);
    List<Profile> getUserWithoutReferralCode();
    Profile assignReferralCode(AssignReferralCodePojo assignReferralCodePojo);

    List<WalletTransactionPojo> sendReferralBonusToUser(BonusTransferRequest transfer);
    List<WalletTransactionPojo> sendReferralBonusToMultipleUsers(List<BonusTransferRequest> transfer);
    ResponseEntity<?> sendBulkReferralBonusTo(MultipartFile file, HttpServletRequest request, Device device);
    Map<String, Object> getUserThatHaveBeenReferred(String referralCode, int page, int size);

    Map<String, Object> getUsersSMSAlertStatus(int page, int size);

    List<WalletTransactionPojo> refundFailedTransaction(RefundTransactionRequest transfer) throws CustomException;
}
