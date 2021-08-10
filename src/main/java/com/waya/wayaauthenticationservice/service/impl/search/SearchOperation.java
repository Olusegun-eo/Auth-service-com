package com.waya.wayaauthenticationservice.service.impl.search;

public enum SearchOperation {
	EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, SORT_ASC, SORT_DESC, LIKE;

    public static final String[] SIMPLE_OPERATION_SET = {":", "!", ">", "<", "+", "-", "~"};

    public static SearchOperation getSimpleOperation(String input) {
        switch (input) {
            case ":":
                return EQUALITY;
            case "~":
                return LIKE;
            case "!":
                return NEGATION;
            case ">":
                return GREATER_THAN;
            case "<":
                return LESS_THAN;
            case "+":
            case " ": // + is encoded in query strings as a space
                return SORT_ASC;
            case "-":
                return SORT_DESC;
            default:
                return null;
        }
    }
}