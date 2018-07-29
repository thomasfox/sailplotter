package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Wind;

public class VelocityBearingAnalyzer
{
  public List<DataPoint> analyze(List<DataPoint> points, double windBearing)
  {
    for (int i = 1; i < points.size() - 1; ++i)
    {
      DataPoint pointBefore = getPointWithLocationBefore(points, i);
      DataPoint point = points.get(i);
      DataPoint pointAfter = getPointWithLocationAfter(points, i);
      double distance = pointAfter.location.distance(pointBefore.location);
      point.location.velocityFromLatLong = distance / pointAfter.timeDistanceMillis(pointBefore) * 1000 / Constants.NAUTICAL_MILE * 3600d;
      point.location.bearingFromLatLong = pointBefore.getBearingTo(pointAfter);
      point.location.velocityBearingAveragedOverDistance = distance;
      if (point.wind == null)
      {
        point.wind = new Wind();
      }
      point.wind.direction = windBearing;
    }
    return points;
  }

  public DataPoint getPointWithLocationBefore(List<DataPoint> points, int index)
  {
    for (int i = index - 1; i >= 0; i--)
    {
      DataPoint candidate = points.get(i);
      if (candidate.hasLocation())
      {
        return candidate;
      }
    }
    return null;
  }

  public DataPoint getPointWithLocationAfter(List<DataPoint> points, int index)
  {
    for (int i = index + 1; i < points.size(); i++)
    {
      DataPoint candidate = points.get(i);
      if (candidate.hasLocation())
      {
        return candidate;
      }
    }
    return null;
  }
}
