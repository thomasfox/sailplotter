package com.github.thomasfox.sailplotter.gui.component.worker;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.github.thomasfox.sailplotter.analyze.Analyzer;
import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.importer.FormatAwareImporter;
import com.github.thomasfox.sailplotter.model.Data;

public class LoadFileWorker extends SwingWorker<Data, Void>
{
  private final LoadProgress loadProgress;

  private final File file;

  private final double windBearing;

  private final Consumer<Data> dataConsumer;

  public LoadFileWorker(LoadProgress loadProgress, File file, double windBearing, Consumer<Data> dataConsumer)
  {
    this.loadProgress = loadProgress;
    this.file = file;
    this.windBearing = windBearing;
    this.dataConsumer = dataConsumer;
  }

  @Override
  protected Data doInBackground() throws Exception
  {
    loadProgress.start();
    Data data = new FormatAwareImporter(loadProgress).read(file);
    loadProgress.analyzingStarted();
    Analyzer.analyze(data, windBearing, loadProgress);
    return data;
  }

  @Override
  public void done()
  {
    loadProgress.finished();
    try
    {
      dataConsumer.accept(get());
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
    catch (ExecutionException e)
    {
      throw new RuntimeException(e);
    }
  }
}
