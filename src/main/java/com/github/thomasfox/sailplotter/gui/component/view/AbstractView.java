package com.github.thomasfox.sailplotter.gui.component.view;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import com.github.thomasfox.sailplotter.gui.component.Layout;
import com.github.thomasfox.sailplotter.listener.DataChangeListener;
import com.github.thomasfox.sailplotter.listener.ZoomChangeListener;

public abstract class AbstractView extends JPanel implements DataChangeListener, ZoomChangeListener
{
  private static final long serialVersionUID = 1L;

  public AbstractView()
  {
    setLayout(new GridBagLayout());
  }

  public Layout createLayout()
  {
    return new Layout(this);
  }
}
