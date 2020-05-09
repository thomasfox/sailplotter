package com.github.thomasfox.sailplotter.importer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;

public class ViewRangerImporter implements Importer
{
  final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Data read(File file)
  {
    Data result = new Data();
    ViewRangerData rawData = readFileInternal(file);
    int index = 0;
    for (ViewRangerPoint rawPoint : rawData.points)
    {
      DataPoint dataPoint = new DataPoint(index);
      dataPoint.location = new Location();
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
    catch (JsonMappingException e)
    {
      // ViewRanger forgets closing tags when saving. Try adding them.
      try
      {
        String fileContentAsString = FileUtils.readFileToString(file, StandardCharsets.ISO_8859_1);
        fileContentAsString = fileContentAsString + "]}";
        ViewRangerData readValue = mapper.readValue(fileContentAsString, ViewRangerData.class);
        return readValue;
      }
      catch (IOException ee)
      {
        throw new RuntimeException(ee);
      }
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
