package com.github.thomasfox.sailplotter.model;

import java.text.DecimalFormat;
import java.util.List;

public class Tack
{
  public PointOfSail pointOfSail;

  public DataPoint start;

  public int startIndex;

  public DataPoint end;

  public int endIndex;

  public List<DataPoint> pointsWithinTack;

  public Double windDirection;

  public ManeuverType maneuverTypeAtStart;

  public ManeuverType maneuverTypeAtEnd;

  public DataPoint tackStraightLineIntersectionStart;

  public DataPoint tackStraightLineIntersectionEnd;

  /**
   * @return the length of the tack in meters
   */
  public double getLength()
  {
    return end.distance(start);
  }

  /**
   * @return the duration of the tack in milliseconds
   */
  public long getDuration()
  {
    return end.time - start.time;
  }

  /**
   * Returns the absolute bearing of the tack, i.e. the bearing from tack start to tack end.
   *
   * @return the absolute bearing in arcs, in the range [0, 2*PI[,
   *         or null if the tack direction cannot be determined.
   */
  public Double getAbsoluteBearingInArcs()
  {
    if (end == null)
    {
      return null;
    }
    return start.getBearingTo(end);
  }

  /**
   * Returns the absolute bearing of the tack, i.e. the bearing from tack start to tack end.
   *
   * @return the absolute bearing in degrees, in the range [0°, 360°[,
   *         or null if the tack direction cannot be determined.
   */
  public Double getAbsoluteBearingInDegrees()
  {
    Double absoluteBearingInArcs = getAbsoluteBearingInArcs();
    if (absoluteBearingInArcs == null)
    {
      return null;
    }
    return absoluteBearingInArcs / Math.PI * 180;
  }

  public Double getRelativeBearingInArcs()
  {
    if (windDirection == null || end == null)
    {
      return null;
    }
    Double absoluteBearing = start.getBearingTo(end);
    if (absoluteBearing == null)
    {
      return null;
    }
    double result = absoluteBearing - windDirection;
    if (result < 0)
    {
      result += 2 * Math.PI;
    }
    return result;
  }

  public Double getRelativeBearingInDegrees()
  {
    Double averageRelativeBearingInArcs = getRelativeBearingInArcs();
    if (averageRelativeBearingInArcs == null)
    {
      return null;
    }
    double result = averageRelativeBearingInArcs * 360 / 2 / Math.PI;
    if (result > 180d)
    {
      result = result - 360d;
    }
    return result;
  }

  public Double getVelocityInKnots()
  {
    if (end == null)
    {
      return null;
    }
    return end.getVelocityInKnotsBetween(start);
  }

  public Double getAverageVMGInKnots()
  {
    Double averageVelocity = getVelocityInKnots();
    Double averageRelativeBearing = getRelativeBearingInArcs();
    if (averageVelocity == null || averageRelativeBearing == null)
    {
      return null;
    }
    return averageVelocity * Math.cos(averageRelativeBearing);
  }

  public void start(DataPoint startPoint, int dataPointIndex)
  {
    start = startPoint;
    startIndex = dataPointIndex;
    pointOfSail = startPoint.getPointOfSail();
    windDirection = startPoint.windDirection;
  }

  public void end(DataPoint point, int dataPointIndex, List<DataPoint> completeData)
  {
    end = point;
    endIndex = dataPointIndex;
    pointOfSail = PointOfSail.ofRelativeBearing(start.getRelativeBearingTo(end, windDirection));
    pointsWithinTack = completeData.subList(startIndex, endIndex + 1);
  }

  /**
   * Returns a point in the tack after the maneuver starting the tack has finished.
   * Ideally this point is close to the start of the tack.
   *
   * @return a point in the tack after the maneuver starting the tack has finished,
   *         or null if no such point can be determined.
   */
  public DataPoint getAfterStartManeuver()
  {
    // simplistic approach: must have sailed n metres
    DataPoint result = null;
    for (DataPoint candidate : pointsWithinTack)
    {
      if (candidate.distance(start) > 20d)
      {
        result = candidate;
        break;
      }
    }
    // sanity check: must not be too close to end
    if (result != null && result.distance(end) > 20d)
    {
      return result;
    }
    return null;
  }

  /**
   * Returns a point in the tack before the maneuver ending the tack has begun.
   * Ideally this point is close to the end of the tack.
   *
   * @return a point in the tack before the maneuver ending the tack has begun,
   *         or null if no such point can be determined.
   */
  public DataPoint getBeforeEndManeuver()
  {
    // simplistic approach: must have sailed n metres
    DataPoint result = null;
    for (int i = pointsWithinTack.size() - 1; i >= 0; --i)
    {
      DataPoint candidate = pointsWithinTack.get(i);
      if (candidate.distance(end) > 20d)
      {
        result = candidate;
        break;
      }
    }
    // sanity check: must not be too close to start
    if (result != null && result.distance(start) > 20d)
    {
      return result;
    }
    return null;
  }

  /**
   * Checks whether there is data between AfterStartManeuver and beforeEndManeuver
   * @return
   */
  public boolean hasMainPoints()
  {
    DataPoint afterStartManeuver = getAfterStartManeuver();
    if (afterStartManeuver == null)
    {
      return false;
    }
    DataPoint beforeEndManeuver = getBeforeEndManeuver();
    if (beforeEndManeuver == null)
    {
      return false;
    }
    return (afterStartManeuver.distance(beforeEndManeuver) > 10d);
  }

  /**
   * Calculates the angle between this tack and the other tack.
   * For the tack direction, the intersection points with the other tacks are used.
   *
   * @param other the other tack, or null.
   *
   * @return the angle in arcs between the tacks, or null.
   */
  public Double getIntersectionAngles(Tack other)
  {
    if (other == null
        || tackStraightLineIntersectionStart == null
        || tackStraightLineIntersectionEnd == null
        || other.tackStraightLineIntersectionStart == null
        || other.tackStraightLineIntersectionEnd == null)
    {
      return null;
    }
    double thisTackBearing
        = tackStraightLineIntersectionStart.getBearingTo(tackStraightLineIntersectionEnd);
    double otherTackBearing
        = other.tackStraightLineIntersectionStart.getBearingTo(other.tackStraightLineIntersectionEnd);
    double tackAngle = otherTackBearing - thisTackBearing;
    if (tackAngle < - Math.PI)
    {
      tackAngle += 2 * Math.PI;
    }
    else if (tackAngle > Math.PI)
    {
      tackAngle -= 2 * Math.PI;
    }
    return tackAngle;
  }

  /**
   * Calculates the angle between this tack and the other tack.
   * For the tack direction, the intersection points with the other tacks are used.
   *
   * @param other the other tack, or null.
   *
   * @return the angle in degrees(360) between the tacks, or null.
   */
  public Double getIntersectionAnglesInDegrees(Tack other)
  {
    Double intersectionAnglesInArcs = getIntersectionAngles(other);
    if (intersectionAnglesInArcs == null)
    {
      return null;
    }
    return intersectionAnglesInArcs / Math.PI * 180;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    result.append("Tack: ").append(pointOfSail);
    if (end != null)
    {
      result.append(" Duration:").append(new DecimalFormat("0.0").format(getDuration() / 1000d));
      result.append(" Length:").append(new DecimalFormat("0").format(getLength()));
    }
    return result.toString();
  }
}
