package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.vector.CoordinateSystem;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

/**
 * This analyzer assumes that the device is fixed in an arbitrary orientation on the boat.
 * It then finds the boat directions (front, right, down)
 * in the device's coordinate system in the following steps:
 * <ul>
 *   <li>
 *     The up direction at any point is determined by getting the acceleration
 *     of the device.
 *     By determining the mean acceleration, we will get the mean direction of gravity up.
 *     IF the boat is not heeled or rolled on average, this will also be the
 *     the up direction of the boat.
 *   </li>
 *   <li>
 *     The front direction is determined as follows:
 *     As an approximation, we assume that the boat is moving on average forward
 *     (may not be exactly true due to current and leeway).
 *     The forward direction at one point relative to the earth
 *     can be determined by the gps-based absolute bearing.
 *     A fixed direction on the earth coordinate system
 *     (probably close to north, but that is not important)
 *     is obtained by measuring the horizontal component
 *     of the magnetic field in the device's coordinate system.
 *     Thus the difference between compass bearing and gps-based
 *     bearing should be fixed.
 *     <br>
 *     This difference is determined by obtaining the maximum difference count
 *     in a histogram of angle differences.
 *     Adding this difference to the compass bearing,
 *     we get the approximate front direction of the boat
 *     for every point where we have a compass bearing
 *   </li>
 *   <li>
 *     Boat right is then the cross product between down and front.
 *   </li>
 * </ul>
 *
 */
public class DeviceOrientationAnalyzer
{
  private static final int HISTOGRAM_SIZE = 360;

  private static final int MAX_ACCELERATION_DISTANCE_MILLIS = 1000;

  public Data analyze(Data data, LoadProgress loadProgress)
  {
    loadProgress.startAnalyzeOrientationCalculateHorizontalCoordinateSystem();
    // calculate a horizontal Coordinate system in device coordinates from average acceleration
    CoordinateSystem approximateHorizontalCoordinateSystem
        = getHorizontalCoordinateSystemFromAverageAcceleration(data);

    if (approximateHorizontalCoordinateSystem == null)
    {
      return data;
    }

    // in the horizontal plane, calculate the compass direction with respect to the horizontal
    // coordinate system, and save angle as compass bearing
    loadProgress.startAnalyzeOrientationSetCompassBearings();
    setCompassBearings(data, approximateHorizontalCoordinateSystem);

    // To find where front is in the horizontal coordinate system in boat coordinates:
    // Create a histogram for the angle betweeen gps direction and "compass north" direction
    // and take the bucket with the most occurences as angle between x direction and boat front
    loadProgress.startAnalyzeOrientationGetCompassToGpsAngle();
    Double maxOccurenceOfRelativeBearingInArcs
        = getMaximumOccurenceOfRelativeBearingOfCompassToGpsInArcs(data.getAllPoints());

    if (maxOccurenceOfRelativeBearingInArcs != null)
    {
      approximateHorizontalCoordinateSystem
          = approximateHorizontalCoordinateSystem.getRotatedAroundZ(maxOccurenceOfRelativeBearingInArcs);
      data.setBoatCoordinatesInDeviceCoordinates(approximateHorizontalCoordinateSystem);
      loadProgress.startAnalyzeOrientationSetCompassBearings();
      setCompassBearings(data, approximateHorizontalCoordinateSystem);
      loadProgress.startAnalyzeOrientationSetHeelAndRoll();
      setHeelAndRoll(data.getAllPoints(), approximateHorizontalCoordinateSystem);
    }
    return data;
  }

