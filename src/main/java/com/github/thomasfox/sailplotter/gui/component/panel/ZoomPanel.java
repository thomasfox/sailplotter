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

import com.github.thomasfox.sailplotter.Constants;

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

    endSlider = new JSlider(JSlider.HORIZONTAL, 0, 1, 0);
    endSlider.addChangeListener(this);
    this.add(endSlider);

    label = new Label("Zoom");
    label.setAlignment(Label.CENTER);
    this.add(label);

    zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, Constants.NUMBER_OF_ZOOM_TICKS, Constants.NUMBER_OF_ZOOM_TICKS);
    zoomSlider.addChangeListener(this);
    this.add(zoomSlider);
  }

  public void setDataSize(int dataSize)
  {
    if (dataSize != currentDataSize)
    {
      startSlider.setValue(0);
      startSlider.setMaximum(dataSize - 1);
      endSlider.setMaximum(dataSize - 1);
      endSlider.setValue(dataSize - 1);
      zoomSlider.setValue(Constants.NUMBER_OF_ZOOM_TICKS);
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
        adjustOtherSlidersToStartSliderChange(startSlider.getValue());
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
        adjustOtherSlidersToZoomSliderChange(zoomSlider.getValue());
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
        ajustOtherSlidersToEndSliderChange(endSlider.getValue());
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
      adjustOtherSlidersToStartSliderChange(startIndex);
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
      ajustOtherSlidersToEndSliderChange(endIndex);
    }
    finally
    {
      this.notifyOff = false;
    }
  }

  public void processZoomPanelChangeEvent(ZoomPanelChangeEvent e)
  {
    if (!e.isSource(this))
    {
      setStartIndex(e.getStartIndex(), false);
      setEndIndex(e.getEndIndex(), false);
    }
  }

  private int getZoomIndexFromOtherSliders()
  {
    if (currentDataSize == 0)
    {
      return Constants.NUMBER_OF_ZOOM_TICKS;
    }
    int zoomIndex = Math.min(
        Math.max(
            Constants.NUMBER_OF_ZOOM_TICKS * (endSlider.getValue() - startSlider.getValue()) / (currentDataSize),
            3),
        Constants.NUMBER_OF_ZOOM_TICKS);
    return zoomIndex;
  }

  private int getEndIndexFromOtherSliders()
  {
    int endIndex = Math.min(
        (zoomSlider.getValue() * currentDataSize / Constants.NUMBER_OF_ZOOM_TICKS) + startSlider.getValue(),
        currentDataSize);
    return endIndex;
  }


  private void adjustOtherSlidersToStartSliderChange(int newStartIndex)
  {
    if (startSlider.getValue() > endSlider.getValue())
    {
      endSlider.setValue(newStartIndex);
    }
    zoomSlider.setValue(getZoomIndexFromOtherSliders());
  }

  private void adjustOtherSlidersToZoomSliderChange(int newZoomIndex)
  {
    endSlider.setValue(getEndIndexFromOtherSliders());
  }

  private void ajustOtherSlidersToEndSliderChange(int newEndIndex)
  {
    zoomSlider.setValue(getZoomIndexFromOtherSliders());
    if (startSlider.getValue() > newEndIndex)
    {
      startSlider.setValue(newEndIndex);
    }
  }
}
