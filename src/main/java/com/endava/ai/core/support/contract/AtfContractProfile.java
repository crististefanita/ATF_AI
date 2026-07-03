package com.endava.ai.core.support.contract;

import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractAnnotationSpec;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;

public enum AtfContractProfile {
    CORE_CONTRACT,
    API_BASE_CONTRACT,
    LIVE_API_SMOKE_CONTRACT,
    REPORTING_CONTRACT,
    EXTENT_CONTRACT,
    ALLURE_CONTRACT,
    UI_SUPPORT_CONTRACT,
    UI_PRODUCT_SMOKE_CONTRACT,
    FRAMEWORK_ATLAS_CONTRACT;

    public String recommendedEngine() {
        return spec().recommendedEngine();
    }

    public boolean isPureFramework() {
        return spec().pureFramework();
    }

    public boolean isLiveBoundary() {
        return spec().liveBoundary();
    }

    public boolean isAllureOnly() {
        return "allure".equals(recommendedEngine());
    }

    public boolean isExtentRecommended() {
        return "extent".equals(recommendedEngine());
    }

    private AtfCoreYamlContractAnnotationSpec spec() {
        return AtfCoreYamlContractCatalog.contractAnnotationProfile(name());
    }
}
