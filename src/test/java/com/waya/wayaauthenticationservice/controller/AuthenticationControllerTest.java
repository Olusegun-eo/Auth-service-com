package com.waya.wayaauthenticationservice.controller;

//@ActiveProfiles("test")
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class AuthenticationControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    Users user = new Users();
//    Users userPojo = new UserPojo();
//
//    @BeforeAll
//    public void setUp() {
//        user.setEmail("stan@toju.com");
//        user.setFirstName("Stan");
//        user.setSurname("Toju");
//        user.setPhoneNumber("0029934");
//        user.setId(1);
//        user.setCorporate(false);
//        user.setEmailVerified(false);
//        userRepository.save(user);
//    }
//
//
//    @Test
//    @DisplayName("create personal profile successfully")
//    public void createUser() throws Exception {
//        UserPojo userPojo = new UserPojo();
//
//
//        createNewUser(user, "$.message", "profile created successfully", status().isCreated());
//    }
//
//
//    private void createNewUser(UserPojo userPojo, String jsonPath, String jsonPathMessage, ResultMatcher expectedStatus) throws Exception {
//
//        mockMvc.perform(post("/profile")
//                .contentType(APPLICATION_JSON).content(asJsonString(user)))
//                .andExpect(expectedStatus)
//                .andExpect(jsonPath(jsonPath, Is.is(jsonPathMessage)));
//    }

//}