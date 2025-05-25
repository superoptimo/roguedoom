import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import PhysicEntity;
import Labyrinth;
import java.awt.event.*;
import java.awt.*;
public class LeonRocket extends PhysicEntity
{
	public TransformGroup tg;
	public Transform3D   tf;
	public PointLight m_light; 
	LeonRocket(Labyrinth lab, Vector3d dir, Vector3d pos)
	{		
		super(lab);
		direction.set(dir);
		directionVel.set(dir);
		position.set(pos);		
		
		///agregar la bola
		tg = new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		radius=0.125;
		
		Appearance app = new Appearance();
		app.setMaterial(new Material(new Color3f(0.0f,0.0f,0.0f),///ambient
					new Color3f(1.0f,1.0f,0.0f),			
					new Color3f(1.0f,0.0f,0.0f),
					new Color3f(1.0f,0.0f,0.0f),
					80.0f));
	
		Sphere  sph = new Sphere((float)radius,app);
		tg.addChild(sph);
		////agregar  la luz
		m_light = m_Labyrinth.getDynamicLight();		
		
		
		tf =  new Transform3D();
		tf.set(pos);		
		tg.setTransform(tf);
		
		br.addChild(tg);
	}
	
	
	public void initialize()
	{
		////position= new Vector3d(0.0,0.0,0.0);	
		///direction= new Vector3d(0.0,0.0,1.0);	
		///directionVel= new Vector3d(direction);
		///vecside= new Vector3d(-1.0,0.0,0.0);	
		m_light.setEnable(true);
		perpendicular = new Vector3d(0.0,1.0,0.0);	
		normal= new Vector3d(0.0,0.0,0.0);
		//velocity= new Vector3d(0.0,0.0,0.0);		
		impulse=0.2;
		friction=0.0;
		angle=0.0;
		angfriction=0.0;
		
		applyfriction= false;
		applyanglefriction= false;	
		applynormal= false;			
		
		
		///m_cell = m_Labyrinth.getCellbyPosit(0,0,false);
		
		/*position.set(m_Labyrinth.getCellPosition(m_cell.m_row,
										m_cell.m_column, m_cell.m_upstairs));*/
		/*position.y= position.y + Labyrinth.CELLHEIGHT/2;	*/											
		wakeupOn(new WakeupOnElapsedTime(5));		
		
	}
	
	public void processStimulus(java.util.Enumeration criteria)
	{
		updatePosition();
		if(applynormal) ///ha chocado con algo
		{
			m_light.setEnable(false);
			br.detach();		
		}
		else		
		{	
			BoundingSphere bounds = new BoundingSphere(new Point3d(position.x,position.y, position.z), Labyrinth.CELLWIDTH*2);	
			m_light.setInfluencingBounds(bounds);
			m_light.setPosition((float)position.x,(float)position.y, (float)position.z);
			
			tf.set(position);		
			tg.setTransform(tf);
			wakeupOn(new WakeupOnElapsedTime(5));
		}
	
	}

}