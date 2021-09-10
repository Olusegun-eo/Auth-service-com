package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.entity.ReferralBonusEarning;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.ReferralBonusEarningRepository;
import com.waya.wayaauthenticationservice.repository.ReferralBonusRepository;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.response.NewWalletResponse;
import com.waya.wayaauthenticationservice.response.ReferralBonusResponse;
import com.waya.wayaauthenticationservice.service.ManageReferralService;
import com.waya.wayaauthenticationservice.util.BearerTokenUtil;
import com.waya.wayaauthenticationservice.util.CommonUtils;
import com.waya.wayaauthenticationservice.util.Constant;
import com.waya.wayaauthenticationservice.util.ReferralBonusStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;


@Slf4j
@Service
public class ManageReferralServiceImpl implements ManageReferralService {


    private final ReferralBonusRepository referralBonusRepository;
    private final ReferralCodeServiceImpl referralCodeService;
    private final ProfileRepository profileRepository;
    private final ReferralBonusEarningRepository referralBonusEarningRepository;
    private final ReferralCodeRepository referralCodeRepository;
    private final WalletProxy walletProxy;

    @Autowired
    public ManageReferralServiceImpl(ReferralBonusRepository referralBonusRepository, ReferralCodeServiceImpl referralCodeService, ProfileRepository profileRepository, ReferralBonusEarningRepository referralBonusEarningRepository, ReferralCodeRepository referralCodeRepository, WalletProxy walletProxy) {
        this.referralBonusRepository = referralBonusRepository;
        this.referralCodeService = referralCodeService;
        this.profileRepository = profileRepository;
        this.referralBonusEarningRepository = referralBonusEarningRepository;
        this.referralCodeRepository = referralCodeRepository;
        this.walletProxy = walletProxy;
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
            ReferralBonus referralBonus1 = referralBonusRepository.save(referralBonus);
            return referralBonus1;

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

        for (int i = 0; i < profileList.size(); i++) {
            if (profileList.get(i).getReferral() == null){
                Profile profile = profileList.get(i);
//                profile.setReferral(null);
               // ProfileResponse profileResponse = getProfileResponse(profileList.get(i));

               log.info("profile " + profile);
                profileList2.add(profile);
            }
        }

        Map<String, Object> response = new HashMap<>();

        response.put("users", profileList2);
        response.put("currentPage", profilePage.getNumber());
        response.put("totalItems", profilePage.getTotalElements());
        response.put("totalPages", profilePage.getTotalPages());

        return response;
    }


