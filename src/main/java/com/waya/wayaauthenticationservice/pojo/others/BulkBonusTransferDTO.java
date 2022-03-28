package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkBonusTransferDTO {
    @NotEmpty(message= "List Should Not be Empty")
    private Set<@Valid BonusTransferExcelPojo> bonusList;
}
