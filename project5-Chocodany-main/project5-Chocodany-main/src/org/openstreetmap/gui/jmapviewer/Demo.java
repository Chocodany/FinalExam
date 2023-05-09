package org.openstreetmap.gui.jmapviewer;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Demo extends JFrame implements JMapViewerEventListener {

    private static final long serialVersionUID = 1L;

    private final JMapViewerTree treeMap;
    static JComboBox<String> comboBox;
    private final JLabel zoomLabel;
    private final JLabel zoomValue;

    private final JLabel mperpLabelName;
    private final JLabel mperpLabelValue;

	private List<Coordinate> coordinates;

	private int currentIndex;

    private static final int ANIMATION_DURATION = 5000; 
    
    private static int TIMER_DELAY = 50;
    private static final int LINE_WIDTH = 3;
	private Image image;
	
    public Demo() {
    	
        super("Project 5 - Daniela Sanchez");
        setSize(400, 400);

        treeMap = new JMapViewerTree("Zones");

        map().addJMVListener(this);

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        JPanel helpPanel = new JPanel();

        mperpLabelName = new JLabel("");
        mperpLabelValue = new JLabel(String.format("%s", map().getMeterPerPixel()));

        zoomLabel = new JLabel("");
        zoomValue = new JLabel(String.format("%s", map().getZoom()));

        add(panel, BorderLayout.NORTH);
        add(helpPanel, BorderLayout.SOUTH);
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelBottom, BorderLayout.SOUTH);
       
        JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] {
                new OsmTileSource.Mapnik(),
                new OsmTileSource.TransportMap(),
        });
        
        
        tileSourceSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileSource((TileSource) e.getItem());
            }
        });
        
        JComboBox<TileLoader> tileLoaderSelector;
        tileLoaderSelector = new JComboBox<>(new TileLoader[] {new OsmTileLoader(map())});
        tileLoaderSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileLoader((TileLoader) e.getItem());
            }
        });
        map().setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
        panelTop.add(tileSourceSelector);
        
        //Play Button
        JButton button1 = new JButton(); 
        button1.setBounds(600,15,60,25);
        button1.setText("Play");
        button1.setFocusable(false);
        panelTop.add(button1);
        
        //Stop CheckBox
        JCheckBox check = new JCheckBox();
        check.setBounds(450,15,100,25);
        check.setText("Include Stops");
        check.setFocusable(false);
        panelTop.add(check);
        
        //ComboBox Animation Time
        String[] speed = {"Animation Time", "15", "30", "60", "90"};
        comboBox = new JComboBox<String>(speed);
        comboBox.setVisible(true);
        panelTop.add(comboBox);
        comboBox.setBounds(100,15,100,25);

        //Map Center
        add(treeMap, BorderLayout.CENTER);
        Coordinate lol = new Coordinate(35.0078,-97.0929);
        map().setDisplayPosition(lol,5);
        //
        LayerGroup germanyGroup = new LayerGroup("Oklahoma");
        Layer germanyWestLayer = germanyGroup.addLayer("Oklahoma West");
        Layer germanyEastLayer = germanyGroup.addLayer("Oklahoma East");
        
        
        treeMap.addLayer(germanyWestLayer);
        treeMap.addLayer(germanyEastLayer);
        
        //Read File
        String csvFile = "triplog.csv";
        String line = "";
        String cvsSplitBy = ",";
        List<Coordinate> coordinates = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] coordStr = line.split(cvsSplitBy);
                if (!line.contains("Time")) {
                	double lat = Double.parseDouble(coordStr[1]);
                    double lon = Double.parseDouble(coordStr[2]);
                    coordinates.add(new Coordinate(lat, lon));
    				}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        map().addMapPolygon(new MapPolygonImpl(new Coordinate[0]));
        
        
        int delayTime;
        
        if (comboBox.getSelectedItem() == "15") {
        	delayTime = 15;
        } else if (comboBox.getSelectedItem() == "30"){
        		delayTime = 30;
        } else if (comboBox.getSelectedItem() == "60") {
        	delayTime = 60;
        } else if (comboBox.getSelectedItem() == "90") {
        	delayTime = 90;
        } else { 
        	delayTime = 15;
        }
        //Line Animation
        Timer timer = new Timer(delayTime, new ActionListener() {
            
        	@Override
            public void actionPerformed(ActionEvent e) {
        		
                currentIndex++;
                if (currentIndex >= coordinates.size()) {
                    ((Timer) e.getSource()).stop();
                    return;
                }
                
                map().removeAllMapMarkers();
                map().removeAllMapPolygons();
                map().addMapMarker(new MapMarkerDot(coordinates.get(currentIndex)));
                map().addMapPolygon(new MapPolygonImpl(coordinates.subList(0, currentIndex + 1).toArray(new Coordinate[currentIndex + 1])));
            
            }
        });

        
        button1.addActionListener(e -> timer.start());
        //comboBox.addActionListener(e ->delayTime);
        
        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    map().getAttribution().handleAttribution(e.getPoint(), true);
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                 }
        });
    }
    
    private JMapViewer map() {
        return treeMap.getViewer();
    }

    public static void main(String[] args) {
        new Demo().setVisible(true);
    }

    private void updateZoomParameters() {
        if (mperpLabelValue != null)
            mperpLabelValue.setText(String.format("%s", map().getMeterPerPixel()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", map().getZoom()));
    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }
}






