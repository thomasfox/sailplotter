package com.github.thomasfox.sailplotter.gui.component.panel;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.thomasfox.sailplotter.gui.component.Layout;
import com.github.thomasfox.sailplotter.listener.DataChangeListener;
import com.github.thomasfox.sailplotter.model.Data;

public class ZoomPanel extends JPanel implements ChangeListener, DataChangeListener, ZoomChangeListener
{
  private static final long serialVersionUID = 1L;

  private final JSlider startSlider;

  private final JSlider endSlider;

  private final JSlider zoomSlider;

  private final List<ZoomChangeListener> listeners = new ArrayList<>();

  private boolean notifyOff = false;

  private int currentDataSize;

  public ZoomPanel()
  {
    this.setLayout(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    Label label = new Label("Start time");
    label.setAlignment(Label.CENTER);
    label.setPreferredSize(new Dimension(0, 15));
    new Layout(this)
        .withGridy(0)
        .withWeighty(0.16666)
        .add(label);

    startSlider = new JSlider(JSlider.HORIZONTAL, 0, 1, 0);
    startSlider.addChangeListener(this);
    new Layout(this)
        .withGridy(1)
        .withWeighty(0.16666)
        .add(startSlider);

    label = new Label("End time");
    label.setAlignment(Label.CENTER);
    label.setPreferredSize(new Dimension(0, 15));
    new Layout(this)
        .withGridy(2)
        .withWeighty(0.16666)
        .add(label);

    endSlider = new JSlider(JSlider.HORIZONTAL, 0, 1, 1);
    endSlider.addChangeListener(this);
    new Layout(this)
        .withGridy(3)
        .withWeighty(0.16666)
        .add(endSlider);

    label = new Label("Zoom");
    label.setAlignment(Label.CENTER);
    new Layout(this)
        .withGridy(4)
        .withWeighty(0.16666)
        .add(label);

    zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 1, 1);
    zoomSlider.addChangeListener(this);
    new Layout(this)
        .withGridy(5)
        .withWeighty(0.16666)
        .add(zoomSlider);
  }

  @Override
  public void dataChanged(Data data)
  {
    int dataSize = data.getPointsWithLocation().size();
    if (dataSize != currentDataSize)
    {
      try
      {
        this.notifyOff = true;
        startSlider.setMaximum(dataSize - 1);
        startSlider.setValue(0);
        zoomSlider.setMaximum(dataSize - 1);
        zoomSlider.setValue(dataSize - 1);
        endSlider.setMaximum(dataSize - 1);
        endSlider.setValue(dataSize - 1);
        currentDataSize = dataSize;
      }
      finally
      {
        this.notifyOff = false;
      }
    }
  }

  @Override
  public synchronized void stateChanged(ChangeEvent e)
  {
    if (notifyOff)
    {
      return;
    }
    if (e.getSource() == startSlider)
    {
      try
      {
        this.notifyOff = true;
        adjustOtherSlidersToStartSliderChange();
      }
      finally
      {
        this.notifyOff = false;
      }
    }
    if (e.getSource() == zoomSlider)
    {
      try
      {
        this.notifyOff = true;
        adjustOtherSlidersToZoomSliderChange();
      }
      finally
      {
        this.notifyOff = false;
      }
    }
    if (e.getSource() == endSlider)
    {
      try
      {
        this.notifyOff = true;
        ajustOtherSlidersToEndSliderChange();
      }
      finally
      {
        this.notifyOff = false;
      }
    }
    notifyListeners(e);
  }

  public void addListener(ZoomChangeListener listener)
  {
    listeners.add(listener);
  }

  public void removeListener(ZoomChangeListener listener)
  {
    listeners.remove(listener);
  }

  public void clearListeners()
  {
    listeners.clear();
  }

  private synchronized void notifyListeners(ChangeEvent e)
  {
    if (notifyOff)
    {
      return;
    }
    ZoomChangeEvent event = new ZoomChangeEvent(startSlider.getValue(), endSlider.getValue(), this);
    listeners.stream().forEach(l -> l.zoomChanged(event));
  }

  public int getStartIndex()
  {
    return startSlider.getValue();
  }

  public synchronized void setStartIndex(int startIndex, boolean notify)
  {
    try
    {
      if (!notify)
      {
        this.notifyOff = true;
      }
      startSlider.setValue(startIndex);
      this.notifyOff = true;
      adjustOtherSlidersToStartSliderChange();
    }
    finally
    {
      this.notifyOff = false;
    }
  }

  public int getZoomIndex()
  {
    return zoomSlider.getValue();
  }

  public int getEndIndex()
  {
    return endSlider.getValue();
  }

  public synchronized void setEndIndex(int endIndex, boolean notify)
  {
    try
    {
      if (!notify)
      {
        this.notifyOff = true;
      }
      endSlider.setValue(endIndex);
      this.notifyOff = true;
      ajustOtherSlidersToEndSliderChange();
    }
    finally
    {
      this.notifyOff = false;
    }
  }

  public ZoomChangeEvent getChangeEventFromCurrentData()
  {
    return new ZoomChangeEvent(startSlider.getValue(), endSlider.getValue(), this);
  }

  @Override
  public void zoomChanged(ZoomChangeEvent e)
  {
    if (!e.isSource(this))
    {
      setStartIndex(e.getStartIndex(), false);
      setEndIndex(e.getEndIndex(), false);
    }
  }

  private void adjustOtherSlidersToStartSliderChange()
  {
    if (startSlider.getValue() > endSlider.getValue())
    {
      endSlider.setValue(startSlider.getValue());
    }
    zoomSlider.setValue(getZoomIndexFromOtherSliders());
  }

  private void adjustOtherSlidersToZoomSliderChange()
  {
    int newStartEndInterval = zoomSlider.getValue();
    int middleValue = (startSlider.getValue() + endSlider.getValue()) / 2;
    int newStartIndex = middleValue - newStartEndInterval / 2;
    int newEndIndex;
    if (newStartIndex < 0)
    {
      newStartIndex = 0;
      newEndIndex = Math.min(newStartEndInterval, (currentDataSize - 1));
    }
    else
    {
      newEndIndex = newStartIndex + newStartEndInterval;
      if (newEndIndex >= currentDataSize)
      {
        newEndIndex = currentDataSize - 1;
        newStartIndex = Math.max(newEndIndex - newStartEndInterval, 0);
      }
    }
    startSlider.setValue(newStartIndex);
    endSlider.setValue(newEndIndex);
  }

  private void ajustOtherSlidersToEndSliderChange()
  {
    zoomSlider.setValue(getZoomIndexFromOtherSliders());
    if (startSlider.getValue() > endSlider.getValue())
    {
      startSlider.setValue(endSlider.getValue());
    }
  }

  private int getZoomIndexFromOtherSliders()
  {
    return Math.max(endSlider.getValue() - startSlider.getValue(), 0);
  }
}
