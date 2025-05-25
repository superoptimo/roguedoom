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
import Cell;
import LeonCuboide;
import LeonTronco;
import LeonMath;

////clase de utilidad para guardar los indices
class PrimitiveIndex
{
	int index;	
	PrimitiveIndex()
	{
		index=0;		
	}
}


class Laberinth 
{
	public static final int MAXELEVATORS = 6;	
	public static final double CELLWIDTH = 6.0;	
	public static final double CELLHEIGHT = 4.0;	
	
	protected Cell cells[]= new Cell[800];///20 x 20 * 2 pisos
	protected int elevator_col[]= new int[MAXELEVATORS];
	protected int elevator_row[]= new int[MAXELEVATORS];
	protected int elevatorCount;
	int debugcount;
	/////materiales
	protected Appearance matRedFloor, matBlueFloor, matRedCeil, matBlueCeil, matRedWall,
			 matBlueWall, matRedBaseWall, matBlueBaseWall, matRedAltar,matBlueAltar,
			 matRedSource, matBlueSource, matElevatorWall, matElevator;
	
	/////primitivas 3d	
	protected Geometry flagstone, Cuboide, Cilindro;
	protected TransformGroup  RedFloor, BlueFloor, RedCeil, BlueCeil, RedWall,
			 BlueWall, RedBaseWall, BlueBaseWall, RedAltar,BlueAltar,
			 RedSource, BlueSource,	 Elevator, ElevatorRoom;

	protected TransformGroup elevators[],elevatorooms[];			
	protected BranchGroup m_scene;	
	protected Switch m_staticentities;
	protected BitSet m_visiblemask;
	protected int m_numchildren;
	
	////////variables de estado auxiliares para 
	///////rutinas recursivas
	protected static boolean topwall,bottomwall,leftwall, rightwall; ////indican si el portal correspondiente mira hacia la camara	
	protected Stack markedstack= new Stack();
	protected Stack cellstack = new Stack();	
	protected Cell warpcell;
	protected boolean canwarp;	
	protected static Vector3d m_vecdir,m_vecside, m_vecpos;
	protected double leftTangent=0.0,rightTangent=0.0;
	/////recursivas
	public Laberinth()
	{
		
		debugcount=600;
		m_numchildren=0;
		m_scene=new BranchGroup();	
		m_staticentities=new Switch();		
		m_staticentities.setCapability(Switch.ALLOW_SWITCH_WRITE);		
		
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000.0);
		
