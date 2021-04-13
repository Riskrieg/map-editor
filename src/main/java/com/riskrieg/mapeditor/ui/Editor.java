package com.riskrieg.mapeditor.ui;

import com.riskrieg.mapeditor.Constants;
import com.riskrieg.mapeditor.map.graph.Territory;
import com.riskrieg.mapeditor.model.EditMode;
import com.riskrieg.mapeditor.model.EditorModel;
import com.riskrieg.mapeditor.util.ImageUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Editor extends JFrame {

  private JPanel mapPanel;
  private JPanel sidePanel;

  private final JList<Territory> territoryJList;

  private static final int SIDE_BAR_WIDTH_PX = 100;
  private static final int WINDOW_WIDTH = 1280 + SIDE_BAR_WIDTH_PX;
  private static final int WINDOW_HEIGHT = 720 + (int) (((float) 720 / WINDOW_WIDTH) * SIDE_BAR_WIDTH_PX);

  private final EditorModel editorModel;

  public Editor() {
    this.editorModel = new EditorModel(EditMode.NO_EDIT);
    this.territoryJList = new JList<>(editorModel.getTerritoryListModel());
    this.territoryJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // Window generation
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle(Constants.NAME + " Map Editor");

    setJMenuBar(menuBar());
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.add(sidePanel(), BorderLayout.WEST);
    container.add(mapPanel());
    this.add(container);

    pack();
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setResizable(true);
    setLocationRelativeTo(null);
  }

  public Editor(String mapName) {
    this.editorModel = new EditorModel(EditMode.ADD_TERRITORY);
    this.territoryJList = new JList<>(editorModel.getTerritoryListModel());
    this.territoryJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    try {
      this.editorModel.setMapName(mapName);
      this.editorModel.setBaseLayer(ImageIO.read(new File(Constants.MAP_PATH + mapName + "/" + mapName + "-base.png")));
      this.editorModel.setTextLayer(ImageIO.read(new File(Constants.MAP_PATH + mapName + "/" + mapName + "-text.png")));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Window generation
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle(Constants.NAME + " Map Editor");

    setJMenuBar(menuBar());
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.add(sidePanel(), BorderLayout.WEST);
    container.add(mapPanel());
    this.add(container);

    pack();
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    int screenWidth = gd.getDisplayMode().getWidth();
    int screenHeight = gd.getDisplayMode().getHeight();

    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

    if (editorModel.getBaseLayer().isPresent()) {
      BufferedImage baseLayer = editorModel.getBaseLayer().get();
      if (baseLayer.getWidth() < screenWidth || baseLayer.getHeight() < screenHeight) {
        setSize(baseLayer.getWidth() + 145, baseLayer.getHeight() + 75);
      }
    }

    setResizable(true);
    setLocationRelativeTo(null);

    rebuildMapPanel();
    rebuildSidePanel();
  }

  private JMenuBar menuBar() {
    // Creating the menu.
    JMenuBar menuBar = new JMenuBar();
    JMenu menuFile = new JMenu("File");

    JMenuItem miOpenBaseLayer = new JMenuItem(new AbstractAction("Open Base Layer...") {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "png", "jpg", "jpeg");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            editorModel.setMapName(chooser.getSelectedFile().getName().replace(".png", "").replace("-base", ""));
            editorModel.setBaseLayer(ImageIO.read(chooser.getSelectedFile()));
            rebuildMapPanel();
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    JMenuItem miOpenTextLayer = new JMenuItem(new AbstractAction("Open Text Layer...") {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "png", "jpg", "jpeg");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            if (editorModel.getBaseLayer().isEmpty()) {
              JOptionPane.showMessageDialog(null, "You need to import a base map layer before importing your text layer.");
            }

            BufferedImage baseLayer = editorModel.getBaseLayer().get();
            BufferedImage mTextLayer = ImageIO.read(chooser.getSelectedFile());

            if (mTextLayer.getHeight() == baseLayer.getHeight() && mTextLayer.getWidth() == baseLayer.getWidth()) {
              editorModel.setTextLayer(mTextLayer);
              rebuildMapPanel();
            } else {
              JOptionPane.showMessageDialog(null, "Your text layer must match the width and height of your base layer.");
            }
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    JMenuItem miSave = new JMenuItem(new AbstractAction("Export to Json") {
      @Override
      public void actionPerformed(ActionEvent e) {
        JTextArea fileNameArea = new JTextArea();
        fileNameArea.setEditable(true);

        String fileName = JOptionPane.showInputDialog(fileNameArea, "Enter map name:", editorModel.getMapName().orElse(null));
        if (fileName == null || fileName.isEmpty() || editorModel.getBaseLayer().isEmpty() || editorModel.getTextLayer().isEmpty()) {
          JOptionPane.showMessageDialog(null, "Nothing to export.");
          return;
        }
        if (editorModel.save(fileName)) {
          JOptionPane.showMessageDialog(null, "Graph file saved in current directory.");
        } else {
          JOptionPane.showMessageDialog(null, "Error saving map file.");
        }
      }
    });

    JMenu menuEdit = new JMenu("Edit");
    JMenuItem modeAddTerritory = new JMenuItem(new AbstractAction("Mode: Add Territory") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (editorModel.imageLayersPresent()) {
          editorModel.setEditMode(EditMode.ADD_TERRITORY);
          editorModel.clearSelection();
          rebuildSidePanel();
          rebuildMapPanel();
        } else {
          JOptionPane.showMessageDialog(null, "You need to import a base map layer and a text map layer before switching to an editing mode.");
        }
      }
    });

    JMenuItem modeAddNeighbors = new JMenuItem(new AbstractAction("Mode: Add Neighbor") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (editorModel.imageLayersPresent()) {
          editorModel.setEditMode(EditMode.ADD_NEIGHBORS);
          for (Point point : editorModel.getSelectedRegions()) {
            if (editorModel.getBaseLayer().isPresent()) {
              ImageUtil.bucketFill(editorModel.getBaseLayer().get(), point, Constants.TERRITORY_COLOR);
            }
          }
          editorModel.clearActivePoints();
          rebuildSidePanel();
          rebuildMapPanel();
        } else {
          JOptionPane.showMessageDialog(null, "You need to import a base map layer and a text map layer before switching to an editing mode.");
        }
      }
    });

    menuFile.add(miOpenBaseLayer);
    menuFile.add(miOpenTextLayer);
    menuFile.add(miSave);
    menuEdit.add(modeAddTerritory);
    menuEdit.add(modeAddNeighbors);
    menuBar.add(menuFile);
    menuBar.add(menuEdit);
    return menuBar;
  }

  private JScrollPane mapPanel() {
    mapPanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane(mapPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private JPanel sidePanel() {
    sidePanel = new JPanel();
    sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Territories", TitledBorder.CENTER, TitledBorder.CENTER));
    sidePanel.setLayout(new BorderLayout());

    JPanel buttonArea = new JPanel();
    buttonArea.setLayout(new GridLayout(2, 1));

    sidePanel.add(buttonArea, BorderLayout.NORTH);

    JScrollPane territoryScroll = new JScrollPane();
    territoryScroll.getVerticalScrollBar().setUnitIncrement(16);
    territoryScroll.setPreferredSize(new Dimension(SIDE_BAR_WIDTH_PX, this.getHeight()));
    territoryScroll.setViewportBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

    territoryScroll.setViewportView(territoryJList);
    sidePanel.add(territoryScroll, BorderLayout.CENTER);
    return sidePanel;
  }

  // Non-UI

  private void rebuildMapPanel() {
    mapPanel.removeAll();

    JLabel baseLabel = new JLabel();
    baseLabel.setLayout(new BorderLayout());

    if (editorModel.getBaseLayer().isPresent()) {
      editorModel.getSubmitted()
          .forEach(submitted -> submitted.seedPoints().forEach(sp -> ImageUtil.bucketFill(editorModel.getBaseLayer().get(), sp.getLocation(), Constants.SUBMITTED_COLOR)));
      editorModel.getFinished()
          .forEach(finished -> finished.seedPoints().forEach(sp -> ImageUtil.bucketFill(editorModel.getBaseLayer().get(), sp.getLocation(), Constants.FINISHED_COLOR)));
      editorModel.getSelectedNeighbors()
          .forEach(sn -> sn.seedPoints().forEach(sp -> ImageUtil.bucketFill(editorModel.getBaseLayer().get(), sp.getLocation(), Constants.NEIGHBOR_SELECT_COLOR)));
      editorModel.getSelected()
          .ifPresent(selected -> selected.seedPoints().forEach(sp -> ImageUtil.bucketFill(editorModel.getBaseLayer().get(), sp.getLocation(), Constants.SELECT_COLOR)));

      editorModel.setBaseLayer(ImageUtil.convert(editorModel.getBaseLayer().get(), 2));
      baseLabel.setIcon(new ImageIcon(editorModel.getBaseLayer().get()));
    }

    baseLabel.addMouseListener(mapClickListener());

    if (editorModel.getTextLayer().isPresent()) {
      JLabel textLabel = new JLabel();
      textLabel.setIcon(new ImageIcon(editorModel.getTextLayer().get()));

      baseLabel.add(textLabel);
    }

    mapPanel.add(baseLabel);
    mapPanel.repaint();
    mapPanel.revalidate();
  }

  private void rebuildSidePanel() {
    sidePanel.removeAll();

    JPanel buttonArea = new JPanel();
    GridLayout buttonAreaLayout = new GridLayout(2, 1);
    buttonAreaLayout.setVgap(4);
    buttonArea.setLayout(buttonAreaLayout);
    buttonArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 2));

    switch (editorModel.getEditMode()) {
      case NO_EDIT -> sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Territories", TitledBorder.CENTER, TitledBorder.CENTER));
      case ADD_TERRITORY -> {
        sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Add Territories", TitledBorder.CENTER, TitledBorder.CENTER));

        JButton addTerritoryButton = new JButton("+");
        addTerritoryButton.setPreferredSize(new Dimension(10, 25));
        buttonArea.add(addTerritoryButton);
        addTerritoryButton.addMouseListener(addTerritoryButtonListener());

        JButton deleteTerritoryButton = new JButton("-");
        deleteTerritoryButton.setPreferredSize(new Dimension(10, 25));
        buttonArea.add(deleteTerritoryButton);
        deleteTerritoryButton.addMouseListener(removeTerritoryButtonListener());
      }
      case ADD_NEIGHBORS -> {
        sidePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), "Neighbor Select", TitledBorder.CENTER, TitledBorder.CENTER));

        JButton submitNeighborButton = new JButton("Submit");
        submitNeighborButton.setPreferredSize(new Dimension(10, 25));
        buttonArea.add(submitNeighborButton);
        submitNeighborButton.addMouseListener(submitNeighborsButtonListener());
      }
    }

    sidePanel.add(buttonArea, BorderLayout.NORTH);

    JScrollPane territoryScroll = new JScrollPane();
    territoryScroll.getVerticalScrollBar().setUnitIncrement(16);
    territoryScroll.setPreferredSize(new Dimension(SIDE_BAR_WIDTH_PX, this.getHeight()));
    territoryScroll.setViewportBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

    territoryScroll.setViewportView(territoryJList);
    sidePanel.add(territoryScroll, BorderLayout.CENTER);

    sidePanel.repaint();
    sidePanel.revalidate();
  }

  private MouseInputAdapter mapClickListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            Point cursor = new Point(e.getX(), e.getY());
            if (editorModel.getBaseLayer().isPresent()) {
              BufferedImage base = editorModel.getBaseLayer().get();

              switch (editorModel.getEditMode()) {
                case ADD_TERRITORY -> {
                  if (ImageUtil.getPixelColor(base, cursor).equals(Constants.TERRITORY_COLOR)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                      editorModel.selectRegion(ImageUtil.getRootPixel(base, cursor));
                      ImageUtil.bucketFill(base, cursor, Constants.SELECT_COLOR);
                    }
                  } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SELECT_COLOR)) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                      editorModel.deselectRegion(ImageUtil.getRootPixel(base, cursor));
                      ImageUtil.bucketFill(base, cursor, Constants.TERRITORY_COLOR);
                    }
                  }
                }
                case ADD_NEIGHBORS -> {
                  if (editorModel.getSelected().isPresent()) {
                    if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SUBMITTED_COLOR) || ImageUtil.getPixelColor(base, cursor).equals(Constants.FINISHED_COLOR)) {
                      if (e.getButton() == MouseEvent.BUTTON1) {
                        getTerritory(cursor).ifPresent(editorModel::selectNeighbor);
                      }
                    } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.NEIGHBOR_SELECT_COLOR)) {
                      if (e.getButton() == MouseEvent.BUTTON1) {
                        getTerritory(cursor).ifPresent(editorModel::deselectNeighbor);
                      }
                    } else if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SELECT_COLOR)) {
                      if (e.getButton() == MouseEvent.BUTTON1) {
                        editorModel.clearSelection();
                      }
                    }
                  } else {
                    if (ImageUtil.getPixelColor(base, cursor).equals(Constants.SUBMITTED_COLOR)) {
                      if (e.getButton() == MouseEvent.BUTTON1) {
                        getTerritory(cursor).ifPresent(editorModel::select);
                      }
                    } else if (editorModel.getSelectedNeighbors().isEmpty() && ImageUtil.getPixelColor(base, cursor).equals(Constants.FINISHED_COLOR)) {
                      if (e.getButton() == MouseEvent.BUTTON1) {
                        getTerritory(cursor).ifPresent(editorModel::select);
                      }
                    }
                  }
                }
                case NO_EDIT -> {
                }
              }

            }
            rebuildMapPanel();
          }
        }
      }
    };
  }

  private MouseInputAdapter addTerritoryButtonListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            JTextArea nameArea = new JTextArea();
            nameArea.setEditable(true);

            String name = JOptionPane.showInputDialog(nameArea, "Enter territory name:");
            if (name == null || name.isEmpty()) {
              JOptionPane.showMessageDialog(null, "You did not enter a name so no changes were made.");
              return;
            }
            if (editorModel.getSelectedRegions().isEmpty()) {
              JOptionPane.showMessageDialog(null, "You need to select a region or set of regions to constitute a territory.");
              return;
            }
            Territory newlySubmitted = new Territory(name, new HashSet<>(editorModel.getSelectedRegions()));
            editorModel.submitTerritory(newlySubmitted);
            editorModel.clearActivePoints();
            rebuildMapPanel();
          }
        }
      }
    };
  }

  private MouseInputAdapter removeTerritoryButtonListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            Territory selected = territoryJList.getSelectedValue();
            if (selected == null) {
              JOptionPane.showMessageDialog(null, "You need to select a territory to remove from the list.");
              return;
            }
            if (editorModel.getBaseLayer().isPresent()) {
              for (Point point : selected.seedPoints()) {
                ImageUtil.bucketFill(editorModel.getBaseLayer().get(), point, Constants.TERRITORY_COLOR);
              }
            }
            editorModel.removeSubmittedTerritory(selected);
            rebuildMapPanel();
          }
        }
      }
    };
  }

  private MouseInputAdapter submitNeighborsButtonListener() {
    return new MouseInputAdapter() {

      boolean pressed = false;

      @Override
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          if (new Rectangle(e.getComponent().getLocationOnScreen(), e.getComponent().getSize()).contains(e.getLocationOnScreen())) {
            pressed = false;
            if (editorModel.getSelected().isPresent() && !editorModel.getSelectedNeighbors().isEmpty()) {
              editorModel.submitNeighbors();
            }
            rebuildMapPanel();
          }
        }
      }
    };
  }

  // Utility

  private Optional<Territory> getTerritory(Point point) {
    if (editorModel.getBaseLayer().isPresent()) {
      Point rootPoint = ImageUtil.getRootPixel(editorModel.getBaseLayer().get(), point);
      for (Territory territory : editorModel.getSubmitted()) {
        if (territory.seedPoints().contains(rootPoint)) {
          return Optional.of(territory);
        }
      }
    }
    return Optional.empty();
  }

}
