import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import Labyrinth;
public class PhysicEntity extends Behavior
{
	public Vector3d collisionpoint = new Vector3d();
	public Vector3d position= new Vector3d();	
	public Vector3d direction= new Vector3d();	
	public Vector3d directionVel= new Vector3d();	
	public Vector3d vecside= new Vector3d();	
	public Vector3d perpendicular = new Vector3d();
	public Vector3d normal= new Vector3d();
	public Vector3d velocity= new Vector3d();
	double impulse=0.0,friction=0.2;
	double angle=0.0,angfriction=0.025;
	double radius=1.0;
	boolean applyfriction= true;	
	boolean applyanglefriction= true;	
	boolean applynormal= false;
	protected Labyrinth m_Labyrinth;
	protected Cell m_cell;
	protected BranchGroup	br= new BranchGroup();
	
	
	PhysicEntity(Labyrinth Labyrinth )
	{
		m_Labyrinth = Labyrinth;
		///setEnable(false);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000.0);
		setSchedulingBounds(bounds);		
		br.setCapability(BranchGroup.ALLOW_DETACH);
		br.addChild(this);
		
		
		
	}
	
	public void create()
	{
		m_Labyrinth.getUniverse().addBranchGraph(br);	
	}
	
	
	public void initialize()
	{
		////position= new Vector3d(0.0,0.0,0.0);	
		direction= new Vector3d(0.0,0.0,1.0);	
		directionVel= new Vector3d(direction);
		vecside= new Vector3d(-1.0,0.0,0.0);	
		perpendicular = new Vector3d(0.0,1.0,0.0);	
		normal= new Vector3d(0.0,0.0,0.0);
		velocity= new Vector3d(0.0,0.0,0.0);		
		impulse=0.0;
		friction=0.08;
		angle=0.0;
		angfriction=0.05;
		radius=1.0;
		applyfriction= true;
		applyanglefriction= true;	
		applynormal= false;			
		
		
		m_cell = m_Labyrinth.getCell(0,0,false);
		
		position.set(m_Labyrinth.getCellPosition(m_cell.m_row,
										m_cell.m_column, m_cell.m_upstairs));
		position.y= position.y + Labyrinth.CELLHEIGHT/2;												
		wakeupOn(new WakeupOnElapsedTime(10));
		
		
		
	}
	public void applyFriction()
	{
		double signus = 1.0;
		if(applyfriction==false) return;
		
		
		if(impulse!=0.0)
		{
			if(impulse<0) signus = -1.0;
			impulse= impulse-friction*signus;
			if(signus>0.0)
			{
				if(impulse<0.0)	impulse=0.0;
			}
			else
			{
				if(impulse>0.0)	impulse=0.0;
			}	
		}
	}
	
	public void applyAngFriction()
	{
		double signus = 1.0;
		if(applyanglefriction==false) return;
		
		if(angle!=0.0)
		{
			if(angle<0) signus = -1.0;		
			angle= angle-angfriction*signus;
			if(signus>0.0)
			{
				if(angle<0.0)	angle=0.0;
			}
			else
			{
				if(angle>0.0)	angle=0.0;
			}	
		}
	}
	
	public void calcRotation()
	{
		if(angle==0.0)
		{			
			return;
		}
		
		Transform3D t= new Transform3D();				
		Vector3d vd = new Vector3d();		
		t.rotY(angle); 		
		t.transform(direction);
		t.transform(directionVel);
		t.transform(vecside);
		t.transform(perpendicular);
		applyAngFriction();
	}

	
	public void calcVelocity()
	{
		if(impulse==0.0)
		{
			velocity.set(new Vector3d(0.0,0.0,0.0));
			return;
		}
		Vector3d vdir=new Vector3d(directionVel);	
		Vector3d vnormal=new Vector3d(normal);
		
		applyFriction();	
		vdir.scale(impulse);		
		
		/*if(applynormal)
		{
			
			Vector3d auxvector= new Vector3d();		
			Vector3d auxvector2= new Vector3d();		
			auxvector.cross(normal,vdir);
			if(auxvector.length()>0.001)
			{			
				auxvector2.cross(normal,auxvector);	
				
				auxvector2.normalize();
				auxvector2.scale(vdir.dot(auxvector2));
				vdir.set(auxvector2);
		}
			
		}*/		
		
		velocity.set(vdir);
		
	}

	public void updatePosition()
	{
		
		Vector3d newpos= new Vector3d();
		Cell cel1[]= new Cell[1];		
		calcVelocity();
		newpos.set(position);
		newpos.add(velocity);		
		applynormal =m_Labyrinth.crossLabyrinth(radius,position ,newpos,normal,collisionpoint,cel1);		
		position.set(newpos);			
		calcRotation();
		
		m_cell = cel1[0];
	}

	public void processStimulus(java.util.Enumeration criteria)
	{
		updatePosition();
		wakeupOn(new WakeupOnElapsedTime(10));
	}
		
}