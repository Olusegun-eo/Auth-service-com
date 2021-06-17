package com.waya.wayaauthenticationservice.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@JsonDeserialize
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletStatus {

	private Long id;
    private String code;
    private String value;
    private boolean submittedAndPendingApproval;
    private boolean approved;
    private boolean rejected;
    private boolean withdrawnByApplicant;
    private boolean active;
    private boolean closed;
}


/**
 *    "status": {
 *         "id": 93,
 *         "code": "savingsAccountStatusType.active",
 *         "value": "Active",
 *         "submittedAndPendingApproval": false,
 *         "approved": true,
 *         "rejected": false,
 *         "withdrawnByApplicant": false,
 *         "active": true,
 *         "closed": false
 *       },
 */
