package com.github.thomasfox.sailplotter.importer;

import java.io.File;

import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.importer.saillogger.SailDataImporter;

public class FormatAwareImporter implements Importer
{
  private final LoadProgress loadProgress;

  public FormatAwareImporter(LoadProgress loadProgress)
  {
    this.loadProgress = loadProgress;
  }

  @Override
  public ImporterResult read(File file)
  {
    ImporterResult result;
    if (file.getPath().endsWith(".log"))
    {
      result = new SailRacerImporter(loadProgress).read(file);
    }
    if (file.getPath().endsWith(".saillog") || file.getPath().endsWith(".saildata"))
    {
      result = new SailDataImporter(loadProgress).read(file);
    }
    else if (file.getPath().endsWith(".vrtp"))
    {
      result = new ViewRangerImporter(loadProgress).read(file);
    }
    else if (file.getPath().endsWith(".gpx"))
    {
      result = new GpxImporter(loadProgress).read(file);
    }
    else
    {
      throw new RuntimeException("unknown extenson of file " + file.getName()
          + ", known extensions are .gpx, .log, .saillog, .saildata and .vrtp");
    }
    if (result.getData() == null
        || result.getData().getPointsWithLocation().size() < 2)
    {
      throw new RuntimeException("Track contains lesss than 2 locations");
    }
    result.getData().setFile(file);
    return result;
  }
}
