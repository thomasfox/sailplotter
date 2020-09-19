package com.github.thomasfox.sailplotter.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.github.thomasfox.sailplotter.analyze.Analyzer;
import com.github.thomasfox.sailplotter.exporter.Exporter;
import com.github.thomasfox.sailplotter.gui.component.Menubar;
import com.github.thomasfox.sailplotter.gui.component.SailplotterFrame;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.gui.component.progress.ProgressDialog;
import com.github.thomasfox.sailplotter.gui.component.view.DirectionsView;
import com.github.thomasfox.sailplotter.gui.component.view.InfoView;
import com.github.thomasfox.sailplotter.gui.component.view.MagneticFieldAccelerationView;
import com.github.thomasfox.sailplotter.gui.component.view.Overview;
import com.github.thomasfox.sailplotter.gui.component.view.RelativeToWindView;
import com.github.thomasfox.sailplotter.gui.component.worker.LoadFileWorker;
import com.github.thomasfox.sailplotter.model.Data;

public class SwingGui
{
  private static final String OVERVIEW_VIEW_NAME = "Overview";

  private static final String ANGLES_VIEW_NAME = "Angles";

  private static final String INFO_VIEW_NAME = "Info";

  private static final String RAW_DATA_VIEW_NAME = "Magnetic Field / Acceleration";

  private static final String RELATIVE_TO_WIND_VIEW_NAME = "Relative To Wind";

  private final SailplotterFrame frame;

  private final Menubar menubar;

  private final Overview overview;

  private final DirectionsView directionsView;

  private final InfoView commentsView;

  private final MagneticFieldAccelerationView magneticFieldAccelerationView;

  private final RelativeToWindView relativeToWindView;

  private final ProgressDialog progressDialog;

  private Data data;

  private final JPanel views;

  public boolean inUpdate = false;

  public SwingGui(String filePath)
  {
    overview = new Overview(this);
    directionsView = new DirectionsView(this);
    commentsView = new InfoView(this);
    magneticFieldAccelerationView = new MagneticFieldAccelerationView(this);
    relativeToWindView = new RelativeToWindView(this);

    views = new JPanel(new CardLayout());
    views.add(overview, OVERVIEW_VIEW_NAME);
    views.add(directionsView, ANGLES_VIEW_NAME);
    views.add(commentsView, INFO_VIEW_NAME);
    views.add(magneticFieldAccelerationView, RAW_DATA_VIEW_NAME);
    views.add(relativeToWindView, RELATIVE_TO_WIND_VIEW_NAME);
    views.setPreferredSize(new Dimension(1400, 700));

    frame = new SailplotterFrame();

    File currentFile = Optional.ofNullable(filePath).map(File::new).orElse(null);
    menubar = new Menubar(frame)
        .addLoadFileMenuItem(currentFile, this::loadFile)
        .addSaveFileMenuItem(new Exporter().replaceExtension(currentFile), this::saveFile)
        .addViews(this::changeView,
            OVERVIEW_VIEW_NAME,
            ANGLES_VIEW_NAME,
            INFO_VIEW_NAME,
            RAW_DATA_VIEW_NAME,
            RELATIVE_TO_WIND_VIEW_NAME);
    frame.setJMenuBar(menubar);

    frame.setViews(views);

    frame.setVisible(true);
    frame.pack();

    progressDialog = new ProgressDialog(frame);

    if (filePath != null)
    {
      loadFile(new File(filePath));
    }
    else
    {
      data = new Data();
    }
  }

  public static void main(String[] args)
  {
    if (args.length > 2)
    {
      printUsage();
      return;
    }
    String filename = null;
    if (args.length > 0)
    {
      filename = args[0];
      File file;
      try
      {
        file = new File(filename);
      }
      catch (Exception e)
      {
        printUsage();
        return;
      }
      if (!file.canRead())
      {
        System.out.println("File " + filename + " cannot be read");
        return;
      }
    }
    final String filenameToPass = filename;
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        new SwingGui(filenameToPass);
      }
    });
  }

  private static void printUsage()
  {
    System.out.println("Usage: ${startcommand} [${file}]");
  }

  public void zoomChanged(ZoomChangeEvent e)
  {
    overview.zoomChanged(e);
    directionsView.zoomChanged(e);
    commentsView.zoomChanged(e);
    magneticFieldAccelerationView.zoomChanged(e);
    relativeToWindView.zoomChanged(e);
  }

  public void windDirectionChanged(ActionEvent event)
  {
    String inputValue = event.getActionCommand();
    try
    {
      int newWindDirection = Integer.parseInt(inputValue);
      data.setAverageWindBearing(newWindDirection * Math.PI / 180d);
      Analyzer.analyze(data, new LoadProgress(null));
      dataChanged();
    }
    catch (Exception e)
    {
      System.err.println("Could not update wind direction");
      e.printStackTrace(System.err);
    }
  }

  public void loadFile(File file)
  {
    menubar.setLoadStartFile(file);
    menubar.setSaveStartFile(new Exporter().replaceExtension(file));

    LoadProgress loadProgress = new LoadProgress(progressDialog);
    try
    {
      LoadFileWorker worker = new LoadFileWorker(loadProgress, file, this::setData, frame);
      worker.execute();
    }
    catch (Throwable t)
    {
      t.printStackTrace();
      JOptionPane.showMessageDialog(
          frame,
          "Could not load File: " + t.getClass().getName() + ":" + t.getMessage(),
          "Error loading File",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void setData(Data data)
  {
    this.data = data;
    dataChanged();
    frame.setTitleFromData(data);
  }

  public void dataChanged()
  {
    try
    {
      inUpdate = true;
      overview.dataChanged(data);
      directionsView.dataChanged(data);
      commentsView.dataChanged(data);
      magneticFieldAccelerationView.dataChanged(data);
      relativeToWindView.dataChanged(data);
    }
    finally
    {
      inUpdate = false;
    }
  }

  public void saveFile(File file)
  {
    try
    {
      if (file.exists()) {
        JOptionPane.showMessageDialog(
            frame,
            "File exists" ,
            "Error saving File",
            JOptionPane.ERROR_MESSAGE);
      }
      else
      {
        new Exporter().save(file, data);
        JOptionPane.showMessageDialog(
            frame,
            "File saved: " + file.getName() ,
            "File saved",
            JOptionPane.INFORMATION_MESSAGE);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          frame,
          "Could not save File: " + e.getClass().getName() + ":" + e.getMessage(),
          "Error saving File",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void changeView(String viewName)
  {
    CardLayout cl = (CardLayout)(views.getLayout());
    cl.show(views, viewName);
  }
}