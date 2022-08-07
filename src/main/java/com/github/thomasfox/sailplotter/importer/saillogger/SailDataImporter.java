package com.github.thomasfox.sailplotter.importer.saillogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.importer.Importer;
import com.github.thomasfox.sailplotter.importer.ImporterResult;
import com.github.thomasfox.sailplotter.model.Acceleration;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;
import com.github.thomasfox.sailplotter.model.MagneticField;

public class SailDataImporter implements Importer
{
  private final ObjectMapper mapper = new ObjectMapper();

  private final LoadProgress loadProgress;

  public SailDataImporter(LoadProgress loadProgress)
  {
    this.loadProgress = loadProgress;
  }

  @Override
  public ImporterResult read(File file)
  {
    Data data = new Data();
    List<String> warningList = new ArrayList<>();
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
      try
      {
        data.add(dataPoint);
      }
      catch (RuntimeException e)
      {
        warningList.add("Could not add point with index " + index + ":" + e.getMessage());
      }
      index++;
    }
    return new ImporterResult(data, warningList);
  }

  public SailLoggerData readFileInternal(File file)
  {
    try
    {
      loadProgress.fileReadingStarted();
      SailLoggerData readValue = mapper.readValue(file, SailLoggerData.class);
      loadProgress.fileReadingFinished();
      return readValue;
    }
    catch (IOException e)
    {
      loadProgress.finished();
      throw new RuntimeException(e);
    }
  }
}
