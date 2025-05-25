import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.keyboard.*; 
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.math.*;
import java.util.*;
import javax.swing.*;
import Cell;
import Labyrinth;
import LeonCamera;

public class CapturaFlor extends Applet 
{
	
	Canvas3D c;
	Labyrinth Labyrinth;
	SimpleUniverse u;
	LeonCamera camera;
	BranchGroup scene;
	
	
		 
	public void init()
	{
		setLayout(new BorderLayout());
        GraphicsConfiguration config =
           SimpleUniverse.getPreferredConfiguration();

        c = new Canvas3D(config);        
        u = new SimpleUniverse(c);
        
		
		add("Center", c);
		
		
		Labyrinth = new Labyrinth();
		System.out.println("Creando Escena");
		
		Labyrinth.createScene();
		System.out.println("Terminando Escena");
		
		
		camera = new LeonCamera(Labyrinth,u);					
		c.addKeyListener(camera);		
		c.transferFocus();
		///camara.setEnable(true);
		///////agregar el laberinto al universo
		scene= Labyrinth.getScene();
		u.addBranchGraph(scene);
		
		//////
		
		/////la posision inicial de la camara
		
		
	}

	
	public static void main(String[] args) {
	new MainFrame(new CapturaFlor(), 640, 500);
    }
	
}