package com.github.thomasfox.sailplotter.importer;

import java.util.List;

import com.github.thomasfox.sailplotter.model.Data;

public class ImporterResult
{
  private final Data data;

  private final List<String> warnings;

  public ImporterResult(Data data, List<String> warnings)
  {
    this.data = data;
    this.warnings = warnings;
  }

  public Data getData()
  {
    return data;
  }

  public List<String> getWarnings()
  {
    return warnings;
  }

  public boolean hasWarnMessages()
  {
    return !warnings.isEmpty();
  }
}
