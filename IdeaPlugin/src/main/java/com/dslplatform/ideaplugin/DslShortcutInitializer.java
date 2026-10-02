package com.dslplatform.ideaplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class DslShortcutInitializer implements StartupActivity {
	@Override
	public void runActivity(@NotNull Project project) {
		DslSettings.applyShortcutToActiveKeymap(DslSettings.getInstance().getAutocompleteShortcut());
	}
}
