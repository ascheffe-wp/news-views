package com.schef.rss.android;


import android.content.Context;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;

import android.content.Intent;
import android.util.Log;

import com.schef.rss.android.db.ArsEntity;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

/**
 * Created by scheffela on 7/6/14.
 */
public class Ars3d implements ApplicationListener {

    AndroidLauncher parent;

    public Environment environment;
    public PerspectiveCamera cam;
    public CameraInputController camController;
    public ModelBatch modelBatch;
    public SpriteBatch spriteBatch;
//    public SpriteBatch spriteBatch2;
//    public DecalBatch decalBatch;
    public Boolean flyMode = true;

    public Boolean imageUpdates = false;


//    public Decal sprite;
//    public Decal sprite1;
//    public Decal sprite2;

    public GroupStrategy strategy;

    private BlendingAttribute blendingAttribute;
    private BlendingAttribute blendingAttribute2;
    private BlendingAttribute blendingAttribute3;

    public Array<ArticleInstance> cylinderSides = new Array<ArticleInstance>();

    private Texture img4;
    private Texture img5, img6, image;
    private TextureRegion icons;

    public Map<String, Texture> textureMap = new HashMap<String, Texture>();
    public Map<String, Texture> textureMap2 = new HashMap<String, Texture>();

    private Gson gson;

    private Context context;

    private CountDownLatch startSignal = new CountDownLatch(1);

    public Map<Float, List<ArticleInstance>> rows = new HashMap<Float, List<ArticleInstance>>();
    public Map<Float, List<ArticleInstance>> cols = new HashMap<Float, List<ArticleInstance>>();

    private BitmapFont font;
    private ShaderProgram fontShader;

    private ModelInstance stop;
    private ModelInstance sbot;
//    private ModelInstance stxt;

    private ModelInstance arrow = null;

    private Sprite sprite;

//    private ModelInstance background;
//    private ModelInstance icon1;
//    private ModelInstance icon2;
//
//    private float dist = 0f;
//    private Double sqrtw;
//    private Double sqrth;
//
//    private Double tanW;
//    private Double tanH;

    AssetManager manager;

    Float startPosition;
    Float startRotation;

    public Ars3d(Context context, float position, float rotation) {
        this.context = context;
        this.startPosition = position;
        this.startRotation = rotation;
    }


    public Ars3d(Context context, AndroidLauncher parent) {
        this.context = context;
        this.parent = parent;
    }

    @Override
    public void create() {

//        Gdx.graphics.setContinuousRendering(false);

        NewApplication.getInstance().setArs3d(this);
        manager = new AssetManager();

        gson = new GsonBuilder().create();

//        img3 = new Texture(Gdx.files.internal("test403.png"));
//        img4 = new Texture(Gdx.files.internal("backg.jpg"));
//        img5 = new Texture(Gdx.files.internal("123750890.jpg"));
//        img6 = new Texture(Gdx.files.internal("brankic1979-icon-set.jpg"));
////        img6 = new Texture(Gdx.files.internal("tmpy.jpg"));
//        image = new Texture(Gdx.files.internal("earthmoon.jpg"));

        manager.load(new AssetDescriptor(Gdx.files.internal("backg.jpg"), Texture.class));
        manager.load(new AssetDescriptor(Gdx.files.internal("123750890.jpg"), Texture.class));
        manager.load(new AssetDescriptor(Gdx.files.internal("brankic1979-icon-set.jpg"), Texture.class));
        manager.load(new AssetDescriptor(Gdx.files.internal("earthmoon.jpg"), Texture.class));
        manager.update();
        manager.finishLoading();

        img4 = manager.get("backg.jpg",Texture.class);
        img5 = manager.get("123750890.jpg",Texture.class);
        img6 = manager.get("brankic1979-icon-set.jpg",Texture.class);
        image = manager.get("earthmoon.jpg",Texture.class);
//        icons = new TextureRegion(img6);

//        font = new BitmapFont();
        blendingAttribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.9f);
        blendingAttribute2 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.2f);
        blendingAttribute3 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.2f);

//        environment = new Environment();
//        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1f));
        modelBatch = new ModelBatch(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 4)));

        cam = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0.0f, 0.0f, -5.6f);
        if(startPosition != null) {
            cam.position.set(0.0f, 0.0f, startPosition);
        }
        cam.lookAt(0.0f, 0.0f, 0.0f);
        cam.near = 0.01f;
        cam.far = 100f;

        if(startRotation != null) {
            cam.up.rotate(startRotation,0f,0f,1.01f);
            cam.update();
        }

        cam.update(true);

        ModelBuilder mb = new ModelBuilder();
        Model model = null;
        ArticleInstance instance2 = null;

//        // batches
//        strategy = new CameraGroupStrategy(cam);
//        decalBatch = new DecalBatch(strategy);

//        sprite = Decal.newDecal(1,1,new TextureRegion(img6,950,875,35,40));
//        sprite1 = Decal.newDecal(1,1,new TextureRegion(img6,950,875,35,40));
//        sprite2 = Decal.newDecal(25,15,new TextureRegion(image),true);

//        dist = sprite2.getPosition().dst2(cam.position);
//
//
//        sqrtw = Math.sqrt((dist*dist) + (12.5*12.5));
//        tanW = Math.tan(dist/sqrtw);
//        sqrth = Math.sqrt((dist*dist) + (7.5*7.5));
//        tanH = Math.tan(dist/sqrtw);
//
//        Vector3 vec1 = cam.frustum.planePoints[4];
//        Vector3 vec2 = cam.frustum.planePoints[5];
//        Vector3 vec3 = cam.frustum.planePoints[6];
//        Vector3 vec4 = cam.frustum.planePoints[7];
//
//        model = mb.createRect(vec1.x,vec1.y,vec1.z,vec2.x,vec2.y,vec2.z,vec3.x,vec3.y,vec3.z,vec4.x,vec4.y,vec4.z,
//                0f,0f,0f,
//                new Material(ColorAttribute.createDiffuse(Color.WHITE), TextureAttribute.createDiffuse(image), blendingAttribute),
//                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
//
//        background = new ModelInstance(model);


//        String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
//                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
//                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
//                + "uniform mat4 u_projTrans;\n" //
//                + "varying vec4 v_color;\n" //
//                + "uniform float u_opacity;\n"
//                + "varying float v_opacity;\n"
//                + "varying vec2 v_texCoords;\n" //
//                + "\n" //
//                + "void main()\n" //
//                + "{\n" //
//                + "   v_opacity = u_opacity;\n"
//                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
//                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
//                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
//                + "}\n";
//        String fragmentShader = "#ifdef GL_ES\n" //
//                + "precision mediump float;\n" //
//                + "#endif\n" //
//                + "varying vec4 v_color;\n" //
//                + "varying vec2 v_texCoords;\n" //
//                + "uniform sampler2D u_texture;\n" //
//                + "varying float v_opacity;\n"
//                + "void main()\n"//
//                + "{\n" //
//                + "  vec4 diffuse = texture2D(u_texture, v_texCoords) * v_color;"
//                + "  gl_FragColor.rgb = diffuse.rgb;"
//                + "  gl_FragColor.a = diffuse.a * 0.9;\n"
//                //+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
//                + "}";
//
//        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);

        spriteBatch = new SpriteBatch(1);
//        spriteBatch2 = new SpriteBatch(1);

