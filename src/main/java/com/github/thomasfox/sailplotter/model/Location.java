package com.github.thomasfox.sailplotter.model;

import com.github.thomasfox.sailplotter.Constants;

/**
 * Location of boat at specific point in time, typically from GPS.
 */
public class Location
{
  /** Geographical Latitude (Distance from the Aequator in direction North) in arcs. */
  public Double latitude;

  /** Geographical Longitude (Distance from the Greenwich Meridian in direction East) in arcs. */
  public Double longitude;

  /** Altitude above sea level, in meters */
  public Double altitude;

  /** Velocity in knots, measured from GPS. */
  public Double velocity;

  /** Bearing in arcs, measured from GPS. */
  public Double bearing;

  /** velocity over ground in knots, calculated from change in latitude and longitude. */
  public Double velocityFromLatLong;

  /** bearing over ground in arcs, calculated from change in latitude and longitude. */
  public Double bearingFromLatLong;

  /** velocity averaging distance in meters. */
  public Double velocityBearingAveragedOverDistance;

  /** Time from GPS signal, as unix timestamp. */
  public Long satelliteTime;

  /** true if this location is not measured directly but interpolated from neighbouring points. */
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

  public static Location fromXAndY(double x, double y)
  {
    Location result = new Location();
    result.setXAndY(x, y);
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

  public Double getBearingFromLatLongAs360Degrees()
  {
    if (bearingFromLatLong != null)
    {
      return bearingFromLatLong / 2 / Math.PI * 360;
    }
    return null;
  }

  public Double getVelocityFromLatLong()
  {
    return velocityFromLatLong;
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
    result.setXAndY(newX, newY);
    return result;
  }

  public Double distance(Location other)
  {
    double xDist = getX() - other.getX();
    double yDist = getY() - other.getY();
    return Math.sqrt(xDist * xDist + yDist * yDist);
  }

  public Double xRelativeTo(Location reference)
  {
    return getX() - reference.getX();
  }

  public Double yRelativeTo(Location reference)
  {
    return getY() - reference.getY();
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
