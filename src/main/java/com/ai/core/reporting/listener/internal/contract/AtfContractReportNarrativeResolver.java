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
    private static final String MCP_CONTRACT_PACKAGE = AI_ROOT_PACKAGE + "skills.mcp.contract.";
    private static final String ATLAS_CONTRACT_PACKAGE = AI_ROOT_PACKAGE + "skills.atlas.contract.";
    private static final String MCP_CONTRACT_CATALOG =
            AI_ROOT_PACKAGE + "skills.mcp.contracts.catalog.ContractCatalog";
    private static final String ATF_CONTRACT_ANNOTATION = ATF_CONTRACT_PACKAGE + "AtfContract";
    private static final String ATF_CONTRACT_SUITE_ANNOTATION = ATF_CONTRACT_PACKAGE + "AtfContractSuite";
    private static final String ATF_CONTRACT_REF_ANNOTATION = ATF_CONTRACT_PACKAGE + "AtfContractRef";
    private static final String ATF_CONTRACT_SUITE_REF_ANNOTATION =
            ATF_CONTRACT_PACKAGE + "AtfContractSuiteRef";
    private static final String MCP_CHECK_REF_ANNOTATION = MCP_CONTRACT_PACKAGE + "checks.McpCheckRef";
    private static final String MCP_SUITE_REF_ANNOTATION = MCP_CONTRACT_PACKAGE + "suites.McpSuiteRef";
    private static final String ATLAS_CONTRACT_SUITE_ANNOTATION =
            ATLAS_CONTRACT_PACKAGE + "AtlasContractSuite";
    private static final String ATLAS_CONTRACT_SUITE_REF_ANNOTATION =
            ATLAS_CONTRACT_PACKAGE + "AtlasContractSuiteRef";

    private AtfContractReportNarrativeResolver() {
    }

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

    static McpCheckMetadata findMcpCheck(ITestResult result) {
        if (result == null || result.getMethod() == null
                || result.getMethod().getConstructorOrMethod() == null) {
            return null;
        }

        Method javaMethod = result.getMethod().getConstructorOrMethod().getMethod();
        if (javaMethod == null) {
            return null;
        }
        Annotation checkRef = ReportAnnotationIntrospector.find(javaMethod.getAnnotations(), MCP_CHECK_REF_ANNOTATION);
        return checkRef == null ? null : McpCheckMetadata.fromCheckRef(
                ReportAnnotationIntrospector.readString(checkRef, "value"));
    }

    static McpSuiteMetadata findMcpSuite(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return null;
        }

        Class<?> realClass = result.getMethod().getRealClass();
        if (realClass == null) {
            return null;
        }
        Annotation suiteRef = ReportAnnotationIntrospector.find(realClass.getAnnotations(), MCP_SUITE_REF_ANNOTATION);
        return suiteRef == null ? null : McpSuiteMetadata.fromSuiteRef(
                ReportAnnotationIntrospector.readString(suiteRef, "value"));
    }

    static AtlasSuiteMetadata findAtlasSuite(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return null;
        }

        Class<?> realClass = result.getMethod().getRealClass();
        if (realClass == null) {
            return null;
        }
        Annotation suite = ReportAnnotationIntrospector.find(realClass.getAnnotations(), ATLAS_CONTRACT_SUITE_ANNOTATION);
        if (suite != null) {
            return AtlasSuiteMetadata.fromSuite(suite);
        }
        Annotation suiteRef = ReportAnnotationIntrospector.find(
                realClass.getAnnotations(), ATLAS_CONTRACT_SUITE_REF_ANNOTATION);
        return suiteRef == null ? null : AtlasSuiteMetadata.fromSuiteRef(
                ReportAnnotationIntrospector.readString(suiteRef, "value"));
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

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class McpCheckMetadata {
        final String id;
        final String expected;
        final String artifacts;
        final String failureMeaning;
        final String repairHint;

        private static McpCheckMetadata fromCheckRef(String id) {
            try {
                Object spec = ReportAnnotationIntrospector.invokeStatic(
                        MCP_CONTRACT_CATALOG,
                        "requireMcpCheck",
                        String.class,
                        id);
                return new McpCheckMetadata(
                        id,
                        ReportAnnotationIntrospector.invokeString(spec, "expected"),
                        String.join(",", ReportAnnotationIntrospector.invokeStringList(spec, "artifacts")),
                        ReportAnnotationIntrospector.invokeString(spec, "failureMeaning"),
                        ReportAnnotationIntrospector.invokeString(spec, "repairHint")
                );
            } catch (ReflectiveOperationException e) {
                return new McpCheckMetadata(
                        id,
                        "MCP check metadata could not be resolved from YAML.",
                        "",
                        "The executable method still references the MCP check id.",
                        "Repair MCP contract YAML/catalog loading for " + id + "."
                );
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class McpSuiteMetadata {
        final String id;
        final String profile;
        final String category;
        final String title;
        final String htmlReq;
        final String purpose;

        private static McpSuiteMetadata fromSuiteRef(String id) {
            try {
                Object spec = ReportAnnotationIntrospector.invokeStatic(
                        MCP_CONTRACT_CATALOG,
                        "requireMcpSuite",
                        String.class,
                        id);
                return new McpSuiteMetadata(
                        id,
                        ReportAnnotationIntrospector.invokeString(spec, "profile"),
                        ReportAnnotationIntrospector.invokeString(spec, "category"),
                        ReportAnnotationIntrospector.invokeString(spec, "title"),
                        ReportAnnotationIntrospector.invokeString(spec, "htmlReq"),
                        ReportAnnotationIntrospector.invokeString(spec, "purpose")
                );
            } catch (ReflectiveOperationException e) {
                return new McpSuiteMetadata(
                        id,
                        "unknown",
                        "unknown",
                        "MCP suite metadata could not be resolved from YAML.",
                        "",
                        "Repair MCP suite YAML/catalog loading for " + id + "."
                );
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class AtlasSuiteMetadata {
        final String id;
        final String profile;
        final String htmlOwner;
        final String artifact;
        final String sourceRoot;
        final String purpose;

        private static AtlasSuiteMetadata fromSuite(Annotation annotation) {
            return new AtlasSuiteMetadata(
                    annotation.annotationType().getName(),
                    ReportAnnotationIntrospector.readString(annotation, "profile"),
                    ReportAnnotationIntrospector.readString(annotation, "htmlOwner"),
                    ReportAnnotationIntrospector.readString(annotation, "artifact"),
                    ReportAnnotationIntrospector.readString(annotation, "sourceRoot"),
                    ReportAnnotationIntrospector.readString(annotation, "purpose")
            );
        }

        private static AtlasSuiteMetadata fromSuiteRef(String id) {
            try {
                Object spec = ReportAnnotationIntrospector.invokeStatic(
                        AI_ROOT_PACKAGE + "skills.atlas.contract.yaml.AtlasYamlSuiteCatalog",
                        "require",
                        String.class,
                        id);
                return new AtlasSuiteMetadata(
                        id,
                        ReportAnnotationIntrospector.invokeString(spec, "profile"),
                        ReportAnnotationIntrospector.invokeString(spec, "htmlOwner"),
                        ReportAnnotationIntrospector.invokeString(spec, "artifact"),
                        ReportAnnotationIntrospector.invokeString(spec, "sourceRoot"),
                        ReportAnnotationIntrospector.invokeString(spec, "purpose")
                );
            } catch (ReflectiveOperationException e) {
                return new AtlasSuiteMetadata(
                        id,
                        "unknown",
                        "",
                        "",
                        "",
                        "Repair Atlas suite YAML/catalog loading for " + id + "."
                );
            }
        }
    }

}
