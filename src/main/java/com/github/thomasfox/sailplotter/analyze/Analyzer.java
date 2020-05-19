package com.github.thomasfox.sailplotter.analyze;

import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.model.Data;

public class Analyzer
{
  public static void analyze(Data data, LoadProgress loadProgress)
  {
    loadProgress.startCorrectTimeUsingGpsTime();
    new UseGpsTimeDataCorrector().correct(data);
    loadProgress.startInterpolatingLocation();
    new LocationInterpolator().interpolateLocation(data);
    loadProgress.startCalculateLocationAndBearing();
    new VelocityBearingAnalyzer().analyze(data);
    loadProgress.startCalculateTackList();
    data.getTackList().clear();
    data.getTackList().addAll(new TackListByCorrelationAnalyzer().analyze(data));
    loadProgress.startCalculateTackSeriesList();
    data.getTackSeriesList().clear();
    data.getTackSeriesList().addAll(new TackSeriesAnalyzer().analyze(data.getTackList()));
    loadProgress.startAnalyzeOrientation();
    new DeviceOrientationAnalyzer().analyze(data, loadProgress);
  }
}
