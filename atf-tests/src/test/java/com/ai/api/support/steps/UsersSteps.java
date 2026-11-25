package com.ai.api.support.steps;

import com.ai.api.core.client.ApiActions;
import com.ai.api.core.client.ApiClient;
import com.ai.api.support.factory.UserFactory;
import com.ai.api.support.model.UserRequest;
import com.ai.api.support.model.UserResponse;
import com.ai.api.support.service.UsersService;
import com.ai.api.core.utils.JsonUtils;
import com.ai.api.core.validation.ResponseValidator;
import io.restassured.response.Response;

import java.util.Map;

import static com.ai.api.core.client.HttpMethod.DELETE;
import static com.ai.api.core.client.HttpMethod.GET;
import static com.ai.api.core.client.HttpMethod.PATCH;
import static com.ai.api.core.client.HttpMethod.POST;
import static com.ai.api.core.client.HttpMethod.PUT;

public class UsersSteps {

    private static final String CREATE_VALID_USER = "create_user";
    private static final String CREATE_INVALID_EMAIL = "invalid_email_is_rejected";

    private final UsersService svc = new UsersService();

    public UserResponse createValidUser() {
        return createAndValidateUser(
                UserFactory.validUser(CREATE_VALID_USER)
        ).as(UserResponse.class);
    }

    public Response createValidUserResponse() {
        return createAndValidateUser(
                UserFactory.validUser(CREATE_VALID_USER)
        );
    }

    public Response createUserResponse(UserRequest request) {
        return createUser(request);
    }

    public Response createUserWithInvalidEmail(String invalidEmail) {
        return createUser(
                UserFactory.withEmail(CREATE_INVALID_EMAIL, invalidEmail)
        );
    }

    public Response createUserWithoutAuth() {
        UserRequest req = UserFactory.validUser("missing_auth");

        return executeWithBody(
                POST,
                svc.basePath(),
                req,
                () -> ApiClient.requestWithoutAuth()
                        .body(req)
                        .post(svc.basePath())
        );
    }

    public Response createUserWithAuthToken(UserRequest request, String token) {
        return executeWithBody(
                POST,
                svc.basePath(),
                request,
                () -> ApiClient.requestWithoutAuth()
                        .header("Authorization", "Bearer " + token)
                        .body(request)
                        .post(svc.basePath())
        );
    }

    public Response createUserWithRawJson(String rawJson) {
        return execute(
                POST,
                svc.basePath(),
                rawJson,
                () -> ApiClient.request()
                        .body(rawJson)
                        .post(svc.basePath())
        );
    }

    public Response getUser(Integer id) {
        String path = svc.basePath() + "/" + id;

        return execute(
                GET,
                path,
                null,
                () -> svc.getUser(id)
        );
    }

    public Response listUsers(Map<String, ?> queryParams) {
        return execute(
                GET,
                svc.basePath(),
                null,
                () -> svc.listUsers(queryParams)
        );
    }

    public Response waitUntilAvailable(Integer userId) {
        return ResponseValidator.waitForStatus(
                () -> svc.getUser(userId),
                200
        );
    }

    public Response patchUser(Integer id, UserRequest patch) {
        String path = svc.basePath() + "/" + id;

        return executeWithBody(
                PATCH,
                path,
                patch,
                () -> svc.patchUser(id, patch)
        );
    }

    public Response replaceUser(Integer id, UserRequest replacement) {
        String path = svc.basePath() + "/" + id;

        return executeWithBody(
                PUT,
                path,
                replacement,
                () -> svc.putUser(id, replacement)
        );
    }

    public Response deleteUser(Integer userId) {
        String path = svc.basePath() + "/" + userId;

        Response resp = execute(
                DELETE,
                path,
                null,
                () -> svc.deleteUser(userId)
        );

        ResponseValidator.statusIs(resp, 204);
        return resp;
    }

    public Response deleteUserNonExisting(Integer id) {
        String path = svc.basePath() + "/" + id;

        return execute(
                DELETE,
                path,
                null,
                () -> svc.deleteUser(id)
        );
    }

    public void deleteUserSilently(Integer userId) {
        try {
            svc.deleteUser(userId);
        } catch (Exception ignored) {
            // Best-effort cleanup without reporting noise.
        }
    }

    private Response createAndValidateUser(UserRequest req) {
        Response resp = createUser(req);
        ResponseValidator.statusIs(resp, 201);
        return resp;
    }

    private Response createUser(UserRequest req) {
        return executeWithBody(
                POST,
                svc.basePath(),
                req,
                () -> svc.createUser(req)
        );
    }

    private Response executeWithBody(
            String method,
            String path,
            Object body,
            ApiActions.ApiCall call
    ) {
        return execute(method, path, JsonUtils.toJson(body), call);
    }

    private Response execute(
            String method,
            String path,
            String payload,
            ApiActions.ApiCall call
    ) {
        return ApiActions.execute(method, path, path, payload, call);
    }
}
