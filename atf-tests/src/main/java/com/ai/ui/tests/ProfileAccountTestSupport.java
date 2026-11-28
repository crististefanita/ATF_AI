package com.ai.ui.tests;

import com.ai.ui.core.runtime.BaseTestUI;
import com.ai.ui.support.component.ProfileAccountComponent;
import com.ai.ui.support.validation.ProfileAccountValidation;

abstract class ProfileAccountTestSupport extends BaseTestUI {
    protected final ProfileAccountComponent account = new ProfileAccountComponent();
    protected final ProfileAccountValidation verify = new ProfileAccountValidation();
}
