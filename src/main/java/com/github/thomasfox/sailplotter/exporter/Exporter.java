package com.github.thomasfox.sailplotter.exporter;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.thomasfox.sailplotter.model.Data;

public class Exporter
{
  private final String EXTENSION = ".sailplot";

  public File replaceExtension(File file)
  {
    if (file == null)
    {
      return null;
    }
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
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file, data);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

}
