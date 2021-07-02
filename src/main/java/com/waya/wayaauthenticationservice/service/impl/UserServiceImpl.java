package com.waya.wayaauthenticationservice.service.impl;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.waya.wayaauthenticationservice.config.ApplicationConfig;
import com.waya.wayaauthenticationservice.entity.Roles;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.ContactPojo;
import com.waya.wayaauthenticationservice.pojo.ContactPojoReq;
import com.waya.wayaauthenticationservice.pojo.MainWalletResponse;
import com.waya.wayaauthenticationservice.pojo.UserEditPojo;
import com.waya.wayaauthenticationservice.pojo.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.pojo.UserRoleUpdateRequest;
import com.waya.wayaauthenticationservice.pojo.UserWalletPojo;
import com.waya.wayaauthenticationservice.proxy.WalletProxy;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.util.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository usersRepo;

	// @Autowired
	// private BCryptPasswordEncoder passwordEncoder;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(Collection<Roles> roles) {
		List<GrantedAuthority> authorities = roles.stream().map(p -> new SimpleGrantedAuthority(p.getName()))
				.collect(toList());
		return authorities;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<Users> user = usersRepo.findByEmailOrPhoneNumber(email, email);

		if (!user.isPresent()) {
			throw new UsernameNotFoundException(email);
		} else {
			return new org.springframework.security.core.userdetails.User(user.get().getEmail(),
					user.get().getPassword(), true, true, true, true, new ArrayList<>());
		}
	}

	@Override
	public ResponseEntity<?> getUser(Long userId) {
		Users user = usersRepo.findById(userId).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Id"), HttpStatus.BAD_REQUEST);
		} else {
			UserProfileResponsePojo userDto = this.toModelDTO(user);
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getUsers() {
		Users user = authenticatedUserFacade.getUser();
		if (!validateAdmin(user)) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Access"), HttpStatus.BAD_REQUEST);
		}
		List<UserProfileResponsePojo> users = usersRepo.findAll().stream()
				.map(u -> this.toModelDTO(u))
				.collect(Collectors.toList());
		return new ResponseEntity<>(new SuccessResponse("User info fetched", users), HttpStatus.OK);
	}

	private boolean validateAdmin(Users user) {
		if (user == null) {
			return false;
		}
		Roles adminRole = rolesRepo.findByName("ROLE_ADMIN")
				.orElseThrow(() -> new CustomException("User Role Not Available", HttpStatus.BAD_REQUEST));
		Optional<Collection<Roles>> roles = Optional.ofNullable(user.getRolesList());

		if (!roles.isPresent())
			return false;

		return roles.get().contains(adminRole);
	}

	@Override
	public ResponseEntity<?> getUsersByRole(int roleId) {
		Users user = authenticatedUserFacade.getUser();
		if (!validateAdmin(user)) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Access"), HttpStatus.BAD_REQUEST);
		}
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
		Users user = usersRepo.findByEmail(email).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid email"), OK);
		} else {
			UserProfileResponsePojo userDto = this.toModelDTO(user);
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getUserByPhone(String phone, String token) {
		Users user = usersRepo.findByPhoneNumber(phone).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), HttpStatus.OK);
		}
		ApiResponse<MainWalletResponse> mainWalletResponse = walletProxy.getDefaultWallet(token);
//        WalletResponse gr = restTemplate.getForObject(WALLET_SERVICE+"wallet/default-account/"+ user.getId(), WalletResponse.class);
		if (mainWalletResponse != null) {
			UserWalletPojo userWalletPojo = new UserWalletPojo(user, mainWalletResponse.getData().getAccountNo(),
					mainWalletResponse.getData().getId());
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
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	// Edit user, mostly to update role list from the role service
	@Override
	public UserRoleUpdateRequest UpdateUser(UserRoleUpdateRequest user) {
		try {
			return usersRepo.findById(user.getId()).map(mUser -> {
				List<Roles> roleList = new ArrayList<>();
				for (Integer i : user.getRolesList()) {
					Optional<Roles> mUrole = rolesRepo.findById(i);
					if (mUrole.isPresent()) {
						roleList.add(mUrole.get());
						mUser.getRolesList().add(mUrole.get());
					}
				}
//				mUser.setRolesList(user.getRolesList());
				usersRepo.save(mUser);
				return user;
			}).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.NOT_FOUND));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
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
				us.setRolesList(new ArrayList<Roles>(user.getRolesList()));
				us.setSurname(user.getSurname());
				us.setEmailVerified(user.isEmailVerified());
				return us;
			}).orElseThrow(() -> new CustomException("", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
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
			throw new CustomException(errorMessages, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return userPage;
	}

	@Override
	public ResponseEntity<?> createUsers(@Valid BulkPrivateUserCreationDTO userBulk, HttpServletRequest request,
			Device device) {
		// TODO Auto-generated method stub
		return null;
	}

}