  /**
   * Returns a horizontal coordinate system in the device coordinate system.
   * Coordinate system x is approximately device x,
   * coordinate system y is approximately device y,
   * and coordinate system z is up, determined from mean device acceleration.
   *
   * @param data the data to analyze, not null.
   *
   * @return a horizontal coordinate system, or null, if no up direction could be determined.
   */
  CoordinateSystem getHorizontalCoordinateSystemFromAverageAcceleration(Data data)
  {
    CoordinateSystem horizontalCoordinateSystem = new CoordinateSystem();

    // assuming average is upright position of boat
    ThreeDimVector uprightAcceleration = data.getAverageAcceleration();

    if (uprightAcceleration == null || uprightAcceleration.length() < 1)
    {
      // should be close to 10. Less than 1 means we have no data or we have no defined upright position
      return null;
    }

    horizontalCoordinateSystem.z = uprightAcceleration.normalize();
    if (Math.abs(uprightAcceleration.x) > Math.abs(uprightAcceleration.y))
    {
      horizontalCoordinateSystem.x = new ThreeDimVector(uprightAcceleration.z, 0d, -uprightAcceleration.x).normalize();
      horizontalCoordinateSystem.y = horizontalCoordinateSystem.z.crossProduct(horizontalCoordinateSystem.x).normalize();
    }
    else
    {
      horizontalCoordinateSystem.y = new ThreeDimVector(0d, uprightAcceleration.z, -uprightAcceleration.y).normalize();
      horizontalCoordinateSystem.x = horizontalCoordinateSystem.y.crossProduct(horizontalCoordinateSystem.z).normalize();
    }
    return horizontalCoordinateSystem;
  }

  /**
   * Sets the compass bearings for the points with magnetic field measurements.
   *
   * @param data the data to set compass bearings for, non null.
   * @param approximateHorizontalCoordinateSystem A coordinate systems which is fixed at the device
   *        and which z axis is approximately up in neutral position of the ship, not null.
   */
  private void setCompassBearings(
      Data data,
      CoordinateSystem approximateHorizontalCoordinateSystem)
  {
    for (int i = 1; i < data.size() - 1; ++i)
    {
      DataPoint point = data.get(i);
      if (point.hasMagneticField())
      {
        // get the projection of the device-fixed coordinate system on a coordinate system which is truly
        // horizontal.
        ThreeDimVector up = getAccelerationAt(i, data);
        if (up == null)
        {
          continue;
        }
        up = up.normalize();
        ThreeDimVector horizontalX = approximateHorizontalCoordinateSystem.x;
        ThreeDimVector horizontalY = up.crossProduct(horizontalX).normalize();
        horizontalX = horizontalY.crossProduct(up).normalize();
        CoordinateSystem trulyHorizontalCoordinateSystem = new CoordinateSystem(horizontalX, horizontalY, up);
        TwoDimVector horizontalMagneticField = new TwoDimVector(
            trulyHorizontalCoordinateSystem.x.scalarProduct(point.magneticField),
            trulyHorizontalCoordinateSystem.y.scalarProduct(point.magneticField));
        // 2pi - fieldDir because we look at the fixed field from the turned device
        Double compassBearing = Double.valueOf(2 * Math.PI - horizontalMagneticField.getBearingToYInArcs());
        point.magneticField.compassBearing = compassBearing;
      }
    }
  }

  private void setHeelAndRoll(
      List<DataPoint> points,
      CoordinateSystem horizontalCoordinateSystem)
  {
    for (int i = 1; i < points.size() - 1; ++i)
    {
      DataPoint point = points.get(i);
      if (point.hasAcceleration())
      {
        ThreeDimVector normalizedAcceleration = point.acceleration.normalize();
        point.acceleration.heel = Math.atan(horizontalCoordinateSystem.getX(normalizedAcceleration));
        point.acceleration.roll = Math.atan(horizontalCoordinateSystem.getY(normalizedAcceleration));
      }
    }
  }

  private Double getMaximumOccurenceOfRelativeBearingOfCompassToGpsInArcs(
      List<DataPoint> points)
  {
    int[] bearingHistogram = new int[HISTOGRAM_SIZE];
    for (int i = 1; i < points.size() - 1; ++i)
    {
      DataPoint point = points.get(i);
      if (point.hasMagneticField() && point.location != null && point.location.bearing != null)
      {
        Double relativeNormalizedBearing = getNormalizedRelativeBearingOfCompassToGps(point);
        if (relativeNormalizedBearing != null)
        {
          int histogramBucket = Double.valueOf(HISTOGRAM_SIZE * relativeNormalizedBearing).intValue();
          if (histogramBucket >= 0 && histogramBucket < HISTOGRAM_SIZE)
          {
            bearingHistogram[histogramBucket]++;
          }
        }
      }
    }
    Integer relativeBearing = null;
    int maxBearingCount = 0;
    for (int bearing = 0; bearing < HISTOGRAM_SIZE; bearing++)
    {
      if (bearingHistogram[bearing] > maxBearingCount)
      {
        maxBearingCount = bearingHistogram[bearing];
        relativeBearing = bearing;
      }
    }
    if (relativeBearing == null)
    {
      return null;
    }
    return relativeBearing * 2 * Math.PI / HISTOGRAM_SIZE;
  }

