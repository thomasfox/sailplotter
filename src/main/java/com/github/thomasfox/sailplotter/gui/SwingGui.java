package com.github.thomasfox.sailplotter.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.analyze.TackListByCorrelationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.VelocityBearingAnalyzer;
import com.github.thomasfox.sailplotter.importer.ViewRangerImporter;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Tack;

public class SwingGui implements ZoomPanelChangeListener, ListSelectionListener
{
  private final ZoomPanel zoomPanel;

  private final TimeSeriesCollection fullVelocityBearingOverTimeDataset = new TimeSeriesCollection();

  private final TimeSeriesCollection zoomedVelocityBearingOverTimeDataset = new TimeSeriesCollection();

  SimpleHistogramDataset bearingHistogramDataset;

  List<SimpleHistogramBin> bearingHistogramBins = new ArrayList<>();

  XYSeriesCollection velocityBearingPolar = new XYSeriesCollection();

  XYSeriesCollection tackVelocityBearingPolar = new XYSeriesCollection();

  XYSeriesCollection xyDataset = new XYSeriesCollection();

  XYSeriesCollection zoomXyDataset = new XYSeriesCollection();

  XYPlot zoomXyPlot;

  List<DataPoint> data;

  List<Tack> tacks;

  JTable tacksTable;

  double windBearing;

  public SwingGui(String filePath, int windDirectionInDegrees)
  {
    this.windBearing = 2 * Math.PI * windDirectionInDegrees / 360d;
    data = getData(filePath);
    new VelocityBearingAnalyzer().analyze(data, windBearing);
    zoomPanel = new ZoomPanel(data.size());

    JFrame frame = new JFrame("SailPlotter");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new GridLayout(0,3));

