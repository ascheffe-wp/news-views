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
    public ModelBatch modelBatch2;
    public ModelBatch modelBatch3;
    public SpriteBatch spriteBatch;
    public Boolean flyMode = true;

    public Boolean imageUpdates = false;

    public GroupStrategy strategy;

    private BlendingAttribute blendingAttribute;
    private BlendingAttribute blendingAttribute2;
    private BlendingAttribute blendingAttribute3;
    private BlendingAttribute blendingAttribute4;
    private BlendingAttribute blendingAttribute5;

    public Array<ArticleInstance> cylinderSides = new Array<ArticleInstance>();

    private Texture img4;
    private Texture img5, img6, image;
    private TextureRegion icons;

    public Map<String, Texture> textureMap = new HashMap<String, Texture>();
    public Map<String, Texture> textureMap2 = new HashMap<String, Texture>();

    private Gson gson;

    private Context context;

    private CountDownLatch startSignal = new CountDownLatch(1);

    public static Map<Float, List<ArticleInstance>> rows = new HashMap<Float, List<ArticleInstance>>();
    public static Map<Float, List<ArticleInstance>> cols = new HashMap<Float, List<ArticleInstance>>();

    private BitmapFont font;
    private ShaderProgram fontShader;

    private ModelInstance stop;
    private ModelInstance sbot;

    private ModelInstance arrow = null;

    AssetManager manager;

    Float startPosition;
    Float startRotation;

    public static volatile boolean tapable = true;

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

        NewApplication.getInstance().setArs3d(this);
        manager = new AssetManager();

        gson = new GsonBuilder().create();

        manager.load(new AssetDescriptor(Gdx.files.internal("backg.jpg"), Texture.class));
        manager.load(new AssetDescriptor(Gdx.files.internal("123750890.jpg"), Texture.class));
        manager.load(new AssetDescriptor(Gdx.files.internal("brankic1979-icon-set.jpg"), Texture.class));
        manager.load(new AssetDescriptor(Gdx.files.internal("earthmoon.jpg"), Texture.class));
        manager.update();
        manager.finishLoading();

        img4 = manager.get("backg.jpg", Texture.class);
        img5 = manager.get("123750890.jpg", Texture.class);
        img6 = manager.get("brankic1979-icon-set.jpg", Texture.class);
        image = manager.get("earthmoon.jpg", Texture.class);

        blendingAttribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f);
        blendingAttribute2 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.6f);
        blendingAttribute3 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.2f);
        blendingAttribute4 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.15f);
        blendingAttribute5 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.01f);

        ArsShaderProvider asp = new ArsShaderProvider(Gdx.files.internal("default.vertex3.glsl"),Gdx.files.internal("default.fragment3.glsl"));
        asp = new ArsShaderProvider();

        modelBatch = new ModelBatch(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 4)),asp,null);

        ArsShaderProvider asp2 = new ArsShaderProvider(Gdx.files.internal("default.vertex.glsl"),Gdx.files.internal("default.fragment.glsl"),
                Gdx.files.internal("default.vertex.glsl"),Gdx.files.internal("default.fragment2.glsl"));

        modelBatch2 = new ModelBatch(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 4)),
                asp2,null);

//        modelBatch2 = new ModelBatch(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 4)),
//                new DefaultShaderProvider(Gdx.files.internal("default.vertex.glsl"),Gdx.files.internal("default.fragment.glsl")),null);
//        modelBatch3 = new ModelBatch(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 4)),
//                new DefaultShaderProvider(Gdx.files.internal("default.vertex.glsl"),Gdx.files.internal("default.fragment2.glsl")),null);

        cam = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0.0f, 0.0f, -5.6f);
        if (startPosition != null) {
            cam.position.set(0.0f, 0.0f, startPosition);
        }
        cam.lookAt(0.0f, 0.0f, 0.0f);
        cam.near = 0.01f;
        cam.far = 100f;

        if (startRotation != null) {
            cam.up.rotate(startRotation, 0f, 0f, 1.01f);
            cam.update();
        }

        cam.update(true);

        ModelBuilder mb = new ModelBuilder();
        Model model = null;
        ArticleInstance instance2 = null;

        spriteBatch = new SpriteBatch(1);

        fontShader = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font.frag"));
        if (!fontShader.isCompiled()) {
            Gdx.app.error("fontShader", "compilation failed:\n" + fontShader.getLog());
        }

        Texture texture = new Texture(Gdx.files.internal("txt3.png"), true); // true enables mipmaps
        texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear); // linear filtering in nearest mipmap image

        font = new BitmapFont(Gdx.files.internal("txt3.fnt"), new TextureRegion(texture), false);
        pm = new Pixmap(Gdx.files.internal("txt3.png"));
        FileHandle fh = Gdx.files.internal("objs2.json");

