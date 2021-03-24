package com.waya.wayaauthenticationservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class
ChatPojo {
    private String id;
    private String fromId;
    private String toId;
    private String message;
    private String type;
    private boolean forwardMessage;
    private String groupId;
    private boolean deleted;
}