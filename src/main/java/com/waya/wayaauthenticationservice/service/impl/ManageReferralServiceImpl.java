package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.*;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfilePojo;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.*;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.service.ManageReferralService;
import com.waya.wayaauthenticationservice.util.BearerTokenUtil;
import com.waya.wayaauthenticationservice.util.CommonUtils;
import com.waya.wayaauthenticationservice.util.Constant;
import com.waya.wayaauthenticationservice.util.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


@Slf4j
@Service
public class ManageReferralServiceImpl implements ManageReferralService {


    private final ReferralBonusRepository referralBonusRepository;
    private final ReferralCodeServiceImpl referralCodeService;
    private final ProfileRepository profileRepository;
    private final ReferralBonusEarningRepository referralBonusEarningRepository;
    private final ReferralCodeRepository referralCodeRepository;
    private final WalletProxy walletProxy;
    private final ProfileServiceDAO jdbcprofileService;
    private final SMSAlertConfigRepository smsAlertConfigRepository;
    private final UserRepository userRepository;


    @Autowired
    public ManageReferralServiceImpl(ReferralBonusRepository referralBonusRepository, ReferralCodeServiceImpl referralCodeService, ProfileRepository profileRepository, ReferralBonusEarningRepository referralBonusEarningRepository, ReferralCodeRepository referralCodeRepository, WalletProxy walletProxy, ProfileServiceDAO jdbcprofileService, SMSAlertConfigRepository smsAlertConfigRepository, UserRepository userRepository) {
        this.referralBonusRepository = referralBonusRepository;
        this.referralCodeService = referralCodeService;
        this.profileRepository = profileRepository;
        this.referralBonusEarningRepository = referralBonusEarningRepository;
        this.referralCodeRepository = referralCodeRepository;
        this.walletProxy = walletProxy;
        this.jdbcprofileService = jdbcprofileService;
        this.smsAlertConfigRepository = smsAlertConfigRepository;
        this.userRepository = userRepository;
    }


    private ReferralBonus getReferralBonusById(Long id) throws CustomException {
        return referralBonusRepository.findById(id).orElseThrow(() -> new CustomException("Invalid id provided", HttpStatus.NOT_FOUND));
    }

