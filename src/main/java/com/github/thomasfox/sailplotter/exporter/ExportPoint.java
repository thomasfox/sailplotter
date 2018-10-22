package com.github.thomasfox.sailplotter.exporter;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;

class ExportPoint
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