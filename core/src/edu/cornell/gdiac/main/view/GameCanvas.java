package edu.cornell.gdiac.main.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;

import javax.swing.*;
import javax.xml.soap.Text;

public class GameCanvas {
    /** Drawing context to handle textures as sprites */
    private SpriteBatch spriteBatch;
    /** Drawing context to handle textures as sprites */
    private ShapeRenderer shapeRenderer;
    /** Value to cache window width (if we are currently full screen) */
    private int width;
    /** Value to cache window height (if we are currently full screen) */
    private int height;
    /** line drawer */
    private ShapeRenderer lineDrawer;

    /** Track whether or not we are active (for error checking) */
    private boolean active;

    // CACHE OBJECTS
    /** Affine cache for current sprite to draw */
    private Affine2 local;
    /** Affine cache for all sprites this drawing pass */
    private Affine2 global;
    /** Cache object to unify everything under a master draw method */
    private TextureRegion holder;

    /**
     * Creates a new GameCanvas determined by the application configuration.
     *
     * Width, height, and fullscreen are taken from the LWGJApplicationConfig
     * object used to start the application.
     */
    public GameCanvas() {
        this.width  = Gdx.graphics.getWidth();
        this.height = Gdx.graphics.getHeight();
        lineDrawer = new ShapeRenderer();

        active = false;
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Set the projection matrix (for proper scaling)
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());

