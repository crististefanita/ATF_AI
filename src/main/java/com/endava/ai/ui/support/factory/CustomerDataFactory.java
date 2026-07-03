package com.endava.ai.ui.support.factory;

import com.endava.ai.ui.support.model.CustomerData;
import com.endava.ai.ui.support.utils.DataGenerator;
import com.endava.ai.ui.support.utils.JsonTestData;

public final class CustomerDataFactory {

    private static final String REGISTRATION_DATA = "testdata/registration_positive.json";

    private CustomerDataFactory() {
    }

    public static CustomerData validCustomer() {
        String json = JsonTestData.readResource(REGISTRATION_DATA);

        return new CustomerData.Builder()
                .firstName(JsonTestData.getString(json, "firstName"))
                .lastName(JsonTestData.getString(json, "lastName"))
                .dob(JsonTestData.getString(json, "dob"))
                .country(JsonTestData.getString(json, "country"))
                .postalCode(JsonTestData.getString(json, "postal"))
                .houseNumber(orDefault(JsonTestData.getString(json, "houseNumber"), "1"))
                .street(JsonTestData.getString(json, "street"))
                .city(JsonTestData.getString(json, "city"))
                .state(JsonTestData.getString(json, "state"))
                .phone(JsonTestData.getString(json, "phone"))
                .email(DataGenerator.uniqueEmail())
                .password(DataGenerator.strongPassword())
                .build();
    }

    public static String newStrongPassword() {
        return DataGenerator.strongPassword();
    }

    private static String orDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
