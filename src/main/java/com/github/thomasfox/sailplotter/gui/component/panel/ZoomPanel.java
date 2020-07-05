package com.github.thomasfox.sailplotter.gui.component.panel;

import java.awt.Dimension;
import java.awt.Label;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ZoomPanel extends JPanel implements ChangeListener
{
  private static final long serialVersionUID = 1L;

  private final JSlider startSlider;

  private final JSlider endSlider;

  private final JSlider zoomSlider;

  private final List<ZoomPanelChangeListener> listeners = new ArrayList<>();

  private boolean notifyOff = false;

  private int currentDataSize;

  public ZoomPanel()
  {
    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    Label label = new Label("Start time");
    label.setAlignment(Label.CENTER);
    label.setPreferredSize(new Dimension(0, 15));
    this.add(label);

    startSlider = new JSlider(JSlider.HORIZONTAL, 0, 1, 0);
    startSlider.addChangeListener(this);
    this.add(startSlider);

    label = new Label("End time");
    label.setAlignment(Label.CENTER);
    label.setPreferredSize(new Dimension(0, 15));
    this.add(label);

    endSlider = new JSlider(JSlider.HORIZONTAL, 0, 1, 1);
    endSlider.addChangeListener(this);
    this.add(endSlider);

    label = new Label("Zoom");
    label.setAlignment(Label.CENTER);
    this.add(label);

    zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 1, 1);
    zoomSlider.addChangeListener(this);
    this.add(zoomSlider);
  }

  public void setDataSize(int dataSize)
  {
    if (dataSize != currentDataSize)
    {
      startSlider.setMaximum(dataSize - 1);
      startSlider.setValue(0);
      zoomSlider.setMaximum(dataSize - 1);
      zoomSlider.setValue(dataSize - 1);
      endSlider.setMaximum(dataSize - 1);
      endSlider.setValue(dataSize - 1);
      currentDataSize = dataSize;
    }
  }

  @Override
  public void stateChanged(ChangeEvent e)
  {
    notifyListeners(e);
  }

  public void addListener(ZoomPanelChangeListener listener)
  {
    listeners.add(listener);
  }

  public void removeListener(ZoomPanelChangeListener listener)
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

    ZoomPanelChangeEvent event = new ZoomPanelChangeEvent(startSlider.getValue(), endSlider.getValue(), this);
    listeners.stream().forEach(l -> l.stateChanged(event));
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

  public ZoomPanelChangeEvent getChangeEventFromCurrentData()
  {
    return new ZoomPanelChangeEvent(startSlider.getValue(), endSlider.getValue(), this);
  }

  public void processZoomPanelChangeEvent(ZoomPanelChangeEvent e)
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
