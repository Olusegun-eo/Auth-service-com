package com.waya.wayaauthenticationservice.service.impl.search;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.waya.wayaauthenticationservice.entity.Users;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchSpecification implements Specification<Users> {

	private static final long serialVersionUID = 1L;

	private SearchCriteria criteria;
	
	@Override
	public Predicate toPredicate(Root<Users> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		switch (criteria.getOperation()) {
		case EQUALITY:
			return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
		case LIKE:
			final String value = "%" + criteria.getValue().toString() + "%";
			return criteriaBuilder.like(root.get(criteria.getKey()), value);
		case NEGATION:
			return criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue());
		case LESS_THAN:
			return criteriaBuilder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
		case GREATER_THAN:
			return criteriaBuilder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
		default:
			return null;
		}
	}
}
