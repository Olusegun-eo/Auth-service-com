package com.waya.wayaauthenticationservice.proxy;

import org.slf4j.Logger;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.proxy.impl.ApiClientExceptionHandler;
import com.waya.wayaauthenticationservice.response.ApiResponseBody;
import com.waya.wayaauthenticationservice.response.ImageUrlResponse;
import com.waya.wayaauthenticationservice.util.HandleFeignError;

@FeignClient(name = "file-resource-service", url = "${app.config.file-resource.base-url}")
public interface FileResourceServiceFeignClient {

	@HandleFeignError(ApiClientExceptionHandler.class)
	@PostMapping(value = "/upload/others", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ApiResponseBody<String> uploadOtherImage(@RequestPart("file") MultipartFile file,
			@RequestParam("fileName") String fileName, @RequestParam("userId") String userId);

	@PostMapping(value = "/upload/profile-picture/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ApiResponseBody<ImageUrlResponse> uploadProfileImage(@RequestPart("file") MultipartFile file,
			@PathVariable("userId") String userId);

	/**
	 * An implementation for uploading image by calling the file resource service
	 * using feign client
	 *
	 * @return ApiResponse<ProfileImageResponse>
	 */
	static ApiResponseBody<ImageUrlResponse> uploadImage(FileResourceServiceFeignClient fileResourceServiceFeignClient,
			MultipartFile profileImage, String userId, Logger log) {
		String error = "error";
		try {
			ApiResponseBody<ImageUrlResponse> apiResponse = fileResourceServiceFeignClient.uploadProfileImage(profileImage,
					userId);
			log.info("calling file resource service...... :::");
			if (apiResponse.getStatus()) {
				return apiResponse;
			}
			error = apiResponse.getMessage();
			throw new CustomException(error, HttpStatus.UNPROCESSABLE_ENTITY);
		} catch (Exception exception) {
			log.error(error, exception);
			throw new CustomException("encountered and error while trying to upload image",
					HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
}
