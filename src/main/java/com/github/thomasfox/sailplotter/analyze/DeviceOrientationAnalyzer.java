package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

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
 *     The up direction is determined by getting the mean acceleration
 *     of the device. The mean direction will point in the direction
 *     of gravity up.
 *     IF the boat is not heeled on average, this will also be the
 *     the up direction of the boat.
 *   </li>
 *   <li>
 *     The front direction is determined as follows:
 *     As an approximation, we assume that the boat is moving on average forward
 *     (may not be exactly true due to current and leeway).
 *     The forward direction at one point relative to the earth
 *     can thus be determined by the gps-based absolute bearing.
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

  public Data analyze(Data data)
  {
    CoordinateSystem horizontalCoordinateSystem
        = getHorizontalCoordinateSystemFromAverageAcceleration(data.getAllPoints());

    if (horizontalCoordinateSystem == null)
    {
      return data;
    }

    setCompassBearings(data.getAllPoints(), horizontalCoordinateSystem);

    Double maxOccurenceOfRelativeBearingInArcs
        = getMaximumOccurenceOfRelativeBearingOfCompassToGpsInArcs(data.getAllPoints());

    if (maxOccurenceOfRelativeBearingInArcs != null)
    {
      horizontalCoordinateSystem = horizontalCoordinateSystem.getRotatedAroundZ(maxOccurenceOfRelativeBearingInArcs);
      setCompassBearings(data.getAllPoints(), horizontalCoordinateSystem);
      setHeelAndRoll(data.getAllPoints(), horizontalCoordinateSystem);
    }
    return data;
  }

  /**
   * Returns a horizontal coordinate system in the device coordinate system.
   * Coordinate system x is approximately device x,
   * coordinate system y is approximately device y,
   * and coordinate system z is up, determined from mean device acceleration.
   *
   * @param data the data points to analyze, not null.
   *
   * @return a horizontal coordinate system, or null, if no up direction could be determined.
   */
  CoordinateSystem getHorizontalCoordinateSystemFromAverageAcceleration(List<DataPoint> points)
  {
    CoordinateSystem horizontalCoordinateSystem = new CoordinateSystem();

    // assuming average is upright position of boat
    ThreeDimVector uprightAcceleration = getAverageAcceleration(points);

    if (uprightAcceleration == null || uprightAcceleration.length() < 0.1)
    {
      // we have no data or we have no defined upright position
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

  ThreeDimVector getAverageAcceleration(List<DataPoint> points)
  {
    ThreeDimVector averageAcceleration = new ThreeDimVector(0d, 0d, 0d);
    int accelerationCount = 0;

    for (int i = 0; i < points.size(); ++i)
    {
      DataPoint point = points.get(i);
      if (point.hasAcceleration())
      {
        averageAcceleration.add(point.acceleration);
        accelerationCount++;
      }
    }
    if (accelerationCount == 0)
    {
      return null;
    }
    averageAcceleration = averageAcceleration.multiplyBy(1d / accelerationCount);
    return averageAcceleration;
  }

  private void setCompassBearings(
      List<DataPoint> points,
      CoordinateSystem horizontalCoordinateSystem)
  {
    for (int i = 1; i < points.size() - 1; ++i)
    {
      DataPoint point = points.get(i);
      if (point.hasMagneticField())
      {
        TwoDimVector horizontalMagneticField = new TwoDimVector(
            horizontalCoordinateSystem.x.scalarProduct(point.magneticField),
            horizontalCoordinateSystem.y.scalarProduct(point.magneticField));
        // 2pi - fieldDir because we look at the fixed field from the turned device
        Double compassBearing = new Double(2 * Math.PI - horizontalMagneticField.getBearingToYInArcs());
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
          int histogramBucket = new Double(HISTOGRAM_SIZE * relativeNormalizedBearing).intValue();
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
}
