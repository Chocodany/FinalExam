import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Driver {
	
	public static int animationSec;
	public static ArrayList<TripPoint> trip;
	public static BufferedImage raccoon;

    public static void main(String[] args) throws FileNotFoundException, IOException {
    	TripPoint.readFile("triplog.csv");
    	TripPoint.h2StopDetection();
    	raccoon = ImageIO.read(new File("arrow.png"));
    	
    	// set up frame
        JFrame frame = new JFrame("Map Viewer");
        frame.setLayout(new BorderLayout());
        
        // set up top panel for input selections
        JPanel topPanel = new JPanel();
        frame.add(topPanel, BorderLayout.NORTH);
        // play button
        JButton play = new JButton("Play");
        // checkbox to enable/disable stops
        JCheckBox includeStops = new JCheckBox("Include Stops");
        // dropbox to pick animation time
        String[] timeList = {"Animation Time", "15", "30", "60", "90"};
        JComboBox<String> animationTime = new JComboBox<String>(timeList);
        animationSec = 0;
        // add all to top panel
        topPanel.add(animationTime);
        topPanel.add(includeStops);
        topPanel.add(play);
        
        // set up mapViewer
        JMapViewer mapViewer = new JMapViewer();
        frame.add(mapViewer);
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mapViewer.setTileSource(new OsmTileSource.TransportMap());
        
        // add listeners
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                    	mapViewer.removeAllMapMarkers(); // remove all markers from the map
                    	mapViewer.removeAllMapPolygons();
                    	if (includeStops.isSelected()) {
                    		trip = TripPoint.getTrip();
                    	}
                    	else {
                    		trip = TripPoint.getMovingTrip();
                    	}
                        plotTrip(animationSec, trip, mapViewer);
                        return null;
                    }
                };
                worker.execute();
            }
        });
        animationTime.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object selectedItem = animationTime.getSelectedItem();
                    if (selectedItem instanceof String) {
                        String selectedString = (String) selectedItem;
                        if (!selectedString.equals("Animation Time")) {
                            animationSec = Integer.parseInt(selectedString);
                            System.out.println("Updated to " + animationSec);
                        }
                    }
                }
            }
        });

        // set the map center and zoom level
        mapViewer.setDisplayPosition(new Coordinate(34.82, -107.99), 6);
        
    }
    
    
    // plot the given trip ArrayList with animation time in seconds
    public static void plotTrip(int seconds, ArrayList<TripPoint> trip, JMapViewer map) throws IOException {
    	// amount of time between each point in milliseconds
    	long delayTime = (seconds * 1000) / trip.size();
    	
    	Coordinate c1;
    	Coordinate c2 = null;
    	MapMarker marker;
    	MapMarker prevMarker = null;
    	MapPolygonImpl line;
    	
    	for (int i = 0; i < trip.size(); ++i) {
    		c1 = new Coordinate(trip.get(i).getLat(), trip.get(i).getLon());
    		//double angle = Math.atan2(trip.get(i).getLat() - trip.get(i-1).getLat(), trip.get(i).getLon() - trip.get(i-1).getLon());
    	    //double angleDegrees = Math.toDegrees(angle);
    	    //Graphics2D g = raccoon.createGraphics();
    	    //g.rotate(angleDegrees);
    	    //g.drawImage(raccoon, null, map);
    	    //g.dispose();
    	    //marker.setImage(raccoon);
        	marker = new IconMarker(c1, raccoon);
        	
        	
    		map.addMapMarker(marker);
    		if (i != 0) {
    			c2 = new Coordinate(trip.get(i-1).getLat(), trip.get(i-1).getLon());
    		}
    		if (c2 != null) {
    			line = new MapPolygonImpl(c1, c2, c2);
    			line.setColor(Color.RED);
    			line.setStroke(new BasicStroke(3));
    			map.addMapPolygon(line);
    			map.removeMapMarker(prevMarker);
    			//g.rotate(Math.toRadians(angleDegrees), raccoon.getWidth() / 2, raccoon.getHeight() / 2);
        	    //g.drawImage(raccoon, null, 0, 0);
    		}
    		try {
				Thread.sleep(delayTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	    
    		
    		prevMarker = marker;
    	}
    	
    }
    
}