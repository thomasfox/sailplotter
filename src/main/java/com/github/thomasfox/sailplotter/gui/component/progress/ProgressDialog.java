package com.github.thomasfox.sailplotter.gui.component.progress;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * A GUI Dialog which displays the progress of a work item.
 */
public class ProgressDialog extends JDialog implements ProgressChanged
{
  private static final long serialVersionUID = 1L;

  private final JProgressBar progressBar = new JProgressBar();

  private final JLabel label = new JLabel();

  public ProgressDialog(JFrame parentFrame)
  {
    super(parentFrame, false);
    add(progressBar, BorderLayout.CENTER);
    add(label, BorderLayout.NORTH);
    label.setPreferredSize(new Dimension(300, 20));
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    setSize(300, 70);
    setLocationRelativeTo(parentFrame);
  }

  @Override
  public void start(String headline)
  {
    setTitle(headline);
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
  public void finished()
  {
    progressBar.setVisible(false);
    setVisible(false);
  }
}
