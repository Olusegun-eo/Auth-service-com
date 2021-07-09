package com.waya.wayaauthenticationservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonString {

    private JsonString() {
    }

    /**
     * convert object to json string.
     *
     * @param obj object
     * @return String
     */
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}