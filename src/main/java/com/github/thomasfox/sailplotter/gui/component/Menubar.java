package com.github.thomasfox.sailplotter.gui.component;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class Menubar extends JMenuBar
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final JMenu fileMenu = new JMenu("File");

  private final JMenu viewMenu = new JMenu("View");

  private final JFrame applicationFrame;

  private final JFileChooser fileChooser = new JFileChooser();

  private Consumer<File> loadFileConsumer;

  private Consumer<File> saveFileConsumer;

  private File loadStartFile;

  private File saveStartFile;

  private Consumer<String> viewChangedConsumer;

  /**
   * Constructor for the menubar of the application.
   *
   * @param applicationFrame The application's main window, not null.
   */
  public Menubar(JFrame applicationFrame)
  {
    this.applicationFrame = applicationFrame;
    fileMenu.setMnemonic(KeyEvent.VK_F);
    add(fileMenu);
    viewMenu.setMnemonic(KeyEvent.VK_V);
    add(viewMenu);
  }

  /**
   * Adds a load file menu item.
   *
   * @param startFile the file which is selected by the fileChooser  at the start.
   * @param loadFileConsumer the function which is called whenever the user opens a file.
   */
  public Menubar addLoadFileMenuItem(File startFile, Consumer<File> loadFileConsumer)
  {
    this.loadFileConsumer = loadFileConsumer;
    if (startFile != null)
    {
      loadStartFile = startFile;
    }

    JMenuItem loadFile = new JMenuItem("load", KeyEvent.VK_L);
    loadFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
    loadFile.addActionListener(this::loadFile);
    fileMenu.add(loadFile);
    return this;
  }

  public void loadFile(ActionEvent e)
  {
    if (loadStartFile != null)
    {
      fileChooser.setSelectedFile(loadStartFile);
    }
    int returnVal = fileChooser.showOpenDialog(applicationFrame);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile();
      loadFileConsumer.accept(file);
    }
  }

  public void setLoadStartFile(File loadStartFile)
  {
    this.loadStartFile = loadStartFile;
  }

  /**
   * Adds a save file menu item.
   *
   * @param startFile the file which is selected by the fileChooser  at the start.
   * @param saveFileConsumer the function which is called whenever the user specifies a file to save to.
   */
  public Menubar addSaveFileMenuItem(File startFile, Consumer<File> saveFileConsumer)
  {
    this.saveFileConsumer = saveFileConsumer;
    if (startFile != null)
    {
      saveStartFile = startFile;
    }

    JMenuItem saveFile = new JMenuItem("save", KeyEvent.VK_S);
    saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
    saveFile.addActionListener(this::saveFile);
    fileMenu.add(saveFile);
    return this;
  }

  public void saveFile(ActionEvent e)
  {
    if (saveStartFile != null)
    {
      fileChooser.setSelectedFile(saveStartFile);
    }
    int returnVal = fileChooser.showOpenDialog(applicationFrame);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile();
      saveFileConsumer.accept(file);
    }
  }

  public void setSaveStartFile(File saveStartFile)
  {
    this.saveStartFile = saveStartFile;
  }

  public Menubar addViews(Consumer<String> viewChangedConsumer, String... viewNames)
  {
    for (String viewName : viewNames)
    {
      JMenuItem viewMenuItem = new JMenuItem(viewName);
      viewMenuItem.addActionListener(this::viewChanged);
      viewMenu.add(viewMenuItem);
    }
    this.viewChangedConsumer = viewChangedConsumer;
    return this;
  }

  public void viewChanged(ActionEvent e)
  {
    JMenuItem selectedViewItem = (JMenuItem) e.getSource();
    viewChangedConsumer.accept(selectedViewItem.getText());
  }
}