//        icons.setRegion(950f/img6.getWidth(),875f/img6.getHeight(),985f/img6.getWidth(),915f/img6.getHeight());
//        sprite = new Sprite(icons);
//        sprite.setSize(.02f,.02f);
//        spriteBatch.enableBlending();
//        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

//        Vector3 vec5 = cam.frustum.planePoints[0];
//        Vector3 vec6 = cam.frustum.planePoints[1];
//        Vector3 vec7 = cam.frustum.planePoints[2];
//        Vector3 vec8 = cam.frustum.planePoints[3];
//
//        mb.begin();
//        MeshPartBuilder mpb = mb.part("rect", primitiveType, attributes, material);
//        mpb.setUVRange();
//        mpb.rect(vec5.x, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, normalX,
//                normalY, normalZ);
//
//        model = mb.end();

//        image.setFilter(Texture.TextureFilter.Linear,
//                Texture.TextureFilter.Linear);
//        image.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
//
//        float w = Gdx.graphics.getWidth();
//        float h = Gdx.graphics.getHeight();
//        sprite = Decal.newDecal(w, h, new TextureRegion(image), true);

        fontShader = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font.frag"));
        if (!fontShader.isCompiled()) {
            Gdx.app.error("fontShader", "compilation failed:\n" + fontShader.getLog());
        }

        Texture texture = new Texture(Gdx.files.internal("txt3.png"), true); // true enables mipmaps
        texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear); // linear filtering in nearest mipmap image

        font = new BitmapFont(Gdx.files.internal("txt3.fnt"), new TextureRegion(texture), false);
        pm = new Pixmap(Gdx.files.internal("txt3.png"));
//        BitmapFont.TextBounds tb = font.getBounds("The six tech policy problems Congress failed to fix this year");

//        Pixmap pm2 = new Pixmap((int)tb.width,143, pm.getFormat());
//
//        int currentWidth = 0;
//        for(char ch : "The six tech policy problems Congress failed to fix this year".toCharArray()) {
//            BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
//
////            int endWidth = currentWidth + glyph.width - 1;
//            if(ch != 32) {
//                pm2.drawPixmap(pm, glyph.srcX, glyph.srcY, glyph.width, glyph.height, currentWidth, Math.abs(glyph.height/2 + glyph.yoffset/2), glyph.width/2, glyph.height/2);
//                currentWidth += (glyph.width/2);
//            } else {
//                currentWidth += 26/2;
//            }
//        }
//
//        txt2 = new Texture(pm2);
//        TextureRegion textureRegion = new TextureRegion(txt2, txt2.getWidth()/4, txt2.getHeight());
//        int count = 0;
        FileHandle fh = Gdx.files.internal("objs2.json");

//        ModelBuilder mb = new ModelBuilder();

//        TextureAttribute texture2 = TextureAttribute.createDiffuse(txt2);
//        texture2.textureDescription.uWrap = Texture.TextureWrap.ClampToEdge;
//        texture2.textureDescription.vWrap = Texture.TextureWrap.ClampToEdge;
//        texture2.textureDescription.texture.setWrap(Texture.TextureWrap.ClampToEdge,Texture.TextureWrap.ClampToEdge);


        ImagePane[] ips = gson.fromJson(fh.reader(), ImagePane[].class);
        for (ImagePane ip : ips) {
            if (count % 2 == 0) {
                mb.begin();
                MeshPartBuilder mpb = mb.part("cylinderA" + count, GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                        new Material(ColorAttribute.createDiffuse(Color.WHITE), /*TextureAttribute.createDiffuse(img5),*/ blendingAttribute));
                mpb.sphere(6f, 6f, 6f, 120, 120, ip.uFrom, ip.uTo, ip.vFrom, ip.vTo);
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                meshBuilder.sphere2(6.015f, 6.015f, 6.015f, 200, 2, ip.uFrom, ip.uTo, ip.vTo-4, ip.vTo,0.5f,1.0f);
                Mesh mesh1 = meshBuilder.end();
                mb.part("cylinderB",mesh1,GL20.GL_TRIANGLES,new Material(blendingAttribute));
                model = mb.end();
            } else {
                mb.begin();
                MeshPartBuilder mpb = mb.part("cylinderA" + count, GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                        new Material(ColorAttribute.createDiffuse(Color.WHITE), /*TextureAttribute.createDiffuse(img4),*/ blendingAttribute));
                mpb.sphere(6f, 6f, 6f, 120, 120, ip.uFrom, ip.uTo, ip.vFrom, ip.vTo);
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                meshBuilder.sphere2(6.015f, 6.015f, 6.015f, 200, 2, ip.uFrom, ip.uTo, ip.vTo-4, ip.vTo,0.5f,1.0f);
                Mesh mesh1 = meshBuilder.end();
                mb.part("cylinderB",mesh1,GL20.GL_TRIANGLES,new Material(blendingAttribute));

                model = mb.end();
            }
            instance2 = new ArticleInstance(model, ip.center, ip.dimensions, ip.radius, ip.bounds, ip.row, ip.col, 30l, count * 400);
            instance2.startSpinner();
            List<ArticleInstance> row = rows.get(ip.row);
            if (row == null) {
                row = new ArrayList<ArticleInstance>();
            }
            row.add(instance2);
            rows.put(ip.row, row);

            List<ArticleInstance> col = cols.get(ip.col);
            if (col == null) {
                col = new ArrayList<ArticleInstance>();
            }
            col.add(instance2);
            cols.put(ip.col, col);

            cylinderSides.add(instance2);
            Log.e("Box", "[" + count + "," + instance2.bounds.toString());
            count++;
        }


//        List<ImagePane> mods = new ArrayList<ImagePane>();
//        int count = 0;
//        for(float i = 0, col = 0; i < 360f; i+=40f, col++) {
//            for (float p = 57f, row = 0; p < 107; p += 23f, row++) {
//                if (count % 2 == 0) {
//                    model = mb.createSphere(6f, 6f, 6f, 120, 120,
//                            new Material(//IntAttribute.createCullFace(GL20.GL_FRONT_AND_BACK),//For some reason, libgdx ModelBuilder makes boxes with faces wound in reverse, so cull FRONT
//                                    //new BlendingAttribute(1f), //opaque since multiplied by vertex color
////                                    new DepthTestAttribute(GL20.GL_ALWAYS,true), //don't want depth mask or rear cubes might not show through
//                                    ColorAttribute.createDiffuse(Color.WHITE),
//                                    TextureAttribute.createDiffuse(img5), //),
//                                    blendingAttribute),
//                            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
//                            i, i + 38f, p, p + 21f
//                    );
//                } else {
//                    model = mb.createSphere(6f, 6f, 6f, 120, 120,
//                            new Material(//IntAttribute.createCullFace(GL20.GL_FRONT_AND_BACK),//For some reason, libgdx ModelBuilder makes boxes with faces wound in reverse, so cull FRONT
//                                    //new BlendingAttribute(1f), //opaque since multiplied by vertex color
//                                    //new DepthTestAttribute(GL20.GL_ALWAYS,true), //don't want depth mask or rear cubes might not show through
//                                    ColorAttribute.createDiffuse(Color.WHITE),
//                                    TextureAttribute.createDiffuse(img3), //),
//                                    blendingAttribute),
//                            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
//                            i, i + 38f, p, p + 21f
//                    );
//                }
//                instance2 = new ArticleInstance(model);
//
//                ImagePane ip = new ImagePane(i, i + 38f, p, p + 21f, instance2.center, instance2.dimensions, instance2.radius, instance2.bounds,row,col);
//                mods.add(ip);
//                cylinderSides.add(instance2);
//                Log.e("Box", "[" + count + "," + instance2.bounds.toString());
//
//                List<ArticleInstance> rowL = rows.get(ip.row);
//                if (rowL == null) {
//                    rowL = new ArrayList<ArticleInstance>();
//                }
//                rowL.add(instance2);
//                rows.put(ip.row, rowL);
//
//                List<ArticleInstance> colL = cols.get(ip.col);
//                if (colL == null) {
//                    colL = new ArrayList<ArticleInstance>();
//                }
//                colL.add(instance2);
//                cols.put(ip.col, colL);
//
//                cylinderSides.add(instance2);
//                Log.e("Box", "[" + count + "," + instance2.bounds.toString());
//                count++;
//                count++;
//            }
//        }
//        File sdDir = context.getExternalFilesDir(null);
//        File output = new File(sdDir,"output.txt");
//        output.delete();
//        try {
//            FileWriter writer = new FileWriter(output);
//            gson.toJson(mods, writer); // writing to file
//            writer.flush();
//            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//



        camController = new CameraInputController(cam);
        camController.pinchZoomFactor = 6f;