    public Profile assignReferralCode(AssignReferralCodePojo assignReferralCodePojo){
        try{
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

    public NewWalletResponse sendReferralBonusToUser(UserTransferToDefaultWallet transfer){
        String token = BearerTokenUtil.getBearerTokenHeader();

        try{
            // make a call to credit users wallet
            ResponseEntity<WalletAccountInfo> responseEntity = walletProxy.sendMoneyToWallet(transfer,token);
            WalletAccountInfo infoResponse = (WalletAccountInfo) responseEntity.getBody();
            log.info("mainWalletResponse :: {} " +infoResponse.data);
            NewWalletResponse mainWalletResponse = infoResponse.data;
            return mainWalletResponse;

         } catch (Exception e) {
            System.out.println("Error is here " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }

    public Map<String, Object> getUsersWithTheirReferralsByPhoneNumber(String value, int page, int size){
        Pageable paging = PageRequest.of(page, size);
        List<Profile> profileList = new ArrayList<>();

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


        String district = "";
        String stateE = "";
        String addressE = "";
        Pageable paging = PageRequest.of(page, size);
        List<Profile> profileList = new ArrayList<>();
        List<Profile> profileList2 = new ArrayList<>();
        List<ReferralPojo> referralPojos = new ArrayList<>();

        Page<Profile> profilePage = profileRepository.findAll(paging, false);
//        Page<Profile> profilePage = profileRepository.getAllByUserId(paging, false);
//        profileList2 = profilePage.getContent();

//        log.info("profileList2 :::: " +profileList2);
        // get all user with referralCode
        profileList = profilePage.getContent();

        for (int i = 0; i < profileList.size(); i++) {
            ReferralPojo referralPojo = new ReferralPojo();
            Optional<ReferralCode> referralCode = referralCodeRepository.findByUserId(profileList.get(i).getUserId().toString());

            if (referralCode.isPresent()){
                if(profileList.get(i).getReferral() !=null){
                    Profile referU = getReferredDetails(profileList.get(i).getReferral());
                    if (referU.getDistrict() == null){
                        district = "";
                    }else{
                        district = referU.getDistrict();
                    }
                    if (referU.getState() == null){
                        stateE = "";
                    }else{
                        stateE = referU.getState();
                    }
                    if (referU.getAddress() == null){
                        addressE = "";
                    }else{
                        addressE = referU.getAddress();
                    }
                    referralPojo.setReferralEmail(referU.getEmail());
                    referralPojo.setReferredBy(referU.getSurname() + " " + referU.getFirstName());
                    referralPojo.setReferralLocation(district + " " + stateE + " " + addressE);
                    referralPojo.setReferralPhone(referU.getPhoneNumber());
                }else{
                    referralPojo.setReferralEmail("");
                    referralPojo.setReferredBy("");
                    referralPojo.setReferralLocation("");
                    referralPojo.setReferralPhone("");
                }
                referralPojo.setDateJoined(profileList.get(i).getCreatedAt());
                referralPojo.setReferralUser(profileList.get(i).getSurname() + " " + profileList.get(i).getFirstName());
                referralPojo.setReferralCode(referralCode.get().getReferralCode());

                if (referralCode.get().getReferralCode() !=null){
                    log.info("referralCode.get().getReferralCode() " + referralCode.get().getReferralCode());

                    referralPojo.setUsersReferred(getProfileDetails(referralCode.get().getReferralCode(),paging));
                    referralPojo.setEarnings(BigDecimal.ONE);
                    referralPojos.add(referralPojo);
                }

            }

        }


        Map<String, Object> response = new HashMap<>();

        response.put("users", referralPojos);
        response.put("currentPage", profilePage.getNumber());
        response.put("totalItems", profilePage.getTotalElements());
        response.put("totalPages", profilePage.getTotalPages());

        return response;
    }

    private List<Profile> getProfileDetails(String referralCode, Pageable paging){
        Page<Profile> profilePage1 = profileRepository.findAllByReferralCode(referralCode,paging, false);

        return profilePage1.getContent();
    }

    private Profile getReferredDetails(String referralCode){
        Optional<Profile> profile2 = null;
        Optional<ReferralCode> referralCode2 = referralCodeRepository.getReferralCodeByCode(referralCode);
        if (referralCode2.isPresent()){
            profile2 = profileRepository.findByUserId(false,referralCode2.get().getUserId());
        }

        return profile2.get();
    }

    public Map<String, Object> getUserThatHaveBeenReferred(String referralCode, int page, int size){
        String surname = "";
        String firstname = "";
        String middlename = "";

        String token = BearerTokenUtil.getBearerTokenHeader();

        Pageable paging = PageRequest.of(page, size);
        List<ReferredUsesPojo> referredUsesPojoList = new ArrayList<>();
        List<Profile> profileList = new ArrayList<>();
        Page<Profile> profilePage = profileRepository.findAllByReferralCode(referralCode,paging, false);
        profileList = profilePage.getContent();

        for (int i = 0; i < profileList.size(); i++) {
            long count = referralCodeService.getTrans(profileList.get(i).getUserId().toString(),token);

            ReferredUsesPojo referredUsesPojo = new ReferredUsesPojo();
            if (profileList.get(i).getSurname() !=null){
                surname = profileList.get(i).getSurname();
            }else{
                surname = "";
            }
            if (profileList.get(i).getFirstName() !=null){
                firstname = profileList.get(i).getFirstName();
            }else{
                firstname = "";
            }
            if (profileList.get(i).getMiddleName() !=null){
                middlename = profileList.get(i).getMiddleName();

            }else {
                middlename = "";
            }

            referredUsesPojo.setFullName(surname + " " +firstname + " " + middlename );
            referredUsesPojo.setPhone(profileList.get(i).getPhoneNumber());
            referredUsesPojo.setEmail(profileList.get(i).getEmail());
            referredUsesPojo.setLocation(profileList.get(i).getAddress());
            referredUsesPojo.setDateJoined(profileList.get(i).getCreatedAt());
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



}
