package com.github.thomasfox.sailplotter.model;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;

import org.jfree.data.time.Millisecond;

import com.github.thomasfox.sailplotter.Constants;

public class DataPoint
{
  /** Geographical Latitude (Distance from the Aquator in direction North) in arcs */
  public Double latitude;

  /** Geographical Longitude (Distance from the Greenwich Meridian in direction East) in arcs */
  public Double longitude;

  /** velocity in knots */
  public Double velocity;

  /** bearing in arcs */
  public Double bearing;

  /** millis since 01.01.1970 0:00:00.000 */
  public long time;

  /** wind direction in arcs */
  public Double windDirection;

  /** wind velocity in m/s */
  public Double windVelocity;

  /** velocity averaging distance in meters. */
  public Double velocityBearingAveragedOverDistance;

  public ManoeuverState manoeuverState;

//  public Double distance(DataPoint other)
//  {
//    Double dist = Math.acos(
//        Math.sin(latitude) * Math.sin(other.latitude)
//        + Math.cos(latitude) * Math.cos(other.latitude) * Math.cos(longitude - other.longitude))
//      * Constants.EARTH_RADIUS;
//    return dist;
//  }

  public Double distance(DataPoint other)
  {
    double xDist = getX() - other.getX();
    double yDist = getY() - other.getY();
    return Math.sqrt(xDist * xDist + yDist * yDist);
  }

  public LocalDateTime getLocalDateTime()
  {
    return getLocalDateTime(time);
  }

  private LocalDateTime getLocalDateTime(long millisSince1970)
  {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millisSince1970), Constants.timeZoneId);
  }

  public Millisecond getMillisecond()
  {
    LocalDateTime localDateTime = getLocalDateTime();
    return new Millisecond(
        localDateTime.getNano() / 1000000,
        localDateTime.getSecond(),
        localDateTime.getMinute(),
        localDateTime.getHour(),
        localDateTime.getDayOfMonth(),
        localDateTime.getMonth().getValue(),
        localDateTime.getYear());
  }

  public long averageTime(DataPoint other)
  {
    return (time + other.time) / 2;
  }

  public long timeDistanceMillis(DataPoint other)
  {
    long result = time - other.time;
    return result;
  }

  public double getY()
  {
    return latitude * Constants.EARTH_RADIUS;
  }

  public double getX()
  {
    return longitude * Math.cos(latitude) * Constants.EARTH_RADIUS;
  }

  public double getBearingAs360Degrees()
  {
    return bearing / 2 / Math.PI * 360;
  }

  public Double getVelocity()
  {
    return velocity;
  }

  public Double getRelativeBearingInArcs()
  {
    if (windDirection == null || bearing == null)
    {
      return null;
    }
    double result =  bearing - windDirection;
    if (result < 0)
    {
      result += 2 * Math.PI;
    }
    return result;
  }

  public Double getRelativeBearingAs360Degrees()
  {
    Double relativeBearingInArcs = getRelativeBearingInArcs();
    if (relativeBearingInArcs == null)
    {
      return null;
    }
    double result = relativeBearingInArcs / 2 / Math.PI * 360;
    if (result > 180d)
    {
      result = result - 360d;
    }
    return result;
  }

  public PointOfSail getPointOfSail()
  {
    Double relativeBearingInArcs = getRelativeBearingInArcs();
    if (relativeBearingInArcs == null)
    {
      return null;
    }
    return PointOfSail.ofRelativeBearing(relativeBearingInArcs);
  }

  public Double getBearingTo(DataPoint other)
  {
    double xDistance = other.getX() - getX();
    double yDistance = other.getY() - getY();
    Double result = null;
    if (yDistance != 0)
    {
      result = Math.atan(xDistance / yDistance);
      if (yDistance < 0)
      {
        result += Math.PI;
      }
    }
    else if (xDistance > 0)
    {
      result = Math.PI / 2;
    }
    else if (xDistance < 0)
    {
      result =  3 * Math.PI / 2;
    }
    if (result != null && result < 0)
    {
      result += 2 * Math.PI;
    }
    return result;
  }

  public Double getRelativeBearingTo(DataPoint other, Double windDirection)
  {
    if (windDirection == null)
    {
      return null;
    }
    Double bearingToOther = getBearingTo(other);
    if (bearingToOther == null)
    {
      return null;
    }
    double result =  bearingToOther - windDirection;
    if (result < 0)
    {
      result += 2 * Math.PI;
    }
    return result;
  }



  public double getVelocityInKnotsBetween(DataPoint other)
  {
    return distance(other) / timeDistanceMillis(other) * 1000 / Constants.NAUTICAL_MILE * 3600d;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder()
        .append("DataPoint: ")
        .append(getLocalDateTime())
        .append(" (")
        .append(new DecimalFormat("0").format(getX()))
        .append("m,")
        .append(new DecimalFormat("0").format(getY()))
        .append("m) ");
    if (velocity != null)
    {
      result.append(new DecimalFormat("0.0").format(velocity))
          .append("kts ");
    }
    if (bearing != null)
    {
      result.append(new DecimalFormat("0.0").format(getBearingAs360Degrees()))
          .append("°Abs ");
    }
    if (bearing != null && windDirection != null)
    {
      result.append(new DecimalFormat("0.0").format(getRelativeBearingAs360Degrees()))
          .append("°Rel");
    }
    return result.toString();
  }

}