        // Initialize the cache objects
        holder = new TextureRegion();
        local  = new Affine2();
        global = new Affine2();
    }

    /**
     * Creates a new GameCanvas with the giving parameters.
     *
     * This constructor will completely override the settings in the
     * LWGJApplicationConfig object used to start the application.
     *
     * @param width 		The width of the canvas window
     * @param height 		The height of the canvas window
     * @param fullscreen 	Whether or not the window should be full screen.
     */
    protected GameCanvas(int width, int height, boolean fullscreen) {
        // Create a new graphics manager.
        this.width  = width;
        this.height = height;
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(width, height);
        }

        // Continue as normal
        active = false;
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Set the projection matrix (for proper scaling)
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());

        // Initialize the cache objects
        holder = new TextureRegion();
        local  = new Affine2();
        global = new Affine2();
    }

    /**
     * Creates a new GameCanvas of the given size.
     *
     * The canvas will be displayed in a window, not fullscreen.
     *
     * @param width 	The width of the canvas window
     * @param height 	The height of the canvas window
     */
    public GameCanvas(int width, int height) {
        this(width,height,false);
    }
    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
            return;
        }
    }
    /**
     * Returns the width of this canvas
     *
     * This currently gets its value from Gdx.graphics.getWidth()
     *
     * @return the width of this canvas
     */
    public int getWidth() {
        return Gdx.graphics.getWidth();
    }

    /**
     * Changes the width of this canvas
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * This method has no effect if the resolution is full screen.  In that case, the
     * resolution was fixed at application startup.  However, the value is cached, should
     * we later switch to windowed mode.
     *
     * @param width the canvas width
     */
    public void setWidth(int width) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.width = width;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(width, getHeight());
        }
        resize();
    }

    /**
     * Returns the height of this canvas
     *
     * This currently gets its value from Gdx.graphics.getHeight()
     *
     * @return the height of this canvas
     */
    public int getHeight() {
        return Gdx.graphics.getHeight();
    }

    /**
     * Changes the height of this canvas
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * This method has no effect if the resolution is full screen.  In that case, the
     * resolution was fixed at application startup.  However, the value is cached, should
     * we later switch to windowed mode.
     *
     * @param height the canvas height
     */
    public void setHeight(int height) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.height = height;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(getWidth(), height);
        }
        resize();
    }

    /**
     * Returns the dimensions of this canvas
     *
     * @return the dimensions of this canvas
     */
    public Vector2 getSize() {
        return new Vector2(width,height);
    }

    /**
     * Changes the width and height of this canvas
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * This method has no effect if the resolution is full screen.  In that case, the
     * resolution was fixed at application startup.  However, the value is cached, should
     * we later switch to windowed mode.
     *
     * @param width the canvas width
     * @param height the canvas height
     */
    public void setSize(int width, int height) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        this.width = width;
        this.height = height;
        if (!isFullscreen()) {
            Gdx.graphics.setWindowedMode(width, height);
        }
        resize();
    }

    /**
     * Returns whether this canvas is currently fullscreen.
     *
     * @return whether this canvas is currently fullscreen.
     */
    public boolean isFullscreen() {
        return Gdx.graphics.isFullscreen();
    }
    /**
     * Sets whether or not this canvas should change to fullscreen.
     *
     * Changing to fullscreen will use the resolution of the application at startup.
     * It will NOT use the dimension settings of this canvas (which are for window
     * display only).
     *
     * This method raises an IllegalStateException if called while drawing is
     * active (e.g. in-between a begin-end pair).
     *
     * @param value		Whether this canvas should change to fullscreen.
     */
    public void setFullscreen(boolean value) {
        if (active) {
            Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
            return;
        }
        if (value) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(width, height);
        }
    }

    /**
     * Resets the SpriteBatch camera when this canvas is resized.
     *
     * If you do not call this when the window is resized, you will get
     * weird scaling issues.
     */
    public void resize() {
        // Resizing screws up the spriteBatch projection matrix
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
    }
    /**
     * Start and active drawing sequence with the identity transform.
     *
     * Nothing is flushed to the graphics card until the method end() is called.
     */
    public void begin() {
        spriteBatch.begin();
        active = true;
    }

    /**
     * Ends a drawing sequence, flushing textures to the graphics card.
     */
    public void end() {
        spriteBatch.end();
        active = false;
    }

    /**
     * Draws the texture at the given position.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     */
    public void draw(Texture image, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        // Call the master drawing method
        holder.setRegion(image);
        draw(image, Color.WHITE,0,0,x,y,0,1.0f,1.0f);
    }
    /**
     * Draws the tinted texture at the given position.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     */
    public void draw(Texture image, Color tint, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        // Call the master drawing method
        holder.setRegion(image);
        draw(image,tint,0,0,x,y,0,1.0f,1.0f);
    }

    /**
     * Draws the tinted texture with the given transformations
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the screen location
     * @param y 	The y-coordinate of the screen location
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx 	The x-axis scaling factor
     * @param sy 	The y-axis scaling factor
     */
    public void draw(Texture image, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        // Call the master drawing method
        holder.setRegion(image);
        draw(holder,tint,ox,oy,x,y,angle,sx,sy);
    }

    /**
     * Draws the tinted texture with the given transformation
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformation is applied BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * @param image The texture to draw
     * @param tint  The color tint
     * @param trans The coordinate space transform
     */
    public void draw(Texture image, Color tint, Affine2 trans) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        // Call the master drawing method
        holder.setRegion(image);
        draw(holder,tint,trans);
    }

    /**
     * Draws the tinted texture region (filmstrip) with the given transformation
     *
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformation is applied BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * @param region	The texture to draw
     * @param tint  	The color tint
     * @param trans 	The coordinate space transform
     */
    public void draw(TextureRegion region, Color tint, Affine2 trans) {
        // THIS METHOD SHOULD ONLY BE USED BY THE LOADING SCREEN
        // It is unattached to the master draw method
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        local.set(global);
        local.mul(trans);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
    }

    /**
     * Draws the texture region (filmstrip) at the given position.
     *
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param region	The texture to draw
     * @param x 		The x-coordinate of the bottom left corner
     * @param y 		The y-coordinate of the bottom left corner
     */
    public void draw(TextureRegion region, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        // Call the master drawing method
        draw(region,Color.WHITE,0,0,x,y,0,1.0f,1.0f);
    }

    /**
     * Draws the tinted texture region (filmstrip) at the given position.
     *
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * Unless otherwise transformed by the global transform (@see begin(Affine2)),
     * the texture will be unscaled.  The bottom left of the texture will be positioned
     * at the given coordinates.
     *
     * @param region	The texture to draw
     * @param tint  	The color tint
     * @param x 		The x-coordinate of the bottom left corner
     * @param y 		The y-coordinate of the bottom left corner
     */
    public void draw(TextureRegion region, Color tint, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        // Call the master drawing method
        draw(region,tint,0,0,x,y,0,1.0f,1.0f);
    }

    /**
     * Draws the tinted texture region (filmstrip) with the given transformations
     *
     * A texture region is a single texture file that can hold one or more textures.
     * It is used for filmstrip animation.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * The transformations are BEFORE after the global transform (@see begin(Affine2)).
     * As a result, the specified texture origin will be applied to all transforms
     * (both the local and global).
     *
     * The local transformations in this method are applied in the following order:
     * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
     *
     * @param region	The texture to draw
     * @param tint  	The color tint
     * @param ox 		The x-coordinate of texture origin (in pixels)
     * @param oy 		The y-coordinate of texture origin (in pixels)
     * @param x 		The x-coordinate of the texture origin
     * @param y 		The y-coordinate of the texture origin
     * @param angle 	The rotation angle (in degrees) about the origin.
     * @param sx 		The x-axis scaling factor
     * @param sy 		The y-axis scaling factor
     */
    public void draw(TextureRegion region, Color tint, float ox, float oy,
                     float x, float y, float angle, float sx, float sy) {
        //  THIS IS THE MASTER DRAW METHOD
        // This is the method that you should alter to implement clipping.
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        computeTransform(ox,oy,x,y,angle,sx,sy);
        spriteBatch.setColor(tint);
        spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
    }

    public void draw(Texture region, Color tint, float x, float y, float width, float height){
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        spriteBatch.setColor(tint);
        spriteBatch.draw(region,x,y,width,height);
    }
    /**
     * Compute the affine transform (and store it in local) for this image.
     *
     * This helper is meant to simplify all of the math in the above draw method
     * so that you do not need to worry about it when working on Exercise 4.
     *
     * @param ox 	The x-coordinate of texture origin (in pixels)
     * @param oy 	The y-coordinate of texture origin (in pixels)
     * @param x 	The x-coordinate of the texture origin
     * @param y 	The y-coordinate of the texture origin
     * @param angle The rotation angle (in degrees) about the origin.
     * @param sx 	The x-axis scaling factor
     * @param sy 	The y-axis scaling factor
     */
    private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
        local.set(global);
        // Post multiplication means we read this in reverse order
        local.translate(x,y);
        local.rotate(angle);
        local.scale(sx,sy);
        local.translate(-ox,-oy);
    }

    /**
     * Draw an unscaled overlay image.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     *
     * @param image Texture to draw as an overlay
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     */
    public void drawOverlay(Texture image, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        drawOverlay(image,Color.WHITE,x,y);
    }

    /**
     * Draw an unscaled overlay image tinted by the given color.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * @param image Texture to draw as an overlay
     * @param tint  The color tint
     * @param x 	The x-coordinate of the bottom left corner
     * @param y 	The y-coordinate of the bottom left corner
     */
    public void drawOverlay(Texture image, Color tint, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        spriteBatch.setColor(tint);
        spriteBatch.draw(image, x, y);

    }

    /**
     * Draw an stretched overlay image.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     *
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     *
     * @param image Texture to draw as an overlay
     * @param fill	Whether to stretch the image to fill the screen
     */
    public void drawOverlay(Texture image, boolean fill) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        drawOverlay(image,Color.WHITE,fill);
    }

    /**
     * Draw an stretched overlay image tinted by the given color.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     *
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     *
     * The texture colors will be multiplied by the given color.  This will turn
     * any white into the given color.  Other colors will be similarly affected.
     *
     * @param image Texture to draw as an overlay
     * @param tint  The color tint
     * @param fill	Whether to stretch the image to fill the screen
     */
    public void drawOverlay(Texture image, Color tint, boolean fill) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        float w, h;
        if (fill) {
            w = getWidth();
            h = getHeight();
        } else {
            w = image.getWidth();
            h = image.getHeight();
        }
        spriteBatch.setColor(tint);
        spriteBatch.draw(image, 0, 0, w, h);
    }

    public void drawText(BitmapFont font, String text, float x, float y) {
        if (!active) {
            Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
            return;
        }
        font.draw(spriteBatch, text, x, y);
    }

    public void drawLine(Color color, Vector2 start, Vector2 end, int lineWidth){
        spriteBatch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(lineWidth);
        shapeRenderer.setColor(color);
        shapeRenderer.line(start, end);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();
        spriteBatch.begin();
    }

}
