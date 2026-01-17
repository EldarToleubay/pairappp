package com.pairapp;

import com.pairapp.dto.AuthRegisterRequest;
import com.pairapp.dto.AuthResponse;
import com.pairapp.dto.MoodAnswerRequest;
import com.pairapp.dto.MoodRequestResponse;
import com.pairapp.dto.PairInviteResponse;
import com.pairapp.dto.PairJoinRequest;
import com.pairapp.enums.BaseFeeling;
import com.pairapp.enums.MoodAvoid;
import com.pairapp.enums.MoodMode;
import com.pairapp.enums.NotePreset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MoodFlowIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("pairapp")
            .withUsername("pairapp")
            .withPassword("pairapp");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-1234567890");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void moodRequestFlow() {
        AuthResponse userA = register("Alex", "alex@example.com");
        AuthResponse userB = register("Sam", "sam@example.com");

        PairInviteResponse invite = authPost(userA.accessToken(), "/api/v1/pairs/invite", null,
                PairInviteResponse.class).getBody();
        Assertions.assertNotNull(invite);

        PairJoinRequest joinRequest = new PairJoinRequest(invite.code());
        ResponseEntity<String> joinResponse = authPost(userB.accessToken(), "/api/v1/pairs/join", joinRequest,
                String.class);
        Assertions.assertTrue(joinResponse.getStatusCode().is2xxSuccessful());

        MoodRequestResponse moodRequest = authPost(userA.accessToken(), "/api/v1/mood-requests", null,
                MoodRequestResponse.class).getBody();
        Assertions.assertNotNull(moodRequest);

        ResponseEntity<MoodRequestResponse[]> pending = authGet(userB.accessToken(), "/api/v1/mood-requests/pending",
                MoodRequestResponse[].class);
        Assertions.assertEquals(1, pending.getBody().length);

        MoodAnswerRequest answer = new MoodAnswerRequest(BaseFeeling.OK, MoodMode.SUPPORT,
                List.of(MoodAvoid.PRESSURE), NotePreset.MSG_1);
        ResponseEntity<String> answerResponse = authPost(userB.accessToken(),
                "/api/v1/mood-requests/" + moodRequest.id() + "/answer", answer, String.class);
        Assertions.assertTrue(answerResponse.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> statusResponse = authGet(userA.accessToken(), "/api/v1/mood-status/partner",
                String.class);
        Assertions.assertTrue(statusResponse.getStatusCode().is2xxSuccessful());
    }

    private AuthResponse register(String name, String email) {
        AuthRegisterRequest request = new AuthRegisterRequest(name, email, "StrongPass123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(baseUrl() + "/api/v1/auth/register",
                request, AuthResponse.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        return response.getBody();
    }

    private <T> ResponseEntity<T> authPost(String token, String path, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl() + path, HttpMethod.POST, entity, responseType);
    }

    private <T> ResponseEntity<T> authGet(String token, String path, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl() + path, HttpMethod.GET, entity, responseType);
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