//        camController.decal = sprite2;
//        Gdx.input.setInputProcessor(camController);
        MyGestureListener myGestureListener = new MyGestureListener(this);
        GestureDetector gd = new GestureDetector(myGestureListener);
        myGestureListener.setGd(gd);

        Gdx.input.setInputProcessor(new InputMultiplexer(gd, camController));

        model = mb.createSphere(6f, 6f, 6f, 120, 120,
                new Material(ColorAttribute.createDiffuse(Color.WHITE),
                        blendingAttribute2),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                0f, 360f, 0f, 50f
        );

        stop = new ModelInstance(model);

        model = mb.createSphere(6f, 6f, 6f, 120, 120,
                new Material(ColorAttribute.createDiffuse(Color.WHITE),
                        blendingAttribute2),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                0f, 360f, 130f, 180f
        );

        sbot = new ModelInstance(model);

//        mb.begin();
//        MeshPartBuilder mpb = mb.part("cylinder", GL20.GL_TRIANGLES,
//                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
//                new Material(ColorAttribute.createDiffuse(Color.BLUE),blendingAttribute2));
//        mpb.sphere(300f, 300f, 300f, 30, 30, 0f, 360f, 88f, 92f);
//        mpb = mb.part("cylinder", GL20.GL_TRIANGLES,
//                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
//                new Material(ColorAttribute.createDiffuse(Color.WHITE),blendingAttribute2,texture2));
//        mpb.sphere(300f, 300f, 300f, 30, 30, 0f, 360f, 88f, 92f);
//        model = mb.end();


//        model = mb.createSphere(300f, 300f, 300f, 30, 30,
//                new Material(ColorAttribute.createDiffuse(Color.WHITE),
//                        blendingAttribute2,texture2),
//                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
//                0f, 360f, 88f, 92f
//        );

//        stxt = new ModelInstance(model);

//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.scale(.5f,.5f,1f);

//        stxt.model.meshes.get(0).setAutoBind(true);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setAutoBind(true);
//            Vector3 v1 = cam.frustum.planePoints[4];
//            Vector3 v2 = cam.frustum.planePoints[5];
//            Vector3 v3 = cam.frustum.planePoints[6];
//            Vector3 v4 = cam.frustum.planePoints[7];
//
//            model = mb.createRect(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, v3.x, v3.y, v3.z, v4.x, v4.y, v4.z, 0f, 0f, 0f, new Material(//IntAttribute.createCullFace(GL20.GL_FRONT_AND_BACK),//For some reason, libgdx ModelBuilder makes boxes with faces wound in reverse, so cull FRONT
//                            //new BlendingAttribute(1f), //opaque since multiplied by vertex color
//                            //new DepthTestAttribute(GL20.GL_ALWAYS,true), //don't want depth mask or rear cubes might not show through
//                            ColorAttribute.createDiffuse(Color.WHITE),
//                            TextureAttribute.createDiffuse(img6), //),
//                            blendingAttribute),
//                    VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
//            );
//        instance3 = new ArticleInstance(model);

        startSignal.countDown();
        if(NewApplication.getInstance().mBound) {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    Log.e("Handler","Called Parse");
                    NewApplication.getInstance().mService.parse();
                }
            }).start();
        }


//        final RotateRow rr = new RotateRow(this,2f,0.4f,100,15l);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    new Thread(rr).start();
//                }
//            }
//        }).start();


