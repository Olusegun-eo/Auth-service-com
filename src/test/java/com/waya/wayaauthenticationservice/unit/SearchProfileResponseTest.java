package com.waya.wayaauthenticationservice.unit;

import com.waya.wayaauthenticationservice.response.SearchProfileResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class SearchProfileResponseTest {
    @Test
    void searchProfileResponse() {
        SearchProfileResponse searchProfileResponse = new SearchProfileResponse();
        searchProfileResponse.setPhoneNumber("009");
        searchProfileResponse.setEmail("a@app.com");
        searchProfileResponse.setSurname("surname");
        searchProfileResponse.setFirstName("fullName");
        searchProfileResponse.setId(UUID.fromString("40855e1e-09fe-4d2b-9a39-243af6d64ef8"));

        Assertions.assertEquals("009", searchProfileResponse.getPhoneNumber());
        Assertions.assertEquals("a@app.com", searchProfileResponse.getEmail());
        Assertions.assertEquals("surname", searchProfileResponse.getSurname());
        Assertions.assertEquals("fullName", searchProfileResponse.getFirstName());
        Assertions.assertEquals(UUID.fromString("40855e1e-09fe-4d2b-9a39-243af6d64ef8"),
                searchProfileResponse.getId());

    }
}