  /**
   * Returns the relative bearing between horizontal compass direction
   * and GPS bearing as a value between 0 (0 degrees) and 1 (360 degrees).
   * If no compass direction or GPS Direction can be obtained,
   * null is returned.
   *
   * @param point the point to calculate the relative bearing for.
   * @param horizontalCoordinateSystem
   * @return
   */
  public Double getNormalizedRelativeBearingOfCompassToGps(DataPoint point)
  {
    if (point.magneticField == null || point.magneticField.compassBearing == null
        || point.location == null || point.location.bearing == null)
    {
      return null;
    }
    double compassBearing = point.magneticField.compassBearing;
    double normalizedRelativeBearing = (compassBearing - point.location.bearing) / 2 / Math.PI;
    if (normalizedRelativeBearing < 0)
    {
      normalizedRelativeBearing += 1;
    }
    return normalizedRelativeBearing;
  }

  /**
   * Approximates the reading of the acceleration sensor at the point with index <code>i</code>
   * by using a weighted average of the nearest acceleration readings.
   * Returns null if no points above and below with acceleration readings can be found,
   * or if the two points are too far apart (more than <code>MAX_ACCELERATION_DISTANCE_MILLIS</code>),
   *
   * @param index the index of the point to get the acceleration reading from
   * @param dataPoints the list of data points, not null. Must be in ascending order with respect to time.
   *
   * @return the approximate acceleration reading at point <code>index</code>,
   *         or null if it cannot be determined.
   */
  ThreeDimVector getAccelerationAt(int index, Data data)
  {
    long time = data.get(index).time;
    ThreeDimVector nearestBelow = null;
    Long nearestBelowTime = null;
    for (int i = index; i >=0 ; i--)
    {
      DataPoint dataPoint = data.get(i);
      if (!dataPoint.hasAcceleration())
      {
        continue;
      }
      if (dataPoint.time == time)
      {
        return dataPoint.acceleration;
      }
      if (dataPoint.time > time)
      {
        throw new IllegalArgumentException("points are not ordered, point " + index + " has time " + time
            + " while point " + i + " has time " + dataPoint.time);
      }
      if (dataPoint.time < time - MAX_ACCELERATION_DISTANCE_MILLIS)
      {
        // point is too far away
        break;
      }
      nearestBelowTime = dataPoint.time;
      nearestBelow = dataPoint.acceleration;
      break;
    }

    ThreeDimVector nearestAbove = null;
    Long nearestAboveTime = null;
    for (int i = index; i < data.size(); i++)
    {
      DataPoint dataPoint = data.get(i);
      if (!dataPoint.hasAcceleration())
      {
        continue;
      }
      if (dataPoint.time == time)
      {
        return dataPoint.acceleration;
      }
      if (dataPoint.time < time)
      {
        throw new IllegalArgumentException("points are not ordered, point " + index + " has time " + time
            + " while point " + i + " has time " + dataPoint.time);
      }
      if (dataPoint.time > time + MAX_ACCELERATION_DISTANCE_MILLIS)
      {
        // point is too far away
        break;
      }
      nearestAboveTime = dataPoint.time;
      nearestAbove = dataPoint.acceleration;
      break;
    }

    if (nearestBelowTime == null || nearestAboveTime == null)
    {
      // we did not find two enclosing data points with acceleration
      return null;
    }
    if (nearestAboveTime - nearestBelowTime > MAX_ACCELERATION_DISTANCE_MILLIS)
    {
      // enclosing points are too far apart
      return null;
    }
    return ThreeDimVector.weightedAdd(
        nearestBelow, nearestAboveTime - time,
        nearestAbove, time - nearestBelowTime);
  }
}
