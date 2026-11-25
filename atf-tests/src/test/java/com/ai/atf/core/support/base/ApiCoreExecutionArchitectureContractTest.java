package com.ai.atf.core.support.base;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.api.core.client.ApiActions;
import com.ai.api.core.client.ApiConfigKeys;
import com.ai.api.core.client.ApiExecutionReporter;
import com.ai.api.core.client.ApiExecutionRequest;
import com.ai.api.core.client.ApiExecutionResult;
import com.ai.api.core.client.ApiRateLimitPolicy;
import com.ai.api.core.client.ApiRequestExecutor;
import com.ai.api.core.client.ApiRetryDecision;
import com.ai.api.core.client.ApiRetrySleeper;
import com.ai.api.core.client.ApiRuntimeOptions;
import com.ai.api.core.client.RetryAfterParser;
import com.ai.api.core.validation.ResponseValidator;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.listener.TestListener;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        Assert.assertEquals(RetryAfterParser.parse(response(429, "0"), options), Duration.ofSeconds(5));
        Assert.assertEquals(RetryAfterParser.parse(response(429, "-7"), options), Duration.ofSeconds(5));
        Response lowercaseHeader = response(429, null);
        when(lowercaseHeader.getHeader("retry-after")).thenReturn(" 7 ");
        Assert.assertEquals(RetryAfterParser.parse(lowercaseHeader, options), Duration.ofSeconds(7));
    }

    @Test
    public void retryAfterFallbackIsDistinctFromThePositiveHeaderCap() {
        ApiRuntimeOptions options = ApiRuntimeOptions.of(
                2, Duration.ofSeconds(5), Duration.ofSeconds(3), false);

        Assert.assertEquals(RetryAfterParser.parse(response(429, null), options), Duration.ofSeconds(5),
                "An absent header returns the configured fallback without applying the positive-header cap.");
        Assert.assertEquals(RetryAfterParser.parse(response(429, "soon"), options), Duration.ofSeconds(5),
                "An unparseable header retains the configured fallback.");
        Assert.assertEquals(RetryAfterParser.parse(response(429, "0"), options), Duration.ofSeconds(5),
                "A nonpositive header retains the configured fallback.");
        Assert.assertEquals(RetryAfterParser.parse(response(429, "90"), options), Duration.ofSeconds(3),
                "A positive parsed header is capped independently of the configured fallback.");
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
    public void requestExecutorRetriesRateLimitsAndReturnsTheObservedSuccessInOrder() {
        ApiExecutionReporter reporter = mock(ApiExecutionReporter.class);
        ApiRetrySleeper sleeper = mock(ApiRetrySleeper.class);
        ApiActions.ApiCall call = mock(ApiActions.ApiCall.class);
        Response limited = response(429, "7");
        Response accepted = response(201, null);
        when(call.run()).thenReturn(limited, accepted);
        ApiExecutionRequest request = new ApiExecutionRequest("POST", "http://local.test/users", "/users", "{}", call);
        ApiRuntimeOptions options = ApiRuntimeOptions.of(2, Duration.ofSeconds(5), Duration.ofSeconds(30), false);

        ApiExecutionResult result = new ApiRequestExecutor(new ApiRateLimitPolicy(), reporter, sleeper).execute(request, options);

        Assert.assertSame(result.response(), accepted, "The final observed response must be returned unchanged.");
        Assert.assertEquals(result.attempts(), 2, "One rate limit and one success require exactly two calls.");
        ArgumentCaptor<ApiRetryDecision> delay = ArgumentCaptor.forClass(ApiRetryDecision.class);
        InOrder order = inOrder(reporter, call, sleeper);
        order.verify(reporter).request(request, options);
        order.verify(call).run();
        order.verify(reporter).response(limited, options);
        order.verify(reporter).retryDelay(delay.capture());
        order.verify(sleeper).sleep(delay.getValue());
        order.verify(reporter).retryAttempt(1);
        order.verify(reporter).request(request, options);
        order.verify(call).run();
        order.verify(reporter).response(accepted, options);
        order.verifyNoMoreInteractions();
        Assert.assertTrue(delay.getValue().retry());
        Assert.assertEquals(delay.getValue().delay(), Duration.ofSeconds(7));
    }

    @Test
    public void requestExecutorExhaustsTheRetryBudgetWithoutAnExtraCallOrSleep() {
        ApiExecutionReporter reporter = mock(ApiExecutionReporter.class);
        ApiRetrySleeper sleeper = mock(ApiRetrySleeper.class);
        ApiActions.ApiCall call = mock(ApiActions.ApiCall.class);
        Response limited = response(429, "11");
        when(call.run()).thenReturn(limited);
        ApiExecutionRequest request = new ApiExecutionRequest("GET", "http://local.test/users", "/users", null, call);
        ApiRuntimeOptions options = ApiRuntimeOptions.of(2, Duration.ofSeconds(5), Duration.ofSeconds(30), false);

        ApiExecutionResult result = new ApiRequestExecutor(new ApiRateLimitPolicy(), reporter, sleeper).execute(request, options);

        Assert.assertSame(result.response(), limited, "Exhaustion preserves the last actual 429 response.");
        Assert.assertEquals(result.attempts(), 3, "A budget of two retries permits the initial call plus two retries.");
        verify(call, times(3)).run();
        verify(reporter, times(3)).request(request, options);
        verify(reporter, times(3)).response(limited, options);
        verify(reporter).retryAttempt(1);
        verify(reporter).retryAttempt(2);
        ArgumentCaptor<ApiRetryDecision> delays = ArgumentCaptor.forClass(ApiRetryDecision.class);
        verify(reporter, times(2)).retryDelay(delays.capture());
        ArgumentCaptor<ApiRetryDecision> sleeps = ArgumentCaptor.forClass(ApiRetryDecision.class);
        verify(sleeper, times(2)).sleep(sleeps.capture());
        for (ApiRetryDecision decision : delays.getAllValues()) {
            Assert.assertTrue(decision.retry());
            Assert.assertEquals(decision.delay(), Duration.ofSeconds(11));
        }
        for (ApiRetryDecision decision : sleeps.getAllValues()) {
            Assert.assertTrue(decision.retry());
            Assert.assertEquals(decision.delay(), Duration.ofSeconds(11));
        }
        verifyNoMoreInteractions(reporter, sleeper, call);
    }

    @DataProvider(name = "nonRateLimitedStatuses")
    public Object[][] nonRateLimitedStatuses() {
        return new Object[][] {{200}, {204}, {404}, {503}};
    }

    @Test(dataProvider = "nonRateLimitedStatuses")
    public void requestExecutorDoesNotRetryStatusesOutsideTheRateLimitPolicy(int status) {
        ApiExecutionReporter reporter = mock(ApiExecutionReporter.class);
        ApiRetrySleeper sleeper = mock(ApiRetrySleeper.class);
        ApiActions.ApiCall call = mock(ApiActions.ApiCall.class);
        Response observed = response(status, null);
        when(call.run()).thenReturn(observed);
        ApiExecutionRequest request = new ApiExecutionRequest("GET", "http://local.test/users", "/users", null, call);
        ApiRuntimeOptions options = ApiRuntimeOptions.of(2, Duration.ofSeconds(5), Duration.ofSeconds(30), false);

        ApiExecutionResult result = new ApiRequestExecutor(new ApiRateLimitPolicy(), reporter, sleeper).execute(request, options);

        Assert.assertSame(result.response(), observed);
        Assert.assertEquals(result.attempts(), 1);
        InOrder order = inOrder(reporter, call);
        order.verify(reporter).request(request, options);
        order.verify(call).run();
        order.verify(reporter).response(observed, options);
        order.verifyNoMoreInteractions();
        verifyNoInteractions(sleeper);
    }

    @Test
    public void requestExecutorPropagatesCallFailureWithoutRetriesOrSyntheticResponse() {
        ApiExecutionReporter reporter = mock(ApiExecutionReporter.class);
        ApiRetrySleeper sleeper = mock(ApiRetrySleeper.class);
        ApiActions.ApiCall call = mock(ApiActions.ApiCall.class);
        IllegalStateException original = new IllegalStateException("Local request supplier failed");
        when(call.run()).thenThrow(original);
        ApiExecutionRequest request = new ApiExecutionRequest("GET", "http://local.test/users", "/users", null, call);
        ApiRuntimeOptions options = ApiRuntimeOptions.of(2, Duration.ofSeconds(5), Duration.ofSeconds(30), false);

        IllegalStateException actual = Assert.expectThrows(IllegalStateException.class,
                () -> new ApiRequestExecutor(new ApiRateLimitPolicy(), reporter, sleeper).execute(request, options));

        Assert.assertSame(actual, original, "Preserve the original supplier failure for the facade to report.");
        verify(reporter).request(request, options);
        verify(call).run();
        verifyNoMoreInteractions(reporter, call);
        verifyNoInteractions(sleeper);
    }

    @Test
    public void requestExecutorDoesNotStartAnotherAttemptWhenRetrySleepFails() {
        ApiExecutionReporter reporter = mock(ApiExecutionReporter.class);
        ApiRetrySleeper sleeper = mock(ApiRetrySleeper.class);
        ApiActions.ApiCall call = mock(ApiActions.ApiCall.class);
        Response limited = response(429, "7");
        when(call.run()).thenReturn(limited);
        IllegalStateException original = new IllegalStateException("Retry wait was interrupted");
        doThrow(original).when(sleeper).sleep(any(ApiRetryDecision.class));
        ApiExecutionRequest request = new ApiExecutionRequest("GET", "http://local.test/users", "/users", null, call);
        ApiRuntimeOptions options = ApiRuntimeOptions.of(2, Duration.ofSeconds(5), Duration.ofSeconds(30), false);

        IllegalStateException actual = Assert.expectThrows(IllegalStateException.class,
                () -> new ApiRequestExecutor(new ApiRateLimitPolicy(), reporter, sleeper).execute(request, options));

        Assert.assertSame(actual, original);
        verify(call).run();
        verify(reporter).request(request, options);
        verify(reporter).response(limited, options);
        ArgumentCaptor<ApiRetryDecision> decision = ArgumentCaptor.forClass(ApiRetryDecision.class);
        verify(reporter).retryDelay(decision.capture());
        verify(sleeper).sleep(decision.getValue());
        Assert.assertEquals(decision.getValue().delay(), Duration.ofSeconds(7));
        verify(reporter, never()).retryAttempt(anyInt());
        verifyNoMoreInteractions(reporter, call, sleeper);
    }

    @DataProvider(name = "negativeRetryBudgets")
    public Object[][] negativeRetryBudgets() {
        return new Object[][] {{-1}, {Integer.MIN_VALUE}};
    }

    @Test(dataProvider = "negativeRetryBudgets")
    public void runtimeOptionsFactoryRejectsNegativeRetryBudgetsBeforeAnyCall(int retries) {
        IllegalArgumentException failure = Assert.expectThrows(IllegalArgumentException.class,
                () -> ApiRuntimeOptions.of(retries, Duration.ZERO, Duration.ZERO, false));
        Assert.assertTrue(failure.getMessage().contains("maxRateLimitRetries"));
        Assert.assertTrue(failure.getMessage().contains(Integer.toString(retries)));
    }

    @Test
    public void configuredNegativeRetryBudgetNamesTheInvalidConfigurationKey() {
        try (AtfRuntimeConfigSandbox settings = AtfRuntimeConfigSandbox.open()
                .setSystemProperty(ApiConfigKeys.MAX_RATE_LIMIT_RETRIES, "-1")
                .setSystemProperty(ApiConfigKeys.DEFAULT_RATE_LIMIT_DELAY_SECONDS, "5")
                .setSystemProperty(ApiConfigKeys.MAX_RATE_LIMIT_DELAY_SECONDS, "30")
                .setSystemProperty(ApiConfigKeys.CONSOLE_DETAILS_ENABLED, "false")) {
            IllegalStateException failure = Assert.expectThrows(IllegalStateException.class, ApiRuntimeOptions::load);
            Assert.assertTrue(failure.getMessage().contains(ApiConfigKeys.MAX_RATE_LIMIT_RETRIES));
            Assert.assertTrue(failure.getMessage().contains("-1"));
        }
    }

    @Test
    public void zeroRetryBudgetStillExecutesTheInitialRequestWithoutSleeping() {
        ApiExecutionReporter reporter = mock(ApiExecutionReporter.class);
        ApiRetrySleeper sleeper = mock(ApiRetrySleeper.class);
        ApiActions.ApiCall call = mock(ApiActions.ApiCall.class);
        Response observed = response(429, "7");
        when(call.run()).thenReturn(observed);
        ApiExecutionRequest request = new ApiExecutionRequest("GET", "http://local.test/users", "/users", null, call);
        ApiRuntimeOptions options = ApiRuntimeOptions.of(0, Duration.ZERO, Duration.ZERO, false);

        ApiExecutionResult result = new ApiRequestExecutor(new ApiRateLimitPolicy(), reporter, sleeper).execute(request, options);

        Assert.assertEquals(options.defaultRateLimitDelay(), Duration.ZERO,
                "The test factory's existing zero-delay behavior must remain available.");
        Assert.assertEquals(options.maxRateLimitDelay(), Duration.ZERO);
        Assert.assertEquals(result.attempts(), 1);
        Assert.assertSame(result.response(), observed);
        verify(reporter).request(request, options);
        verify(call).run();
        verify(reporter).response(observed, options);
        verifyNoMoreInteractions(reporter, call);
        verifyNoInteractions(sleeper);
    }

    @Test
    public void matchingObservedStatusPublishesValidationPass() {
        ReportLogger logger = mock(ReportLogger.class);
        Response observed = response(200, null);
        StepLogger.clear();
        StepLogger.setDelegate(logger);
        try {
            StepLogger.withConsoleMuted(() -> ResponseValidator.statusIs(observed, 200));

            InOrder order = inOrder(logger);
            order.verify(logger).startStep("Validate: status=200");
            order.verify(logger).logDetail("actual=200");
            order.verify(logger).pass("Validated");
            order.verifyNoMoreInteractions();
            Assert.assertFalse(StepLogger.testHasFailed(), "Matching observed status must not mark failure.");
        } finally {
            StepLogger.clear();
        }
    }

    @DataProvider(name = "rejectedHttpStatuses")
    public Object[][] rejectedHttpStatuses() {
        return new Object[][] {{404}, {503}};
    }

    @Test(dataProvider = "rejectedHttpStatuses")
    public void completedTransportDoesNotReplaceTheStatusValidationVerdict(int status) {
        ApiExecutionReporter reporter = mock(ApiExecutionReporter.class);
        ApiRetrySleeper sleeper = mock(ApiRetrySleeper.class);
        ApiActions.ApiCall call = mock(ApiActions.ApiCall.class);
        Response observed = response(status, null);
        when(call.run()).thenReturn(observed);
        ApiExecutionRequest request = new ApiExecutionRequest("GET", "http://local.test/users", "/users", null, call);
        ApiRuntimeOptions options = ApiRuntimeOptions.of(2, Duration.ofSeconds(5), Duration.ofSeconds(30), false);
        ApiExecutionResult result = new ApiRequestExecutor(new ApiRateLimitPolicy(), reporter, sleeper).execute(request, options);
        Assert.assertSame(result.response(), observed, "Transport completion must preserve the actual error response.");
        Assert.assertEquals(result.attempts(), 1);
        verify(call).run();
        verifyNoInteractions(sleeper);

        ReportLogger logger = mock(ReportLogger.class);
        StepLogger.clear();
        StepLogger.setDelegate(logger);
        try {
            AssertionError failure = Assert.expectThrows(AssertionError.class, () ->
                    StepLogger.withConsoleMuted(() -> ResponseValidator.statusIs(result.response(), 200)));

            Assert.assertTrue(failure.getMessage().contains("Unexpected HTTP status"));
            Assert.assertTrue(failure.getMessage().contains(Integer.toString(status)));
            Assert.assertTrue(StepLogger.testHasFailed(), "A semantic status mismatch must mark the fake report failed.");
            ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> stacktrace = ArgumentCaptor.forClass(String.class);
            verify(logger).fail(message.capture(), stacktrace.capture());
            Assert.assertEquals(message.getValue(), failure.getMessage());
            Assert.assertTrue(stacktrace.getValue().contains("Unexpected HTTP status"));
            verify(logger, never()).pass(anyString());
        } finally {
            // The mocked delegate owns the negative verdict; the outer passing contract must not inherit it.
            StepLogger.clear();
        }
    }

    @Test
    public void statusValidationPreservesTheOriginalAssertionFailureWithoutPublishingPass() {
        ReportLogger logger = mock(ReportLogger.class);
        Response observed = mock(Response.class);
        AssertionError original = new AssertionError("The observed status could not be read");
        when(observed.getStatusCode()).thenThrow(original);
        StepLogger.clear();
        StepLogger.setDelegate(logger);
        try {
            AssertionError actual = Assert.expectThrows(AssertionError.class, () ->
                    StepLogger.withConsoleMuted(() -> ResponseValidator.statusIs(observed, 200)));

            Assert.assertSame(actual, original, "The validation step must rethrow the exact assertion cause.");
            Assert.assertEquals(actual.getSuppressed().length, 0, "Validation must not invent a secondary failure.");
            Assert.assertTrue(StepLogger.testHasFailed());
            ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
            verify(logger).fail(message.capture(), anyString());
            Assert.assertEquals(message.getValue(), original.getMessage());
            verify(logger, never()).pass(anyString());
        } finally {
            StepLogger.clear();
        }
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
                "com.ai.api.core.validation.ResponseValidator"));

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
