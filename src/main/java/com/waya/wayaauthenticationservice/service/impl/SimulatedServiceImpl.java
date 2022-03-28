package com.waya.wayaauthenticationservice.service.impl;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.waya.wayaauthenticationservice.controller.UserController;
import com.waya.wayaauthenticationservice.dao.ProfileServiceDAO;
import com.waya.wayaauthenticationservice.entity.Privilege;
import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.exception.ErrorMessages;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfilePojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.UserProfileResponsePojo;
import com.waya.wayaauthenticationservice.proxy.VirtualAccountProxy;
import com.waya.wayaauthenticationservice.repository.UserRepository;
import com.waya.wayaauthenticationservice.response.ErrorResponse;
import com.waya.wayaauthenticationservice.response.SuccessResponse;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.SimulatedService;
import com.waya.wayaauthenticationservice.service.impl.search.SearchCriteria;
import com.waya.wayaauthenticationservice.service.impl.search.SearchOperation;
import com.waya.wayaauthenticationservice.service.impl.search.SearchService;
import com.waya.wayaauthenticationservice.service.impl.search.SearchSpecification;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class SimulatedServiceImpl implements SimulatedService {

	@Autowired
	ProfileService profileService;
	
	@Autowired
	VirtualAccountProxy virtualAccountProxy;
	
	@Autowired
	private UserRepository usersRepository;
	
	@Autowired
	SearchService searchService;
	
	@Autowired
	ProfileServiceDAO jdbcprofileService;

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

	public UserProfileResponsePojo toModelDTO(Users user) {
		if (user == null)
			return null;

		Set<String> roles = user.getRoleList().stream().map(Role::getName).collect(Collectors.toSet());
		Set<String> permits = new HashSet<>();
		user.getRoleList().forEach(
				u -> permits.addAll(u.getPrivileges().stream().map(Privilege::getName).collect(Collectors.toSet())));

		UserProfileResponsePojo userDto = UserProfileResponsePojo.builder().email(user.getEmail()).id(user.getId())
				.referenceCode(user.getReferenceCode()).isEmailVerified(user.isEmailVerified())
				.phoneNumber(user.getPhoneNumber()).firstName(user.getFirstName()).lastName(user.getSurname())
				.isAdmin(user.isAdmin()).isPhoneVerified(user.isPhoneVerified()).isAccountDeleted(user.isDeleted())
				.isAccountExpired(!user.isAccountNonExpired()).isCredentialsExpired(!user.isCredentialsNonExpired())
				.isActive(user.isActive()).isAccountLocked(!user.isAccountNonLocked()).roles(roles).permits(permits)
				.pinCreated(user.isPinCreated()).isCorporate(user.isCorporate()).build();
		userDto.add(linkTo(methodOn(UserController.class).findUser(user.getId())).withSelfRel());

		return userDto;
	}
	
	@Override
	public Page<Users> getAllUsers(int page, int size, String searchString) {

		List<SearchCriteria> searchCriteria = searchService.parse(searchString);
		// Default isDeleted to false
		searchCriteria.add(new SearchCriteria("isDeleted", SearchOperation.EQUALITY, "false"));

		List<SearchSpecification> specList = searchCriteria.stream().map(SearchSpecification::new)
				.collect(Collectors.toList());
		Specification<Users> specs = searchService.andSpecification(specList).orElse(null);

		List<Sort> sortList = searchService.generateSortList(searchCriteria);
		Sort sort = searchService.andSort(sortList).orElse(Sort.unsorted());
		Pageable pageableRequest = PageRequest.of(page, size, sort);

		Page<Users> userPage;
		try {
			userPage = usersRepository.findAllSimulated(specs, pageableRequest);
		} catch (Exception ex) {
			log.error(ex.getCause() + "message");
			String errorMessages = String.format("%s %s", ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage(),
					ex.getMessage());
			throw new CustomException(errorMessages, HttpStatus.UNPROCESSABLE_ENTITY);
		}
		return userPage;
	}
	
	public ResponseEntity<?> getAllUsersRec() {
		List<UserProfilePojo> user = jdbcprofileService.GetAllUserProfile();
		if (user.isEmpty())
            return new ResponseEntity<>(new ErrorResponse("UNABLE TO FETCH DATA"), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(new SuccessResponse("DATA FETCH", user), HttpStatus.OK);
		//return user;
	}

}
