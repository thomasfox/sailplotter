package com.github.thomasfox.sailplotter.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.thomasfox.sailplotter.model.vector.CoordinateSystem;

public class Data
{
  private final List<DataPoint> points = new ArrayList<>();

  private transient List<DataPoint> locationPoints;

  private transient List<Tack> tackList = new ArrayList<Tack>();

  /**
   * Coordinate System of the boat main axes (front, right, down)
   * in the coordinate system of the measuring device.
   */
  @JsonInclude(Include.NON_NULL)
  public CoordinateSystem deviceOrientation = null;

  /**
   * Textual comment on the data set.
   */
  public String comment;

  public void add(DataPoint point)
  {
    points.add(new DataPoint(point));
    resetCache();
  }

  @JsonIgnore
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

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public List<Tack> getTackList()
  {
    return tackList;
  }
}
