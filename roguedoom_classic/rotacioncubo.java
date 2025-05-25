import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.keyboard.*; 
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.math.*;
import java.util.*;
import javax.swing.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class rotacioncubo extends Applet implements KeyListener
{
 Canvas3D lienzo;	
 SimpleUniverse u;
 TransformGroup marco;
 BranchGroup bg;
 Primitive cubo;
 Appearance tex;
 DirectionalLight luz= new DirectionalLight(new Color3f(1.0f,1.0f,0.0f),new Vector3f(1.0f,-1.0f,-1.0f));
 Transform3D tf= new Transform3D();
 
 
  public void init ()
 {
  setLayout(new BorderLayout());
  GraphicsConfiguration config =SimpleUniverse.getPreferredConfiguration();

  lienzo = new Canvas3D(config);   //Inicializacion Lienzo     
  u = new SimpleUniverse(lienzo);  //Inicializar la coleccion de objetos
  add("Center", lienzo);	
  u.getViewingPlatform().setNominalViewingTransform();

  
  cubo=new com.sun.j3d.utils.geometry.Box();
  tex= new Appearance();
  tex.setMaterial(new Material(new Color3f(0.4f,0.4f,0.4f), new Color3f(1.0f,0.4f,0.4f),
  			new Color3f(1.0f,1.0f,1.0f), new Color3f(0.0f,0.0f,0.0f), 5.0f)); 
  
  cubo.setAppearance(tex);
  
  marco=new TransformGroup();
  bg=new BranchGroup();
  
  marco.addChild(cubo);
  marco.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
  
  bg.addChild(marco);
  bg.addChild(luz);
  u.addBranchGraph(bg);
  
   
  tf.setTranslation(new Vector3f(0.0f,0.0f,-2.0f));
  
  marco.setTransform(tf);
  
  lienzo.addKeyListener(this);
 }	
 
 public void keyTyped(KeyEvent e)
 {
 	
 }
 
 public void keyPressed(KeyEvent e)
 {
 	Transform3D td=new Transform3D();
 	if(e.getKeyCode()==KeyEvent.VK_LEFT)
    {
	 td.rotY(-Math.PI/6);
    }
	else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
	{
	 td.rotY(Math.PI/6);	
	}
	
	tf.mul(td);
	marco.setTransform(tf);	
 }
 
 public void keyReleased(KeyEvent e)
 {
 	
 }
 
 
}