    updateFullVelocityBearingOverTimeDataset();
    JFreeChart fullVelocityBearingOverTimeChart = ChartFactory.createTimeSeriesChart("Velocity and Bearing (Full)", "Time", "Velocity [kts] / Bearing [arcs]", fullVelocityBearingOverTimeDataset, false, false, false);
    XYPlot fullVelocityBearingOverTimePlot = (XYPlot) fullVelocityBearingOverTimeChart.getPlot();
    Range dataRange = new DateRange(data.get(0).time, data.get(data.size() -1).time);
    fullVelocityBearingOverTimePlot.getDomainAxis().setRange(dataRange);
    Range valueRange = new DateRange(0, getMaximum(data, DataPoint::getVelocity));
    fullVelocityBearingOverTimePlot.getRangeAxis().setRange(valueRange);
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(3, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(4, new Color(0x00, 0xFF, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(5, new Color(0x00, 0x00, 0x00));
    ChartPanel fullVelocityBearingOverTimeChartPanel = new ChartPanel(fullVelocityBearingOverTimeChart);
    frame.getContentPane().add(fullVelocityBearingOverTimeChartPanel);

    updateZoomedVelocityBearingOverTimeDataset();
    JFreeChart zoomedVelocityBearingOverTimeChart = ChartFactory.createTimeSeriesChart("Velocity and Bearing (Zoom)", "Time", "Velocity [kts] / Bearing [arcs]", zoomedVelocityBearingOverTimeDataset, false, false, false);
    XYPlot zoomedVelocityBearingOverTimePlot = (XYPlot) zoomedVelocityBearingOverTimeChart.getPlot();
    zoomedVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    zoomedVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(1, new Color(0x00, 0xFF, 0x00));
    ((XYLineAndShapeRenderer) zoomedVelocityBearingOverTimePlot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) zoomedVelocityBearingOverTimePlot.getRenderer()).setSeriesShapesVisible(1, true);
    ChartPanel zoomedvelocityBearingOverTimeChartPanel = new ChartPanel(zoomedVelocityBearingOverTimeChart);
    frame.getContentPane().add(zoomedvelocityBearingOverTimeChartPanel);

    bearingHistogramDataset = new SimpleHistogramDataset("Relative Bearing");
    bearingHistogramDataset.setAdjustForBinSize(false);
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      SimpleHistogramBin bin = new SimpleHistogramBin(
          (i * 360d / Constants.NUMBER_OF_BEARING_BINS) - 180d,
          ((i + 1) * 360d / Constants.NUMBER_OF_BEARING_BINS) - 180d,
          true,
          false);
      bearingHistogramBins.add(bin);
      bearingHistogramDataset.addBin(bin);
    }
    updateBearingHistogramDataset();
    JFreeChart bearingHistogramChart = ChartFactory.createHistogram("Relative Bearing", "Relative Bearing [°]", "Occurances",  bearingHistogramDataset, PlotOrientation.VERTICAL, false, false, false);
    ChartPanel bearingChartPanel = new ChartPanel(bearingHistogramChart);
    frame.getContentPane().add(bearingChartPanel);

    updateVelocityBearingPolar();
    JFreeChart chart = ChartFactory.createPolarChart("Velocity over Relative Bearing", velocityBearingPolar, false, false, false);
    ChartPanel chartPanel = new ChartPanel(chart);
    frame.getContentPane().add(chartPanel);

    zoomPanel.addListener(this);

    frame.getContentPane().add(zoomPanel);

    updateXyDataset();
    JFreeChart xyChart = ChartFactory.createXYLineChart("Sail Map", "X", "Y", xyDataset, PlotOrientation.VERTICAL, false, false, false);
    XYPlot xyPlot = (XYPlot) xyChart.getPlot();
    Range xRange = new Range(
        getMinimum(data, DataPoint::getX) - data.get(0).getX(),
        getMaximum(data, DataPoint::getX) - data.get(0).getX());
    xyPlot.getDomainAxis().setRange(xRange);
    Range yRange = new Range(
        getMinimum(data, DataPoint::getY) - data.get(0).getY(),
        getMaximum(data, DataPoint::getY) - data.get(0).getY());
    xyPlot.getRangeAxis().setRange(yRange);
    expandRangesToAspectRatio(xyPlot, Constants.MAP_ASPECT_RATIO);
    xyPlot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    xyPlot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    xyPlot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    ChartPanel xyChartPanel = new ChartPanel(xyChart);
    frame.getContentPane().add(xyChartPanel);

    tacks = new TackListByCorrelationAnalyzer().analyze(data);
    DefaultTableModel model = new DefaultTableModel(
        new String[] {
            "Point of Sail",
            "length (m)",
            "duration (sec)",
            "Average relative Bearing (degrees)",
            "Average Speed (knots)",
            "Maneuver at Start",
            "Maneuver loss at Start (sec)",
            "Maneuver at End"},
        0);

    Tack lastTack = null;
    for (Tack tack : tacks)
    {
      model.addRow(new Object[] {
          tack.pointOfSail,
          new DecimalFormat("0").format(tack.getLength()),
          new DecimalFormat("0.0").format(tack.getDuration() / 1000d),
          new DecimalFormat("0").format(tack.getAverageRelativeBearingInDegrees()),
          new DecimalFormat("0.0").format(tack.getAverageVelocityInKnots()),
          tack.maneuverTypeAtStart == null ? "" : tack.maneuverTypeAtStart.toString(),
          (lastTack == null
           || lastTack.tackStraightLineIntersectionEnd == null
           || tack.tackStraightLineIntersectionStart == null
           || lastTack.tackStraightLineIntersectionEnd.time == null
           || tack.tackStraightLineIntersectionStart.time == null)
            ? ""
            : new DecimalFormat("0.0").format(
                (tack.tackStraightLineIntersectionStart.time - lastTack.tackStraightLineIntersectionEnd.time)
                / 1000d),
          tack.maneuverTypeAtEnd == null ? "" : tack.maneuverTypeAtEnd.toString()});
      lastTack = tack;
    }
    tacksTable = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(tacksTable);
    tacksTable.setFillsViewportHeight(true);
    tacksTable.getSelectionModel().addListSelectionListener(this);
    frame.getContentPane().add(scrollPane);

    updateTackVelocityBearingPolar();
    JFreeChart tackVelocityBearingChart = ChartFactory.createPolarChart("Tack Velocity over Relative Bearing", tackVelocityBearingPolar, false, false, false);
    PolarPlot tackVelocityBearingPlot = (PolarPlot) tackVelocityBearingChart.getPlot();
    tackVelocityBearingPlot.setRenderer(new PolarScatterRenderer());
    ChartPanel tackVelocityBearingChartPanel = new ChartPanel(tackVelocityBearingChart);
    frame.getContentPane().add(tackVelocityBearingChartPanel);


    updateZoomXyDataset();
    JFreeChart zoomXyChart = ChartFactory.createXYLineChart("Sail Map Zoom", "X", "Y", zoomXyDataset, PlotOrientation.VERTICAL, false, true, false);
    zoomXyPlot = (XYPlot) zoomXyChart.getPlot();
    updateZoomXyRange();
    zoomXyPlot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    ((XYLineAndShapeRenderer) zoomXyPlot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) zoomXyPlot.getRenderer()).setBaseToolTipGenerator(new XYTooltipFromLabelGenerator());
    ChartPanel zoomXyChartPanel = new ChartPanel(zoomXyChart);
    frame.getContentPane().add(zoomXyChartPanel);

    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args)
  {
    if (args.length != 2)
    {
      printUsage();
      return;
    }
    String filename = args[0];
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
    int windDirectionInDegreees;
    try
    {
      windDirectionInDegreees = Integer.parseInt(args[1]);
    }
    catch (Exception e)
    {
      printUsage();
      return;
    }
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run() {
        new SwingGui(filename, windDirectionInDegreees);
      }
    });
  }

  private static void printUsage()
  {
    System.out.println("Usage: ${startcommand} ${file} ${windDirectionInDegreees}");
  }

  private List<DataPoint> getData(String filePath)
  {
    File file = new File(filePath);
    List<DataPoint> data = new ViewRangerImporter().read(file);
    return data;
  }

  private void updateFullVelocityBearingOverTimeDataset()
  {
    fullVelocityBearingOverTimeDataset.removeAllSeries();
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.BEFORE));
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.IN));
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.AFTER));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.BEFORE));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.IN));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.AFTER));
  }

  private void updateZoomedVelocityBearingOverTimeDataset()
  {
    zoomedVelocityBearingOverTimeDataset.removeAllSeries();
    zoomedVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.IN));
    zoomedVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.IN));
  }

  public static TimeSeries getLatitudeTimeSeries(List<DataPoint> data)
  {
    TimeSeries series = new TimeSeries("latitude");
    for (DataPoint point: data)
    {
      series.addOrUpdate(point.getMillisecond(), point.latitude);
    }
    return series;
  }

  private double getMaximum(List<DataPoint> data, Function<DataPoint, Double> pointFunction)
  {
    double maxValue = Double.MIN_VALUE;
    for (DataPoint dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue > maxValue)
      {
        maxValue = pointValue;
      }
    }
    return maxValue;
  }

  private double getMinimum(List<DataPoint> data, Function<DataPoint, Double> pointFunction)
  {
    double minValue = Double.MAX_VALUE;
    for (DataPoint dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue < minValue)
      {
        minValue = pointValue;
      }
    }
    return minValue;
  }

  public TimeSeries getVelocityTimeSeries(List<DataPoint> data, TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("velocity");
    for (DataPoint point : getSubset(data, position))
    {
      series.addOrUpdate(point.getMillisecond(), point.velocity);
    }
    return series;
  }

  public TimeSeries getBearingTimeSeries(List<DataPoint> data, TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("bearing");
    for (DataPoint point : getSubset(data, position))
    {
      series.addOrUpdate(point.getMillisecond(), point.bearing);
    }
    return series;
  }

  public TimeSeries getZoomDisplaySeries(List<DataPoint> data)
  {
    Millisecond startValue = data.get(getDataStartIndex(data)).getMillisecond();
    Millisecond endValue = data.get(getDataEndIndex(data)).getMillisecond();
    TimeSeries series = new TimeSeries("velocity");
    series.addOrUpdate(startValue, 2);
    series.addOrUpdate(endValue, 2);
    return series;
  }

  public int getDataStartIndex(List<DataPoint> data)
  {
    return zoomPanel.getStartIndex();
  }

  public int getDataEndIndex(List<DataPoint> data)
  {
    int zoom = zoomPanel.getZoomIndex();
    int startIndex = getDataStartIndex(data);
    int result = startIndex + zoom * (data.size() - 1) / Constants.NUMER_OF_ZOOM_TICKS;
    result = Math.min(result, (data.size() - 1));
    return result;
  }

  public LocalDateTime getDataStartTime()
  {
    return data.get(getDataStartIndex(data)).getLocalDateTime();
  }

  public LocalDateTime getDataEndTime()
  {
    return data.get(getDataEndIndex(data)).getLocalDateTime();
  }

  public void updateBearingHistogramDataset()
  {
    for (SimpleHistogramBin bin : bearingHistogramBins)
    {
      bin.setItemCount(0);
    }
    for (DataPoint point : data)
    {
      if (point.getLocalDateTime().isAfter(getDataStartTime())
          && point.getLocalDateTime().isBefore(getDataEndTime()))
      {
        if (point.bearing != null)
        {
          bearingHistogramDataset.addObservation(point.getRelativeBearingAs360Degrees());
        }
      }
    }
  }

  public void updateVelocityBearingPolar()
  {
    List<List<Double>> velocityBuckets = new ArrayList<>(Constants.NUMBER_OF_BEARING_BINS);
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      velocityBuckets.add(new ArrayList<Double>());
    }
    for (DataPoint point : data)
    {
      if (point.getLocalDateTime().isAfter(getDataStartTime())
          && point.getLocalDateTime().isBefore(getDataEndTime()))
      {
        if (point.bearing != null && point.velocity != null)
        {
          int bucket = new Double(point.getRelativeBearingInArcs() * Constants.NUMBER_OF_BEARING_BINS / 2 / Math.PI).intValue();
          velocityBuckets.get(bucket).add(point.velocity);
        }
      }
    }
    XYSeries maxVelocity = new XYSeries("maxVelocity");
    XYSeries medianVelocity = new XYSeries("medianVelocity");
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      List<Double> velocityDistribution = velocityBuckets.get(i);
      Collections.sort(velocityDistribution);
      if (!velocityDistribution.isEmpty())
      {
        maxVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, velocityDistribution.get(velocityDistribution.size() - 1));
      }
      else
      {
        maxVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, 0);
      }
      if (velocityDistribution.size() >= 1)
      {
        medianVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, velocityDistribution.get(velocityDistribution.size() / 2));
      }
      else
      {
        medianVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, 0);
      }
    }
    velocityBearingPolar.removeAllSeries();
    velocityBearingPolar.addSeries(maxVelocity);
    velocityBearingPolar.addSeries(medianVelocity);
  }

  public void updateTackVelocityBearingPolar()
  {
    XYSeries tackVelocity = new XYSeries("tackVelocity", false, true);
    for (Tack tack : tacks)
    {
      if (tack.end.getLocalDateTime().isAfter(getDataStartTime())
          && tack.start.getLocalDateTime().isBefore(getDataEndTime()))
      {
        if (tack.getAverageRelativeBearingInDegrees() != null && tack.getAverageVelocityInKnots() != null)
        {
          tackVelocity.add(tack.getAverageRelativeBearingInDegrees(), tack.getAverageVelocityInKnots());
        }
      }
    }
    tackVelocityBearingPolar.removeAllSeries();
    tackVelocityBearingPolar.addSeries(tackVelocity);
  }

  private boolean isInSelectedPosition(DataPoint point, TimeWindowPosition position)
  {
    if (position == TimeWindowPosition.BEFORE && point.getLocalDateTime().isAfter(getDataStartTime()))
    {
      return false;
    }
    if (position == TimeWindowPosition.IN
        && (!point.getLocalDateTime().isAfter(getDataStartTime())
            || !point.getLocalDateTime().isBefore(getDataEndTime())))
    {
      return false;
    }
    if (position == TimeWindowPosition.AFTER && point.getLocalDateTime().isBefore(getDataEndTime()))
    {
      return false;
    }
    return true;
  }

  private void updateXyDataset()
  {
    xyDataset.removeAllSeries();
    xyDataset.addSeries(getXySeries(data, TimeWindowPosition.BEFORE, data.get(0).getX(), data.get(0).getY()));
    xyDataset.addSeries(getXySeries(data, TimeWindowPosition.IN, data.get(0).getX(), data.get(0).getY()));
    xyDataset.addSeries(getXySeries(data, TimeWindowPosition.AFTER, data.get(0).getX(), data.get(0).getY()));
  }

  private void updateZoomXyDataset()
  {
    zoomXyDataset.removeAllSeries();
    zoomXyDataset.addSeries(getXySeries(data, TimeWindowPosition.IN, data.get(0).getX(), data.get(0).getY()));
    zoomXyDataset.addSeries(getTackIntersectionSeries(tacks, TimeWindowPosition.IN, data.get(0).getX(), data.get(0).getY()));
  }

  private void updateZoomXyRange()
  {
    List<DataPoint> dataSubset = getSubset(data, TimeWindowPosition.IN);
    double minimumX = getMinimum(dataSubset, DataPoint::getX);
    double maximumX = getMaximum(dataSubset, DataPoint::getX);
    Range zoomXRange = new Range(
        minimumX - data.get(0).getX() - (maximumX - minimumX) * 0.1,
        maximumX - data.get(0).getX() + (maximumX - minimumX) * 0.1);
    double minimumY = getMinimum(dataSubset, DataPoint::getY);
    double maximumY = getMaximum(dataSubset, DataPoint::getY);
    Range zoomYRange = new Range(
        minimumY - data.get(0).getY() - (maximumY - minimumY) * 0.1,
        maximumY - data.get(0).getY() + (maximumY - minimumY) * 0.1);

    zoomXyPlot.getDomainAxis().setRange(zoomXRange);
    zoomXyPlot.getRangeAxis().setRange(zoomYRange);
    expandRangesToAspectRatio(zoomXyPlot, Constants.MAP_ASPECT_RATIO);
  }

  public void expandRangesToAspectRatio(XYPlot plot, double aspectRatio)
  {
    Range xRange = plot.getDomainAxis().getRange();
    Range yRange = plot.getRangeAxis().getRange();
    if (xRange.getLength() > aspectRatio * yRange.getLength())
    {
      yRange = new Range(
          yRange.getCentralValue() - 0.5d * xRange.getLength() / aspectRatio,
          yRange.getCentralValue() + 0.5d * xRange.getLength() / aspectRatio);
      plot.getRangeAxis().setRange(yRange);
    }
    else
    {
      xRange = new Range(
          xRange.getCentralValue() - 0.5d * yRange.getLength() * aspectRatio,
          xRange.getCentralValue() + 0.5d * yRange.getLength() * aspectRatio);
      plot.getDomainAxis().setRange(xRange);
    }
  }

  public XYSeries getXySeries(List<DataPoint> data, TimeWindowPosition position, double xOffset, double yOffset)
  {
    XYSeries series = new XYSeries("XY", false, true);
    for (DataPoint point : getSubset(data, position))
    {
      series.add(new XYLabeledDataItem(point.getX() - xOffset, point.getY() - yOffset, point.getXYLabel()));
    }
    return series;
  }

  public XYSeries getTackIntersectionSeries(List<Tack> tacks, TimeWindowPosition position, double xOffset, double yOffset)
  {
    XYSeries series = new XYSeries("XY", false, true);
    for (Tack tack : tacks)
    {
      if (!isInSelectedPosition(tack.start, position) && !isInSelectedPosition(tack.end, position))
      {
        continue;
      }
      if (tack.tackStraightLineIntersectionStart != null && tack.tackStraightLineIntersectionEnd != null)
      {
        series.add(
            tack.tackStraightLineIntersectionStart.getX() - xOffset,
            tack.tackStraightLineIntersectionStart.getY() - yOffset);
        series.add(
            tack.tackStraightLineIntersectionEnd.getX() - xOffset,
            tack.tackStraightLineIntersectionEnd.getY() - yOffset);
      }
    }
    return series;
  }

  List<DataPoint> getSubset(List<DataPoint> data, TimeWindowPosition position)
  {
    List<DataPoint> result = new ArrayList<>();
    for (DataPoint point : data)
    {
      if (!isInSelectedPosition(point, position))
      {
        continue;
      }

      result.add(point);
    }
    return result;
  }


  @Override
  public void stateChanged(ZoomPanelChangeEvent e)
  {
    updateFullVelocityBearingOverTimeDataset();
    updateZoomedVelocityBearingOverTimeDataset();
    updateBearingHistogramDataset();
    updateVelocityBearingPolar();
    updateXyDataset();
    updateTackVelocityBearingPolar();
    updateZoomXyDataset();
    updateZoomXyRange();
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting())
    {
      return;
    }
    ListSelectionModel model = tacksTable.getSelectionModel();
    int index = model.getAnchorSelectionIndex();
    Tack tack = tacks.get(index);
    zoomPanel.setStartIndex(Math.max(tack.startIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
    zoomPanel.setZoomIndex(Math.min(
        Math.max(
            Constants.NUMER_OF_ZOOM_TICKS * (tack.endIndex - tack.startIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (data.size()),
            3),
        Constants.NUMER_OF_ZOOM_TICKS));
  }
}