package com.github.thomasfox.sailplotter.importer;

import java.io.File;

import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.model.Data;

public class FormatAwareImporter implements Importer
{
  private final LoadProgress loadProgress;

  public FormatAwareImporter(LoadProgress loadProgress)
  {
    this.loadProgress = loadProgress;
  }

  @Override
  public Data read(File file)
  {
    Data data;
    if (file.getPath().endsWith(".log"))
    {
      data = new SailRacerImporter(loadProgress).read(file);
    }
    if (file.getPath().endsWith(".saillog"))
    {
      data = new SailLoggerImporter(loadProgress).read(file);
    }
    else if (file.getPath().endsWith(".vrtp"))
    {
      data = new ViewRangerImporter(loadProgress).read(file);
    }
    else
    {
      throw new RuntimeException("unknown extenson of file " + file.getName()
          + ", known extensions are .log, .saillog and .vrtp");
    }
    if (data.getPointsWithLocation().size() < 2) {
      throw new RuntimeException("Track contains lesss than 2 locations");
    }
    data.setFile(file);
    return data;
  }
}
