package com.dslplatform.ideaplugin;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class DslSettingsPage implements Configurable {
	private JPanel panel;
	private JCheckBox showTooltips;
	private JCheckBox showConcepts;
	private JComboBox<String> shortcutCombo;

	@Nls(capitalization = Nls.Capitalization.Title)
	@Override
	public String getDisplayName() {
		return "DSL Platform";
	}

	@NotNull
	@Override
	public JComponent createComponent() {
		panel = new JPanel(new BorderLayout());
		showTooltips = new JCheckBox("Show grammar tooltip on hover");
		showConcepts = new JCheckBox("Show available concepts");
		shortcutCombo = new JComboBox<String>(DslSettings.SHORTCUT_OPTION_LABELS);
		JPanel shortcutRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		shortcutRow.add(new JLabel("Autocomplete shortcut:"));
		shortcutRow.add(shortcutCombo);
		Box box = Box.createVerticalBox();
		box.add(showTooltips);
		box.add(Box.createVerticalStrut(6));
		box.add(showConcepts);
		box.add(Box.createVerticalStrut(6));
		box.add(shortcutRow);
		panel.add(box, BorderLayout.NORTH);
		return panel;
	}

	@Override
	public boolean isModified() {
		if (showTooltips == null || showConcepts == null) return false;
		return showTooltips.isSelected() != DslSettings.getInstance().isShowTooltips()
			|| showConcepts.isSelected() != DslSettings.getInstance().isShowConcepts()
			|| !selectedShortcutId().equals(DslSettings.getInstance().getAutocompleteShortcut());
	}

	@Override
	public void apply() {
		DslSettings settings = DslSettings.getInstance();
		settings.setShowTooltips(showTooltips.isSelected());
		settings.setShowConcepts(showConcepts.isSelected());
		String shortcut = selectedShortcutId();
		if (shortcut != null) {
			settings.setAutocompleteShortcut(shortcut);
			DslSettings.applyShortcutToActiveKeymap(shortcut);
		}
	}

	private String selectedShortcutId() {
		int idx = shortcutCombo.getSelectedIndex();
		return idx >= 0 ? DslSettings.SHORTCUT_OPTION_IDS[idx] : null;
	}

	@Override
	public void reset() {
		showTooltips.setSelected(DslSettings.getInstance().isShowTooltips());
		showConcepts.setSelected(DslSettings.getInstance().isShowConcepts());
		int idx = -1;
		for (int i = 0; i < DslSettings.SHORTCUT_OPTION_IDS.length; i++) {
			if (DslSettings.SHORTCUT_OPTION_IDS[i].equals(DslSettings.getInstance().getAutocompleteShortcut())) {
				idx = i;
				break;
			}
		}
		shortcutCombo.setSelectedIndex(idx >= 0 ? idx : 0);
	}

	@Override
	public void disposeUIResources() {
		panel = null;
		showTooltips = null;
		showConcepts = null;
		shortcutCombo = null;
	}
}
