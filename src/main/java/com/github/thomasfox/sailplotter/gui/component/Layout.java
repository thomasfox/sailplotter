package com.github.thomasfox.sailplotter.gui.component;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JPanel;

public class Layout
{
  private final JPanel parent;

  private final GridBagConstraints gridBagConstraints = new GridBagConstraints();

  public Layout(JPanel parent)
  {
    this.parent = parent;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.weighty = 1;
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

  public void add(Component component)
  {
    parent.add(component, gridBagConstraints);
  }
}