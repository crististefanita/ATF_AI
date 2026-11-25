package com.ai.core.reporting.listener.internal.contract;

import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractNarrativeSpec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.testng.ITestResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

final class AtfContractReportNarrativeResolver {
    private static final String AI_ROOT_PACKAGE = "com.ai.";
    private static final String ATF_CONTRACT_PACKAGE = AI_ROOT_PACKAGE + "core.support.contract.";
    private static final String ATF_CONTRACT_ANNOTATION = ATF_CONTRACT_PACKAGE + "AtfContract";
    private static final String ATF_CONTRACT_SUITE_ANNOTATION = ATF_CONTRACT_PACKAGE + "AtfContractSuite";
    private static final String ATF_CONTRACT_REF_ANNOTATION = ATF_CONTRACT_PACKAGE + "AtfContractRef";
    private static final String ATF_CONTRACT_SUITE_REF_ANNOTATION =
            ATF_CONTRACT_PACKAGE + "AtfContractSuiteRef";
    private AtfContractReportNarrativeResolver() {}

    static ContractMetadata findContract(ITestResult result) {
        if (result == null || result.getMethod() == null
                || result.getMethod().getConstructorOrMethod() == null) {
            return null;
        }

        Method javaMethod = result.getMethod().getConstructorOrMethod().getMethod();
        ContractMetadata methodContract = javaMethod == null ? null : contractFrom(javaMethod.getAnnotations());
        if (methodContract != null) {
            return methodContract;
        }

        Class<?> realClass = result.getMethod().getRealClass();
        return realClass == null ? null : contractFrom(realClass.getAnnotations());
    }

    static SuiteMetadata findSuite(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return null;
        }

        Class<?> realClass = result.getMethod().getRealClass();
        return realClass == null ? null : suiteFrom(realClass.getAnnotations());
    }

    private static ContractMetadata contractFrom(Annotation[] annotations) {
        Annotation contract = ReportAnnotationIntrospector.find(annotations, ATF_CONTRACT_ANNOTATION);
        if (contract != null) {
            return ContractMetadata.from(contract);
        }
        Annotation contractRef = ReportAnnotationIntrospector.find(annotations, ATF_CONTRACT_REF_ANNOTATION);
        return contractRef == null ? null : ContractMetadata.fromContractRef(
                ReportAnnotationIntrospector.readString(contractRef, "value"));
    }

    private static SuiteMetadata suiteFrom(Annotation[] annotations) {
        Annotation suite = ReportAnnotationIntrospector.find(annotations, ATF_CONTRACT_SUITE_ANNOTATION);
        if (suite != null) {
            return SuiteMetadata.from(suite);
        }
        Annotation suiteRef = ReportAnnotationIntrospector.find(annotations, ATF_CONTRACT_SUITE_REF_ANNOTATION);
        return suiteRef == null ? null : SuiteMetadata.fromSuiteRef(
                ReportAnnotationIntrospector.readString(suiteRef, "value"));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class ContractMetadata {
        final String area;
        final String owner;
        final String expected;
        final String observed;
        final String repairHint;

        private static ContractMetadata from(Annotation annotation) {
            return new ContractMetadata(
                    ReportAnnotationIntrospector.readString(annotation, "area"),
                    ReportAnnotationIntrospector.readString(annotation, "owner"),
                    ReportAnnotationIntrospector.readString(annotation, "expected"),
                    ReportAnnotationIntrospector.readString(annotation, "observed"),
                    ReportAnnotationIntrospector.readString(annotation, "repairHint")
            );
        }

        private static ContractMetadata fromContractRef(String id) {
            AtfCoreYamlContractNarrativeSpec spec =
                    AtfCoreYamlContractCatalog.requireContractNarrativeContract(id);
            return new ContractMetadata(
                    spec.contractArea(),
                    spec.contractOwner(),
                    spec.contractExpected(),
                    spec.contractObserved(),
                    spec.contractRepairHint()
            );
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class SuiteMetadata {
        final String profile;
        final String capability;
        final String purpose;
        final String reportHint;

        private static SuiteMetadata from(Annotation annotation) {
            return new SuiteMetadata(
                    ReportAnnotationIntrospector.readString(annotation, "profile"),
                    ReportAnnotationIntrospector.readString(annotation, "capability"),
                    ReportAnnotationIntrospector.readString(annotation, "purpose"),
                    ReportAnnotationIntrospector.readString(annotation, "reportHint")
            );
        }

        private static SuiteMetadata fromSuiteRef(String id) {
            AtfCoreYamlContractNarrativeSpec spec =
                    AtfCoreYamlContractCatalog.requireContractNarrativeSuite(id);
            return new SuiteMetadata(
                    spec.suiteProfile(),
                    spec.suiteCapability(),
                    spec.suitePurpose(),
                    spec.suiteReportHint()
            );
        }
    }

}
