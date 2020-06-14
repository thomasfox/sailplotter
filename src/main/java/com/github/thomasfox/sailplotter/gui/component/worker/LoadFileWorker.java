package com.github.thomasfox.sailplotter.gui.component.worker;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.github.thomasfox.sailplotter.analyze.Analyzer;
import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.importer.FormatAwareImporter;
import com.github.thomasfox.sailplotter.model.Data;

public class LoadFileWorker extends SwingWorker<Data, Void>
{
  private final LoadProgress loadProgress;

  private final File file;

  private final Consumer<Data> dataConsumer;

  private final JFrame frame;

  public LoadFileWorker(LoadProgress loadProgress, File file, Consumer<Data> dataConsumer, JFrame frame)
  {
    this.loadProgress = loadProgress;
    this.file = file;
    this.dataConsumer = dataConsumer;
    this.frame = frame;
  }

  @Override
  protected Data doInBackground() throws Exception
  {
    loadProgress.start();
    Data data = new FormatAwareImporter(loadProgress).read(file);
    loadProgress.analyzingStarted();
    Analyzer.analyze(data, loadProgress);
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
      handleException(e);
    }
    catch (ExecutionException e)
    {
      handleException(e.getCause());
    }
  }

  private void handleException(Throwable t)
  {
    t.printStackTrace();
    JOptionPane.showMessageDialog(
        frame,
        "Could not load File: " + t.getClass().getName() + ":" + t.getMessage(),
        "Error loading File",
        JOptionPane.ERROR_MESSAGE);
  }
}
