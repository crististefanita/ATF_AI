package com.endava.ai.ui.tests;

import com.endava.ai.ui.core.runtime.BaseTestUI;
import com.endava.ai.ui.support.component.ProfileAccountComponent;
import com.endava.ai.ui.support.validation.ProfileAccountValidation;

abstract class ProfileAccountTestSupport extends BaseTestUI {
    protected final ProfileAccountComponent account = new ProfileAccountComponent();
    protected final ProfileAccountValidation verify = new ProfileAccountValidation();
}
