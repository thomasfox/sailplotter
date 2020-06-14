package com.github.thomasfox.sailplotter.importer.saillogger;

public final class SailLoggerEnd
{
  /** The time when recording ended (typically using the hardware time of the recording device) */
  public long endT;
  /** The time when recording started, in a human readable format (dd.MM.yyyy' 'HH:mm:ss.SSSZ) */
  public String endTFormatted;
}