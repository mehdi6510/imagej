//
// ResolveDependencies.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.updater.gui;

import imagej.updater.core.Conflicts;
import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.Conflicts.Resolution;
import imagej.updater.core.FilesCollection;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

@SuppressWarnings("serial")
public class ResolveDependencies extends JDialog implements ActionListener {

	protected UpdaterFrame updaterFrame;
	protected JPanel rootPanel;
	public JTextPane panel; // this is public for debugging purposes
	protected SimpleAttributeSet bold, indented, italic, normal, red;
	protected JButton ok, cancel;

	protected Conflicts conflicts;
	protected boolean forUpload, wasCanceled;

	public ResolveDependencies(final UpdaterFrame owner,
		final FilesCollection files)
	{
		this(owner, files, false);
	}

	public ResolveDependencies(final UpdaterFrame owner,
		final FilesCollection files, final boolean forUpload)
	{
		super(owner, "Resolve dependencies");

		updaterFrame = owner;
		this.forUpload = forUpload;
		conflicts = new Conflicts(files);

		rootPanel = SwingTools.verticalPanel();
		setContentPane(rootPanel);

		panel = new JTextPane();
		panel.setEditable(false);

		bold = new SimpleAttributeSet();
		StyleConstants.setBold(bold, true);
		StyleConstants.setFontSize(bold, 16);
		indented = new SimpleAttributeSet();
		StyleConstants.setLeftIndent(indented, 40);
		italic = new SimpleAttributeSet();
		StyleConstants.setItalic(italic, true);
		normal = new SimpleAttributeSet();
		red = new SimpleAttributeSet();
		StyleConstants.setForeground(red, Color.RED);

		SwingTools.scrollPane(panel, 450, 350, rootPanel);

		final JPanel buttons = new JPanel();
		ok = SwingTools.button("OK", "OK", this, buttons);
		cancel = SwingTools.button("Cancel", "Cancel", this, buttons);
		rootPanel.add(buttons);

		// do not show, right now
		pack();
		setModal(true);
		setLocationRelativeTo(owner);

		final int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		SwingTools.addAccelerator(cancel, rootPanel, this, KeyEvent.VK_ESCAPE, 0);
		SwingTools.addAccelerator(cancel, rootPanel, this, KeyEvent.VK_W, ctrl);
		SwingTools.addAccelerator(ok, rootPanel, this, KeyEvent.VK_ENTER, 0);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == cancel) {
			wasCanceled = true;
			dispose();
		}
		else if (e.getSource() == ok) {
			if (!ok.isEnabled()) return;
			dispose();
		}
	}

	public boolean resolve() {
		listIssues();

		if (panel.getDocument().getLength() > 0) setVisible(true);
		return !wasCanceled;
	}

	protected void listIssues() {
		panel.setText("");

		int count = 0;
		for (final Conflict conflict : conflicts.getConflicts(forUpload)) {
			count++;
			maybeAddSeparator();
			newText(conflict.isError() ? "Conflict: " : "Warning: ", conflict
				.isError() ||
				conflict.isCritical() ? red : normal);
			addText(conflict.getFilename(), bold);
			addText(conflict.getConflict());
			addText("\n    ");
			for (final Resolution resolution : conflict.getResolutions()) {
				addButton(resolution.getDescription(), new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						resolution.resolve();
						listIssues();
					}
				});
			}
		}

		ok.setEnabled(count == 0);
		if (ok.isEnabled()) ok.requestFocus();

		if (isShowing()) {
			if (panel.getStyledDocument().getLength() == 0) addText(
				"No more issues to be resolved!", italic);
			panel.setCaretPosition(0);
			panel.repaint();
		}
	}

	protected void addButton(final String label, final ActionListener listener) {
		final JButton button = SwingTools.button(label, null, listener, null);
		selectEnd();
		panel.insertComponent(button);
	}

	protected void selectEnd() {
		final int end = panel.getStyledDocument().getLength();
		panel.select(end, end);
	}

	protected void newText(final String message) {
		newText(message, normal);
	}

	protected void newText(final String message, final SimpleAttributeSet style) {
		if (panel.getStyledDocument().getLength() > 0) addText("\n\n");
		addText(message, style);
	}

	protected void addText(final String message) {
		addText(message, normal);
	}

	protected void addText(final String message, final SimpleAttributeSet style) {
		final int end = panel.getStyledDocument().getLength();
		try {
			panel.getStyledDocument().insertString(end, message, style);
		}
		catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	protected void maybeAddSeparator() {
		if (panel.getText().equals("") && panel.getComponents().length == 0) return;
		addText("\n");
		selectEnd();
		panel.insertComponent(new JSeparator());
	}

}
