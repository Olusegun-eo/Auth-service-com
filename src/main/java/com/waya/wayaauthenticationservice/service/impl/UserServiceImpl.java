package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.notification.DataPojo;
import com.waya.wayaauthenticationservice.pojo.notification.NamesPojo;
import com.waya.wayaauthenticationservice.pojo.notification.NotificationResponsePojo;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.*;
import com.waya.wayaauthenticationservice.proxy.NotificationProxy;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.proxy.WayagramProxy;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.HelperUtils;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.OK;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    KafkaMessageProducer kafkaMessageProducer;
    @Autowired
    ProfileServiceDAO profileServiceDAO;
    @Autowired
    ProfileService profileService;
    @Autowired
    private UserRepository usersRepository;
    @Autowired
    private AuthenticatedUserFacade authenticatedUserFacade;
    @Autowired
    private RolesRepository rolesRepo;
    @Autowired
    private WalletProxy walletProxy;
    @Autowired
    VirtualAccountProxy virtualAccountProxy;
    @Autowired
    private ReqIPUtils reqUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authService;
    @Autowired
    private NotificationProxy notificationProxy;
    @Autowired
    private WayagramProxy wayagramProxy;

    @Value("${api.server.deployed}")
    private String urlRedirect;

    private String getBaseUrl(HttpServletRequest request) {
        return "http://" + urlRedirect + ":" + request.getServerPort() + request.getContextPath();
    }

    @Override
    public ResponseEntity<?> getUserById(Long id) {
        try {
            Users user = usersRepository.findById(false, id).orElse(null);
            UserProfileResponsePojo userDto = this.toModelDTO(user);
            if (userDto == null) {
                return new ResponseEntity<>(new ErrorResponse("Invalid id, No User Found"), HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> getUsers() {
        Users user = authenticatedUserFacade.getUser();
        if (!validateAdmin(user)) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Access"), HttpStatus.BAD_REQUEST);
        }
        List<UserProfileResponsePojo> users = usersRepository.findAll().stream().map(u -> this.toModelDTO(u))
                .collect(Collectors.toList());
        return new ResponseEntity<>(new SuccessResponse("User info fetched", users), HttpStatus.OK);
    }

    private boolean validateAdmin(Users user) {
        if (user == null) {
            return false;
        }
        Role adminRole = rolesRepo.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
        Optional<Collection<Role>> roles = Optional.ofNullable(user.getRoleList());
        if (!roles.isPresent())
            return false;

        return roles.get().contains(adminRole);
    }

    @Override
    public ResponseEntity<?> getUsersByRole(long roleId) {
//		Users user = authenticatedUserFacade.getUser();
//		if (!validateAdmin(user)) {
//			return new ResponseEntity<>(new ErrorResponse("Invalid Access"), HttpStatus.BAD_REQUEST);
//		}
        Role role = rolesRepo.findById(roleId).orElse(null);
        if (role == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Role"), HttpStatus.BAD_REQUEST);
        }
        List<UserProfileResponsePojo> userList = new ArrayList<>();
        rolesRepo.findAll().forEach(roles -> {
            usersRepository.findAll().forEach(us -> {
                us.getRoleList().forEach(usRole -> {
                    if (usRole.getId().equals(roleId)) {
                        UserProfileResponsePojo u = this.toModelDTO(us);
                        userList.add(u);
                    }
                });
            });
        });
        return new ResponseEntity<>(new SuccessResponse("User by roles fetched", userList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserByEmail(String email) {
        Users user = usersRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid email"), HttpStatus.NOT_FOUND);
        } else {
            UserProfileResponsePojo userDto = this.toModelDTO(user);
            return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<?> getUserByPhone(String phone) {
        Users user = usersRepository.findByPhoneNumber(phone).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), HttpStatus.NOT_FOUND);
        }
        UserProfileResponsePojo userDtO = toModelDTO(user);
        return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserInfoByPhoneOrEmailForServiceConsumption(String value) {
        Users user = usersRepository.findByEmailOrPhoneNumber(value).orElse(null);
        if (user == null)
            return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), HttpStatus.NOT_FOUND);
        UserProfileResponsePojo userDtO = toModelDTO(user);
        if (userDtO != null) {
            return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ErrorResponse("Account information is not Existing"), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> getUserInfoByUserIdForServiceConsumption(Long id) {
        Users user = usersRepository.findById(false, id).orElse(null);
        if (user == null)
            return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), HttpStatus.NOT_FOUND);
        UserProfileResponsePojo userDtO = toModelDTO(user);
        if (userDtO != null) {
            return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ErrorResponse("Account information is not Existing"), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> wayaContactCheck(ContactPojoReq contacts) {
        List<ContactPojo> contactPojos = new ArrayList<>();
        for (ContactPojo c : contacts.getContacts()) {
            if (usersRepository.findByPhoneNumber(c.getPhone()).orElse(null) != null) {
                c.setWayaUser(true);
            }
            contactPojos.add(c);
        }
        return new ResponseEntity<>(new SuccessResponse("Contact processed", contactPojos), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getMyInfo() {
        Users user = authenticatedUserFacade.getUser();
        UserProfileResponsePojo userDto = this.toModelDTO(user);
        return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteUser(Long id) {
        try {
            //if (validateUser(token)) {
            Users user = usersRepository.findById(false, id)
                    .orElseThrow(() -> new CustomException("User with id " + id + " not found", HttpStatus.NOT_FOUND));
            user.setActive(false);
            user.setDeleted(true);
            user.setDateOfActivation(LocalDateTime.now());

            // Deactivates other Services tied to the UserId
            String token = this.authService.generateToken(authenticatedUserFacade.getUser());

            // Delete Virtual Account Call
            CompletableFuture.runAsync(() -> virtualAccountProxy.deleteAccountByUserId(id, token));

            //TODO: Internal Wallet Delete Account Call
            // Waiting on Emmanuel Njoku to provide API for this Service

            // Disables User Profile and Wayagram Services
            CompletableFuture.runAsync(() -> disableUserProfile(String.valueOf(user.getId()), token))
                    .thenRun(() -> usersRepository.saveAndFlush(user));

            return new ResponseEntity<>(new SuccessResponse("Account deleted", OK), OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> unDeleteUser(Long id) {
        try {
            Users user = usersRepository.findById(true, id)
                    .orElseThrow(() -> new CustomException("User with deleted id " + id + " not found", HttpStatus.NOT_FOUND));

            if (usersRepository.existsByEmail(user.getEmail()) || usersRepository.existsByPhoneNumber(user.getPhoneNumber()))
                throw new CustomException("Account Un-deletion Failed: Another User exists with same details", HttpStatus.EXPECTATION_FAILED);

            user.setDeleted(false);
            user.setDateOfActivation(LocalDateTime.now());

            // Reactivates other Services tied to the UserId
            String token = this.authService.generateToken(authenticatedUserFacade.getUser());
            CompletableFuture.runAsync(() -> enableUserProfile(String.valueOf(user.getId()), token))
                    .thenRun(() -> usersRepository.saveAndFlush(user));

            return new ResponseEntity<>(new SuccessResponse("Account Undeleted", OK), OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> deactivateAccounts(BulkPrivateUserCreationDTO bulkUpload) {
        if(bulkUpload != null && !bulkUpload.getUsersList().isEmpty()){
            try{
                bulkUpload.getUsersList().forEach(user -> {
                    Users dbUser = usersRepository.findByEmailIgnoreCase(user.getEmail()).orElse(null);
                    if(dbUser != null && dbUser.isActive()){
                        dbUser.setActive(false);
                        usersRepository.saveAndFlush(dbUser);
                    }
                });
                return new ResponseEntity<>(new SuccessResponse("Accounts Deactivated", OK), OK);
            }catch(Exception e){
                log.error("An Exception Occurred :: {}", e.getMessage());
                return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(new ErrorResponse("Excel contains no Data"), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> isUserAdmin(Long userId) {
        Users user = usersRepository.findById(false, userId)
                .orElseThrow(() -> new CustomException("User with id  not found", HttpStatus.BAD_REQUEST));
        return new ResponseEntity<>(new SuccessResponse("IsUserAdmin", user.isAdmin()), HttpStatus.OK);
    }

    @Override
    public Integer getUsersCount(String roleName) {
        try {
            List<Users> users = new ArrayList<Users>();
            rolesRepo.findAll().forEach(role -> {
                usersRepository.findAll().forEach(user -> {
                    user.getRoleList().forEach(uRole -> {
                        if (uRole.getName().equals(roleName)) {
                            users.add(user);
                        }
                    });
                });
            });
            return users.size();
        } catch (Exception e) {
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    // Edit user, mostly to update role list from the role service
    @Override
    public SuccessResponse UpdateUser(UserRoleUpdateRequest user) {
        try {
            return usersRepository.findById(false, user.getUserId()).map(mUser -> {
                for (Integer i : user.getRolesList()) {
                    Optional<Role> mRole = rolesRepo.findById(Long.parseLong(String.valueOf(i)));
                    if (mRole.isPresent()) {
                        if (mUser.getRoleList().contains(mRole.get()))
                            continue;
                        mUser.getRoleList().add(mRole.get());
                    }
                }
                usersRepository.save(mUser);
                return new SuccessResponse("User Role Updated", user);
            }).orElseThrow(() -> new CustomException("User Id provided not found", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public UserEditPojo UpdateUserDetails(UserEditPojo userEditPojo) {
        Users user = usersRepository.findById(false, userEditPojo.getId()).orElseThrow(() -> new CustomException("", HttpStatus.UNPROCESSABLE_ENTITY));
        user.setCorporate(userEditPojo.isCorporate());
        user.setEmail(userEditPojo.getEmail());
        user.setFirstName(userEditPojo.getFirstName());
        user.setPhoneNumber(userEditPojo.getPhoneNumber());
        user.setPhoneVerified(userEditPojo.isPhoneVerified());
        user.setPinCreated(userEditPojo.isPinCreated());
        user.setReferenceCode(userEditPojo.getReferenceCode());
        user.setSurname(userEditPojo.getSurname());
        user = usersRepository.save(user);
        return new UserEditPojo(user.getId(), user.getEmail(), user.getPhoneNumber(), user.getReferenceCode(), user.getFirstName(), user.getSurname(), user.isPhoneVerified(), user.isEmailVerified(), user.isPinCreated(), user.isCorporate(), (List<Role>) user.getRoleList());
    }

    @Override
    public UserEditPojo getUserForRole(Long id) {
        try {
            return usersRepository.findById(false, id).map(user -> {
                UserEditPojo us = new UserEditPojo();
                us.setCorporate(user.isCorporate());
                us.setEmail(user.getEmail());
                us.setFirstName(user.getFirstName());
                us.setId(user.getId());
                us.setPhoneNumber(user.getPhoneNumber());
                us.setPhoneVerified(user.isPhoneVerified());
                us.setPinCreated(user.isPinCreated());
                us.setReferenceCode(user.getReferenceCode());
                us.setRoleList(new ArrayList<>(user.getRoleList()));
                us.setSurname(user.getSurname());
                us.setEmailVerified(user.isEmailVerified());
                return us;
            }).orElseThrow(() -> new CustomException("User with Supplied id not Found", HttpStatus.UNPROCESSABLE_ENTITY));
        } catch (Exception e) {
            log.error("Error::: {}", e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void disableUserProfile(String userId, String token) {
        try {
            // Profile Service Delete Call
            DeleteRequest deleteRequest = DeleteRequest.builder()
                    .userId(userId)
                    .deleteType(DeleteType.DELETE)
                    .build();
            var returnValue = this.profileService.toggleDelete(deleteRequest);
            log.info("Profile Deleted: {} {}", returnValue.getCode(), returnValue.getMessage());

            // Wayagram delete Account call
            UserIDPojo idPojo = new UserIDPojo(userId);

            var resp = this.wayagramProxy.toggleActivation(idPojo, token);
            log.info("Wayagram Account Activation: {} - {}", resp.getBody(), resp.getStatusCode());
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
        }
    }

    private void enableUserProfile(String userId, String token) {
        try {
            // Profile Service Un-Delete Call
            DeleteRequest deleteRequest = DeleteRequest.builder()
                    .userId(userId)
                    .deleteType(DeleteType.RESTORE)
                    .build();
            var returnValue = this.profileService.toggleDelete(deleteRequest);
            log.info("Profile Deleted: {} {}", returnValue.getCode(), returnValue.getMessage());

            //TODO: Wallet Un-Delete Account Call

            // Wayagram Activate Account call
            UserIDPojo idPojo = new UserIDPojo(userId);
            var resp = this.wayagramProxy.toggleActivation(idPojo, token);
            log.info("Wayagram Account Activation: {} - {}", resp.getBody(), resp.getStatusCode());
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
        }
    }


    @Override
    public UserProfileResponsePojo toModelDTO(Users user) {
        if (user == null)
            return null;

        Set<String> roles = user.getRoleList().stream().map(u -> u.getName()).collect(Collectors.toSet());
        Set<String> permits = new HashSet<>();
        user.getRoleList().forEach(u -> {
            permits.addAll(u.getPrivileges().stream().map(p -> p.getName()).collect(Collectors.toSet()));
        });

        UserProfileResponsePojo userDto = UserProfileResponsePojo.builder().email(user.getEmail())
                .id(user.getId()).referenceCode(user.getReferenceCode())
                .isEmailVerified(user.isEmailVerified()).phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName()).lastName(user.getSurname()).isAdmin(user.isAdmin())
                .isPhoneVerified(user.isPhoneVerified()).isAccountDeleted(user.isDeleted())
                .isAccountExpired(!user.isAccountNonExpired()).isCredentialsExpired(!user.isCredentialsNonExpired())
                .isActive(user.isActive()).isAccountLocked(!user.isAccountNonLocked()).roles(roles).permits(permits)
                .pinCreated(user.isPinCreated()).isCorporate(user.isCorporate()).build();

        userDto.add(linkTo(methodOn(UserController.class).findUser(user.getId())).withSelfRel());

        return userDto;
    }

    @Override
    public Page<Users> getAllUsers(int page, int size) {
        Pageable pageableRequest = PageRequest.of(page, size);
        Page<Users> userPage;
        try {
            userPage = usersRepository.findAll(pageableRequest);
            if (userPage == null) {
                userPage = Page.empty(pageableRequest);
            }
        } catch (Exception ex) {
            log.error(ex.getCause() + "message");
            String errorMessages = String.format("%s %s", ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage(),
                    ex.getMessage());
            throw new CustomException(errorMessages, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return userPage;
    }

    @Override
    public ResponseEntity<?> createUsers(BulkCorporateUserCreationDTO userList, HttpServletRequest request,
                                         Device device) {
        int count = 0;
        try {
            DevicePojo dev = reqUtil.GetDevice(device);
            final String ip = reqUtil.getClientIP(request);
            for (CorporateUserPojo mUser : userList.getUsersList()) {
                // Check if email exists
                Users existingEmail = mUser.getEmail() == null ? null
                        : usersRepository.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
                if (existingEmail != null)
                    continue;

                // Check if Phone exists
                Users existingTelephone = mUser.getPhoneNumber() == null ? null
                        : usersRepository.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
                if (existingTelephone != null)
                    continue;

                Role userRole = rolesRepo.findByName("ROLE_USER")
                        .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

                Role merchRole = rolesRepo.findByName("ROLE_CORP")
                        .orElseThrow(() -> new CustomException("User Corp Role Not Available", HttpStatus.BAD_REQUEST));

                List<Role> roleList = new ArrayList<>(Arrays.asList(userRole, merchRole));
                if (mUser.isAdmin()) {
                    Role corpAdminRole = rolesRepo.findByName("ROLE_CORP_ADMIN")
                            .orElseThrow(() -> new CustomException("User Corp Admin Role Not Available", HttpStatus.BAD_REQUEST));
                    roleList.add(corpAdminRole);
                }

                // Generate and Save Random Password
                String randomPassword = HelperUtils.generateRandomPassword();
                mUser.setPassword(randomPassword);
                log.info("Password for {} is {}", mUser.getEmail(), randomPassword);

                Users user = new Users();
                user.setId(0L);
//                String publicUserId = HelperUtils.generateRandomPassword();
//                while (usersRepo.existsByUserId(publicUserId)) {
//                    publicUserId = HelperUtils.generateRandomPassword();
//                }
//                user.setUserId(publicUserId);
                user.setCorporate(true);
                user.setDateCreated(LocalDateTime.now());
                user.setRegDeviceIP(ip);
                user.setRegDevicePlatform(dev.getPlatform());
                user.setRegDeviceType(dev.getDeviceType());
                user.setPassword(passwordEncoder.encode(mUser.getPassword()));
                user.setDateOfActivation(LocalDateTime.now());
                user.setActive(true);
                user.setAccountStatus(-1);
                user.setRoleList(roleList);
                user.setEmail(mUser.getEmail().trim());
                user.setEmailVerified(false);
                user.setFirstName(mUser.getFirstName());
                user.setPhoneNumber(mUser.getPhoneNumber());
                user.setPhoneVerified(false);
                user.setPinCreated(false);
                user.setReferenceCode(mUser.getReferenceCode());
                user.setSurname(mUser.getSurname());
                String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
                user.setName(fullName);
                Users regUser = usersRepository.save(user);
                if (regUser == null)
                    continue;

                CompletableFuture.runAsync(() -> sendEmailNewPassword(mUser.getPassword(), mUser.getEmail(), mUser.getFirstName()));

                String token = this.authService.generateToken(regUser);
                this.authService.createCorporateUser(mUser, regUser.getId(), token, getBaseUrl(request));
                ++count;
            }
            String message = String.format("%s  Corporate Account Created Successfully and Sub-account creation in process.", count);
            return new ResponseEntity<>(new SuccessResponse(message), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error in Creating Bulk Account:: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> createUsers(BulkPrivateUserCreationDTO userList, HttpServletRequest request,
                                         Device device) {
        int count = 0;
        try {
            if (userList == null || userList.getUsersList().isEmpty())
                return new ResponseEntity<>(new ErrorResponse("User List cannot be null or Empty"), HttpStatus.BAD_REQUEST);

            DevicePojo dev = reqUtil.GetDevice(device);
            final String ip = reqUtil.getClientIP(request);
            for (BaseUserPojo mUser : userList.getUsersList()) {
                // Check if email exists
                Users existingEmail = mUser.getEmail() == null ? null
                        : usersRepository.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
                if (existingEmail != null)
                    continue;

                // Check if Phone exists
                Users existingTelephone = mUser.getPhoneNumber() == null ? null
                        : usersRepository.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
                if (existingTelephone != null)
                    continue;

                List<Role> roleList = new ArrayList<>();

                Role userRole = rolesRepo.findByName("ROLE_USER")
                        .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
                roleList.add(userRole);

                if (mUser.isAdmin()) {
                    Role adminRole = rolesRepo.findByName("ROLE_APP_ADMIN")
                            .orElseThrow(() -> new CustomException("User App Admin Role Not Available", HttpStatus.BAD_REQUEST));
                    roleList.add(adminRole);
                }

                // Generate and Save Random Password
                String randomPassword = HelperUtils.generateRandomPassword();
                mUser.setPassword(randomPassword);
                log.info("Password for {} is {}", mUser.getEmail(), randomPassword);

                Users user = new Users();
                user.setId(0L);
//                String publicUserId = HelperUtils.generateRandomPassword();
//                while (usersRepo.existsByUserId(publicUserId)) {
//                    publicUserId = HelperUtils.generateRandomPassword();
//                }
//                user.setUserId(publicUserId);
                user.setAdmin(mUser.isAdmin());
                user.setAccountStatus(-1);
                user.setEmail(mUser.getEmail().trim());
                user.setFirstName(mUser.getFirstName());
                user.setPhoneNumber(mUser.getPhoneNumber());
                user.setReferenceCode(mUser.getReferenceCode());
                user.setSurname(mUser.getSurname());
                user.setDateCreated(LocalDateTime.now());
                user.setRegDeviceIP(ip);
                String fullName = String.format("%s %s", user.getFirstName(), user.getSurname());
                user.setName(fullName);
                user.setRegDevicePlatform(dev.getPlatform());
                user.setRegDeviceType(dev.getDeviceType());
                user.setDateOfActivation(LocalDateTime.now());
                user.setActive(true);
                user.setPassword(passwordEncoder.encode(mUser.getPassword()));
                user.setRoleList(roleList);

                Users regUser = usersRepository.save(user);
                if (regUser == null)
                    continue;

                String token = this.authService.generateToken(authenticatedUserFacade.getUser());
                this.authService.createPrivateUser(mUser, regUser.getId(), token, getBaseUrl(request));
                CompletableFuture.runAsync(() -> sendEmailNewPassword(mUser.getPassword(), mUser.getEmail(), mUser.getFirstName()));
                ++count;
            }
            String message = String.format("%s Private Accounts Created Successfully and Sub-account creation in process.", count);
            return new ResponseEntity<>(new SuccessResponse(message), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error in Creating Bulk Account:: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private void sendEmailNewPassword(String randomPassword, String email, String firstName) {
        //Email Sending of new Password Here
        NotificationResponsePojo notification = new NotificationResponsePojo();
        NamesPojo name = new NamesPojo();
        name.setEmail(email);
        name.setFullName(firstName);
        List<NamesPojo> names = new ArrayList<>();
        names.add(name);
        DataPojo dataPojo = new DataPojo();
        String message = String.format("<h3>Hello %s </h3><br> <p> Kindly Use the password below to login to the System, " +
                        "ensure you change it.</p> <br> <h4 style=\"font-weight:bold\"> %s </h4>",
                firstName, randomPassword);
        dataPojo.setMessage(message);
        dataPojo.setNames(names);
        notification.setData(dataPojo);
        notification.setEventType("EMAIL");
        notification.setInitiator(email);
        CompletableFuture.runAsync(() -> notificationProxy.sendEmail(notification));
    }

}
