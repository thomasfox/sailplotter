package com.github.thomasfox.sailplotter.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.Acceleration;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;
import com.github.thomasfox.sailplotter.model.MagneticField;

public class SailLoggerImporter implements Importer
{
  final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Data read(File file)
  {
    Data result = new Data();
    SailLoggerData rawData = readFileInternal(file);
    int index = 0;
    for (SailLoggerTrackPoint rawPoint : rawData.track)
    {
      if (!rawPoint.hasGpsData() && !rawPoint.hasCompassData() && !rawPoint.hasAccelerationData())
      {
        continue;
      }
      DataPoint dataPoint = new DataPoint(index);
      if (rawPoint.hasGpsData())
      {
        dataPoint.location = new Location();
        dataPoint.location.latitude = rawPoint.locLat / 180d * Math.PI;
        dataPoint.location.longitude = rawPoint.locLong / 180d * Math.PI;
        dataPoint.location.velocity = rawPoint.locVel / Constants.NAUTICAL_MILE * 3600d;
        dataPoint.location.bearing = rawPoint.locBear / 180d * Math.PI;
        dataPoint.location.satelliteTime = rawPoint.locT;
        dataPoint.time = rawPoint.locDevT;
      }
      if (rawPoint.hasCompassData())
      {
        dataPoint.magneticField = new MagneticField(rawPoint.magX, rawPoint.magY, rawPoint.magZ);
        dataPoint.time = rawPoint.magT;
      }
      if (rawPoint.hasAccelerationData())
      {
        dataPoint.acceleration = new Acceleration(rawPoint.accX, rawPoint.accY, rawPoint.accZ);
        dataPoint.time = rawPoint.accT;
      }
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
    public float locAlt;
    public Long locDevT;
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

    public boolean hasCompassData()
    {
      return (magX != null && magY != null && magZ != null && magT != null);
    }

    public boolean hasAccelerationData()
    {
      return (accX != null && accY != null && accZ != null && accT != null);
    }
  }

  public static final class SailLoggerStart
  {
    public String format;
    public String loggedBy;
    public String loggedByVersion;
    public long startT;
    public String startTFormatted;
  }

  public static final class SailLoggerEnd
  {
    public long endT;
    public String endTFormatted;
  }
}
