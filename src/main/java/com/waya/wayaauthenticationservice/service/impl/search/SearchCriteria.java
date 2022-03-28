package com.waya.wayaauthenticationservice.service.impl.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {
	private String key;
	private SearchOperation operation;
	private Object value;
}
