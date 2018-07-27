package com.github.thomasfox.sailplotter.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class SailLoggerImporter implements Importer
{
  final ObjectMapper mapper = new ObjectMapper();

  @Override
  public List<DataPoint> read(File file)
  {
    List<DataPoint> result = new ArrayList<>();
    SailLoggerData rawData = readFileInternal(file);
    int index = 0;
    for (SailLoggerPoint rawPoint : rawData)
    {
      DataPoint dataPoint = new DataPoint(index);
      dataPoint.location.latitude = rawPoint.locLat;
      dataPoint.location.longitude = rawPoint.locLong;
      dataPoint.time = rawPoint.locT;
      result.add(dataPoint);
      index++;
    }
    return result;
  }

  public SailLoggerData readFileInternal(File file)
  {
    try
    {
      SailLoggerData readValue = mapper.readValue(file, SailLoggerData.class);
      return readValue;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static final class SailLoggerData extends ArrayList<SailLoggerPoint>
  {
    private static final long serialVersionUID = 1L;
  }

  public static final class SailLoggerPoint
  {
    public long locT;
    public float locAcc;
    public double locLat;
    public double locLong;
    public float locBear;
    public float locVel;
    public long magT;
    public double magX;
    public double magY;
    public double magZ;
    public long tAcc;
    public double accX;
    public double accY;
    public double accZ;
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
  }
}
