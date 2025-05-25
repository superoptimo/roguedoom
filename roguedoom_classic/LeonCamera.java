import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import PhysicEntity;
import Labyrinth;
import LeonRocket;
import java.awt.event.*;
import java.awt.*;
public class LeonCamera extends PhysicEntity implements KeyListener
{
	double fov;
	TransformGroup camera;
	
	///variables de interaccion
	int elevatorcount =0;
	int debugcount = 200;
	boolean check=false;
	
	LeonCamera(Labyrinth labyrinth )
	{
		super(labyrinth);	
		Canvas3D c;		
		m_Labyrinth.getUniverse().getViewingPlatform().setNominalViewingTransform();
		camera = m_Labyrinth.getUniverse().getViewingPlatform().getViewPlatformTransform();
		c = m_Labyrinth.getUniverse().getCanvas();
		fov=c.getView().getFieldOfView();		
		
		elevatorcount=0;
	}
	public void calcVisibility()
	{
		
		Transform3D t= new Transform3D();
		
		Point3d eye= new Point3d(),eyeto = new Point3d();
		eye.set(position);
		eyeto.x= position.x+ direction.x;
		eyeto.y= position.y+ direction.y;
		eyeto.z= position.z+ direction.z;
		t.lookAt(eye,eyeto,perpendicular);
		
		
		t.invert();
		
		camera.setTransform(t);
		/*if(check)
		{*/
			m_Labyrinth.checkVisibleEntities(direction,vecside,position,fov);		
			/*check=false;
		}*/	
		
	}
	
       
    public void keyTyped(KeyEvent e)
    {
    }
    
    public void keyPressed(KeyEvent e)
	{
		
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
		{
			angle= Math.PI/60; 
			applyanglefriction= false;
		}
		else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
		{
			angle= -Math.PI/60;
			applyanglefriction= false; 
		}	
		if(elevatorcount>0) return;
		
		if(e.getKeyCode()==KeyEvent.VK_UP)
		{
			impulse = 0.5;
			applyfriction= false; 
					
		}
		else if(e.getKeyCode()==KeyEvent.VK_DOWN)
		{
			impulse = -0.5;
			applyfriction= false; 
		}
		
		if(e.getKeyCode()==KeyEvent.VK_CONTROL)
		{			
			LeonRocket ro;
			ro= new LeonRocket(m_Labyrinth,directionVel,position);
			ro.create();
		}
			
	}

	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
		{
			applyanglefriction= true;
		}
		else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
		{
			applyanglefriction= true; 
		}	
		
		if(elevatorcount>0) return;
		
		if(e.getKeyCode()==KeyEvent.VK_UP)
		{
			applyfriction= true; 
					
		}
		else if(e.getKeyCode()==KeyEvent.VK_DOWN)
		{
			applyfriction= true; 
		}		
	}
	
	public void processStimulus(java.util.Enumeration criteria)
	{
		
		calcVisibility();
		
		Cell oldcell;
		oldcell = m_cell;
		updatePosition();
		
		if(elevatorcount>0)
		{
				elevatorcount--;
				if(	elevatorcount==0)////restaurar velocidad
				{
					velocity.set(new Vector3d(0.0,1.0,0.0));
					impulse=0.0;
					directionVel.set(direction);
				}
		}			
		
		
		if(oldcell!=m_cell)
		{
			if(oldcell.mark!=Cell.ELEVATOR)
			{				
				if(m_cell.mark==Cell.ELEVATOR)////entramos al ascensor
				{
					applyfriction= false;
					applynormal= false;
					elevatorcount = 20;
					if(m_cell.m_upstairs)
					{
						impulse= -Labyrinth.CELLHEIGHT/elevatorcount;
					}
					else
					{
						impulse= Labyrinth.CELLHEIGHT/elevatorcount;
					}
					
					directionVel.set(new Vector3d(0.0,1.0,0.0));
					position.set(m_Labyrinth.getCellPosition(m_cell.m_row,
										m_cell.m_column, m_cell.m_upstairs));
										
					position.y= position.y + Labyrinth.CELLHEIGHT/2;				
				}	
			}			
					
		}
		
		wakeupOn(new WakeupOnElapsedTime(10));
	/*	if(debugcount>0)
		{
			debugcount--;
			System.out.println("Estimulando");
			System.out.println("Impulso " + impulso);
		}*/
				
	}
	
}
