package com.github.thomasfox.sailplotter.exporter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;

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

  public void save(File file, Data data)
  {
    try
    {
      DataPoint startPoint = data.getAllPoints().stream().filter(d -> d.location != null).findFirst().orElse(null);
      List<ExportPoint> exportPoints = data.getAllPoints().stream().map(d -> new ExportPoint(d, startPoint.location)).collect(Collectors.toList());
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file, exportPoints);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

}
