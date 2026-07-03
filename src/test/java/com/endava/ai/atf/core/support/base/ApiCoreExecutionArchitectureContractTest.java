package com.endava.ai.atf.core.support.base;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.api.core.client.ApiActions;
import com.endava.ai.api.core.client.ApiRateLimitPolicy;
import com.endava.ai.api.core.client.ApiRetryDecision;
import com.endava.ai.api.core.client.ApiRuntimeOptions;
import com.endava.ai.api.core.client.RetryAfterParser;
import com.endava.ai.core.reporting.listener.TestListener;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.API_CORE_EXECUTION_ARCHITECTURE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.API_CORE_EXECUTION_ARCHITECTURE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ApiCoreExecutionArchitectureContractTest {

    @Test
    public void retryAfterParserUsesDefaultsAndCapsHeaderDelay() {
        ApiRuntimeOptions options = ApiRuntimeOptions.of(
                2,
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                false
        );
        Response noHeader = response(429, null);
        Response cappedHeader = response(429, "90");
        Response invalidHeader = response(429, "soon");

        Assert.assertEquals(RetryAfterParser.parse(noHeader, options), Duration.ofSeconds(5));
        Assert.assertEquals(RetryAfterParser.parse(cappedHeader, options), Duration.ofSeconds(30));
        Assert.assertEquals(RetryAfterParser.parse(invalidHeader, options), Duration.ofSeconds(5));
    }

    @Test
    public void rateLimitPolicyReturnsTypedRetryDecision() {
        ApiRuntimeOptions options = ApiRuntimeOptions.of(
                1,
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                false
        );
        ApiRateLimitPolicy policy = new ApiRateLimitPolicy();

        ApiRetryDecision retry = policy.decide(response(429, "7"), 0, options);
        ApiRetryDecision complete = policy.decide(response(200, null), 0, options);
        ApiRetryDecision exhausted = policy.decide(response(429, "7"), 1, options);

        Assert.assertTrue(retry.retry());
        Assert.assertEquals(retry.delay(), Duration.ofSeconds(7));
        Assert.assertFalse(complete.retry());
        Assert.assertFalse(exhausted.retry());
    }

    @Test
    public void apiActionsFacadeDoesNotOwnRetryParsingOrSleeping() throws Exception {
        String source = Files.readString(AtfSourcePaths.mainJava(ApiActions.class));

        Assert.assertTrue(source.contains("ApiExecutionRequest"));
        Assert.assertTrue(source.contains("EXECUTOR.execute"));
        Assert.assertFalse(source.contains("Retry-After"));
        Assert.assertFalse(source.contains("ApiSleeper.sleep"));
        Assert.assertFalse(source.contains("ConfigManager.get"));
        Assert.assertFalse(source.contains("ConsoleLogger.console"));
        Assert.assertTrue(ApiActions.class.getDeclaredMethods().length <= 2,
                "ApiActions must stay a small compatibility facade.");
    }

    @Test
    public void responseValidatorFacadeDelegatesToAssertionFamilies() throws Exception {
        String source = Files.readString(AtfSourcePaths.mainJava(
                "com.endava.ai.api.core.validation.ResponseValidator"));

        Assert.assertTrue(source.contains("StatusAssertions.statusIs"));
        Assert.assertTrue(source.contains("HeaderAssertions.headerPresent"));
        Assert.assertTrue(source.contains("BodyAssertions.bodyContains"));
        Assert.assertTrue(source.contains("JsonFieldAssertions.fieldEquals"));
        Assert.assertTrue(source.contains("JsonArrayAssertions.arrayNotEmpty"));
        Assert.assertTrue(source.contains("ErrorPayloadAssertions.messageEquals"));
        Assert.assertTrue(source.contains("PollingAssertions.waitForStatus"));
        Assert.assertFalse(source.contains("Assert.assert"));
        Assert.assertFalse(source.contains("StepLogger.logDetail"));
        Assert.assertFalse(source.contains("WaitUtils.waitUntil"));
    }

    private static Response response(int status, String retryAfter) {
        Response response = mock(Response.class);
        when(response.getStatusCode()).thenReturn(status);
        when(response.getHeader("Retry-After")).thenReturn(retryAfter);
        when(response.getHeader("retry-after")).thenReturn(null);
        when(response.getHeader("x-ratelimit-reset")).thenReturn(null);
        when(response.getHeader("X-RateLimit-Reset")).thenReturn(null);
        return response;
    }
}
