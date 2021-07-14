package com.waya.wayaauthenticationservice.pojo.others;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WayagramPojo {
    private String user_id;
    private String username;
    private boolean notPublic = false;
}
