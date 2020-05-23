package com.github.thomasfox.sailplotter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.thomasfox.sailplotter.model.vector.CoordinateSystem;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class Data
{
  private final List<DataPoint> points = new ArrayList<>();

  /** The file where the data was loaded from.
   */
  private File file;

  /**
   * The direction in arcs where the wind, on average, came from
   * within the time range of the recorded data.
   */
  private double averageWindBearing;

  /**
   * The coordinate system of the boat (front, right, up) in device coordinate system.
   */
  private CoordinateSystem boatCoordinatesInDeviceCoordinates;

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
    if (size() > 0 && point.time != null && getLast().time != null && getLast().time > point.time)
    {
      throw new IllegalArgumentException(" points must be ordered in time. "
          + "Newly added point has time " + point.time
          + " while last point in list has time " + getLast().time);
    }
    points.add(new DataPoint(point));
    resetCache();
  }

  public void add(int position, DataPoint point)
  {
    points.add(position, new DataPoint(point));
    resetCache();
  }

  /**
   * Returns the data point at a certain index.
   * If the data point is modified, resetCache() should be called on this object.
   *
   * @param index the data point index
   * @return the data point at the index
   * @throws IndexOutOfBoundsException if the index is out of range
   *         ({@code index < 0 || index >= size()})
   */
  public DataPoint get(int index)
  {
    return points.get(index);
  }

  /**
   * Returns the last data point.
   *
   * @return the last data point.
   * @throws IndexOutOfBoundsException if no data points are available.
   */
  public DataPoint getLast()
  {
    return points.get(points.size() - 1);
  }

  public int size()
  {
    return points.size();
  }

  /**
   * Returns all data points.
   * The points are expected to be ordered in time, though this is not fully enforced.
   * If a data point is modified, resetCache() should be called on this object.
   *
   * @return all data points, never null, may be empty.
   */
  public List<DataPoint> getAllPoints()
  {
    return new ArrayList<>(points);
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

  public void resetCache()
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

  public CoordinateSystem getBoatCoordinatesInDeviceCoordinates()
  {
    return boatCoordinatesInDeviceCoordinates;
  }

  public void setBoatCoordinatesInDeviceCoordinates(
      CoordinateSystem boatCoordinatesInDeviceCoordinates)
  {
    this.boatCoordinatesInDeviceCoordinates = boatCoordinatesInDeviceCoordinates;
  }

  public double getAverageWindBearing()
  {
    return averageWindBearing;
  }

  public void setAverageWindBearing(double averageWindBearing)
  {
    this.averageWindBearing = averageWindBearing;
  }

  public int getAverageWindDirectionInDegrees()
  {
    return (int) (this.averageWindBearing / 2d / Math.PI * 360d);
  }

  public Long getStartTime()
  {
    if (points.isEmpty())
    {
      return null;
    }
    return points.get(0).time;
  }

  public Long getEndTime()
  {
    if (points.isEmpty())
    {
      return null;
    }
    return points.get(points.size() - 1).time;
  }

  public Long getLocationStartTime()
  {
    if (getPointsWithLocation().isEmpty())
    {
      return null;
    }
    return locationPoints.get(0).time;
  }

  public Long getLocationEndTime()
  {
    if (getPointsWithLocation().isEmpty())
    {
      return null;
    }
    return locationPoints.get(locationPoints.size() - 1).time;
  }

  public Long getMagneticFieldStartTime()
  {
    if (getPointsWithMagneticField().isEmpty())
    {
      return null;
    }
    return magneticFieldPoints.get(0).time;
  }

  public Long getMagneticFieldEndTime()
  {
    if (getPointsWithMagneticField().isEmpty())
    {
      return null;
    }
    return magneticFieldPoints.get(magneticFieldPoints.size() - 1).time;
  }

  public Long getAccelerationStartTime()
  {
    if (getPointsWithAcceleration().isEmpty())
    {
      return null;
    }
    return accelerationPoints.get(0).time;
  }

  public Long getAccelerationEndTime()
  {
    if (getPointsWithAcceleration().isEmpty())
    {
      return null;
    }
    return accelerationPoints.get(accelerationPoints.size() - 1).time;
  }

  public double getAverageLocationPointFrequency()
  {
    if (getPointsWithLocation().isEmpty())
    {
      return 0;
    }
    long timespan = getLocationEndTime() - getLocationStartTime();
    if (timespan == 0)
    {
      return Double.NaN;
    }
    return 1000d * getPointsWithLocation().size() / timespan;
  }

  public double getAverageMagneticFieldPointFrequency()
  {
    if (getPointsWithMagneticField().isEmpty())
    {
      return 0;
    }
    long timespan = getMagneticFieldEndTime() - getMagneticFieldStartTime();
    if (timespan == 0)
    {
      return Double.NaN;
    }
    return 1000d * getPointsWithMagneticField().size() / timespan;
  }

  public double getAverageAccelerationPointFrequency()
  {
    if (getPointsWithAcceleration().isEmpty())
    {
      return 0;
    }
    long timespan = getAccelerationEndTime() - getAccelerationStartTime();
    if (timespan == 0)
    {
      return Double.NaN;
    }
    return 1000d * getPointsWithAcceleration().size() / timespan;
  }

  public ThreeDimVector getAverageAcceleration()
  {
    ThreeDimVector averageAcceleration = new ThreeDimVector(0d, 0d, 0d);
    int accelerationCount = 0;

    for (DataPoint point : getPointsWithAcceleration())
    {
      averageAcceleration.add(point.acceleration);
      accelerationCount++;
    }
    if (accelerationCount == 0)
    {
      return null;
    }
    averageAcceleration = averageAcceleration.multiplyBy(1d / accelerationCount);
    return averageAcceleration;
  }

}
