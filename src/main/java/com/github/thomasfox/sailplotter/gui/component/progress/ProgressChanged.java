package com.github.thomasfox.sailplotter.gui.component.progress;

import java.util.List;

/**
 * A worker can broadcast its progress through this interface.
 */
public interface ProgressChanged
{
  void start(String headline);

  void setToDisplay(String toDisplay);

  void setWarnings(List<String> warnings);

  void finished();
}
