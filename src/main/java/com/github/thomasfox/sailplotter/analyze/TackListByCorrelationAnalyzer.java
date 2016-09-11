package com.github.thomasfox.sailplotter.analyze;

import java.util.ArrayList;
import java.util.List;

import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.PointOfSail;
import com.github.thomasfox.sailplotter.model.Tack;

public class TackListByCorrelationAnalyzer
{
  private static final double OFF_TACK_BEARING = Math.PI / 4; // 45 degrees

  private static final int OFF_TACK_COUNTS_STARTS_NEW = 2;

  public List<Tack> analyze(List<DataPoint> points)
  {
    List<Tack> firstPass = new ArrayList<>();
    Tack currentTack = null;
    int offTackCounter = 0;
    for (int dataPointIndex = 0; dataPointIndex < points.size(); ++dataPointIndex)
    {
      DataPoint point = points.get(dataPointIndex);
      PointOfSail pointOfSail = point.getPointOfSail();
      if (pointOfSail == null)
      {
        if (currentTack == null)
        {
          continue;
        }
        offTackCounter++;
        currentTack.end = point;
        if (offTackCounter < OFF_TACK_COUNTS_STARTS_NEW)
        {
          continue;
        }
        currentTack.end(point, dataPointIndex);
        firstPass.add(currentTack);
        currentTack = null;
        offTackCounter = 0;
      }
      else // if (pointOfSail != null)
      {
        if (currentTack == null)
        {
          currentTack = new Tack();
          currentTack.start(point, dataPointIndex);
          continue;
        }
        currentTack.end(point, dataPointIndex);
        if (point.bearing == null
            || Math.abs(point.bearing - currentTack.getAverageBearingInArcs()) > OFF_TACK_BEARING)
        {
          offTackCounter++;
        }
        else
        {
          offTackCounter = 0;
        }
        currentTack.end(point, dataPointIndex);
        if (offTackCounter >= OFF_TACK_COUNTS_STARTS_NEW)
        {
          firstPass.add(currentTack);
          currentTack = new Tack();
          currentTack.start(point, dataPointIndex);
          offTackCounter = 0;
        }
      }
    }
    // TODO last tack
    return firstPass;
  }
}
