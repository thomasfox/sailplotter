package com.github.thomasfox.sailplotter.model;

import java.util.ArrayList;

public class TackList extends ArrayList<Tack>
{
  private static final long serialVersionUID = 1L;

  public Integer getTackIndex(int dataPointIndex)
  {
    int tackIndex = 0;
    for (Tack tack : this)
    {
      if (tack.startOfTackDataPointIndex <= dataPointIndex
          && tack.endOfTackDataPointIndex >= dataPointIndex)
      {
        return tackIndex;
      }
      tackIndex++;
    }
    return null;
  }
}
