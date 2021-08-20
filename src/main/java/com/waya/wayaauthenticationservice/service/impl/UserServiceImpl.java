package com.waya.wayaauthenticationservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.UserSetup;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.DeleteType;
import com.waya.wayaauthenticationservice.enums.ERole;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.access.UserAccessResponse;
import com.waya.wayaauthenticationservice.pojo.log.LogRequest;
import com.waya.wayaauthenticationservice.pojo.mail.context.PasswordCreateContext;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.*;
import com.waya.wayaauthenticationservice.proxy.*;
import com.waya.wayaauthenticationservice.repository.RolesRepository;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.repository.UserSetupRepository;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.security.AuthenticatedUserFacade;
import com.waya.wayaauthenticationservice.security.UserPrincipal;
import com.waya.wayaauthenticationservice.service.AuthenticationService;
import com.waya.wayaauthenticationservice.service.MessagingService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.UserService;
import com.waya.wayaauthenticationservice.service.impl.search.SearchCriteria;
import com.waya.wayaauthenticationservice.service.impl.search.SearchOperation;
import com.waya.wayaauthenticationservice.service.impl.search.SearchService;
import com.waya.wayaauthenticationservice.service.impl.search.SearchSpecification;
import com.waya.wayaauthenticationservice.util.Constant;
import com.waya.wayaauthenticationservice.util.HelperUtils;
import com.waya.wayaauthenticationservice.util.ReqIPUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.waya.wayaauthenticationservice.util.HelperUtils.emailPattern;
import static com.waya.wayaauthenticationservice.util.HelperUtils.phoneNumPattern;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.*;

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
	VirtualAccountProxy virtualAccountProxy;
	@Autowired
	private UserRepository usersRepository;
	@Autowired
	private AuthenticatedUserFacade authenticatedUserFacade;
	@Autowired
	private RolesRepository rolesRepo;
	@Autowired
	private WalletProxy walletProxy;
	@Autowired
	private ReqIPUtils reqUtil;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private AuthenticationService authService;
	@Autowired
	private WayagramProxy wayagramProxy;
	@Autowired
	private AccessProxy accessProxy;
	@Autowired
	LoggingProxy loggingProxy;
	@Autowired
	MessagingService messagingService;
	@Autowired
	ObjectMapper mapper;
	@Autowired
	UserSetupRepository userSetupRepository;

	@Autowired
	SearchService searchService;

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
				return new ResponseEntity<>(new ErrorResponse("Invalid id, No User Found"),
						HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> getUsers() {
		Users user = authenticatedUserFacade.getUser();
		if (!validateAdmin(user)) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Access"), BAD_REQUEST);
		}
		List<UserProfileResponsePojo> users = usersRepository.findAll().stream().map(u -> this.toModelDTO(u))
				.collect(Collectors.toList());
		return new ResponseEntity<>(new SuccessResponse("User info fetched", users), HttpStatus.OK);
	}

	private boolean validateAdmin(Users user) {
		if (user == null) {
			return false;
		}
		Role adminRole = rolesRepo.findByName("ROLE_APP_ADMIN")
				.orElseThrow(() -> new CustomException("User Role Not Available", BAD_REQUEST));
		Optional<Collection<Role>> roles = Optional.ofNullable(user.getRoleList());
		if (!roles.isPresent())
			return false;

		return roles.get().contains(adminRole);
	}

	@Override
	public ResponseEntity<?> getUsersByRole(long roleId) {
		Role role = rolesRepo.findById(roleId).orElse(null);
		if (role == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Role"), BAD_REQUEST);
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
			return new ResponseEntity<>(new ErrorResponse("Invalid email"), NOT_FOUND);
		} else {
			UserProfileResponsePojo userDto = this.toModelDTO(user);
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getUserByPhone(String phone) {
		String principal = phone.replaceAll("\\s+", "").trim();
		if (principal.startsWith("+")) {
			principal = principal.substring(1);
		}
		if (principal.length() > 10) {
			principal = principal.substring(principal.length() - 10);
		}
		Users user = usersRepository.findByPhoneNumber(principal).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), NOT_FOUND);
		}
		UserProfileResponsePojo userDtO = toModelDTO(user);
		return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getUserInfoByPhoneOrEmailForServiceConsumption(String value) {
		String principal = value.replaceAll("\\s+", "").trim();
		boolean isEmail = emailPattern.matcher(principal).matches();
		if (!isEmail) {
			if (principal.startsWith("+")) {
				principal = principal.substring(1);
			}
			if (principal.length() > 10) {
				principal = principal.substring(principal.length() - 10);
			}
		}
		Users user = usersRepository.findByEmailOrPhoneNumber(principal).orElse(null);
		if (user == null)
			return new ResponseEntity<>(new ErrorResponse("Invalid Phone number"), NOT_FOUND);
		UserProfileResponsePojo userDtO = toModelDTO(user);
		if (userDtO != null) {
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
		}
		return new ResponseEntity<>(new ErrorResponse("Account information is not Existing"), BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> getUserInfoByUserIdForServiceConsumption(Long id) {
		Users user = usersRepository.findById(false, id).orElse(null);
		if (user == null)
			return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), NOT_FOUND);
		UserProfileResponsePojo userDtO = toModelDTO(user);
		if (userDtO != null) {
			return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
		}
		return new ResponseEntity<>(new ErrorResponse("Account information is not Existing"), BAD_REQUEST);
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
	public ResponseEntity<?> validateWalletUserCall(Long userId, String key) {
		if (!key.equalsIgnoreCase("WALMIFOS"))
			return new ResponseEntity<>(new ErrorResponse("Invalid KEY Passed"), HttpStatus.BAD_REQUEST);

		Users user = usersRepository.findById(userId).orElse(null);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User Id Passed"), NOT_FOUND);
		}
		UserProfileResponsePojo userDtO = toModelDTO(user);
		return new ResponseEntity<>(new SuccessResponse("User info fetched", userDtO), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getMyInfo() {
		Users user = authenticatedUserFacade.getUser();
		UserProfileResponsePojo userDto = this.toModelDTO(user);
		return new ResponseEntity<>(new SuccessResponse("User info fetched", userDto), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> deleteUser(Long userId) {
		try {
			Users user = usersRepository.findById(false, userId).orElseThrow(
					() -> new CustomException("User with id " + userId + " not found", NOT_FOUND));

			// Generate token to use for deactivation of other Services tied to the UserId
			String token = this.authService.generateToken(authenticatedUserFacade.getUser());

			deactivationServices(user, token);

			return new ResponseEntity<>(new SuccessResponse("Account deleted", OK), OK);
		} catch (Exception e) {
			if (e instanceof CustomException) {
				CustomException ex = (CustomException) e;
				return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), ex.getStatus());
			}
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	public void deactivationServices(Users user, String token) throws InterruptedException, ExecutionException {
		List<WalletAccount> wallets = fetchUsersWallet(user.getId(), token).get();
		BigDecimal clrBalAmt = new BigDecimal("0.00");
		for (WalletAccount account : wallets) {
			if (!account.isAcctClsFlg()) {
				clrBalAmt = clrBalAmt.add(account.getClrBalAmt());
			}
		}
		int moreOrNegativeBalance = clrBalAmt.compareTo(new BigDecimal("0.00"));
		if (moreOrNegativeBalance != 0)
			throw new CustomException("User needs to nil off Balance across all Wallets", BAD_REQUEST);

		for (WalletAccount account : wallets) {
			ApiResponseBody<WalletAccount> res = this.deleteUserWallet(account.getAccountNo(), token);
			if (!res.getStatus()) {
				String error = String.format("Error in Closing Wallet account %s : %s", account.getAccountNo(),
						res.getMessage());
				throw new CustomException(error, BAD_REQUEST);
			}
		}
		// De-activate and Delete Existing Accounts
		user.setActive(false);
		user.setDeleted(true);
		user.setDateOfActivation(LocalDateTime.now());

		// Disables User Profile and Wayagram Services
		// Delete Virtual Account Call
		// Delete All User's Wallets
		CompletableFuture.runAsync(() -> disableUserProfile(user.getId(), token))
				.thenRun(() -> this.deleteUsersVirtualAccount(user.getId(), token))
				.thenRun(() -> usersRepository.saveAndFlush(user));
	}

	private CompletableFuture<List<WalletAccount>> fetchUsersWallet(Long userId, String token) {
		return CompletableFuture.supplyAsync(() -> walletProxy.fetchUsersWallets(userId, token))
				.orTimeout(2, TimeUnit.MINUTES).handle((res, ex) -> {
					if (ex != null) {
						log.error("Error Fetching Accounts, {}", ex.getMessage());
						return Collections.emptyList();
					}
					return res.getData();
				});
	}

	private ApiResponseBody<WalletAccount> modifyUserWallet(WalletAccessPojo pojo, String token) {
		try {
			return walletProxy.modifyUserWallet(pojo, token);
		} catch (CustomException ex) {
			log.error("Call to Modify Account Access fails:: {}", ex.getMessage());
			try {
				@SuppressWarnings("unchecked")
				ApiResponseBody<WalletAccount> result = mapper.readValue(ex.getMessage(), ApiResponseBody.class);
				return result;
			} catch (JsonProcessingException e) {
				log.error("Error Parsing Body to Json");
			}
			String error = Constant.ERROR_MESSAGE + ": " + ex.getMessage();
			return new ApiResponseBody<>(error, false);
		}
	}

	private ApiResponseBody<WalletAccount> deleteUserWallet(String accountNumber, String token) {
		WalletAccessPojo pojo = new WalletAccessPojo();
		pojo.setAcctClosed(true);
		pojo.setFreezCode("");
		pojo.setFreezReason("");
		pojo.setLienAmount(new BigDecimal("0.00"));
		pojo.setLienReason("");
		pojo.setCustomerAccountNumber(accountNumber);

		log.debug("Request Object sent for Account Deletion:: {}", new Gson().toJson(pojo));
		return this.modifyUserWallet(pojo, token);
	}

	private void deleteUsersVirtualAccount(Long id, String token) {
		CompletableFuture.supplyAsync(() -> virtualAccountProxy.deleteAccountByUserId(id, token))
				.orTimeout(3, TimeUnit.MINUTES).handle((res, ex) -> {
					if (ex != null) {
						log.error("Error Fetching Accounts, {}", ex.getMessage());
						return new ApiResponseBody<>("An error has occurred", false);
					}
					return res;
				}).thenAccept(p -> {
					log.debug("Response from API Call to Delete Virtual Account is: {}, status is: {} data is {}",
							p.getMessage(), p.getStatus(), p.getData());
				});
	}

	@Override
	public ResponseEntity<?> unDeleteUser(Long id) {
		try {
			Users user = usersRepository.findById(true, id).orElseThrow(
					() -> new CustomException("User with deleted id " + id + " not found", NOT_FOUND));

			if (usersRepository.existsByEmail(user.getEmail())
					|| usersRepository.existsByPhoneNumber(user.getPhoneNumber()))
				throw new CustomException("Account Un-deletion Failed: Another User exists with same details",
						HttpStatus.EXPECTATION_FAILED);

			user.setDeleted(false);
			user.setDateOfActivation(LocalDateTime.now());

			// Reactivates other Services tied to the UserId
			String token = this.authService.generateToken(authenticatedUserFacade.getUser());
			CompletableFuture.runAsync(() -> enableUserProfile(user.getId(), token))
					.thenRun(() -> usersRepository.saveAndFlush(user));

			return new ResponseEntity<>(new SuccessResponse("Account Undeleted", OK), OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> deactivateAccounts(BulkPrivateUserCreationDTO bulkUpload) {
		if (bulkUpload != null && !bulkUpload.getUsersList().isEmpty()) {
			try {
				bulkUpload.getUsersList().forEach(user -> {
					Users dbUser = usersRepository.findByEmailIgnoreCase(user.getEmail()).orElse(null);
					if (dbUser != null && dbUser.isActive()) {
						dbUser.setActive(false);
						usersRepository.saveAndFlush(dbUser);
					}
				});
				return new ResponseEntity<>(new SuccessResponse("Accounts Deactivated", OK), OK);
			} catch (Exception e) {
				log.error("An Exception Occurred :: {}", e.getMessage());
				return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(new ErrorResponse("Excel contains no Data"), BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> activateAccounts(BulkPrivateUserCreationDTO bulkUpload) {
		if (bulkUpload != null && !bulkUpload.getUsersList().isEmpty()) {
			try {
				bulkUpload.getUsersList().forEach(user -> {
					Users dbUser = usersRepository.findByEmailIgnoreCase(user.getEmail()).orElse(null);
					if (dbUser != null && !dbUser.isActive()) {
						dbUser.setActive(true);
						usersRepository.saveAndFlush(dbUser);
					}
				});
				return new ResponseEntity<>(new SuccessResponse("Accounts Activated", OK), OK);
			} catch (Exception e) {
				log.error("An Exception Occurred :: {}", e.getMessage());
				return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(new ErrorResponse("Excel contains no Data"), BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> toggleActivation(Long id) {
		try {
			Users dbUser = usersRepository.findById(false, id).orElse(null);
			if (dbUser != null) {
				dbUser.setActive(!dbUser.isActive());
				dbUser.setDateOfActivation(LocalDateTime.now());
				usersRepository.saveAndFlush(dbUser);
				return new ResponseEntity<>(new SuccessResponse("Account activation status Changed Successfully"), OK);
			}
			return new ResponseEntity<>(new ErrorResponse("User Does not exists"), NOT_FOUND);
		} catch (Exception e) {
			log.error("An Exception Occurred :: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> toggleLock(Long id) {
		try {
			Users dbUser = usersRepository.findById(false, id).orElse(null);
			if (dbUser != null) {
				dbUser.setAccountNonLocked(!dbUser.isAccountNonLocked());
				dbUser.setAccountLockDate(LocalDateTime.now());
				usersRepository.saveAndFlush(dbUser);
				return new ResponseEntity<>(new SuccessResponse("Account activation status Changed Successfully"), OK);
			}
			return new ResponseEntity<>(new ErrorResponse("User Does not exists"), NOT_FOUND);
		} catch (Exception e) {
			log.error("An Exception Occurred :: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	public ResponseEntity<?> isUserAdmin(Long userId) {
		Users user = usersRepository.findById(false, userId)
				.orElseThrow(() -> new CustomException("User with id  not found", NOT_FOUND));

		boolean isUserAdmin = roleCheck(user.getRoleList(), ERole.ROLE_APP_ADMIN.getRole());
		return new ResponseEntity<>(new SuccessResponse("IsUserAdmin", isUserAdmin), HttpStatus.OK);
	}

	private boolean roleCheck(Collection<Role> roleList, String role) {
		return roleList.stream().anyMatch(e -> e.getName().equals(role));
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
			}).orElseThrow(() -> new CustomException("User Id provided not found", NOT_FOUND));
		} catch (Exception e) {
			log.info("Error::: {}, {} and {}", e.getMessage(), 2, 3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Override
	public UserEditPojo UpdateUserDetails(UserEditPojo userEditPojo) {
		Users user = usersRepository.findById(false, userEditPojo.getId())
				.orElseThrow(() -> new CustomException("", HttpStatus.UNPROCESSABLE_ENTITY));
		user.setCorporate(userEditPojo.isCorporate());
		user.setEmail(userEditPojo.getEmail());
		user.setFirstName(userEditPojo.getFirstName());
		user.setPhoneNumber(userEditPojo.getPhoneNumber());
		user.setPhoneVerified(userEditPojo.isPhoneVerified());
		user.setPinCreated(userEditPojo.isPinCreated());
		user.setReferenceCode(userEditPojo.getReferenceCode());
		user.setSurname(userEditPojo.getSurname());
		user = usersRepository.save(user);
		return new UserEditPojo(user.getId(), user.getEmail(), user.getPhoneNumber(), user.getReferenceCode(),
				user.getFirstName(), user.getSurname(), user.isPhoneVerified(), user.isEmailVerified(),
				user.isPinCreated(), user.isCorporate(), (List<Role>) user.getRoleList());
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
			}).orElseThrow(
					() -> new CustomException("User with Supplied id not Found", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			log.error("Error::: {}", e.getMessage());
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	private void disableUserProfile(Long userId, String token) {
		try {
			// Profile Service Delete Call
			DeleteRequest deleteRequest = DeleteRequest.builder().userId(userId).deleteType(DeleteType.DELETE).build();
			var returnValue = this.profileService.toggleDelete(deleteRequest);
			log.info("Profile Deleted: {} {}", returnValue.getCode(), returnValue.getMessage());

			// Wayagram delete Account call
			UserIDPojo idPojo = new UserIDPojo(String.valueOf(userId));

			var resp = this.wayagramProxy.deleteWayagramAccount(idPojo, token);
			log.info("Wayagram Account Activation: {} - {}", resp.getBody(), resp.getStatusCode());
		} catch (Exception e) {
			log.error("Error deleting user: {}", e.getMessage());
		}
	}

	private void enableUserProfile(Long userId, String token) {
		try {
			// Profile Service Un-Delete Call
			DeleteRequest deleteRequest = DeleteRequest.builder().userId(userId).deleteType(DeleteType.RESTORE).build();
			var returnValue = this.profileService.toggleDelete(deleteRequest);
			log.info("Profile Deleted: {} {}", returnValue.getCode(), returnValue.getMessage());

			// TODO: Wallet Un-Delete Account Call

			// Wayagram Activate Account call
			UserIDPojo idPojo = new UserIDPojo(String.valueOf(userId));
			var resp = this.wayagramProxy.undeleteAccount(idPojo, token);
			log.info("Wayagram Account Activation: {} - {}", resp.getBody(), resp.getStatusCode());
		} catch (Exception e) {
			log.error("Error deleting user: {}", e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> validateUser() {
		UserPrincipal userPrincipal = (UserPrincipal) authenticatedUserFacade.getAuthentication().getPrincipal();
		if (userPrincipal == null || !userPrincipal.getUser().isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Invalid user."), HttpStatus.OK);
		} else {
			Users user = userPrincipal.getUser().get();
			Set<String> roles = new HashSet<>();
			Collection<Role> userRoles = user.getRoleList();
			Set<String> permits = new HashSet<>();
			for (Role r : userRoles) {
				roles.add(r.getName());
				permits.addAll(r.getPrivileges().stream().map(p -> p.getName()).collect(Collectors.toSet()));
			}
			BigDecimal tranLimit = null;
			UserAccessResponse access = userPrincipal.getAccess();
			if (access != null) {
				roles.add(access.getRoleName());
				permits.add(access.getPermissionName());
				if (user.isAdmin())
					tranLimit = access.getTransactionLimit();
			}
			if (tranLimit == null) {
				UserSetup setUp = userSetupRepository.findByUserId(user.getId());
				tranLimit = setUp == null ? new BigDecimal("0.00") : setUp.getTransactionLimit();
			}
			ValidateUserPojo validateUserPojo = new ValidateUserPojo();
			validateUserPojo.setCorporate(user.isCorporate());
			validateUserPojo.setEmail(Objects.toString(user.getEmail(), ""));
			validateUserPojo.setEmailVerified(user.isEmailVerified());
			validateUserPojo.setFirstName(user.getFirstName());
			validateUserPojo.setSurname(user.getSurname());
			validateUserPojo.setPhoneVerified(user.isPhoneVerified());
			validateUserPojo.setPinCreated(user.isPinCreated());
			validateUserPojo.setId(Objects.toString(user.getId(), "0"));
			validateUserPojo.setReferenceCode(Objects.toString(user.getReferenceCode(), ""));
			validateUserPojo.setPhoneNumber(Objects.toString(user.getPhoneNumber(), ""));
			validateUserPojo.setRoles(roles);
			validateUserPojo.setPermits(permits);
			validateUserPojo.setTransactionLimit(Objects.toString(tranLimit, "0.00"));

			return new ResponseEntity<>(new SuccessResponse("User valid.", validateUserPojo), HttpStatus.OK);
		}
	}

	@Override
	public ApiResponseBody<UserAccessResponse> getAccessResponse(Long userId) {
		try {
			return accessProxy.GetUsersAccess(userId);
		} catch (Exception e) {
			return new ApiResponseBody<>(Constant.ERROR_MESSAGE, false);
		}
	}

	@Override
	public void saveLog(LogRequest logPojo) {
		try{
			loggingProxy.saveNewLog(logPojo);
		}catch(Exception e){
			log.error("Error saving Logs:: {}", e.getMessage());
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
		String phoneNumber = user.getPhoneNumber().contains("+") ? user.getPhoneNumber()
				: (user.getPhoneNumber().startsWith("234") ? String.format("+%s", user.getPhoneNumber())
						: user.getPhoneNumber());

		UserProfileResponsePojo userDto = UserProfileResponsePojo.builder().email(user.getEmail()).id(user.getId())
				.referenceCode(user.getReferenceCode()).isEmailVerified(user.isEmailVerified()).phoneNumber(phoneNumber)
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
	public Page<Users> getAllUsers(int page, int size, String searchString) {

		List<SearchCriteria> searchCriteria = searchService.parse(searchString);
		// Default isDeleted to false
		searchCriteria.add(new SearchCriteria("isDeleted", SearchOperation.EQUALITY, "false"));

		List<SearchSpecification> specList = searchCriteria.stream()
				.map(criterion -> new SearchSpecification(criterion))
				.collect(Collectors.toList());
		Specification<Users> specs = searchService.andSpecification(specList).orElse(null);

		List<Sort> sortList = searchService.generateSortList(searchCriteria);
		Sort sort = searchService.andSort(sortList).orElse(Sort.unsorted());
		Pageable pageableRequest = PageRequest.of(page, size, sort);

		Page<Users> userPage;
		try {
			userPage = usersRepository.findAll(specs, pageableRequest);
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
		List<String> messages = new ArrayList<>();
		try {
			DevicePojo dev = reqUtil.GetDevice(device);
			final String ip = reqUtil.getClientIP(request);
			for (CorporateUserPojo mUser : userList.getUsersList()) {

				if(mUser.getPhoneNumber() != null){
					boolean isPhoneValid = phoneNumPattern.matcher(mUser.getPhoneNumber()).find()
							&& mUser.getPhoneNumber().startsWith("234")
							&& mUser.getPhoneNumber().length() == 13;
					if(!isPhoneValid){
						messages.add(String.format("Phone Number is not valid for %s, " +
								"ensure it starts with 234 and is 13 characters in length",
								mUser.getPhoneNumber()));
						continue;
					}
				}

				if(mUser.getEmail() != null){
					boolean isEmailValid = emailPattern.matcher(mUser.getEmail().toLowerCase()).matches();
					if(!isEmailValid) {
						messages.add(String.format("Email is not valid for: %s", mUser.getEmail()));
						continue;
					}
				}

				// Check if email exists
				Users user = mUser.getEmail() == null ? null
						: usersRepository.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
				if (user != null){
					messages.add(String.format("User with Email already exists: %s", mUser.getEmail()));
					continue;
				}

				// Check if Phone exists
				user = mUser.getPhoneNumber() == null ? null
						: usersRepository.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
				if (user != null) {
					messages.add(String.format("User with Phone Number exists %s",
							mUser.getPhoneNumber()));
					continue;
				}

				if(mUser.getEmail() == null && mUser.getPhoneNumber() == null){
					messages.add(String.format("Both Email and Phone Number cannot be null"));
					continue;
				}

				Role userRole = rolesRepo.findByName("ROLE_USER")
						.orElseThrow(() -> new CustomException("User Role Not Available", BAD_REQUEST));

				Role merchRole = rolesRepo.findByName("ROLE_CORP")
						.orElseThrow(() -> new CustomException("User Corp Role Not Available", BAD_REQUEST));

				List<Role> roleList = new ArrayList<>(Arrays.asList(userRole, merchRole));
				if (mUser.isAdmin()) {
					Role corpAdminRole = rolesRepo.findByName("ROLE_CORP_ADMIN")
							.orElseThrow(() -> new CustomException("User Corp Admin Role Not Available", BAD_REQUEST));
					roleList.add(corpAdminRole);
				}

				// Generate and Save Random Password
				String randomPassword = HelperUtils.generateRandomPassword();
				mUser.setPassword(randomPassword);

				user = new Users();
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

				CompletableFuture.runAsync(() -> sendNewPassword(mUser.getPassword(), regUser));

				String token = this.authService.generateToken(regUser);
				this.authService.createCorporateUser(mUser, regUser.getId(), token, getBaseUrl(request));
				++count;
			}
			String message = String
					.format("%s  Corporate Account Created Successfully and Sub-account creation in process.", count);
			messages.add(message);
			return new ResponseEntity<>(new SuccessResponse(messages), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error in Creating Bulk Account:: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> createUsers(BulkPrivateUserCreationDTO userList, HttpServletRequest request,
			Device device) {
		int count = 0;
		List<String> messages = new ArrayList<>();
		try {
			if (userList == null || userList.getUsersList().isEmpty())
				return new ResponseEntity<>(new ErrorResponse("User List cannot be null or Empty"), BAD_REQUEST);

			DevicePojo dev = reqUtil.GetDevice(device);
			final String ip = reqUtil.getClientIP(request);
			for (BaseUserPojo mUser : userList.getUsersList()) {

				if(mUser.getPhoneNumber() != null){
					boolean isPhoneValid = phoneNumPattern.matcher(mUser.getPhoneNumber()).find()
							&& mUser.getPhoneNumber().startsWith("234")
							&& mUser.getPhoneNumber().length() == 13;
					if(!isPhoneValid){
						messages.add(String.format("Phone Number is not valid for %s, " +
										"ensure it starts with 234 and is 13 characters in length",
								mUser.getPhoneNumber()));
						continue;
					}
				}

				if(mUser.getEmail() != null){
					boolean isEmailValid = emailPattern.matcher(mUser.getEmail().toLowerCase()).matches();
					if(!isEmailValid) {
						messages.add(String.format("Email is not valid for: %s", mUser.getEmail()));
						continue;
					}
				}

				// Check if email exists
				Users user = mUser.getEmail() == null ? null
						: usersRepository.findByEmailIgnoreCase(mUser.getEmail()).orElse(null);
				if (user != null){
					messages.add(String.format("User with Email already exists: %s", mUser.getEmail()));
					continue;
				}

				// Check if Phone exists
				user = mUser.getPhoneNumber() == null ? null
						: usersRepository.findByPhoneNumber(mUser.getPhoneNumber()).orElse(null);
				if (user != null) {
					messages.add(String.format("User with Phone Number exists %s",
							mUser.getPhoneNumber()));
					continue;
				}

				if(mUser.getEmail() == null && mUser.getPhoneNumber() == null){
					messages.add(String.format("Both Email and Phone Number cannot be null"));
					continue;
				}

				List<Role> roleList = new ArrayList<>();

				Role userRole = rolesRepo.findByName("ROLE_USER")
						.orElseThrow(() -> new CustomException("User Role Not Available", BAD_REQUEST));
				roleList.add(userRole);

				if (mUser.isAdmin()) {
					Role adminRole = rolesRepo.findByName("ROLE_APP_ADMIN")
							.orElseThrow(() -> new CustomException("User App Admin Role Not Available", BAD_REQUEST));
					roleList.add(adminRole);
				}

				// Generate and Save Random Password
				String randomPassword = HelperUtils.generateRandomPassword();
				mUser.setPassword(randomPassword);

				user = new Users();
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
				CompletableFuture.runAsync(() -> sendNewPassword(mUser.getPassword(), regUser));
				++count;
			}
			String message = String
					.format("%s Private Accounts Created Successfully and Sub-account creation in process.", count);
			messages.add(message);
			return new ResponseEntity<>(new SuccessResponse(messages), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error in Creating Bulk Account:: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	private void sendNewPassword(String randomPassword, Users user) {

		if(user.getEmail() != null){
			// Email Sending of new Password Here
			PasswordCreateContext context = new PasswordCreateContext();
			context.init(user);
			context.setPassword(randomPassword);
			this.messagingService.sendMail(context);
		}else{
			String message = String.format("An account has been created for you with password: %s." +
					" Kindly login with your phone Number and change your password", randomPassword);
			this.messagingService.sendSMS(user.getFirstName(), message, user.getPhoneNumber());
		}
	}

	@Override
	public ResponseEntity<?> getUserSetupById(Long id) {
		try {
			UserSetup setup = userSetupRepository.findByUserId(id);
			if (setup != null) {
				UserSetupPojo pojo = new UserSetupPojo(setup.getId(), setup.getUser().getId(),
						setup.getTransactionLimit());
				return new ResponseEntity<>(new SuccessResponse(pojo), HttpStatus.OK);
			}
			return new ResponseEntity<>(new ErrorResponse("No Setup Exists"), NOT_FOUND);
		} catch (Exception e) {
			log.error("Error in Fetching User's Setup::{}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> maintainUserSetup(UserSetupPojo pojo) {
		try {
			Optional<Users> userOpt = usersRepository.findById(Long.valueOf(pojo.getUserId()));
			if (userOpt.isEmpty()) {
				return new ResponseEntity<>(new ErrorResponse(ErrorMessages.NO_RECORD_FOUND.getErrorMessage() + " For User with " + pojo.getUserId()),
						NOT_FOUND);
			}
			Users user = usersRepository.getOne(Long.valueOf(pojo.getUserId()));
			UserSetup setup = userSetupRepository.findByUserId(user.getId());
			if (setup == null) {
				setup = new UserSetup();
				setup.setTransactionLimit(pojo.getTransactionLimit());
				setup.setUser(user);
			} else {
				setup.setTransactionLimit(pojo.getTransactionLimit());
			}
			setup = userSetupRepository.save(setup);
			pojo.setId(setup.getId());
			return new ResponseEntity<>(new SuccessResponse(pojo), HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error in Maintaining User's Setup::{}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getMessage()), BAD_REQUEST);
		}
	}

}
