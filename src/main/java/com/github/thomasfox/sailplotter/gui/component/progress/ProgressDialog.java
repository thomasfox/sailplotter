package com.github.thomasfox.sailplotter.gui.component.progress;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

/**
 * A GUI Dialog which displays the progress of a work item.
 */
public class ProgressDialog extends JDialog implements ProgressChanged
{
  private static final int HEIGHT_WITHOUT_WARNINGS = 80;
  private static final int HEIGHT_WITH_WARNINGS = 250;
  private final static int WIDTH = 500;
  private static final long serialVersionUID = 1L;

  private final JProgressBar progressBar = new JProgressBar();

  private final JLabel label = new JLabel();

  private final JLabel warnings = new JLabel();

  JScrollPane scrollPane = new JScrollPane(warnings);

  public ProgressDialog(JFrame parentFrame)
  {
    super(parentFrame, false);
    add(progressBar, BorderLayout.CENTER);
    add(label, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.SOUTH);
    resetLayout();
    setLocationRelativeTo(parentFrame);
  }

  private void resetLayout()
  {
    label.setPreferredSize(new Dimension(WIDTH, 20));
    progressBar.setPreferredSize(new Dimension(WIDTH, 20));
    scrollPane.setPreferredSize(new Dimension(WIDTH, 200));
    scrollPane.setVisible(false);
    warnings.setText(null);
    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    setSize(WIDTH, HEIGHT_WITHOUT_WARNINGS);
  }

  @Override
  public void start(String headline)
  {
    setTitle(headline);
    resetLayout();
    label.setVisible(true);
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);

    setVisible(true);
  }

  @Override
  public void setToDisplay(String toDisplay)
  {
    label.setText(toDisplay);
  }

  @Override
  public void setWarnings(List<String> warningList)
  {
    String warningsText = String.join("\n", warningList);
    warnings.setText(warningsText);
    warnings.setVisible(true);
    scrollPane.setVisible(true);
    setSize(WIDTH, HEIGHT_WITH_WARNINGS);
  }

  @Override
  public void finished()
  {
    progressBar.setVisible(false);
    if (warnings.getText() == null || "".equals(warnings.getText()))
    {
      warnings.setVisible(false);
      setVisible(false);
    }
  }
}
