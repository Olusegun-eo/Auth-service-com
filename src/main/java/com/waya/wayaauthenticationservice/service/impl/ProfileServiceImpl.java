package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.OtherDetails;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.SMSAlertConfig;
import com.waya.wayaauthenticationservice.entity.SMSCharge;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.mail.context.WelcomeEmailContext;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient;
import com.waya.wayaauthenticationservice.proxy.ReferralProxy;
import com.waya.wayaauthenticationservice.repository.OtherDetailsRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.SMSAlertConfigRepository;
import com.waya.wayaauthenticationservice.repository.SMSChargeRepository;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.service.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient.uploadImage;
import static com.waya.wayaauthenticationservice.util.Constant.*;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.validateNum;
import static org.springframework.http.HttpStatus.OK;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    private static final String SECRET_TOKEN = "wayas3cr3t";
    private static final String TOKEN_PREFIX = "serial ";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;
    private final ReferralProxy referralProxy;
    private final SMSTokenService smsTokenService;
    private final EmailService emailService;
    private final FileResourceServiceFeignClient fileResourceServiceFeignClient;
    private final OtherDetailsRepository otherDetailsRepository;
    private final RestTemplate restClient;
    private final SMSAlertConfigRepository smsAlertConfigRepository;
    private final SMSChargeRepository smsChargeRepository;
    private final MessageQueueProducer messageQueueProducer;
    @Value("${app.config.main.profile.base-url}")
    private String getAddUrl;
    @Value("${app.config.auto.follow.base-url}")
    private String getAutoFollowUrl;
    @Value("${app.config.auto.follow.base-url}")
    private String getProfileUrl;
    private final MailService mailService;

    @Autowired
    public ProfileServiceImpl(ModelMapper modelMapper,
                              ProfileRepository profileRepository,
                              SMSTokenService smsTokenService,
                              FileResourceServiceFeignClient fileResourceServiceFeignClient,
                              OtherDetailsRepository otherDetailsRepository,
                              @Qualifier("restClient") RestTemplate restClient,
                              ReferralProxy referralProxy,
                              SMSAlertConfigRepository smsAlertConfigRepository,
                              SMSChargeRepository smsChargeRepository,
                              MessageQueueProducer messageQueueProducer,
                              EmailService emailService,
                              MailService mailService) {
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.smsTokenService = smsTokenService;
        this.fileResourceServiceFeignClient = fileResourceServiceFeignClient;
        this.otherDetailsRepository = otherDetailsRepository;
        this.restClient = restClient;
        this.referralProxy = referralProxy;
        this.smsAlertConfigRepository = smsAlertConfigRepository;
        this.smsChargeRepository = smsChargeRepository;
        this.emailService = emailService;
        this.messageQueueProducer = messageQueueProducer;
        this.mailService = mailService;
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
        //TODO: This method should be accessible by admins only
        try {

            if (validateNum(page).equals(false)) throw new CustomException("invalid page number",
                    HttpStatus.BAD_REQUEST);

            int parsePageNumber = Integer.parseInt(page);

            if (parsePageNumber > 0) parsePageNumber--;

            // make a call to the profile service to get getReferralCodeByUserId
            ReferralCodePojo referralCodePojo = referralProxy.getReferralCodeByUserId(userId);

            // ReferralCode referrals = referralCodeRepository.getReferralCodeByUserId(userId);

            return profileRepository.findAllByReferralCode(referralCodePojo.getReferralCode(), LIMIT,
                    parsePageNumber * LIMIT, false)
                    .stream().map(this::setProfileResponse)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), exception,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * creates a personal profile
     *
     * @param request profile
     */
    @Override
    public ApiResponse<String> createProfile(PersonalProfileRequest request, String baseUrl) {
        try {
            //check if the user exist in the profile table
            Optional<Profile> profile = profileRepository.findByEmail(
                    false, request.getEmail().trim());
            //check if the user exist in the referral table
            ///get-user-by-referral-code/{userId}

            ReferralCodePojo referralCodePojo = referralProxy.getUserByReferralCode(request.getUserId());

//          Optional<ReferralCode> referralCode = referralCodeRepository
//                    .findByUserId(request.getUserId());
            //validation check
            ApiResponse<String> validationCheck = validationCheckOnProfile(profile, referralCodePojo);

            if (validationCheck.getStatus()) {
                Profile newProfile = modelMapper.map(request, Profile.class);
                newProfile.setReferral(request.getReferralCode());
                newProfile.setCorporate(false);
                //save new personal profile
                Profile savedProfile = profileRepository.save(newProfile);
                log.info("saving new personal profile ::: {}", newProfile);
                //save referral code
                saveReferralCode(savedProfile, request.getUserId());

                String fullName = String.format("%s %s", savedProfile.getFirstName(),
                        savedProfile.getSurname());

                String message = VERIFY_EMAIL_TOKEN_MESSAGE + "placeholder" + MESSAGE_2;
                //send otp
                CompletableFuture.runAsync(() -> smsTokenService.sendSMSOTP(
                        savedProfile.getPhoneNumber(), fullName));

                // send email otp
                CompletableFuture.runAsync(() -> emailService.sendAcctVerificationEmailToken(
                        baseUrl, savedProfile.getEmail()));

                //create waya gram profile
                CompletableFuture.runAsync(() -> createWayagramProfile(savedProfile.getUserId(), savedProfile.getSurname()));
                return new ApiResponse<>(null,
                        CREATE_PROFILE_SUCCESS_MSG, true, OK);

            } else {
                //return the error
                return validationCheck;
            }
        } catch (DataIntegrityViolationException dve) {
            log.error(CATCH_EXCEPTION_MSG, dve);
            return new ApiResponse<>(null,
                    DUPLICATE_KEY,
                    false, HttpStatus.UNPROCESSABLE_ENTITY);

        } catch (Exception exception) {
            log.error(CATCH_EXCEPTION_MSG, exception);
            return new ApiResponse<>(null,
                    COULD_NOT_PROCESS_REQUEST,
                    false, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * create a new corporate profile.
     *
     * @param profileRequest corporate profile request
     */
    @Transactional
    @Override
    public ApiResponse<String> createProfile(CorporateProfileRequest profileRequest, String baseUrl) {
        try {
            //check if the user exist in the profile table
            Optional<Profile> profile = profileRepository.findByEmail(
                    false, profileRequest.getEmail().trim());
            //check if the user exist in the referral table
            // now this check will extend to the referral service

            ReferralCodePojo referralCodePojo = referralProxy.getUserByReferralCode(profileRequest.getUserId());

//            Optional<ReferralCode> referralCode = referralCodeRepository
//                    .findByUserId(profileRequest.getUserId());
            //validation check
            ApiResponse<String> validationCheck = validationCheckOnProfile(profile, referralCodePojo);

            if (validationCheck.getStatus()) {
                Profile newCorporateProfile = saveCorporateProfile(profileRequest);
                //save the referral code
                // make request to the referral service
                saveReferralCode(newCorporateProfile, profileRequest.getUserId());

                String fullName = String.format("%s %s", newCorporateProfile.getFirstName(),
                        newCorporateProfile.getSurname());
                String message = VERIFY_EMAIL_TOKEN_MESSAGE + "placeholder" + MESSAGE_2;
                //send sms otp
                CompletableFuture.runAsync(() -> smsTokenService.sendSMSOTP(
                        newCorporateProfile.getPhoneNumber(), fullName));

                // send email otp
                CompletableFuture.runAsync(() -> emailService.sendAcctVerificationEmailToken(
                       baseUrl, newCorporateProfile.getEmail()));

                return new ApiResponse<>(null,
                        CREATE_PROFILE_SUCCESS_MSG, true, OK);
            } else {
                //return the error
                return validationCheck;
            }
        } catch (DataIntegrityViolationException dve) {
            log.error(CATCH_EXCEPTION_MSG, dve.getMessage());
            return new ApiResponse<>(null,
                    DUPLICATE_KEY,
                    false, HttpStatus.UNPROCESSABLE_ENTITY);

        } catch (Exception exception) {
            log.error(CATCH_EXCEPTION_MSG, exception.getMessage());
            return new ApiResponse<>(null,
                    COULD_NOT_PROCESS_REQUEST,
                    false, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Profile saveCorporateProfile(CorporateProfileRequest profileRequest) {

        OtherDetailsRequest otherDetailsRequest = new OtherDetailsRequest();
        otherDetailsRequest.setBusinessType(profileRequest.getBusinessType());
        otherDetailsRequest.setOrganisationName(profileRequest.getOrganisationName());
        otherDetailsRequest.setOrganisationType(profileRequest.getOrganisationType());

        OtherDetails otherDetails = saveOtherDetails(otherDetailsRequest);
        Profile profile = new Profile();
        profile.setCorporate(true);
        profile.setEmail(profileRequest.getEmail());
        profile.setFirstName(profileRequest.getFirstName());
        profile.setSurname(profileRequest.getSurname());
        profile.setPhoneNumber(profileRequest.getPhoneNumber());
        profile.setUserId(profileRequest.getUserId());
        profile.setReferral(profileRequest.getReferralCode());
        profile.setOrganisationName(profileRequest.getOrganisationName());
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
        otherDetails.setId(otherDetailsRequest.getOtherDetailsId());
        otherDetails.setBusinessType(otherDetailsRequest.getBusinessType());
        otherDetails.setOrganisationName(otherDetailsRequest.getOrganisationName());
        otherDetails.setOrganisationType(otherDetailsRequest.getOrganisationType());

        otherDetails = otherDetailsRepository.save(otherDetails);
        return otherDetails;
    }

    /**
     * save referral code for the new profile.
     *
     * @param newProfile new profile
     */
    private void saveReferralCode(Profile newProfile, String userId) {

        try {
            log.info("saving referral code for this new profile");
            ResponseEntity<String> response = referralProxy.saveReferralCode(newProfile, userId);
            log.info("Response: {}", response.getBody());
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new CustomException(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }

//        ReferralCodePojo pro saveReferralCode
        /**
         * check for the availability of the service
         * rollback if the service is unavailable
         */
        // provide endpoint to send data to referral service


        // send details to the referral Service
//        referralCodeRepository.save(
//                new ReferralCode(generateReferralCode(REFERRAL_CODE_LENGHT),
//                        newProfile, userId));

    }

    /**
     * get user personal profile and also put in cache for subsequent request
     * notificat
     *
     * @param userId  user id
     * @param request http servelet request
     * @return PersonalProfileResponse
     */
    //@Cacheable(cacheNames = "PersonalProfile", key = "#userId")
    public UserProfileResponse getUserProfile(String userId, HttpServletRequest request) {
        try {
            Optional<Profile> profile = profileRepository.findByUserId(false, userId);
            if (profile.isPresent())
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
     * @param updatePersonalProfileRequest profile update request
     * @param userId                       user id
     * @return Object
     */
    //@CachePut(cacheNames = "PersonalProfile", key = "#userId")
    public UserProfileResponse updateProfile(
            UpdatePersonalProfileRequest updatePersonalProfileRequest, String userId) {

        try {
            Optional<Profile> profile = profileRepository.findByUserId(false, userId);

            if (profile.isPresent()) {
                Profile personalProfile = processPersonalProfileUpdateRequest(
                        updatePersonalProfileRequest, profile.get(), userId);

                return setProfileResponse(personalProfile);

            } else {
                throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
            }

        } catch (IllegalArgumentException illegalArgumentException) {
            throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.BAD_REQUEST);

        } catch (DataIntegrityViolationException dve) {
            throw new CustomException(PROFILE_NOT_EXIST, dve, HttpStatus.UNPROCESSABLE_ENTITY);

        } catch (Exception exception) {

            throw new CustomException(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private Profile processPersonalProfileUpdateRequest(
            UpdatePersonalProfileRequest updatePersonalProfileRequest,
            Profile profile, String userId) {

        Profile updatedProfile = modelMapper
                .map(updatePersonalProfileRequest, Profile.class);

        updatedProfile.setId(profile.getId());
        updatedProfile.setUserId(userId);
        updatedProfile.setProfileImage(profile.getProfileImage());

        log.info("updating  user profile ::: {}", updatedProfile);
        return profileRepository.save(updatedProfile);
    }

    /**
     * update a corporate profile
     *
     * @param corporateProfileRequest corporate profile request
     * @param userId                  user id
     * @return CorporateProfileResponse
     */
    @Override
    public UserProfileResponse updateProfile(
            UpdateCorporateProfileRequest corporateProfileRequest, String userId
    ) {
        try {
            Optional<Profile> profile = profileRepository.findByUserId(false, userId);

            if (profile.isPresent()) {
                //process corporate request
                Profile savedProfile = processCorporateProfileUpdateRequest(profile.get(),
                        corporateProfileRequest);

                OtherDetailsRequest otherDetailsRequest = new OtherDetailsRequest();
                otherDetailsRequest.setOtherDetailsId(profile.get().getOtherDetails().getId());
                otherDetailsRequest.setBusinessType(corporateProfileRequest.getBusinessType());
                otherDetailsRequest.setOrganisationType(corporateProfileRequest.getOrganisationType());
                otherDetailsRequest.setOrganisationName(corporateProfileRequest.getOrganisationName());

                saveOtherDetails(otherDetailsRequest);

                return setProfileResponse(savedProfile);

            } else {
                throw new CustomException("user with that id not found",
                        HttpStatus.NOT_FOUND);
            }
        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), exception,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Profile processCorporateProfileUpdateRequest(
            Profile profile, UpdateCorporateProfileRequest corporateProfileRequest
    ) {
        profile.setSurname(corporateProfileRequest.getSurname());
        profile.setFirstName(corporateProfileRequest.getFirstName());
        profile.setEmail(corporateProfileRequest.getOrganisationEmail());
        profile.setPhoneNumber(corporateProfileRequest.getPhoneNumber());
        profile.setAddress(corporateProfileRequest.getOfficeAddress());
        profile.setOrganisationName(corporateProfileRequest.getOrganisationName());
        profile.setCity(corporateProfileRequest.getCity());
        profile.setState(corporateProfileRequest.getState());
        profile.setGender(corporateProfileRequest.getGender());

        log.info("updating  user profile with values ::: {}", profile);
        return profileRepository.save(profile);
    }

    /**
     * This method updates a users profile image.
     *
     * @param userId       user id
     * @param profileImage request
     */
    @Async
    @Override
    public CompletableFuture<ApiResponse<ProfileImageResponse>> updateProfileImage(
            String userId, MultipartFile profileImage
    ) {
        try {
            CompletableFuture<Profile> profile = profileRepository
                    .findByUserIdAsync(false, userId);

            return profile.thenApply(item -> {
                if (item == null) throw new CustomException(
                        PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
                //call file resource service with feign client to upload and return image url
                ApiResponse<ProfileImageResponse> apiResponse = uploadImage(
                        fileResourceServiceFeignClient, profileImage, userId, log);
                //update the profile image
                item.setProfileImage(apiResponse.getData().getImageUrl());
                //save back to the database
                profileRepository.save(item);
                return apiResponse;
            });
        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(),
                    HttpStatus.BAD_REQUEST);
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
            return profileRepository.searchByName(
                    "%" + name.toLowerCase() + "%", false)
                    .stream().map(ProfileServiceImpl::apply)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
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
            return profileRepository.searchByPhoneNumber(
                    "%" + phoneNumber + "%", false)
                    .stream().map(ProfileServiceImpl::apply)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("caught an exception ::: ", exception);
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
            return profileRepository.searchByEmail(
                    "%" + email.toLowerCase() + "%", false)
                    .stream().map(ProfileServiceImpl::apply)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
            log.error("caught an exception ::: ", exception);
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
    public List<SearchProfileResponse> searchProfileByOrganizationName(
            String name
    ) {
        try {
            return profileRepository.searchByCompanyName(
                    "%" + name.toLowerCase() + "%", false)
                    .stream().map(ProfileServiceImpl::apply)
                    .collect(Collectors.toList());

        } catch (Exception exception) {
            throw new CustomException("could not process request",
                    exception, HttpStatus.UNPROCESSABLE_ENTITY);
        }

    }

    private UserProfileResponse setProfileResponse(Profile profile) {
        //initialize to empty
        Optional<OtherDetails> otherDetails = Optional.empty();
        //check if other details is present in profile
        if (profile.getOtherDetails() != null) {
            otherDetails = otherDetailsRepository
                    .findById(profile.getOtherDetails().getId());
        }
        //initialize to null
        OtherdetailsResponse otherdetailsResponse = null;
        //map data if present
        if (otherDetails.isPresent()) {
            otherdetailsResponse = modelMapper
                    .map(otherDetails.get(), OtherdetailsResponse.class);
        }
        return UserProfileResponse.builder()
                .address(profile.getAddress())
                .district(profile.getDistrict())
                .profileImage(profile.getProfileImage())
                .email(profile.getEmail())
                .gender(profile.getGender())
                .id(profile.getId().toString())
                .dateOfBirth(profile.getDateOfBirth())
                .firstName(profile.getFirstName())
                .surname(profile.getSurname())
                .middleName(profile.getMiddleName())
                .phoneNumber(profile.getPhoneNumber())
                .userId(profile.getUserId())
                .city(profile.getCity())
                .corporate(profile.isCorporate())
                .otherDetails(otherdetailsResponse)
                .build();
    }

    private ApiResponse<String> validationCheckOnProfile(
            Optional<Profile> profile, ReferralCodePojo referralCodePojo) {

        if (profile.isPresent()) {
            return new ApiResponse<>(null,
                    DUPLICATE_KEY, false, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (referralCodePojo != null) {
            return new ApiResponse<>(null, "user id already exists",
                    false, HttpStatus.UNPROCESSABLE_ENTITY);
        } else {
            return new ApiResponse<>(null, "",
                    true, OK);
        }
    }

    /**
     * Create a wayagram profile
     *
     * @param userId
     * @param username
     */
    private void createWayagramProfile(String userId, String username) {

        log.info("Creating waya gram Profile  with userid .....{}", userId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("username", username);
            map.put("notPublic", false);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restClient.postForEntity(getAddUrl, entity, String.class);
            if (response.getStatusCode() == OK) {
                log.info("Wayagram profile Request Successful with body:: {}", response.getBody());
                log.info("creating auto follow.....");
                //make call to auto follow makeAutoFollow
                //  Executor delayed = CompletableFuture.delayedExecutor(5L, TimeUnit.SECONDS);
                CompletableFuture.runAsync(() -> makeAutoFollow(userId));
            } else {
                log.info("Wayagram profile Request Failed with body:: {}", response.getStatusCode());
            }
        } catch (Exception unhandledException) {
            log.error("Exception was thrown while creating Waya profile ... ", unhandledException);
        }
    }

    /**
     * @param deleteRequest deleteRequest
     * @return
     */
    public ResponseEntity<DeleteResponse> toggleDelete(DeleteRequest deleteRequest) {
        DeleteResponse deleteResponse = new DeleteResponse();
        try {
            if (deleteRequest.getDeleteType().equals(DeleteType.DELETE)) {
                Optional<Profile> optionalProfile = profileRepository.findByUserId(false, deleteRequest.getUserId());
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
                Optional<Profile> optionalProfile = profileRepository.findByUserId(true, deleteRequest.getUserId());
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
            log.error("Error while calling toggle delete ", e);
            deleteResponse.setCode("400");
            deleteResponse.setError("Error while performing operation");
            return ResponseEntity.ok(deleteResponse);

        }
        return ResponseEntity.ok(deleteResponse);
    }

    public ToggleSMSResponse toggleSMSAlert(ToggleSMSRequest toggleSMSRequest) {
        if (Objects.isNull(toggleSMSRequest.getPhoneNumber())) {
            throw new CustomException(PHONE_NUMBER_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Optional<SMSAlertConfig> smsCharges = smsAlertConfigRepository.findByPhoneNumber(toggleSMSRequest.getPhoneNumber());

        ToggleSMSResponse toggleSMSResponse;
        if (smsCharges.isPresent()) {
            smsCharges.get().setActive(!smsCharges.get().isActive());
            smsAlertConfigRepository.save(smsCharges.get());
            toggleSMSResponse = new ToggleSMSResponse(smsCharges.get().getId(), smsCharges.get().getPhoneNumber(), smsCharges.get().isActive());
        } else {
            SMSAlertConfig smsCharges1 = new SMSAlertConfig();
            smsCharges1.setActive(smsCharges1.isActive());
            smsCharges1.setPhoneNumber(toggleSMSRequest.getPhoneNumber());
            smsCharges1 = smsAlertConfigRepository.save(smsCharges1);
            toggleSMSResponse = new ToggleSMSResponse(smsCharges1.getId(), smsCharges1.getPhoneNumber(), smsCharges1.isActive());
        }
        return toggleSMSResponse;
    }

    public SMSChargeResponse configureSMSCharge(SMSChargeFeeRequest smsChargeFeeRequest) {
        SMSChargeResponse smsChargeResponse = null;
        try {
            SMSCharge smsCharge = new SMSCharge();
            smsCharge.setFee(smsChargeFeeRequest.getFee());
            smsCharge = smsChargeRepository.save(smsCharge);
            smsChargeResponse = new SMSChargeResponse(smsCharge.getId(), smsCharge.getFee(), smsCharge.isActive());
            log.info("ProfileService::: {} done creating SMS Charge");
        } catch (Exception ex) {
            log.error("Cannot create configureSMSCharge {}", ex.getMessage());
            throw new CustomException("Cannot create configureSMSCharge ", HttpStatus.BAD_REQUEST);
        }
        return smsChargeResponse;
    }

    public ToggleSMSResponse getSMSAlertStatus(String phoneNumber) {
        ToggleSMSResponse toggleSMSResponse = null;
        if (Objects.isNull(phoneNumber) || phoneNumber.isEmpty()) {
            throw new CustomException(PHONE_NUMBER_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Optional<SMSAlertConfig> smsCharges = smsAlertConfigRepository.findByPhoneNumber(phoneNumber);
        if (smsCharges.isPresent()) {
            toggleSMSResponse = new ToggleSMSResponse(smsCharges.get().getId(), smsCharges.get().getPhoneNumber(), smsCharges.get().isActive());
        }
        return toggleSMSResponse;
    }

    public SMSChargeResponse toggleSMSCharge(Long id) throws CustomException {
        if (Objects.isNull(id)) {
            throw new CustomException(ID_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        SMSCharge smsCharge = smsChargeRepository.findById(id).orElseThrow(() -> new CustomException(ID_IS_UNKNOWN, HttpStatus.BAD_REQUEST));
        smsCharge.setActive(!smsCharge.isActive());
        smsCharge = smsChargeRepository.save(smsCharge);
        return new SMSChargeResponse(smsCharge.getId(), smsCharge.getFee(), smsCharge.isActive());

    }

    public SMSChargeResponse getActiveSMSCharge() {
        SMSChargeResponse smsChargeResponse = null;
        try {
            Optional<SMSCharge> smsCharge = smsChargeRepository.findByActive();
            if (smsCharge.isPresent()) {
                smsChargeResponse = new SMSChargeResponse(smsCharge.get().getId(), smsCharge.get().getFee(), smsCharge.get().isActive());
            }
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return smsChargeResponse;
    }

    private void makeAutoFollow(String userId) {
        try {
            log.info("creating auto follow ... {}", userId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", userId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restClient.postForEntity(getAutoFollowUrl, entity, String.class);
            if (response.getStatusCode() == OK) {
                log.info("wayaOfficialHandle follow has been created:: {}", response.getBody());
            } else {
                log.info("wayaOfficialHandle  Request Failed with body:: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("wayaOfficialHandle  Exception: ", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String email) {
        Profile userProfile = profileRepository.findByEmail(false, email)
                .orElseThrow(() -> new CustomException("profile does not exist", HttpStatus.NOT_FOUND));
        WelcomeEmailContext emailContext = new WelcomeEmailContext();
        emailContext.init(userProfile);
        try{
            mailService.sendMail(emailContext);
        }catch(Exception e){
            log.error("An Error Occurred:: {}", e.getMessage());
        }
        // mailService.sendMail(user.getEmail(), message);
        log.info("Welcome email sent!! \n");
    }
}