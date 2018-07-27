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

  /** velocity over ground in knots, from location data. */
  public Double velocity;

  /** bearing over ground in arcs, from location data. */
  public Double bearing;

  /** velocity averaging distance in meters. */
  public Double velocityBearingAveragedOverDistance;

  public Location()
  {
  }

  public Location(Location toCopy)
  {
    this.latitude = toCopy.latitude;
    this.longitude = toCopy.longitude;
    this.velocity = toCopy.velocity;
    this.bearing = toCopy.bearing;
    this.velocityBearingAveragedOverDistance = toCopy.velocityBearingAveragedOverDistance;
  }

  public static Location copy(Location toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new Location(toCopy);
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


}
