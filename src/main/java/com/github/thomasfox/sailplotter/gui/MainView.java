package com.github.thomasfox.sailplotter.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class MainView extends JPanel
{
  private static final long serialVersionUID = 1L;

  public MainView()
  {
    setLayout(new GridBagLayout());
  }

  public Layout createLayout()
  {
    return new Layout(this);
  }

  public static class Layout
  {
    MainView panel;
    private final GridBagConstraints gridBagConstraints = new GridBagConstraints();

    private Layout(MainView panel)
    {
      this.panel = panel;
      gridBagConstraints.fill = GridBagConstraints.BOTH;
    }

    public Layout withGridx(int x)
    {
      gridBagConstraints.gridx = x;
      return this;
    }

    public Layout withGridy(int y)
    {
      gridBagConstraints.gridy = y;
      return this;
    }

    public Layout withGridxy(int x, int y)
    {
      gridBagConstraints.gridx = x;
      gridBagConstraints.gridy = y;
      return this;
    }

    public Layout withWeightx(double weightx)
    {
      gridBagConstraints.weightx = weightx;
      return this;
    }

    public Layout withWeighty(double weighty)
    {
      gridBagConstraints.weighty = weighty;
      return this;
    }

    public Layout withColumnSpan(int width)
    {
      gridBagConstraints.gridwidth = width;
      return this;
    }

    public Layout withNoFillY()
    {
      gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
      return this;
    }

    public void add(Component cmponent)
    {
      panel.add(cmponent, gridBagConstraints);
    }
  }
}
