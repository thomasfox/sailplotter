package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;

/**
 * Corrects the data so that GPS time is used as data point time,
 * as long as it differs from the point time
 * (which is device local time in case of saillog data).
 */
public class UseGpsTimeDataCorrector
{
  public void correct(Data data)
  {
    long timeDistanceSum = 0;
    int count = 0;
    List<DataPoint> pointsWithLocation = data.getPointsWithLocation();
    for (DataPoint point : pointsWithLocation)
    {
      if (point.location.satelliteTime != null)
      {
        timeDistanceSum += point.location.satelliteTime - point.time;
        count++;
      }
    }
    if (count > 0 && timeDistanceSum > 0)
    {
      long timeDistance = timeDistanceSum / count;
      for (DataPoint point : data.getAllPoints())
      {
        point.time = point.time + timeDistance;
      }
    }
  }
}
