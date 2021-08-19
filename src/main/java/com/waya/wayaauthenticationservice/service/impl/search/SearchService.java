package com.waya.wayaauthenticationservice.service.impl.search;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SearchService {

	private static String wordRegex = "[a-zA-Z]\\w*";
	private static String valueRegex = "\\w+";
	private static String operatorRegex = "(:|<|>|!|\\+|-|~|HAS)";
	private static String timestampRegex = "[0-9]{2}-[0-9]{2}-[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}";
	private static String fullRegex = "(" + wordRegex + ")" + operatorRegex + "(" + timestampRegex + "|" + valueRegex + ")?,";
	private static final Pattern searchPattern = Pattern.compile(fullRegex);
	
    /**
     * Parses the search string from the endpoint.
     *
     * @param searchString unparsed search string
     * @return list of of search criteria
     */
	public List<SearchCriteria> parse(String searchString) {
	    List<SearchCriteria> searchCriterion = new ArrayList<>();
	    if (searchString != null) {
	        Matcher matcher = searchPattern.matcher(searchString + ",");
	        while (matcher.find()) {
	            SearchCriteria searchCriteria = new SearchCriteria();
				String key = matcher.group(1);
	            String operation = matcher.group(2);
				String value = matcher.group(3);

	            searchCriteria.setKey(key);
	            searchCriteria.setOperation(SearchOperation.getSimpleOperation(operation));
	            searchCriteria.setValue(value);
	            if ((searchCriteria.getOperation() != SearchOperation.SORT_DESC && searchCriteria.getOperation() != SearchOperation.SORT_ASC) || searchCriteria.getValue() == null) {
	                searchCriterion.add(searchCriteria);
	            }
	        }
	    }
	    return searchCriterion;
	}
	
	public <T, V extends Specification<T>> Optional<Specification<T>> andSpecification(List<V> criteria) {
		Iterator<V> itr = criteria.iterator();
		if (itr.hasNext()) {
			Specification<T> spec = Specification.where(itr.next());
			while (itr.hasNext()) {
				spec = spec.and(itr.next());
			}
			return Optional.of(spec);
		}
		return Optional.empty();
	}

	public <T, V extends Sort> Optional<Sort> andSort(List<V> criteria) {

		Iterator<V> itr = criteria.iterator();
		if (itr.hasNext()) {
			Sort sort = (itr.next());
			while (itr.hasNext()) {
				sort = sort.and(itr.next());
			}
			return Optional.of(sort);
		}
		return Optional.empty();
	}

	public List<Sort> generateSortList(List<SearchCriteria> criteria) {
		return criteria.stream().map((criterion) -> {
			switch (criterion.getOperation()) {
			case SORT_ASC:
				return Sort.by(Order.asc(criterion.getKey()));
			case SORT_DESC:
				return Sort.by(Order.desc(criterion.getKey()));
			default:
				return null;
			}
		}).filter((sort) -> sort != null).collect(Collectors.toList());
	}

}
