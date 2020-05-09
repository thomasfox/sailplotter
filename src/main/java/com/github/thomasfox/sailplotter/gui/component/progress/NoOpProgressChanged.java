package com.github.thomasfox.sailplotter.gui.component.progress;

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
  public void finished()
  {
  }
}