package com.waya.wayaauthenticationservice.controller;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.entity.ReferralBonusEarning;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.*;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.NewWalletResponse;
import com.waya.wayaauthenticationservice.response.ReferralBonusResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.ManageReferralService;
import com.waya.wayaauthenticationservice.service.ProfileService;
import com.waya.wayaauthenticationservice.util.Constant;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.waya.wayaauthenticationservice.util.Constant.*;

@Tag(name = "REFERRAL ADMIN RESOURCE", description = "REST API for Referral Admin Service API")
@CrossOrigin
@RestController
@RequestMapping("/api/v1/referral/admin")
@PreAuthorize(value = "hasRole('APP_ADMIN')")
@Validated
public class ReferralAdminController {


    private final ProfileService profileService;
    private final ManageReferralService referralService;

    @Autowired
    public ReferralAdminController(ProfileService profileService, ManageReferralService referralService) {
        this.profileService = profileService;
        this.referralService = referralService;
    }


    @ApiOperation(value = "Save Referral Bonus Amount : This API is used to modify a bonus amount", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
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
//
//    @ApiOperation(value = "Edit Referral Bonus Amount : This API is used to modify a bonus amount", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
//    @ApiResponses(value = {
//            @io.swagger.annotations.ApiResponse(code = 200, message = MESSAGE_200),
//            @io.swagger.annotations.ApiResponse(code = 400, message = MESSAGE_400),
//            @io.swagger.annotations.ApiResponse(code = 422, message = MESSAGE_422)
//    })
//    @GetMapping("/config/amount")
//    ResponseEntity<ApiResponseBody<ReferralBonus>> updateReferralAmount(@Valid @RequestBody ReferralBonusRequest referralBonusRequest, @ApiIgnore @RequestAttribute(Constant.USERNAME) String username, @RequestHeader("Authorization") String token) throws URISyntaxException, CustomException {
//
//        ReferralBonus userProfileResponse = referralService.editReferralAmount(referralBonusRequest);
//        ApiResponseBody<ReferralBonus> response = new ApiResponseBody<>(userProfileResponse, "updated data successfully", true);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

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

    @ApiOperation( value = "/filter-users/{value}", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/filter-users/{value}")
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> getUsersWithTheirReferralsByPhoneNumber(
            @PathVariable String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> referralCodeResponse =  referralService.getUsersWithTheirReferralsByPhoneNumber(value,page,size);
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
        Map<String, Object> referralCodeResponse =  referralService.getUsersWithTheirReferrals(page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation( value = "GET USERS THAT HAVE BEEN REFERRED", notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = Constant.MESSAGE_400),
            @io.swagger.annotations.ApiResponse(code = 422, message = Constant.MESSAGE_422)
    })
    @GetMapping("/get-users-that-have-been-referred/{referralCode}")
    public ResponseEntity<ApiResponseBody<Map<String, Object>>> getUserThanHaveBeenReferred(
            @PathVariable String referralCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> referralCodeResponse =  referralService.getUserThatHaveBeenReferred(referralCode,page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(referralCodeResponse, "Retrieved data successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @ApiOperation(value = " Get All users without referralCode.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @GetMapping("/users-with-no-referral-code")
    ResponseEntity<ApiResponseBody<Map<String, Object>>> getUserWithoutReferralCode(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws CustomException {

        Map<String, Object> map = referralService.getUserWithoutReferralCode(page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(map, "Data retrieved successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = " Get All users without referralCode.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @GetMapping("/users-with-no-referral-code/list")
    ResponseEntity<ApiResponseBody<List<Profile>>> getUserWithoutReferralCode() throws CustomException {
        List<Profile> map = referralService.getUserWithoutReferralCode();
        ApiResponseBody<List<Profile>> response = new ApiResponseBody<>(map, "Data retrieved successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @ApiOperation(value = "Assign referralcode to users.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping("/users/assign-referral-code")
    ResponseEntity<ApiResponseBody<Profile>> assignReferralCode(@Valid @RequestBody
        AssignReferralCodePojo assignReferralCodePojo) throws CustomException {

        Profile referralBonus = referralService.assignReferralCode(assignReferralCodePojo);
        ApiResponseBody<Profile> response = new ApiResponseBody<>(referralBonus, "Referral code assigned successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @ApiOperation(value = "Send referral bonus to users.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping("/users/send-referral-bonus")
    ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> sendReferralBonusToUser(@Valid @RequestBody
                                                                                                 BonusTransferRequest transfer) throws CustomException {
        List<WalletTransactionPojo> referralBonus = referralService.sendReferralBonusToUser(transfer);
        ApiResponseBody<List<WalletTransactionPojo>> response = new ApiResponseBody<>(referralBonus, "Referral Bonus sent successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @ApiOperation(value = "Send referral bonus to multiple users.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping("/users/send-referral-bonus-to-multiple-users")
    ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> sendReferralBonusToMultipleUsers(@Valid @RequestBody
                                                                                                List<BonusTransferRequest> transfer) throws CustomException {
        List<WalletTransactionPojo> referralBonus = referralService.sendReferralBonusToMultipleUsers(transfer);
        ApiResponseBody<List<WalletTransactionPojo>> response = new ApiResponseBody<>(referralBonus, "Referral Bonus sent successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @ApiOperation(value = "Auto send referral bonus to users.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping("/users/auto-send-referral-bonus/{userId}")
    ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> sendSignUpBonusToUser(@PathVariable String userId) throws CustomException {
        List<WalletTransactionPojo> referralBonus = profileService.sendSignUpBonusToUser(userId);
        ApiResponseBody<List<WalletTransactionPojo>> response = new ApiResponseBody<>(referralBonus, "Referral Bonus sent successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Bulk Referral Bonus Payment", tags = { "REFERRAL ADMIN RESOURCE" })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Response Headers") })
    @PostMapping(path = "/users/bulk-referral-bonus-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendBulkReferralBonusTo(@RequestPart("file") MultipartFile file, HttpServletRequest request, Device device) {
        return referralService.sendBulkReferralBonusTo(file, request, device);
    }



    @ApiOperation(value = " Get All users SMS alert status.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @GetMapping("/get-all-users-sms-alert-status")
    ResponseEntity<ApiResponseBody<Map<String, Object>>> getUsersSMSAlertStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws CustomException {
        Map<String, Object> map = referralService.getUsersSMSAlertStatus(page,size);
        ApiResponseBody<Map<String, Object>> response = new ApiResponseBody<>(map, "Data retrieved successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @ApiOperation(value = "Send refund failed transaction to users users.",notes = "", tags = {"REFERRAL ADMIN RESOURCE"})
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successful"),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping("/users/refund-faild-transaction")
    ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> refundFailedTransaction(@Valid @RequestBody
                                                                                                 RefundTransactionRequest transfer) throws CustomException {
        List<WalletTransactionPojo> referralBonus = referralService.refundFailedTransaction(transfer);
        ApiResponseBody<List<WalletTransactionPojo>> response = new ApiResponseBody<>(referralBonus, "Referral Bonus sent successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }








}
