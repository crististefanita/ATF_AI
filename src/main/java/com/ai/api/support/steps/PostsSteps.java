package com.ai.api.support.steps;

import com.ai.api.core.client.ApiActions;
import com.ai.api.support.model.PostRequest;
import com.ai.api.support.service.PostsService;
import com.ai.api.core.utils.JsonUtils;
import io.restassured.response.Response;

import java.util.Map;

import static com.ai.api.core.client.HttpMethod.GET;
import static com.ai.api.core.client.HttpMethod.POST;

public class PostsSteps {

    private final PostsService service = new PostsService();

    public Response listPosts(Map<String, ?> queryParams) {
        return ApiActions.execute(
                GET,
                service.basePath(),
                service.basePath(),
                null,
                () -> service.listPosts(queryParams)
        );
    }

    public Response getPost(Integer postId) {
        String path = service.basePath() + "/" + postId;

        return ApiActions.execute(
                GET,
                path,
                path,
                null,
                () -> service.getPost(postId)
        );
    }

    public Response listUserPosts(Integer userId, Map<String, ?> queryParams) {
        String path = "/public/v2/users/" + userId + "/posts";

        return ApiActions.execute(
                GET,
                path,
                path,
                null,
                () -> service.listUserPosts(userId, queryParams)
        );
    }

    public Response createPostForUser(Integer userId, PostRequest request) {
        String path = "/public/v2/users/" + userId + "/posts";

        return ApiActions.execute(
                POST,
                path,
                path,
                JsonUtils.toJson(request),
                () -> service.createUserPost(userId, request)
        );
    }
}
