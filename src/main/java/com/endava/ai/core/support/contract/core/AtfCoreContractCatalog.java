package com.endava.ai.core.support.contract.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class AtfCoreContractCatalog {
    private AtfCoreContractCatalog() {
    }

    public static List<AtfCoreContractSpec> contractsFor(Class<?> testClass) {
        List<AtfCoreContractSpec> contracts = new ArrayList<>();
        AtfCoreContract classContract = testClass.getAnnotation(AtfCoreContract.class);
        if (classContract != null) {
            contracts.add(new AtfCoreContractSpec(classContract));
        }
        for (Method method : testClass.getDeclaredMethods()) {
            AtfCoreContract methodContract = method.getAnnotation(AtfCoreContract.class);
            if (methodContract != null) {
                contracts.add(new AtfCoreContractSpec(methodContract));
            }
        }
        return List.copyOf(contracts);
    }

    public static List<AtfCoreContractEvidence> evidenceFor(Class<?> testClass) {
        List<AtfCoreContractEvidence> evidence = new ArrayList<>();
        AtfCoreCheck classCheck = testClass.getAnnotation(AtfCoreCheck.class);
        if (classCheck != null) {
            evidence.add(new AtfCoreContractEvidence(classCheck));
        }
        for (Method method : testClass.getDeclaredMethods()) {
            AtfCoreCheck methodCheck = method.getAnnotation(AtfCoreCheck.class);
            if (methodCheck != null) {
                evidence.add(new AtfCoreContractEvidence(methodCheck));
            }
        }
        return List.copyOf(evidence);
    }
}
