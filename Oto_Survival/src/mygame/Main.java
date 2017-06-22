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
import com.jme3.input.controls.AnalogListener;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import com.jme3.system.AppSettings;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Giovanni Garcia Ribeiro de Souza e Marício Luís de Lorenzi
 */
public class Main
        extends SimpleApplication
        implements ActionListener, PhysicsCollisionListener {

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings cfg = new AppSettings(true);
        cfg.setFrameRate(60); // set to less than or equal screen refresh rate
        cfg.setVSync(true);   // prevents page tearing
        cfg.setFrequency(60); // set to screen refresh rate
        cfg.setResolution(1024, 768);
        cfg.setFullscreen(true);
        cfg.setSamples(2);    // anti-aliasing
        cfg.setTitle("My jMonkeyEngine 3 Game"); // branding: window name
        try {
            // Branding: window icon
            cfg.setIcons(new BufferedImage[]{ImageIO.read(new File("assets/Interface/icon.gif"))});
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Icon missing.", ex);
        }
        // branding: load splashscreen from assets
        cfg.setSettingsDialogImage("Interface/MySplashscreen.png");
        app.setShowSettings(false); // or don't display splashscreen
        app.setSettings(cfg);
        app.start();
    }
    private BulletAppState bulletAppState;
    private PlayerCamera player;
    private boolean up = false, down = false, left = false, right = false, reinicia = false, pausar = false;
    private Material boxMatColosion;
    AudioNode somTiro;    
    public ArrayList<Integer> recordes = new ArrayList<Integer>();
    private boolean isRunning = true;
    private final ActionListener pauseActionListener;
    private final AnalogListener pauseAnalogListener;
    
    
    public Main()
    {
            this.pauseAnalogListener = new AnalogListener() {
            @Override
            public void onAnalog(String name, float value, float tpf) {
                if (isRunning) {

                } else {
                    if(pausar != true)
                        menu();
                }
            }
        };
        this.pauseActionListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean keyPressed,
                    float tpf) {
                if (name.equals("Pause") && !keyPressed) {
                    isRunning = !isRunning;
                    if(pausar != true)
                        menu();
                }
            }
        };
    }
    @Override
    public void simpleInitApp() {

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        createLigth();
        createCity();
        
        
        boxMatColosion = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
        boxMatColosion.setBoolean("UseMaterialColors", true);
        boxMatColosion.setColor("Ambient", ColorRGBA.Blue);
        boxMatColosion.setColor("Diffuse", ColorRGBA.Blue); 
        
        ajustaCamera();
        createPlayer();
        createCubo(-25, 3,0);
        initAudio();
        initKeys();
        
       // bulletAppState.setDebugEnabled(false);


        bulletAppState.setDebugEnabled(false);
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }

    @Override
    public void simpleUpdate(float tpf) {
        


        if(!pausar)
        {
            missingShoot();
        }
        
        if (reinicia) {
            //setar posicao inicial do personagem
            //CalculaRecordes(placar);
            //placar = 0;
            reinicia = false;
        }
        player.upDateKeys(tpf, up, down, left, right);
       
        //precisa alterar (frescurinha)
        if (!isRunning) {
            /*guiNode.detachAllChildren();
            guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
            BitmapText helloText = new BitmapText(guiFont, false);
            helloText.setSize(guiFont.getCharSet().getRenderedSize());
            helloText.setColor(ColorRGBA.Black);
            helloText.setText("PAUSE");
            helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
            guiNode.attachChild(helloText);*/
            setPausar(true);
        } else {
            //guiNode.detachAllChildren();
            setPausar(false);
            /*guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
            BitmapText text = new BitmapText(guiFont, false);
            text.setSize(guiFont.getCharSet().getRenderedSize());
            text.setColor(ColorRGBA.Black);
            text.setText("Placar: "/* + placar);
            text.setLocalTranslation(30, text.getLineHeight() + 445, 0);
            guiNode.attachChild(text);*/
        }
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
        if (binding.equals("Disparo")) {
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
        
        RigidBodyControl boxPhysicsNode = new RigidBodyControl(0.5f);
        boxGeo.addControl(boxPhysicsNode);
        bulletAppState.getPhysicsSpace().add(boxPhysicsNode);
        Random gR = new Random();
        boxPhysicsNode.setGravity(Vector3f.UNIT_Y.add(0, ((gR.nextFloat()*(-2f))-1), 0));
    }
    
    private void createTiro() {
        Sphere sphere = new Sphere(10, 10, 0.03f);
        Geometry tiro = new Geometry("Tiro", sphere);
        Material tiro_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tiro_mat.setColor("Color", ColorRGBA.Red);
        tiro.setMaterial(tiro_mat);
        tiro.setLocalTranslation((player.getLocalTranslation().x -25),(player.getLocalTranslation().y + 0.3f),(player.getLocalTranslation().z)/0.6f);
        rootNode.attachChild(tiro);
        
        RigidBodyControl tiroPhysicsNode = new RigidBodyControl(1);
        tiro.addControl(tiroPhysicsNode);
        bulletAppState.getPhysicsSpace().add(tiroPhysicsNode);
        tiroPhysicsNode.setLinearVelocity(new Vector3f(0f,15,0f));
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
    
    void missingShoot(){
         Spatial tiro = rootNode.getChild("Tiro");
         if (tiro != null) {
            if (tiro.getLocalTranslation().y > 2) {
                int index = rootNode.getChildIndex(tiro);
                rootNode.detachChildAt(index);
                bulletAppState.getPhysicsSpace().removeAll(tiro);
            }
        }
    }
    
    void removeHitedBox(Spatial hited){
        int index = rootNode.getChildIndex(hited);
        rootNode.detachChildAt(index);
        bulletAppState.getPhysicsSpace().removeAll(hited);
    }

    private void initKeys() {
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Disparo", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));

        inputManager.addListener(pauseActionListener, new String[]{"Pause"});
        inputManager.addListener(this, "CharLeft", "CharRight");
        inputManager.addListener(this, "Disparo", "CharBackward");

    }
    
    private void initAudio(){
        somTiro = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
    }
/* A colored lit cube. Needs light source! */ 
    
    
    @Override
    public void collision(PhysicsCollisionEvent event) {

        if(event.getNodeA().getName().equals("Tiro") || event.getNodeB().getName().equals("Tiro")){
        
            if(event.getNodeA().getName().equals("Box")){
                  Spatial s = event.getNodeA();
                  removeHitedBox(s);
                  s.setMaterial(boxMatColosion);
            }
            else
            if(event.getNodeB().getName().equals("Box")){
                  Spatial s = event.getNodeB();
                  removeHitedBox(s);
                  s.setMaterial(boxMatColosion);
            }
            
        }
        
    }
    
    public void menu()
    {
        List<String> optionList = new ArrayList<String>();        
        optionList.add("1");
        optionList.add("2");
        optionList.add("3");
        optionList.add("4");
        optionList.add("5");
        optionList.add("6");
        optionList.add("7");
        optionList.add("8");
        optionList.add("9");
        optionList.add("10");
        
        Object[] options = optionList.toArray();
        int value;
        value = JOptionPane.showOptionDialog(
                null,
                "Selecione um dos itens:\n "
                + "1 - Retomar o Jogo\n"
                + "2 - Novo Jogo\n"
                + "3 - Dificuldade\n"        
                + "4 - Áudio\n"
                + "5 - Ranking\n"
                + "6 - Ajuda\n"
                + "7 - Github\n"
                + "8 - Referências e Fontes\n"
                + "9 - Sobre\n"
                + "10 - Sair\n",   
                "Opção:",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                optionList.get(0));
        
        if (value == 2) {
            System.out.println(1);
            pausar = false;
            setPausar(false);
            reinicia = true;
        }
        if(value == 5){
            CalculaRecordes(0);
            Object[] rc = recordes.toArray();
            JOptionPane.showOptionDialog(
                    null,
                    "Ranking:\n ",
                    "",
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    rc, recordes.get(0));
            
        }
        if(value == 6){
            JOptionPane.showMessageDialog(null,
                  "Teclas de Comando:"
                + "W - Atirar\n"
                + "A - Andar para a esquerda\n"
                + "D - Andar para a direita\n"                
                + "P - Pausar\n"
                + "Objetivo:"
                + "Não deixar nenhum bloco cair no chão, senão o jogo acaba."
                + "Ganha quem sobreviver mais tempo.");   
            menu();
        }
        if(value == 9){
            JOptionPane.showMessageDialog(null,
                   "Otto survival é um jogo desenvolvido para a disciplina de Computação Gráfica II,\n"
                   + "do curso Engenharia da Computação, ministrada pelo professor doutor Glauco Todesco\n"
                   + "na Faculdade de Engenharia de Sorocaba(FACENS).\n"
                   + "Autores: \n"
                   + "Giovanni Garcia R. de Souza - 140872\n"
                   + "Maurício Luís de Lorenzi - 141269");
            menu();
            
        }
        if(value == 7){
            JOptionPane.showMessageDialog(null,
                  "https://github.com/JamalBR/ProjetoCG2");
            menu();
            
        }
        if(value == 8){
            JOptionPane.showMessageDialog(null,
                  "Referências:\n"
                + "Todas as imagens e áudios utilizados no projeto são do JMonkey e utilizados para teste."
                + "Todos com os devidos direitos autorais permitidos.");
            menu();
        }
        if(value == 4){
            List<String> som = new ArrayList<String>();
            som.add("Sim");
            som.add("Não");
            Object[] somOp = som.toArray();
            int op;
            op = JOptionPane.showOptionDialog(
                    null,
                    "Deseja som?\n ",
                    "Opção:",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    somOp,
                    som.get(0));
            if(op == 0){
                //somAmbiente.play();
            }
            if(op == 1){
                //somAmbiente.stop();
            }
        }
        if(value == 1){
            if(pausar == true){
                pausar = false;
                setPausar(false);
            }
        }
        
        if(value == 10){
            System.exit(0);
        }
        
        if(value == 3){
            List<String> dif = new ArrayList<String>();
            dif.add("Easy");
            dif.add("Medium");
            dif.add("Hard");
            Object[] Ops = dif.toArray();
            int opDif;
            opDif = JOptionPane.showOptionDialog(
                    null,
                    "Qual dificuldade?\n ",
                    "Opções:",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    Ops,
                    dif.get(0));
                  
        }
    }
    //metodo que pausa o jogo
    public void setPausar(boolean y) {
        if (y) {
            pausar = true;
            inputManager.removeListener(this);
            inputManager.removeListener(this);
        }
        if (!y) {
            pausar = false;
            inputManager.addListener(this);
            inputManager.addListener(this, "CharLeft", "CharRight");
            inputManager.addListener(this, "Disparo", "CharBackward");
        }
    }

    private void CalculaRecordes(int i) {
        
    }
}
