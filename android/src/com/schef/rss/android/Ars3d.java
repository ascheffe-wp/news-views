package com.schef.rss.android;


import android.content.Context;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

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
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayDeque;
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
public class Ars3d implements ApplicationListener /*, SurfaceTexture.OnFrameAvailableListener */ {

    private AndroidLauncher parent;

    public Environment environment;
    public PerspectiveCamera cam;
    public CameraInputController camController;
    public ModelBatch modelBatch;
    public ModelBatch modelBatch2;
    public SpriteBatch spriteBatch;
    public Boolean flyMode = true;

    public Boolean imageUpdates = false;

    private BlendingAttribute blendingAttribute;
    private BlendingAttribute blendingAttribute2;
    private BlendingAttribute blendingAttribute3;
    private BlendingAttribute blendingAttribute4;
    private BlendingAttribute blendingAttribute5;

    public Array<ArticleInstance> cylinderSides = new Array<ArticleInstance>();

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

    private AssetManager manager;

    private Float startPosition;
    private Float startRotation;

    private List<SurfaceBean> sbBeans = new ArrayList<SurfaceBean>();

    public static volatile boolean textureHeadlines = true;

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
        manager.load(new AssetDescriptor(Gdx.files.internal("earthmoon.jpg"), Texture.class));
        manager.load(new AssetDescriptor(Gdx.files.internal("tbsign1.png"), Texture.class));
        manager.update();
        manager.finishLoading();


        blendingAttribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f);
        blendingAttribute2 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.6f);
        blendingAttribute3 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.2f);
        blendingAttribute4 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.15f);
        blendingAttribute5 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.01f);

        ArsShaderProvider asp = new ArsShaderProvider(Gdx.files.internal("default.vertex3.glsl"),Gdx.files.internal("default.fragment3.glsl"));
        asp = new ArsShaderProvider();

        modelBatch = new ModelBatch(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 4, 4)),asp,null);

        ArsShaderProvider asp2 = new ArsShaderProvider(Gdx.files.internal("default.vertex.glsl"),Gdx.files.internal("default.fragment.glsl"),
                Gdx.files.internal("default.vertex.glsl"),Gdx.files.internal("default.fragment2.glsl"));

        modelBatch2 = new ModelBatch(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 4, 4)),
                asp2,null);

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

        Texture textTexture = new Texture(Gdx.files.internal("txt3.png"), true); // true enables mipmaps
        textTexture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear); // linear filtering in nearest mipmap image

        font = new BitmapFont(Gdx.files.internal("txt3.fnt"), new TextureRegion(textTexture), false);
        pm = new Pixmap(Gdx.files.internal("txt3.png"));
        FileHandle fh = Gdx.files.internal("objs2.json");

        ImagePane[] ips = gson.fromJson(fh.reader(), ImagePane[].class);
        for (ImagePane ip : ips) {
            if (count % 2 == 0) {
                mb.begin();
                MeshPartBuilder mpb = mb.part("cylinderA" + count, GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                        new Material(ColorAttribute.createDiffuse(Color.WHITE), /*TextureAttribute.createDiffuse(img5),*/ blendingAttribute2));
                mpb.sphere(6f, 6f, 6f, 120, 120, ip.uFrom, ip.uTo, ip.vFrom, ip.vTo);

                if(textureHeadlines) {
                    MeshBuilder meshBuilder = new MeshBuilder();
                    meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                    meshBuilder.sphere2(6.015f, 6.015f, 6.015f, 200, 2, ip.uFrom, ip.uTo, ip.vTo, ip.vTo+2, 0.9f, 1.0f);
                    Mesh mesh1 = meshBuilder.end();
                    mb.part("cylinderB", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute));
                    meshBuilder = new MeshBuilder();
                    meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                    meshBuilder.sphere2(5.985f, 5.985f, 5.985f, 200, 2, ip.uFrom, ip.uTo, ip.vTo, ip.vTo+2, 0.9f, 1.0f);
                    mesh1 = meshBuilder.end();
                    mb.part("cylinderC", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute));
                } else {
                    GlyphUnit gu = textToTexture2("This is a test. Just a test no need to get worried or anything");
                    float pixelToTextSize = 38f / (float) gu.totalWidth;
                    float pixelToTextHeight = 4f / 143f;
                    float degreeOffset = ip.uFrom;
                    int count = 0;
                    for (BitmapFont.Glyph glyph : gu.glyphs) {
                        MeshBuilder meshBuilder = new MeshBuilder();
                        float degreeToEndOffset = (glyph.width * pixelToTextSize) + degreeOffset;
                        TextureRegion tr = new TextureRegion(textTexture, glyph.srcX, glyph.srcY, glyph.width, glyph.height);
                        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                        meshBuilder.sphere(6.115f, 6.115f, 6.115f, 1, 1, degreeOffset, degreeToEndOffset, ip.vTo + (Math.abs(glyph.height + glyph.yoffset) * pixelToTextHeight), ip.vTo + ((Math.abs(glyph.height + glyph.yoffset) + glyph.height) * pixelToTextHeight));
                        Mesh mesh1 = meshBuilder.end();
                        mb.part("text" + count, mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute, TextureAttribute.createDiffuse(tr)));
                        degreeOffset = (glyph.xadvance * pixelToTextSize) + degreeOffset;
                    }
                }

                model = mb.end();
            } else {
                mb.begin();
                MeshPartBuilder mpb = mb.part("cylinderA" + count, GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
                        new Material(ColorAttribute.createDiffuse(Color.WHITE), /*TextureAttribute.createDiffuse(img4),*/ blendingAttribute2));
                mpb.sphere(6f, 6f, 6f, 120, 120, ip.uFrom, ip.uTo, ip.vFrom, ip.vTo);
                    if(textureHeadlines) {
                        MeshBuilder meshBuilder = new MeshBuilder();
                        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                        meshBuilder.sphere2(6.015f, 6.015f, 6.015f, 200, 2, ip.uFrom, ip.uTo, ip.vTo, ip.vTo+2, 0.9f, 1.0f);
                        Mesh mesh1 = meshBuilder.end();
                        mb.part("cylinderB", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute));
                        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                        meshBuilder.sphere2(5.985f, 5.985f, 5.985f, 200, 2, ip.uFrom, ip.uTo, ip.vTo, ip.vTo+2, 0.9f, 1.0f);
                        mesh1 = meshBuilder.end();
                        mb.part("cylinderC", mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute));
                    } else {
                        GlyphUnit gu = textToTexture2("This is a test. Just a test no need to get worried or anything");
                        float pixelToTextSize = 38f / (float) gu.totalWidth;
                        float pixelToTextHeight = 4f / 143f;
                        float degreeOffset = ip.uFrom;
                        int count = 0;
                        for (BitmapFont.Glyph glyph : gu.glyphs) {
                            MeshBuilder meshBuilder = new MeshBuilder();
                            float degreeToEndOffset = (glyph.width * pixelToTextSize) + degreeOffset;
                            TextureRegion tr = new TextureRegion(textTexture, glyph.srcX, glyph.srcY, glyph.width, glyph.height);
                            meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                            meshBuilder.sphere(6.115f, 6.115f, 6.115f, 1, 1, degreeOffset, degreeToEndOffset, ip.vTo + (Math.abs(glyph.height + glyph.yoffset) * pixelToTextHeight), ip.vTo + ((Math.abs(glyph.height + glyph.yoffset) + glyph.height) * pixelToTextHeight));
                            Mesh mesh1 = meshBuilder.end();
                            mb.part("text" + count, mesh1, GL20.GL_TRIANGLES, new Material(blendingAttribute, TextureAttribute.createDiffuse(tr)));
                            degreeOffset = (glyph.xadvance * pixelToTextSize) + degreeOffset;
                        }
                    }

                model = mb.end();
            }
            instance2 = new ArticleInstance(model, ip.center, ip.dimensions, ip.radius, ip.bounds, ip.row, ip.col, 30l, count * 400);
            if(textureHeadlines) {
                instance2.textureHeadline = textureHeadlines;
                instance2.startSpinner();
            }
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


