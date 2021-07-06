package com.waya.wayaauthenticationservice.proxy;

import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.response.ProfileImageResponse;
import com.waya.wayaauthenticationservice.util.ApiResponse;
import org.slf4j.Logger;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "file-resource-service", url = "http://46.101.41.187:9098/file-resource/api")
public interface FileResourceServiceFeignClient {

    @PostMapping(value = "/upload/profile-picture/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ApiResponse<ProfileImageResponse> uploadProfileImage(@RequestPart("file") MultipartFile file, @PathVariable("userId") String userId);

    /**
     * An implementation for uploading image by calling the file resource service using feign client
     *
     * @return ApiResponse<ProfileImageResponse>
     */
    static ApiResponse<ProfileImageResponse> uploadImage(FileResourceServiceFeignClient fileResourceServiceFeignClient,
                                                         MultipartFile profileImage, String userId, Logger log) {
        String error = "error";
        try {
            ApiResponse<ProfileImageResponse> apiResponse = fileResourceServiceFeignClient.uploadProfileImage(profileImage, userId);
            log.info("calling file resource service...... :::");
            if (apiResponse.getStatus()) {
                return apiResponse;
            }
            error = apiResponse.getMessage();
            throw new CustomException("", HttpStatus.UNPROCESSABLE_ENTITY);

        } catch (Exception exception) {
            log.error(error, exception);
            throw new CustomException("encountered and error while trying to upload image", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
