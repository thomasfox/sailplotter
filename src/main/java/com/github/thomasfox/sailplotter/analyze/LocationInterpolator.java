package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;

/**
 * Interpolates location data for points which do not have location data.
 * This is done only for points between other points with location data.
 */
public class LocationInterpolator
{
  public void interpolateLocation(Data data)
  {
    List<DataPoint> allPoints = data.getAllPoints();
    Location lastLocation = null;
    Long lastLocationTime = null;
    Location nextLocation = null;
    Long nextLocationTime = null;
    for (int i = 1; i < data.size() - 1; ++i)
    {
      DataPoint point = data.get(i);
      if (point.hasLocation())
      {
        lastLocation = point.location;
        lastLocationTime = point.time;
        DataPoint nextPointWithLocation = getNextPointWithLocation(allPoints, i);
        if (nextPointWithLocation == null)
        {
          break;
        }
        nextLocation = nextPointWithLocation.location;
        nextLocationTime = nextPointWithLocation.time;
        continue;
      }
      if (lastLocation != null)
      {
        point.location = Location.interpolate(lastLocation, nextLocationTime - point.time, nextLocation, point.time - lastLocationTime);
      }
    }
  }

  private DataPoint getNextPointWithLocation(List<DataPoint> points, int fromIndex)
  {
    if (fromIndex >= points.size())
    {
      return null;
    }
    for (int i = fromIndex + 1; i < points.size(); ++i)
    {
      if (points.get(i).hasLocation())
      {
        return points.get(i);
      }
    }
    return null;
  }
}