//        ShaderProgram sp = new ShaderProgram(Gdx.files.internal("default.vertex.glsl"),Gdx.files.internal("default.fragment2.glsl"));



        ImagePane[] ips = gson.fromJson(fh.reader(), ImagePane[].class);
        for (ImagePane ip : ips) {
            if (count % 2 == 0) {
                mb.begin();
                MeshPartBuilder mpb = mb.part("cylinderA" + count, GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                        new Material(ColorAttribute.createDiffuse(Color.WHITE), /*TextureAttribute.createDiffuse(img5),*/ blendingAttribute2));
                mpb.sphere(6f, 6f, 6f, 120, 120, ip.uFrom, ip.uTo, ip.vFrom, ip.vTo);
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                meshBuilder.sphere2(6.015f, 6.015f, 6.015f, 200, 2, ip.uFrom, ip.uTo, ip.vTo - 4, ip.vTo, 0.9f, 1.0f);
                Mesh mesh1 = meshBuilder.end();
                mb.part("cylinderB", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute3));
                meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                meshBuilder.sphere2(5.985f, 5.985f, 5.985f, 200, 2, ip.uFrom, ip.uTo, ip.vTo - 4, ip.vTo, 0.9f, 1.0f);
                mesh1 = meshBuilder.end();
                mb.part("cylinderC", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute3));
                model = mb.end();
            } else {
                mb.begin();
                MeshPartBuilder mpb = mb.part("cylinderA" + count, GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                        new Material(ColorAttribute.createDiffuse(Color.WHITE), /*TextureAttribute.createDiffuse(img4),*/ blendingAttribute2));
                mpb.sphere(6f, 6f, 6f, 120, 120, ip.uFrom, ip.uTo, ip.vFrom, ip.vTo);
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                meshBuilder.sphere2(6.015f, 6.015f, 6.015f, 200, 2, ip.uFrom, ip.uTo, ip.vTo - 4, ip.vTo, 0.9f, 1.0f);
                Mesh mesh1 = meshBuilder.end();
                mb.part("cylinderB", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute3));
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                meshBuilder.sphere2(5.985f, 5.985f, 5.985f, 200, 2, ip.uFrom, ip.uTo, ip.vTo - 4, ip.vTo, 0.9f, 1.0f);
                mesh1 = meshBuilder.end();
                mb.part("cylinderC", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute3));
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
        MyGestureListener myGestureListener = new MyGestureListener(this);
        GestureDetector gd = new GestureDetector(myGestureListener);
        myGestureListener.setGd(gd);

        Gdx.input.setInputProcessor(new InputMultiplexer(gd, camController));

        model = mb.createSphere(6f, 6f, 6f, 120, 120,
                new Material(ColorAttribute.createDiffuse(Color.WHITE),
                        blendingAttribute4),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                0f, 360f, 0f, 50f
        );

        stop = new ModelInstance(model);

        model = mb.createSphere(6f, 6f, 6f, 120, 120,
                new Material(ColorAttribute.createDiffuse(Color.WHITE),
                        blendingAttribute4),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                0f, 360f, 130f, 180f
        );

        sbot = new ModelInstance(model);

        startSignal.countDown();

        if (NewApplication.getInstance().mBound) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("Handler", "Called Parse");
                    NewApplication.getInstance().mService.parse();
                }
            }).start();
        }

    }


    public RotateGlobe rg = null;

    public void startRotateGlobe(float deg) {
        if (rg != null) {
            rg.running = false;
        }

        rg = new RotateGlobe(camController, deg);

        new Thread(rg).start();
    }

    public void stopRotateGlobe() {
        if (rg != null) {
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
            if (cameraInputController != null && cameraInputController.camera != null && cameraInputController.camera.up != null) {
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
            try {
                tapable = false;
                List<ArticleInstance> row = ars3d.rows.get(rowId);
                if(row != null) {
                    for (int count = 0; count < rotateCycles; count++) {
                        try {
                            Thread.sleep(cycleTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (ArticleInstance ai : row) {
                            ai.transform.rotate(0.0f, 600.0f, 0.0f, degreesToRotate);
                            ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                            ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                        }
                    }
                    for (ArticleInstance ai2 : row) {
                        if (degreesToRotate > 0) {
                            float newRowId = ai2.col - 1;
                            if (newRowId < 0) {
                                newRowId = 8;
                            }
                            ai2.col = newRowId;
                        } else {
                            float newRowId = ai2.col + 1;
                            if (newRowId > 8) {
                                newRowId = 0;
                            }
                            ai2.col = newRowId;
                        }
                        cols.get(ai2.col).set((int)rowId,ai2);

                    }

                    resetColumns(rowId);
                    resetBlending = true;
                }
            } finally {
                tapable = true;
            }
        }


        private void resetColumns (float rowId) {
//            List<ArticleInstance> row0 = ars3d.rows.get(0f);
//            List<ArticleInstance> row1 = ars3d.rows.get(1f);
//            List<ArticleInstance> row2 = ars3d.rows.get(2f);
//
//            List<ArticleInstance> rowToProcess;
//            if(rowId == 0.0f) {
//                rowToProcess = row0;
//            } else if(rowId == 1.0f) {
//                rowToProcess = row1;
//            } else {
//                rowToProcess = row2;
//            }
//
//            for(int i = 0; i < rowToProcess.size(); i++) {
//
//                float colToCorrect = rowToProcess.get(i).col;
//
//                row0.get(i).col = colToCorrect;
//                row1.get(i).col = colToCorrect;
//                row2.get(i).col = colToCorrect;
//            }



            List<ArticleInstance> row0 = ars3d.rows.get(0f);
            List<ArticleInstance> row1 = ars3d.rows.get(1f);
            List<ArticleInstance> row2 = ars3d.rows.get(2f);
            for(int i = 0; i < row0.size(); i++) {
                float col0val = row0.get(i).col;
                float col1val = row1.get(i).col;
                float col2val = row2.get(i).col;

//                List<ArticleInstance> newCol = new ArrayList<ArticleInstance>();
                if(col0val == col1val) {
                    row0.get(i).col = col2val;
                    row1.get(i).col = col2val;

                } else if(col0val == col2val) {
                    row0.get(i).col = col1val;
                    row2.get(i).col = col1val;
                } else if(col1val == col2val) {
                    row1.get(i).col = col0val;
                    row2.get(i).col = col0val;
                }
//                newCol.add(row0.get(i));
//                newCol.add(row1.get(i));
//                newCol.add(row2.get(i));
//                cols.put(new Float(row0.get(i).col), newCol);

            }


        }
    }

    public void startAllGlobeRotation() {
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

    public static boolean incenter = true;
    public static boolean resetBlending = false;
    public Float prevCol = new Float(6.0);
//    boolean orientationChange = false;

    ModelBatch modelBatchInternal = null;
    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        try {
            if (imageUpdates) {
                updateImages();
                imageUpdates = false;
            }
            manager.update();
        } catch (Exception e) {
            e.printStackTrace();
        }

        camController.update();
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);


        //x = a+r\,\cos t,\,
//        Double r = Math.sqrt((cam.position.x*cam.position.x) + (cam.position.y*cam.position.y));
//        double xz = cam.position.x/r;
//        double angel = Math.acos(xz);
//        Log.e("Sphere","Inside Sphere [" + angel + "]");

//        Log.e("Sphere","Inside Sphere [" + cam.position + "]");
        float distToCenter = cam.position.dst2(0f, 0f, 0f);
//        float distToCenter2 = cam.position.dst2(0f,0f,0f);
//        Log.e("Sphere","[" + cam.position.toString() + "] " +" [" + Math.sqrt(distToCenter) + "] " + " [" + Math.sqrt(distToCenter2) + "]");
        if (distToCenter < 9.0f && !incenter) {
            incenter = true;
            modelBatchInternal = modelBatch2;
//            Log.e("Sphere","Inside Sphere [" + distToCenter + "]");
//            orientationChange = true;
        } else if (distToCenter > 9.0f && incenter) {
            incenter = false;
            modelBatchInternal = modelBatch;
//            Log.e("Sphere","Outside Sphere [" + distToCenter + "]");
//            orientationChange = true;
        }

        spriteBatch.begin();
        spriteBatch.draw(manager.get(Gdx.files.internal("earthmoon.jpg").path(), Texture.class), 0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.end();

        if(modelBatchInternal != null) {
            modelBatchInternal.begin(cam);

            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);

            List<ArticleInstance> row1 = rows.get(1.0f);


            for (ArticleInstance ai : row1) {
                if (ai.update) {
                    Material material = ai.materials.get(0);
                    if (ai.arsEntity.localImgPath != null) {
                        if (manager.isLoaded(Gdx.files.absolute(ai.arsEntity.localImgPath).path())) {
                            if (ai.flipped) {

                            } else {
                                material.set(TextureAttribute.createDiffuse(manager.get(new AssetDescriptor<Texture>(Gdx.files.absolute(ai.arsEntity.localImgPath).path(), Texture.class))));
                            }
                            material = ai.materials.get(1);
                            Material material2 = ai.materials.get(2);
                            if (ai.arsEntity.title != null) {
                                Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                                if (tmpTexture2 != null) {
                                    material.set(TextureAttribute.createDiffuse(tmpTexture2));
                                    material.id = "st";
                                    material2.set(TextureAttribute.createDiffuse(tmpTexture2));
                                    material2.id = "st";
                                }
                            }
                            ai.update = false;
                        }
                    } else {
                        material.set(TextureAttribute.createDiffuse(manager.get(Gdx.files.internal("backg.jpg").path(), Texture.class)));
                        material = ai.materials.get(1);
                        Material material2 = ai.materials.get(2);
                        if (ai.arsEntity.title != null) {
                            Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                            if (tmpTexture2 != null) {
                                material.set(TextureAttribute.createDiffuse(tmpTexture2), blendingAttribute);
                                material.id = "st";
                                material2.set(TextureAttribute.createDiffuse(tmpTexture2));
                                material2.id = "st";
                            }
                        }
                        ai.update = false;
                    }

                }
            }

            List<ArticleInstance> row2 = rows.get(2.0f);

            for (ArticleInstance ai : row2) {
                if (ai.update) {
                    Material material = ai.materials.get(0);
                    if (ai.arsEntity.localImgPath != null) {
                        if (manager.isLoaded(Gdx.files.absolute(ai.arsEntity.localImgPath).path())) {
                            material.set(TextureAttribute.createDiffuse(manager.get(new AssetDescriptor<Texture>(Gdx.files.absolute(ai.arsEntity.localImgPath).path(), Texture.class))));
                            material = ai.materials.get(1);
                            Material material2 = ai.materials.get(2);
                            if (ai.arsEntity.title != null) {
                                Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                                if (tmpTexture2 != null) {
                                    material.set(TextureAttribute.createDiffuse(tmpTexture2));
                                    material.id = "st";
                                    material2.set(TextureAttribute.createDiffuse(tmpTexture2));
                                    material2.id = "st";
                                }
                            }
                            ai.update = false;
                        }
                    } else {
                        material.set(TextureAttribute.createDiffuse(manager.get(Gdx.files.internal("backg.jpg").path(), Texture.class)));
                        material = ai.materials.get(1);
                        Material material2 = ai.materials.get(2);
                        if (ai.arsEntity.title != null) {
                            Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                            if (tmpTexture2 != null) {
                                material.set(TextureAttribute.createDiffuse(tmpTexture2));
                                material.id = "st";
                                material2.set(TextureAttribute.createDiffuse(tmpTexture2));
                                material2.id = "st";
                            }
                        }
                        ai.update = false;
                    }

                }
            }

            List<ArticleInstance> row0 = rows.get(0.0f);

            for (ArticleInstance ai : row0) {
                if (ai.update) {
                    Material material = ai.materials.get(0);
                    if (ai.arsEntity.localImgPath != null) {
                        if (manager.isLoaded(Gdx.files.absolute(ai.arsEntity.localImgPath).path())) {
                            material.set(TextureAttribute.createDiffuse(manager.get(new AssetDescriptor<Texture>(Gdx.files.absolute(ai.arsEntity.localImgPath).path(), Texture.class))));
                            material = ai.materials.get(1);
                            Material material2 = ai.materials.get(2);
                            if (ai.arsEntity.title != null) {
                                Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                                if (tmpTexture2 != null) {
                                    material.set(TextureAttribute.createDiffuse(tmpTexture2));
                                    material.id = "st";
                                    material2.set(TextureAttribute.createDiffuse(tmpTexture2));
                                    material2.id = "st";
                                }
                            }
                            ai.update = false;
                        }
                    } else {
                        material.set(TextureAttribute.createDiffuse(manager.get(Gdx.files.internal("backg.jpg").path(), Texture.class)));
                        material = ai.materials.get(1);
                        Material material2 = ai.materials.get(2);
                        if (ai.arsEntity.title != null) {
                            Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                            if (tmpTexture2 != null) {
                                material.set(TextureAttribute.createDiffuse(tmpTexture2));
                                material.id = "st";
                                material2.set(TextureAttribute.createDiffuse(tmpTexture2));
                                material2.id = "st";
                            }
                        }
                        ai.update = false;
                    }

                }
            }

            TreeMap<Float, Integer> tm = getByDistance();

            if(resetBlending) {
                for(ArticleInstance ai3 : cylinderSides) {
                    ai3.materials.get(0).set(blendingAttribute2);
                    ai3.materials.get(1).set(blendingAttribute4);
                    ai3.materials.get(2).set(blendingAttribute4);
                }
                resetBlending = false;
            }


            ArticleInstance closest = cylinderSides.get((Integer) tm.values().toArray()[0]);

            if (incenter) {
                int objLoc = getObject2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
                if(objLoc > 0) {
                    closest = cylinderSides.get(objLoc);
                }
            }

            if (closest != null && tapable) {
                float visCol = closest.col;
                List<ArticleInstance> rowEntries = cols.get(visCol);
                for (ArticleInstance ais : rowEntries) {
                    ais.materials.get(0).set(blendingAttribute);
                    ais.materials.get(1).set(blendingAttribute);
                    ais.materials.get(2).set(blendingAttribute);
                }
                if (prevCol.compareTo(visCol) != 0) {
                    rowEntries = cols.get(prevCol);
                    for (ArticleInstance ais : rowEntries) {
                        ais.materials.get(0).set(blendingAttribute2);
                        ais.materials.get(1).set(blendingAttribute4);
                        ais.materials.get(2).set(blendingAttribute4);
                    }
                    prevCol = visCol;
                }
            }


            for (Integer in : tm.descendingMap().values()) {
                modelBatchInternal.render(cylinderSides.get(in));
            }

            if (arrow != null) {
                modelBatchInternal.render(arrow);
            }

            modelBatchInternal.render(stop);
            modelBatchInternal.render(sbot);
            modelBatchInternal.end();
        }
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        modelBatch2.dispose();
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
            if (ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            if(tapable) {
                int index = getObject((int) Math.round(x), (int) Math.round(y));
                if (index > 0) {
                    Intent intent = new Intent(context, WebViewActivity.class);
                    ArticleInstance ai = cylinderSides.get(index);
                    if (ai != null && ai.arsEntity != null && ai.arsEntity.getLink() != null && !ai.arsEntity.getLink().isEmpty()) {
                        intent.putExtra(WebViewActivity.URL, cylinderSides.get(index).arsEntity.getLink());
                        if (ars3d.parent != null) {
                            ars3d.parent.startActivity(intent);
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            if (ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            if (ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        float panStartX, panStartY;

        boolean firstPan = true;

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (ars3d.flyMode) {
                return false;
            } else {
                if (firstPan) {
                    panStartX = x;
                    panStartY = y;
                    firstPan = false;
                }

                return true;
            }
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            if (ars3d.flyMode) {
                return false;
            } else {
                int startIndex = getObject((int) Math.round(panStartX), (int) Math.round(panStartY));
                if (startIndex > 0) {// && stopIndex > 0) {
                    ArticleInstance aeStart = cylinderSides.get(startIndex);
                    float deg = -0.4f;
                    if (panStartX < x) {
                        deg = 0.4f;
                    }

                    if(incenter) {
                        if(deg == 0.4f) {
                            deg = -0.4f;
                        } else {
                            deg = 0.4f;
                        }
                    }

                    final RotateRow rr = new RotateRow(ars3d, aeStart.row, deg, 100, 5l);
                    new Thread(rr).start();
                }
                firstPan = true;

                return true;

            }
        }

        @Override
        public boolean zoom(float originalDistance, float currentDistance) {
            if (ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean pinch(Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
            if (ars3d.flyMode) {
                return false;
            } else {
                return true;
            }
        }

        public void setSelected(int value) {
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

        public int getObject(int screenX, int screenY) {
            cam.update();
            Ray ray = cam.getPickRay(screenX, screenY);

            int result = -1;
            float distance = -1;

            for (int i = 0; i < cylinderSides.size; ++i) {
                final ArticleInstance instance = cylinderSides.get(i);

                instance.transform.getTranslation(position);
                position.add(instance.center);

                final float len = ray.direction.dot(position.x - ray.origin.x, position.y - ray.origin.y, position.z - ray.origin.z);
                if (len < 0f)
                    continue;

                float dist2 = position.dst2(ray.origin.x + ray.direction.x * len, ray.origin.y + ray.direction.y * len, ray.origin.z + ray.direction.z * len);
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

    public int getObject2(int screenX, int screenY) {
        cam.update();
        Ray ray = cam.getPickRay(screenX, screenY);

        int result = -1;
        float distance = -1;

        for (int i = 0; i < cylinderSides.size; ++i) {
            final ArticleInstance instance = cylinderSides.get(i);

            instance.transform.getTranslation(position);
            position.add(instance.center);

            final float len = ray.direction.dot(position.x - ray.origin.x, position.y - ray.origin.y, position.z - ray.origin.z);
            if (len < 0f)
                continue;

            float dist2 = position.dst2(ray.origin.x + ray.direction.x * len, ray.origin.y + ray.direction.y * len, ray.origin.z + ray.direction.z * len);
            if (distance >= 0f && dist2 > distance)
                continue;

            if (dist2 <= instance.radius * instance.radius) {
                result = i;
                distance = dist2;
            }
        }
        return result;
    }

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

        Log.e("TreeSet", "size is " + treeSet.size());

        synchronized (treeSet) {
            Iterator<ArsEntity> it = treeSet.iterator();
            List<ArticleInstance> row = rows.get(1.0f);

            if (it != null) {
                Log.e("TreeSet", "row 1 size is " + row.size());
                for (int i = 0; i < row.size() && it.hasNext(); i++) {
                    ArsEntity tmpEntity = it.next();
                    Log.w("Ars3dArs1", tmpEntity.toString());
                    ArticleInstance ai = row.get(i);
                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                        ai.arsEntity = tmpEntity;
                        textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        ai.update = true;
                        if (ai.arsEntity.getLocalImgPath() != null && !ai.arsEntity.getLocalImgPath().isEmpty()) {
                            AssetDescriptor ad = new AssetDescriptor(Gdx.files.absolute(ai.arsEntity.localImgPath), Texture.class);
                            if (ad.file.exists()) {
                                manager.load(ad);
                            } else {
                                ai.arsEntity.setLocalImgPath(null);
                            }
                        }
                    }
                }
                row = rows.get(0.0f);
                Log.e("TreeSet", "row 0 size is " + row.size());
                for (int i = 0; i < row.size() && it.hasNext(); i++) {
                    ArsEntity tmpEntity = it.next();
                    Log.w("Ars3dArs0", tmpEntity.toString());
                    ArticleInstance ai = row.get(i);
                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                        ai.arsEntity = tmpEntity;
                        textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        ai.update = true;
                        if (ai.arsEntity.getLocalImgPath() != null && !ai.arsEntity.getLocalImgPath().isEmpty()) {
                            AssetDescriptor ad = new AssetDescriptor(Gdx.files.absolute(ai.arsEntity.localImgPath), Texture.class);
                            if (ad.file.exists()) {
                                manager.load(ad);
                            } else {
                                ai.arsEntity.setLocalImgPath(null);
                            }
                        }
                    }
                }
                row = rows.get(2.0f);
                Log.e("TreeSet", "row 2 size is " + row.size());
                for (int i = 0; i < row.size() && it.hasNext(); i++) {
                    ArsEntity tmpEntity = it.next();
                    Log.w("Ars3dArs2", tmpEntity.toString());
                    ArticleInstance ai = row.get(i);
                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                        ai.arsEntity = tmpEntity;
                        textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        ai.update = true;
                        if (ai.arsEntity.getLocalImgPath() != null && !ai.arsEntity.getLocalImgPath().isEmpty()) {
                            AssetDescriptor ad = new AssetDescriptor(Gdx.files.absolute(ai.arsEntity.localImgPath), Texture.class);
                            if (ad.file.exists()) {
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
    }


    public static class ArticleInstance extends ModelInstance {
        public Vector3 center = new Vector3();
        public Vector3 dimensions = new Vector3();
        public float radius;
        public float row;
        public float col;

        public BoundingBox bounds = new BoundingBox();

        public ArsEntity arsEntity;
        public boolean update;

        public TextSpinner textSpinner;
        public Thread textSpinnerThread;
        public long textSleepTime;
        public long initialPause;

        public boolean flipped = false;

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

        public void startSpinner() {
            if (textSpinnerThread == null) {
                textSpinner = new TextSpinner(this, textSleepTime, initialPause);
                textSpinnerThread = new Thread(textSpinner);
                textSpinnerThread.start();
            }

        }

        public void stopSpinner() {
            if (textSpinner != null) {
                textSpinner.stopSpinner();
                textSpinnerThread = null;
                textSpinner = null;
            }
        }
    }

    public static class TextSpinner implements Runnable {

        public ArticleInstance ai;
        public static float[] all2 = new float[10000];
        public static float[] all2Back = new float[10000];
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
                MeshPart meshPartBack = ai.nodes.get(0).parts.get(2).meshPart;
                Mesh meshBack = meshPartBack.mesh;

                int cap2 = mesh.getVerticesBuffer().capacity();
                mesh.getVerticesBuffer().limit(cap2);
                meshBack.getVerticesBuffer().limit(cap2);

                synchronized (all2) {
                    mesh.getVertices(0, cap2, all2);
                    meshBack.getVertices(0, cap2, all2Back);

                    if(incenter) {
                        for (int i = 6; i < cap2; i += 8) {
                            float tmp = all2[i] - .005f;
                            if (tmp < 0.0) {
                                tmp = tmp + 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2[i] = tmp;

                            float tmp2 = all2Back[i] - .005f;
                            if (tmp2 < 0.0) {
                                tmp2 = tmp2 + 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2Back[i] = tmp2;
                        }
                    } else {
                        for (int i = 6; i < cap2; i += 8) {
                            float tmp = all2[i] + .005f;
                            if (tmp > 1.0) {
                                tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2[i] = tmp;

                            float tmp2 = all2Back[i] + .005f;
                            if (tmp2 > 1.0) {
                                tmp2 = tmp2 - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2Back[i] = tmp2;
                        }
                    }
                    mesh.setVertices(all2, 0, cap2);
                    meshBack.setVertices(all2Back, 0, cap2);
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

    private Pixmap textToTexture(String text) {
        BitmapFont.TextBounds tb = font.getBounds(text);

        int totalWidth = 52;
        for (char ch : text.toCharArray()) {
            BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
            if (ch != 32 && glyph != null) {
                totalWidth += (glyph.width);
            } else {
                totalWidth += 26;
            }
        }

        Pixmap pm2 = new Pixmap((totalWidth + 260), 143, pm.getFormat());

        int currentWidth = 52;
        for (char ch : text.toCharArray()) {
            BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
            if (ch != 32 && glyph != null) {
                pm2.drawPixmap(pm, glyph.srcX, glyph.srcY, glyph.width, glyph.height, currentWidth, Math.abs(glyph.height + glyph.yoffset), glyph.width, glyph.height);
                currentWidth += (glyph.width);
            } else {
                currentWidth += 26;
            }

            if (ch == 'f') {
                currentWidth -= 15;
            }
        }

        if (pm2.getWidth() > 4048) {
            Pixmap tmp = new Pixmap(4048, 143, pm.getFormat());
            tmp.drawPixmap(pm2, 0, 0, 4048, 143, 0, 0, 4048, 143);
            pm2 = tmp;
        }
        return pm2;
    }

    public Boolean getImageUpdates() {
        return imageUpdates;
    }

    public void setImageUpdates(Boolean imageUpdates) {
        this.imageUpdates = imageUpdates;
    }
}
