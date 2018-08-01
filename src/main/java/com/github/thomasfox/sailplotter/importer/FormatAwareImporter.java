package com.github.thomasfox.sailplotter.importer;

import java.io.File;

import com.github.thomasfox.sailplotter.model.Data;

public class FormatAwareImporter implements Importer
{
  @Override
  public Data read(File file)
  {
    Data data;
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
    if (data.getPointsWithLocation().size() < 2) {
      throw new RuntimeException("Track contains lesss than 2 locations");
    }
    return data;
  }
}
