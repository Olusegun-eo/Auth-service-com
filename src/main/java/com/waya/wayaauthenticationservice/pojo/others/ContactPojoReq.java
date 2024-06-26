package com.waya.wayaauthenticationservice.pojo.others;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactPojoReq {
    private List<ContactPojo> contacts;
}
