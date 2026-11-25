package com.ai.atf.core.reporting.adapters.support;

import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ExtentSparkPresentationFixtureSupport extends ExtentSparkPresentationLifecycleSupport {

    protected static ITestResult successResult(String methodName) {
        return ExtentSparkTestNgResults.successResult(methodName);
    }

    protected static ITestResult result(String methodName, Class<?> realClass, int status) {
        return ExtentSparkTestNgResults.result(methodName, realClass, status);
    }

    protected static ITestResult result(String methodName, Class<?> realClass, int status, Throwable throwable) {
        return ExtentSparkTestNgResults.result(methodName, realClass, status, throwable);
    }

    protected static ITestNGMethod fakeMethod(String methodName) {
        return ExtentSparkTestNgResults.fakeMethod(methodName);
    }

    protected static ITestNGMethod fakeMethod(String methodName, Class<?> realClass) {
        return ExtentSparkTestNgResults.fakeMethod(methodName, realClass);
    }

    protected String nestedExtentReportHtml() throws IOException {
        return ExtentSparkReportFixtures.nestedExtentReportHtml(contractReportsDir);
    }

    protected Path apiLikeNestedExtentReport(String methodName) throws IOException {
        return ExtentSparkReportFixtures.apiLikeNestedExtentReport(contractReportsDir, methodName);
    }

    protected Path uiLikeNestedExtentReport(String methodName) throws IOException {
        return ExtentSparkReportFixtures.uiLikeNestedExtentReport(contractReportsDir, methodName);
    }

    protected Path finalScreenshotExtentReport(String screenshotTitle) throws IOException {
        return ExtentSparkReportFixtures.finalScreenshotExtentReport(
                contractReportsDir,
                screenshotTitle,
                ExtentSparkVisualContractTokens.ONE_PIXEL_PNG
        );
    }

    protected Path finalScreenshotRuntimeAuditedDashboardReport(String screenshotTitle) throws IOException {
        return ExtentSparkReportFixtures.finalScreenshotRuntimeAuditedDashboardReport(
                contractReportsDir,
                screenshotTitle,
                ExtentSparkVisualContractTokens.ONE_PIXEL_PNG
        );
    }

    protected Path multiCollectionTaggedExtentReport() throws IOException {
        return ExtentSparkReportFixtures.multiCollectionTaggedExtentReport(contractReportsDir);
    }

    protected Path componentAdapterFailureReport() throws IOException {
        return ExtentSparkReportFixtures.componentAdapterFailureReport(contractReportsDir);
    }

    protected Path passFailSkipStatusDropdownReport() throws IOException {
        return ExtentSparkStatusDropdownFixtures.passFailSkipStatusDropdownReport(contractReportsDir);
    }

    protected String runtimeAuditedDashboardHtml() throws IOException {
        return Files.readString(runtimeAuditedDashboardReport());
    }

    protected String runtimeAuditedSingleClassDashboardHtml() throws IOException {
        return ExtentSparkReportFixtures.runtimeAuditedSingleClassDashboardHtml(contractReportsDir);
    }

    protected Path runtimeAuditedDashboardReport() throws IOException {
        return ExtentSparkReportFixtures.runtimeAuditedDashboardReport(contractReportsDir);
    }

    protected Path runtimeAuditedFailureFixtureReport() throws IOException {
        return ExtentSparkReportFixtures.runtimeAuditedFailureFixtureReport(contractReportsDir);
    }

    protected Path extentReportContaining(String marker) throws IOException {
        return ExtentAdapterContractSupport.extentReportContaining(contractReportsDir, marker);
    }

    protected long extentReportCount() throws IOException {
        return ExtentAdapterContractSupport.countExtentHtmlReports(contractReportsDir);
    }
}
