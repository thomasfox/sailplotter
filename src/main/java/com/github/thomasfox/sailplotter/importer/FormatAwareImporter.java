package com.github.thomasfox.sailplotter.importer;

import java.io.File;
import java.util.List;

import com.github.thomasfox.sailplotter.model.DataPoint;

public class FormatAwareImporter implements Importer
{
  @Override
  public List<DataPoint> read(File file)
  {
    List<DataPoint> data;
    if (file.getPath().endsWith(".log"))
    {
      data = new SailRacerImporter().read(file);
    }
    if (file.getPath().endsWith(".saillog"))
    {
      data = new SailLoggerImporter().read(file);
    }
    else if (file.getPath().endsWith(".vrtp"))
    {
      data = new ViewRangerImporter().read(file);
    }
    else
    {
      throw new RuntimeException("unknown extenson of file " + file.getName()
          + ", known extensions are .log, .saillog and .vrtp");
    }
    if (data.size() < 2) {
      throw new RuntimeException("Track contains lesss than 2 points");
    }
    return data;
  }
}
