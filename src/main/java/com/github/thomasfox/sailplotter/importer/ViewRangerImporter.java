package com.github.thomasfox.sailplotter.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class ViewRangerImporter implements Importer
{
  final ObjectMapper mapper = new ObjectMapper();

  @Override
  public List<DataPoint> read(File file)
  {
    List<DataPoint> result = new ArrayList<>();
    ViewRangerData rawData = readFileInternal(file);
    int index = 0;
    for (ViewRangerPoint rawPoint : rawData.points)
    {
      DataPoint dataPoint = new DataPoint(index);
      dataPoint.location.latitude = rawPoint.lat / 180d * Math.PI;
      dataPoint.location.longitude = rawPoint.lon / 180d * Math.PI;
      dataPoint.time = rawPoint.time;
      result.add(dataPoint);
      index++;
    }
    return result;
  }

  public ViewRangerData readFileInternal(File file)
  {
    try
    {
      ViewRangerData readValue = mapper.readValue(file, ViewRangerData.class);
      return readValue;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static final class ViewRangerData
  {
    public ViewRangerHeader header;

    public List<ViewRangerPoint> points;
  }

  public static final class ViewRangerHeader
  {
    public int colour;

    public String name;

    public long lastModTime;

    public int gridPositionCoordType;
  }

  public static final class ViewRangerPoint
  {
    public double lat;

    public double lon;

    public double map_x;

    public double map_y;

    public double alt;

    public long time;

    public Boolean from_gps;

    public Boolean is_marker;

    public Boolean segment_start;

    public Boolean has_position;
  }
}
