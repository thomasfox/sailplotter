package com.github.thomasfox.sailplotter.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.github.thomasfox.sailplotter.analyze.DeviceOrientationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.LocationInterpolator;
import com.github.thomasfox.sailplotter.analyze.TackListByCorrelationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.TackSeriesAnalyzer;
import com.github.thomasfox.sailplotter.analyze.UseGpsTimeDataCorrector;
import com.github.thomasfox.sailplotter.analyze.VelocityBearingAnalyzer;
import com.github.thomasfox.sailplotter.exporter.Exporter;
import com.github.thomasfox.sailplotter.gui.component.view.CommentsView;
import com.github.thomasfox.sailplotter.gui.component.view.DirectionsView;
import com.github.thomasfox.sailplotter.gui.component.view.Overview;
import com.github.thomasfox.sailplotter.importer.FormatAwareImporter;
import com.github.thomasfox.sailplotter.model.Data;

public class SwingGui
{
  private static final String OVERVIEW_VIEW_NAME = "Overview";

  private static final String ANGLES_VIEW_NAME = "Angles";

  private static final String COMMENTS_VIEW_NAME = "Comments";

  private final JFrame frame;

  private final Menubar menubar;

  private final Overview overview;

  private final DirectionsView directionsView;

  private final  CommentsView commentsView;

  Data data;

  JPanel views;

  double windBearing;

  public boolean inUpdate = false;

  public SwingGui(String filePath, Integer windDirectionInDegrees)
  {
    overview = new Overview(this);
    directionsView = new DirectionsView(this);
    commentsView = new CommentsView(this);

    if (windDirectionInDegrees == null)
    {
      windDirectionInDegrees = 0;
    }
    this.windBearing = 2 * Math.PI * windDirectionInDegrees / 360d;


    views = new JPanel(new CardLayout());
    views.add(overview, OVERVIEW_VIEW_NAME);
    views.add(directionsView, ANGLES_VIEW_NAME);
    views.add(commentsView, COMMENTS_VIEW_NAME);

    frame = new JFrame("SailPlotter");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    File currentFile = Optional.ofNullable(filePath).map(File::new).orElse(null);
    menubar = new Menubar(frame)
        .addLoadFileMenuItem(currentFile, this::loadFile)
        .addSaveFileMenuItem(new Exporter().replaceExtension(currentFile), this::saveFile)
        .addViews(this::changeView,
            OVERVIEW_VIEW_NAME,
            ANGLES_VIEW_NAME,
            COMMENTS_VIEW_NAME);
    frame.setJMenuBar(menubar);

    frame.getContentPane().add(views, BorderLayout.CENTER);

    if (filePath != null)
    {
      loadFile(new File(filePath));
    }
    else
    {
      data = new Data();
    }

    frame.pack();
    frame.setVisible(true);
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
    Integer windDirectionInDegrees = null;
    if (args.length > 1)
    {
      try
      {
        windDirectionInDegrees = Integer.parseInt(args[1]);
      }
      catch (Exception e)
      {
        printUsage();
        return;
      }
    }
    final String filenameToPass = filename;
    final Integer windDirectionInDegreesToPass = windDirectionInDegrees;
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run() {
        new SwingGui(filenameToPass, windDirectionInDegreesToPass);
      }
    });
  }

  private static void printUsage()
  {
    System.out.println("Usage: ${startcommand} ${file} ${windDirectionInDegreees}");
  }

  public void analyze()
  {
    new UseGpsTimeDataCorrector().correct(data);
    new LocationInterpolator().interpolateLocation(data);
    new VelocityBearingAnalyzer().analyze(data, windBearing);
    data.getTackList().clear();
    data.getTackList().addAll(new TackListByCorrelationAnalyzer().analyze(data));
    overview.tackSeriesList = new TackSeriesAnalyzer().analyze(data.getTackList());
    new DeviceOrientationAnalyzer().analyze(data);
  }

  public void redisplay(boolean updateTableContent)
  {
    try
    {
      inUpdate = true;
      overview.redisplay(updateTableContent);
      directionsView.redisplay();
      commentsView.redisplay();
    }
    finally
    {
      inUpdate = false;
    }
  }

  public void zoomPanelStateChanged(ZoomPanelChangeEvent e)
  {
    redisplay(false);
    if (!e.isSource(overview.zoomPanel))
    {
      overview.zoomPanel.setStartIndex(e.getStartIndex(), false);
      overview.zoomPanel.setZoomIndex(e.getZoomPosition(), false);
    }
    if (!e.isSource(directionsView.zoomPanel))
    {
      directionsView.zoomPanel.setStartIndex(e.getStartIndex(), false);
      directionsView.zoomPanel.setZoomIndex(e.getZoomPosition(), false);
    }
  }

  public void windDirectionChanged(ActionEvent event)
  {
    String inputValue = event.getActionCommand();
    try
    {
      int newWindDirection = Integer.parseInt(inputValue);
      this.windBearing = newWindDirection * Math.PI / 180d;
      dataChanged();
      redisplay(true);
    }
    catch (Exception e)
    {
      System.err.println("Could not update wind direction");
      e.printStackTrace(System.err);
    }
  }

  public void loadFile(File file)
  {
    try
    {
      data = new FormatAwareImporter().read(file);
      menubar.setLoadStartFile(file);
      menubar.setSaveStartFile(new Exporter().replaceExtension(file));
      dataChanged();
      redisplay(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          frame,
          "Could not load File: " + e.getClass().getName() + ":" + e.getMessage(),
          "Error loading File",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void dataChanged()
  {
    analyze();
    overview.dataChanged(data);
    directionsView.dataChanged(data);
    commentsView.dataChanged(data);
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

  public int getWindDirectionInDegrees()
  {
    return (int) (this.windBearing / 2d / Math.PI * 360d);

  }
}