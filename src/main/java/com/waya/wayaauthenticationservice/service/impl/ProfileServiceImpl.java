package com.waya.wayaauthenticationservice.service.impl;

import static com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient.uploadImage;
import static com.waya.wayaauthenticationservice.util.Constant.CATCH_EXCEPTION_MSG;
import static com.waya.wayaauthenticationservice.util.Constant.COULD_NOT_PROCESS_REQUEST;
import static com.waya.wayaauthenticationservice.util.Constant.CREATE_PROFILE_SUCCESS_MSG;
import static com.waya.wayaauthenticationservice.util.Constant.DUPLICATE_KEY;
import static com.waya.wayaauthenticationservice.util.Constant.ID_IS_REQUIRED;
import static com.waya.wayaauthenticationservice.util.Constant.ID_IS_UNKNOWN;
import static com.waya.wayaauthenticationservice.util.Constant.LIMIT;
import static com.waya.wayaauthenticationservice.util.Constant.PHONE_NUMBER_REQUIRED;
import static com.waya.wayaauthenticationservice.util.Constant.PROFILE_NOT_EXIST;
import static com.waya.wayaauthenticationservice.util.Constant.REFERRAL_CODE_LENGHT;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.generateReferralCode;
import static com.waya.wayaauthenticationservice.util.profile.ProfileServiceUtil.validateNum;
import static org.springframework.http.HttpStatus.OK;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.waya.wayaauthenticationservice.entity.OtherDetails;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.entity.SMSAlertConfig;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.mail.context.WelcomeEmailContext;
import com.waya.wayaauthenticationservice.pojo.others.CorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.DeleteRequest;
import com.waya.wayaauthenticationservice.pojo.others.OtherDetailsRequest;
import com.waya.wayaauthenticationservice.pojo.others.PersonalProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.SMSChargeFeeRequest;
import com.waya.wayaauthenticationservice.pojo.others.ToggleSMSRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdateCorporateProfileRequest;
import com.waya.wayaauthenticationservice.pojo.others.UpdatePersonalProfileRequest;
import com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient;
import com.waya.wayaauthenticationservice.repository.OtherDetailsRepository;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.repository.SMSAlertConfigRepository;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import com.waya.wayaauthenticationservice.response.DeleteResponse;
import com.waya.wayaauthenticationservice.response.OtherdetailsResponse;
import com.waya.wayaauthenticationservice.response.ProfileImageResponse;
import com.waya.wayaauthenticationservice.response.SMSChargeResponse;
import com.waya.wayaauthenticationservice.response.SearchProfileResponse;
import com.waya.wayaauthenticationservice.response.ToggleSMSResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.EmailService;
import com.waya.wayaauthenticationservice.service.MailService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.SMSTokenService;

@Service
public class ProfileServiceImpl implements ProfileService {

    //    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
//    private static final String SECRET_TOKEN = "wayas3cr3t";
//    private static final String TOKEN_PREFIX = "serial ";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;
    private final SMSTokenService smsTokenService;
    private final EmailService emailService;
    private final FileResourceServiceFeignClient fileResourceServiceFeignClient;
    private final OtherDetailsRepository otherDetailsRepository;
    private final RestTemplate restClient;
    private final SMSAlertConfigRepository smsAlertConfigRepository;
    private final MailService mailService;
    private final ReferralCodeRepository referralCodeRepository;

    @Value("${app.config.wayagram-profile.base-url}")
    private String getAddUrl;
    @Value("${app.config.auto.follow.base-url}")
    private String getAutoFollowUrl;
    @Value("${app.config.auto.follow.base-url}")
    private String getProfileUrl;


