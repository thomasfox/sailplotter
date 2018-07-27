package com.github.thomasfox.sailplotter.importer;

import java.io.File;
import java.util.List;

import com.github.thomasfox.sailplotter.model.DataPoint;

/**
 * Reads a file and loads the contained Data into sailplotter.
 */
public interface Importer
{
  /**
   * Reads a file and loads the contained Data into sailplotter.
   *
   * @param file the file to read, not null.
   *
   * @return the data points contained in the file.
   */
  List<DataPoint> read(File file);
}