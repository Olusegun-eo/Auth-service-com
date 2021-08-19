package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.*;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.mail.context.WelcomeEmailContext;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserIDPojo;
import com.waya.wayaauthenticationservice.proxy.FileResourceServiceFeignClient;
import com.waya.wayaauthenticationservice.proxy.WayagramProxy;
import com.waya.wayaauthenticationservice.repository.*;
import com.waya.wayaauthenticationservice.response.*;
import com.waya.wayaauthenticationservice.service.MessagingService;
import com.waya.wayaauthenticationservice.service.OTPTokenService;
import com.waya.wayaauthenticationservice.service.ProfileService;
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

    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final OTPTokenService otpTokenService;
    private final FileResourceServiceFeignClient fileResourceServiceFeignClient;
    private final OtherDetailsRepository otherDetailsRepository;
    private final WayagramProxy wayagramProxy;
    private final SMSAlertConfigRepository smsAlertConfigRepository;
    private final MessagingService messagingService;
    private final ReferralCodeRepository referralCodeRepository;
    private final RestTemplate restClient;
    @Value("${app.config.wayagram-profile.base-url}")
    private String wayagramUrl;

    @Autowired
    public ProfileServiceImpl(ModelMapper modelMapper, ProfileRepository profileRepository,
                              UserRepository userRepository, OTPTokenService otpTokenService,
                              FileResourceServiceFeignClient fileResourceServiceFeignClient,
                              @Qualifier("restClient") RestTemplate restClient,
                              OtherDetailsRepository otherDetailsRepository, WayagramProxy wayagramProxy,
                              SMSAlertConfigRepository smsAlertConfigRepository, MessagingService messagingService,
                              ReferralCodeRepository referralCodeRepository) {
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.otpTokenService = otpTokenService;
        this.fileResourceServiceFeignClient = fileResourceServiceFeignClient;
        this.otherDetailsRepository = otherDetailsRepository;
        this.wayagramProxy = wayagramProxy;
        this.smsAlertConfigRepository = smsAlertConfigRepository;
        this.messagingService = messagingService;
        this.referralCodeRepository = referralCodeRepository;
        this.userRepository = userRepository;
        this.restClient = restClient;
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
            // ReferralCodePojo referralCodePojo =
            // referralProxy.getReferralCodeByUserId(userId);

            ReferralCode referrals = referralCodeRepository.getReferralCodeByUserId(userId);
            if (referrals == null) {
                return Collections.emptyList();
            }
            return profileRepository
                    .findAllByReferralCode(referrals.getReferralCode(), LIMIT, parsePageNumber * LIMIT, false).stream()
                    .map(this::setProfileResponse).collect(Collectors.toList());

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

            if (request.getReferralCode() != null && !request.getReferralCode().isBlank()) {
                ReferralCode referralCode1 = referralCodeRepository.getReferralCodeByUserId(request.getReferralCode());
                if (referralCode1 == null)
                    request.setReferralCode(null);
            }
            // check if the user exist in the profile table
            Optional<Profile> profile = request.getEmail() == null ? Optional.empty() :
                    profileRepository.findByEmail(false, request.getEmail());

            // check if the user exist in the referral table
            Optional<ReferralCode> referralCode = referralCodeRepository.findByUserId(request.getUserId());

            // validation check
            ApiResponseBody<String> validationCheck = validationCheckOnProfile(profile, referralCode);
            if (validationCheck.getStatus()) {
                Profile newProfile = modelMapper.map(request, Profile.class);
                // check if this referral code is already mapped to a user
                newProfile.setReferral(request.getReferralCode());
                newProfile.setCorporate(false);
                newProfile.setDateOfBirth(request.getDateOfBirth().toString());
                // save new personal profile
                Profile savedProfile = profileRepository.save(newProfile);
                log.info("saving new personal profile ::: {}", newProfile);
                // save referral code
                saveReferralCode(savedProfile, request.getUserId());

                String fullName = String.format("%s %s", savedProfile.getFirstName(), savedProfile.getSurname());

                // send otp to Phone and Email
                CompletableFuture.runAsync(
                        () -> otpTokenService.sendAccountVerificationToken(user, JOINT_VERIFICATION, baseUrl));

                // create waya gram profile
                CompletableFuture.runAsync(
                        () -> createWayagramProfile(savedProfile.getUserId(), savedProfile.getSurname(), fullName))
                        .orTimeout(3, TimeUnit.MINUTES).handle((res, ex) -> {
                    if (ex != null) {
                        log.error("Error With Setting up Wayagram Profile:: {}", ex.getMessage());
                    }
                    return res;
                });
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

    /**
     * create a new corporate profile.
     *
     * @param baseUrl
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

            if (profileRequest.getReferralCode() != null && !profileRequest.getReferralCode().isBlank()) {
                ReferralCode referralCode1 = referralCodeRepository
                        .getReferralCodeByUserId(profileRequest.getReferralCode());
                if (referralCode1 == null)
                    profileRequest.setReferralCode(null);
            }
            // check if the user exist in the profile table
            Optional<Profile> profile = profileRequest.getEmail() == null ? Optional.empty() :
                    profileRepository.findByEmail(false, profileRequest.getEmail());

            // check if the user exist in the referral table
            // now this check will extend to the referral service
            Optional<ReferralCode> referralCode = referralCodeRepository.findByUserId(profileRequest.getUserId());
            // validation check
            ApiResponseBody<String> validationCheck = validationCheckOnProfile(profile, referralCode);

            if (validationCheck.getStatus()) {
                Profile newCorporateProfile = saveCorporateProfile(profileRequest);

                // save the referral code make request to the referral service
                saveReferralCode(newCorporateProfile, profileRequest.getUserId());

                // send otp to Phone and Email
                CompletableFuture.runAsync(() -> otpTokenService.sendAccountVerificationToken(user,
                        JOINT_VERIFICATION, baseUrl));

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
        UUID id = otherDetailsRequest.getOtherDetailsId() == null ? UUID.randomUUID()
                : otherDetailsRequest.getOtherDetailsId();
        otherDetails.setId(id);
        otherDetails.setBusinessType(otherDetailsRequest.getBusinessType());
        otherDetails.setOrganisationName(otherDetailsRequest.getOrganisationName());
        otherDetails.setOrganisationType(otherDetailsRequest.getOrganisationType());

        otherDetails = otherDetailsRepository.save(otherDetails);
        return otherDetails;
    }

    /**
     * check for the availability of the service rollback if the service is
     * unavailable
     */
    private void saveReferralCode(Profile newProfile, String userId) {
        // send details to the referral Service
        referralCodeRepository.save(new ReferralCode(generateReferralCode(REFERRAL_CODE_LENGTH), newProfile, userId));

        log.info("saving referral code for this new profile");
    }

    /**
     * get user personal profile and also put in cache for subsequent request
     * notificat
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
     * @param updatePersonalProfileRequest profile update request
     * @param userId                       user id
     * @return Object
     */
    // @CachePut(cacheNames = "PersonalProfile", key = "#userId")
    public UserProfileResponse updateProfile(UpdatePersonalProfileRequest updatePersonalProfileRequest, String userId) {

        try {
            Optional<Users> user = userRepository.findById(false, Long.parseLong(userId));
            if (user.isPresent()) {
                Optional<Profile> profile = profileRepository.findByUserId(false, userId);

                if (profile.isPresent()) {
                    updateUserAccount(user.get(), updatePersonalProfileRequest);

                    Profile personalProfile = processPersonalProfileUpdateRequest(updatePersonalProfileRequest,
                            profile.get(), userId);

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

    private void updateUserAccount(Users user, UpdatePersonalProfileRequest newProfile) {
        if (userRepository.existsByEmail(newProfile.getEmail())
                && !isEqual(user.getEmail(), newProfile.getEmail()))
            throw new CustomException("Email for Update already Belongs to another User", HttpStatus.BAD_REQUEST);

        if (!isEqual(user.getEmail(), newProfile.getEmail())) {
            user.setPhoneVerified(false);
        }
        user.setEmail(newProfile.getEmail());
        if (userRepository.existsByPhoneNumber(newProfile.getPhoneNumber().trim())
                && !isEqual(user.getPhoneNumber(), newProfile.getPhoneNumber()))
            throw new CustomException("Phone Number for Update already Belongs to another User",
                    HttpStatus.BAD_REQUEST);

        if (!isEqual(user.getPhoneNumber(), newProfile.getPhoneNumber())) {
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

    private boolean isEqual(String str1, String str2) {
        if (str1 == null && str2 == null)
            return true;
        if (str1 == null || str2 == null)
            return false;
        return str1.trim().equalsIgnoreCase(str2.trim());
    }

    private Profile processPersonalProfileUpdateRequest(UpdatePersonalProfileRequest updatePersonalProfileRequest,
                                                        Profile profile, String userId) {
        try {
            Profile updatedProfile = modelMapper.map(updatePersonalProfileRequest, Profile.class);

            updatedProfile.setId(profile.getId());
            updatedProfile.setUserId(userId);
            updatedProfile.setProfileImage(profile.getProfileImage());
            updatedProfile.setDateOfBirth(updatePersonalProfileRequest.getDateOfBirth().toString());

            log.info("updating  user profile ::: {}", updatedProfile);
            return profileRepository.save(updatedProfile);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
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
    public UserProfileResponse updateProfile(UpdateCorporateProfileRequest corporateProfileRequest, String userId) {
        try {
            Optional<Users> user = userRepository.findById(false, Long.parseLong(userId));
            if (user.isPresent() && user.get().isCorporate()) {
                Optional<Profile> profile = profileRepository.findByUserId(false, userId);
                if (profile.isPresent() && profile.get().isCorporate()) {

                    // Update Base User
                    updateUserAccount(user.get(), corporateProfileRequest);

                    // process corporate request
                    Profile savedProfile = processCorporateProfileUpdateRequest(profile.get(), corporateProfileRequest);

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
            throw new CustomException("user with that id not found or is not a Corporate User", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            throw new CustomException(exception.getMessage(), exception, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Profile processCorporateProfileUpdateRequest(Profile profile,
                                                         UpdateCorporateProfileRequest corporateProfileRequest) {
        try {
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
                    if (response != null && response.getStatus()) {
                        // update the profile image
                        item.setProfileImage(response.getData().getImageUrl());
                        // save back to the database
                        profileRepository.save(item);
                    }
                    return new ApiResponseBody<>("", "Uploaded Successfully", true);
                }
                throw new CustomException(PROFILE_NOT_EXIST, HttpStatus.NOT_FOUND);
            } else {
                return new ApiResponseBody<String>("Invalid Image Passed", "Error", false);
            }
        } catch (MagicParseException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            return new ApiResponseBody<String>("Invalid Image Passed", "Error", false);
        } catch (MagicMatchNotFoundException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            return new ApiResponseBody<String>("Invalid Image Passed", "Error", false);
        } catch (MagicException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            return new ApiResponseBody<String>("Invalid Image Passed", "Error", false);
        } catch (IOException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            return new ApiResponseBody<String>("Error uploading Image Passed", "Error", false);
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
     * @return
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
                        if (response != null && response.getStatus()) {
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
        } catch (MagicParseException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            throw new CustomException("Invalid Image Passed", HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (MagicMatchNotFoundException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            throw new CustomException("Invalid Image Passed", HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (MagicException e) {
            log.error("caught an exception ::: {}", e.getMessage());
            throw new CustomException("Invalid Image Passed", HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (IOException e) {
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

    private UserProfileResponse setProfileResponse(Profile profile) {
        // initialize to empty
        Optional<OtherDetails> otherDetails = Optional.empty();
        // check if other details is present in profile
        if (profile.getOtherDetails() != null) {
            otherDetails = otherDetailsRepository.findById(profile.getOtherDetails().getId());
        }

        // get referralCodeValue for this user
        Optional<ReferralCode> referralCode;
        String referralCodeValue = null;
        if (profile.getUserId() != null) {
            referralCode = referralCodeRepository.findByUserId(profile.getUserId());
            if (referralCode.isPresent()) {
                referralCodeValue = referralCode.get().getReferralCode();
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
        OtherdetailsResponse otherdetailsResponse = null;
        // map data if present
        if (otherDetails.isPresent()) {
            otherdetailsResponse = modelMapper.map(otherDetails.get(), OtherdetailsResponse.class);
        }
        return UserProfileResponse.builder().address(profile.getAddress()).district(profile.getDistrict())
                .profileImage(profile.getProfileImage()).email(profile.getEmail()).gender(profile.getGender())
                .id(profile.getId().toString()).dateOfBirth(profile.getDateOfBirth()).firstName(profile.getFirstName())
                .surname(profile.getSurname()).middleName(profile.getMiddleName()).phoneNumber(profile.getPhoneNumber())
                .referenceCode(referralCodeValue).smsAlertConfig(isSMSAlertActive).userId(profile.getUserId())
                .city(profile.getCity()).corporate(profile.isCorporate()).otherDetails(otherdetailsResponse).build();
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

    /**
     * Create a wayagram profile
     *
     * @param name
     * @param userId
     * @param username
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
     * @return
     */
    public DeleteResponse toggleDelete(DeleteRequest deleteRequest) {
        DeleteResponse deleteResponse = new DeleteResponse();
        try {
            if (deleteRequest.getDeleteType().equals(DeleteType.DELETE)) {
                Optional<Profile> optionalProfile = profileRepository.findByUserId(false, String.valueOf(deleteRequest.getUserId()));
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
                Optional<Profile> optionalProfile = profileRepository.findByUserId(true, String.valueOf(deleteRequest.getUserId()));
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

    public SMSResponse toggleSMSAlert(SMSRequest smsRequest) {
        Users user = userRepository.findByPhoneNumber(smsRequest.getPhoneNumber())
                .orElse(null);
        if (user == null) {
            throw new CustomException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User: " +
                    smsRequest.getPhoneNumber(), HttpStatus.NOT_FOUND);
        }

        Optional<SMSAlertConfig> smsCharges = smsAlertConfigRepository
                .findByPhoneNumber(smsRequest.getPhoneNumber());

        SMSResponse SMSResponse;
        if (smsCharges.isPresent()) {
            smsCharges.get().setActive(!smsCharges.get().isActive());
            smsAlertConfigRepository.save(smsCharges.get());
            SMSResponse = new SMSResponse(smsCharges.get().getId(),
                    smsCharges.get().getPhoneNumber(),
                    smsCharges.get().isActive());
        } else {
            SMSAlertConfig smsCharges1 = new SMSAlertConfig();
            smsCharges1.setActive(smsCharges1.isActive());
            smsCharges1.setPhoneNumber(smsRequest.getPhoneNumber());
            smsCharges1.setUserId(user.getId());
            smsCharges1 = smsAlertConfigRepository.save(smsCharges1);
            SMSResponse = new SMSResponse(smsCharges1.getId(), smsCharges1.getPhoneNumber(),
                    smsCharges1.isActive());
        }
        return SMSResponse;
    }

    public SMSResponse getSMSAlertStatus(String phoneNumber) {
        SMSResponse SMSResponse = null;
        if (Objects.isNull(phoneNumber) || phoneNumber.isEmpty()) {
            throw new CustomException(PHONE_NUMBER_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Optional<SMSAlertConfig> smsCharges = smsAlertConfigRepository.findByPhoneNumber(phoneNumber);

        if (smsCharges.isPresent()) {
            SMSResponse = new SMSResponse(smsCharges.get().getId(), smsCharges.get().getPhoneNumber(),
                    smsCharges.get().isActive());
        }
        return SMSResponse;
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

            profile = profileRepository.findByUserId(false, referralCode1.getUserId());

            if (!profile.isPresent()) {
                throw new CustomException("Null", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return setProfileResponse(profile.get());

    }

}