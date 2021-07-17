package com.waya.wayaauthenticationservice.pojo.others;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WayagramPojo {

    private String user_id;
    private String username;
    private String displayName;
    private boolean notPublic = false;
}
