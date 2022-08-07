package com.github.thomasfox.sailplotter.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;

/**
 * Imports data from a GPX file.
 */
public class GpxImporter implements Importer
{
  private final LoadProgress loadProgress;


  public GpxImporter(LoadProgress loadProgress)
  {
    this.loadProgress = loadProgress;
  }

  @Override
  public ImporterResult read(File file)
  {
    List<String> warnMessages = new ArrayList<>();
    Data data = new Data();
    List<GpxPoint> rawData = readFileInternal(file);
    int index = 0;
    for (GpxPoint rawPoint : rawData)
    {
      DataPoint dataPoint = new DataPoint(index);
      dataPoint.location = new Location();
      dataPoint.location.latitude = rawPoint.lat / 180d * Math.PI;
      dataPoint.location.longitude = rawPoint.lon / 180d * Math.PI;
      try
      {
        dataPoint.time = rawPoint.time.getTime();
      }
      catch (Exception e)
      {
        warnMessages.add("bad timestamp in data point with index " + index);
        continue;
      }
      data.add(dataPoint);
      index++;
    }
    return new ImporterResult(data, warnMessages);
  }

  public List<GpxPoint> readFileInternal(File file)
  {
    List<GpxPoint> result = new ArrayList<>();
    loadProgress.fileReadingStarted();
    try (InputStream is = new FileInputStream(file))
    {
      ObjectMapper xmlMapper = new XmlMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      Gpx value = xmlMapper.readValue(is, Gpx.class);
      result = value.trk.trkseg.trkpt;
    }
    catch (IOException e)
    {
      loadProgress.finished();
      throw new RuntimeException(e);
    }
    loadProgress.fileReadingFinished();
    return result;
  }

  public static class Gpx
  {
    public GpxTrack trk;
  }

  public static class GpxTrack
  {
    public GpxTrackSegment trkseg;
  }

  public static class GpxTrackSegment
  {
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<GpxPoint> trkpt;
  }

  public static final class GpxPoint
  {
    public Date time;

    @JacksonXmlProperty(isAttribute = true)
    public double lat;

    @JacksonXmlProperty(isAttribute = true)
    public double lon;
  }
}
