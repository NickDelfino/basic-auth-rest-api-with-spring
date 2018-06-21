package com.example.auth.authexample;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthExampleApplicationTests {

	@LocalServerPort
	private int port;

	private URL base;

	@Autowired
	private TestRestTemplate restTemplate;

	final private String TEST_USER = "testUser";
    final private String TEST_PASSWORD = "test123";

	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port + "/");
	}

    /**
     * Creates a user using the post endpoint and then retrieves the created user using the authed endpoint.
     * @throws Exception
     */
	@Test
	public void createAndGetUserTest() throws Exception {
        createUser();

        HttpHeaders headers = new HttpHeaders(){{
            set("Authorization", getAuthorizationHeader(TEST_USER, TEST_PASSWORD));
        }};
        HttpEntity<String> entity = new HttpEntity<>(headers);

        //The response entity being returned could be turned into a class which implements a UserDetails object.
        //Instead, a string is being turn into a JSONObject to use.
        ResponseEntity<String> response = this.restTemplate.exchange(String.format(this.base + "/user/%s", TEST_USER), HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        JSONObject user = new JSONObject(response.getBody());
        assertThat("User should not be null.", user, notNullValue());
        assertThat("Usernames don't match.", user.get("username"), is(TEST_USER));
        assertThat("Passwords don't match.", user.get("password"), is(TEST_PASSWORD));
	}

    /**
     * Attempts to get user info without creating a user. The call should fail with a 403.
     * @throws Exception
     */
	@Test
    public void getUserWithoutCreateTest() throws Exception {
        ResponseEntity<Object> response = this.restTemplate.getForEntity(String.format(this.base + "/user/%s", TEST_USER), Object.class);
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    /**
     * Creates a user for test. Validates that the call was successful.
     */
	private void createUser() {
        String requestJson = String.format("{\"userName\": \"%s\", \"password\":\"%s\"}", TEST_USER, TEST_PASSWORD);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity response = this.restTemplate.postForEntity(this.base + "user", entity, String.class);
        assertThat("Failed to create user.", response.getStatusCode(), is(HttpStatus.CREATED));
    }

    /**
     * Pass in the username and password of a user to get their authorization header.
     * @param userName
     * @param password
     * @return
     */
    private String getAuthorizationHeader(String userName, String password) {
        String auth = userName + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")) );

        return "Basic " + new String( encodedAuth );
    }
}
