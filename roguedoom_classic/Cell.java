import java.util.*;

class Cell
{
	public static final int NORMALROOM = 0;	
	public static final int REDBASE = 1;
	public static final int BLUEBASE = 2;
	public static final int REDALTAR = 3;
	public static final int BLUEALTAR = 4;
	public static final int ELEVATOR= 5;
	public static final int ELEVATORROOM= 6;
	public static final int REDSOURCE= 7;
	public static final int BLUESOURCE= 8;
	
	public boolean EastBound,SouthBound;
	public int mark;
	
	public int m_row,m_column;
	public boolean m_upstairs;
	public Vector m_entities;
	/////para calculos de visibilidad
	double leftTangent=0, rightTangent=0;	
	public boolean mark2;	
	
	/*
	0 top
	1 bottom
	2 left
	3 right
	*/
	
	
	public Cell()
	{
		mark= Cell.NORMALROOM;
		mark2 = false;
		EastBound=false;
		SouthBound=false;		
		m_entities = new Vector();
		
	}	
	
	
	
	
	
}
