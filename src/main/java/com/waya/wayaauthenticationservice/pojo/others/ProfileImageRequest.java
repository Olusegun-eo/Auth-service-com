package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ProfileImageRequest {

    @NotBlank(message = "please enter the image")
    private String profileImage;
}

