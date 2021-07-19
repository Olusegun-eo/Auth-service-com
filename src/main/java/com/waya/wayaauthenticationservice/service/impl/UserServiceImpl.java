package com.waya.wayaauthenticationservice.service.impl;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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
import com.waya.wayaauthenticationservice.pojo.others.ContactPojo;
import com.waya.wayaauthenticationservice.pojo.others.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.others.DeleteRequest;
import com.waya.wayaauthenticationservice.pojo.others.DevicePojo;
import com.waya.wayaauthenticationservice.pojo.others.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.others.UserRoleUpdateRequest;
import com.waya.wayaauthenticationservice.pojo.others.UserWalletPojo;
import com.waya.wayaauthenticationservice.pojo.others.WalletAccount;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkCorporateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.proxy.NotificationProxy;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.HelperUtils;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;

import lombok.extern.slf4j.Slf4j;

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
    
	/*
	 * @Autowired private RestTemplate restClient;
	 * @Autowired private ApplicationConfig applicationConfig;
	 */
    
    @Autowired
    private ReqIPUtils reqUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authService;
    @Autowired
    private NotificationProxy notificationProxy;

    @Value("${api.server.deployed}")
    private String urlRedirect;

    private String getBaseUrl(HttpServletRequest request) {
        return "http://" + urlRedirect + ":" + request.getServerPort() + request.getContextPath();
    }

    @Override
    public ResponseEntity<?> getUserById(Long id) {
        try {
            Users user = usersRepository.findById(id).orElse(null);

            UserProfileResponsePojo userDto = this.toModelDTO(user);
            if (userDto == null) {
                return new ResponseEntity<>(new ErrorResponse("Invalid id"), HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<?> getUserAndWalletByPhoneOrEmail(String value) {
        Users user = usersRepository.findByEmailOrPhoneNumber(value).orElse(null);
        if (user == null)
            return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), HttpStatus.NOT_FOUND);
        UserProfileResponsePojo userDtO = toModelDTO(user);
        ApiResponse<List<WalletAccount>> walletResponse = walletProxy.getUsersWallet(user.getId());
        if (walletResponse != null) {
            UserWalletPojo userWalletPojo = new UserWalletPojo(userDtO, walletResponse.getData(),
                    walletResponse.getMessage());
            return new ResponseEntity<>(new SuccessResponse("User info fetched", userWalletPojo), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> getUserAndWalletByUserId(Long id) {
        Users user = usersRepository.findById(id).orElse(null);
        if (user == null)
            return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), HttpStatus.NOT_FOUND);
        UserProfileResponsePojo userDtO = toModelDTO(user);
        ApiResponse<List<WalletAccount>> walletResponse = walletProxy.getUsersWallet(user.getId());
        if (walletResponse != null) {
            UserWalletPojo userWalletPojo = new UserWalletPojo(userDtO, walletResponse.getData(),
                    walletResponse.getMessage());
            return new ResponseEntity<>(new SuccessResponse("User info fetched", userWalletPojo), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ErrorResponse(), HttpStatus.BAD_REQUEST);
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
            Users user = usersRepository.findById(id)
                    .orElseThrow(() -> new CustomException("User with id " + id + " not found", HttpStatus.NOT_FOUND));
            user.setActive(false);
            user.setDeleted(true);
            user.setDateOfActivation(LocalDateTime.now());
            Users finalUser = usersRepository.saveAndFlush(user);

            CompletableFuture.runAsync(() -> disableUserProfile(String.valueOf(finalUser.getId())));

            return new ResponseEntity<>(new SuccessResponse("Account deleted", OK), OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> isUserAdmin(Long userId) {
        Users user = usersRepository.findById(userId)
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
    public UserRoleUpdateRequest UpdateUser(UserRoleUpdateRequest user) {
        try {
            return usersRepository.findById(user.getUserId()).map(mUser -> {
                for (Integer i : user.getRolesList()) {
                    Optional<Role> mRole = rolesRepo.findById(Long.parseLong(String.valueOf(i)));
                    if (mRole.isPresent()) {
                        if (mUser.getRoleList().contains(mRole.get()))
                            continue;
                        mUser.getRoleList().add(mRole.get());
                    }
                }
                usersRepository.save(mUser);
                return user;
            }).orElseThrow(() -> new CustomException("User Id provided not found", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public UserEditPojo UpdateUserDetails(UserEditPojo userEditPojo) {
       Users user = usersRepository.findById(userEditPojo.getId()).orElseThrow(() -> new CustomException("", HttpStatus.UNPROCESSABLE_ENTITY));
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
            return usersRepository.findById(id).map(user -> {
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
            }).orElseThrow(() -> new CustomException("", HttpStatus.UNPROCESSABLE_ENTITY));
        } catch (Exception e) {
            log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
            throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void disableUserProfile(String userId) {
        try {
            // Profile Service Delete Call
            DeleteRequest deleteRequest = DeleteRequest.builder()
                    .userId(userId)
                    .deleteType(DeleteType.DELETE)
                    .build();
            var returnValue = this.profileService.toggleDelete(deleteRequest);
            log.info("Profile Deleted: {} {}", returnValue.getBody().getCode(), returnValue.getBody().getMessage());

            // Wallet Delete Account Call

            // Wayagram delete Account call

        } catch (Exception e) {
            log.error("Error deleting user: ", e);
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

                Role merchRole = rolesRepo.findByName("ROLE_MERCH")
                        .orElseThrow(() -> new CustomException("Merchant Role Not Available", HttpStatus.BAD_REQUEST));

                Role userRole = rolesRepo.findByName("ROLE_USER")
                        .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

                List<Role> roleList = new ArrayList<>();
                roleList.addAll(Arrays.asList(userRole, merchRole));

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
                user.setAccountStatus(-1);
                user.setDateCreated(LocalDateTime.now());
                user.setRegDeviceIP(ip);
                user.setRegDevicePlatform(dev.getPlatform());
                user.setRegDeviceType(dev.getDeviceType());
                user.setPassword(passwordEncoder.encode(mUser.getPassword()));
                user.setDateOfActivation(LocalDateTime.now());
                user.setActive(true);
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

                String token = this.authService.generateToken(regUser);
                this.authService.createCorporateUser(mUser, regUser.getId(), token, getBaseUrl(request));
                sendEmailNewPassword(randomPassword, regUser.getEmail(), regUser.getFirstName());
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
                    Role adminRole = rolesRepo.findByName("ROLE_ADMIN")
                            .orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
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
                this.authService.createPrivateUser(regUser, getBaseUrl(request));
                sendEmailNewPassword(randomPassword, regUser.getEmail(), regUser.getFirstName());
                ++count;
            }
            String message = String.format("%s Private Accounts Created Successfully and Sub-account creation in process.", count);
            return new ResponseEntity<>(new SuccessResponse(message), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error in Creating Bulk Account:: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private void sendEmailNewPassword(String randomPassword, String email, String firstName){
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
