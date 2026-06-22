package com.hospital;

import com.hospital.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of the role-based access control rules, booting the real app
 * (H2 + seeded demo data) on a random port and calling it over HTTP.
 *
 * This is the safety net for the single most important behavior in the project:
 * "a doctor sees only their own patients."
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccessControlTest {

    @Autowired
    TestRestTemplate rest;

    private String login(String email, String password) {
        LoginResponse res = rest.postForObject(
                "/api/auth/login", Map.of("email", email, "password", password), LoginResponse.class);
        return res.accessToken();
    }

    private HttpEntity<Void> auth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return new HttpEntity<>(h);
    }

    @Test
    void admin_sees_all_patients() {
        String token = login("rakesh@hospital.in", "admin123");
        ResponseEntity<Map> res = rest.exchange(
                "/api/patients?size=50", HttpMethod.GET, auth(token), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("total_elements")).isEqualTo(14); // all seeded patients
        assertThat((List<?>) res.getBody().get("content")).hasSize(14);
    }

    @Test
    void doctor_sees_only_their_own_patients() {
        String token = login("kamlesh@hospital.in", "doctor123");
        ResponseEntity<Map> res = rest.exchange(
                "/api/patients?size=50", HttpMethod.GET, auth(token), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("total_elements")).isEqualTo(7); // only Dr. Kamlesh's patients
    }

    @Test
    void patient_list_is_paginated() {
        String token = login("rakesh@hospital.in", "admin123");
        ResponseEntity<Map> res = rest.exchange(
                "/api/patients?page=0&size=10", HttpMethod.GET, auth(token), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) res.getBody().get("content")).hasSize(10); // first page caps at 10
        assertThat(res.getBody().get("total_pages")).isEqualTo(2);
        assertThat(res.getBody().get("first")).isEqualTo(true);
    }

    @Test
    void doctor_cannot_view_another_doctors_patient() {
        String token = login("kamlesh@hospital.in", "doctor123");
        // Patient 3 (Vivaan) is assigned to Dr. Ravali, not Dr. Kamlesh.
        ResponseEntity<String> res = rest.exchange(
                "/api/patients/3", HttpMethod.GET, auth(token), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody()).contains("Not authorized");
    }

    @Test
    void unauthenticated_request_is_rejected() {
        ResponseEntity<String> res = rest.getForEntity("/api/patients", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void admin_sees_all_appointments_doctor_sees_only_theirs() {
        String adminToken = login("rakesh@hospital.in", "admin123");
        ResponseEntity<Map> all = rest.exchange(
                "/api/appointments?size=50", HttpMethod.GET, auth(adminToken), Map.class);
        assertThat(all.getBody().get("total_elements")).isEqualTo(4); // all seeded appointments

        String raoToken = login("kamlesh@hospital.in", "doctor123");
        ResponseEntity<Map> mine = rest.exchange(
                "/api/appointments?size=50", HttpMethod.GET, auth(raoToken), Map.class);
        assertThat(mine.getBody().get("total_elements")).isEqualTo(3); // only Dr. Kamlesh's appointments
    }

    @Test
    void user_can_change_own_password_then_login_with_it() {
        String token = login("reception@hospital.in", "reception123");
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);

        // Wrong current password is rejected (400).
        HttpEntity<Map<String, String>> bad = new HttpEntity<>(
                Map.of("current_password", "wrong", "new_password", "newpass123"), h);
        assertThat(rest.postForEntity("/api/auth/change-password", bad, String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        // Correct current password succeeds.
        HttpEntity<Map<String, String>> ok = new HttpEntity<>(
                Map.of("current_password", "reception123", "new_password", "newpass123"), h);
        assertThat(rest.postForEntity("/api/auth/change-password", ok, String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        // The new password now works for login (proves the change persisted).
        assertThat(login("reception@hospital.in", "newpass123")).isNotBlank();
    }

    @Test
    void doctor_cannot_register_users() {
        String token = login("kamlesh@hospital.in", "doctor123");
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        HttpEntity<Map<String, String>> req = new HttpEntity<>(
                Map.of("name", "X", "email", "x@x.com", "password", "secret1", "role", "DOCTOR"), h);
        ResponseEntity<String> res = rest.postForEntity("/api/auth/register", req, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
