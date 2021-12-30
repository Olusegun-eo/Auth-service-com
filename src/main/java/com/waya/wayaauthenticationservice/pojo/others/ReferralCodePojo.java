package com.waya.wayaauthenticationservice.pojo.others;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Data;

@Data
public class ReferralCodePojo {

    private UUID id;
    private String referralCode;
    private String userId;
    private String profile;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    public ReferralCodePojo(String referralCode, String profile, String userId) {
        this.referralCode = referralCode;
        this.profile = profile;
        this.userId = userId;
    }

	public ReferralCodePojo() {
		super();
	}
    
}
