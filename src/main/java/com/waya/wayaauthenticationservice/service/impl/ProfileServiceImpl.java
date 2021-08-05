package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.*;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.mail.context.WelcomeEmailContext;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserIDPojo;
import com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient;
import com.waya.wayaauthenticationservice.proxy.WayagramProxy;
import com.waya.wayaauthenticationservice.repository.*;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.service.MailService;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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

    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final OTPTokenService otpTokenService;
    private final FileResourceServiceFeignClient fileResourceServiceFeignClient;
    private final OtherDetailsRepository otherDetailsRepository;
    private final WayagramProxy wayagramProxy;
    private final SMSAlertConfigRepository smsAlertConfigRepository;
    private final MailService mailService;
    private final ReferralCodeRepository referralCodeRepository;

    @Autowired
    public ProfileServiceImpl(ModelMapper modelMapper,
                              ProfileRepository profileRepository,
                              UserRepository userRepository,
                              OTPTokenService otpTokenService,
                              FileResourceServiceFeignClient fileResourceServiceFeignClient,
                              OtherDetailsRepository otherDetailsRepository,
                              WayagramProxy wayagramProxy,
                              SMSAlertConfigRepository smsAlertConfigRepository,
                              MailService mailService, ReferralCodeRepository referralCodeRepository) {
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.otpTokenService = otpTokenService;
        this.fileResourceServiceFeignClient = fileResourceServiceFeignClient;
        this.otherDetailsRepository = otherDetailsRepository;
        this.wayagramProxy = wayagramProxy;
        this.smsAlertConfigRepository = smsAlertConfigRepository;
        this.mailService = mailService;
        this.referralCodeRepository = referralCodeRepository;
        this.userRepository = userRepository;
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
            if(referrals == null){
                return Collections.emptyList();
            }
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
            Users user = this.userRepository.findById(false, Long.valueOf(request.getUserId())).orElse(null);
            if(user == null) throw new CustomException("Base User with Provided ID not Found", HttpStatus.BAD_REQUEST);

            Optional<Profile> profileWithUserId = profileRepository.findByUserId(false, request.getUserId());
            if(profileWithUserId.isPresent()) throw new CustomException("Profile with Provided User ID already Exists", HttpStatus.BAD_REQUEST);

            if(request.getReferralCode() != null && !request.getReferralCode().isBlank()){
                ReferralCode referralCode1 = referralCodeRepository.getReferralCodeByUserId(request.getReferralCode());
                if(referralCode1 == null)
                    request.setReferralCode(null);
            }
            //check if the user exist in the profile table
            Optional<Profile> profile = profileRepository.findByEmail(
                    false, request.getEmail().trim());

            //check if the user exist in the referral table
            Optional<ReferralCode> referralCode = referralCodeRepository
                    .findByUserId(request.getUserId());

            //validation check
            ApiResponse<String> validationCheck = validationCheckOnProfile(profile, referralCode);
            if (validationCheck.getStatus()) {
                Profile newProfile = modelMapper.map(request, Profile.class);
                // check if this referral code is already mapped to a user
                newProfile.setReferral(request.getReferralCode());
                newProfile.setCorporate(false);
                newProfile.setDateOfBirth(request.getDateOfBirth().toString());
                //save new personal profile
                Profile savedProfile = profileRepository.save(newProfile);
                log.info("saving new personal profile ::: {}", newProfile);
                //save referral code
                saveReferralCode(savedProfile, request.getUserId());

                String fullName = String.format("%s %s", savedProfile.getFirstName(),
                        savedProfile.getSurname());

                //send otp to Phone and Email
                CompletableFuture.runAsync(() -> otpTokenService.sendAccountVerificationToken(
                       savedProfile, JOINT_VERIFICATION, baseUrl));

                //create waya gram profile
                CompletableFuture.runAsync(() -> createWayagramProfile(savedProfile.getUserId(), savedProfile.getSurname(), fullName))
                .orTimeout(3, TimeUnit.MINUTES).handle((res, ex) -> {
                   if(ex != null){
                       log.error("Error With Setting up Wayagram Profile:: {}", ex.getMessage());
                   }
                   return res;
                });
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
                    exception.getMessage(),
                    false, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * create a new corporate profile.
     * @param baseUrl
     * @param profileRequest corporate profile request
     */
    @Transactional
    @Override
    public ApiResponse<String> createProfile(CorporateProfileRequest profileRequest, String baseUrl) {
        try {
            Users user = this.userRepository.findById(false, Long.valueOf(profileRequest.getUserId())).orElse(null);
            if(user == null) throw new CustomException("Base User with Provided ID not Found", HttpStatus.BAD_REQUEST);

            Optional<Profile> profileWithUserId = profileRepository.findByUserId(false, profileRequest.getUserId());
            if(profileWithUserId.isPresent()) throw new CustomException("Profile with Provided User ID already Exists", HttpStatus.BAD_REQUEST);

            if(profileRequest.getReferralCode() != null && !profileRequest.getReferralCode().isBlank()){
                ReferralCode referralCode1 = referralCodeRepository.getReferralCodeByUserId(profileRequest.getReferralCode());
                if(referralCode1 == null)
                    profileRequest.setReferralCode(null);
            }
            //check if the user exist in the profile table
            Optional<Profile> profile = profileRepository.findByEmail(
                    false, profileRequest.getEmail().trim());

            //check if the user exist in the referral table
            // now this check will extend to the referral service
            Optional<ReferralCode> referralCode = referralCodeRepository
                    .findByUserId(profileRequest.getUserId());
            //validation check
            ApiResponse<String> validationCheck = validationCheckOnProfile(profile, referralCode);

            if (validationCheck.getStatus()) {
                Profile newCorporateProfile = saveCorporateProfile(profileRequest);

                //save the referral code make request to the referral service
                saveReferralCode(newCorporateProfile, profileRequest.getUserId());

                //send otp to Phone and Email
                CompletableFuture.runAsync(() -> otpTokenService.sendAccountVerificationToken(
                        newCorporateProfile, JOINT_VERIFICATION, baseUrl));

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
                    exception.getMessage(),
                    false, HttpStatus.BAD_REQUEST);
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
        profile.setDateOfBirth(profileRequest.getDateOfBirth().toString());
        profile.setGender(profileRequest.getGender());
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
        UUID id = otherDetailsRequest.getOtherDetailsId() == null ? UUID.randomUUID() : otherDetailsRequest.getOtherDetailsId();
        otherDetails.setId(id);
        otherDetails.setBusinessType(otherDetailsRequest.getBusinessType());
        otherDetails.setOrganisationName(otherDetailsRequest.getOrganisationName());
        otherDetails.setOrganisationType(otherDetailsRequest.getOrganisationType());

        otherDetails = otherDetailsRepository.save(otherDetails);
        return otherDetails;
    }

    /**
     * check for the availability of the service
     * rollback if the service is unavailable
     */
    private void saveReferralCode(Profile newProfile, String userId) {
        // send details to the referral Service
        referralCodeRepository.save(
                new ReferralCode(generateReferralCode(REFERRAL_CODE_LENGTH),
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
     * @param updatePersonalProfileRequest profile update request
     * @param userId                       user id
     * @return Object
     */
    //@CachePut(cacheNames = "PersonalProfile", key = "#userId")
    public UserProfileResponse updateProfile(
            UpdatePersonalProfileRequest updatePersonalProfileRequest, String userId) {

        try {
            Optional<Users> user = userRepository.findById(false, Long.parseLong(userId));
            if (user.isPresent()) {
                Optional<Profile> profile = profileRepository.findByUserId(false, userId);

                if (profile.isPresent()) {
                    updateUserAccount(user.get(), updatePersonalProfileRequest);

                    Profile personalProfile = processPersonalProfileUpdateRequest(
                            updatePersonalProfileRequest, profile.get(), userId);

                    return setProfileResponse(personalProfile);
                } else {
                    throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
                }
            }
            throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void updateUserAccount(Users users, UpdatePersonalProfileRequest newProfile) {
        if(userRepository.existsByEmail(newProfile.getEmail().trim()) && !compareTwoString(users.getEmail(), newProfile.getEmail()))
            throw new CustomException("Email for Update already Belongs to another User", HttpStatus.BAD_REQUEST);
        users.setEmail(newProfile.getEmail());
        if(userRepository.existsByPhoneNumber(newProfile.getPhoneNumber().trim()) && !compareTwoString(users.getPhoneNumber(), newProfile.getPhoneNumber()))
            throw new CustomException("Phone Number for Update already Belongs to another User", HttpStatus.BAD_REQUEST);
        users.setPhoneNumber(newProfile.getPhoneNumber());
        users.setSurname(newProfile.getSurname());
        users.setFirstName(newProfile.getFirstName());
        String name = String.format("%s %s %s", newProfile.getFirstName(),
                newProfile.getMiddleName(), newProfile.getSurname()).replaceAll("\\s+", " ").trim();
        users.setName(name);
        userRepository.save(users);
    }

    private boolean compareTwoString(String str1, String str2){
        if(str1 == null && str2 == null) return true;
        if(str1 == null || str2 == null) return false;
        return str1.trim().equalsIgnoreCase(str2.trim());
    }

    private Profile processPersonalProfileUpdateRequest(
            UpdatePersonalProfileRequest updatePersonalProfileRequest,
            Profile profile, String userId) {
        try{
            Profile updatedProfile = modelMapper
                    .map(updatePersonalProfileRequest, Profile.class);

            updatedProfile.setId(profile.getId());
            updatedProfile.setUserId(userId);
            updatedProfile.setProfileImage(profile.getProfileImage());
            updatedProfile.setDateOfBirth(updatePersonalProfileRequest.getDateOfBirth().toString());

            log.info("updating  user profile ::: {}", updatedProfile);
            return profileRepository.save(updatedProfile);
        }catch(Exception e){
            throw new CustomException(e.getMessage(),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
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
            Optional<Users> user = userRepository.findById(false, Long.parseLong(userId));
            if (user.isPresent() && user.get().isCorporate()) {
                Optional<Profile> profile = profileRepository.findByUserId(false, userId);
                if (profile.isPresent() && profile.get().isCorporate()) {

                    // Update Base User
                    updateUserAccount(user.get(), corporateProfileRequest);

                    //process corporate request
                    Profile savedProfile = processCorporateProfileUpdateRequest(profile.get(),
                            corporateProfileRequest);

                    OtherDetailsRequest otherDetailsRequest = new OtherDetailsRequest();
                    UUID id = profile.get().getOtherDetails() == null ? null : profile.get().getOtherDetails().getId();
                    otherDetailsRequest.setOtherDetailsId(id);
                    otherDetailsRequest.setBusinessType(corporateProfileRequest.getBusinessType());
                    otherDetailsRequest.setOrganisationType(corporateProfileRequest.getOrganisationType());
                    otherDetailsRequest.setOrganisationName(corporateProfileRequest.getOrganisationName());

                    saveOtherDetails(otherDetailsRequest);

                    return setProfileResponse(savedProfile);
                } else {
                    throw new CustomException("user with that id not found or is not a Corporate User",
                            HttpStatus.NOT_FOUND);
                }
            }
            throw new CustomException("user with that id not found or is not a Corporate User",
                    HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), exception,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Profile processCorporateProfileUpdateRequest(
            Profile profile, UpdateCorporateProfileRequest corporateProfileRequest
    ) {
        try{
            profile.setSurname(corporateProfileRequest.getSurname());
            profile.setFirstName(corporateProfileRequest.getFirstName());
            profile.setEmail(corporateProfileRequest.getOrganisationEmail());
            profile.setPhoneNumber(corporateProfileRequest.getPhoneNumber());
            profile.setAddress(corporateProfileRequest.getOfficeAddress());
            profile.setOrganisationName(corporateProfileRequest.getOrganisationName());
            profile.setCity(corporateProfileRequest.getCity());
            profile.setState(corporateProfileRequest.getState());
            profile.setGender(corporateProfileRequest.getGender());
            profile.setDateOfBirth(corporateProfileRequest.getDateOfBirth().toString());

            log.info("updating  user profile with values ::: {}", profile);
            return profileRepository.save(profile);
        }catch(Exception ex){
            throw new CustomException(ex.getMessage(),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * This method updates a users profile image.
     *
     * @param userId       user id
     * @param profileImage request
     */
    //@Async
    @Override
    public ApiResponse<String> updateProfileImage(
            String userId, MultipartFile profileImage
    ) {
        try {
            Optional<Profile> profile = profileRepository
                    .findByUserId(false, userId);
            if(profile.isPresent()){
                Profile item = profile.get();

                ApiResponse<ProfileImageResponse> response
                        = uploadImage(fileResourceServiceFeignClient, profileImage, userId, log);
                if(response != null && response.getStatus()){
                    //update the profile image
                    item.setProfileImage(response.getData().getImageUrl());
                    //save back to the database
                    profileRepository.save(item);
                }
                return new ApiResponse<>("", "Uploaded Successfully", true);
            }

           throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            if (exception instanceof CustomException) {
                CustomException ex = (CustomException) exception;
                throw new CustomException(ex.getMessage(), ex.getStatus());
            }
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

        // get referralCodeValue for this user
        Optional<ReferralCode> referralCode;
        String referralCodeValue = null;
        if (profile.getUserId() !=null) {
            referralCode = referralCodeRepository.findByUserId(profile.getUserId());
            if(referralCode.isPresent()){
                referralCodeValue = referralCode.get().getReferralCode();
            }
        }

        // check user SMS alert Status
        Optional<SMSAlertConfig> smsAlertConfig;
        boolean isSMSAlertActive = false;
        if (profile.getUserId() !=null) {
            smsAlertConfig = smsAlertConfigRepository.findByPhoneNumber(profile.getPhoneNumber());
            if(smsAlertConfig.isPresent()){
                isSMSAlertActive = smsAlertConfig.get().isActive();
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
                .referenceCode(referralCodeValue)
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
     * @param name
     * @param userId
     * @param username
     */
    private void createWayagramProfile(String userId, String username, String name) {

        log.info("Creating waya gram Profile  with userid .....{}", userId);
        try {
			//HttpHeaders headers = new HttpHeaders();
			//headers.setContentType(MediaType.APPLICATION_JSON);
			//headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			//Map<String, Object> map = new HashMap<>();
			//map.put("user_id", userId);
			//map.put("username", username);
			//map.put("displayName", name);
			//map.put("notPublic", false);
			//HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        	CreateWayagram createWayagram = new CreateWayagram();
        	createWayagram.setUser_id(userId);
        	createWayagram.setUsername(username);
        	createWayagram.setDisplayName(name);
        	createWayagram.setNotPublic(false);
            ResponseEntity<String> response = this.wayagramProxy.createWayagramProfile(createWayagram);
            if (response.getStatusCode() == OK) {
                log.info("Wayagram profile Request Successful with body:: {}", response.getBody());
                log.info("creating auto follow.....");
                //make call to auto follow makeAutoFollow
                //  Executor delayed = CompletableFuture.delayedExecutor(5L, TimeUnit.SECONDS);
                CompletableFuture.runAsync(() -> makeAutoFollow(userId))
                    .orTimeout(3, TimeUnit.MINUTES)
                    .handle((res, ex) -> {
                        if(ex != null){
                            log.error("Error With Setting up Wayagram Auto-follow:: {}", ex.getMessage());
                        }
                        return res;
                    });
            } else {
                log.info("Wayagram profile Request Failed with body:: {}", response.getStatusCode());
            }
        } catch (Exception unhandledException) {
            log.error("Exception was thrown while creating Waya profile ... ", unhandledException);
        }
    }

    private void makeAutoFollow(String userId) {
        try {
			//log.info("creating auto follow ... {}", userId);
			//HttpHeaders headers = new HttpHeaders();
			//headers.setContentType(MediaType.APPLICATION_JSON);
			//headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			//Map<String, Object> map = new HashMap<>();
			//map.put("user_id", userId);
			//HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        	UserIDPojo userIdPojo = new UserIDPojo(userId);
        	ResponseEntity<String> response = this.wayagramProxy.autoFollowWayagram(userIdPojo);
            if (response.getStatusCode() == OK) {
                log.info("wayaOfficialHandle follow has been created:: {}", response.getBody());
            } else {
                log.info("wayaOfficialHandle  Request Failed with body:: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("wayaOfficialHandle  Exception: ", e);
        }
    }

    /**
     * @param deleteRequest deleteRequest
     * @return
     */
    public DeleteResponse toggleDelete(DeleteRequest deleteRequest) {
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
            log.error("Error while calling toggle delete:: {}", e);
            deleteResponse.setCode("400");
            deleteResponse.setError("Error while performing operation");
            return deleteResponse;
        }
        return deleteResponse;
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