package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.*;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.WelcomeEmailContext;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserIDPojo;
import com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient;
import com.waya.wayaauthenticationservice.proxy.ReferralProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.*;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.util.BearerTokenUtil;
import com.waya.wayaauthenticationservice.util.CommonUtils;
import com.waya.wayaauthenticationservice.util.Constant;
import lombok.extern.slf4j.Slf4j;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.waya.wayaauthenticationservice.enums.OTPRequestType.JOINT_VERIFICATION;
import static com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient.uploadImage;
import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateReferralCode;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.validateNum;
import static org.springframework.http.HttpStatus.OK;

@Service
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final KafkaMessageProducer kafkaMessageProducer;
    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final OTPTokenService otpTokenService;
    private final FileResourceServiceFeignClient fileResourceServiceFeignClient;
    private final OtherDetailsRepository otherDetailsRepository;
    private final SMSAlertConfigRepository smsAlertConfigRepository;
    private final MessagingService messagingService;
    private final ReferralCodeRepository referralCodeRepository;
    private final RestTemplate restClient;
    @Value("${app.config.wayagram-profile.base-url}")
    private String wayagramUrl;
    private final WalletProxy walletProxy;
    private final ReferralBonusRepository referralBonusRepository;
    private final ReferralProxy referralProxy;

    @Value("${referral.account}")
    public String referralAccount;

    @Autowired
    public ProfileServiceImpl(KafkaMessageProducer kafkaMessageProducer, ModelMapper modelMapper, ProfileRepository profileRepository,
                              UserRepository userRepository, OTPTokenService otpTokenService,
                              FileResourceServiceFeignClient fileResourceServiceFeignClient,
                              @Qualifier("restClient") RestTemplate restClient,
                              OtherDetailsRepository otherDetailsRepository,
                              SMSAlertConfigRepository smsAlertConfigRepository, MessagingService messagingService,
                              ReferralCodeRepository referralCodeRepository, WalletProxy walletProxy, ReferralBonusRepository referralBonusRepository, ReferralProxy referralProxy) {
        this.kafkaMessageProducer = kafkaMessageProducer;
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.otpTokenService = otpTokenService;
        this.fileResourceServiceFeignClient = fileResourceServiceFeignClient;
        this.otherDetailsRepository = otherDetailsRepository;
        this.smsAlertConfigRepository = smsAlertConfigRepository;
        this.messagingService = messagingService;
        this.referralCodeRepository = referralCodeRepository;
        this.userRepository = userRepository;
        this.restClient = restClient;
        this.walletProxy = walletProxy;
        this.referralBonusRepository = referralBonusRepository;
        this.referralProxy = referralProxy;
    }

    private static SearchProfileResponse apply(Profile profilePersonal) {
        SearchProfileResponse searchProfileResponse = new SearchProfileResponse();
        searchProfileResponse.setId(profilePersonal.getId());
        searchProfileResponse.setFirstName(profilePersonal.getFirstName());
        searchProfileResponse.setEmail(profilePersonal.getEmail());
        searchProfileResponse.setAvatar(profilePersonal.getProfileImage());
        searchProfileResponse.setSurname(profilePersonal.getSurname());
        searchProfileResponse.setPhoneNumber(profilePersonal.getPhoneNumber());
        searchProfileResponse.setUserId(profilePersonal.getUserId());
        return searchProfileResponse;
    }

    /**
     * get all users referral
     *
     * @param userId userid
     * @return List<UserProfileResponse>
     */
    @Override
    public List<UserProfileResponse> findAllUserReferral(String userId, String page) {
        // TODO: This method should be accessible by admins only
        try {
            if (validateNum(page).equals(false))
                throw new CustomException("invalid page number", HttpStatus.BAD_REQUEST);

            int parsePageNumber = Integer.parseInt(page);

            if (parsePageNumber > 0)
                parsePageNumber--;

            // make a call to the profile service to get getReferralCodeByUserId
            //ReferralCodePojo referralCodePojo = checkReferralCode(userId);
           // log.info("referralCodePojo :::: " + referralCodePojo);

            ReferralCode referrals = referralCodeRepository.getReferralCodeByUserId(userId).orElse(null);
            if (referrals == null) {
                return Collections.emptyList();
            }
//            return profileRepository
//                    .findAllByReferralCode(referrals.getReferralCode(), LIMIT, parsePageNumber * LIMIT, false).stream()
//                    .map(this::setProfileResponse).collect(Collectors.toList());
            List<UserProfileResponse> list = new ArrayList<>();
            for (Profile profile : profileRepository
                    .findAllByReferralCode(referrals.getReferralCode(), LIMIT, parsePageNumber * LIMIT, false)) {
                UserProfileResponse userProfileResponse = setProfileResponse(profile);
                list.add(userProfileResponse);
            }
            return list;

        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), exception, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * creates a personal profile
     *
     * @param request profile
     */
    @Override
    public ApiResponseBody<String> createProfile(PersonalProfileRequest request, String baseUrl) {

        try {
            Users user = this.userRepository.findById(false, Long.valueOf(request.getUserId())).orElse(null);
            if (user == null)
                throw new CustomException("Base User with Provided ID not Found", HttpStatus.BAD_REQUEST);

            Optional<Profile> profileWithUserId = profileRepository.findByUserId(false, request.getUserId());
            if (profileWithUserId.isPresent())
                throw new CustomException("Profile with Provided User ID already Exists", HttpStatus.BAD_REQUEST);

            // check if the user exist in the profile table
            Optional<Profile> profile = request.getEmail() == null ? Optional.empty() :
                    profileRepository.findByEmail(false, request.getEmail());

            // check if the user exist in the referral table
            //ReferralCodePojo referralCodePojo = checkReferralCode(request.getUserId());
          //  log.info("referralCodePojo :::: " + referralCodePojo);

            Optional<ReferralCode> referralCode = referralCodeRepository.findByUserId(request.getUserId());

            // validation check
            //ApiResponseBody<String> validationCheck2 = validationCheckOnProfile2(profile, referralCodePojo);  // external implementation
            ApiResponseBody<String> validationCheck = validationCheckOnProfile(profile, referralCode);
            if (validationCheck.getStatus()) {
                Profile newProfile = modelMapper.map(request, Profile.class);

                // check if this referral code is already mapped to a user
//                if (request.getReferralCode() == null){
//                    newProfile.setReferral(null);
//                }else{

//                }
                newProfile.setReferral(request.getReferralCode());
                newProfile.setCorporate(false);
                newProfile.setDateOfBirth(request.getDateOfBirth().toString());
                // save new personal profile
                Profile savedProfile = profileRepository.save(newProfile);

                // save referral code to referral service
                CompletableFuture.runAsync(() -> kafkaMessageProducer.send(CREATE_REFERRAL_TOPIC,savedProfile));

                // save referral code in auth service:: NOTE THIS WILL BE REMOVED SOON
                saveReferralCode(savedProfile, request.getUserId());

                String fullName = String.format("%s %s", savedProfile.getFirstName(), savedProfile.getSurname());

                // send otp to Phone and Email
                CompletableFuture.runAsync(
                        () -> otpTokenService.sendAccountVerificationToken(user, JOINT_VERIFICATION, baseUrl));

                // create waya gram profile
                CompletableFuture.runAsync(
                        () -> createWayagramProfile(savedProfile.getUserId(), savedProfile.getSurname(), fullName))
                        .orTimeout(5, TimeUnit.MINUTES).handle((res, ex) -> {
                    if (ex != null) {
                        log.error("Error With Setting up Wayagram Profile:: {}", ex.getMessage());
                    }
                    return res;
                });

                if (request.getReferralCode() !=null){
                    CompletableFuture.runAsync(
                            () -> sendSignUpBonusToUser(savedProfile.getUserId()));
                }

                return new ApiResponseBody<>(null, CREATE_PROFILE_SUCCESS_MSG, true);
            } else {
                // return the error
                return validationCheck;
            }
        } catch (DataIntegrityViolationException dve) {
            log.error("{} {}", CATCH_EXCEPTION_MSG, dve.getMessage());
            return new ApiResponseBody<>(null, DUPLICATE_KEY, false);
        } catch (Exception exception) {
            log.error(CATCH_EXCEPTION_MSG, exception);
            return new ApiResponseBody<>(null, exception.getMessage(), false);
        }
    }

    private ReferralCodePojo checkReferralCode(String userId) throws Exception {
        try {
            ResponseEntity<ApiResponseBody<ReferralCodePojo>> responseEntity = referralProxy.getUserByReferralCode(userId);
            ApiResponseBody<ReferralCodePojo> responseBody = responseEntity.getBody();
            ReferralCodePojo referralCodePojo = responseBody.getData();
            log.info("referralCodePojo :::: " + referralCodePojo);
            return referralCodePojo;
        }catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }

    }

    public ResponseEntity<ReferralCodePojo> checkReferralCode2(String userId) throws Exception {
        try {
            ResponseEntity<ApiResponseBody<ReferralCodePojo>> responseEntity = referralProxy.getUserByReferralCode(userId);
            ApiResponseBody<ReferralCodePojo> responseBody = responseEntity.getBody();
            ReferralCodePojo referralCodePojo = responseBody.getData();
            log.info("referralCodePojo :::: " + referralCodePojo);

            return new ResponseEntity<>(referralCodePojo, OK);
        }catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }

    }

    public void postReferralCode2(Profile savedProfile, String userId) throws Exception {
        try {
            ReferralCodeRequest referralCodeRequest = new ReferralCodeRequest();
            referralCodeRequest.setUserId(userId);
            referralCodeRequest.setProfile(savedProfile.getId());

            ResponseEntity<String> responseEntity = referralProxy.saveReferralCode(referralCodeRequest);
            log.info("responseEntity :::: " + responseEntity);
        }catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }

    }
    private void postReferralCode(Profile savedProfile, String userId) throws Exception {
        try {
            log.info("Creating ReferralCode in referral-service :::: " + savedProfile);
            ReferralCodeRequest referralCodeRequest = new ReferralCodeRequest();
            referralCodeRequest.setUserId(userId);
            referralCodeRequest.setProfile(savedProfile.getId());

            ResponseEntity<String> responseEntity = referralProxy.saveReferralCode(referralCodeRequest);
            log.info("responseEntity :::: " + responseEntity);
        }catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }

    }


    public void postMigrateReferralCode(UUID profileId, String userId, String token) {

            ReferralCodeRequest referralCodeRequest = new ReferralCodeRequest();
            referralCodeRequest.setUserId(userId);
            referralCodeRequest.setProfile(profileId);

            ResponseEntity<String> responseEntity = referralProxy.saveReferralCode(referralCodeRequest);
            log.info("responseEntity :::: " + responseEntity);

    }

    /**
     * create a new corporate profile.
     *
     * @param baseUrl url to direct request to
     * @param profileRequest corporate profile request
     */
    @Transactional
    @Override
    public ApiResponseBody<String> createProfile(CorporateProfileRequest profileRequest, String baseUrl) {
        try {

            Users user = this.userRepository.findById(false, Long.valueOf(profileRequest.getUserId())).orElse(null);
            if (user == null)
                throw new CustomException("Base User with Provided ID not Found", HttpStatus.BAD_REQUEST);

            Optional<Profile> profileWithUserId = profileRepository.findByUserId(false, profileRequest.getUserId());

            if (profileWithUserId.isPresent())
                throw new CustomException("Profile with Provided User ID already Exists", HttpStatus.BAD_REQUEST);

//            if (profileRequest.getReferralCode() != null && !profileRequest.getReferralCode().isBlank()) {
//                ReferralCode referralCode1 = referralCodeRepository
//                        .getReferralCodeByCode(profileRequest.getReferralCode()).orElse(null);
//                if (referralCode1 == null)
//                    profileRequest.setReferralCode(null);
//            }
            // check if the user exist in the profile table
            Optional<Profile> profile = profileRequest.getEmail() == null ? Optional.empty() :
                    profileRepository.findByEmail(false, profileRequest.getEmail());

            // check if the user exist in the referral table
            // now this check will extend to the referral service
           // ReferralCodePojo referralCodePojo = checkReferralCode(profileRequest.getUserId());   // external call implementation
           // log.info("referralCodePojo :::: " + referralCodePojo);

            Optional<ReferralCode> referralCode = referralCodeRepository.findByUserId(profileRequest.getUserId()); // internal call implementation
            // validation check
           // ApiResponseBody<String> validationCheck2 = validationCheckOnProfile2(profile, referralCodePojo); // External call implementation
            ApiResponseBody<String> validationCheck = validationCheckOnProfile(profile, referralCode);       // Internal call
            if (validationCheck.getStatus()) {
                Profile savedProfile = saveCorporateProfile(profileRequest);

                // save the referral code make request to the referral service
                CompletableFuture.runAsync(() -> kafkaMessageProducer.send(CREATE_REFERRAL_TOPIC,savedProfile));

                saveReferralCode(savedProfile, profileRequest.getUserId());



                // send otp to Phone and Email
                CompletableFuture.runAsync(() -> otpTokenService.sendAccountVerificationToken(user,
                        JOINT_VERIFICATION, baseUrl));

                String fullName = String.format("%s %s", savedProfile.getFirstName(), savedProfile.getSurname());

                // create waya gram profile
                CompletableFuture.runAsync(
                        () -> createWayagramProfile(savedProfile.getUserId(), savedProfile.getSurname(), fullName))
                        .orTimeout(5, TimeUnit.MINUTES).handle((res, ex) -> {
                    if (ex != null) {
                        log.error("Error With Setting up Wayagram Profile:: {}", ex.getMessage());
                    }
                    return res;
                });

                if (profileRequest.getReferralCode() !=null){
                    String token = BearerTokenUtil.getBearerTokenHeader();
                    // send request to referral service
                    AutoSignUpReferralRequest request = new AutoSignUpReferralRequest();
                    request.setUserId(savedProfile.getUserId());
                    CompletableFuture.runAsync(() -> referralProxy.autoSendSignUpReferralBonus(request,token));

                    CompletableFuture.runAsync(() -> sendSignUpBonusToUser(savedProfile.getUserId()));
                }

                return new ApiResponseBody<>(null, CREATE_PROFILE_SUCCESS_MSG, true);
            } else {
                // return the error
                return validationCheck;
            }
        } catch (DataIntegrityViolationException dve) {
            log.error(CATCH_EXCEPTION_MSG, dve);
            return new ApiResponseBody<>(null, DUPLICATE_KEY, false);
        } catch (Exception exception) {
            log.error(CATCH_EXCEPTION_MSG, exception);
            return new ApiResponseBody<>(null, exception.getMessage(), false);
        }
    }

    private Profile saveCorporateProfile(CorporateProfileRequest profileRequest) {

        OtherDetailsRequest otherDetailsRequest = new OtherDetailsRequest();
        otherDetailsRequest.setBusinessType(profileRequest.getBusinessType());
        otherDetailsRequest.setOrganisationName(profileRequest.getOrganisationName());
        otherDetailsRequest.setOrganisationType(profileRequest.getOrganisationType());
        otherDetailsRequest.setOrganisationEmail(profileRequest.getOrganisationEmail());
        otherDetailsRequest.setOrganisationPhone(profileRequest.getOrganisationPhone());
        otherDetailsRequest.setOrganizationCity(profileRequest.getOrganizationCity());
        otherDetailsRequest.setOrganizationAddress(profileRequest.getOfficeAddress());
        otherDetailsRequest.setOrganizationState(profileRequest.getOrganizationState());

        OtherDetails otherDetails = saveOtherDetails(otherDetailsRequest);

        Profile profile = modelMapper.map(profileRequest, Profile.class);
        profile.setCorporate(true);
        profile.setDateOfBirth(profileRequest.getDateOfBirth().toString());
        profile.setGender(profileRequest.getGender());
        profile.setEmail(profileRequest.getEmail());
        profile.setFirstName(profileRequest.getFirstName());
        profile.setSurname(profileRequest.getSurname());
        profile.setPhoneNumber(profileRequest.getPhoneNumber());
        profile.setUserId(profileRequest.getUserId());
        profile.setReferral(profileRequest.getReferralCode());
        profile.setOtherDetails(otherDetails);

        return profileRepository.save(profile);
    }

    /**
     * save other details
     *
     * @param otherDetailsRequest other details request
     * @return OtherDetails
     */
    private OtherDetails saveOtherDetails(OtherDetailsRequest otherDetailsRequest) {
        OtherDetails otherDetails = new OtherDetails();
        UUID id = otherDetailsRequest.getOtherDetailsId() == null ? UUID.randomUUID()
                : otherDetailsRequest.getOtherDetailsId();
        otherDetails.setId(id);
        otherDetails.setBusinessType(otherDetailsRequest.getBusinessType());
        otherDetails.setOrganisationName(otherDetailsRequest.getOrganisationName());
        otherDetails.setOrganisationType(otherDetailsRequest.getOrganisationType());
        otherDetails.setOrganisationEmail(otherDetailsRequest.getOrganisationEmail());
        otherDetails.setOrganizationCity(otherDetailsRequest.getOrganizationCity());
        otherDetails.setOrganizationAddress(otherDetailsRequest.getOrganizationAddress());
        otherDetails.setOrganizationState(otherDetailsRequest.getOrganizationState());
        otherDetails.setOrganisationPhone(otherDetailsRequest.getOrganisationPhone());

        otherDetails = otherDetailsRepository.save(otherDetails);
        return otherDetails;
    }

    /**
     * check for the availability of the service rollback if the service is
     * unavailable
     */
    public void saveReferralCode(Profile newProfile, String userId) {
        // send details to the referral Service
        referralCodeRepository.save(new ReferralCode(generateReferralCode(REFERRAL_CODE_LENGTH), newProfile, userId));

        log.info("saving referral code for this new profile");
    }

    /**
     * get user personal profile and also put in cache for subsequent request
     * notification
     *
     * @param userId  user id
     * @param request http servlet request
     * @return PersonalProfileResponse
     */
    // @Cacheable(cacheNames = "PersonalProfile", key = "#userId")
    public UserProfileResponse getUserProfile(String userId, HttpServletRequest request) {
        try {
            Optional<Profile> profile = profileRepository.findByUserId(false, userId);
            if (profile.isEmpty())
                throw new CustomException("profile with that user id is not found", HttpStatus.NOT_FOUND);

            log.info("getting users profile from db ::: {}", profile.get());
            return setProfileResponse(profile.get());

        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), exception, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * updates a personal profile.
     *
     * @param updatePojo profile update request
     * @param userId                       user id
     * @return Object
     */
    // @CachePut(cacheNames = "PersonalProfile", key = "#userId")
    public UserProfileResponse updateProfile(UpdatePersonalProfileRequest updatePojo, String userId) {

        try {
            Optional<Users> user = userRepository.findById(false, Long.parseLong(userId));
            if (user.isPresent()) {
                Optional<Profile> profileOp = profileRepository.findByUserId(false, userId);

                if (profileOp.isPresent()) {
                    // Throw Error if any Validation error on Auth
                    updateUserAccount(user.get(), updatePojo);

                    Profile updatedProfile = profileOp.get();
                    updatedProfile.setFirstName(updatePojo.getFirstName());
                    updatedProfile.setSurname(updatePojo.getSurname());
                    updatedProfile.setPhoneNumber(updatePojo.getPhoneNumber());
                    updatedProfile.setEmail(updatePojo.getEmail());
                    updatedProfile.setMiddleName(updatePojo.getMiddleName());
                    updatedProfile.setCity(updatePojo.getCity());
                    updatedProfile.setGender(updatePojo.getGender());
                    updatedProfile.setState(updatePojo.getState());
                    updatedProfile.setDistrict(updatePojo.getDistrict());
                    updatedProfile.setAddress(updatePojo.getAddress());
                    updatedProfile.setDateOfBirth(updatePojo.getDateOfBirth().toString());
                    updatedProfile = profileRepository.save(updatedProfile);

                    return setProfileResponse(updatedProfile);
                } else {
                    throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
                }
            }
            throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void updateUserAccount(Users user, UpdatePersonalProfileRequest newProfile) {
        if (userRepository.existsByEmail(newProfile.getEmail())
                && isNotEqual(user.getEmail(), newProfile.getEmail()))
            throw new CustomException("Email for Update already Belongs to another User", HttpStatus.BAD_REQUEST);

        if (isNotEqual(user.getEmail(), newProfile.getEmail())) {
            user.setPhoneVerified(false);
        }
        user.setEmail(newProfile.getEmail());
        if (userRepository.existsByPhoneNumber(newProfile.getPhoneNumber().trim())
                && isNotEqual(user.getPhoneNumber(), newProfile.getPhoneNumber()))
            throw new CustomException("Phone Number for Update already Belongs to another User",
                    HttpStatus.BAD_REQUEST);

        if (isNotEqual(user.getPhoneNumber(), newProfile.getPhoneNumber())) {
            user.setPhoneVerified(false);
        }
        user.setPhoneNumber(newProfile.getPhoneNumber());
        user.setSurname(newProfile.getSurname());
        user.setFirstName(newProfile.getFirstName());
        String name = String
                .format("%s %s %s", newProfile.getFirstName(), newProfile.getMiddleName(), newProfile.getSurname())
                .replaceAll("\\s+", " ").trim();
        user.setName(name);
        userRepository.save(user);
    }

    private boolean isNotEqual(String str1, String str2) {
        if (str1 == null && str2 == null)
            return false;
        if (str1 == null || str2 == null)
            return true;
        return !str1.trim().equalsIgnoreCase(str2.trim());
    }

    /**
     * update a corporate profile
     *
     * @param corp corporate profile request
     * @param userId                  user id
     * @return CorporateProfileResponse
     */
    @Override
    public UserProfileResponse updateProfile(UpdateCorporateProfileRequest corp, String userId) {
        try {
            Optional<Users> user = userRepository.findById(false, Long.parseLong(userId));
            if (user.isPresent() && user.get().isCorporate()) {
                Optional<Profile> profile = profileRepository.findByUserId(false, userId);
                if (profile.isPresent() && profile.get().isCorporate()) {
                    // Update Base User, throw Validation error if any issue
                    updateUserAccount(user.get(), corp);

                    OtherDetailsRequest detailsRequest = new OtherDetailsRequest();
                    UUID id = profile.get().getOtherDetails() == null ? null : profile.get().getOtherDetails().getId();
                    detailsRequest.setOtherDetailsId(id);
                    detailsRequest.setBusinessType(corp.getBusinessType());
                    detailsRequest.setOrganisationType(corp.getOrganisationType());
                    detailsRequest.setOrganisationName(corp.getOrganisationName());
                    detailsRequest.setOrganizationCity(corp.getOrganizationCity());
                    detailsRequest.setOrganisationPhone(corp.getOrganisationPhone());
                    detailsRequest.setOrganizationState(corp.getOrganizationState());
                    detailsRequest.setOrganisationEmail(corp.getOrganisationEmail());
                    detailsRequest.setOrganizationAddress(corp.getOfficeAddress());

                    OtherDetails details = saveOtherDetails(detailsRequest);

                    // process corporate request
                    Profile savedProfile = processCorporateProfileUpdateRequest(profile.get(), details, corp);

                    return setProfileResponse(savedProfile);
                } else {
                    throw new CustomException("user with that id not found or is not a Corporate User",
                            HttpStatus.NOT_FOUND);
                }
            }
            throw new CustomException("user with that id not found or is not a Corporate User", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), exception, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Profile processCorporateProfileUpdateRequest(Profile profile, OtherDetails details,
                                                         UpdateCorporateProfileRequest corp) {
        try {
            profile.setSurname(corp.getSurname());
            profile.setFirstName(corp.getFirstName());
            profile.setMiddleName(corp.getMiddleName());
            profile.setEmail(corp.getEmail());
            profile.setPhoneNumber(corp.getPhoneNumber());
            profile.setAddress(corp.getAddress());
            profile.setCity(corp.getCity());
            profile.setDistrict(corp.getDistrict());
            profile.setState(corp.getState());
            profile.setGender(corp.getGender());
            profile.setDateOfBirth(corp.getDateOfBirth().toString());
            profile.setOtherDetails(details);

            log.info("updating  user profile with values ::: {}", profile);
            return profileRepository.save(profile);
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * This method updates a users profile image.
     *
     * @param userId user id
     * @param file   request
     */
    // @Async
    @Override
    public ApiResponseBody<String> updateProfileImage(Long userId, MultipartFile file) {
        try {
            String mimeType = Magic.getMagicMatch(file.getBytes(), false).getMimeType();
            if (mimeType.startsWith("image/")) {
                // It's an image.
                Optional<Profile> profile = profileRepository.findByUserId(false, String.valueOf(userId));
                if (profile.isPresent()) {
                    Profile item = profile.get();
                    ApiResponseBody<ImageUrlResponse> response = uploadImage(fileResourceServiceFeignClient, file, String.valueOf(userId),
                            log);
                    if (response.getStatus()) {
                        // update the profile image
                        item.setProfileImage(response.getData().getImageUrl());
                        // save back to the database
                        profileRepository.save(item);
                    }
                    return new ApiResponseBody<>("", "Uploaded Successfully", true);
                }
                throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
            } else {
                return new ApiResponseBody<>("Invalid Image Passed", "Error", false);
            }
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            return new ApiResponseBody<>("Invalid Image Passed", "Error", false);
        } catch (IOException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            return new ApiResponseBody<>("Error uploading Image Passed", "Error", false);
        } catch (Exception exception) {
            if (exception instanceof CustomException) {
                CustomException ex = (CustomException) exception;
                throw new CustomException(ex.getMessage(), ex.getStatus());
            }
            throw new CustomException(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param userId: Corporate UserId
     * @param file:   Multipart Image file to upload
     * @param type:   either FRONT, LEFT or RIGHT
     * @return ApiResponseBody<String>
     */
    @Override
    public ApiResponseBody<String> uploadOtherImage(Long userId, MultipartFile file, String type) {
        try {
            String mimeType = Magic.getMagicMatch(file.getBytes(), false).getMimeType();
            if (mimeType.startsWith("image/")) {
                // It's an image.
                Optional<Profile> profile = profileRepository.findByUserId(false, String.valueOf(userId));
                if (profile.isPresent()) {
                    Profile item = profile.get();
                    if (item.isCorporate()) {
                        String fileName = String.format("%s_%s", item.getFirstName(), type);
                        ApiResponseBody<String> response = fileResourceServiceFeignClient
                                .uploadOtherImage(file, fileName, String.valueOf(userId));

                        log.info("Response from Upload:: {}", response.toString());
                        if (response.getStatus()) {
                            // update the profile image
                            switch (type) {
                                case "FRONT":
                                    item.getOtherDetails().setFrontImage(response.getData());
                                    break;
                                case "LEFT":
                                    item.getOtherDetails().setLeftImage(response.getData());
                                    break;
                                case "RIGHT":
                                    item.getOtherDetails().setRightImage(response.getData());
                                    break;
                            }
                            // save back to the database
                            profileRepository.save(item);
                        }
                        return new ApiResponseBody<>(Constant.SUCCESS_MESSAGE, "Uploaded Successfully", true);
                    }
                    throw new CustomException("User is not a Corporate Account Holder", HttpStatus.BAD_REQUEST);
                }
                throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
            } else {
                throw new CustomException("Invalid Image Passed", HttpStatus.BAD_REQUEST);
            }
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException | IOException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            throw new CustomException("Invalid Image Passed", HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception exception) {
            if (exception instanceof CustomException) {
                CustomException ex = (CustomException) exception;
                throw new CustomException(ex.getMessage(), ex.getStatus());
            }
            throw new CustomException(exception.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * search for profile by name
     *
     * @param name fullName
     * @return List<ProfilePersonal>
     */
    @Override
    public List<SearchProfileResponse> searchProfileByName(String name) {
        try {
            return profileRepository.searchByName("%" + name.toLowerCase() + "%", false).stream()
                    .map(ProfileServiceImpl::apply).collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("caught an exception ::: {}", exception.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * search for profile by phone number
     *
     * @param phoneNumber phone number
     * @return List<ProfilePersonal>
     */
    @Override
    public List<SearchProfileResponse> searchProfileByPhoneNumber(String phoneNumber) {
        try {
            return profileRepository.searchByPhoneNumber("%" + phoneNumber + "%", false).stream()
                    .map(ProfileServiceImpl::apply).collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("caught an exception ::: {}", exception.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * search for profile by email
     *
     * @param email email
     * @return List<ProfilePersonal>
     */
    @Override
    public List<SearchProfileResponse> searchProfileByEmail(String email) {
        try {
            return profileRepository.searchByEmail("%" + email.toLowerCase() + "%", false).stream()
                    .map(ProfileServiceImpl::apply).collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("caught an exception ::: {}", exception.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * search profile by organization name
     *
     * @param name name
     * @return List<SearchProfileResponse>
     */
    @Override
    public List<SearchProfileResponse> searchProfileByOrganizationName(String name) {
        try {
            return profileRepository.searchByCompanyName("%" + name.toLowerCase() + "%", false).stream()
                    .map(ProfileServiceImpl::apply).collect(Collectors.toList());

        } catch (Exception exception) {
            throw new CustomException("could not process request", exception, HttpStatus.UNPROCESSABLE_ENTITY);
        }

    }

    public UserProfileResponse setProfileResponse(Profile profile) {
      


        // initialize to empty
        Optional<OtherDetails> otherDetails = Optional.empty();
        // check if other details is present in profile
        if (profile.getOtherDetails() != null) {
            otherDetails = Optional.of(profile.getOtherDetails());
        }
//        ReferralCodePojo referralCodePojo = null;
//        try{
//         referralCodePojo = checkReferralCode(profile.getUserId());
//        log.info("referralCodePojo :::: " + referralCodePojo.getReferralCode());
//        } catch (Exception exception) {
//            throw new CustomException(exception.getMessage(), exception, HttpStatus.UNPROCESSABLE_ENTITY);
//        }
        // get referralCodeValue for this user
        Optional<ReferralCode> referralCode;
        String referralCodeValue = null;
        if (profile.getUserId() != null) {
            referralCode = referralCodeRepository.findByUserId(profile.getUserId().toString());
            if (referralCode.isPresent()) {
                referralCodeValue = referralCode.get().getReferralCode();
               // referralCodeValue = referralCodePojo.getReferralCode();
            }
        }

        // check user SMS alert Status
        Optional<SMSAlertConfig> smsAlertConfig;
        boolean isSMSAlertActive = false;
        if (profile.getUserId() != null) {
            smsAlertConfig = smsAlertConfigRepository.findByPhoneNumber(profile.getPhoneNumber());
            if (smsAlertConfig.isPresent()) {
                isSMSAlertActive = smsAlertConfig.get().isActive();
            }
        }

        // initialize to null
        OtherDetailsResponse otherdetailsResponse = null;
        // map data if present
        if (otherDetails.isPresent()) {
            otherdetailsResponse = modelMapper.map(otherDetails.get(), OtherDetailsResponse.class);
        }
        return UserProfileResponse.builder()
                .address(profile.getAddress())
                .district(profile.getDistrict())
                .profileImage(profile.getProfileImage())
                .email(profile.getEmail()).gender(profile.getGender())
                .id(profile.getId().toString())
                .dateOfBirth(profile.getDateOfBirth())
                .firstName(profile.getFirstName())
                .surname(profile.getSurname())
                .middleName(profile.getMiddleName())
                .phoneNumber(profile.getPhoneNumber())
                .referral(profile.getReferral())
                .referenceCode(referralCodeValue)
                .smsAlertConfig(isSMSAlertActive)
                .userId(profile.getUserId().toString())
                .city(profile.getCity())
                .corporate(profile.isCorporate())
                .deviceToken(profile.getDeviceToken())
                .otherDetails(otherdetailsResponse).build();

    }


    private ApiResponseBody<String> validationCheckOnProfile(Optional<Profile> profile,
                                                             Optional<ReferralCode> referralCodePojo) {
        if (profile.isPresent()) {
            return new ApiResponseBody<>(null, DUPLICATE_KEY, false);
        }
        if (referralCodePojo.isPresent()) {
            return new ApiResponseBody<>(null, "user id already exists", false);
        } else {
            return new ApiResponseBody<>(null, "", true);
        }
    }

    private ApiResponseBody<String> validationCheckOnProfile2(Optional<Profile> profile,
                                                              ReferralCodePojo referralCodePojo) {
        if (profile.isPresent()) {
            return new ApiResponseBody<>(null, DUPLICATE_KEY, false);
        }
        if (referralCodePojo !=null) {
            return new ApiResponseBody<>(null, "user id already exists", false);
        } else {
            return new ApiResponseBody<>(null, "", true);
        }
    }

    /**
     * Create a wayagram profile
     *
     * @param name name for display
     * @param userId of the Auth User
     * @param username to use for Wayagram creation
     */
    private void createWayagramProfile(String userId, String username, String name) {

        log.info("Creating waya gram Profile  with userid .....{}", userId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String createUrl = wayagramUrl + "/main/profile/create";

            CreateWayagram createWayagram = new CreateWayagram();
            createWayagram.setUser_id(userId);
            createWayagram.setUsername(username);
            createWayagram.setDisplayName(name);
            createWayagram.setNotPublic(false);

            HttpEntity<CreateWayagram> entity = new HttpEntity<>(createWayagram, headers);
            ResponseEntity<String> response = restClient.postForEntity(createUrl, entity, String.class);
            if (response.getStatusCode() == OK) {
                log.info("Wayagram profile Request Successful with body:: {}", response.getBody());
                log.info("creating auto follow.....");
                // make call to auto follow makeAutoFollow
                // Executor delayed = CompletableFuture.delayedExecutor(5L, TimeUnit.SECONDS);
                CompletableFuture.runAsync(() -> makeAutoFollow(userId)).orTimeout(3, TimeUnit.MINUTES)
                        .handle((res, ex) -> {
                            if (ex != null) {
                                log.error("Error With Setting up Wayagram Auto-follow:: {}", ex.getMessage());
                            }
                            return res;
                        });
            } else {
                log.info("Wayagram profile Request Failed with body:: {}", response.getStatusCode());
            }
        } catch (Exception unhandledException) {
            log.error("Exception was thrown while creating Waya profile ... {}", unhandledException.getMessage());
        }
    }

    private void makeAutoFollow(String userId) {
        try {
            log.info("creating auto follow ... {}", userId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String autoFollowUrl = wayagramUrl + "/graph/friend/waya-auto-follow";
            UserIDPojo userIdPojo = new UserIDPojo(userId);
            HttpEntity<UserIDPojo> entity = new HttpEntity<>(userIdPojo, headers);
            ResponseEntity<String> response = restClient.postForEntity(autoFollowUrl, entity, String.class);
            if (response.getStatusCode() == OK) {
                log.info("wayaOfficialHandle follow has been created:: {}", response.getBody());
            } else {
                log.info("wayaOfficialHandle  Request Failed with body:: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("wayaOfficialHandle  Exception: {} ", e.getMessage());
        }
    }

    /**
     * @param deleteRequest deleteRequest
     * @return DeleteResponse
     */
    public DeleteResponse toggleDelete(DeleteRequest deleteRequest) {
        DeleteResponse deleteResponse = new DeleteResponse();
        try {
            if (deleteRequest.getDeleteType().equals(DeleteType.DELETE)) {
                Optional<Profile> optionalProfile = profileRepository.findByUserId(false, deleteRequest.getUserId().toString());
                if (optionalProfile.isPresent()) {
                    Profile profile = optionalProfile.get();
                    log.info("profile found :: {}", profile);
                    profile.setDeleted(true);
                    profileRepository.saveAndFlush(profile);
                    deleteResponse.setCode("200");
                    deleteResponse.setMessage("Deletion successful");
                } else {
                    deleteResponse.setCode("300");
                    deleteResponse.setError("Profile with userId do not exist or already deleted");
                }
            } else if (deleteRequest.getDeleteType().equals(DeleteType.RESTORE)) {
                Optional<Profile> optionalProfile = profileRepository.findByUserId(true, deleteRequest.getUserId().toString());
                if (optionalProfile.isPresent()) {
                    Profile profile = optionalProfile.get();
                    profile.setDeleted(false);
                    profileRepository.saveAndFlush(profile);
                    deleteResponse.setCode("200");
                    deleteResponse.setMessage("Profile has been restored");
                } else {
                    deleteResponse.setCode("300");
                    deleteResponse.setError("Profile with userId do not exist or already restored");
                }
            } else {
                deleteResponse.setCode("401");
                deleteResponse.setError("Invalid delete type, try RESTORE OR DELETE");
            }

        } catch (Exception e) {
            log.error("Error while calling toggle delete:: {}", e.getMessage());
            deleteResponse.setCode("400");
            deleteResponse.setError("Error while performing operation");
            return deleteResponse;
        }
        return deleteResponse;
    }

    public List<SMSAlertConfig> getPhoneNumber(String phoneNumber){
        return smsAlertConfigRepository.findByAllPhoneNumber(phoneNumber);
    }

    public SMSResponse toggleSMSAlert(SMSRequest smsRequest) {
        try{


            Users user = userRepository.findByPhoneNumber(smsRequest.getPhoneNumber())
                    .orElse(null);
            if (user == null) {
                throw new CustomException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User: " +
                        smsRequest.getPhoneNumber(), HttpStatus.NOT_FOUND);
            }

            SMSAlertConfig smsCharges = smsAlertConfigRepository.findByPhone(smsRequest.getPhoneNumber());

            SMSResponse SMSResponse;
            if (smsCharges !=null) {
                smsCharges.setActive(!smsCharges.isActive());
                smsCharges.setUserId(user.getId());
                smsCharges = smsAlertConfigRepository.save(smsCharges);
                SMSResponse = new SMSResponse(smsCharges.getId(),"",
                        smsCharges.getPhoneNumber(),
                        smsCharges.isActive(), smsCharges.getUserId());
            } else {
                SMSAlertConfig smsCharges1 = new SMSAlertConfig();
                smsCharges1.setActive(smsCharges1.isActive());
                smsCharges1.setPhoneNumber(smsRequest.getPhoneNumber());
                smsCharges1.setUserId(user.getId());
                smsCharges1 = smsAlertConfigRepository.save(smsCharges1);
                SMSResponse = new SMSResponse(smsCharges1.getId(), "",smsCharges1.getPhoneNumber(),
                        smsCharges1.isActive(), smsCharges.getUserId());
            }
            return SMSResponse;
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public SMSResponse getSMSAlertStatus(String phoneNumber) {
        SMSResponse SMSResponse = null;
        if (Objects.isNull(phoneNumber) || phoneNumber.isEmpty()) {
            throw new CustomException(PHONE_NUMBER_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        try{
            SMSAlertConfig smsCharges = smsAlertConfigRepository.findByPhone(phoneNumber);
            log.info("smsCharges ::: " +smsCharges);

            if (smsCharges !=null) {
                SMSResponse = new SMSResponse(smsCharges.getId(), "",smsCharges.getPhoneNumber(),
                        smsCharges.isActive(), smsCharges.getUserId());
            }else{
                throw new CustomException("PhoneNumber not found", HttpStatus.BAD_REQUEST);
            }
            return SMSResponse;
        }catch (Exception exception){
            throw new CustomException(exception.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }

    @Override
    public void sendWelcomeEmail(Users user) {
        WelcomeEmailContext emailContext = new WelcomeEmailContext();
        emailContext.init(user);
        try {
            messagingService.sendMail(emailContext);
        } catch (Exception e) {
            log.error("An Error Occurred:: {}", e.getMessage());
        }
        log.info("Welcome email sent!! \n");
    }

    @Override
    public UserProfileResponse getProfileByReferralCode(String referralCode) {
        try {
            String token = BearerTokenUtil.getBearerTokenHeader();

            ResponseEntity<ApiResponseBody<ReferralCodePojo>> responseEntity = referralProxy.getReferralCodeByCode(referralCode,token);
            ApiResponseBody<ReferralCodePojo> responseBody = responseEntity.getBody();
            ReferralCodePojo referralCodePojo = responseBody.getData();

            log.info("Inside getProfileByReferralCode :: {} " + referralCodePojo);

            ReferralCode referral = referralCodeRepository.getReferralCodeByCode(referralCode).orElse(null);
            if (referral == null) {
                throw new CustomException("Referral Code Supplied does not exist", HttpStatus.BAD_REQUEST);
            }
            return setProfileResponse(referral.getProfile());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public List<WalletTransactionPojo> sendSignUpBonusToUser(String userId){
        BonusTransferRequest transfer = new BonusTransferRequest();

        String token = BearerTokenUtil.getBearerTokenHeader();

        // get users wallet
        ResponseEntity<ApiResponseBody<NewWalletResponse>> responseEntity1 = walletProxy.getDefaultWallet(userId, token);
        ApiResponseBody<NewWalletResponse> infoResponse2 = responseEntity1.getBody();
        NewWalletResponse mainWalletResponse2 = infoResponse2.getData();

        // get the referral Amount

        ReferralBonus referralBonus = referralBonusRepository.findByActive(true);

        // build the request body
        transfer.setAmount(referralBonus.getAmount());
        transfer.setCustomerCreditAccount(mainWalletResponse2.getAccountNo());
        transfer.setOfficeDebitAccount(referralAccount);
        transfer.setPaymentReference(CommonUtils.generatePaymentTransactionId());
        transfer.setTranCrncy("NGN");
        transfer.setTranType("LOCAL");
        transfer.setTranNarration("REFERRAL-BONUS-PAYMENT");


        try{
            // make a call to credit users wallet
            ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> responseEntity = walletProxy.sendSignUpBonusToWallet(transfer,token);
            ApiResponseBody<List<WalletTransactionPojo>> infoResponse = (ApiResponseBody<List<WalletTransactionPojo>>) responseEntity.getBody();

            List<WalletTransactionPojo> mainWalletResponse = infoResponse.getData();
            log.info("mainWalletResponse :: {} " +mainWalletResponse);
            return mainWalletResponse;

        } catch (Exception e) {
            System.out.println("Error is here " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }



    public UserProfileResponse saveDeviceToken(DeviceTokenRequest deviceTokenRequest) throws Exception {

        Optional<Profile> profile = profileRepository.findByUserId(false, deviceTokenRequest.getUserId().toString());

        if (!profile.isPresent())
            throw new CustomException("ID not found", HttpStatus.NOT_FOUND);

        profile.get().setDeviceToken(deviceTokenRequest.getToken());

        return setProfileResponse(profileRepository.save(profile.get()));

    }












}