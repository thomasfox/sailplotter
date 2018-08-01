package com.github.thomasfox.sailplotter.importer;

import java.io.File;

import com.github.thomasfox.sailplotter.model.Data;

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
  Data read(File file);
}