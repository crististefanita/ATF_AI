package com.endava.ai.core.support.contract.ids;

import com.endava.ai.core.support.contract.AtfContract;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuite;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractNarrativeSpec;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;

public final class AtfContractSteps {

    private AtfContractSteps() {
    }

    public static void expected(AtfContract contract) {
        if (AtfContractNarrationState.autoExpectedPublished()) {
            return;
        }
        StepLogger.startStep(AtfContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT);
        logDetail(AtfContractReportLabels.AREA, contract.area());
        logDetail(AtfContractReportLabels.OWNER, contract.owner());
        logDetail(AtfContractReportLabels.EXPECTED_INVARIANT, contract.expected());
        StepLogger.pass(AtfContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT_RECORDED);
    }

    public static void expected(ContractDetails contract) {
        if (AtfContractNarrationState.autoExpectedPublished()) {
            return;
        }
        StepLogger.startStep(AtfContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT);
        logDetail(AtfContractReportLabels.AREA, contract.area());
        logDetail(AtfContractReportLabels.OWNER, contract.owner());
        logDetail(AtfContractReportLabels.EXPECTED_INVARIANT, contract.expected());
        StepLogger.pass(AtfContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT_RECORDED);
    }

    public static void observed(String details) {
        AtfContractNarrationState.markManualObservedPublished();
        StepLogger.startStep(AtfContractReportLabels.OBSERVED_FRAMEWORK_RESULT);
        StepLogger.logDetail(details);
        StepLogger.pass(AtfContractReportLabels.OBSERVED_FRAMEWORK_RESULT_RECORDED);
    }

    public static void verdict(String why, String repairHint) {
        AtfContractNarrationState.markManualVerdictPublished();
        StepLogger.startStep(AtfContractReportLabels.COLD_FRAMEWORK_VERDICT);
        logDetail(AtfContractReportLabels.PASS_FAIL, "PASS");
        logDetail(AtfContractReportLabels.WHY, why);
        logDetail(AtfContractReportLabels.REPAIR_HINT_IF_THIS_FAILS, repairHint);
        StepLogger.pass(AtfContractReportLabels.COLD_FRAMEWORK_VERDICT_RECORDED);
    }

    public static void verdict(String why, ContractDetails contract) {
        verdict(why, contract.repairHint());
    }

    private static void logDetail(String label, Object value) {
        StepLogger.logDetail(label + ": " + value);
    }

    public static AtfContract contractFor(Class<?> testClass, String methodName) {
        Class<?> current = testClass;
        while (current != null && current != Object.class) {
            for (java.lang.reflect.Method method : current.getDeclaredMethods()) {
                if (!methodName.equals(method.getName())) {
                    continue;
                }
                AtfContract methodContract = method.getAnnotation(AtfContract.class);
                if (methodContract != null) {
                    return methodContract;
                }
            }

            AtfContract classContract = current.getAnnotation(AtfContract.class);
            if (classContract != null) {
                return classContract;
            }

            current = current.getSuperclass();
        }

        throw new IllegalStateException("Missing @AtfContract on " + testClass.getName() + "#" + methodName);
    }

    public static ContractDetails contractDetailsFor(Class<?> testClass, String methodName) {
        Class<?> current = testClass;
        while (current != null && current != Object.class) {
            for (java.lang.reflect.Method method : current.getDeclaredMethods()) {
                if (!methodName.equals(method.getName())) {
                    continue;
                }
                AtfContract methodContract = method.getAnnotation(AtfContract.class);
                if (methodContract != null) {
                    return ContractDetails.from(methodContract);
                }
                AtfContractRef methodContractRef = method.getAnnotation(AtfContractRef.class);
                if (methodContractRef != null) {
                    return ContractDetails.fromRef(methodContractRef.value());
                }
            }

            AtfContract classContract = current.getAnnotation(AtfContract.class);
            if (classContract != null) {
                return ContractDetails.from(classContract);
            }
            AtfContractRef classContractRef = current.getAnnotation(AtfContractRef.class);
            if (classContractRef != null) {
                return ContractDetails.fromRef(classContractRef.value());
            }

            current = current.getSuperclass();
        }

        throw new IllegalStateException(
                "Missing @AtfContract or @AtfContractRef on " + testClass.getName() + "#" + methodName
        );
    }

    public static SuiteDetails suiteDetailsFor(Class<?> testClass) {
        Class<?> current = testClass;
        while (current != null && current != Object.class) {
            AtfContractSuite suite = current.getAnnotation(AtfContractSuite.class);
            if (suite != null) {
                return SuiteDetails.from(suite);
            }
            AtfContractSuiteRef suiteRef = current.getAnnotation(AtfContractSuiteRef.class);
            if (suiteRef != null) {
                return SuiteDetails.fromRef(suiteRef.value());
            }

            current = current.getSuperclass();
        }

        throw new IllegalStateException(
                "Missing @AtfContractSuite or @AtfContractSuiteRef on " + testClass.getName()
        );
    }

    public static final class ContractDetails {
        private final String area;
        private final String owner;
        private final String expected;
        private final String observed;
        private final String repairHint;

        private ContractDetails(String area, String owner, String expected, String observed, String repairHint) {
            this.area = area;
            this.owner = owner;
            this.expected = expected;
            this.observed = observed;
            this.repairHint = repairHint;
        }

        public String area() {
            return area;
        }

        public String owner() {
            return owner;
        }

        public String expected() {
            return expected;
        }

        public String observed() {
            return observed;
        }

        public String repairHint() {
            return repairHint;
        }

        private static ContractDetails from(AtfContract contract) {
            return new ContractDetails(
                    String.valueOf(contract.area()),
                    contract.owner(),
                    contract.expected(),
                    contract.observed(),
                    contract.repairHint()
            );
        }

        private static ContractDetails fromRef(String id) {
            AtfCoreYamlContractNarrativeSpec spec = AtfCoreYamlContractCatalog.requireContractNarrativeContract(id);
            return new ContractDetails(
                    spec.contractArea(),
                    spec.contractOwner(),
                    spec.contractExpected(),
                    spec.contractObserved(),
                    spec.contractRepairHint()
            );
        }
    }

    public static final class SuiteDetails {
        private final String profile;
        private final String area;
        private final String capability;
        private final String purpose;
        private final String reportHint;

        private SuiteDetails(String profile, String area, String capability, String purpose, String reportHint) {
            this.profile = profile;
            this.area = area;
            this.capability = capability;
            this.purpose = purpose;
            this.reportHint = reportHint;
        }

        public String profile() {
            return profile;
        }

        public String area() {
            return area;
        }

        public String capability() {
            return capability;
        }

        public String purpose() {
            return purpose;
        }

        public String reportHint() {
            return reportHint;
        }

        private static SuiteDetails from(AtfContractSuite suite) {
            return new SuiteDetails(
                    String.valueOf(suite.profile()),
                    String.valueOf(suite.area()),
                    suite.capability(),
                    suite.purpose(),
                    suite.reportHint()
            );
        }

        private static SuiteDetails fromRef(String id) {
            AtfCoreYamlContractNarrativeSpec spec = AtfCoreYamlContractCatalog.requireContractNarrativeSuite(id);
            return new SuiteDetails(
                    spec.suiteProfile(),
                    spec.suiteArea(),
                    spec.suiteCapability(),
                    spec.suitePurpose(),
                    spec.suiteReportHint()
            );
        }
    }
}