//        Thread th = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    for(int in = 0; in < cylinderSides.size; in ++) {
//                        MeshPart meshPart = cylinderSides.get(in).model.nodes.get(0).parts.get(1).meshPart;
//                        Mesh mesh = meshPart.mesh;
//                        int cap = mesh.getVerticesBuffer().capacity();
//
//                        mesh.getVertices(meshPart.indexOffset, all2);
//
//                        for (int i = 6; i < all2.length; i += 8) {
//                            float tmp = all2[i] + .001f;
//                            if (tmp > 1.0) {
//                                tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
//                            }
//                            all2[i] = tmp;
//                        }
//
//                        mesh.updateVertices(meshPart.indexOffset,all2);
//                    }
//                }
//            }
//        });
//        th.start();
//
//        th = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    for(int in = 0; in < 8; in ++) {
//                        int cap = cylinderSides.get(in).model.nodes.get(1).parts.get(0).meshPart.mesh.getVerticesBuffer().capacity();
//                        float[] all2 = new float[cap];
//                        cylinderSides.get(in).model.nodes.get(1).parts.get(0).meshPart.mesh.getVertices(all2);
//
//                        for (int i = 6; i < all2.length; i += 8) {
//                            float tmp = all2[i] + .001f;
//                            if (tmp > 1.0) {
//                                tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
//                            }
//                            all2[i] = tmp;
//                        }
//
//                        cylinderSides.get(in).model.nodes.get(1).parts.get(0).meshPart.mesh.updateVertices(0,all2);
//                    }
//                }
//            }
//        });
//        th.start();
//
//        th = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    for(int in = 8; in < 16; in ++) {
//                        int cap = cylinderSides.get(in).model.nodes.get(1).parts.get(0).meshPart.mesh.getVerticesBuffer().capacity();
//                        float[] all2 = new float[cap];
//                        cylinderSides.get(in).model.nodes.get(1).parts.get(0).meshPart.mesh.getVertices(all2);
//
//                        for (int i = 6; i < all2.length; i += 8) {
//                            float tmp = all2[i] + .001f;
//                            if (tmp > 1.0) {
//                                tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
//                            }
//                            all2[i] = tmp;
//                        }
//
//                        cylinderSides.get(in).model.nodes.get(1).parts.get(0).meshPart.mesh.updateVertices(0,all2);
//                    }
//                }
//            }
//        });
//        th.start();
//
//        th = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    for(int in = 16; in < cylinderSides.size; in ++) {
//                        int cap = cylinderSides.get(in).model.nodes.get(0).parts.get(1).meshPart.mesh.getVerticesBuffer().capacity();
//                        float[] all2 = new float[cap];
//                        cylinderSides.get(in).model.nodes.get(0).parts.get(1).meshPart.mesh.getVertices(all2);
//
//                        for (int i = 6; i < all2.length; i += 8) {
//                            float tmp = all2[i] + .001f;
//                            if (tmp > 1.0) {
//                                tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
//                            }
//                            all2[i] = tmp;
//                        }
//
//                        cylinderSides.get(in).model.nodes.get(0).parts.get(1).meshPart.mesh.updateVertices(0,all2);
//                    }
//                }
//            }
//        });
//        th.start();
    }


    public RotateGlobe rg = null;

    public void startRotateGlobe(float deg) {
        if(rg != null) {
            rg.running = false;
        }

        rg = new RotateGlobe(camController,deg);

        new Thread(rg).start();
    }

    public void stopRotateGlobe() {
        if(rg != null) {
            rg.running = false;
        }
    }

    public static class RotateGlobe implements Runnable {

        private CameraInputController cameraInputController;
        private float degrees;
        public boolean running = true;

        public RotateGlobe(CameraInputController cameraInputController, float degrees) {
            this.cameraInputController = cameraInputController;
            this.degrees = degrees;
        }

        @Override
        public void run() {
            if(cameraInputController != null && cameraInputController.camera != null && cameraInputController.camera.up != null) {
                while (running) {
                    cameraInputController.camera.up.rotate(degrees, 0f, 0f, 1.01f);
                    cameraInputController.camera.update();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static class RotateRow implements Runnable {

        public Ars3d ars3d;
        public float rowId;
        public float degreesToRotate;
        public int rotateCycles;
        public long cycleTime;

        public RotateRow(Ars3d ars3d, float rowId, float degreesToRotate, int rotateCycles, long cycleTime) {
            this.ars3d = ars3d;
            this.rowId = rowId;
            this.degreesToRotate = degreesToRotate;
            this.rotateCycles = rotateCycles;
            this.cycleTime = cycleTime;
        }

        @Override
        public void run() {
            for (int count = 0; count < rotateCycles; count++) {
                try {
                    Thread.sleep(cycleTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<ArticleInstance> row = ars3d.rows.get(rowId);
                for (ArticleInstance ai : row) {
                    ai.transform.rotate(0.0f, 600.0f, 0.0f, degreesToRotate);
                    ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                    ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                }
            }
        }
    }

    public void startAllGlobeRotation () {
        final RotateAll rr = new RotateAll(this, cylinderSides, 0.1f, 20l);
        new Thread(rr).start();
    }


    public static class RotateAll implements Runnable {

        public Ars3d ars3d;
        public float degreesToRotate;
        public boolean rotating = true;
        public long cycleTime;
        public Array<ArticleInstance> allPanes;

        public RotateAll(Ars3d ars3d, Array<ArticleInstance> allPanes, float degreesToRotate, long cycleTime) {
            this.ars3d = ars3d;
            this.allPanes = allPanes;
            this.degreesToRotate = degreesToRotate;
            this.cycleTime = cycleTime;
        }

        @Override
        public void run() {
            while (rotating) {
                try {
                    Thread.sleep(cycleTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (ArticleInstance ai : allPanes) {
                    ai.transform.rotate(0.0f, 600.0f, 0.0f, degreesToRotate);
                    ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                    ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                }
            }
        }
    }

    float[] all2 = new float[10000];
    private Pixmap pm;

    int count = 0;

    boolean firstRender;

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        try {
            if(imageUpdates) {
                updateImages();
                imageUpdates = false;
            }
            manager.update();
        } catch (Exception e) {
            e.printStackTrace();
        }

        camController.update();
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);

//        if(!firstRender) {
        spriteBatch.begin();
//        spriteBatch.enableBlending();
//        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        spriteBatch.draw(image, 0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

//        spriteBatch.flush();
        spriteBatch.draw(manager.get(Gdx.files.internal("earthmoon.jpg").path(), Texture.class), 0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2)
//        spriteBatch.draw(manager.get(Gdx.files.internal("brankic1979-icon-set.jpg").path(), Texture.class), 20f, 20f, 35f, 40f, 950.0f / img6.getWidth(), 875.0f / img6.getHeight(),
//                    985.0f / img6.getHeight(), 915.0f / img6.getHeight());
//        spriteBatch.flush();
//        icons.setRegion(950,875,35,40);
//        spriteBatch.draw(icons, 200, 200,20,20);
            //        sprite = Decal.newDecal(1,1,new TextureRegion(img6,950,875,35,40));
//        sprite1 = Decal.newDecal(1,1,new TextureRegion(img6,950,875,35,40));
        spriteBatch.end();
//            firstRender = false;
//        }

//        spriteBatch2.begin();
//        sprite.draw(spriteBatch2);
//        spriteBatch2.end();

        modelBatch.begin(cam);

//        Vector3 vec1 = cam.frustum.planePoints[4];
//        Vector3 vec2 = cam.frustum.planePoints[5];
//        Vector3 vec3 = cam.frustum.planePoints[6];
//        Vector3 vec4 = cam.frustum.planePoints[7];


//        background.transform.set()
//
//        MeshPart meshPart = background.model.nodes.get(0).parts.get(0).meshPart;
//        Mesh mesh = meshPart.mesh;
//
//        int cap2 = mesh.getVerticesBuffer().capacity();
//        mesh.getVerticesBuffer().limit(cap2);
//
//        float [] verts = new float[32];
//
//
//        mesh.getVertices(0, cap2, verts);
//        verts[0] = vec1.x;
//        verts[1] = vec1.y;
//        verts[2] = vec1.z;
//        verts[8] = vec2.x;
//        verts[9] = vec2.y;
//        verts[10] = vec2.z;
//        verts[16] = vec3.x;
//        verts[17] = vec3.y;
//        verts[18] = vec3.z;
//        verts[24] = vec4.x;
//        verts[25] = vec4.y;
//        verts[26] = vec4.z;
//        mesh.setVertices(verts, 0, cap2);

//        BoundingBox bounds = new BoundingBox();
//        background.calculateBoundingBox(bounds);


//        background.transform.setToLookAt(cam.position,bounds.getCenter(), cam.up);
//        mesh.set(position).sub(this.position).nor();
//        setRotation(dir, up)

//        background.model.nodes.get(0).parts.get(0).meshPart.mesh.getNumVertices()

//        sprite.lookAt(cam.position, cam.up);
//        decalBatch.add(sprite);
//        sprite2.lookAt(cam.position, cam.up);
//        sprite2.setScale(camController.gestureListener.previousZoom * camController.pinchZoomFactor);
//        cam.frustum.planePoints[]
//        sprite2.setWidth(cam.view.Gdx.graphics.getWidth());
//        sprite2.setHeight(Gdx.graphics.getHeight());
//        sprite2.setScaleX(cam.projection.);
//        sprite2.setScaleY(cam.projection.getScaleY());

//        sprite2.setScale(1+((vec1.dst2(vec4) / orVec1.dst2(orVec4)) - 1)*100);
//        float distsq = sprite2.getPosition().dst2(cam.position);
//        Double newW = Math.sqrt(sqrtw - (distsq * distsq))*2;
//        Double newH = Math.sqrt(sqrth - (distsq * distsq))*2;
//
//        sprite2.setWidth(new Double(tanW + distsq).floatValue());
//        sprite2.setHeight(new Double(tanH + distsq).floatValue());
//        decalBatch.add(sprite2);
//        decalBatch.flush();

//        modelBatch.render(background);


        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);

        List<ArticleInstance> row1 = rows.get(1.0f);

        boolean wasUpdate = false;

        for (ArticleInstance ai : row1) {
//            ai.transform.rotate(0.0f, 600.0f, 0.0f, -1.0f);
//            ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
//            ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
            if (ai.update) {
                wasUpdate = true;
//                ai.update = false;
                Material material = ai.materials.get(0);
                if (ai.arsEntity.localImgPath != null) {
                    if(manager.isLoaded(Gdx.files.absolute(ai.arsEntity.localImgPath).path())) {
//                        Texture tmpTexture = textureMap.get(ai.arsEntity.link);
//                        if (tmpTexture == null) {
//                            tmpTexture = new Texture(Gdx.files.absolute(ai.arsEntity.localImgPath));
//                        }
                        material.set(TextureAttribute.createDiffuse(manager.get(new AssetDescriptor<Texture>(Gdx.files.absolute(ai.arsEntity.localImgPath).path(), Texture.class))));
//                        textureMap.put(ai.arsEntity.link, tmpTexture);

                        material = ai.materials.get(1);
                        if (ai.arsEntity.title != null) {
                            Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                            if (tmpTexture2 != null) {
                                material.set(TextureAttribute.createDiffuse(tmpTexture2));
                            }
                        }
                        ai.update = false;
                    }
                } else {
//                    if (!img4.getTextureData().isPrepared()) img4.getTextureData().prepare();
//                    img4.bind();
                    material.set(TextureAttribute.createDiffuse(manager.get(Gdx.files.internal("backg.jpg").path(), Texture.class)));
                    material = ai.materials.get(1);
                    if (ai.arsEntity.title != null) {
                        Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                        if (tmpTexture2 != null) {
                            material.set(TextureAttribute.createDiffuse(tmpTexture2), blendingAttribute);
                        }
                    }
                    ai.update = false;
                }

            }
//            ai.calculateTransforms();
        }

//        for (ArticleInstance ai : row1) {
////            ai.transform.rotate(0.0f, 600.0f, 0.0f, 1.0f);
////            ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), 1.0f);
////            ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), 1.0f);
//            if (ai.update) {
//                wasUpdate = true;
//                ai.update = false;
//                Material material = ai.materials.get(0);
//                if (ai.arsEntity.localImgPath != null) {
//
//
//
//                    Texture tmpTexture = textureMap.get(ai.arsEntity.link);
//                    if (tmpTexture == null) {
//                        tmpTexture = new Texture(Gdx.files.absolute(ai.arsEntity.localImgPath));
//                    }
//                    material.set(TextureAttribute.createDiffuse(tmpTexture));
//                    textureMap.put(ai.arsEntity.link,tmpTexture);
//                } else {
////                    if (!img4.getTextureData().isPrepared()) img4.getTextureData().prepare();
////                    img4.bind();
//
//                    material.set(TextureAttribute.createDiffuse(img4));
//                }
//                material = ai.materials.get(1);
//                if (ai.arsEntity.title != null) {
//                    Pixmap tmpTexture2 = textureMap2.get(ai.arsEntity.title);
//                    if (tmpTexture2 != null) {
//                        material.set(TextureAttribute.createDiffuse(new Texture(tmpTexture2)));
//                    }
//                }
//            }
////            ai.calculateTransforms();
//        }

        List<ArticleInstance> row2 = rows.get(2.0f);

        for (ArticleInstance ai : row2) {
//            ai.transform.rotate(0.0f, 600.0f, 0.0f, -1.0f);
//            ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
//            ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
            if (ai.update) {
                wasUpdate = true;
//                ai.update = false;
                Material material = ai.materials.get(0);
                if (ai.arsEntity.localImgPath != null) {
                    if(manager.isLoaded(Gdx.files.absolute(ai.arsEntity.localImgPath).path())) {
//                        Texture tmpTexture = textureMap.get(ai.arsEntity.link);
//                        if (tmpTexture == null) {
//                            tmpTexture = new Texture(Gdx.files.absolute(ai.arsEntity.localImgPath));
//                        }
                        material.set(TextureAttribute.createDiffuse(manager.get(new AssetDescriptor<Texture>(Gdx.files.absolute(ai.arsEntity.localImgPath).path(), Texture.class))));
//                        textureMap.put(ai.arsEntity.link, tmpTexture);

                        material = ai.materials.get(1);
                        if (ai.arsEntity.title != null) {
                            Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                            if (tmpTexture2 != null) {
                                material.set(TextureAttribute.createDiffuse(tmpTexture2));
                            }
                        }
                        ai.update = false;
                    }
                } else {
//                    if (!img4.getTextureData().isPrepared()) img4.getTextureData().prepare();
//                    img4.bind();
                    material.set(TextureAttribute.createDiffuse(manager.get(Gdx.files.internal("backg.jpg").path(), Texture.class)));
                    material = ai.materials.get(1);
                    if (ai.arsEntity.title != null) {
                        Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                        if (tmpTexture2 != null) {
                            material.set(TextureAttribute.createDiffuse(tmpTexture2));
                        }
                    }
                    ai.update = false;
                }

            }
//            ai.calculateTransforms();
        }

//        for (ArticleInstance ai : row2) {
////            ai.transform.rotate(0.0f, 600.0f, 0.0f, -1.0f);
////            ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
////            ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
//            if (ai.update) {
//                wasUpdate = true;
//                ai.update = false;
//                Material material = ai.materials.get(0);
//                if (ai.arsEntity.localImgPath != null) {
//                    Texture tmpTexture = textureMap.get(ai.arsEntity.link);
//                    if (tmpTexture == null) {
//                        tmpTexture = new Texture(Gdx.files.absolute(ai.arsEntity.localImgPath));
//                    }
//                    material.set(TextureAttribute.createDiffuse(tmpTexture));
//                    textureMap.put(ai.arsEntity.link,tmpTexture);
//                } else {
////                    if (!img4.getTextureData().isPrepared()) img4.getTextureData().prepare();
////                    img4.bind();
//                    material.set(TextureAttribute.createDiffuse(img4));
//                }
//                material = ai.materials.get(1);
//                if (ai.arsEntity.title != null) {
//                    Pixmap tmpTexture2 = textureMap2.get(ai.arsEntity.title);
//                    if (tmpTexture2 != null) {
//                        material.set(TextureAttribute.createDiffuse(new Texture(tmpTexture2)));
//                    }
//                }
//            }
////            ai.calculateTransforms();
//        }

        List<ArticleInstance> row0 = rows.get(0.0f);

        for (ArticleInstance ai : row0) {
//            ai.transform.rotate(0.0f, 600.0f, 0.0f, -1.0f);
//            ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
//            ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), -1.0f);
            if (ai.update) {
                wasUpdate = true;
//                ai.update = false;
                Material material = ai.materials.get(0);
                if (ai.arsEntity.localImgPath != null) {
                    if(manager.isLoaded(Gdx.files.absolute(ai.arsEntity.localImgPath).path())) {
//                        Texture tmpTexture = textureMap.get(ai.arsEntity.link);
//                        if (tmpTexture == null) {
//                            tmpTexture = new Texture(Gdx.files.absolute(ai.arsEntity.localImgPath));
//                        }
                        material.set(TextureAttribute.createDiffuse(manager.get(new AssetDescriptor<Texture>(Gdx.files.absolute(ai.arsEntity.localImgPath).path(), Texture.class))));
//                        textureMap.put(ai.arsEntity.link, tmpTexture);

                        material = ai.materials.get(1);
                        if (ai.arsEntity.title != null) {
                            Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                            if (tmpTexture2 != null) {
                                material.set(TextureAttribute.createDiffuse(tmpTexture2));
                            }
                        }
                        ai.update = false;
                    }
                } else {
//                    if (!img4.getTextureData().isPrepared()) img4.getTextureData().prepare();
//                    img4.bind();
                    material.set(TextureAttribute.createDiffuse(manager.get(Gdx.files.internal("backg.jpg").path(), Texture.class)));
                    material = ai.materials.get(1);
                    if (ai.arsEntity.title != null) {
                        Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                        if (tmpTexture2 != null) {
                            material.set(TextureAttribute.createDiffuse(tmpTexture2));
                        }
                    }
                    ai.update = false;
                }

            }
//            ai.calculateTransforms();
        }


//        if(count++ % 2 == 0) {
//            if (!wasUpdate) {
//                for (int in2 = 0; in2 < cylinderSides.size; in2++) {
//                    MeshPart meshPart = cylinderSides.get(in2).model.nodes.get(0).parts.get(1).meshPart;
//                    Mesh mesh = meshPart.mesh;
//
//                    int cap2 = mesh.getVerticesBuffer().capacity();
//                    mesh.getVerticesBuffer().limit(cap2);
//                    mesh.getVertices(0, cap2, all2);
//
//                    for (int i = 6; i < cap2; i += 8) {
//                        float tmp = all2[i] + .005f;
//                        if (tmp > 1.0) {
//                            tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
//                        }
//                        all2[i] = tmp;
//                    }
//                    mesh.setVertices(all2, 0, cap2);
//                }
//            }
//        }

        TreeMap<Float, Integer> tm = getByDistance();
        for (Integer in : tm.descendingMap().values()) {
            modelBatch.render(cylinderSides.get(in));
        }

        if(arrow != null) {
            modelBatch.render(arrow);
        }

        modelBatch.render(stop);
        modelBatch.render(sbot);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().capacity());
//        int vetrs = stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getNumVertices();
//        int cap = stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().capacity();
//        float [] all2 = new float[1];
//        for(int i = 6; i < cap; i +=8) {
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(i,1,all2);
//            float tmp = all2[0] + .001f;
//            if(tmp > 1.0) {
//                tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
//            }
//            all2[0] = tmp;
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(i, all2);
//        }
//
//        float [] all = new float[8];
//        for(int i = 0; i < cap; i +=8) {
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(i,8,all);
//            Log.w("Cords", Arrays.toString(all));
//        }

//
//
//        float [] oldverts1 = new float[2];
//        float [] oldverts2 = new float[2];
//        float [] oldverts3 = new float[2];
//        float [] oldverts4 = new float[2];
//        float [] newverts1 = new float[2];
//        float [] newverts2 = new float[2];
//        float [] newverts3 = new float[2];
//        float [] newverts4 = new float[2];
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(6,2,oldverts1);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(14,2,oldverts2);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(22,2,oldverts3);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(30,2,oldverts4);
//
//        for(int i = 38; i < vetrs; i = i + 32) {
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
////            int pos = stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().position();
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().position(i);
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().get(newverts, 0, 2);
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().position(pos);
//
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(i,2,newverts1);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(i+8,2,newverts2);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(i+16,2,newverts3);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVertices(i+24,2,newverts4);
//
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(oldverts1,i,2);
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(i,oldverts1);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(oldverts2,i+8,2);
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(i+8,oldverts2);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(oldverts3,i+16,2);
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(i+16,oldverts3);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(oldverts4,i+24,2);
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(i+24,oldverts4);
//
////            stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(i,oldverts);
//            System.arraycopy(newverts1,0,oldverts1,0,2);
//            System.arraycopy(newverts2,0,oldverts2,0,2);
//            System.arraycopy(newverts3,0,oldverts3,0,2);
//            System.arraycopy(newverts4,0,oldverts4,0,2);
////            oldverts = newverts;
//        }
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(newverts1,6,2);
////        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(6,oldverts1);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(newverts2,14,2);
////        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(14,oldverts2);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(newverts3,22,2);
////        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(22,oldverts3);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.getVerticesBuffer().limit(cap);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.setVertices(newverts4,30,2);
////        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.updateVertices(30,oldverts3);
////        stxt.model.meshes.get(0).updateVertices(6,oldverts);

//        Matrix3 mat = new Matrix3();
//        mat.setToRotation(.5f);
//        mat.trn(.0002f,1f);
//        stxt.model.nodes.get(0).parts.get(0).meshPart.mesh.transformUV(mat);

//        modelBatch.render(stxt);
//        modelBatch.flush();



//        spriteBatch.setProjectionMatrix(cam.combined );
//        spriteBatch.begin();
////        spriteBatch.disableBlending();
////        spriteBatch.setColor(Color.WHITE);
//        spriteBatch.draw(img5,0,0,200,200);
////        font.setColor(Color.BLACK);
//        spriteBatch.setShader(fontShader);
////        font.setScale(12);
//        font.setColor(Color.RED);
//        font.draw(spriteBatch, "Hello smooth world!", 0f, 0f);
//        spriteBatch.setShader(null);
////        spriteBatch.flush();
//        spriteBatch.end();
        modelBatch.end();
//        spriteBatch.begin();
//        spriteBatch.disableBlending();
//        spriteBatch.draw(manager.get(Gdx.files.internal("brankic1979-icon-set.jpg").path(), Texture.class), 20f, 20f, 35f, 40f, 950.0f / img6.getWidth(), 875.0f / img6.getHeight(),
//                985.0f / img6.getHeight(), 915.0f / img6.getHeight());
//        spriteBatch.end();


    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        for (ModelInstance mi : cylinderSides) {
            mi.model.dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private int selected = -1, selecting = -1;


    public class MyGestureListener implements GestureDetector.GestureListener {

        Ars3d ars3d;
        GestureDetector gd;

        public MyGestureListener(Ars3d ars3d) {
            this.ars3d = ars3d;
        }

        public GestureDetector getGd() {
            return gd;
        }

        public void setGd(GestureDetector gd) {
            this.gd = gd;
        }

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            if(ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
//            if(ars3d.flyMode) {
//                return false;
//            } else {

                int index = getObject((int)Math.round(x),(int)Math.round(y));
                if(index > 0) {
                    Intent intent = new Intent(context, WebViewActivity.class);
                    ArticleInstance ai = cylinderSides.get(index);
                    if(ai != null && ai.arsEntity != null && ai.arsEntity.getLink() != null && !ai.arsEntity.getLink().isEmpty()) {
                        intent.putExtra(WebViewActivity.URL, cylinderSides.get(index).arsEntity.getLink());
                        if (ars3d.parent != null) {
                            ars3d.parent.startActivity(intent);
                        }
                    }
                }
                return false;
//            }
        }

        @Override
        public boolean longPress(float x, float y) {
            if(ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            if(ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        float panStartX, panStartY, panStopX, panStopY;

        boolean firstPan = true;

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if(ars3d.flyMode) {
                return false;
            } else {
                if(firstPan) {
                    panStartX = x;
                    panStartY = y;
                    firstPan = false;
                }

                return true;
            }
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            if(ars3d.flyMode) {
                return false;
            } else {
                int startIndex = getObject((int)Math.round(panStartX),(int)Math.round(panStartY));
//                int stopIndex = getObject((int)Math.round(x),(int)Math.round(y));
                if(startIndex > 0 ) {// && stopIndex > 0) {
                    ArticleInstance aeStart = cylinderSides.get(startIndex);
//                    ArticleInstance aeStop = cylinderSides.get(stopIndex);
//                    if ((int) aeStart.row == (int) aeStop.row) {
                        float deg = -0.4f;
                        if(panStartX < x) {
                            deg = 0.4f;
                        }
                        final RotateRow rr = new RotateRow(ars3d, aeStart.row, deg, 100, 5l);
                        new Thread(rr).start();
//                    }

                }
                firstPan = true;

                return true;

            }
        }

        @Override
        public boolean zoom (float originalDistance, float currentDistance){
            if(ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
            if(ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        public void setSelected (int value) {
            if (selected == value) return;
            if (selected >= 0) {
            }
            selected = value;
            if (selected >= 0) {
                Log.e("Touched", cylinderSides.get(selected).arsEntity.getTitle());


                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(WebViewActivity.URL, cylinderSides.get(selected).arsEntity.getLink());
                ars3d.parent.startActivity(intent);

            }
        }

        public int getObject (int screenX, int screenY) {
            cam.update();
            Ray ray = cam.getPickRay(screenX, screenY);

            int result = -1;
            float distance = -1;

            for (int i = 0; i < cylinderSides.size; ++i) {
                final ArticleInstance instance = cylinderSides.get(i);

                instance.transform.getTranslation(position);
                position.add(instance.center);

                final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
                if (len < 0f)
                    continue;

                float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
                if (distance >= 0f && dist2 > distance)
                    continue;

                if (dist2 <= instance.radius * instance.radius) {
                    result = i;
                    distance = dist2;
                }
            }
            return result;
        }
    }


//    @Override
//    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//        if(flyMode) {
//            return super.touchDown(screenX, screenY, pointer, button);
//        } else {
//            selecting = getObject(screenX, screenY);
//            if(selecting < cylinderSides.size && selecting >= 0) {
//                Log.d("Touched", cylinderSides.get(selecting).arsEntity.getTitle());
//            }
//            return selecting >= 0;
//        }
//    }

//    @Override
//    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
//        if(flyMode) {
//            return super.touchUp(screenX, screenY, pointer, button);
//        } else {
//            if (selecting >= 0) {
//                if (selecting == getObject(screenX, screenY))
//                    setSelected(selecting);
//                selecting = -1;
//                return true;
//            }
//            return false;
//        }

//        int index = getObject(screenX, screenY);
//        if(index < cylinderSides.size && index >= 0) {
//            Log.d("Touched", cylinderSides.get(index).arsEntity.getTitle());
//        }

//        cam.update();
//        Ray ray = cam.getPickRay(screenX, screenY);
//
//        Vector3 v4 = cam.unproject(ray.direction.set(screenX, screenY, 0.5f), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        Vector3 v5 = cam.unproject(ray.direction.set(screenX, screenY, 1.0f), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


//        ModelBuilder mb = new ModelBuilder();
//        Vector3 v3 = new Vector3();
//        ray.getEndPoint(v3,20f);
//        try {
//            Model marrow = mb.createArrow(ray.origin.x, ray.origin.y, ray.origin.z,
//                    v3.x, v3.y, v3.z,
//                    0.02f, 0.02f, 5, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.RED), blendingAttribute3),
//                    VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

//            Model marrow = mb.createArrow(cam.position.x + (cam.position.x*(screenX/Gdx.graphics.getWidth())), cam.position.y + (cam.position.y*(screenY/Gdx.graphics.getHeight())), cam.position.z,
//                    cam.direction.x + (cam.direction.x*(screenX/Gdx.graphics.getWidth())), cam.direction.y + (cam.direction.y*(screenY/Gdx.graphics.getHeight())), cam.direction.z,
//                    0.02f, 0.02f, 5, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.RED), blendingAttribute2),
//                    VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

//        Model marrow = mb.createArrow(ray.origin,v3,new Material(ColorAttribute.createDiffuse(Color.RED)),VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
//            arrow = new ModelInstance(marrow);
//        }catch (Exception e) {
//
//        }
//        if (selecting >= 0) {
//            if (selecting == getObject(screenX, screenY))
//                setSelected(selecting);
//            selecting = -1;
//            return true;
//        }
//        return false;

//    }

//    @Override
//    public boolean touchDragged(int screenX, int screenY, int pointer) {
//        if(flyMode) {
//            return super.touchDragged(screenX,screenY,pointer);
//        } else {
//            return selecting >= 0;
//        }
//    }



//    public int getObject (int screenX, int screenY) {
//        Ray ray2 = camController.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
//        Ray ray = cam.getPickRay(screenX, screenY, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        int result = -1;
//        float distance = -1;
//        for (int i = 0; i < cylinderSides.size; ++i) {
//            final ArticleInstance instance = cylinderSides.get(i);
//            instance.transform.getTranslation(position);
//            position.add(instance.center);
//            float dist2 = ray.origin.dst2(position);
//            if (distance >= 0f && dist2 > distance) continue;
//            if (Intersector.intersectRaySphere(ray, position, instance.radius, null)) {
//                result = i;
//                distance = dist2;
//            }
//        }
//        return result;
//    }

    private Vector3 position = new Vector3();

    public TreeMap<Float, Integer> getByDistance() {
        TreeMap<Float, Integer> tm = new TreeMap<Float, Integer>();
        for (int i = 0; i < cylinderSides.size; ++i) {
            final ArticleInstance instance = cylinderSides.get(i);
            Float dist2 = cam.position.dst2(instance.center);
            tm.put(dist2, i);
        }
        return tm;
    }

    public void updateImages() {

        try {
            startSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TreeSet<ArsEntity> treeSet = NewApplication.getInstance().getItemsTreeSet();

        Log.e("TreeSet","size is " + treeSet.size());

        synchronized (treeSet) {
            Iterator<ArsEntity> it = treeSet.iterator();
            List<ArticleInstance> row = rows.get(1.0f);

            if (it != null) {
                Log.e("TreeSet","row 1 size is " + row.size());
                for (int i = 0; i < row.size() && it.hasNext(); i++) {
                    ArsEntity tmpEntity = it.next();
                    Log.w("Ars3dArs1", tmpEntity.toString());
                    ArticleInstance ai = row.get(i);
                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                        ai.arsEntity = tmpEntity;
                        textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        ai.update = true;
                        if(ai.arsEntity.getLocalImgPath() != null && !ai.arsEntity.getLocalImgPath().isEmpty()) {
                            AssetDescriptor ad = new AssetDescriptor(Gdx.files.absolute(ai.arsEntity.localImgPath), Texture.class);
                            if(ad.file.exists()) {
                                manager.load(ad);
                            } else {
                                ai.arsEntity.setLocalImgPath(null);
                            }
                        }
                    }
                }
                row = rows.get(0.0f);
                Log.e("TreeSet","row 0 size is " + row.size());
                for (int i = 0; i < row.size() && it.hasNext(); i++) {
                    ArsEntity tmpEntity = it.next();
                    Log.w("Ars3dArs0", tmpEntity.toString());
                    ArticleInstance ai = row.get(i);
                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                        ai.arsEntity = tmpEntity;
                        textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        ai.update = true;
                        if(ai.arsEntity.getLocalImgPath() != null && !ai.arsEntity.getLocalImgPath().isEmpty()) {
                            AssetDescriptor ad = new AssetDescriptor(Gdx.files.absolute(ai.arsEntity.localImgPath), Texture.class);
                            if(ad.file.exists()) {
                                manager.load(ad);
                            } else {
                                ai.arsEntity.setLocalImgPath(null);
                            }
                        }
                    }
                }
                row = rows.get(2.0f);
                Log.e("TreeSet","row 2 size is " + row.size());
                for (int i = 0; i < row.size() && it.hasNext(); i++) {
                    ArsEntity tmpEntity = it.next();
                    Log.w("Ars3dArs2", tmpEntity.toString());
                    ArticleInstance ai = row.get(i);
                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                        ai.arsEntity = tmpEntity;
                        textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        ai.update = true;
                        if(ai.arsEntity.getLocalImgPath() != null && !ai.arsEntity.getLocalImgPath().isEmpty()) {
                            AssetDescriptor ad = new AssetDescriptor(Gdx.files.absolute(ai.arsEntity.localImgPath), Texture.class);
                            if(ad.file.exists()) {
                                manager.load(ad);
                            } else {
                                ai.arsEntity.setLocalImgPath(null);
                            }
                        }
                    }
                }
            } else {
                Log.e("Ars3d", "Row or It null row [" + row + "] it [" + it + "]");
            }
        }

//        img4 = new Texture(img4.getTextureData());


    }


    public static class ArticleInstance extends ModelInstance {
        public Vector3 center = new Vector3();
        public Vector3 dimensions = new Vector3();
        public float radius;
        public float row;
        public float col;

        public BoundingBox bounds = new BoundingBox();

        public ArsEntity arsEntity;
        public Texture texture;
        public boolean update;

        public TextSpinner textSpinner;
        public Thread textSpinnerThread;
        public long textSleepTime;
        public long initialPause;

        public ArticleInstance(Model model, Vector3 center, Vector3 dimensions, float radius, BoundingBox bounds,
                               float row, float col, long textSleepTime, long initialPause) {
            super(model);
            this.center = center;
            this.dimensions = dimensions;
            this.radius = radius;
            this.bounds = bounds;
            this.row = row;
            this.col = col;
            this.textSleepTime = textSleepTime;
            this.initialPause = initialPause;
        }

        public ArticleInstance(Model model) {
            super(model);
            calculateBoundingBox(bounds);
            center.set(bounds.getCenter());
            dimensions.set(bounds.getDimensions());
            radius = dimensions.len() / 2f;
        }

        public ArticleInstance(Model model, float f1, float f2, float f3) {
            super(model, f1, f2, f3);
            calculateBoundingBox(bounds);
            center.set(bounds.getCenter());
            dimensions.set(bounds.getDimensions());
            radius = dimensions.len() / 2f;
        }

        public void startSpinner () {
            if(textSpinnerThread == null) {
                textSpinner = new TextSpinner(this,textSleepTime,initialPause);
                textSpinnerThread = new Thread(textSpinner);
                textSpinnerThread.start();
            }

        }

        public void stopSpinner () {
            if(textSpinner != null) {
                textSpinner.stopSpinner();
                textSpinnerThread = null;
                textSpinner = null;
            }
        }
    }

    public static class TextSpinner implements Runnable {

        public ArticleInstance ai;
        public static float[] all2 = new float[10000];
        public long sleepTime;
        public long initialPause;
        public boolean running = true;

        public TextSpinner(ArticleInstance ai, long sleepTime, long initialPause) {
            this.ai = ai;
            this.sleepTime = sleepTime;
            this.initialPause = initialPause;
        }

        public void stopSpinner() {
            running = false;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(initialPause);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (running) {
                MeshPart meshPart = ai.nodes.get(0).parts.get(1).meshPart;
                Mesh mesh = meshPart.mesh;

                int cap2 = mesh.getVerticesBuffer().capacity();
                mesh.getVerticesBuffer().limit(cap2);

                synchronized (all2) {
                    mesh.getVertices(0, cap2, all2);

                    for (int i = 6; i < cap2; i += 8) {
                        float tmp = all2[i] + .005f;
                        if (tmp > 1.0) {
                            tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                        }
                        all2[i] = tmp;
                    }
                    mesh.setVertices(all2, 0, cap2);
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class ImagePane implements Serializable {
        public float uFrom;
        public float uTo;
        public float vFrom;
        public float vTo;
        public float row;
        public float col;

        public Vector3 center = new Vector3();
        public Vector3 dimensions = new Vector3();
        public float radius;

        public BoundingBox bounds = new BoundingBox();

        public ImagePane() {
            super();
        }

        public ImagePane(float uFrom, float uTo, float vFrom, float vTo, Vector3 center, Vector3 dimensions, float radius, BoundingBox bounds, float row, float col) {
            this.uFrom = uFrom;
            this.uTo = uTo;
            this.vFrom = vFrom;
            this.vTo = vTo;
            this.center = center;
            this.dimensions = dimensions;
            this.radius = radius;
            this.bounds = bounds;
            this.row = row;
            this.col = col;

        }

        public float getuFrom() {
            return uFrom;
        }

        public void setuFrom(float uFrom) {
            this.uFrom = uFrom;
        }

        public float getuTo() {
            return uTo;
        }

        public void setuTo(float uTo) {
            this.uTo = uTo;
        }

        public float getvFrom() {
            return vFrom;
        }

        public void setvFrom(float vFrom) {
            this.vFrom = vFrom;
        }

        public float getvTo() {
            return vTo;
        }

        public void setvTo(float vTo) {
            this.vTo = vTo;
        }

        public Vector3 getCenter() {
            return center;
        }

        public void setCenter(Vector3 center) {
            this.center = center;
        }

        public Vector3 getDimensions() {
            return dimensions;
        }

        public void setDimensions(Vector3 dimensions) {
            this.dimensions = dimensions;
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public BoundingBox getBounds() {
            return bounds;
        }

        public void setBounds(BoundingBox bounds) {
            this.bounds = bounds;
        }

        public float getRow() {
            return row;
        }

        public void setRow(float row) {
            this.row = row;
        }

        public float getCol() {
            return col;
        }

        public void setCol(float col) {
            this.col = col;
        }
    }

    private Pixmap textToTexture (String text) {
        BitmapFont.TextBounds tb = font.getBounds(text);

        int totalWidth = 52;
        for(char ch : text.toCharArray()) {
            BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
            if(ch != 32 && glyph != null) {
                totalWidth += (glyph.width);
            } else {
                totalWidth += 26;
            }
        }

        Pixmap pm2 = new Pixmap((totalWidth + 260),143, pm.getFormat());

        int currentWidth = 52;
        for(char ch : text.toCharArray()) {
            BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
            if(ch != 32 && glyph != null) {
                pm2.drawPixmap(pm, glyph.srcX, glyph.srcY, glyph.width, glyph.height, currentWidth, Math.abs(glyph.height + glyph.yoffset), glyph.width, glyph.height);
                currentWidth += (glyph.width);
            } else {
                currentWidth += 26;
//                Log.e("Str", "Bad Char [" + ch + "] String [" +text + "]");
            }

            if(ch == 'f') {
                currentWidth -= 15;
            }
        }

        if(pm2.getWidth() > 4048) {
            Pixmap tmp = new Pixmap(4048, 143, pm.getFormat());
            tmp.drawPixmap(pm2,0,0,4048,143,0,0,4048,143);
            pm2 = tmp;
        }



//        Log.e("Widths", "pm [" + pm2.getWidth() + "] tb [" + ((int)tb.width + 378) + "] actual [" + currentWidth + "] title: " + text);

        return pm2;
    }

    public Boolean getImageUpdates() {
        return imageUpdates;
    }

    public void setImageUpdates(Boolean imageUpdates) {
        this.imageUpdates = imageUpdates;
    }
}
