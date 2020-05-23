package com.github.thomasfox.sailplotter.gui.component.panel;

public class ZoomPanelChangeEvent
{
  private final int startIndex;

  private final int zoomPosition;

  private final ZoomPanel source;

  ZoomPanelChangeEvent(int startIndex, int zoomPosition, ZoomPanel source)
  {
    this.startIndex = startIndex;
    this.zoomPosition = zoomPosition;
    this.source = source;
  }

  public int getStartIndex()
  {
    return startIndex;
  }

  public int getZoomPosition()
  {
    return zoomPosition;
  }

  public boolean isSource(ZoomPanel panel)
  {
    return this.source == panel;
  }
}
