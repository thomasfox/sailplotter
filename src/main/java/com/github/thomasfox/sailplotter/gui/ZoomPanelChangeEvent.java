package com.github.thomasfox.sailplotter.gui;

public class ZoomPanelChangeEvent
{
  private final int startIndex;

  private final int zoomPosition;

  ZoomPanelChangeEvent(int startIndex, int zoomPosition)
  {
    this.startIndex = startIndex;
    this.zoomPosition = zoomPosition;
  }

  public int getStartIndex()
  {
    return startIndex;
  }

  public int getZoomPosition()
  {
    return zoomPosition;
  }
}
