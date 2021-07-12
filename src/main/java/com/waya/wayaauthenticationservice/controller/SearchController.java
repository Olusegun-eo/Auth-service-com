package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.response.SearchProfileResponse;
import com.waya.wayaauthenticationservice.service.impl.ProfileServiceImpl;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.waya.wayaauthenticationservice.response.ApiResponse;
import java.util.List;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_400;
import static com.waya.wayaauthenticationservice.util.Constant.MESSAGE_422;
import static com.waya.wayaauthenticationservice.util.Constant.*;

@Api(tags = {"Search Profile Resource"})
@SwaggerDefinition(tags = {
        @Tag(name = "Search Profile Resource", description = "REST API to Search for a users Profile.")
})
@CrossOrigin
@RestController
public class SearchController {

    private final ProfileServiceImpl profileService;

    @Autowired
    public SearchController(ProfileServiceImpl profileService) {
        this.profileService = profileService;
    }


    /**
     * search for a profile by name
     *
     * @param name name
     * @return Object
     */
    @ApiOperation(
            value = "${api.profile.search-name.description}",
            notes = "${api.profile.search-name.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("search-profile-name/{name}")
    ResponseEntity<ApiResponse<List<SearchProfileResponse>>> findWayaUserByName(@PathVariable String name){
        List<SearchProfileResponse> searchProfileResponses = profileService.searchProfileByName(name);
        ApiResponse<List<SearchProfileResponse>> response = new ApiResponse<>(searchProfileResponses,
                RETRIEVE_DATA_SUCCESS_MSG, true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * search for profile by users phone number
     *
     * @param phoneNumber phoneNumber
     * @return Object
     */
    @ApiOperation(
            value = "${api.profile.search-phone.description}",
            notes = "${api.profile.search-phone.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("search-profile-phoneNumber/{phoneNumber}")
    ResponseEntity<ApiResponse<List<SearchProfileResponse>>> findWayaUserByPhoneNumber(
            @PathVariable String phoneNumber) {
        List<SearchProfileResponse> searchProfileResponses =
                profileService.searchProfileByPhoneNumber(phoneNumber);
        ApiResponse<List<SearchProfileResponse>> response = new ApiResponse<>(searchProfileResponses,
                RETRIEVE_DATA_SUCCESS_MSG, true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * search for profile by email.
     *
     * @param email user email
     * @return Object
     */
    @ApiOperation(
            value = "${api.profile.search-email.description}",
            notes = "${api.profile.search-email.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("search-profile-email/{email}")
    ResponseEntity<ApiResponse<List<SearchProfileResponse>>> findWayaUserByEmail(@PathVariable String email)
    {
        List<SearchProfileResponse> searchProfileResponses = profileService.searchProfileByEmail(email);
        ApiResponse<List<SearchProfileResponse>> response = new ApiResponse<>(searchProfileResponses,
                RETRIEVE_DATA_SUCCESS_MSG, true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    /**
     * search for profile by organization name.
     *
     * @param organisationName organization name
     * @return Object
     */
    @ApiOperation(
            value = "${api.profile.search-organization.description}",
            notes = "${api.profile.search-organization.notes}")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("search-profile-organisationName/{organisationName}")
    ResponseEntity<ApiResponse<List<SearchProfileResponse>>> findWayaUserByOrganisationName(@PathVariable String organisationName)
    {
        List<SearchProfileResponse> searchProfileResponses = profileService
                .searchProfileByOrganizationName(organisationName);

        ApiResponse<List<SearchProfileResponse>> response = new ApiResponse<>(searchProfileResponses,
                RETRIEVE_DATA_SUCCESS_MSG, true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