    @Autowired
    public ProfileServiceImpl(ModelMapper modelMapper,
                              ProfileRepository profileRepository,
                              SMSTokenService smsTokenService,
                              FileResourceServiceFeignClient fileResourceServiceFeignClient,
                              OtherDetailsRepository otherDetailsRepository,
                              @Qualifier("restClient") RestTemplate restClient,
                              SMSAlertConfigRepository smsAlertConfigRepository,
                              EmailService emailService,
                              MailService mailService, ReferralCodeRepository referralCodeRepository) {
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.smsTokenService = smsTokenService;
        this.fileResourceServiceFeignClient = fileResourceServiceFeignClient;
        this.otherDetailsRepository = otherDetailsRepository;
        this.restClient = restClient;
        this.smsAlertConfigRepository = smsAlertConfigRepository;
        this.emailService = emailService;
        this.mailService = mailService;
        this.referralCodeRepository = referralCodeRepository;
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
            //ReferralCodePojo referralCodePojo = referralProxy.getReferralCodeByUserId(userId);

            ReferralCode referrals = referralCodeRepository.getReferralCodeByUserId(userId);

            return profileRepository.findAllByReferralCode(referrals.getReferralCode(), LIMIT,
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

            ReferralCode referralCode1 = referralCodeRepository.getReferralCodeByUserId(request.getReferralCode());
            if(referralCode1 == null)
                throw new CustomException("Please enter a valid referral code", HttpStatus.NO_CONTENT);
            //check if the user exist in the profile table
            Optional<Profile> profile = profileRepository.findByEmail(
                    false, request.getEmail().trim());
            //check if the user exist in the referral table
            ///get-user-by-referral-code/{userId}
//            ReferralCodePojo referralCodePojo = referralProxy.getUserByReferralCode(request.getUserId());
            Optional<ReferralCode> referralCode = referralCodeRepository
                    .findByUserId(request.getUserId());
            //validation check
            ApiResponse<String> validationCheck = validationCheckOnProfile(profile, referralCode);
            if (validationCheck.getStatus()) {
                Profile newProfile = modelMapper.map(request, Profile.class);
                // check if this referral code is already mapped to a user
                newProfile.setReferral(request.getReferralCode());
                newProfile.setCorporate(false);
                //save new personal profile
                Profile savedProfile = profileRepository.save(newProfile);
                log.info("saving new personal profile ::: {}", newProfile);
                //save referral code
                saveReferralCode(savedProfile, request.getUserId());
                // CompletableFuture.runAsync(() -> saveReferralCode(savedProfile, request.getUserId()));

                String fullName = String.format("%s %s", savedProfile.getFirstName(),
                        savedProfile.getSurname());

                //send otp
                CompletableFuture.runAsync(() -> smsTokenService.sendSMSOTP(
                        savedProfile.getPhoneNumber(), fullName));

                // send email otp
                CompletableFuture.runAsync(() -> emailService.sendAcctVerificationEmailToken(
                        baseUrl, savedProfile.getEmail()));

                //create waya gram profile
                CompletableFuture.runAsync(() -> createWayagramProfile(savedProfile.getUserId(), savedProfile.getSurname(), fullName));
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
            // validate that the user ID exist


            //check if the user exist in the profile table
            Optional<Profile> profile = profileRepository.findByEmail(
                    false, profileRequest.getEmail().trim());
            //check if the user exist in the referral table
            // now this check will extend to the referral service

//          ReferralCodePojo referralCodePojo = referralProxy.getUserByReferralCode(profileRequest.getUserId());

            Optional<ReferralCode> referralCode = referralCodeRepository
                    .findByUserId(profileRequest.getUserId());
            //validation check
            ApiResponse<String> validationCheck = validationCheckOnProfile(profile, referralCode);

            if (validationCheck.getStatus()) {
                Profile newCorporateProfile = saveCorporateProfile(profileRequest);
                //save the referral code
                // make request to the referral service
                saveReferralCode(newCorporateProfile, profileRequest.getUserId());

                String fullName = String.format("%s %s", newCorporateProfile.getFirstName(),
                        newCorporateProfile.getSurname());
                //String message = VERIFY_EMAIL_TOKEN_MESSAGE + "placeholder" + MESSAGE_2;
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
//    private void saveReferralCode(Profile newProfile, String userId) {
//        ProfileDto profileDto = new ProfileDto();
//
//        profileDto.setAddress(newProfile.getAddress());
//        profileDto.setCity(newProfile.getCity());
//        profileDto.setCorporate(false);
//        profileDto.setDateOfBirth(newProfile.getDateOfBirth());
//        profileDto.setDistrict(newProfile.getDistrict());
//        profileDto.setEmail(newProfile.getEmail());
//        profileDto.setFirstName(newProfile.getFirstName());
//        profileDto.setGender(newProfile.getGender());
//        profileDto.setMiddleName(newProfile.getMiddleName());
//        profileDto.setOrganisationName(newProfile.getOrganisationName());
//        profileDto.setSurname(newProfile.getSurname());
//        profileDto.setUserId(newProfile.getUserId());
//        profileDto.setPhoneNumber(newProfile.getPhoneNumber());
//        profileDto.setReferral(newProfile.getReferral());
//        profileDto.setState(newProfile.getState());
//
//        try {
//            log.info("saving referral code for this new profile");
//            ResponseEntity<String> response = referralProxy.saveReferralCode(profileDto, userId);
//            log.info("Response: {}", response.getBody());
//        } catch (Exception exception) {
//            log.error(exception.getMessage());
//            throw new CustomException(exception.getMessage(), HttpStatus.BAD_REQUEST);
//        }

//        ReferralCodePojo pro saveReferralCode

    /**
     * check for the availability of the service
     * rollback if the service is unavailable
     */
    // provide endpoint to send data to referral service
    private void saveReferralCode(Profile newProfile, String userId) {
        // send details to the referral Service
        referralCodeRepository.save(
                new ReferralCode(generateReferralCode(REFERRAL_CODE_LENGHT),
                        newProfile, userId));

        log.info("saving referral code for this new profile");
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
            if (!profile.isPresent())
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

        // get referralcode for this user
        Optional<ReferralCode> referralCode = Optional.empty();
        if (profile.getUserId() !=null) {
            referralCode = referralCodeRepository.findByUserId(profile.getUserId());
        }

        // check user SMS alert Status
        Optional<SMSAlertConfig> smsAlertConfig;
        boolean isSMSAlertActive = false;
        if (profile.getUserId() !=null) {
            smsAlertConfig = smsAlertConfigRepository.findByPhoneNumber(profile.getPhoneNumber());
            if (smsAlertConfig.get().isActive()){
                isSMSAlertActive = true;
            }
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
                .referenceCode(referralCode.get().getReferralCode())
                .smsAlertConfig(isSMSAlertActive)
                .userId(profile.getUserId())
                .city(profile.getCity())
                .corporate(profile.isCorporate())
                .otherDetails(otherdetailsResponse)
                .build();
    }

    private ApiResponse<String> validationCheckOnProfile(
            Optional<Profile> profile, Optional<ReferralCode> referralCodePojo) {

        if (profile.isPresent()) {
            return new ApiResponse<>(null,
                    DUPLICATE_KEY, false, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (referralCodePojo.isPresent()) {
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
    private void createWayagramProfile(String userId, String username, String name) {

        log.info("Creating waya gram Profile  with userid .....{}", userId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("username", username);
            map.put("displayName", name);
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
        try {
            mailService.sendMail(emailContext);
        } catch (Exception e) {
            log.error("An Error Occurred:: {}", e.getMessage());
        }
        // mailService.sendMail(user.getEmail(), message);
        log.info("Welcome email sent!! \n");

    }

    @Override
    public UserProfileResponse getProfileByReferralCode(String referralCode) {
        ReferralCode referralCode1;
        Optional<Profile> profile;
        try {
            referralCode1 = referralCodeRepository.getReferralCodeByUserId(referralCode);

            if (referralCode1 == null) {
                throw new CustomException("Null", HttpStatus.BAD_REQUEST);
            }

            profile =  profileRepository.findByUserId(false,referralCode1.getUserId());

            if (!profile.isPresent()) {
                throw new CustomException("Null", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.fillInStackTrace());
        }

        return setProfileResponse(profile.get());

    }
}