package com.waya.wayaauthenticationservice.service.impl;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.waya.wayaauthenticationservice.config.ApplicationConfig;
import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.BulkCorporateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.ContactPojo;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.CorporateUserPojo;
import com.waya.wayaauthenticationservice.pojo.DevicePojo;
import com.waya.wayaauthenticationservice.pojo.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.pojo.UserRoleUpdateRequest;
import com.waya.wayaauthenticationservice.pojo.UserWalletPojo;
import com.waya.wayaauthenticationservice.pojo.WalletAccount;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.ApiResponse;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository usersRepo;

	@Autowired
	private AuthenticatedUserFacade authenticatedUserFacade;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	private RolesRepository rolesRepo;

	@Autowired
	private WalletProxy walletProxy;

	@Autowired
	private RestTemplate restClient;

	@Autowired
	private ApplicationConfig applicationConfig;

	@Autowired
	private ReqIPUtils reqUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationService authService;

	@Autowired
	KafkaMessageProducer kafkaMessageProducer;

	@Autowired
	ProfileServiceDAO profileServiceDAO;

	@Override
	public ResponseEntity<?> getUser(Long userId) {
		Users user = usersRepo.findById(userId).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Id"), HttpStatus.NOT_FOUND);
		} else {
			UserProfileResponsePojo userDto = this.toModelDTO(user);
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getUsers() {
//		Users user = authenticatedUserFacade.getUser();
//		if (!validateAdmin(user)) {
//			return new ResponseEntity<>(new ErrorResponse("Invalid Access"), HttpStatus.BAD_REQUEST);
//		}
		List<UserProfileResponsePojo> users = usersRepo.findAll().stream().map(u -> this.toModelDTO(u))
				.collect(Collectors.toList());
		return new ResponseEntity<>(new SuccessResponse("User info fetched", users), HttpStatus.OK);
	}

//	private boolean validateAdmin(Users user) {
//		if (user == null) {
//			return false;
//		}
//		Roles adminRole = rolesRepo.findByName("ROLE_ADMIN")
//				.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
//		Optional<Collection<Roles>> roles = Optional.ofNullable(user.getRolesList());
//		if (!roles.isPresent())
//			return false;
//
//		return roles.get().contains(adminRole);
//	}

	@Override
	public ResponseEntity<?> getUsersByRole(int roleId) {
//		Users user = authenticatedUserFacade.getUser();
//		if (!validateAdmin(user)) {
//			return new ResponseEntity<>(new ErrorResponse("Invalid Access"), HttpStatus.BAD_REQUEST);
//		}
		Roles role = rolesRepo.findById(roleId).orElse(null);
		if (role == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Role"), HttpStatus.BAD_REQUEST);
		}
		List<UserProfileResponsePojo> userList = new ArrayList<>();
		rolesRepo.findAll().forEach(roles -> {
			usersRepo.findAll().forEach(us -> {
				us.getRolesList().forEach(usRole -> {
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
		Users user = usersRepo.findByEmailIgnoreCase(email).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid email"), HttpStatus.NOT_FOUND);
		} else {
			UserProfileResponsePojo userDto = this.toModelDTO(user);
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getUserByPhone(String phone) {
		Users user = usersRepo.findByPhoneNumber(phone).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), HttpStatus.NOT_FOUND);
		}
		UserProfileResponsePojo userDtO = toModelDTO(user);
		return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getUserAndWalletByPhoneOrEmail(String value) {
		Users user = usersRepo.findByEmailOrPhoneNumber(value).orElse(null);
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
		Users user = usersRepo.findById(id).orElse(null);
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
			if (usersRepo.findByPhoneNumber(c.getPhone()).orElse(null) != null) {
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
	public ResponseEntity<?> getUserById(Long id) {
		try {
			Users user = usersRepo.findById(id).orElse(null);

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
	public ResponseEntity<?> deleteUser(Long id, String token) {
		try {
			if (validateUser(token)) {
				Users user = usersRepo.findById(id)
						.orElseThrow(() -> new CustomException("User with id  not found", HttpStatus.NOT_FOUND));
				user.setActive(false);
				user.setDateOfActivation(LocalDateTime.now());
				usersRepo.saveAndFlush(user);

				CompletableFuture.runAsync(() -> disableUserProfile(String.valueOf(id), token));
				return new ResponseEntity<>(new CustomException("Account deleted", OK), OK);
			} else {
				return new ResponseEntity<>(new ErrorResponse("Invalid Token"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> isUserAdmin(long userId) {
		Users user = usersRepo.findById(userId)
				.orElseThrow(() -> new CustomException("User with id  not found", HttpStatus.BAD_REQUEST));
		return new ResponseEntity<>(new SuccessResponse("IsUserAdmin", user.isAdmin()), HttpStatus.OK);
	}

	@Override
	public Integer getUsersCount(String roleName) {
		try {
			List<Users> users = new ArrayList<Users>();
			rolesRepo.findAll().forEach(role -> {
				usersRepo.findAll().forEach(user -> {
					user.getRolesList().forEach(uRole -> {
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
			return usersRepo.findById(user.getId()).map(mUser -> {
				for (Integer i : user.getRolesList()) {
					Optional<Roles> mRole = rolesRepo.findById(i);
					if (mRole.isPresent()) {
						if (mUser.getRolesList().contains(mRole.get()))
							continue;
						mUser.getRolesList().add(mRole.get());
					}
				}
				usersRepo.save(mUser);
				return user;
			}).orElseThrow(() -> new CustomException("User Id provided not found", HttpStatus.NOT_FOUND));
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public UserEditPojo getUserForRole(Long id) {
		try {
			return usersRepo.findById(id).map(user -> {
				UserEditPojo us = new UserEditPojo();
				us.setCorporate(user.isCorporate());
				us.setEmail(user.getEmail());
				us.setFirstName(user.getFirstName());
				us.setId(user.getId());
				us.setPhoneNumber(user.getPhoneNumber());
				us.setPhoneVerified(user.isPhoneVerified());
				us.setPinCreated(user.isPinCreated());
				us.setReferenceCode(user.getReferenceCode());
				us.setRolesList(new ArrayList<>(user.getRolesList()));
				us.setSurname(user.getSurname());
				us.setEmailVerified(user.isEmailVerified());
				return us;
			}).orElseThrow(() -> new CustomException("", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	private void disableUserProfile(String token, String userId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.set("authorization", token);
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userId);
			map.put("deleteType", "DELETE");
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
			ResponseEntity<String> response = restClient.postForEntity(applicationConfig.getDeleteProfileUrl(), entity,
					String.class);
			if (response.getStatusCode() == OK) {
				log.info("User deleted {}", response.getBody());
			} else {
				log.info("User not deleted :: {}", response.getStatusCode());
			}
		} catch (Exception e) {
			log.error("Error deleting user: ", e);
		}
	}

	private boolean validateUser(String token) {
		try {
			log.info("validating user token ... {}", token);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.set("authorization", token);

			Map<String, Object> map = new HashMap<>();
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
			ResponseEntity<String> response = restClient.postForEntity(applicationConfig.getValidateUser(), entity,
					String.class);
			if (response.getStatusCode() == OK) {
				log.info("User verified with body {}", response.getBody());
				return true;
			} else {
				log.info("user not verified :: {}", response.getStatusCode());
				return false;
			}
		} catch (Exception e) {
			log.error("Error verifying user: ", e);
			return false;
		}
	}

	@Override
	public UserProfileResponsePojo toModelDTO(Users user) {
		if (user == null)
			return null;

		Set<String> roles = user.getRolesList().stream().map(u -> u.getName()).collect(Collectors.toSet());
		Set<String> permits = new HashSet<>();
		user.getRolesList().forEach(u -> {
			permits.addAll(u.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet()));
		});

		UserProfileResponsePojo userDto = UserProfileResponsePojo.builder().email(user.getEmail()).id(user.getId())
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
			userPage = usersRepo.findAll(pageableRequest);
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
	public ResponseEntity<?> createUsers(@Valid BulkCorporateUserCreationDTO userList, HttpServletRequest request,
			Device device) {
		int count = 0;
		try {
			DevicePojo dev = reqUtil.GetDevice(device);
			final String ip = reqUtil.getClientIP(request);
			for (CorporateUserPojo mUser : userList.getUsersList()) {
				// Check if email exists
				Users existingEmail = mUser.getEmail() == null ? null
						: usersRepo.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
				if (existingEmail != null)
					continue;

				// Check if Phone exists
				Users existingTelephone = mUser.getPhoneNumber() == null ? null
						: usersRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
				if (existingTelephone != null)
					continue;

				Roles merchRole = rolesRepo.findByName("ROLE_MERCH")
						.orElseThrow(() -> new CustomException("Merchant Role Not Available", HttpStatus.BAD_REQUEST));

				Roles userRole = rolesRepo.findByName("ROLE_USER")
						.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));

				List<Roles> roleList = new ArrayList<>();
				roleList.addAll(Arrays.asList(userRole, merchRole));

				Users user = new Users();
				user.setAdmin(mUser.isAdmin());
				user.setId(0L);
				user.setCorporate(true);
				user.setDateCreated(LocalDateTime.now());
				user.setRegDeviceIP(ip);
				user.setRegDevicePlatform(dev.getPlatform());
				user.setRegDeviceType(dev.getDeviceType());
				user.setPassword(passwordEncoder.encode(mUser.getPassword()));
				user.setDateOfActivation(LocalDateTime.now());
				user.setActive(true);
				user.setRolesList(roleList);
				user.setEmail(mUser.getEmail().trim());
				user.setEmailVerified(false);
				user.setFirstName(mUser.getFirstName());
				user.setPhoneNumber(mUser.getPhoneNumber());
				user.setPhoneVerified(false);
				user.setPinCreated(false);
				user.setReferenceCode(mUser.getReferenceCode());
				user.setSurname(mUser.getSurname());

				Users regUser = usersRepo.save(user);
				if (regUser == null)
					continue;

				String token = this.authService.generateToken(regUser);

				this.authService.createCorporateUser(mUser, regUser.getId(), token);

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
	public ResponseEntity<?> createUsers(@Valid BulkPrivateUserCreationDTO userList, HttpServletRequest request,
			Device device) {
		int count = 0;
		try {
			DevicePojo dev = reqUtil.GetDevice(device);
			final String ip = reqUtil.getClientIP(request);
			for (BaseUserPojo mUser : userList.getUsersList()) {
				// Check if email exists
				Users existingEmail = mUser.getEmail() == null ? null
						: usersRepo.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
				if (existingEmail != null)
					continue;

				// Check if Phone exists
				Users existingTelephone = mUser.getPhoneNumber() == null ? null
						: usersRepo.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
				if (existingTelephone != null)
					continue;

				List<Roles> roleList = new ArrayList<>();
			
				Roles userRole = rolesRepo.findByName("ROLE_USER")
						.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
				roleList.add(userRole);
				if (mUser.isAdmin()) {
					Roles adminRole = rolesRepo.findByName("ROLE_ADMIN")
							.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
					roleList.add(adminRole);
				}

				Users user = new Users();
				user.setId(0L);
				user.setAdmin(mUser.isAdmin());
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
				user.setRolesList(roleList);

				Users regUser = usersRepo.save(user);
				if (regUser == null)
					continue;

				this.authService.createPrivateUser(regUser);

				++count;
			}
			String message = String.format("%s Private Accounts Created Successfully and Sub-account creation in process.", count);
			return new ResponseEntity<>(new SuccessResponse(message), HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error in Creating Bulk Account:: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

}
