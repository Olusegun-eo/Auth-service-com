package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.ReferralBonusRequest;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.ReferralBonusResponse;
import com.waya.wayaauthenticationservice.service.ManageReferralService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.service.ReferralService;
import com.waya.wayaauthenticationservice.util.CommonUtils;
import com.waya.wayaauthenticationservice.util.Constant;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.Map;

import static com.waya.wayaauthenticationservice.util.Constant.*;

@Tag(name = "REFERRAL ADMIN RESOURCE", description = "REST API for Referral Admin Service API")
@CrossOrigin
@RestController
@RequestMapping("/api/v1/referral/admin")
@Validated
public class ReferralAdminController {


    private final ProfileService profileService;
    private final ManageReferralService referralService;

    @Autowired
    public ReferralAdminController(ProfileService profileService, ManageReferralService referralService) {
        this.profileService = profileService;
        this.referralService = referralService;
    }

    @ApiOperation( value = "profile-with-five-transactions/user/{userId}", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/filter-users/{value}")
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> getUsersWithUpToFiveTransactions(
            @PathVariable String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> referralCodeResponse =  profileService.getUsersWithTheirReferralsByPhoneNumber(value,page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @ApiOperation( value = "GET REFERRALS USERS", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/get-referral-users")
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> getUsersWithTheirReferrals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> referralCodeResponse =  profileService.getUsersWithTheirReferrals(page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Edit Referral Bonus Amount : This API is used to modify a bonus amount", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = MESSAGE_200),
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PostMapping("/config/amount")
    ResponseEntity<ApiResponseBody<ReferralBonusResponse>> configureReferralAmount(@Valid @RequestBody ReferralBonusRequest referralBonusRequest) throws CustomException {


        ReferralBonusResponse userProfileResponse = referralService.createReferralAmount(referralBonusRequest);
        ApiResponseBody<ReferralBonusResponse> response = new ApiResponseBody<>(userProfileResponse, "created data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Edit Referral Bonus Amount : This API is used to modify a bonus amount", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = MESSAGE_200),
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @PutMapping("/config/amount")
    ResponseEntity<ApiResponseBody<ReferralBonus>> updateReferralAmount(@Valid @RequestBody ReferralBonusRequest referralBonusRequest, @ApiIgnore @RequestAttribute(Constant.USERNAME) String username, @RequestHeader("Authorization") String token) throws URISyntaxException, CustomException {

        ReferralBonus userProfileResponse = referralService.editReferralAmount(referralBonusRequest);
        ApiResponseBody<ReferralBonus> response = new ApiResponseBody<>(userProfileResponse, "updated data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Referral Bonus Amount : This API is used to get a bonus amount by Id", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = MESSAGE_200),
            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
    })
    @GetMapping("/config/amount/{id}")
    ResponseEntity<ApiResponseBody<ReferralBonus>> getReferralBonus( @PathVariable String id, @ApiIgnore @RequestAttribute(Constant.USERNAME) String username, @RequestHeader("Authorization") String token) throws CustomException {

        ReferralBonus userProfileResponse = referralService.findReferralBonus(id);
        ApiResponseBody<ReferralBonus> response = new ApiResponseBody<>(userProfileResponse, "done data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Toggle ReferralBonus By Id : This API is used to disable/enable or off/on ReferralBonus status by providing an Id.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @PutMapping("/config/amount/{id}/toggle")
    ResponseEntity<ApiResponseBody<ReferralBonus>> toggleReferralAmount(@ApiParam(example = "1") @PathVariable String id) throws CustomException {


        ReferralBonus referralBonus = referralService.toggleReferralAmount(Long.parseLong(id));
        ApiResponseBody<ReferralBonus> response = new ApiResponseBody<>(referralBonus, "updated data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