//        videoTexture = new Texture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, new VideoTextureData());
//
//        surfaceTexture = new SurfaceTexture(videoTexture.getGlHandle2());
//        surfaceTexture.setOnFrameAvailableListener(this);
//
//        parent.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ((TextureView) parent.findViewById(R.id.surface)).setVisibility(View.VISIBLE);
//            }
//        });

    }

//    public SurfaceTexture surfaceTexture;
//    public boolean frameAvailable;

//    public Texture videoTexture;

    float[] all2 = new float[10000];
    private Pixmap pm;

    int count = 0;

    public static boolean incenter = true;
    public static volatile boolean resetBlending = true;
    public Float prevCol = 8f;

    ModelBatch modelBatchInternal = null;
    float visCol;
    @Override
    public void render() {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        for (SurfaceBean sb : sbBeans) {
            sb.updateTextures();
        }

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


        float distToCenter = cam.position.dst2(0f, 0f, 0f);
        if (distToCenter < 9.0f && !incenter) {
            incenter = true;
            modelBatchInternal = modelBatch2;
        } else if (distToCenter > 9.0f && incenter) {
            incenter = false;
            modelBatchInternal = modelBatch;
        }

        spriteBatch.begin();
        spriteBatch.draw(manager.get(Gdx.files.internal("earthmoon.jpg").path(), Texture.class), 0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.end();

        if(modelBatchInternal != null) {
            modelBatchInternal.begin(cam);

            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);

            updateAllRows();

            TreeMap<Float, Integer> tm = getByDistance();

            if(resetBlending) {
                for(ArticleInstance ai3 : cylinderSides) {
                    ai3.materials.get(0).set(blendingAttribute2);
                    if(ai3.textureHeadline) {
                        ai3.materials.get(1).set(blendingAttribute4);
                        ai3.materials.get(2).set(blendingAttribute4);
                    }
                }
            }


            ArticleInstance closest = cylinderSides.get((Integer) tm.values().toArray()[0]);

            if (incenter) {
                int objLoc = getObject2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
                if(objLoc > 0) {
                    closest = cylinderSides.get(objLoc);
                }
            }

            if (closest != null && tapable) {
                visCol = closest.col;
                if(prevCol.compareTo(visCol) != 0 || resetBlending) {
                    List<ArticleInstance> rowEntries = cols.get(visCol);
                    for (ArticleInstance ais : rowEntries) {
                        ais.materials.get(0).set(blendingAttribute);
                        if (ais.textureHeadline) {
                            ais.materials.get(1).set(blendingAttribute);
                            ais.materials.get(2).set(blendingAttribute);
                        }
                    }
                    resetBlending = false;
                }
                if (prevCol.compareTo(visCol) != 0) {
                    List<ArticleInstance> rowEntries = cols.get(visCol);
                    rowEntries = cols.get(prevCol);
                    for (ArticleInstance ais : rowEntries) {
                        ais.materials.get(0).set(blendingAttribute2);
                        if(ais.textureHeadline) {
                            ais.materials.get(1).set(blendingAttribute4);
                            ais.materials.get(2).set(blendingAttribute4);
                        }
                    }
                    prevCol = visCol;
                    resetBlending = false;
                }
            }


            for (Integer in : tm.descendingMap().values()) {
                ArticleInstance ai = cylinderSides.get(in);
                    modelBatchInternal.render(ai);
                if(ai.textModel != null) {
                    modelBatchInternal.render(ai.textModel);
                }
            }

            if (arrow != null) {
                modelBatchInternal.render(arrow);
            }

            modelBatchInternal.render(stop);
            modelBatchInternal.render(sbot);
            modelBatchInternal.end();
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

//    @Override
//    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        synchronized (this)
//        {
//            frameAvailable = true;
//        }
//    }




    public void startAllGlobeRotation() {
        final RotateAll rr = new RotateAll(this, cylinderSides, 0.1f, 20l);
        new Thread(rr).start();
    }



    @Override
    public void dispose() {
        modelBatch.dispose();
        modelBatch2.dispose();
        spriteBatch.dispose();
        for (ArticleInstance ai : cylinderSides) {
            ai.model.dispose();
            if(ai.textModel != null) {
                ai.textModel.model.dispose();
            }
        }
        for(Texture text : textureMap2.values()) {
            text.dispose();
        }
        manager.dispose();
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

    private volatile ArticleInstance aiToVid = null;

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
                    final ArticleInstance ai = cylinderSides.get(index);
                    if (ai != null && ai.arsEntity != null && ai.arsEntity.getLink() != null && !ai.arsEntity.getLink().isEmpty()) {

                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.URL, cylinderSides.get(index).arsEntity.getLink());
                        intent.setData(Uri.parse(ai.arsEntity.getLink()));
                        if (ars3d.parent != null) {
                            ars3d.parent.startActivity(intent);
                        }

//                        aiToVid = ai;
//                        SurfaceBean sb = new SurfaceBean();
//
////                        Texture vt = new Texture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, new VideoTextureData());
//                        Texture vt = videoTexture;
//
//                        SurfaceTexture st = new SurfaceTexture(vt.getGlHandle2());
//                        surfaceTexture = st;
////                        sb.setSt(st);
//                        st.setOnFrameAvailableListener(ars3d);
//                        final Surface surface = new Surface(st);
//
//
//
//                        ///////
//                        final RelativeLayout rl = (RelativeLayout) parent.findViewById(R.id.drawer_layout);
//                        TextureView tv = new TextureView(parent);
//
//                        int curTextViewId = prevTextViewId + 1;
//                        tv.setId(curTextViewId);
//                        final RelativeLayout.LayoutParams params =
//                                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
//                                        RelativeLayout.LayoutParams.MATCH_PARENT);
//
//                        params.addRule(RelativeLayout.BELOW, prevTextViewId);
//                        tv.setLayoutParams(params);
//                        tv.setVisibility(View.VISIBLE);
//
//                        prevTextViewId = curTextViewId;
//
//
//                        AssetFileDescriptor afd2 = null;
//                        try {
//                            afd2 = parent.getAssets().openFd("big_buck_bunny.mp4");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        final AssetFileDescriptor afd = afd2;
//                        tv.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//
//                            MediaPlayer player;
//                            @Override
//                            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                                startPlaying();
//                            }
//
//                            @Override
//                            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//                            }
//
//                            @Override
//                            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                                return false;
//                            }
//
//                            @Override
//                            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//                            }
//
//                            private void startPlaying()
//                            {
//                                player = new MediaPlayer();
//
//                                try {
//
//                                    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
////                                            player.setDataSource("http://assets.ign.com/videos/zencoder/1920/68d0fff357ad0b6f1be11bcb6f9f4c37-5000000-1417041042-w.mp4");
//                                    player.setSurface(surface);
//                                    player.setLooping(true);
//                                    player.prepare();
//                                    player.start();
//
//                                } catch (IOException e) {
//                                    throw new RuntimeException("Could not open input video!");
//                                }
//                            }
//                        });
//                        //////
//
//
//                        final TextureView tv2 = tv;
//                        parent.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                rl.addView(tv2, params);
//                            }
//                        });
//                        RunVideoSetup rvs = new RunVideoSetup(st,parent, surface);
//                        parent.runOnUiThread(rvs);


//                        ai.materials.first().remove(TextureAttribute.Diffuse);
//                        ai.materials.first().set(TextureAttribute.createVideo(videoTexture));


//                        sbBeans.add(sb);



//                        TextureAttribute ta = (TextureAttribute)ai.materials.get(0).get(TextureAttribute.Diffuse);
//                        ta.textureDescription.textureUnitToUse = textures.get(0);
//                        ai.video = true;
//                        camController.update();
//                        cam.update();
//                        ai.calculateTransforms();
//                        BoundingBox bb = ai.calculateBoundingBox(ai.bounds);
//                        Vector3 vec3 = cam.project(ai.bounds.getCorners());

//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                BoundingBox bb = new BoundingBox();
//                                ai.model.nodes.get(0).calculateBoundingBox(bb);
//                                Vector3 [] vertex3s = bb.getCorners();
//
//                                Vector3 [] newVertex3s = new Vector3[8];
//                                int i = 0;
//                                for(Vector3 vec3 : vertex3s) {
//                                    camController.update();
//                                    cam.update();
//
//                                    vec3.prj(cam.combined);
//                                    vec3.x = Gdx.graphics.getWidth() * (vec3.x + 1) / 2;
//                                    vec3.y = Gdx.graphics.getHeight() * (vec3.y + 1) / 2;
//                                    vec3.z = (vec3.z + 1);
//                                    newVertex3s[i++] = vec3;
//                                    Log.e("VEC", "[" + Gdx.graphics.getWidth() + "," + Gdx.graphics.getHeight() + "] world " + vec3.toString());
//                                }
//                                final float x = /*Gdx.graphics.getWidth() - */newVertex3s[1].x;
//                                final float y = /*Gdx.graphics.getHeight() - */ newVertex3s[1].y;
//                                final float width = newVertex3s[0].x - newVertex3s[1].x;
//                                final float height = newVertex3s[2].y - newVertex3s[1].y;
//
//                                ars3d.parent.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams((int)width,(int)height);
////                                        AbsoluteLayout.LayoutParams allp = new AbsoluteLayout.LayoutParams((int)width,(int)height,(int)x,(int)y);
//
//                                        rllp.leftMargin = (int)x;
//                                        rllp.topMargin = (int)y;
//                                        ImageView imgv = (ImageView)ars3d.parent.findViewById(R.id.tmpImg);
//                                        imgv.setLayoutParams(rllp);
//                                        imgv.setVisibility(View.VISIBLE);
//                                        imgv.invalidate();
//                                    }
//                                });
//
//                            }
//                        }).run();


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

                    final RotateRow rr = new RotateRow(ars3d, aeStart, deg, 100, 5l);
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

    static int prevTextViewId = 9999;

    public static class RunVideoSetup implements Runnable {

        private SurfaceTexture st;
        private AndroidLauncher parent;
        private Surface surface;


        public RunVideoSetup(SurfaceTexture st, AndroidLauncher parent, Surface surface) {
            this.st = st;
            this.parent = parent;
            this.surface = surface;
        }

        @Override
        public void run() {
            RelativeLayout rl = (RelativeLayout) parent.findViewById(R.id.drawer_layout);
            TextureView tv = new TextureView(parent);

            int curTextViewId = prevTextViewId + 1;
            tv.setId(curTextViewId);
            final RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);

            params.addRule(RelativeLayout.BELOW, prevTextViewId);
            tv.setLayoutParams(params);
            tv.setVisibility(View.VISIBLE);

            prevTextViewId = curTextViewId;


            AssetFileDescriptor afd2 = null;
            try {
                afd2 = parent.getAssets().openFd("big_buck_bunny.mp4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            final AssetFileDescriptor afd = afd2;
            tv.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

                MediaPlayer player;
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    startPlaying();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }

                private void startPlaying()
                {
                    player = new MediaPlayer();

                    try {

                        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                                            player.setDataSource("http://assets.ign.com/videos/zencoder/1920/68d0fff357ad0b6f1be11bcb6f9f4c37-5000000-1417041042-w.mp4");
                        player.setSurface(surface);
                        player.setLooping(true);
                        player.prepare();
                        player.start();

                    } catch (IOException e) {
                        throw new RuntimeException("Could not open input video!");
                    }
                }
            });
            rl.addView(tv, params);


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
//                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                    if(tmpEntity != null) {
                        ai.arsEntity = tmpEntity;
                        if(textureHeadlines) {
                            textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        }
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
//                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                    if(tmpEntity != null) {
                        ai.arsEntity = tmpEntity;
                        if(textureHeadlines) {
                            textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        }
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
//                    if (ai.arsEntity == null || ai.arsEntity.getLink() == null || ai.arsEntity.getLink().equalsIgnoreCase(tmpEntity.getLink())) {
                    if(tmpEntity != null) {
                        ai.arsEntity = tmpEntity;
                        if(textureHeadlines) {
                            textureMap2.put(ai.arsEntity.getTitle(), new Texture(textToTexture(ai.arsEntity.getTitle())));
                        }
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
                currentWidth += (glyph.xadvance);
            } else {
                currentWidth += 26;
            }
        }

        if (pm2.getWidth() > 4048) {
            Pixmap tmp = new Pixmap(4048, 143, pm.getFormat());
            tmp.drawPixmap(pm2, 0, 0, 4048, 143, 0, 0, 4048, 143);
            pm2 = tmp;
        }
        return pm2;
    }

    public static class GlyphUnit {
        public ArrayDeque<BitmapFont.Glyph> glyphs = new ArrayDeque<BitmapFont.Glyph>();
        public int totalWidth = 0;

    }


    private GlyphUnit textToTexture2(String text) {
        BitmapFont.TextBounds tb = font.getBounds(text);

        GlyphUnit glyphUnit = new GlyphUnit();

        int totalWidth = 0;
            for (char ch : text.toCharArray()) {
                BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
                glyphUnit.glyphs.addFirst(glyph);
                if (ch != 32 && glyph != null) {
                    totalWidth += (glyph.width);
                } else {
                    totalWidth += 26;
                }
        }

        glyphUnit.totalWidth = totalWidth;
        return glyphUnit;
    }

    public Boolean getImageUpdates() {
        return imageUpdates;
    }

    public void setImageUpdates(Boolean imageUpdates) {
        this.imageUpdates = imageUpdates;
    }

    public void updateAllRows () {
        List<ArticleInstance> row1 = rows.get(1.0f);
        for (ArticleInstance ai : row1) {
            updateImage(ai);
        }

        List<ArticleInstance> row2 = rows.get(2.0f);
        for (ArticleInstance ai : row2) {
            updateImage(ai);
        }

        List<ArticleInstance> row0 = rows.get(0.0f);
        for (ArticleInstance ai : row0) {
            updateImage(ai);
        }
    }

    public void updateImage (ArticleInstance ai) {
        if (ai.update) {
            Material material = ai.materials.get(0);
            if (ai.arsEntity.localImgPath != null) {
                if (manager.isLoaded(Gdx.files.absolute(ai.arsEntity.localImgPath).path())) {
                    material.set(TextureAttribute.createDiffuse(manager.get(new AssetDescriptor<Texture>(Gdx.files.absolute(ai.arsEntity.localImgPath).path(), Texture.class))));
                    if(ai.textureHeadline) {
                        material = ai.materials.get(1);
                        Material material2 = ai.materials.get(2);
                        if (ai.arsEntity.title != null) {
                            Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                            if (tmpTexture2 != null /*&& ai.col == visCol*/) {
                                if(ai.col == visCol) {
                                    material.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute);
                                    material.id = "st";
                                    material2.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute);
                                    material2.id = "st";
                                } else {
                                    material.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute4);
                                    material.id = "st";
                                    material2.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute4);
                                    material2.id = "st";
                                }
                            }
                        }
                    }
                    ai.update = false;
                }
            } else {
                material.set(TextureAttribute.createDiffuse(manager.get(Gdx.files.internal("tbsign1.png").path(), Texture.class)));
                material = ai.materials.get(1);
                Material material2 = ai.materials.get(2);
                if (ai.arsEntity.title != null) {
                    Texture tmpTexture2 = textureMap2.get(ai.arsEntity.title);
                    if (tmpTexture2 != null /*&& ai.col == visCol && ai.textureHeadline*/) {
                        if(ai.col == visCol) {
                            material.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute);
                            material.id = "st";
                            material2.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute);
                            material2.id = "st";
                        } else {
                            material.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute4);
                            material.id = "st";
                            material2.set(TextureAttribute.createDiffuse(tmpTexture2),blendingAttribute4);
                            material2.id = "st";
                        }
                    }
                }
                ai.update = false;
            }

        }
    }

    public static class SurfaceBean implements SurfaceTexture.OnFrameAvailableListener {
        public SurfaceTexture st;
        public boolean frameAvailable;

        public void setSt (SurfaceTexture st) {
            this.st = st;
            this.st.setOnFrameAvailableListener(this);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            synchronized (this)
            {
                frameAvailable = true;
            }
        }

        public void updateTextures () {
            synchronized (this)
            {
                if (frameAvailable)
                {
                    st.updateTexImage();
//                surfaceTexture.getTransformMatrix(videoTextureTransform);
                    frameAvailable = false;
                }

            }
        }
    }
}
