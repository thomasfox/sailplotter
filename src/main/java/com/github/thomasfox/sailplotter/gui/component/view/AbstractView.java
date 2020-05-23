package com.github.thomasfox.sailplotter.gui.component.view;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;

public abstract class AbstractView extends JPanel
{
  private static final long serialVersionUID = 1L;

  public AbstractView()
  {
    setLayout(new GridBagLayout());
  }

  public abstract void alignZoomPanelToChangeEvent(ZoomPanelChangeEvent e);

  public Layout createLayout()
  {
    return new Layout(this);
  }

  public static class Layout
  {
    AbstractView panel;
    private final GridBagConstraints gridBagConstraints = new GridBagConstraints();

    private Layout(AbstractView panel)
    {
      this.panel = panel;
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

    public void add(Component cmponent)
    {
      panel.add(cmponent, gridBagConstraints);
    }
  }
}