		Color3f lColor1 = new Color3f(0.7f, 0.7f, 0.7f);
		Vector3f lDir1  = new Vector3f(-1.0f, -1.0f, -1.0f);
		Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);

		AmbientLight aLgt = new AmbientLight(alColor);
		aLgt.setInfluencingBounds(bounds);
		DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
		lgt1.setInfluencingBounds(bounds);
		
		m_scene.addChild(aLgt);
		m_scene.addChild(lgt1);
				
			
		createMaterials();
		createGeometry();
		createPrimitives();

	}
	
	

		
	//////
	protected void createMaterials()
	{
		
		Material mat;
		mat = new Material();
		mat.setDiffuseColor(1.0f,0.0f,0.2f);
		mat.setSpecularColor(0.0f,0.0f,1.0f);
		mat.setEmissiveColor(0.3f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);
		matRedFloor = new Appearance();		
		matRedFloor.setMaterial(mat);
		
		mat = new Material();
		mat.setDiffuseColor(0.0f,0.2f,1.0f);
		mat.setSpecularColor(0.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.0f,0.3f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);		
		matBlueFloor = new Appearance();		
		matBlueFloor.setMaterial(mat); 
		
		mat = new Material();
		mat.setDiffuseColor(0.7f,0.3f,0.0f);
		mat.setSpecularColor(1.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.3f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);					
		matRedCeil = new Appearance();
		matRedCeil.setMaterial(mat);
		
		mat = new Material();
		mat.setDiffuseColor(0.0f,0.3f,0.7f);
		mat.setSpecularColor(0.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.0f,0.0f,0.3f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);
		matBlueCeil = new Appearance();			
		matBlueCeil.setMaterial(mat); 
		
		
		mat = new Material();
		mat.setDiffuseColor(0.7f,0.3f,0.0f);
		mat.setSpecularColor(1.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.3f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);					
		matRedWall = new Appearance();						
		matRedWall.setMaterial(mat);

		mat = new Material();
		mat.setDiffuseColor(0.0f,0.3f,0.7f);
		mat.setSpecularColor(0.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.0f,0.0f,0.3f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);
		matBlueWall = new Appearance();	
		matBlueWall.setMaterial(mat);
		
		
		mat = new Material();
		mat.setDiffuseColor(0.9f,0.1f,0.0f);
		mat.setSpecularColor(1.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.7f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);					
		matRedBaseWall = new Appearance();	
		matRedBaseWall.setMaterial(mat);
		
		mat = new Material();
		mat.setDiffuseColor(0.0f,0.3f,1.0f);
		mat.setSpecularColor(0.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.0f,0.0f,0.7f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);
		matBlueBaseWall = new Appearance();
		matBlueBaseWall.setMaterial(mat);

		mat = new Material();
		mat.setDiffuseColor(0.9f,0.1f,0.0f);
		mat.setSpecularColor(1.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.7f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);						
		matRedAltar = new Appearance();	
		matRedAltar.setMaterial(mat);

		mat = new Material();
		mat.setDiffuseColor(0.0f,0.3f,1.0f);
		mat.setSpecularColor(0.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.0f,0.0f,0.7f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);					
		matBlueAltar = new Appearance();	
		matBlueAltar.setMaterial(mat);

		mat = new Material();
		mat.setDiffuseColor(0.4f,0.0f,0.0f);
		mat.setSpecularColor(1.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.5f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);											
		matRedSource = new Appearance();
		matRedSource.setMaterial(mat);

		mat = new Material();
		mat.setDiffuseColor(0.0f,0.0f,0.4f);
		mat.setSpecularColor(0.0f,1.0f,0.0f);
		mat.setEmissiveColor(0.0f,0.3f,0.4f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);
		matBlueSource = new Appearance();
		matBlueSource.setMaterial(mat);
		
		mat = new Material();
		mat.setDiffuseColor(1.0f,1.0f,0.0f);
		mat.setSpecularColor(0.0f,0.0f,1.0f);
		mat.setEmissiveColor(0.3f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);				
		matElevatorWall= new Appearance();
		matElevatorWall.setMaterial(mat);	
		
		matElevator= new Appearance();
		mat = new Material();
		mat.setDiffuseColor(0.0f,1.0f,0.5f,0.6f);
		mat.setSpecularColor(0.0f,0.0f,1.0f);
		mat.setEmissiveColor(0.3f,0.0f,0.0f);
		mat.setShininess(80.0f);
		mat.setLightingEnable(true);
		matElevator.setMaterial(mat);
		matElevator.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE|Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		matElevator.setColoringAttributes(new ColoringAttributes(1.0f,0.0f,0.0f,
				ColoringAttributes.SHADE_GOURAUD|ColoringAttributes.FASTEST));
		matElevator.setTransparencyAttributes(	new	TransparencyAttributes(TransparencyAttributes.FASTEST|TransparencyAttributes.BLEND_SRC_ALPHA,0.4f));	
		
	}
	protected void createGeometry()
	{
		QuadArray gFloor;
		
		////el piso de la cell es de 1 x 1
		Vector3f vecnormal;
		gFloor = new QuadArray(4,GeometryArray.COORDINATES|GeometryArray.NORMALS|GeometryArray.TEXTURE_COORDINATE_2);
		gFloor.setCoordinate(0, new Point3f(0.5f,0.0f,0.5f));
		gFloor.setCoordinate(1, new Point3f(0.5f,0.0f,-0.5f));
		gFloor.setCoordinate(2, new Point3f(-0.5f,0.0f,-0.5f));
		gFloor.setCoordinate(3, new Point3f(-0.5f,0.0f,0.5f));
		
		vecnormal = new Vector3f(0.1f,1.0f,0.1f);
		vecnormal.normalize();
		gFloor.setNormal(0, vecnormal);
		
		vecnormal =  new Vector3f(0.1f,1.0f,-0.1f);
		vecnormal.normalize();
		gFloor.setNormal(1,vecnormal);
		
		vecnormal =  new Vector3f(-0.1f,1.0f,-0.1f);
		vecnormal.normalize();
		gFloor.setNormal(2, vecnormal);
		
		vecnormal = new Vector3f(-0.1f,1.0f,0.1f);
		vecnormal.normalize();
		gFloor.setNormal(3, vecnormal);
		
		gFloor.setTextureCoordinate(0,new Point2f(-1.0f,-1.0f));
		gFloor.setTextureCoordinate(1,new Point2f(-1.0f,0.0f));
		gFloor.setTextureCoordinate(2,new Point2f(0.0f,0.0f));
		gFloor.setTextureCoordinate(3,new Point2f(0.0f,-1.0f));		
		
		flagstone = gFloor;		
		////crear un cubo de 4*4*4		
		
		
		Cuboide = new LeonCuboide(0.5f,0.5f,0.5f);
		
		////////crear el cilindro del ascensor con 8 facetas
		
		Cilindro = new LeonTronco(1.0f,0.5f,0.5f,8);
		
	}
	
	
	protected void createPrimitives()
	{
		/////la altura de la planta es de 4, y cada cell es de 4 x 4
		
		
		Matrix3d rotpared= new Matrix3d();
		//////dimensionar
		rotpared.setElement(0,0,0.25);
		rotpared.setElement(1,1,CELLHEIGHT);
		rotpared.setElement(2,2,CELLWIDTH);
		/////
		Matrix3d rotpiso= new Matrix3d();				
		rotpiso.setElement(0,0,CELLWIDTH);
		rotpiso.setElement(1,1,1.0);
		rotpiso.setElement(2,2,CELLWIDTH);

		Matrix3d rottecho= new Matrix3d();				
		rottecho.rotX(Math.PI);////voltear la face
		rottecho.mul(rotpiso);		
		
		
		Vector3d pospared= new Vector3d(0.0,CELLHEIGHT/2,0.0);
		Vector3d pospiso= new Vector3d(0.0,0.05,0.0);
		Vector3d postecho= new Vector3d(0.0,CELLHEIGHT-0.05,0.0);
		
		
		
		
		TransformGroup auxnode;
		Transform3D auxtransform;
		Shape3D shap;
		Matrix3d matrot;
		Vector3d mvec;
		/////elementos basicos de toda cell
		
		RedFloor = new TransformGroup();
		shap= new Shape3D(flagstone,matRedFloor);
		RedFloor.addChild(shap);
		auxtransform=new Transform3D(rotpiso, pospiso,1.0);
		RedFloor.setTransform(auxtransform);
		
		
		BlueFloor = new TransformGroup();
		shap= new Shape3D(flagstone,matBlueFloor);
		BlueFloor.addChild(shap);
		auxtransform=new Transform3D(rotpiso, pospiso,1.0);
		BlueFloor.setTransform(auxtransform);
		
		
		RedCeil = new TransformGroup();
		shap= new Shape3D(flagstone,matRedCeil);
		RedCeil.addChild(shap);
		auxtransform=new Transform3D(rottecho, postecho,1.0);
		RedCeil.setTransform(auxtransform);
		
		
		BlueCeil = new TransformGroup();
		shap= new Shape3D(flagstone,matBlueCeil);
		BlueCeil.addChild(shap);
		auxtransform=new Transform3D(rottecho, postecho,1.0);
		BlueCeil.setTransform(auxtransform);
		
		
		/////paredes
		
		/*DummyWall = new TransformGroup();
		shap= new Shape3D(Cuboide,matElevator);
		DummyWall.addChild(shap);
		auxtransform=new Transform3D(rotpared, pospared,1.0);
		DummyWall.setTransform(auxtransform);*/
		
						
		RedWall = new TransformGroup();
		shap= new Shape3D(Cuboide,matRedWall);
		RedWall.addChild(shap);
		auxtransform=new Transform3D(rotpared, pospared,1.0);
		RedWall.setTransform(auxtransform);
		
		
		
		
		
		BlueWall = new TransformGroup();
		shap= new Shape3D(Cuboide,matBlueWall);
		BlueWall.addChild(shap);
		auxtransform=new Transform3D(rotpared, pospared,1.0);
		BlueWall.setTransform(auxtransform);

		
		
		RedBaseWall = new TransformGroup();
		shap= new Shape3D(Cuboide,matRedBaseWall);
		RedBaseWall.addChild(shap);
		auxtransform=new Transform3D(rotpared, pospared,1.0);
		RedBaseWall.setTransform(auxtransform);
		
		
		BlueBaseWall = new TransformGroup();
		shap= new Shape3D(Cuboide,matBlueBaseWall);
		BlueBaseWall.addChild(shap);
		auxtransform=new Transform3D(rotpared, pospared,1.0);
		BlueBaseWall.setTransform(auxtransform);
		
		////crear el  altar rojo
		RedAltar = new TransformGroup();		
		///crear el piso del altar		
		RedAltar.addChild(RedFloor);
		///crear el techo del altar
		RedAltar.addChild(RedCeil);

		///crear la primera base
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matRedAltar);				
		auxnode.addChild(shap);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(0.0,0.125,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedAltar.addChild(auxnode);
		///crear la segunda base
		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matRedAltar);				
		auxnode.addChild(shap);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,2*CELLWIDTH/3);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,2*CELLWIDTH/3);
		mvec= new Vector3d(0.0,0.25,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedAltar.addChild(auxnode);
		
		///crear la tercera base
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matRedAltar);				
		auxnode.addChild(shap);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH/3);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH/3);
		mvec= new Vector3d(0.0,0.375,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedAltar.addChild(auxnode);
		
		
		////crear el  altar azul
		BlueAltar = new TransformGroup();		
		///crear el piso del altar		
		BlueAltar.addChild(BlueFloor);
		///crear el techo del altar
		BlueAltar.addChild(BlueCeil);

		///crear la primera base
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matBlueAltar);				
		auxnode.addChild(shap);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(0.0,0.125,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueAltar.addChild(auxnode);

		///crear la segunda base
		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matBlueAltar);				
		auxnode.addChild(shap);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,2*CELLWIDTH/3);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,2*CELLWIDTH/3);
		mvec= new Vector3d(0.0,0.25,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueAltar.addChild(auxnode);		
		///crear la tercera base
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matBlueAltar);				
		auxnode.addChild(shap);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH/3);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH/3);
		mvec= new Vector3d(0.0,0.375,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueAltar.addChild(auxnode);
		/////
		
		////portal de salida del 
		RedSource = new TransformGroup();
		
		
		////crear el piso de abajo
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matRedSource);				
		auxnode.addChild(shap);			
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(0.0,0.05,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedSource.addChild(auxnode);
		////crear el techo
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matRedSource);				
		auxnode.addChild(shap);					
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(0.0,CELLHEIGHT-0.05,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedSource.addChild(auxnode);

		
		/////crear el cilindro teletransportador		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matElevator);				
		auxnode.addChild(shap);					
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH/4);
		matrot.setElement(1,1,CELLHEIGHT-0.1);
		matrot.setElement(2,2,CELLWIDTH/4);
		mvec= new Vector3d(0.0,CELLHEIGHT/2,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedSource.addChild(auxnode);
		
		
		/////crear las 2 paredes
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matRedBaseWall);				
		auxnode.addChild(shap);					
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,0.25);
		matrot.setElement(1,1,CELLHEIGHT);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(CELLWIDTH/2,CELLHEIGHT/2,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedSource.addChild(auxnode);
		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matRedBaseWall);				
		auxnode.addChild(shap);					
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,CELLHEIGHT);
		matrot.setElement(2,2,0.25);
		mvec= new Vector3d(0.0,CELLHEIGHT/2,CELLWIDTH/2);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		RedSource.addChild(auxnode);

		
		////portal de salida azul
		BlueSource = new TransformGroup();
		
		
		////crear el piso de abajo
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matBlueSource);				
		auxnode.addChild(shap);			
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(0.0,0.05,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueSource.addChild(auxnode);
		////crear el techo
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matBlueSource);				
		auxnode.addChild(shap);					
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(0.0,CELLHEIGHT-0.05,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueSource.addChild(auxnode);

		
		/////crear el cilindro teletransportador		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cilindro,matElevator);				
		auxnode.addChild(shap);					
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH/4);
		matrot.setElement(1,1,CELLHEIGHT-0.1);
		matrot.setElement(2,2,CELLWIDTH/4);
		mvec= new Vector3d(0.0,CELLHEIGHT/2,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueSource.addChild(auxnode);
		
		
		/////crear las 2 paBluees
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matBlueBaseWall);				
		auxnode.addChild(shap);					
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,0.25);
		matrot.setElement(1,1,CELLHEIGHT);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(CELLWIDTH/2,CELLHEIGHT/2,0.0);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueSource.addChild(auxnode);
		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide,matBlueBaseWall);				
		auxnode.addChild(shap);					
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,CELLHEIGHT);
		matrot.setElement(2,2,0.25);
		mvec= new Vector3d(0.0,CELLHEIGHT/2,CELLWIDTH/2);
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);
		BlueSource.addChild(auxnode);
		
		////crear el elevador		
		
			
		Elevator = new TransformGroup();		
		shap= new Shape3D(Cilindro,matElevator);				
		Elevator.addChild(shap);		
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH/2);
		matrot.setElement(1,1,2*CELLHEIGHT-0.2);
		matrot.setElement(2,2,CELLWIDTH/2);
		mvec= new Vector3d(0.0,CELLHEIGHT,0.0);
		
		auxtransform=new Transform3D(matrot, mvec,1.0);
		Elevator.setTransform(auxtransform);		
			
		/////crear el cuarto del elevador
		ElevatorRoom = new TransformGroup();
		
		
		/////crear el piso de abajo
		matrot= new Matrix3d();
		matrot.setElement(0,0,3*CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,3*CELLWIDTH);
		mvec= new Vector3d(0.0,-0.125,0.0);		
		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);	
		
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		/////crear el piso de upstairs
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,3*CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,3*CELLWIDTH);
		mvec= new Vector3d(0.0,2*CELLHEIGHT,0.0);
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);

		
		
		/////crear el piso de intermedio
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,(5/4)*CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,3*CELLWIDTH);
		mvec= new Vector3d(-(9/8)*CELLWIDTH,CELLHEIGHT,0.0);
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,(5/4)*CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,3*CELLWIDTH);
		mvec= new Vector3d((9/8)*CELLWIDTH,CELLHEIGHT,0.0);		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);

		
		matrot= new Matrix3d();
		matrot.setElement(0,0,(5/4)*CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,(5/4)*CELLWIDTH);
		mvec= new Vector3d(0.0,CELLHEIGHT,-(9/8)*CELLWIDTH);
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,(5/4)*CELLWIDTH);
		matrot.setElement(1,1,0.25);
		matrot.setElement(2,2,(5/4)*CELLWIDTH);
		mvec= new Vector3d(0.0,CELLHEIGHT,(9/8)*CELLWIDTH);
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);


		///crear las paredes
		double solapa= 3.0/2.0;		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,0.25);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(-solapa*CELLWIDTH,CELLHEIGHT,-CELLWIDTH);
		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,0.25);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,CELLWIDTH);		
		mvec= new Vector3d(-solapa*CELLWIDTH,CELLHEIGHT,CELLWIDTH);
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);



		
		matrot= new Matrix3d();
		matrot.setElement(0,0,0.25);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(solapa*CELLWIDTH,CELLHEIGHT,-CELLWIDTH);
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,0.25);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,CELLWIDTH);
		mvec= new Vector3d(solapa*CELLWIDTH,CELLHEIGHT,CELLWIDTH);
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,0.25);
		mvec= new Vector3d(-CELLWIDTH,CELLHEIGHT,-solapa*CELLWIDTH);		
		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,0.25);
		mvec= new Vector3d(CELLWIDTH,CELLHEIGHT,-solapa*CELLWIDTH);		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,0.25);
		mvec= new Vector3d(-CELLWIDTH,CELLHEIGHT,solapa*CELLWIDTH);		
	
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);
		
		matrot= new Matrix3d();
		matrot.setElement(0,0,CELLWIDTH);
		matrot.setElement(1,1,2*CELLHEIGHT);
		matrot.setElement(2,2,0.25);
		mvec= new Vector3d(CELLWIDTH,CELLHEIGHT,solapa*CELLWIDTH);		
		auxnode = new TransformGroup();
		shap= new Shape3D(Cuboide, matElevatorWall);
		auxnode.addChild(shap);			
		auxtransform=new Transform3D(matrot, mvec,1.0);
		auxnode.setTransform(auxtransform);		
		ElevatorRoom.addChild(auxnode);

	}
	
	
	public int addEntity(TransformGroup entity)
	{
		
		PrimitiveIndex pm;
		////vetificar si no ha sido asignado
		pm = (PrimitiveIndex) entity.getUserData();
		if(pm!=null)
		{
			return -1;////ya ha sido agregado a la escena			 
		}
		pm = new PrimitiveIndex();
		pm.index= m_staticentities.numChildren();
		entity.setUserData(pm);
		m_staticentities.addChild(entity);
		return pm.index;		
	}
	
	public void recalcEntitiesIndices()
	{		
		PrimitiveIndex pm;
		TransformGroup tg;
		int i, m;
		m=m_staticentities.numChildren();
		System.out.println("numero de entidades" + m);
		for(i=0;i<m;i++)
		{
			tg=(TransformGroup) m_staticentities.getChild(i); 
			pm = new PrimitiveIndex();
			pm.index= i;
			tg.setUserData(pm);
		}
		////crear la masface de visibilidad
		
		m_staticentities.setWhichChild(Switch.CHILD_ALL);
		m_numchildren = m;
		m_visiblemask= new BitSet(m_numchildren);
		//////m= m_visiblemask.length();
				
		m_scene.addChild(m_staticentities);
		///m_scene.compile();
		
	}
	
	
	/////retorna nulo si el recuadro esta por fuera del campo de vision
	////m_vecdir apunta hacia donde enfoca la camara (Eje Z profundidad)
	////m_vecside es perpendicular a m_vecdir e indica
	////la inclinacion de la camara (Eje X) ; se usa para el campò de vision horizontal
	//////face: entero que indica la face a evaluar
	//////0 : top
	//////1 : bottom
	//////2 : left
	//////3  : right
	protected boolean clipFrustum(int face ,Cell source, Cell result)
	{	
		result.leftTangent= source.leftTangent;
		result.rightTangent= source.rightTangent;
		
		double frontdistance=0.0, sidedistance1=0.0, sidedistance2=0.0;		
		int negcount=0;
		boolean solid[]= new boolean[2];
		Vector3d corner1= new Vector3d();
		Vector3d corner2= new Vector3d();
		Vector3d vdiff= new Vector3d();
		
		////obtener el portal
		getCellPortalCorners(source, face, corner1, corner2, solid);
		/*solid[0]=false;
		solid[1]=false;*/
		/////obtener el minimo y el maximo tangente	
		vdiff.set(corner1);
		vdiff.sub(m_vecpos);
		
		frontdistance = m_vecdir.dot(vdiff);		
		///vdiff.normalize();
		sidedistance1 = m_vecside.dot(vdiff);					
		if(frontdistance<0.0001)
		{
			/*if(sidedistance1>source.rightTangent) sidedistance1=source.rightTangent;
			else if(sidedistance1<source.leftTangent) sidedistance1=source.leftTangent;			*/
			 negcount++;
		}
		else
		{
			sidedistance1 = sidedistance1/frontdistance;
		}
		
		
		
		
		////probar el otro punto
					
		vdiff.set(corner2);
		vdiff.sub(m_vecpos);
		
		frontdistance = m_vecdir.dot(vdiff);
		///vdiff.normalize();
		sidedistance2 = m_vecside.dot(vdiff);	
		
		if(frontdistance<0.001)
		{
			/*if(sidedistance2>source.rightTangent) sidedistance2=source.rightTangent;
			else if(sidedistance2<source.leftTangent) sidedistance2=source.leftTangent;			*/
			negcount++;
		}
		else
		{
			sidedistance2 = sidedistance2/frontdistance;
		}
		
		
		///System.out.println("sidedistance2 " + sidedistance2);
		
		/////////probar si el recuadro encaja en el campo de vision		
		if(negcount>1)///esta por detras de la camara
		{
			 ///System.out.println("Por Negao");
			 return false;
		}
		
		if(sidedistance2>=sidedistance1)
		{
			if(sidedistance1>=source.rightTangent)
			{
				return false;	
			}
			if(sidedistance2<=source.leftTangent)
			{
				return false;	
			}
			if(sidedistance2<source.rightTangent&&solid[1])
			{
				result.rightTangent= sidedistance2;
			}
			if(sidedistance1>source.leftTangent&&solid[0])
			{
				result.leftTangent= sidedistance1;
			}	
			
						
		}
		else
		{
			if(sidedistance2>=source.rightTangent)
			{
				return false;	
			}
			if(sidedistance1<=source.leftTangent)
			{
				return false;	
			}
			if(sidedistance1<source.rightTangent&&solid[0])
			{
				result.rightTangent= sidedistance1;
			}
			if(sidedistance2>source.leftTangent&&solid[1])
			{
				result.leftTangent= sidedistance2;
			}			
		}
				
		return true;
		
		
	}

	//////marca las entidaes de una cell como visibles
	protected void markEntitiesOnCell(Cell  cel)
	{
		TransformGroup auxnode;
		PrimitiveIndex pm;	
		int i;		
		for(i=0;i<cel.m_entities.size();i++)
		{
			auxnode = (TransformGroup)cel.m_entities.get(i);
			pm= (PrimitiveIndex) auxnode.getUserData();
			m_visiblemask.set(pm.index);
		}		
	}
	
	
	
	
	//////rutina que permite calcular la visibilidad en el laberinto
	public void checkVisibleEntities(Vector3d vecdir,Vector3d vecside, Vector3d vecpos ,double fov)
	{
		
		////////limpiar las entidades que han sido gradicadas la anterior vez
		
		int i;					
		int expandedCount=0;		
		Cell cell;
		
		for(i=0;i<m_numchildren;i++)
		{
			m_visiblemask.clear(i);				
		}
		while(markedstack.empty()==false)
		{
			warpcell=(Cell)markedstack.pop();			
			warpcell.mark2=false;
		}
		m_vecdir= vecdir;
		m_vecside= vecside;
		m_vecpos = vecpos;
					////la primera cell de la cual se parte		
		warpcell = getCellByPosition(vecpos);		
			
		
			////preparar el campo de vision
		warpcell.rightTangent=Math.tan(fov);
		warpcell.leftTangent=-Math.tan(fov);
		
			
		cellstack.add(warpcell);				
		
		while(cellstack.empty()==false)
		{				
			expandedCount=cellstack.size();
			
			/////explorar la cell
			////ramificar las cells adyacentes
			while(expandedCount>0)
			{
				expandedCount--;
				cell = (Cell)cellstack.remove(0);
				if(cell.mark2==false)
				{
					cell.mark2= true;
					markEntitiesOnCell(cell);
					markedstack.push(cell);
				}
				for(i=0;i<4;i++)
				{
					
					if(getCellBound(cell,i)==false)
					{
						warpcell = getCellNeighbor(cell,i);
						if(warpcell.mark2==false)
						{
							if(clipFrustum(i,cell,warpcell)==true)
							{
								cellstack.add(warpcell);
							}													
						}
					}
				}				
			}
		}		
				
		/////asignar la masface 
		m_staticentities.setWhichChild(Switch.CHILD_MASK);
		m_staticentities.setChildMask(m_visiblemask);
			
	}
	
	
	public BranchGroup getScene()
	{
		return m_scene;			
	}
	public void createScene()
	{
		int i, j,k;
		
		TransformGroup auxnode;
		Transform3D auxtransform;		
		Cell auxcell, auxcell2, auxcell3;		
		
		//////crear el laberinto
		generateLaberinth();	
		
		/////crear los elevadores
		elevators= new TransformGroup[elevatorCount];
		elevatorooms= new TransformGroup[elevatorCount];
		for(i=0;i<elevatorCount;i++)
		{
			////crear el cuarto del elevador
			elevatorooms[i]= new TransformGroup();
			elevatorooms[i].addChild(ElevatorRoom.cloneTree(false));
			elevators[i] = new TransformGroup();
			elevators[i].addChild(Elevator.cloneTree(false));
			////obtener la posicion	
						
			auxtransform = new Transform3D();
			auxtransform.setTranslation(getCellPosition(elevator_row[i]+1,elevator_col[i]+1,false));
			elevatorooms[i].setTransform(auxtransform);
			elevators[i].setTransform(auxtransform);			
			
		}
		
		//////////asignar los modelos a las cells
		/////cells de abajo
		for(i=0;i<20;i++)
		{
			for(j=0;j<20;j++)
			{
				auxcell=getDownstairsCell(i,j);
				if(auxcell.mark==Cell.ELEVATORROOM)
				{							/////encontrar el ascensor
					for(k=0;k<elevatorCount;k++)
					{
						if(j>=elevator_col[k]&&j<=elevator_col[k]+3)
						{
							if(i>=elevator_row[k]&&i<=elevator_row[k]+3)
							{
								auxcell.m_entities.add(elevatorooms[k]);
								if(i==0)
								{
									if(j==elevator_col[k]+1)
									{
										auxnode = new TransformGroup();
										auxnode.addChild(RedWall.cloneTree(false));
										getCellTopWallTrans(i,j,false,auxnode);
										auxcell.m_entities.add(auxnode); 
									}	
								}
								
								if(i<19)////agregar al siguiente
								{	
									auxcell2=getDownstairsCell(i+1,j);
									auxcell2.m_entities.add(elevatorooms[k]);
								}
								else if(j==elevator_col[k]+1)
								{
									auxnode = new TransformGroup();
									auxnode.addChild(RedWall.cloneTree(false));
									getCellBottomWallTrans(i,j,false,auxnode);
									auxcell.m_entities.add(auxnode); 

								}	
									
									
								if(j==0)
								{
									if(i==elevator_row[k]+1)	
									{
										auxnode = new TransformGroup();
										auxnode.addChild(RedWall.cloneTree(false));
										getCellLeftWallTrans(i,j,false,auxnode);
										auxcell.m_entities.add(auxnode); 
	
									}
								}	
								if(j<19)////agregar al siguiente
								{	
									auxcell2=getDownstairsCell(i,j+1);
									auxcell2.m_entities.add(elevatorooms[k]);
								}
								else if(i==elevator_row[k]+1)
								{
										auxnode = new TransformGroup();
										auxnode.addChild(RedWall.cloneTree(false));
										getCellRightWallTrans(i,j,false,auxnode);
										auxcell.m_entities.add(auxnode); 
	
								}

								break;
							}
						}
					}
					
				}
				else if(auxcell.mark==Cell.ELEVATOR)
				{
					for(k=0;k<elevatorCount;k++)
					{
						if(j>=elevator_col[k]&&j<=elevator_col[k]+3)
						{
							if(i>=elevator_row[k]&&i<=elevator_row[k]+3)
							{
								auxcell.m_entities.add(elevators[k]);
								auxcell.m_entities.add(elevatorooms[k]);
								break;
							}
						}
					}										
					
				}
				else if(auxcell.mark==Cell.REDBASE)
				{	
					auxnode =  new TransformGroup();
					auxnode.addChild(RedCeil.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,false));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
			
					auxnode =  new TransformGroup();
					auxnode.addChild(RedFloor.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,false));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
			

								
					if(i==0)////agregar la pared anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(RedBaseWall.cloneTree(false));
						getCellTopWallTrans(i,j,false,auxnode);	
						auxcell.m_entities.add(auxnode);

					}
					if(i<19&&auxcell.SouthBound==true)
					{
						auxcell2=getDownstairsCell(i+1,j);
						auxnode = new TransformGroup();
						auxnode.addChild(RedBaseWall.cloneTree(false));
						getCellBottomWallTrans(i,j,false,auxnode);	
						auxcell.m_entities.add(auxnode); 							
						auxcell2.m_entities.add(auxnode);						
					}
		
					else if(i==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(RedBaseWall.cloneTree(false));
						getCellBottomWallTrans(i,j,false,auxnode);			

						auxcell.m_entities.add(auxnode);												
					}
					
					if(j==0)////agregar la pared anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(RedBaseWall.cloneTree(false));
						getCellLeftWallTrans(i,j,false,auxnode);				
						auxcell.m_entities.add(auxnode); 
			

					}
					if(j<19&&auxcell.EastBound==true)
					{
						
						auxcell2=getDownstairsCell(i,j+1);						
						auxnode = new TransformGroup();
						auxnode.addChild(RedBaseWall.cloneTree(false));
						getCellRightWallTrans(i,j,false,auxnode);								
			

						auxcell.m_entities.add(auxnode); 							
						auxcell2.m_entities.add(auxnode);												
					}
					
					
						
					else if(j==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(RedBaseWall.cloneTree(false));
						getCellRightWallTrans(i,j,false,auxnode);				
									
						auxcell.m_entities.add(auxnode);					
					}					
		
				}
				
				else if(auxcell.mark==Cell.REDSOURCE)
				{					
					auxnode =  new TransformGroup();
					auxnode.addChild(RedSource.cloneTree(false));
					auxtransform= new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,false));
					auxnode.setTransform(auxtransform);
			
					auxcell.m_entities.add(auxnode); 
					if(i<19)					
					{
						auxcell2=getDownstairsCell(i+1,j);
						auxcell2.m_entities.add(auxnode); 
					}
					if(j<19)					
					{
						auxcell2=getDownstairsCell(i,j+1);
						auxcell2.m_entities.add(auxnode); 
					}				
					
				}
				else if(auxcell.mark==Cell.REDALTAR)
				{	
					auxnode =  new TransformGroup();
					auxnode.addChild(RedAltar.cloneTree(true));
					auxtransform= new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,false));
					auxnode.setTransform(auxtransform);			
					
					auxcell.m_entities.add(auxnode); 					
				}
				else
				{	
					auxnode =  new TransformGroup();
					auxnode.addChild(RedCeil.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,false));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
			
					auxnode =  new TransformGroup();
					auxnode.addChild(RedFloor.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,false));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
		
					if(i==0)////agregar la pared anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(RedWall.cloneTree(false));
						getCellTopWallTrans(i,j,false,auxnode);	
						auxcell.m_entities.add(auxnode);

					}
					
					if(i<19&&auxcell.SouthBound==true)
					{
						auxcell2=getDownstairsCell(i+1,j);						
						if(auxcell2.mark==Cell.REDBASE)
						{
							auxnode = new TransformGroup();
							auxnode.addChild(RedBaseWall.cloneTree(false));
							getCellBottomWallTrans(i,j,false,auxnode);								
													
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode); 							
						}
						else if(auxcell2.mark==Cell.ELEVATORROOM)
						{
							/////encontrar el ascensor
							for(k=0;k<elevatorCount;k++)
							{
								if(j>=elevator_col[k]&&j<=elevator_col[k]+3)
								{
									if(i+1==elevator_row[k])
									{
										auxcell.m_entities.add(elevatorooms[k]);
										break;
									}
								}
							}
									
						}
						else
						{
							auxnode = new TransformGroup();
							auxnode.addChild(RedWall.cloneTree(false));
							getCellBottomWallTrans(i,j,false,auxnode);
			
							
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode);
						}
					}					
					
					
					else if(i==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(RedWall.cloneTree(false));
						getCellBottomWallTrans(i,j,false,auxnode);				
						
						auxcell.m_entities.add(auxnode);
						
					}

					
					if(j==0)////agregar la pared anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(RedWall.cloneTree(false));
						getCellLeftWallTrans(i,j,false,auxnode);				
			
					
						auxcell.m_entities.add(auxnode); 
					}
					if(j<19&&auxcell.EastBound== true)
					{
						auxcell2=getDownstairsCell(i,j+1);						
						if(auxcell2.mark==Cell.REDBASE)
						{
							auxnode = new TransformGroup();
							auxnode.addChild(RedBaseWall.cloneTree(false));
							getCellRightWallTrans(i,j,false,auxnode);				
			
							
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode);
						}
						else if(auxcell2.mark==Cell.ELEVATORROOM)
						{
							/////encontrar el ascensor
							for(k=0;k<elevatorCount;k++)
							{
								if(i>=elevator_row[k]&&i<=elevator_row[k]+3)
								{
									if(j+1==elevator_col[k])
									{
										auxcell.m_entities.add(elevatorooms[k]);
										break;
									}
								}
							}
									
						}
						else
						{
							auxnode = new TransformGroup();
							auxnode.addChild(RedWall.cloneTree(false));
							getCellRightWallTrans(i,j,false,auxnode);											
			
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode);
						}
					}
					
					
					
					
					
					else if(j==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(RedWall.cloneTree(false));
						getCellRightWallTrans(i,j,false,auxnode);									
						auxcell.m_entities.add(auxnode);					
					}						
					
				}
			}	
		}		
	
		/////cells de upstairs
		for(i=0;i<20;i++)
		{
			for(j=0;j<20;j++)
			{
				auxcell=getUpstairsCell(i,j);
				if(auxcell.mark==Cell.ELEVATORROOM)
				{							/////encontrar el ascensor
					for(k=0;k<elevatorCount;k++)
					{
						if(j>=elevator_col[k]&&j<=elevator_col[k]+3)
						{
							if(i>=elevator_row[k]&&i<=elevator_row[k]+3)
							{
								auxcell.m_entities.add(elevatorooms[k]);
								
								if(i==0)
								{
									if(j==elevator_col[k]+1)
									{
										auxnode = new TransformGroup();
										auxnode.addChild(BlueWall.cloneTree(false));
										getCellTopWallTrans(i,j,true,auxnode);
										auxcell.m_entities.add(auxnode); 
									}	
								}
								
								if(i<19)////agregar al siguiente
								{	
									auxcell2=getDownstairsCell(i+1,j);
									auxcell2.m_entities.add(elevatorooms[k]);
								}
								else if(j==elevator_col[k]+1)
								{
									auxnode = new TransformGroup();
									auxnode.addChild(BlueWall.cloneTree(false));
									getCellBottomWallTrans(i,j,true,auxnode);
									auxcell.m_entities.add(auxnode); 

								}	
									
									
								if(j==0)
								{
									if(i==elevator_row[k]+1)	
									{
										auxnode = new TransformGroup();
										auxnode.addChild(BlueWall.cloneTree(false));
										getCellLeftWallTrans(i,j,true,auxnode);
										auxcell.m_entities.add(auxnode); 
	
									}
								}	
								if(j<19)////agregar al siguiente
								{	
									auxcell2=getDownstairsCell(i,j+1);
									auxcell2.m_entities.add(elevatorooms[k]);
								}
								else if(i==elevator_row[k]+1)
								{
										auxnode = new TransformGroup();
										auxnode.addChild(BlueWall.cloneTree(false));
										getCellRightWallTrans(i,j,true,auxnode);
										auxcell.m_entities.add(auxnode); 
	
								}
								break;
							}
						}
					}
					
				}
				else if(auxcell.mark==Cell.ELEVATOR)
				{
					for(k=0;k<elevatorCount;k++)
					{
						if(j>=elevator_col[k]&&j<=elevator_col[k]+3)
						{
							if(i>=elevator_row[k]&&i<=elevator_row[k]+3)
							{
								auxcell.m_entities.add(elevators[k]);
								auxcell.m_entities.add(elevatorooms[k]);
								break;
							}
						}
					}										
					
				}
				else if(auxcell.mark==Cell.BLUEBASE)
				{	
					auxnode =  new TransformGroup();
					auxnode.addChild(BlueCeil.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,true));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
			
					auxnode =  new TransformGroup();
					auxnode.addChild(BlueFloor.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,true));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
			

								
					if(i==0)////agregar la paBlue anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(BlueBaseWall.cloneTree(false));
						getCellTopWallTrans(i,j,true,auxnode);	
						auxcell.m_entities.add(auxnode);

					}
					if(i<19&&auxcell.SouthBound==true)
					{
						auxcell2=getUpstairsCell(i+1,j);
						auxnode = new TransformGroup();
						auxnode.addChild(BlueBaseWall.cloneTree(false));
						getCellBottomWallTrans(i,j,true,auxnode);	
						auxcell.m_entities.add(auxnode); 							
						auxcell2.m_entities.add(auxnode);						
					}
					else if(i==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(BlueBaseWall.cloneTree(false));
						getCellBottomWallTrans(i,j,true,auxnode);			

						auxcell.m_entities.add(auxnode);												
					}
					
					if(j==0)////agregar la paBlue anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(BlueBaseWall.cloneTree(false));
						getCellLeftWallTrans(i,j,true,auxnode);				
						auxcell.m_entities.add(auxnode); 
			

					}
					if(j<19&&auxcell.EastBound==true)
					{
						
						auxcell2=getUpstairsCell(i,j+1);						
						auxnode = new TransformGroup();
						auxnode.addChild(BlueBaseWall.cloneTree(false));
						getCellRightWallTrans(i,j,true,auxnode);								
			

						auxcell.m_entities.add(auxnode); 							
						auxcell2.m_entities.add(auxnode);												
					}	
					else if(j==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(BlueBaseWall.cloneTree(false));
						getCellRightWallTrans(i,j,true,auxnode);				
									
						auxcell.m_entities.add(auxnode);					
					}					
		
				}
				
				else if(auxcell.mark==Cell.BLUESOURCE)
				{					
					auxnode =  new TransformGroup();
					auxnode.addChild(BlueSource.cloneTree(false));
					auxtransform= new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,true));
					auxnode.setTransform(auxtransform);
			
					auxcell.m_entities.add(auxnode); 
					if(i<19)					
					{
						auxcell2=getUpstairsCell(i+1,j);
						auxcell2.m_entities.add(auxnode); 
					}
					if(j<19)					
					{
						auxcell2=getUpstairsCell(i,j+1);
						auxcell2.m_entities.add(auxnode); 
					}				
					
				}
				else if(auxcell.mark==Cell.BLUEALTAR)
				{	
					auxnode =  new TransformGroup();
					auxnode.addChild(BlueAltar.cloneTree(true));
					auxtransform= new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,true));
					auxnode.setTransform(auxtransform);			
					
					auxcell.m_entities.add(auxnode); 					
				}
				else
				{	
					auxnode =  new TransformGroup();
					auxnode.addChild(BlueCeil.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,true));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
			
					auxnode =  new TransformGroup();
					auxnode.addChild(BlueFloor.cloneTree(false));
					auxtransform = new Transform3D();
					auxtransform.setTranslation(getCellPosition(i,j,true));
					auxnode.setTransform(auxtransform);
					auxcell.m_entities.add(auxnode); 
		
					if(i==0)////agregar la paBlue anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(BlueWall.cloneTree(false));
						getCellTopWallTrans(i,j,true,auxnode);	
						auxcell.m_entities.add(auxnode);

					}
					
					if(i<19&&auxcell.SouthBound==true)
					{
						auxcell2=getUpstairsCell(i+1,j);						
						if(auxcell2.mark==Cell.BLUEBASE)
						{
							auxnode = new TransformGroup();
							auxnode.addChild(BlueBaseWall.cloneTree(false));
							getCellBottomWallTrans(i,j,true,auxnode);								
													
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode); 							
						}
						else if(auxcell2.mark==Cell.ELEVATORROOM)
						{
							/////encontrar el ascensor
							for(k=0;k<elevatorCount;k++)
							{
								if(j>=elevator_col[k]&&j<=elevator_col[k]+3)
								{
									if(i+1==elevator_row[k])
									{
										auxcell.m_entities.add(elevatorooms[k]);
										break;
									}
								}
							}
									
						}
						else
						{
							auxnode = new TransformGroup();
							auxnode.addChild(BlueWall.cloneTree(false));
							getCellBottomWallTrans(i,j,true,auxnode);
			
							
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode);
						}
					}					
					else if(i==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(BlueWall.cloneTree(false));
						getCellBottomWallTrans(i,j,true,auxnode);				
						
						auxcell.m_entities.add(auxnode);
						
					}

					
					if(j==0)////agregar la paBlue anterior
					{	
						auxnode = new TransformGroup();
						auxnode.addChild(BlueWall.cloneTree(false));
						getCellLeftWallTrans(i,j,true,auxnode);				
			
					
						auxcell.m_entities.add(auxnode); 
					}
					if(j<19&&auxcell.EastBound== true)
					{
						auxcell2=getUpstairsCell(i,j+1);						
						if(auxcell2.mark==Cell.BLUEBASE)
						{
							auxnode = new TransformGroup();
							auxnode.addChild(BlueBaseWall.cloneTree(false));
							getCellRightWallTrans(i,j,true,auxnode);				
			
							
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode);
						}
						else if(auxcell2.mark==Cell.ELEVATORROOM)
						{
							/////encontrar el ascensor
							for(k=0;k<elevatorCount;k++)
							{
								if(i>=elevator_row[k]&&i<=elevator_row[k]+3)
								{
									if(j+1==elevator_col[k])
									{
										auxcell.m_entities.add(elevatorooms[k]);
										break;
									}
								}
							}
									
						}
						else
						{
							auxnode = new TransformGroup();
							auxnode.addChild(BlueWall.cloneTree(false));
							getCellRightWallTrans(i,j,true,auxnode);											
			
							auxcell.m_entities.add(auxnode); 							
							auxcell2.m_entities.add(auxnode);
						}
					}
					else if(j==19)
					{
						auxnode = new TransformGroup();
						auxnode.addChild(BlueWall.cloneTree(false));
						getCellRightWallTrans(i,j,true,auxnode);									
						auxcell.m_entities.add(auxnode);					
					}						
					
				}
			}	
		}		
	
		
		/////rutina de prueba
		/////Marcar a todos los objetos
		PrimitiveIndex pm;
		for(i=0;i<800;i++)
		{
			auxcell= cells[i];
			for(j=0;j<auxcell.m_entities.size();j++)
			{
				auxnode=(TransformGroup) auxcell.m_entities.get(j);
				addEntity(auxnode);	
			}	
		}
		
		recalcEntitiesIndices();
	
	}
	
	protected void generateLaberinth()
	{
		int  i, j,k, l;
		Cell auxcell, auxcell2;		
		for(i=0;i<800;i++)
		{
			cells[i]= new Cell();	
			if(i<400)
			{
				cells[i].m_upstairs = false;
				cells[i].m_row = i/20;
				cells[i].m_column = i%20;				
			}
			else
			{
				cells[i].m_upstairs = true;
				cells[i].m_row = (i-400)/20;
				cells[i].m_column = i%20;				
			}			
		}
		
		int baserojax,baserojay, baseazulx, baseazuly; ////posisiones en la matriz de las bases
		
		boolean validascensor;
		
		//////ubicar la base roja en la primera planta
		
		baserojax = (int)Math.round(15.0f*Math.random());	
		baserojay = (int)Math.round(15.0f*Math.random());	
		/////configurar la base roja
		
		////colocar los bordes
		if(baserojay>0) //// colocar la pared
		{			
			auxcell = getDownstairsCell(baserojay-1,baserojax);
			auxcell.SouthBound= true;/////pared anterior
			auxcell = getDownstairsCell(baserojay-1,baserojax+1);
			auxcell.SouthBound= true;/////pared anterior
			auxcell = getDownstairsCell(baserojay-1,baserojax+3);
			auxcell.SouthBound= true;/////pared anterior
			auxcell = getDownstairsCell(baserojay-1,baserojax+4);
			auxcell.SouthBound= true;/////pared anterior			
		}
		
		/////colocar los bordes de abajo
		auxcell = getDownstairsCell(baserojay+4,baserojax);
		auxcell.SouthBound= true;/////pared anterior
		auxcell = getDownstairsCell(baserojay+4,baserojax+1);		
		auxcell.SouthBound= true;/////pared anterior
		if(baserojay==15)
		{
			auxcell = getDownstairsCell(baserojay+4,baserojax+2);
			auxcell.SouthBound= true;/////pared anterior
		}
		auxcell = getDownstairsCell(baserojay+4,baserojax+3);
		auxcell.SouthBound= true;/////pared anterior
		auxcell = getDownstairsCell(baserojay+4,baserojax+4);
		auxcell.SouthBound= true;/////pared anterior			
		

		
		if(baserojax>0) //// colocar la pared
		{			
			auxcell = getDownstairsCell(baserojay,baserojax-1);
			auxcell.EastBound= true;/////pared anterior
			auxcell = getDownstairsCell(baserojay+1,baserojax-1);
			auxcell.EastBound= true;/////pared anterior
			auxcell = getDownstairsCell(baserojay+3,baserojax-1);
			auxcell.EastBound= true;/////pared anterior
			auxcell = getDownstairsCell(baserojay+4,baserojax-1);
			auxcell.EastBound= true;/////pared anterior
		}		
		
		
		/////colocar los bordes de la derecha 
		auxcell = getDownstairsCell(baserojay,baserojax+4);
		auxcell.EastBound= true;/////pared anterior
		auxcell = getDownstairsCell(baserojay+1,baserojax+4);		
		auxcell.EastBound= true;/////pared anterior
		if(baserojax==15)
		{
			auxcell = getDownstairsCell(baserojay+2,baserojax+4);
			auxcell.EastBound= true;/////pared anterior
		}
		auxcell = getDownstairsCell(baserojay+3,baserojax+4);
		auxcell.EastBound= true;/////pared anterior
		auxcell = getDownstairsCell(baserojay+4,baserojax+4);
		auxcell.EastBound= true;/////pared anterior			
		
		for(i=0;i<5;i++)
		{
			for(j=0;j<5;j++)
			{
				auxcell = getDownstairsCell(baserojay+i,baserojax+j);
				auxcell.mark=Cell.REDBASE;
			}
		}
		
		auxcell = getDownstairsCell(baserojay+2,baserojax+2);
		auxcell.mark=Cell.REDALTAR;
		auxcell = getDownstairsCell(baserojay+4,baserojax+4);
		auxcell.mark=Cell.REDSOURCE;
		
		//////ubicar la base azul en la SEGUNDA planta
		
		baseazulx = (int)Math.round(15.0f*Math.random());	
		baseazuly = (int)Math.round(15.0f*Math.random());		
		
		////colocar los bordes
		if(baseazuly>0) //// colocar la pared
		{			
			auxcell = getUpstairsCell(baseazuly-1,baseazulx);
			auxcell.SouthBound= true;/////pared anterior
			auxcell = getUpstairsCell(baseazuly-1,baseazulx+1);
			auxcell.SouthBound= true;/////pared anterior
			auxcell = getUpstairsCell(baseazuly-1,baseazulx+3);
			auxcell.SouthBound= true;/////pared anterior
			auxcell = getUpstairsCell(baseazuly-1,baseazulx+4);
			auxcell.SouthBound= true;/////pared anterior			
		}
		
		/////colocar los bordes de abajo
		auxcell = getUpstairsCell(baseazuly+4,baseazulx);
		auxcell.SouthBound= true;/////pared anterior
		auxcell = getUpstairsCell(baseazuly+4,baseazulx+1);		
		auxcell.SouthBound= true;/////pared anterior
		if(baseazuly==15)
		{
			auxcell = getUpstairsCell(baseazuly+4,baseazulx+2);
			auxcell.SouthBound= true;/////pared anterior
		}
		auxcell = getUpstairsCell(baseazuly+4,baseazulx+3);
		auxcell.SouthBound= true;/////pared anterior
		auxcell = getUpstairsCell(baseazuly+4,baseazulx+4);
		auxcell.SouthBound= true;/////pared anterior			
		

		
		if(baseazulx>0) //// colocar la pared
		{			
			auxcell = getUpstairsCell(baseazuly,baseazulx-1);
			auxcell.EastBound= true;/////pared anterior
			auxcell = getUpstairsCell(baseazuly+1,baseazulx-1);
			auxcell.EastBound= true;/////pared anterior
			auxcell = getUpstairsCell(baseazuly+3,baseazulx-1);
			auxcell.EastBound= true;/////pared anterior
			auxcell = getUpstairsCell(baseazuly+4,baseazulx-1);
			auxcell.EastBound= true;/////pared anterior
		}		
		
		
		/////colocar los bordes de la derecha 
		auxcell = getUpstairsCell(baseazuly,baseazulx+4);
		auxcell.EastBound= true;/////pared anterior
		auxcell = getUpstairsCell(baseazuly+1,baseazulx+4);		
		auxcell.EastBound= true;/////pared anterior
		if(baseazulx==15)
		{
			auxcell = getUpstairsCell(baseazuly+2,baseazulx+4);
			auxcell.EastBound= true;/////pared anterior
		}
		auxcell = getUpstairsCell(baseazuly+3,baseazulx+4);
		auxcell.EastBound= true;/////pared anterior
		auxcell = getUpstairsCell(baseazuly+4,baseazulx+4);
		auxcell.EastBound= true;/////pared anterior			
		
		for(i=0;i<5;i++)
		{
			for(j=0;j<5;j++)
			{
				auxcell = getUpstairsCell(baseazuly+i,baseazulx+j);
				auxcell.mark=Cell.BLUEBASE;
			}
		}
		
		auxcell = getUpstairsCell(baseazuly+2,baseazulx+2);
		auxcell.mark=Cell.BLUEALTAR;
		auxcell = getUpstairsCell(baseazuly+4,baseazulx+4);
		auxcell.mark=Cell.BLUESOURCE;
		
		elevatorCount = Laberinth.MAXELEVATORS;
		//////crear los cuartos de ascensor
		for(i=0;i<elevatorCount;i++)
		{
			k=0;
			
			////////buscar una ubicación valida que no interseque a los demas cuartos 
			///////Maximo 10 intentos
			do
			{
				elevator_col[i]=(int) Math.round(17.0f*Math.random());
				elevator_row[i]=(int) Math.round(17.0f*Math.random());
				validascensor=true;////suponer que esta bien ubicado
				//////evaluar si se ha intersecado con otros cuartos
				for(j=0;j<i;j++)
				{
					if(validascensor)
					{
						if(elevator_col[i]	> elevator_col[j]-4 && elevator_col[i] < elevator_col[j]+4) ///esta en el intervalo de columns
						{
							if(elevator_row[i]	> elevator_row[j]-4 && elevator_row[i] < elevator_row[j]+4)
							{///se ha intersecado								
								validascensor= false;////ubicacion no valida
								break;
							}
						}						
					}
				}
				
				if(validascensor)
				{ ////probar con los cuartos bandera					
					if(elevator_col[i]	> baserojax-4 && elevator_col[i]< baserojax+6)
					{
						if(elevator_row[i]	> baserojay-4 && elevator_row[i]< baserojay+6)
						{
							validascensor= false;
						}					
					}
					if(elevator_col[i]	> baseazulx-4 && elevator_col[i]< baseazulx+6)
					{
						if(elevator_row[i]	> baseazuly-4 && elevator_row[i]< baseazuly+6)
						{
							validascensor= false;
						}					
					}
				}
				k++;
			}while(	validascensor==false && k<10);
			
			if(validascensor)/////se puede ubicar el ascensor
			{
				if(elevator_row[i]>0)
				{
					auxcell = getUpstairsCell(elevator_row[i]-1,elevator_col[i]);
					auxcell.SouthBound= true;
					auxcell = getUpstairsCell(elevator_row[i]-1,elevator_col[i]+2);
					auxcell.SouthBound= true;/////pared anterior
					auxcell = getDownstairsCell(elevator_row[i]-1,elevator_col[i]);
					auxcell.SouthBound= true;/////pared anterior
					auxcell = getDownstairsCell(elevator_row[i]-1,elevator_col[i]+2);
					auxcell.SouthBound= true;/////pared anterior						
				}
				
				if(elevator_col[i]>0)
				{
					auxcell = getUpstairsCell(elevator_row[i],elevator_col[i]-1);
					auxcell.EastBound= true;
					auxcell = getUpstairsCell(elevator_row[i]+2,elevator_col[i]-1);
					auxcell.EastBound= true;/////pared anterior
					auxcell = getDownstairsCell(elevator_row[i],elevator_col[i]-1);
					auxcell.EastBound= true;
					auxcell = getDownstairsCell(elevator_row[i]+2,elevator_col[i]-1);
					auxcell.EastBound= true;/////pared anterior

				}

		
				//////ASIGNAR LAS CELDAS DEL CUARTO DEL ELEVATOR
				auxcell = getUpstairsCell(elevator_row[i],elevator_col[i]);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell = getDownstairsCell(elevator_row[i],elevator_col[i]);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell = getUpstairsCell(elevator_row[i],elevator_col[i]+1);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell = getDownstairsCell(elevator_row[i],elevator_col[i]+1);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell = getUpstairsCell(elevator_row[i],elevator_col[i]+2);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell.EastBound= true;
				auxcell = getDownstairsCell(elevator_row[i],elevator_col[i]+2);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell.EastBound= true;
				
				auxcell = getUpstairsCell(elevator_row[i]+1,elevator_col[i]);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell = getDownstairsCell(elevator_row[i]+1,elevator_col[i]);
				auxcell.mark = Cell.ELEVATORROOM;				
				auxcell = getUpstairsCell(elevator_row[i]+1,elevator_col[i]+1);
				auxcell.mark = Cell.ELEVATOR;
				auxcell = getDownstairsCell(elevator_row[i]+1,elevator_col[i]+1);
				auxcell.mark = Cell.ELEVATOR;
				auxcell = getUpstairsCell(elevator_row[i]+1,elevator_col[i]+2);
				auxcell.mark = Cell.ELEVATORROOM;
				if(elevator_col[i]==17)auxcell.EastBound= true;
				auxcell = getDownstairsCell(elevator_row[i]+1,elevator_col[i]+2);
				auxcell.mark = Cell.ELEVATORROOM;
				if(elevator_col[i]==17)auxcell.EastBound= true;

				
				auxcell = getUpstairsCell(elevator_row[i]+2,elevator_col[i]);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell.SouthBound= true;
				auxcell = getDownstairsCell(elevator_row[i]+2,elevator_col[i]);
				auxcell.mark = Cell.ELEVATORROOM;
				auxcell.SouthBound= true;
				auxcell = getUpstairsCell(elevator_row[i]+2,elevator_col[i]+1);
				auxcell.mark = Cell.ELEVATORROOM;
				if(elevator_row[i]==17)auxcell.SouthBound= true;
				auxcell = getDownstairsCell(elevator_row[i]+2,elevator_col[i]+1);
				auxcell.mark = Cell.ELEVATORROOM;
				if(elevator_row[i]==17)auxcell.SouthBound= true;
				auxcell = getUpstairsCell(elevator_row[i]+2,elevator_col[i]+2);
				auxcell.mark = Cell.ELEVATORROOM;				
				auxcell.SouthBound= true;
				auxcell.EastBound= true;
				if(elevator_col[i]==17)auxcell.EastBound= true;				
				auxcell = getDownstairsCell(elevator_row[i]+2,elevator_col[i]+2);
				auxcell.mark = Cell.ELEVATORROOM;				
				auxcell.SouthBound= true;
				auxcell.EastBound= true;
				if(elevator_col[i]==17)auxcell.EastBound= true;
			}
			else //no see puede ubicar el ascensor
			{
				elevatorCount--;
				i--;		
			}
		}
		
		
		///////crear los otros corredores del laberinto
		for(i=0;i<20;i++)
		{
			for(j=0;j<20;j++)
			{
				auxcell = getUpstairsCell(i,j);
				if(	auxcell.mark==Cell.NORMALROOM)
				{
					if(i==19) auxcell.SouthBound= true;
					if(j==19) auxcell.EastBound= true;

					////contar las paredes existentes en este cuarto
					k=0;
					if(i>0)
					{
						auxcell2 = getUpstairsCell(i-1,j);
						if(auxcell2.SouthBound) k++;
					}
					if(j>0)
					{
						auxcell2 = getUpstairsCell(i,j-1);
						if(auxcell2.EastBound) k++;
					}
					
					if(k<2)
					{
						l= (int)Math.round(2.0f*Math.random());
						if(l==1&& auxcell.SouthBound== false)////colocar pared a la derecha
						{
							if(j<19)
							{
								auxcell2 = getUpstairsCell(i,j+1);
								if(auxcell2.mark==Cell.NORMALROOM) auxcell.EastBound = true;	
							}
						}
						else if(l==2&& auxcell.EastBound== false)////colocar pared al sur
						{
							if(i<19)
							{
								auxcell2 = getUpstairsCell(i+1,j);
								if(auxcell2.mark==Cell.NORMALROOM) auxcell.SouthBound = true;	
							}							
						}							
					}
				
				}	
				
				auxcell = getDownstairsCell(i,j);
				if(	auxcell.mark==Cell.NORMALROOM)
				{
					if(i==19) auxcell.SouthBound= true;
					if(j==19) auxcell.EastBound= true;

					////contar las paredes existentes en este cuarto
					k=0;
					if(i>0)
					{
						auxcell2 = getDownstairsCell(i-1,j);
						if(auxcell2.SouthBound) k++;
					}
					if(j>0)
					{
						auxcell2 = getDownstairsCell(i,j-1);
						if(auxcell2.EastBound) k++;
					}
					
					if(k<2)
					{
						l= (int)Math.round(2.0f*Math.random());////escoger pared aleatoria
						if(l==1&& auxcell.SouthBound== false)////colocar pared a la derecha
						{
							if(j<19)
							{
								auxcell2 = getDownstairsCell(i,j+1);
								if(auxcell2.mark==Cell.NORMALROOM) auxcell.EastBound = true;	
							}
						}
						else if(l==2&& auxcell.EastBound== false)////colocar pared al sur
						{
							if(i<19)
							{
								auxcell2 = getUpstairsCell(i+1,j);
								if(auxcell2.mark==Cell.NORMALROOM) auxcell.SouthBound = true;
							}							
						}							
					}										
				}
			}			
		}	
	}			
	
	///////cellfin debe ser un arreglo de 1
	public boolean crossLaberinth(double radius,Vector3d center,Vector3d endposition, Vector3d normal, Cell endcell[])
	{
		Cell cel1, cel2;
		cel1= getCellByPosition(center);
		Stack pcellstack= new Stack(), pcellmarked= new Stack();
		double dis= 1.0;
		double escalar[]= new double[1];
		double pp;
		int i;
		boolean bound=false;
		Vector3d boxmin= new Vector3d();
		Vector3d boxmax= new Vector3d();
		Vector3d vel= new Vector3d();
		Vector3d vdiff= new Vector3d();
		Vector3d velocity= new Vector3d();
		velocity.set(endposition);
		velocity.sub(center);
		
		cel1.mark2=true;
		pcellstack.push(cel1);	
		pcellmarked.push(cel1);	
		
		while(pcellstack.empty()==false)
		{
			cel1=(Cell) pcellstack.pop();
			for(i=0;i<4;i++)
			{
				normal.set(getCellNormal(i));
				getCellBox(cel1,i,boxmin,boxmax);
				boxmin.y= boxmin.y+CELLHEIGHT;
				vel.set(velocity);
				vel.scale(dis);
				bound = LeonMath.collisionSphereVelPlane(radius,center,vel, normal, boxmin, escalar);
				if(bound== true&&dis>=escalar[0])
				{
					bound=getCellBound(cel1,i);
					if(bound==false)
					{
						cel2=getCellNeighbor(cel1,i);
						if(cel2!=null)
						{
							if(isSolidCorner(cel1,getMinCorner(i)))
							{
								bound= LeonMath.collisionSphereVelSphere(radius, center,vel, 0.2,boxmin ,escalar);	
								if(bound&&dis>=escalar[0])
								{
									dis=escalar[0];	
									vel.set(velocity);
									vel.scale(dis);
									endposition.set(center);
									endposition.add(vel);
									normal.set(endposition);
									normal.sub(boxmin);
									normal.normalize();									
								}
								
							}
							if(isSolidCorner(cel1,getMaxCorner(i)))
							{
								bound= LeonMath.collisionSphereVelSphere(radius, center,vel, 0.2,boxmax ,escalar);	
								if(bound&&dis>=escalar[0])
								{
									dis=escalar[0];
									vel.set(velocity);
									vel.scale(dis);
									endposition.set(center);
									endposition.add(vel);
									normal.set(endposition);
									normal.sub(boxmin);
									normal.normalize();										
								}
							}
							if(bound==false)////expandir
							{
								cel2.mark2=true;
								pcellstack.push(cel2);	
								pcellmarked.push(cel2);					
							}
						}		
					}
					else
					{
						dis=escalar[0];
					}
				}	
				
			}
		}///	/end while	
		vel.set(velocity);
		vel.scale(dis);
		endposition.set(center);
		endposition.add(vel);
		endcell[0]= getCellByPosition(endposition);
		while(pcellmarked.empty()==false)
		{
			cel1= (Cell) pcellmarked.pop();
			cel1.mark2= false;	
		}		
		return bound; 
	} 
	
	
	/*dir:
		0 -> top
		1 -> bottom
		2 -> left
		3 -> right 
	*/
	
	public Cell getCellNeighbor(Cell cel, int dir)
	{
		Cell ret=null;
		switch(dir)
		{
			case 0:
				if(cel.m_row>0)
				{
					ret= getCell(cel.m_row-1,cel.m_column,cel.m_upstairs);
				}
				break;
			case 1:
				if(cel.m_row<19)
				{
					ret= getCell(cel.m_row+1,cel.m_column,cel.m_upstairs);
				}
				break;
			case 2:
				if(cel.m_column>0)
				{
					ret= getCell(cel.m_row,cel.m_column-1,cel.m_upstairs);
				}
				break;
			case 3:
				if(cel.m_column<19)
				{
					ret= getCell(cel.m_row,cel.m_column+1,cel.m_upstairs);
				}
				break;
		}	
		return ret;
	}
	
	public boolean getCellBound(Cell cel, int dir)
	{
		boolean bl=false;
		Cell ret;
		switch(dir)
		{
			case 0:
				if(cel.m_row>0)
				{
					ret= getCell(cel.m_row-1,cel.m_column,cel.m_upstairs);
					bl = ret.SouthBound;
				}
				else bl= true;
				break;
			case 1:
				if(cel.m_row<19)
				{
					bl = cel.SouthBound;
				}
				else bl= true;
				break;
			case 2:
				if(cel.m_column>0)
				{
					ret= getCell(cel.m_row,cel.m_column-1,cel.m_upstairs);
					bl = ret.EastBound;
				}
				else bl= true;
				break;
			case 3:
				if(cel.m_column<19)
				{
					bl= cel.EastBound;
				}
				else bl= true;
				break;
		}	
		return bl;
	}
	
	public int getOppositeWall(int dir)
	{
		int ret=0;
		switch(dir)
		{
			case 0:
				ret=1;
				break;
			case 1:
				ret=0;
				break;
			case 2:
				ret=3;
				break;
			case 3:
				ret=2;
				break;
		}	
		return ret;	
	}
	
	public Vector3d getCellNormal(int dir)
	{
		
		Vector3d normal=null;
		switch(dir)
		{
			case 0:
				normal = getCellTopWallNormal();
				break;
			case 1:
				normal = getCellBottomWallNormal();
				break;
			case 2:
				normal = getCellLeftWallNormal();
				break;
			case 3:
				normal = getCellRightWallNormal();
				break;
		}	
		return normal;
	}
	
	/*obtiene las esquinas del portal y la información de 
	solidez en solid[]= new boolean[2]*/
		
	public void getCellPortalCorners(Cell cel, int dir, Vector3d corner1, Vector3d corner2, boolean solid[])
	{
		getCellBox(cel,dir,corner1,corner2);
		corner1.y=corner1.y+CELLWIDTH/2;
		corner2.y=corner2.y-CELLWIDTH/2;
		solid[0]= isSolidCorner(cel,getMinCorner(dir));
		solid[1]= isSolidCorner(cel,getMaxCorner(dir));					
	}
	/*obtiene el recuadro del portal*/
	public void getCellBox(Cell cel, int dir, Vector3d boxmin, Vector3d boxmax)
	{
				
		switch(dir)
		{
			case 0:
				getCellTopWallBox(cel.m_row, cel.m_column, cel.m_upstairs, boxmin,boxmax );
				break;
			case 1:
				getCellBottomWallBox(cel.m_row, cel.m_column, cel.m_upstairs, boxmin,boxmax );
				break;
			case 2:
				getCellLeftWallBox(cel.m_row, cel.m_column, cel.m_upstairs, boxmin,boxmax );
				break;
			case 3:
				getCellRightWallBox(cel.m_row, cel.m_column, cel.m_upstairs, boxmin,boxmax );
				break;
		}	
		
	}
	/*
	retorna
	corner:
	0 -> left,top
	1 -> left,bottom
	2 -> right,top
	3 -> right,bottom
	*/
	public int getMinCorner(int dir)
	{
		int ret=0;
		switch(dir)
		{
			case 0:
				ret=0;
				break;
			case 1:
				ret=1;
				break;
			case 2:
				ret=0;
				break;
			case 3:
				ret=2;
				break;
		}	
		return ret;
	}
	/*
	retorna
	corner:
	0 -> left,top
	1 -> left,bottom
	2 -> right,top
	3 -> right,bottom
	*/
	public int getMaxCorner(int dir)
	{
		int ret=0;
		switch(dir)
		{
			case 0:
				ret=2;
				break;
			case 1:
				ret=3;
				break;
			case 2:
				ret=1;
				break;
			case 3:
				ret=3;
				break;
		}	
		return ret;

	}
	public Cell getCell(int row, int column, boolean upstairs)
	{
		int num;
		if(upstairs)
		{
			
			num = row*20 + column + 400;
			return cells[num];
		}
		num = row*20 + column;
		return cells[num];		
	}
	
	public Cell getUpstairsCell(int row, int column)
	{
		int num;
		num = row*20 + column + 400;
		return cells[num];
	}
	public Cell getDownstairsCell(int row, int column)
	{
		int num;
		num = row*20 + column;
		return cells[num];
	}	
	
	
	public Vector3d getCellPosition(int row, int column, boolean upstairs)
	{
		Vector3d vec;
		
		vec = new  	Vector3d();
		vec.z=CELLWIDTH/2+CELLWIDTH*row;
		vec.x=CELLWIDTH/2+CELLWIDTH*column;
		if(upstairs)	vec.y=CELLHEIGHT;
		else vec.y=0.0;		
		return vec;		
	}
	
	public Cell getCellByPosition(Vector3d pos)
	{
		int i,j;
		i =(int) Math.floor(pos.z/CELLWIDTH);			
		if(i<0) i=0;
		else if(i>19) i=19;
		j =(int) Math.floor(pos.x/CELLWIDTH);
		if(j<0) j=0;
		else if(j>19) j=19;
		if(pos.y<CELLHEIGHT)
		{
			return getDownstairsCell(i,j);
			
		}
		
		return getUpstairsCell(i,j);	
	}
	
	//////las normales interiores de la cell	
	public Vector3d getCellTopWallNormal()
	{
		Vector3d vecnormal= new Vector3d();
		vecnormal.x =0.0;
		vecnormal.y =0.0;
		vecnormal.z =1.0;
		return vecnormal;					
	}

	public Vector3d getCellBottomWallNormal()
	{
		Vector3d vecnormal= new Vector3d();
		vecnormal.x =0.0;		
		vecnormal.y =0.0;
		vecnormal.z =-1.0;					
		return vecnormal;					
	}

	
	public Vector3d getCellLeftWallNormal()
	{
		Vector3d vecnormal= new Vector3d();
		vecnormal.x =1.0;
		vecnormal.y =0.0;
		vecnormal.z =0.0;
		return vecnormal;					
	}

	public Vector3d getCellRightWallNormal()
	{
		Vector3d vecnormal= new Vector3d();
		vecnormal.x =-1.0;		
		vecnormal.y =0.0;
		vecnormal.z =0.0;					
		return vecnormal;					
	}

	//////verifica si la esquina es solida
	/*
	corner:
	0 -> left,top
	1 -> left,bottom
	2 -> right,top
	3 -> right,bottom
	*/
	public boolean isSolidCorner(Cell cel, int corner)
	{
		Cell cel2;
		boolean ret=false;
		switch(corner)
		{
			case 0:///////top,left
				if(cel.m_row>0)
				{
					cel2= getCell(cel.m_row-1, cel.m_column, cel.m_upstairs);
					if(cel2.SouthBound)
					{
						ret= true;
					}
					else if(cel.m_column>0)
					{
						cel2= getCell(cel.m_row, cel.m_column-1, cel.m_upstairs);
						if(cel2.EastBound)
						{
							ret = true;	
						}
						else
						{
							cel2= getCell(cel.m_row-1, cel.m_column-1, cel.m_upstairs);
							if(cel2.EastBound||cel2.SouthBound) ret = true;
						}					
					}
					else
					{
						ret= true;
					}
					
				}
				else
				{
					ret= false;	
				}
				break;	
			case 1://///bottom, left
				if(cel.SouthBound||cel.m_row==19)
				{
					ret= true;	
				}
				else
				{	
					if(cel.m_column>0)
					{
						cel2= getCell(cel.m_row, cel.m_column-1, cel.m_upstairs);
						if(cel2.EastBound||cel2.SouthBound)
						{
							ret= true;
						}
						else
						{
							cel2= getCell(cel.m_row+1, cel.m_column-1, cel.m_upstairs);
							if(cel2.EastBound)
							{
								ret = true;	
							}												
						}
					}
					else
					{
						ret= true;
					}
					
				}
				
				break;	
			case 2://///top, right
				if(cel.EastBound||cel.m_column==19)
				{
					ret= true;	
				}
				else
				{	
					if(cel.m_row>0)
					{
						cel2= getCell(cel.m_row-1, cel.m_column, cel.m_upstairs);
						if(cel2.EastBound||cel2.SouthBound)
						{
							ret= true;
						}
						else
						{
							cel2= getCell(cel.m_row-1, cel.m_column+1, cel.m_upstairs);
							if(cel2.SouthBound)
							{
								ret = true;	
							}												
						}
					}
					else
					{
						ret= true;
					}
					
				}
				break;	
			default:
				if(cel.EastBound||cel.m_column==19)
				{
					ret= true;	
				}
				else
				{	
					if(cel.SouthBound||cel.m_row==19)
					{
						ret= true;	
					}
					else
					{			
						cel2= getCell(cel.m_row, cel.m_column+1, cel.m_upstairs);
						if(cel2.SouthBound)
						{
							ret= true;
						}
						else
						{
							cel2= getCell(cel.m_row+1, cel.m_column, cel.m_upstairs);
							if(cel2.EastBound)
							{
								ret = true;	
							}												
						}
					}		
					
				}

		}///////end swithcv
		return ret;
	}
	//////la dimension de los portales
	public void getCellTopWallBox(int row, int column, boolean upstairs,Vector3d boxmin, Vector3d boxmax)
	{
		boxmin.x=CELLWIDTH*column;
		boxmin.z=CELLWIDTH*row;////-0.125;
		
		
		boxmax.x=CELLWIDTH*(column+1);
		boxmax.z=CELLWIDTH*row;///+0.125;
		
		if(upstairs== true)
		{
			boxmin.y=4.0;
			boxmax.y=8.0;
		}
		else
		{
			boxmin.y=0.0;
			boxmax.y=4.0;
		}
		
	}
	
	public void getCellBottomWallBox(int row, int column, boolean upstairs,Vector3d boxmin, Vector3d boxmax)
	{
		boxmin.x=CELLWIDTH*column;
		boxmin.z=CELLWIDTH*(row+1);///-0.125;
		
		
		boxmax.x=CELLWIDTH*(column+1);
		boxmax.z=CELLWIDTH*(row+1);/////+0.125;
		
		if(upstairs== true)
		{
			boxmin.y=4.0;
			boxmax.y=8.0;
		}
		else
		{
			boxmin.y=0.0;
			boxmax.y=4.0;
		}
		
	}
	
	public void getCellLeftWallBox(int row, int column, boolean upstairs,Vector3d boxmin, Vector3d boxmax)
	{
		boxmin.x=CELLWIDTH*column;////-0.125;
		boxmin.z=CELLWIDTH*row;
		
		boxmax.x=CELLWIDTH*column;///+0.125;
		boxmax.z=CELLWIDTH*(row+1);		
		
		
		if(upstairs== true)
		{
			boxmin.y=4.0;
			boxmax.y=8.0;
		}
		else
		{
			boxmin.y=0.0;
			boxmax.y=4.0;
		}
		
	}

	public void getCellRightWallBox(int row, int column, boolean upstairs,Vector3d boxmin, Vector3d boxmax)
	{
		boxmin.x=CELLWIDTH*(column+1);///-0.125;
		boxmin.z=CELLWIDTH*row;
		
		boxmax.x=CELLWIDTH*(column+1);///+0.125;
		boxmax.z=CELLWIDTH*(row+1);		
		
		
		if(upstairs== true)
		{
			boxmin.y=4.0;
			boxmax.y=8.0;
		}
		else
		{
			boxmin.y=0.0;
			boxmax.y=4.0;
		}		
	}

	
	public void getCellTopWallTrans(int row, int column, boolean upstairs, TransformGroup frame)
	{
		Vector3d vec;
		Transform3D t;
		Matrix3d mat1= new Matrix3d();//, mat2= new Matrix4d(), mat3= new Matrix4d();
		vec = new	Vector3d();
		vec.x=CELLWIDTH/2+CELLWIDTH*column;
		vec.z=CELLWIDTH*row+0.1;		
		if(upstairs)	vec.y=CELLHEIGHT;
		else vec.y=0.0;
		mat1.rotY(Math.PI/2);
		frame.setTransform(new Transform3D(mat1,vec,1.0));
		
	}
	
	public void getCellBottomWallTrans(int row, int column, boolean upstairs, TransformGroup frame)
	{
		Vector3d vec;
		Transform3D t;
		Matrix3d mat1= new Matrix3d();//, mat2= new Matrix3d();
		vec = new	Vector3d();
		vec.x=CELLWIDTH/2+CELLWIDTH*column;
		vec.z=CELLWIDTH*(row+1)-0.1;		
		if(upstairs)	vec.y=CELLHEIGHT;
		else vec.y=0.0;
		mat1.rotY(Math.PI/2);
		frame.setTransform(new Transform3D(mat1,vec,1.0));
	}
	
	public void getCellLeftWallTrans(int row, int column, boolean upstairs, TransformGroup frame)
	{
		Vector3d vec;		
		vec = new Vector3d();
		vec.x=CELLWIDTH*column;
		vec.z=CELLWIDTH/2+CELLWIDTH*row;
		if(upstairs)	vec.y=CELLHEIGHT;
		else vec.y=0.0;
		Transform3D t = new Transform3D();
		t.setTranslation(vec);		
		frame.setTransform(t);		
	}
	
	public void getCellRightWallTrans(int row, int column, boolean upstairs, TransformGroup frame)
	{
		Vector3d vec;
		
		vec = new Vector3d();
		vec.x=CELLWIDTH*(column+1);
		vec.z=CELLWIDTH/2+CELLWIDTH*row;		
		if(upstairs)	vec.y=CELLHEIGHT;
		else vec.y=0.0;
		Transform3D t = new Transform3D();
		t.setTranslation(vec);		
		frame.setTransform(t);		
	}	
	
		
	
}
