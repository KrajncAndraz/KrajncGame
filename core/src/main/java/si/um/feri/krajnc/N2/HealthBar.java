package si.um.feri.krajnc.N2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class HealthBar {
    private Texture borderTexture;
    private Rectangle borderRect;
    private ShapeRenderer healthBar;

    public HealthBar(Texture borderTexture) {
        this.borderTexture = borderTexture;
        healthBar = new ShapeRenderer();
        borderRect = new Rectangle();
        borderRect.set(
            5f,
            Gdx.graphics.getHeight() - borderTexture.getHeight() - 45f,
            borderTexture.getWidth(),
            borderTexture.getHeight()
        );
    }

    void render(Player player){
        healthBar.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.setColor(Color.GREEN);
        healthBar.rect(28f, Gdx.graphics.getHeight() - 84f, Math.max(0, player.getHealth() * 2f), 20f);
        healthBar.end();
    }

    void draw(SpriteBatch batch) {
        batch.draw(borderTexture,  borderRect.getX(), borderRect.getY());}

    void dispose(){
        healthBar.dispose();
        borderTexture.dispose();
    }
}
