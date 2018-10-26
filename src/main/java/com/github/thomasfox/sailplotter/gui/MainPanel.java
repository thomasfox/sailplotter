package com.github.thomasfox.sailplotter.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class MainPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  public MainPanel()
  {
    setLayout(new GridBagLayout());
  }

  public Layout layoutForAdding()
  {
    return new Layout(this);
  }

  public static class Layout
  {
    MainPanel panel;
    private final GridBagConstraints gridBagConstraints = new GridBagConstraints();

    private Layout(MainPanel panel)
    {
      this.panel = panel;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
    }

    public Layout gridx(int x)
    {
      gridBagConstraints.gridx = x;
      return this;
    }

    public Layout gridy(int y)
    {
      gridBagConstraints.gridy = y;
      return this;
    }

    public Layout weightx(double weightx)
    {
      gridBagConstraints.weightx = weightx;
      return this;
    }

    public Layout weighty(double weighty)
    {
      gridBagConstraints.weighty = weighty;
      return this;
    }

    public Layout columnSpan(int width)
    {
      gridBagConstraints.gridwidth = width;
      return this;
    }


    public void add(Component cmponent)
    {
      panel.add(cmponent, gridBagConstraints);
    }
  }
}
