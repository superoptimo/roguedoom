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

class LeonCuboide extends IndexedQuadArray
{
	
	public LeonCuboide(float xsize,float ysize,float zsize)
	{
		//////inicializar los vertices
		super(8,GeometryArray.COORDINATES|GeometryArray.NORMALS|GeometryArray.TEXTURE_COORDINATE_2,
                        24);
        
        setCoordinate(0, new Point3f(xsize,-ysize,zsize));////front, right, down
        setCoordinate(1, new Point3f(xsize,ysize,zsize));////front, right, up
        setCoordinate(2, new Point3f(-xsize,ysize,zsize));////front, left, up
        setCoordinate(3, new Point3f(-xsize,-ysize,zsize));////front, right,down
        
        setCoordinate(4, new Point3f(xsize,-ysize,-zsize));////back, right, down
        setCoordinate(5, new Point3f(xsize,ysize,-zsize));////back, front, right, up
        setCoordinate(6, new Point3f(-xsize,ysize,-zsize));////back, front, left, up
        setCoordinate(7, new Point3f(-xsize,-ysize,-zsize));////back,front, right,down 		
		
		Vector3f vecnormal = new Vector3f(1.0f,-1.0f,1.0f);
		vecnormal.normalize();
		setNormal(0, vecnormal);
		
		vecnormal =  new Vector3f(1.0f,1.0f,1.0f);
		vecnormal.normalize();
		setNormal(1,vecnormal);
		
		vecnormal =  new Vector3f(-1.0f,1.0f,1.0f);
		vecnormal.normalize();
		setNormal(2, vecnormal);
		
		vecnormal =  new Vector3f(-1.0f,-1.0f,1.0f);
		vecnormal.normalize();
		setNormal(3, vecnormal);
		
		vecnormal = new Vector3f(1.0f,-1.0f,-1.0f);
		vecnormal.normalize();
		setNormal(4, vecnormal);
		
		vecnormal =  new Vector3f(1.0f,1.0f,-1.0f);
		vecnormal.normalize();
		setNormal(5,vecnormal);
		
		vecnormal =  new Vector3f(-1.0f,1.0f,-1.0f);
		vecnormal.normalize();
		setNormal(6, vecnormal);
		
		vecnormal =  new Vector3f(-1.0f,-1.0f,-1.0f);
		vecnormal.normalize();
		setNormal(7, vecnormal);		
			
		setTextureCoordinate(0,new Point2f(0.0f,-1.0f));
		setTextureCoordinate(1,new Point2f(0.0f,0.0f));
		setTextureCoordinate(2,new Point2f(-1.0f,0.0f));
		setTextureCoordinate(3,new Point2f(-1.0f,-1.0f));
		
		setTextureCoordinate(4,new Point2f(-1.0f,-1.0f));
		setTextureCoordinate(5,new Point2f(-1.0f,0.0f));
		setTextureCoordinate(6,new Point2f(0.0f,0.0f));
		setTextureCoordinate(7,new Point2f(0.0f,-1.0f));
		////orden de dibujo, indices
		//cara frontal
		setCoordinateIndex(0,0);
		setNormalIndex(0,0);
		setTextureCoordinateIndex(0,0,0);		
		setCoordinateIndex(1,1);
		setNormalIndex(1,1);
		setTextureCoordinateIndex(0,1,1);
		setCoordinateIndex(2,2);
		setNormalIndex(2,2);
		setTextureCoordinateIndex(0,2,2);
		setCoordinateIndex(3,3);
		setNormalIndex(3,3);
		setTextureCoordinateIndex(0,3,3);
		
		//cara derecha
		setCoordinateIndex(4,4);
		setNormalIndex(4,4);
		setTextureCoordinateIndex(0,4,4);		
		setCoordinateIndex(5,5);
		setNormalIndex(5,5);
		setTextureCoordinateIndex(0,5,5);
		setCoordinateIndex(6,1);
		setNormalIndex(6,1);
		setTextureCoordinateIndex(0,6,1);
		setCoordinateIndex(7,0);
		setNormalIndex(7,0);
		setTextureCoordinateIndex(0,7,0);
		
		//cara izquierda
		setCoordinateIndex(8,3);
		setNormalIndex(8,3);
		setTextureCoordinateIndex(0,8,3);		
		setCoordinateIndex(9,2);
		setNormalIndex(9,2);
		setTextureCoordinateIndex(0,9,2);
		setCoordinateIndex(10,6);
		setNormalIndex(10,6);
		setTextureCoordinateIndex(0,10,6);
		setCoordinateIndex(11,7);
		setNormalIndex(11,7);
		setTextureCoordinateIndex(0,11,7);
		//cara trasera
		setCoordinateIndex(12,7);
		setNormalIndex(12,7);
		setTextureCoordinateIndex(0,12,7);		
		setCoordinateIndex(13,6);
		setNormalIndex(13,6);
		setTextureCoordinateIndex(0,13,6);
		setCoordinateIndex(14,5);
		setNormalIndex(14,5);
		setTextureCoordinateIndex(0,14,5);
		setCoordinateIndex(15,4);
		setNormalIndex(15,4);
		setTextureCoordinateIndex(0,15,4);
		
		//cara de abajo
		setCoordinateIndex(16,3);
		setNormalIndex(16,3);
		setTextureCoordinateIndex(0,16,0);		
		setCoordinateIndex(17,7);
		setNormalIndex(17,7);
		setTextureCoordinateIndex(0,17,1);
		setCoordinateIndex(18,4);
		setNormalIndex(18,4);
		setTextureCoordinateIndex(0,18,2);
		setCoordinateIndex(19,0);
		setNormalIndex(19,0);
		setTextureCoordinateIndex(0,19,3);
		
		//cara de arriba
		setCoordinateIndex(20,1);
		setNormalIndex(20,1);
		setTextureCoordinateIndex(0,20,0);		
		setCoordinateIndex(21,5);
		setNormalIndex(21,5);
		setTextureCoordinateIndex(0,21,1);
		setCoordinateIndex(22,6);
		setNormalIndex(22,6);
		setTextureCoordinateIndex(0,22,2);
		setCoordinateIndex(23,2);
		setNormalIndex(23,2);
		setTextureCoordinateIndex(0,23,3);			
	}	
}

