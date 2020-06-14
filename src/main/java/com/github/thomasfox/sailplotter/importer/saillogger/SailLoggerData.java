package com.github.thomasfox.sailplotter.importer.saillogger;

import java.util.ArrayList;

/**
 * Model of the data stored in a saillog file
 */
public final class SailLoggerData
{
  public SailLoggerStart start;
  public ArrayList<SailLoggerTrackPoint> track = new ArrayList<>();
  public SailLoggerEnd end;
}