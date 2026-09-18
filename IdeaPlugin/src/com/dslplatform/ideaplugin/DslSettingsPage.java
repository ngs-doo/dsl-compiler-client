package com.dslplatform.ideaplugin;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class DslSettingsPage implements Configurable {
	private JPanel panel;
	private JCheckBox showTooltips;

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
		panel.add(showTooltips, BorderLayout.NORTH);
		return panel;
	}

	@Override
	public boolean isModified() {
		return showTooltips != null && showTooltips.isSelected() != DslSettings.getInstance().isShowTooltips();
	}

	@Override
	public void apply() {
		DslSettings.getInstance().setShowTooltips(showTooltips.isSelected());
	}

	@Override
	public void reset() {
		showTooltips.setSelected(DslSettings.getInstance().isShowTooltips());
	}

	@Override
	public void disposeUIResources() {
		panel = null;
		showTooltips = null;
	}
}
