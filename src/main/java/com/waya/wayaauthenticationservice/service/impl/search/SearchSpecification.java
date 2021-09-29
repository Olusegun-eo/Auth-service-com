package com.waya.wayaauthenticationservice.service.impl.search;

import com.waya.wayaauthenticationservice.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@Slf4j
public class SearchSpecification implements Specification<Users> {

	private static final long serialVersionUID = 1L;

	private SearchCriteria criteria;

	@Override
	public Predicate toPredicate(Root<Users> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		Path<?> path = root.get(criteria.getKey());
		Class<?> type = path.getJavaType();
		if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
			return isBoolean(root, criteriaBuilder, criteria);
		} else if (Date.class.isAssignableFrom(type)) {
			return isDate(root, criteriaBuilder, criteria);
		} else {
			switch (criteria.getOperation()) {
			case EQUALITY:
				return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue().toString());
			case LIKE:
				final String value = "%" + criteria.getValue().toString() + "%";
				return criteriaBuilder.like(root.get(criteria.getKey()), value);
			case NEGATION:
				return criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue().toString());
			case LESS_THAN:
				return criteriaBuilder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
			case CONTAINS:
				return toInPredicate(root, criteria, criteriaBuilder);
			case GREATER_THAN:
				return criteriaBuilder.greaterThanOrEqualTo(root.get(criteria.getKey()),
						criteria.getValue().toString());
			default:
				return null;
			}
		}
	}

	private Predicate isBoolean(Root<Users> root, CriteriaBuilder criteriaBuilder, SearchCriteria criteria) {
		switch (criteria.getOperation()) {
		case EQUALITY:
			return criteriaBuilder.equal(root.get(criteria.getKey()), Boolean.valueOf(criteria.getValue().toString()));
		case NEGATION:
			return criteriaBuilder.notEqual(root.get(criteria.getKey()),
					Boolean.valueOf(criteria.getValue().toString()));
		default:
			return null;
		}
	}

	private Predicate isDate(Root<Users> root, CriteriaBuilder criteriaBuilder, SearchCriteria criteria) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		Date datePresented = null;
		try {
			datePresented = criteria.getValue() == null ? null : formatter.parse(criteria.getValue().toString());
		} catch (ParseException ex) {
			try {
				datePresented = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse("01/01/1970 01:00:00");
			} catch (ParseException e) {
				log.error("Weird: Error converting Date");
			}
		}
		switch (criteria.getOperation()) {
		case EQUALITY:
			return criteriaBuilder.equal(root.get(criteria.getKey()), datePresented);
		case NEGATION:
			return criteriaBuilder.notEqual(root.get(criteria.getKey()), datePresented);
		case LESS_THAN:
			return criteriaBuilder.lessThanOrEqualTo(root.get(criteria.getKey()), datePresented);
		case GREATER_THAN:
			return criteriaBuilder.greaterThanOrEqualTo(root.get(criteria.getKey()), datePresented);
		default:
			return null;
		}
	}

	private Predicate toInPredicate(Root<Users> root, SearchCriteria criteria, CriteriaBuilder criteriaBuilder) {
		Join<Object, Object> bListJoin = root.join("roleList", JoinType.INNER);
		return criteriaBuilder.equal(bListJoin.get("name"), criteria.getValue().toString().toUpperCase());
//		return criteriaBuilder.and(criteriaBuilder.and(), criteriaBuilder.function("CONTAINS", Boolean.class,
//				root.get(criteria.getKey()), criteriaBuilder.literal(criteria.getValue().toString().toUpperCase())));
	}

	private Predicate inPredicate(Root<Users> root, SearchCriteria criteria) {
		if (criteria.getValue() instanceof List<?>) {
			return getFieldPath(criteria.getKey(), root)
					.in(((List<?>) criteria.getValue()).toArray());
		}
		return null;
	}

	private Path<?> getFieldPath(String key, Root<Users> root) {
		Path<?> fieldPath = root.get(key);
//		if (key.contains(".")) {
//			String[] fields = key.split("\\.");
//			for (String field : fields) {
//				if (fieldPath == null) {
//					fieldPath = root.get(field);
//				} else {
//					fieldPath = fieldPath.get(field);
//				}
//			}
//		} else {
//			fieldPath = root.get(key);
//		}
		return fieldPath;
	}

}
