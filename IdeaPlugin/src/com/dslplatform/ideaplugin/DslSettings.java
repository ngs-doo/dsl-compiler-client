package com.dslplatform.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(name = "DslSettings", storages = @Storage("dsl-platform.xml"))
public final class DslSettings implements PersistentStateComponent<DslSettings> {

	private boolean showTooltips = true;

	public static DslSettings getInstance() {
		return ApplicationManager.getApplication().getService(DslSettings.class);
	}

	public boolean isShowTooltips() {
		return showTooltips;
	}

	public void setShowTooltips(boolean showTooltips) {
		this.showTooltips = showTooltips;
	}

	@NotNull
	@Override
	public DslSettings getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull DslSettings state) {
		showTooltips = state.showTooltips;
	}
}
