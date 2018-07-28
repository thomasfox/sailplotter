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
    for (SailLoggerTrackPoint rawPoint : rawData.track)
    {
      if (rawPoint.hasGpsData())
      {
        DataPoint dataPoint = new DataPoint(index);
        dataPoint.location.latitude = rawPoint.locLat;
        dataPoint.location.longitude = rawPoint.locLong;
        dataPoint.time = rawPoint.locT;
        result.add(dataPoint);
        index++;
      }
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

  public static final class SailLoggerData
  {
    public SailLoggerStart start;
    public ArrayList<SailLoggerTrackPoint> track;
    public SailLoggerEnd end;
  }

  public static final class SailLoggerTrackPoint
  {
    public Long locT;
    public Float locAcc;
    public Double locLat;
    public Double locLong;
    public Float locBear;
    public Float locVel;
    public Long magT;
    public Double magX;
    public Double magY;
    public Double magZ;
    public Long accT;
    public Double accX;
    public Double accY;
    public Double accZ;

    public boolean hasGpsData()
    {
      return (locT != null);
    }
  }

  public static final class SailLoggerStart
  {
    public String format;
    public long startT;
    public String startTFormatted;
  }

  public static final class SailLoggerEnd
  {
    public long endT;
    public String endTFormatted;
  }
}
