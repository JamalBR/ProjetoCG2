/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.Random;

/**
 *
 * @author Giovanni Garcia Ribeiro de Souza e Marício Luís de Lorenzi
 */
public class Main
        extends SimpleApplication
        implements ActionListener, PhysicsCollisionListener {

    public static void main(String[] args) {
        Main app = new Main();
        app.showSettings = false;
        app.start();
    }
    private BulletAppState bulletAppState;
    private PlayerCamera player;
    private boolean up = false, down = false, left = false, right = false;
    private Material boxMatColosion;
    AudioNode somTiro;
    
    
    @Override
    public void simpleInitApp() {

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        createLigth();
        createCity();
        
        
        boxMatColosion = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
        boxMatColosion.setBoolean("UseMaterialColors", true);
        boxMatColosion.setColor("Ambient", ColorRGBA.Red);
        boxMatColosion.setColor("Diffuse", ColorRGBA.Red); 
        
        ajustaCamera();
        createPlayer();
        //createCubo();
        initAudio();
        initKeys();

       // bulletAppState.setDebugEnabled(false);
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }

    @Override
    public void simpleUpdate(float tpf) {

        player.upDateKeys(tpf, up, down, left, right);

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private void createPlayer() {

        player = new PlayerCamera("player", assetManager, bulletAppState, cam);
        rootNode.attachChild(player);
        flyCam.setEnabled(false);

    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        switch (binding) {
            case "CharLeft":
                if (value) {
                    left = true;
                } else {
                    left = false;
                }
                break;
            case "CharRight":
                if (value) {
                    right = true;
                } else {
                    right = false;
                }
                break;
        }
        if (binding.equals("Tiro")) {
            createTiro();
            somTiro.playInstance();
        }
            
    }

    private void createLigth() {

        DirectionalLight l1 = new DirectionalLight();
        l1.setDirection(new Vector3f(1, -0.7f, 0));
        rootNode.addLight(l1);

        DirectionalLight l2 = new DirectionalLight();
        l2.setDirection(new Vector3f(-1, 0, 0));
        rootNode.addLight(l2);

        DirectionalLight l3 = new DirectionalLight();
        l3.setDirection(new Vector3f(0, 0, -1.0f));
        rootNode.addLight(l3);

        DirectionalLight l4 = new DirectionalLight();
        l4.setDirection(new Vector3f(0, 0, 1.0f));
        rootNode.addLight(l4);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);
    }
    
    private void ajustaCamera(){
        CameraNode camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(-30, -4.5f,0));
        camNode.lookAt(rootNode.getLocalTranslation(), Vector3f.UNIT_Y);
        
        
        rootNode.attachChild(camNode);
    }

    private void createCubo(float x, float y, float z) {
        /* A colored lit cube. Needs light source! */
        Box boxMesh = new Box(0.1f, 0.1f, 0.1f);
        Geometry boxGeo = new Geometry("Box", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        boxMat.setBoolean("UseMaterialColors", true);
        boxMat.setColor("Ambient", ColorRGBA.Green);
        boxMat.setColor("Diffuse", ColorRGBA.Green);
        boxGeo.setMaterial(boxMat);
        boxGeo.setLocalTranslation(x,y,z);
        rootNode.attachChild(boxGeo);
        


        RigidBodyControl boxPhysicsNode = new RigidBodyControl(0);
        boxGeo.addControl(boxPhysicsNode);
        bulletAppState.getPhysicsSpace().add(boxPhysicsNode);

    }
    
    private void createTiro() {
        Sphere sphere = new Sphere(10, 10, 0.03f);
        Geometry tiro = new Geometry("Tiro", sphere);
        Material tiro_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tiro_mat.setColor("Color", ColorRGBA.Red);
        tiro.setMaterial(tiro_mat);
        tiro.setLocalTranslation((player.getLocalTranslation().x -25),(player.getLocalTranslation().y + 0.3f),(player.getLocalTranslation().z + 0.4f ));
        rootNode.attachChild(tiro);
        
        RigidBodyControl tiroPhysicsNode = new RigidBodyControl(1);
        tiro.addControl(tiroPhysicsNode);
        bulletAppState.getPhysicsSpace().add(tiroPhysicsNode);
        tiroPhysicsNode.setLinearVelocity(new Vector3f(0f,20,0f));
        
    }

    private void createCity() {
        assetManager.registerLocator("town.zip", ZipLocator.class);
        Spatial scene = assetManager.loadModel("main.scene");
        scene.setLocalTranslation(0, -5.2f, 0);
        rootNode.attachChild(scene);

        RigidBodyControl cityPhysicsNode = new RigidBodyControl(CollisionShapeFactory.createMeshShape(scene), 0);
        scene.addControl(cityPhysicsNode);
        bulletAppState.getPhysicsSpace().add(cityPhysicsNode);
    }

    private void initKeys() {
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Tiro", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));
       

        inputManager.addListener(this, "CharLeft", "CharRight");
        inputManager.addListener(this, "Tiro", "CharBackward");

    }
    
    private void initAudio(){
        somTiro = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
    }
/* A colored lit cube. Needs light source! */ 
    
    
    @Override
    public void collision(PhysicsCollisionEvent event) {

        if(event.getNodeA().getName().equals("Tiro") || event.getNodeA().getName().equals("Tiro")){
        
            if(event.getNodeA().getName().equals("Box")){
                  Spatial s = event.getNodeA();
                  s.setMaterial(boxMatColosion);
            }
            else
            if(event.getNodeB().getName().equals("Box")){
                  Spatial s = event.getNodeB();
                  s.setMaterial(boxMatColosion);
            }
            
        }
        
    }
}
