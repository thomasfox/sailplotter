package com.github.thomasfox.sailplotter.gui.component.panel;

public class ZoomPanelChangeEvent
{
  private final int startIndex;

  private final int endIndex;

  private final ZoomPanel source;

  ZoomPanelChangeEvent(int startIndex, int endIndex, ZoomPanel source)
  {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.source = source;
  }

  public int getStartIndex()
  {
    return startIndex;
  }

  public int getEndIndex()
  {
    return endIndex;
  }

  public boolean isSource(ZoomPanel panel)
  {
    return this.source == panel;
  }
}
