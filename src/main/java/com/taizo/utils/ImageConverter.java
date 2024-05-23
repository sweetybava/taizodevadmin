package com.taizo.utils;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImageConverter {
    public void convertHtmlToImage(String htmlFilePath, String imageFilePath) throws IOException {

		/*
		 * File htmlFile = new File(htmlFilePath); String url =
		 * htmlFile.toURI().toURL().toString();
		 * 
		 * String html = "<h1>Hello, world.</h1>"; int width = 200, height = 100;
		 * System.setProperty("java.awt.headless", "false"); BufferedImage image =
		 * GraphicsEnvironment.getLocalGraphicsEnvironment()
		 * .getDefaultScreenDevice().getDefaultConfiguration()
		 * .createCompatibleImage(width, height);
		 * 
		 * Graphics graphics = image.createGraphics();
		 * 
		 * JEditorPane jep = new JEditorPane("text/html", url); jep.setSize(width,
		 * height); jep.print(graphics);
		 * 
		 * ImageIO.write(image, "png", new File("Image.png"));
		 * 
		 */
    	
    	File htmlFile = new File(htmlFilePath); 
    	String url = htmlFile.toURI().toURL().toString();
    	
    	//load the webpage into the editor
    	JEditorPane ed = new JEditorPane(new URL(url));
    	ed.setSize(2000,2000);

    	//create a new image
    	BufferedImage image = new BufferedImage(ed.getWidth(), ed.getHeight(),
    	                                        BufferedImage.TYPE_INT_ARGB);

    	//paint the editor onto the image
    	SwingUtilities.paintComponent(image.createGraphics(), 
    	                              ed, 
    	                              new JPanel(), 
    	                              0, 0, image.getWidth(), image.getHeight());
    	File file = new File("google.png");
    	//save the image to file
    	ImageIO.write((RenderedImage)image, "png", file.getAbsoluteFile());
		/*
		 * if(file.delete()) { System.out.println(file.getName() + " is deleted!"); }
		 * else { System.out.println("Delete operation is failed."); }
		 */	
    }

}
