package com.github.thomasfox.sailplotter.gui.component.progress;

import java.util.List;

public class NoOpProgressChanged implements ProgressChanged
{
  @Override
  public void start(String headline)
  {
  }

  @Override
  public void setToDisplay(String toDisplay)
  {
  }

  @Override
  public void setWarnings(List<String> warnings)
  {
  }

  @Override
  public void finished()
  {
  }
}