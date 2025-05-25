import javax.vecmath.*;
import java.math.*;
import java.util.*;

/*
CLASE : LeonMath
OBJETIVO:
	PROPORCIONAR FUNCIONES MATEMATICAS PARA CALCULAR 		
	COALISIONES FISICAS
DESCRIPCION;
	CONJUNTO DE FUNCIONES ESTATICAS 
	
*/	


public class LeonMath
{
	
	
	
	/*
	PROPOSITO:
	 encuentra el choque entre una esfera S1
	 con velocity vel1 y center center1, y otra esfera estatica,
	PARAMETROS:
		radius1-> el radio de la esfera1
		center1-> el center de la esfera1
		vel1 -> la velocity de la esfera1
		radius2-> el radio de la esfera2
		center2-> el center de la esfera2
		escalar[] -> arreglo de un elemento que contendra el
					parametro de la recta con punto 'center1	'
					y direccion vel1, en la cual la recta se interseca
	RESULTADO
		boolean->
		true: se intersecan
		false :no
	*/

	
	public static boolean collisionSphereVelSphere(double radius1, Vector3d center1,
				Vector3d vel1, double radius2, Vector3d center2,
				double escalar[] )
	{
		double proj;
		double vl, dis;
		Vector3d vdiff= new Vector3d(center2.x-center1.x,center2.y-center1.y, center2.z-center1.z);
		Vector3d vpt= new Vector3d();
		///////hallar la proyeccion de el vector diferencia entre los centers
		proj= vdiff.dot(vel1);
		if(proj<=0) return  false; ////no se intersecan
		vl = vel1.x*vel1.x+vel1.y*vel1.y+vel1.z*vel1.z; /////longitud cuadrada
		///////vl = |vel1|^2
		proj= proj/vl;
		/////proj = vdiff·vel/ |vel1|^2
		vdiff.set(vel1);
		vdiff.scale(proj);		
		vpt.set(center1);
		vpt.add(vdiff);//////vpt  es el punto proyectado
		///////hallar la distancia al cuadrado del center2  a la recta
		vdiff.set(center2);
		vdiff.sub(vpt);
		dis = vdiff.x*vdiff.x+vdiff.y*vdiff.y+vdiff.z*vdiff.z;
		////dis = |center2-vpt|^2
		/////hallar la posicion final de la esfera
		dis = Math.sqrt((radius1+radius2)*(radius1+radius2) - dis);		
		/////desplazamiento= dis = sqrt(vl)-dis
		vl= Math.sqrt(vl);
		dis = vl - dis;
		escalar[0]=dis/vl;
		if(escalar[0]>1.0) return false; /// no chocan
		return true;
		
	}
	
	
	
	/*
	PROPOSITO
		esta funcion encuentra el choque entre 
		dos esferas en movimiento.	
	PARAMETROS:
		radius1-> el radius de la esfera1
		center1-> el center de la esfera1
		vel1 -> la velocity de la esfera1
		radius2-> el radius de la esfera2
		center2-> el center de la esfera2
		escalar[] -> arreglo de un elemento que contendra el
					parametro de la recta con punto 'center1	'
					y direccion vel1, en la cual la recta se interseca
	RESULTADO
		boolean->
		true: se intersecan
		false :no
	*/

	
	public static boolean collisionSphereVelSphereVel(double radius1, Vector3d center1,Vector3d vel1, double radius2, 
					Vector3d center2, Vector3d vel2,
					double escalar1[], double escalar2[])
	{
		Vector3d vdiff= new Vector3d(vel1.x-vel2.x,vel1.y-vel2.y,vel1.z-vel2.z);
		Vector3d newvel= new Vector3d(vel1);
		newvel.add(vdiff);////velocity relativa con respeco a la esfera 2
		if(collisionSphereVelSphere(radius1, center1, vel1, radius2, center2, escalar1)== false) return false;;
		escalar2[0] = escalar1[0];
		return true;		
	}
	
	
	
	/*
	PROPOSITO:
		encuentra la interseccion entre una esfera en movimiento 
		y  un plano. retorna en escalar el parametro 
		en la recta de velocity, el cual determina el punto final 
		del movimiento.	
	
	PARAMETROS:
		radius-> el radius de la esfera
		center-> el center de la esfera
		vel -> la velocity de la esfera
		planonormal-> vector normal al plano
		puntoplano-> vector que se halla en el plano
		escalar[] -> arreglo de un elemento que contendra el
					parametro de la recta con punto 'center	'
					y direccion vel, en la cual la recta se interseca
	RESULTADO
		boolean->
		true: se intersecan
		false :no
	*/
	public static boolean collisionSphereVelPlane(double radius, Vector3d center,
					Vector3d vel, Vector3d planenormal,
					 Vector3d planepoint,double escalar[])
	{
		double t, unproj;
		/////verificar si la velocity coincide con el plano
		unproj=planenormal.x*vel.x+planenormal.y*vel.y+planenormal.z*vel.z;
		if(unproj>=0) return false; ////la esfera no se dirige hacia el plano
		/////hallar la distancia del plano al center	
		t=planenormal.x*(center.x-planepoint.x)+
			planenormal.y*(center.y-planepoint.y)+
			planenormal.z*(center.z-planepoint.z);
		
		if(t<=0) return false; ////la esfera se halla detras del plano
		
			
		/////hallar la projeccion sobre la velocity
		
		unproj= -unproj / vel.length();	////(N·V)/|V| = cos(t)	
		escalar[0]= (t-radius)/unproj;//////cateto/cos(t) = hipotenusa
		if(escalar[0]>1.0) return false;
		
		return true;		
	}



	
	/*
	PROPOSITO:
		encuentra la interseccion entre una esfera en movimiento 
		y  un plano. retorna en escalar el parametro 
		en la recta de velocity, el cual determina el punto final 
		del movimiento.
	PARAMETROS:
		point-> el punto del cual parte  
		dir -> el vector direccion de la recta, tiene magnitud 1
		planonormal-> vector normal al plano
		puntoplano-> vector que se halla en el plano
		escalar[] -> arreglo de un elemento que contendra el
					parametro de la recta con punto 'point	'
					y direccion dir, en la cual la recta se interseca
	RESULTADO
		boolean->
		true: se intersecan
		false :no
	*/
	
	
	public static boolean RayCollisionPlane(Vector3d point, Vector3d dir, Vector3d planenormal,
					 Vector3d planepoint,double escalar[])
	{
		double dist=0.0, tt=0.0;
		
		tt = dir.dot(planenormal);
		if(tt<=0) return false;
		dist=planenormal.x*(point.x-planepoint.x)+
			planenormal.y*(point.y-planepoint.y)+
			planenormal.z*(point.z-planepoint.z);
			
		escalar[0]= dist/tt;
		return true;
	}
	
	
					
}