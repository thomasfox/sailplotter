package com.github.thomasfox.sailplotter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

/**
 * Location of boat at specific point in time, typically from GPS.
 */
public class Location
{
  /** Geographical Latitude (Distance from the Aequator in direction North) in arcs. */
  @JsonInclude(Include.NON_NULL)
  public Double latitude;

  /** Geographical Longitude (Distance from the Greenwich Meridian in direction East) in arcs. */
  @JsonInclude(Include.NON_NULL)
  public Double longitude;

  /** Altitude above sea level, in meters */
  @JsonInclude(Include.NON_NULL)
  public Double altitude;

  /** Velocity in knots, measured from GPS. */
  @JsonInclude(Include.NON_NULL)
  public Double velocity;

  /** Bearing in arcs, measured from GPS. */
  @JsonInclude(Include.NON_NULL)
  public Double bearing;

  /** velocity over ground in knots, calculated from change in latitude and longitude. */
  @JsonInclude(Include.NON_NULL)
  public Double velocityFromLatLong;

  /** bearing over ground in arcs, calculated from change in latitude and longitude. */
  @JsonInclude(Include.NON_NULL)
  public Double bearingFromLatLong;

  /** velocity averaging distance in meters. */
  @JsonInclude(Include.NON_NULL)
  public Double velocityBearingAveragedOverDistance;

  /** Time from GPS signal, as unix timestamp. */
  @JsonInclude(Include.NON_NULL)
  public Long satelliteTime;

  /** true if this location is not measured directly but interpolated from neighbouring points. */
  @JsonInclude(Include.NON_DEFAULT)
  public boolean interpolated;

  public Location()
  {
  }

  public Location(Location toCopy)
  {
    this.latitude = toCopy.latitude;
    this.longitude = toCopy.longitude;
    this.altitude = toCopy.altitude;
    this.velocity = toCopy.velocity;
    this.bearing = toCopy.bearing;
    this.velocityFromLatLong = toCopy.velocityFromLatLong;
    this.bearingFromLatLong = toCopy.bearingFromLatLong;
    this.velocityBearingAveragedOverDistance = toCopy.velocityBearingAveragedOverDistance;
    this.satelliteTime = toCopy.satelliteTime;
    this.interpolated = toCopy.interpolated;
  }

  public static Location copy(Location toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new Location(toCopy);
  }

  public static Location fromXY(double x, double y)
  {
    Location result = new Location();
    result.setXY(x, y);
    return result;
  }

  public static Location fromXY(TwoDimVector xy)
  {
    Location result = new Location();
    result.setXY(xy);
    return result;
  }

  /**
   * Returns the distance from the aequator in north direction in meters.
   *
   * @return the north coordinate in meters.
   */
  @JsonIgnore
  public double getY()
  {
    return latitude * Constants.EARTH_RADIUS;
  }

  /**
   * Returns the distance from the Greenwich meridian in west direction in meters.
   *
   * @return the west coordinate in meters.
   */
  @JsonIgnore
  public double getX()
  {
    return longitude * Math.cos(latitude) * Constants.EARTH_RADIUS;
  }

  /**
   * Returns a TwoDimVector with the distance from the aequator in north direction in meters as x coordinate
   * and the distance from the Greenwich meridian in west direction in meters as y coordinate.
   *
   * @return the north and west coordinates in meters.
   */
  @JsonIgnore
  public TwoDimVector getXY()
  {
    return new TwoDimVector(getX(), getY());
  }

  public void setXY(double x, double y)
  {
    this.latitude = y / Constants.EARTH_RADIUS;
    this.longitude = x / Constants.EARTH_RADIUS / Math.cos(latitude);
  }

  public void setXY(TwoDimVector xy)
  {
    setXY(xy.x, xy.y);
  }


  @JsonIgnore
  public Double getBearingFromLatLongAs360Degrees()
  {
    if (bearingFromLatLong != null)
    {
      return bearingFromLatLong / 2 / Math.PI * 360;
    }
    return null;
  }

  @JsonIgnore
  public Double getGpsBearingAs360Degrees()
  {
    if (bearing != null)
    {
      return bearing / 2 / Math.PI * 360;
    }
    return null;
  }

  public static Location intersection(Location line1Point1, Location line1Point2, Location line2Point1, Location line2Point2)
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
    Location result = new Location();
    result.setXY(newX, newY);
    return result;
  }

  /**
   * Calculates the distance between two locations.
   * This uses an approximation which is only valid if the distance between the two points is small
   * as compared to the earth radius.
   *
   * @param other the location to compute the distance to, not null.
   *
   * @return the distance in meters.
   */
  public double approximateDistance(Location other)
  {
    double xDist = getX() - other.getX();
    double yDist = getY() - other.getY();
    return Math.sqrt(xDist * xDist + yDist * yDist);
  }

  /**
   * Returns the x coordinate of the difference vector relative to a reference location.
   *
   * @param reference the offset to calculate the x difference from, not null.
   *
   * @return the difference in x coordinates between the two locations.
   */
  public Double xRelativeTo(Location reference)
  {
    return getX() - reference.getX();
  }

  /**
   * Returns the y coordinate of the difference vector relative to a reference location.
   *
   * @param reference the offset to calculate the y difference from, not null.
   *
   * @return the difference in y coordinates between the two locations.
   */
  public Double yRelativeTo(Location reference)
  {
    return getY() - reference.getY();
  }

  /**
   * Returns difference vector between this vector and a reference location.
   *
   * @param reference the offset to calculate the difference vector from, not null.
   *
   * @return the difference in x and y coordinates between the two locations.
   */
  public TwoDimVector xyRelativeTo(Location reference)
  {
    return new TwoDimVector(xRelativeTo(reference), yRelativeTo(reference));
  }

  public static Location interpolate(Location loc1, long weight1, Location loc2, long weight2)
  {
    double totalWeight = weight1 + weight2;
    double weightFactor1 = weight1 / totalWeight;
    double weightFactor2 = weight2 / totalWeight;
    Location result = new Location();
    result.interpolated = true;
    if (loc1.latitude != null && loc2.latitude != null)
    {
      result.latitude = weightFactor1 * loc1.latitude + weightFactor2 * loc2.latitude;
    }
    if (loc1.longitude != null && loc2.longitude != null)
    {
      result.longitude = weightFactor1 * loc1.longitude + weightFactor2 * loc2.longitude;
    }
    if (loc1.altitude != null && loc2.altitude != null)
    {
      result.altitude = weightFactor1 * loc1.altitude + weightFactor2 * loc2.altitude;
    }
    if (loc1.velocity != null && loc2.velocity != null)
    {
      result.velocity = weightFactor1 * loc1.velocity + weightFactor2 * loc2.velocity;
    }
    if (loc1.bearing != null && loc2.bearing != null)
    {
      result.bearing = weightFactor1 * loc1.bearing + weightFactor2 * loc2.bearing;
    }
    return result;
  }
}