    public ReferralBonus editReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException {
        try {
            ReferralBonus referralBonus = getReferralBonusById(referralBonusRequest.getId());
            if (referralBonus == null)
                throw new CustomException(Constant.NOTFOUND, HttpStatus.NOT_FOUND);

            referralBonus.setAmount(referralBonusRequest.getAmount());
            referralBonus.setNumberOfTransaction(referralBonusRequest.getNumberOfTransaction());

            // notify inApp

            return referralBonusRepository.save(referralBonus);
        } catch (Exception exception) {
            log.error("Unable to update referral bonus fee", exception);
            throw new CustomException(exception.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ReferralBonus toggleReferralAmount(Long id) throws CustomException {

        if (Objects.isNull(id)){
            throw new CustomException(Constant.ID_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        ReferralBonus referralBonus = referralBonusRepository.findById(id).orElseThrow(() -> new CustomException(Constant.ID_IS_UNKNOWN, HttpStatus.BAD_REQUEST));

        referralBonus.setActive(!referralBonus.isActive());
        try{
            return referralBonusRepository.save(referralBonus);

            // notify inApp

        } catch (Exception exception) {
            log.error("Unable to update referral bonus fee", exception);
            throw new CustomException(exception.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    public ReferralBonusResponse createReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException {
        try {


            ReferralBonus referralBonus = new ReferralBonus();
            referralBonus.setAmount(referralBonusRequest.getAmount());
            referralBonus.setNumberOfTransaction(referralBonusRequest.getNumberOfTransaction());
            referralBonus = referralBonusRepository.save(referralBonus);
            log.info(" referralBonusreferralBonus ::::" + referralBonus);
            return getReferralBonusResponse(referralBonus);
        } catch (Exception exception) {
            log.error("Unable to save referral bonus fee", exception);
            throw new CustomException(Constant.ERROR_MESSAGE,HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ReferralBonusResponse getReferralBonusResponse(ReferralBonus referralBonus){
        ReferralBonusResponse referralBonusResponse = new ReferralBonusResponse();
        referralBonusResponse.setId(referralBonus.getId());
        referralBonusResponse.setNumberOfTransaction(referralBonus.getNumberOfTransaction());
        referralBonusResponse.setAmount(referralBonus.getAmount());

        // notify inApp
        return referralBonusResponse;
    }

    public ReferralBonus findReferralBonus(String id) throws CustomException {
        try {

            return getReferralBonusById(Long.parseLong(id));
        } catch (Exception exception) {
            log.error("Unable to get referral bonus amount", exception);
            throw new CustomException(Constant.ERROR_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
    }


    public Map<String, Object> getUserWithoutReferralCode(int page, int size){
        Pageable paging = PageRequest.of(page, size);
        List<Profile> profileList2 = new ArrayList<>();
        // get all user with referralCode

        Page<Profile> profilePage = profileRepository.findAll(paging, false);
        List<Profile> profileList  = profilePage.getContent();

        for (Profile value : profileList) {
            if (value.getReferral() == null) {

                log.info("profile " + value);
                profileList2.add(value);
            }
        }

        Map<String, Object> response = new HashMap<>();

        response.put("users", profileList2);
        response.put("currentPage", profilePage.getNumber());
        response.put("totalItems", profilePage.getTotalElements());
        response.put("totalPages", profilePage.getTotalPages());

        return response;
    }

    @Override
    public List<Profile> getUserWithoutReferralCode() {
        List<Profile> profileList2 = new ArrayList<>();
        List<Profile> profileList = profileRepository.findAll(false);
        for (Profile value : profileList) {
            if (value.getReferral() == null) {
                profileList2.add(value);
            }
        }
        return profileList2;
    }


    public Profile assignReferralCode(AssignReferralCodePojo assignReferralCodePojo){
        try{

            Optional<Users> user =  userRepository.findById(false,Long.parseLong(assignReferralCodePojo.getUserId()));
            if (!user.isPresent()){
                user.get().setReferenceCode(assignReferralCodePojo.getReferralCode());
                userRepository.save(user.get());
            }

            Optional<Profile> profilePage = profileRepository.findByUserId(false,assignReferralCodePojo.getUserId());
            if (!profilePage.isPresent()){
                throw new CustomException(Constant.ID_IS_INVALID, HttpStatus.NOT_FOUND);
            }else{
                profilePage.get().setReferral(assignReferralCodePojo.getReferralCode());
                return profileRepository.save(profilePage.get());
            }

        }catch (Exception e){
            throw new CustomException( e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    public List<WalletTransactionPojo> sendReferralBonusToUser(BonusTransferRequest transfer){

        String token = BearerTokenUtil.getBearerTokenHeader();
        log.info("BonusTransferRequest ::: " + transfer);

        try{
            transfer.setPaymentReference(CommonUtils.generatePaymentTransactionId());
            // make a call to credit users wallet
            ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> responseEntity = walletProxy.sendMoneyToWallet(transfer,token);
            ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();
            List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
            log.info("responseList " + mainWalletResponseList);

            // record referral earnings
            ReferralBonusEarningRequest referralBonusEarningRequest = new ReferralBonusEarningRequest();
            referralBonusEarningRequest.setAmount(transfer.getAmount());
            referralBonusEarningRequest.setUserId(transfer.getUserId());
            CompletableFuture.runAsync(() -> saveReferralEarnings(referralBonusEarningRequest));

            return mainWalletResponseList;

         } catch (Exception e) {
            System.out.println("Error is here " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }

    public void saveReferralEarnings(ReferralBonusEarningRequest request){
        try{
            Optional<Profile> profile = profileRepository.findByUserId(false,request.getUserId());
            if (profile.isPresent()){
                ReferralBonusEarning referralBonusEarning = new ReferralBonusEarning();
                referralBonusEarning.setStatus(request.getStatus());
                referralBonusEarning.setAmount(request.getAmount());
                referralBonusEarning.setUserId(request.getUserId());
                referralBonusEarning =referralBonusEarningRepository.save(referralBonusEarning);
                log.info("Done saving referral earnings :: {} " + referralBonusEarning);
            }

        }catch (Exception e){
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }

//    private List<NewWalletResponse> getWayaOfficial(String token){
//        NewWalletResponse newWalletResponse;
//        ResponseEntity<ApiResponseBody<List<NewWalletResponse>>> responseEntity2 = walletProxy.getWayaOfficialWallet(token);
//        ApiResponseBody<List<NewWalletResponse>> infoResponse = (ApiResponseBody<List<NewWalletResponse>>) responseEntity2.getBody();
//
//        List<NewWalletResponse> dataList = getWayaOfficial(token);
//        for (int i = 0; i < dataList.size(); i++) {
//                newWalletResponse = new NewWalletResponse();
//                newWalletResponse = dataList.get(i);
//                log.info("newH ::: {} " +newWalletResponse);
//        }
//        return dataList;
//    }

    public List<WalletTransactionPojo> sendReferralBonusToMultipleUsers(List<BonusTransferRequest> transfer){

        String token = BearerTokenUtil.getBearerTokenHeader();
        List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>();

        try {
            for (BonusTransferRequest bonusTransferRequest : transfer) {
                bonusTransferRequest.setPaymentReference(CommonUtils.generatePaymentTransactionId());
                // make a call to credit users wallet
                ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> responseEntity = walletProxy.sendMoneyToWallet(bonusTransferRequest, token);
                ApiResponseBody<List<WalletTransactionPojo>> infoResponse2 = responseEntity.getBody();
                log.info(String.format("mainWalletResponse :: {} %s", infoResponse2.getData()));

                List<WalletTransactionPojo> mainWalletResponseList = infoResponse2.getData();
                log.info("responseList " + mainWalletResponseList);

                walletTransactionPojoList.addAll(mainWalletResponseList);

                // record referral earnings
                ReferralBonusEarningRequest referralBonusEarningRequest = new ReferralBonusEarningRequest();
                referralBonusEarningRequest.setAmount(bonusTransferRequest.getAmount());
                referralBonusEarningRequest.setUserId(bonusTransferRequest.getUserId());
                CompletableFuture.runAsync(() -> saveReferralEarnings(referralBonusEarningRequest));
            }

            } catch (Exception e) {
                System.out.println("Error is here " + e.getMessage());
                throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
            }
        return walletTransactionPojoList;
        }


    public ResponseEntity<?> sendBulkReferralBonusTo(MultipartFile file,
                                                     HttpServletRequest request, Device device) {
        String message;
        if (ExcelHelper.hasExcelFormat(file)) {
            ResponseEntity<?> responseEntity;
            try {
                    responseEntity = payBulkReferrals(ExcelHelper.excelToBonusTransferPojo(file.getInputStream(),
                            file.getOriginalFilename()), request, device);
                return responseEntity;
            } catch (Exception e) {
                throw new CustomException("failed to Parse excel data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        message = "Please upload an excel file!";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    ResponseEntity<?> payBulkReferrals(@Valid BulkBonusTransferDTO bonusList, HttpServletRequest request, Device device){

        List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>();
        String token = BearerTokenUtil.getBearerTokenHeader();
        if (bonusList == null || bonusList.getBonusList().isEmpty())
            return new ResponseEntity<>(new ErrorResponse("User List cannot be null or Empty"), BAD_REQUEST);

            for (BonusTransferExcelPojo mTransfer : bonusList.getBonusList()) {
                BonusTransferRequest transfer = new BonusTransferRequest();
                transfer.setPaymentReference(CommonUtils.generatePaymentTransactionId());
                transfer.setCustomerCreditAccount(mTransfer.getCustomerCreditNumber());
                transfer.setOfficeDebitAccount(mTransfer.getOfficeDebitAccount());
                transfer.setTranNarration(mTransfer.getTranNarration());
                transfer.setTranType("LOCAL");
                transfer.setTranCrncy(mTransfer.getTranCrncy());
                transfer.setAmount(BigDecimal.valueOf(mTransfer.getAmount()));
                transfer.setUserId(mTransfer.getUserId());
                // make a call to credit users wallet
                ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> responseEntity = walletProxy.sendMoneyToWallet(transfer, token);
                ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();
                log.info(String.format("mainWalletResponse :: {} %s", Objects.requireNonNull(infoResponse).getData()));

                List<WalletTransactionPojo> mainWalletResponseList = infoResponse.getData();
                log.info("responseList " + mainWalletResponseList);
                walletTransactionPojoList.addAll(mainWalletResponseList);

                ReferralBonusEarningRequest referralBonusEarningRequest = new ReferralBonusEarningRequest();
                referralBonusEarningRequest.setAmount(transfer.getAmount());
                referralBonusEarningRequest.setUserId(transfer.getUserId());
                CompletableFuture.runAsync(() -> saveReferralEarnings(referralBonusEarningRequest));
            }

        return new ResponseEntity<>(new SuccessResponse(walletTransactionPojoList), HttpStatus.OK);
    }


    public Map<String, Object> getUsersWithTheirReferralsByPhoneNumber(String value, int page, int size){
        Pageable paging = PageRequest.of(page, size);
        List<Profile> profileList;

        if (CommonUtils.isEmpty(value)){
            return getUsersWithTheirReferrals(page,size);
        }
        try{

            Page<Profile> profilePage = profileRepository.findAllByEmailOrPhoneNumber(false,value, paging);
            profileList = profilePage.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("users", profileList);
            response.put("currentPage", profilePage.getNumber());
            response.put("totalItems", profilePage.getTotalElements());
            response.put("totalPages", profilePage.getTotalPages());
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }


    public Map<String, Object> getUsersWithTheirReferrals(int page, int size){

        String district;
        String stateE;
        String addressE;
        Pageable paging = PageRequest.of(page, size);
        List<UserProfilePojo> profileList;
        List<ReferralPojo> referralPojos = new ArrayList<>();

        Page<UserProfilePojo> user = jdbcprofileService.GetAllUserProfile(paging);
        // get all user with referralCode
         profileList = user.getContent();
        for (UserProfilePojo userProfilePojo : profileList) {
            ReferralPojo referralPojo = new ReferralPojo();
            Optional<ReferralCode> referralCode = referralCodeRepository.findByUserId(userProfilePojo.getId().toString());

            if (referralCode.isPresent()) {
                if (userProfilePojo.getReferral() != null) {

                    Profile referU = getReferredDetails(userProfilePojo.getReferral());

                    if (Objects.requireNonNull(referU).getDistrict() == null) {
                        district = "";
                    } else {
                        district = referU.getDistrict();
                    }
                    if (referU.getState() == null) {
                        stateE = "";
                    } else {
                        stateE = referU.getState();
                    }
                    if (referU.getAddress() == null) {
                        addressE = "";
                    } else {
                        addressE = referU.getAddress();
                    }
                    referralPojo.setReferralEmail(referU.getEmail());
                    referralPojo.setReferredBy(referU.getSurname() + " " + referU.getFirstName());
                    referralPojo.setReferralLocation(district + " " + stateE + " " + addressE);
                    referralPojo.setReferralPhone(referU.getPhoneNumber());
                } else {
                    referralPojo.setReferralEmail("");
                    referralPojo.setReferredBy("");
                    referralPojo.setReferralLocation("");
                    referralPojo.setReferralPhone("");
                }
                referralPojo.setDateJoined(userProfilePojo.getCreatedAt());
                referralPojo.setReferralUser(userProfilePojo.getLastName() + " " + userProfilePojo.getFirstName());
                referralPojo.setReferralCode(referralCode.get().getReferralCode());
                referralPojo.setUserId(userProfilePojo.getId().toString());

                if (referralCode.get().getReferralCode() != null) {
                    referralPojo.setUsersReferred(getProfileDetails(referralCode.get().getReferralCode(), paging));
                    referralPojo.setEarnings(BigDecimal.valueOf(getReferralBonusEarning(referralCode.get().getUserId())));
                    referralPojos.add(referralPojo);
                }

            }

        }

        Map<String, Object> response = new HashMap<>();
        response.put("users", referralPojos);
        response.put("currentPage", user.getNumber());
        response.put("totalItems", user.getTotalElements());
        response.put("totalPages", user.getTotalPages());

        return response;
    }

    private List<Profile> getProfileDetails(String referralCode, Pageable paging){
        Page<Profile> profilePage1 = profileRepository.findAllByReferralCode(referralCode,paging, false);

        return profilePage1.getContent();
    }

    public Double getReferralBonusEarning(String userId){

        double totalEarnings = 0.0;
        List<ReferralBonusEarning> referralBonusEarningList = referralBonusEarningRepository.findAllByUserId(userId);

        for (int i = 0; i < referralBonusEarningList.size(); i++) {
            totalEarnings += referralBonusEarningList.get(i).getAmount().doubleValue();
        }
        return totalEarnings;

    }

    private Profile getReferredDetails(String referralCode){

        Optional<Profile> profile2 = Optional.empty();
        Optional<ReferralCode> referralCode2 = referralCodeRepository.getReferralCodeByCode(referralCode);
        if (referralCode2.isPresent()){
            log.info("HERE" +referralCode2.get());
            profile2 = profileRepository.findByUserId(false,referralCode2.get().getUserId());
        }else{
            log.info("null");
        }

        return profile2.isPresent() ? profile2.get() : null;
    }

    public Map<String, Object> getUserThatHaveBeenReferred(String referralCode, int page, int size){
        String surname;
        String firstname;
        String middlename;

        String token = BearerTokenUtil.getBearerTokenHeader();

        Pageable paging = PageRequest.of(page, size);
        List<ReferredUsesPojo> referredUsesPojoList = new ArrayList<>();
        List<Profile> profileList;
        Page<Profile> profilePage = profileRepository.findAllByReferralCode(referralCode,paging, false);
        profileList = profilePage.getContent();

        for (Profile profile : profileList) {
            long count = referralCodeService.getTrans(profile.getUserId(), token);

            ReferredUsesPojo referredUsesPojo = new ReferredUsesPojo();
            if (profile.getSurname() != null) {
                surname = profile.getSurname();
            } else {
                surname = "";
            }
            if (profile.getFirstName() != null) {
                firstname = profile.getFirstName();
            } else {
                firstname = "";
            }
            if (profile.getMiddleName() != null) {
                middlename = profile.getMiddleName();

            } else {
                middlename = "";
            }

            referredUsesPojo.setFullName(surname + " " + firstname + " " + middlename);
            referredUsesPojo.setPhone(profile.getPhoneNumber());
            referredUsesPojo.setEmail(profile.getEmail());
            referredUsesPojo.setLocation(profile.getAddress());
            referredUsesPojo.setDateJoined(profile.getCreatedAt());
            referredUsesPojo.setNumberOfTransactions(count);
            referredUsesPojoList.add(referredUsesPojo);
        }
        Map<String, Object> response = new HashMap<>();

        response.put("referredUsers", referredUsesPojoList);
        response.put("currentPage", profilePage.getNumber());
        response.put("totalItems", profilePage.getTotalElements());
        response.put("totalPages", profilePage.getTotalPages());

        return response;
    }


    public Map<String, Object> getUsersSMSAlertStatus(int page, int size){
       // UserProfileResponse userProfileResponse = new UserProfileResponse();
        Pageable paging = PageRequest.of(page, size);
        List<UserProfilePojo> profileList;
        Page<UserProfilePojo> user = jdbcprofileService.GetAllUserProfile(paging);
        profileList = user.getContent();

        for (UserProfilePojo userProfilePojo : profileList) {
            Optional<SMSAlertConfig> smsAlertConfig = smsAlertConfigRepository.findByPhoneNumber(userProfilePojo.getPhoneNo());
            if (smsAlertConfig.isPresent()) {
                if (smsAlertConfig.get().isActive()) {
                    userProfilePojo.setSMSAlert(true);
                } else {
                    userProfilePojo.setSMSAlert(false);
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();

        response.put("referredUsers", profileList);
        response.put("currentPage", user.getNumber());
        response.put("totalItems", user.getTotalElements());
        response.put("totalPages", user.getTotalPages());
        return response;
    }


    public List<WalletTransactionPojo> refundFailedTransaction(RefundTransactionRequest transfer) throws CustomException {
        String token = BearerTokenUtil.getBearerTokenHeader();
        try {
            ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> response =  walletProxy.refundFailedTransaction(transfer,token);

            ApiResponseBody<List<WalletTransactionPojo>> infoResponse = response.getBody();
            List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
            log.info("responseList " + mainWalletResponseList);

            return mainWalletResponseList;
        } catch (RestClientException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new CustomException(e.getMessage(),HttpStatus.EXPECTATION_FAILED);
        }

    }




}
