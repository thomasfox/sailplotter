package com.github.thomasfox.sailplotter.exporter;

import com.github.thomasfox.sailplotter.model.Location;

class ExportLocation
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

  public Double x;

  public Double y;

  public ExportLocation(Location location, Location referenceLocation)
  {
    this.latitude = location.latitude;
    this.longitude = location.longitude;
    this.velocity = location.velocityFromLatLong;
    this.bearing = location.bearingFromLatLong;
    this.velocityBearingAveragedOverDistance = location.velocityBearingAveragedOverDistance;
    if (referenceLocation != null)
    {
      this.x = location.xRelativeTo(referenceLocation);
      this.y = location.yRelativeTo(referenceLocation);
    }
  }
}