package com.dslplatform.ideaplugin;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@State(name = "DslSettings", storages = @Storage("dsl-platform.xml"))
public final class DslSettings implements PersistentStateComponent<DslSettings> {

	private boolean showTooltips = true;
	private boolean showConcepts = true;
	private String autocompleteShortcut = SystemInfo.isMac ? "alt_space" : "ctrl_space";

	public static final String ACTION_ID = "dsl.ListAvailableConcepts";
	public static final String[] SHORTCUT_OPTION_IDS = {"alt_space", "ctrl_space", "ctrl_shift_space", "alt_shift_space", "custom"};
	public static final String[] SHORTCUT_OPTION_LABELS = {"Option / Alt + Space", "Ctrl + Space", "Ctrl + Shift + Space", "Alt / Option + Shift + Space", "Custom (set in Keymap settings)"};

	public static DslSettings getInstance() {
		return ApplicationManager.getApplication().getService(DslSettings.class);
	}

	public boolean isShowTooltips() {
		return showTooltips;
	}

	public void setShowTooltips(boolean showTooltips) {
		this.showTooltips = showTooltips;
	}

	public boolean isShowConcepts() {
		return showConcepts;
	}

	public void setShowConcepts(boolean showConcepts) {
		this.showConcepts = showConcepts;
	}

	public String getAutocompleteShortcut() {
		return autocompleteShortcut;
	}

	public void setAutocompleteShortcut(String autocompleteShortcut) {
		this.autocompleteShortcut = autocompleteShortcut;
	}

	public static void applyShortcutToActiveKeymap(String option) {
		final int modifiers = modifiersFor(option);
		if (modifiers < 0) return;
		final Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
		for (Shortcut existing : keymap.getShortcuts(ACTION_ID)) {
			keymap.removeShortcut(ACTION_ID, existing);
		}
		try {
			keymap.addShortcut(ACTION_ID, new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, modifiers), null));
		} catch (Exception e) {
			Logger.getInstance("DSL Platform").warn("Unable to assign DSL completion shortcut: " + e.getMessage());
		}
	}

	private static int modifiersFor(String option) {
		if ("alt_space".equals(option)) return InputEvent.ALT_DOWN_MASK;
		if ("ctrl_space".equals(option)) return InputEvent.CTRL_DOWN_MASK;
		if ("ctrl_shift_space".equals(option)) return InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
		if ("alt_shift_space".equals(option)) return InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
		return -1;
	}

	@NotNull
	@Override
	public DslSettings getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull DslSettings state) {
		showTooltips = state.showTooltips;
		showConcepts = state.showConcepts;
		autocompleteShortcut = state.autocompleteShortcut;
	}
}
