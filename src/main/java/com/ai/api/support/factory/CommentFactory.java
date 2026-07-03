package com.ai.api.support.factory;

import com.ai.api.support.model.CommentRequest;
import com.ai.api.core.utils.DataGenerator;

import java.util.Random;

public final class CommentFactory {
    private CommentFactory() {
    }

    public static CommentRequest validComment(String testName) {
        Random random = DataGenerator.forTest(testName);
        int suffix = 1000 + random.nextInt(9000);

        CommentRequest request = new CommentRequest();
        request.name = "Commenter " + suffix;
        request.email = DataGenerator.randomEmail(random);
        request.body = "Comment body for " + testName + " " + suffix;
        return request;
    }
}
