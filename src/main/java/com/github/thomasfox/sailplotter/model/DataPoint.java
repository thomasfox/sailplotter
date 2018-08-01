package com.github.thomasfox.sailplotter.model;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jfree.data.time.Millisecond;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

public class DataPoint
{
  /**
   * The index of the point in the global list of data points,
   * or -1 to indicate that the data point is not a member of the global list of data points.
   */
  public int index = -1;

  /** millis since 01.01.1970 0:00:00.000 GMT.*/
  public Long time;

  /** Location of boat, typically obtained from GPS. */
  public Location location;

  /**
   * Magnetic field at boat position, in microtesla,
   * in arbitrary but constant orientation.
   */
  public ThreeDimVector magneticField;

  /**
   * Measured acceleration in Nm/s^2, including gravitational acceleration,
   * in arbitrary but constant orientation.
   */
  public ThreeDimVector acceleration;

  /** Wind data at place of boat, can be interpolated. */
  public Wind wind;

  public ManoeuverState manoeuverState;

  public DataPoint(int index)
  {
    this.index = index;
  }

  public DataPoint(DataPoint toCopy)
  {
    this.index = toCopy.index;
    this.time = toCopy.time;
    this.location = Location.copy(toCopy.location);
    this.wind = Wind.copy(toCopy.wind);
    this.magneticField = ThreeDimVector.copy(toCopy.magneticField);
    this.acceleration = ThreeDimVector.copy(toCopy.acceleration);
    this.manoeuverState = toCopy.manoeuverState;
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

  public boolean hasLocation()
  {
    return location != null && location.latitude != null && location.longitude != null && !location.interpolated;
  }

  public boolean hasAcceleration()
  {
    return acceleration != null && acceleration.x != null && acceleration.y != null && acceleration.z != null;
  }

  public boolean hasMagneticField()
  {
    return magneticField != null && magneticField.x != null && magneticField.y != null && magneticField.z != null;
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


  public Double getRelativeBearingInArcs()
  {
    if (wind == null || wind.direction == null || location == null || location.bearingFromLatLong == null)
    {
      return null;
    }
    double result =  location.bearingFromLatLong - wind.direction;
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
    double xDistance = other.location.getX() - location.getX();
    double yDistance = other.location.getY() - location.getY();
    return new TwoDimVector(xDistance, yDistance).getBearingToXInArcs();
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

  /**
   * Calculates the bearing difference between the bearing of this point
   * and the passed bearing. The bearing difference is normalized between
   * -PI exclusive and PI inclusive.
   *
   * @param absoluteBearingInArcs the bearing to calculate the difference to.
   *
   * @return the bearing difference, or null if bearing is null or
   *         absoluteBearingInArcs is null.
   */
  public Double getBearingDifference(Double absoluteBearingInArcs)
  {
    if (location == null || location.bearingFromLatLong == null || absoluteBearingInArcs == null)
    {
      return null;
    }
    double bearingDifference = location.bearingFromLatLong - absoluteBearingInArcs;
    if (bearingDifference > Math.PI)
    {
      bearingDifference -= 2 * Math.PI;
    }
    if (bearingDifference <= -Math.PI)
    {
      bearingDifference += 2 * Math.PI;
    }
    return bearingDifference;
  }

  public double getVelocityInKnotsBetween(DataPoint other)
  {
    return location.distance(other.location) / timeDistanceMillis(other) * 1000 / Constants.NAUTICAL_MILE * 3600d;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder()
        .append("DataPoint: ");
    if (time != null)
    {
      result.append(getLocalDateTime());
    }
    if (location !=null && location.latitude != null && location.longitude != null)
    {
      result.append(" (")
        .append(new DecimalFormat("0").format(location.getX()))
        .append("m,")
        .append(new DecimalFormat("0").format(location.getY()))
        .append("m) ");
    }
    if (location !=null && location.velocityFromLatLong != null)
    {
      result.append(new DecimalFormat("0.0").format(location.velocityFromLatLong))
          .append("kts ");
    }
    if (location !=null && location.bearingFromLatLong != null)
    {
      result.append(new DecimalFormat("0.0").format(location.getBearingFromLatLongAs360Degrees()))
          .append("�Abs ");
    }
    if (location !=null && location.bearingFromLatLong != null
        && wind != null && wind.direction != null)
    {
      result.append(new DecimalFormat("0.0").format(getRelativeBearingAs360Degrees()))
          .append("�Rel");
    }
    return result.toString();
  }

  public String getXYLabel()
  {
    StringBuilder result = new StringBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_TIME.format(getLocalDateTime()));
    if (location != null && location.velocityFromLatLong != null)
    {
      result.append(new DecimalFormat("0.0").format(location.velocityFromLatLong))
          .append("kts ");
    }
    if (location != null && location.bearingFromLatLong != null)
    {
      result.append(new DecimalFormat("0.0").format(location.getBearingFromLatLongAs360Degrees()))
          .append("�Abs ");
    }
    if (location != null && location.bearingFromLatLong != null
        && wind != null && wind.direction != null)
    {
      result.append(new DecimalFormat("0.0").format(getRelativeBearingAs360Degrees()))
          .append("�Rel");
    }
    return result.toString();
  }
}
