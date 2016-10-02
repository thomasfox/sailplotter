package com.github.thomasfox.sailplotter.model;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jfree.data.time.Millisecond;

import com.github.thomasfox.sailplotter.Constants;

public class DataPoint
{
  /**
   * The index of the point in the global list of data points,
   * or -1 to indicate that the data point is not a member of the global list of data points.
   */
  public int index;

  /** Geographical Latitude (Distance from the Aquator in direction North) in arcs */
  public Double latitude;

  /** Geographical Longitude (Distance from the Greenwich Meridian in direction East) in arcs */
  public Double longitude;

  /** velocity in knots */
  public Double velocity;

  /** bearing in arcs */
  public Double bearing;

  /** millis since 01.01.1970 0:00:00.000 */
  public Long time;

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

  public DataPoint(int index)
  {
    this.index = index;
  }

  public DataPoint(DataPoint toCopy)
  {
    this.index = toCopy.index;
    this.latitude = toCopy.latitude;
    this.longitude = toCopy.longitude;
    this.velocity = toCopy.velocity;
    this.bearing = toCopy.bearing;
    this.time = toCopy.time;
    this.windDirection = toCopy.windDirection;
    this.windVelocity = toCopy.windVelocity;
    this.velocityBearingAveragedOverDistance = toCopy.velocityBearingAveragedOverDistance;
    this.manoeuverState = toCopy.manoeuverState;
  }

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

  public void setXAndY(double x, double y)
  {
    this.latitude = y / Constants.EARTH_RADIUS;
    this.longitude = x / Constants.EARTH_RADIUS / Math.cos(latitude);
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

  /**
   * Gets the bearing of this point to another point.
   *
   * @param other the other point, not null.
   *
   * @return the bearing to the other point, in arcs, in the range [0, 2*PI[,
   *         or null if the distance between the two points is null.
   */
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
    while (result != null && result < 0)
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

  public static DataPoint intersection(DataPoint line1Point1, DataPoint line1Point2, DataPoint line2Point1, DataPoint line2Point2)
  {
    double deltaXLine1 = line1Point2.getX() - line1Point1.getX();
    double deltaYLine1 = line1Point2.getY() - line1Point1.getY();
    double deltaXLine2 = line2Point2.getX() - line2Point1.getX();
    double deltaYLine2 = line2Point2.getY() - line2Point1.getY();

    double newX = (deltaXLine2*deltaYLine1*line1Point1.getX()
            + deltaXLine1*deltaXLine2*line2Point1.getY()
            - deltaXLine1*deltaYLine2*line2Point1.getX()
            - deltaXLine1*deltaXLine2*line1Point1.getY())
        / (deltaXLine2*deltaYLine1 - deltaXLine1*deltaYLine2);

    double newY = (deltaXLine2*deltaYLine1*line2Point1.getY()
            + deltaYLine1*deltaYLine2*line1Point1.getX()
            - deltaXLine1*deltaYLine2*line1Point1.getY()
            - deltaYLine1*deltaYLine2*line2Point1.getX())
        / (deltaXLine2*deltaYLine1 - deltaXLine1*deltaYLine2);
    DataPoint result = new DataPoint(-1);
    result.setXAndY(newX, newY);
    return result;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder()
        .append("DataPoint: ");
    if (time != null)
    {
      result.append(getLocalDateTime())
        .append(" (");
    }
    if (latitude != null && longitude != null)
    {
      result.append(new DecimalFormat("0").format(getX()))
        .append("m,")
        .append(new DecimalFormat("0").format(getY()))
        .append("m) ");
    }
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

  public String getXYLabel()
  {
    StringBuilder result = new StringBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_TIME.format(getLocalDateTime()));
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
