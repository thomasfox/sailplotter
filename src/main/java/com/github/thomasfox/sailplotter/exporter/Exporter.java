package com.github.thomasfox.sailplotter.exporter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;

public class Exporter
{
  private final String EXTENSION = ".sailplot";

  public File replaceExtension(File file)
  {
    String result;
    String path = file.getPath();
    if (path.indexOf('.') != -1)
    {
      result = path.substring(0, path.lastIndexOf('.'));
    }
    else
    {
      result = path;
    }
    result = result + EXTENSION;
    return new File(result);
  }

  public void save(File file, List<DataPoint> data)
  {
    try
    {
      DataPoint startPoint = data.stream().filter(d -> d.location != null).findFirst().orElse(null);
      List<ExportPoint> exportPoints = data.stream().map(d -> new ExportPoint(d, startPoint.location)).collect(Collectors.toList());
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file, exportPoints);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  private static class ExportPoint
  {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm:ss.SSSZ");

    public final Long time;

    public ExportLocation location;

    public ExportPoint(DataPoint dataPoint, Location referenceLocation)
    {
      this.time = dataPoint.time;
      if (dataPoint.location != null)
      {
        this.location = new ExportLocation(dataPoint.location, referenceLocation);
      }
    }

    public String getDateFormatted()

    {
      return dateFormat.format(new Date(time));
    }
  }

  private static class ExportLocation
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

}
