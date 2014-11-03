package com.schef.rss.android;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by scheffela on 9/20/14.
 */
public class ArsShaderProvider extends DefaultShaderProvider {

    public DefaultShader.Config textConfig;

    public ArsShaderProvider() {
    }

    public ArsShaderProvider(DefaultShader.Config config) {
        super(config);
    }

    public ArsShaderProvider(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public ArsShaderProvider(FileHandle vertexShader, FileHandle fragmentShader) {
        super();
        textConfig = new DefaultShader.Config(vertexShader.readString(), fragmentShader.readString());
    }

    public ArsShaderProvider(FileHandle vertexShader, FileHandle fragmentShader, FileHandle textVertexShader, FileHandle textFragmentShader) {
        super(vertexShader, fragmentShader);
        textConfig = new DefaultShader.Config(textVertexShader.readString(), textFragmentShader.readString());
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        if(textConfig != null && renderable.material.id.equalsIgnoreCase("st")) {
            return new DefaultShader(renderable, textConfig);
        }
        return super.createShader(renderable);
    }

    public DefaultShader.Config getTextConfig() {
        return textConfig;
    }

    public void setTextConfig(DefaultShader.Config textConfig) {
        this.textConfig = textConfig;
    }
}
