package com.github.thomasfox.sailplotter.model;

import java.util.ArrayList;
import java.util.List;

public class Data
{
  private final List<DataPoint> points = new ArrayList<>();

  private transient List<DataPoint> locationPoints;

  public void add(DataPoint point)
  {
    points.add(new DataPoint(point));
    resetCache();
  }

  public List<DataPoint> getPointsWithLocation()
  {
    if (locationPoints == null)
    {
      fillLocationPoints();
    }
    return locationPoints;
  }

  public List<DataPoint> getAllPoints()
  {
    return new ArrayList<>(points);
  }


  private void fillLocationPoints()
  {
    List<DataPoint> locationPoints = new ArrayList<>();
    for (DataPoint point : points)
    {
      if (point.hasLocation())
      {
        locationPoints.add(point);
      }
    }
    this.locationPoints = locationPoints;
  }

  private void resetCache()
  {
    locationPoints = null;
  }
}
