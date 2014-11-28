package com.schef.rss.android;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.io.Serializable;

/**
 * Created by scheffela on 11/28/14.
 */
public class ImagePane implements Serializable {
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
