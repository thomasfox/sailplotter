package com.github.thomasfox.sailplotter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.thomasfox.sailplotter.model.vector.CoordinateSystem;

public class Data
{
  private final List<DataPoint> points = new ArrayList<>();

  private File file;

  private transient List<DataPoint> locationPoints;

  private transient List<DataPoint> magneticFieldPoints;

  private transient List<DataPoint> accelerationPoints;

  private transient List<Tack> tackList = new ArrayList<Tack>();

  private transient List<TackSeries> tackSeriesList = new ArrayList<TackSeries>();

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

  @JsonIgnore
  public List<DataPoint> getPointsWithMagneticField()
  {
    if (magneticFieldPoints == null)
    {
      fillMagneticFieldPoints();
    }
    return magneticFieldPoints;
  }

  private void fillMagneticFieldPoints()
  {
    List<DataPoint> magneticFieldPoints = new ArrayList<>();
    for (DataPoint point : points)
    {
      if (point.hasMagneticField())
      {
        magneticFieldPoints.add(point);
      }
    }
    this.magneticFieldPoints = magneticFieldPoints;
  }

  @JsonIgnore
  public List<DataPoint> getPointsWithAcceleration()
  {
    if (accelerationPoints == null)
    {
      fillAccelerationPoints();
    }
    return accelerationPoints;
  }

  public void fillAccelerationPoints()
  {
    List<DataPoint> accelerationPoints = new ArrayList<>();
    for (DataPoint point : points)
    {
      if (point.hasAcceleration())
      {
        accelerationPoints.add(point);
      }
    }
    this.accelerationPoints = accelerationPoints;
  }

  public List<DataPoint> getAllPoints()
  {
    return new ArrayList<>(points);
  }

  private void resetCache()
  {
    locationPoints = null;
    magneticFieldPoints = null;
    accelerationPoints = null;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public List<Tack> getTackList()
  {
    return tackList;
  }

  public List<TackSeries> getTackSeriesList()
  {
    return tackSeriesList;
  }

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }
}
