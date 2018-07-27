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
      DataPoint pointBefore = points.get(i - 1);
      DataPoint point = points.get(i);
      DataPoint pointAfter = points.get(i + 1);
      double distance = pointAfter.location.distance(pointBefore.location);
      point.location.velocity = distance / pointAfter.timeDistanceMillis(pointBefore) * 1000 / Constants.NAUTICAL_MILE * 3600d;
      point.location.bearing = pointBefore.getBearingTo(pointAfter);
      point.location.velocityBearingAveragedOverDistance = distance;
      if (point.wind == null)
      {
        point.wind = new Wind();
      }
      point.wind.direction = windBearing;
    }
    return points;
  }
}
