package com.github.thomasfox.sailplotter.importer.saillogger;

public final class SailLoggerStart
{
  /** Contains the version of the sailLogger data format used to write the data. */
  public String format;
  /** Contains the name of the program which logged the data. */
  public String loggedBy;
  /** Contains the version of the program which logged the data. */
  public String loggedByVersion;
  /** The manufacturer of the device used to record the data */
  public String recordedByManufactorer;
  /** The model name of the device used to record the data */
  public String recordedByModel;
  /** The time when recording started (typically using the hardware time of the recording device) */
  public long startT;
  /** The time when recording started, in a human readable format (dd.MM.yyyy' 'HH:mm:ss.SSSZ) */
  public String startTFormatted;
}