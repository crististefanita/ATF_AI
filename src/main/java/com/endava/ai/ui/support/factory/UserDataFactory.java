package com.endava.ai.ui.support.factory;

import com.endava.ai.ui.support.model.RegistrationData;
import com.endava.ai.ui.support.utils.DataGenerator;
import com.endava.ai.ui.support.utils.JsonTestData;

public final class UserDataFactory {

    private UserDataFactory() {
    }

    public static RegistrationData validRegistrationData() {
        String json = JsonTestData.readResource("testdata/registration_positive.json");

        return RegistrationData.builder()
                .firstName(JsonTestData.getString(json, "firstName"))
                .lastName(JsonTestData.getString(json, "lastName"))
                .dob(JsonTestData.getString(json, "dob"))
                .street(JsonTestData.getString(json, "street"))
                .postal(JsonTestData.getString(json, "postal"))
                .houseNumber(JsonTestData.getString(json, "houseNumber"))
                .city(JsonTestData.getString(json, "city"))
                .state(JsonTestData.getString(json, "state"))
                .country(JsonTestData.getString(json, "country"))
                .phone(JsonTestData.getString(json, "phone"))
                .email(DataGenerator.uniqueEmail())
                .password(DataGenerator.strongPassword())
                .build();
    }

    public static String strongPassword() {
        return DataGenerator.strongPassword();
    }
}
