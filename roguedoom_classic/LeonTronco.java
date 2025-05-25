import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.keyboard.*; 
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.IndexedTriangleArray;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.math.*;
import java.util.*;
import javax.swing.*;


class LeonTronco extends IndexedTriangleArray
{
	public LeonTronco(double height,double baseradius,double topradius, int numFacets)	
	{
		
		//////inicializar los vertices
		super(4*numFacets+2,GeometryArray.COORDINATES|GeometryArray.NORMALS|GeometryArray.TEXTURE_COORDINATE_2, 6*(numFacets+1) + 6*(numFacets-2) );
        
        Matrix3d matrot= new Matrix3d();        
        Vector3f vecnormal = new Vector3f(1.0f,0.0f,0.0f);        
        Vector3f vecnormaltop = new Vector3f(0.0f,1.0f,0.0f);
        Vector3f vecnormalbase = new Vector3f(0.0f,-1.0f,0.0f);        
        Point3d pt1 = new Point3d(baseradius,-height/2.0,0.0);
        Point3d pt2 = new Point3d(topradius,height/2.0,0.0);
        Point3d auxpt= new Point3d(),auxpt2= new Point3d();   
  		
       
        int i;
        int arrindex;
        float texuinc;
        Point2f texcoord= new Point2f();
     
        texuinc = 1.0f/numFacets;
        
        matrot.rotY(2*Math.PI /numFacets);////rotacion cilindrica
        
        arrindex=0;
        for(i=0;i<=numFacets;i++)
        {
        	    	
        	setCoordinate(arrindex, pt1);        	
  			setNormal(arrindex,vecnormal);
  			texcoord.y=0.0f;
  			setTextureCoordinate(arrindex,texcoord);
  			arrindex++;
 			setCoordinate(arrindex, pt2);
  			setNormal(arrindex,vecnormal);  
  			texcoord.y=-1.0f;
  			setTextureCoordinate(arrindex,texcoord);
  			arrindex++;
  			
  			////incrementar las coordenadas de textura
  			texcoord.x=texcoord.x + texuinc;  			
  			////aplicar rotacion
  			matrot.transform(pt1,auxpt);
  			pt1.x =auxpt.x;
  			pt1.y =auxpt.y;
  			pt1.z =auxpt.z;
  			
  			matrot.transform(pt2,auxpt);
  			pt2.x =auxpt.x;
  			pt2.y =auxpt.y;
  			pt2.z =auxpt.z;

			auxpt2.x =(double)vecnormal.x;
  			auxpt2.y =(double)vecnormal.y;
  			auxpt2.z =(double)vecnormal.z;

  			matrot.transform(auxpt2,auxpt);  			
  			vecnormal.x =(float) auxpt.x;
  			vecnormal.y =(float) auxpt.y;
  			vecnormal.z =(float) auxpt.z;
  			
  			
    	}
    	
    	//los discos
    	pt1 = new Point3d(baseradius,-height/2.0,0.0);
        pt2 = new Point3d(topradius,height/2.0,0.0);
    	for(i=0;i<numFacets;i++)
    	{
  			setCoordinate(arrindex, pt1);
  			setNormal(arrindex,vecnormalbase);
  			arrindex++;
  			
 			setCoordinate(arrindex, pt2);  			
 			setNormal(arrindex,vecnormaltop);         	
			arrindex++;
			////aplicar rotacion
  			matrot.transform(pt1,auxpt);
  			pt1.x =auxpt.x;
  			pt1.y =auxpt.y;
  			pt1.z =auxpt.z;
  			
  			matrot.transform(pt2,auxpt);
  			pt2.x =auxpt.x;
  			pt2.y =auxpt.y;
  			pt2.z =auxpt.z;
			
    	}
        /////crear los indices de caras
        
        arrindex=0;
        for(i=0;i<numFacets;i++)
        {
        	/////cara laterales
        	
        	 setCoordinateIndex(arrindex,((i+1)%numFacets)*2);
			 setNormalIndex(arrindex,((i+1)%numFacets)*2);
			 setTextureCoordinateIndex(arrindex,((i+1)%numFacets)*2);
			 arrindex++; 			 
			 setCoordinateIndex(arrindex,((i+1)%numFacets)*2+1);
			 setNormalIndex(arrindex,((i+1)%numFacets)*2+1);
			 setTextureCoordinateIndex(arrindex,((i+1)%numFacets)*2+1);
			 arrindex++; 
			 setCoordinateIndex(arrindex,(i%numFacets)*2);
			 setNormalIndex(arrindex,(i%numFacets)*2);
			 setTextureCoordinateIndex(arrindex,(i%numFacets)*2);
			 arrindex++;  
			 
			 setCoordinateIndex(arrindex,((i+1)%numFacets)*2+1);
			 setNormalIndex(arrindex,((i+1)%numFacets)*2+1);
			 setTextureCoordinateIndex(arrindex,((i+1)%numFacets)*2+1);
			 arrindex++; 
			 setCoordinateIndex(arrindex,(i%numFacets)*2+1);
			 setNormalIndex(arrindex,(i%numFacets)*2+1);
			 setTextureCoordinateIndex(arrindex,(i%numFacets)*2+1);
			 arrindex++;  
			 setCoordinateIndex(arrindex,(i%numFacets)*2);
			 setNormalIndex(arrindex,(i%numFacets)*2);
			 setTextureCoordinateIndex(arrindex,(i%numFacets)*2);
			 arrindex++;  
		}	 
		////las tapas	 
		for(i=0;i<numFacets-2;i++)
		{
        	 setCoordinateIndex(arrindex,2*(numFacets+1));
			 setNormalIndex(arrindex,2*(numFacets+1) );			 
			 arrindex++; 			 
			 setCoordinateIndex(arrindex,2*(numFacets+1) +(i+2)*2 );
			 setNormalIndex(arrindex,2*(numFacets+1) +(i+2)*2);			 
			 arrindex++; 			 
        	 setCoordinateIndex(arrindex,2*(numFacets+1) +(i+1)*2);
			 setNormalIndex(arrindex,2*(numFacets+1) +(i+1)*2);			 
			 arrindex++;
        	 
			 
         	 setCoordinateIndex(arrindex,2*(numFacets+1) +1);
			 setNormalIndex(arrindex,2*(numFacets+1) +1);			 
			 arrindex++; 			 
			 setCoordinateIndex(arrindex,2*(numFacets+1) +(i+1)*2 +1);
			 setNormalIndex(arrindex,2*(numFacets+1) +(i+1)*2+1);			 
			 arrindex++; 			 
        	 setCoordinateIndex(arrindex,2*(numFacets+1) +(i+2)*2+1 );
			 setNormalIndex(arrindex,2*(numFacets+1) +(i+2)*2+1);			 
			 arrindex++;
        	 
    	}		

	}	
}
