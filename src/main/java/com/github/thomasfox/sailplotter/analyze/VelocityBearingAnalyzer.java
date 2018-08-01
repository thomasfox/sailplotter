package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Wind;

public class VelocityBearingAnalyzer
{
  public void analyze(Data data, double windBearing)
  {
    List<DataPoint> pointsWithLocation = data.getPointsWithLocation();
    for (int i = 1; i < pointsWithLocation.size() - 1; ++i)
    {
      DataPoint point = pointsWithLocation.get(i);
      DataPoint pointBefore = pointsWithLocation.get(i - 1);
      DataPoint pointAfter = pointsWithLocation.get(i + 1);
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
  }
}
