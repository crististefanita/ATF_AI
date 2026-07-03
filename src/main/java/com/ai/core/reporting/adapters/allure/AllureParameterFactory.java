package com.ai.core.reporting.adapters.allure;

import io.qameta.allure.model.Parameter;

public final class AllureParameterFactory {

    private AllureParameterFactory() {
    }

    public static Parameter param(String name, String value) {
        return new Parameter().setName(name).setValue(value);
    }

    public static Parameter fromDetail(String detail) {
        int separator = detail.indexOf('=');
        if (separator <= 0 || separator == detail.length() - 1) {
            return param("detail", detail);
        }

        return param(
                detail.substring(0, separator).trim(),
                detail.substring(separator + 1).trim()
        );
    }
}
