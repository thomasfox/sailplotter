package com.github.thomasfox.sailplotter.gui.component.progress;

import java.util.List;

/**
 * Advertises the progress of loading a file through a ProgressChanged instance.
 */
public class LoadProgress
{
  private final ProgressChanged progressChanged;

  /**
   * Constructor.
   *
   * @param progressChanged where to advertise the loading progress,
   *        or null if no advertising should be done.
   */
  public LoadProgress(ProgressChanged progressChanged)
  {
    if (progressChanged != null)
    {
      this.progressChanged = progressChanged;
    }
    else
    {
      this.progressChanged = new NoOpProgressChanged();
    }
  }

  public void start()
  {
    progressChanged.start("Loading data");
  }

  public void finished()
  {
    progressChanged.setToDisplay(null);
    progressChanged.finished();
  }

  public void fileReadingStarted()
  {
    progressChanged.setToDisplay("loading file...");
  }

  public void fileReadingFinished()
  {
    progressChanged.setToDisplay("file loaded.");
  }

  public void analyzingStarted()
  {
    progressChanged.setToDisplay("analyzing data...");
  }

  public void startCorrectTimeUsingGpsTime()
  {
    progressChanged.setToDisplay("correcting time to GPS time...");
  }

  public void startInterpolatingLocation()
  {
    progressChanged.setToDisplay("interpolating location...");
  }

  public void startCalculateLocationAndBearing()
  {
    progressChanged.setToDisplay("calculating location and bearing...");
  }

  public void startCalculateTackList()
  {
    progressChanged.setToDisplay("calculating tack list...");
  }

  public void startCalculateTackSeriesList()
  {
    progressChanged.setToDisplay("calculating tack series list...");
  }

  public void startAnalyzeOrientation()
  {
    progressChanged.setToDisplay("analyzing orientation...");
  }

  public void startAnalyzeOrientationCalculateHorizontalCoordinateSystem()
  {
    progressChanged.setToDisplay("analyzing orientation: calculate horizontal coordinate system...");
  }

  public void startAnalyzeOrientationSetCompassBearings()
  {
    progressChanged.setToDisplay("analyzing orientation: set compass bearings...");
  }

  public void startAnalyzeOrientationGetCompassToGpsAngle()
  {
    progressChanged.setToDisplay("analyzing orientation: get compass to GPS angle...");
  }

  public void startAnalyzeOrientationSetHeelAndRoll()
  {
    progressChanged.setToDisplay("analyzing orientation: set heel and roll...");
  }

  public void warningsChanged(List<String> allWarnings)
  {
    progressChanged.setWarnings(allWarnings);
  }
}
