package si.um.feri.krajnc.N3;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Background {
    private Array<Rectangle> tiles = new Array<>();
    private Texture texture;
    private float speed;

    public Background(Texture texture, float speed) {
        this.texture = texture;
        tiles = new Array<>();
        Rectangle background = new Rectangle();
        background.x = 0;
        background.y = 0;
        background.width = texture.getWidth();
        background.height = texture.getHeight();
        Rectangle background2 = new Rectangle();
        background2.x = 0;
        background2.y = texture.getHeight();
        background2.width = texture.getWidth();
        background2.height = texture.getHeight();
        tiles.add(background);
        tiles.add(background2);
        this.speed = speed;
    }

    void update(float delta) {
        for (int i = 0; i < tiles.size; i++) {
            Rectangle background = tiles.get(i);
            background.y -= speed * delta;
            if (background.y + texture.getHeight() < 0) {
                background.y = tiles.get(i == tiles.size-1 ? i-1 : i+1).y + texture.getHeight() - speed * delta;
            }
        }
    }

    void draw(SpriteBatch batch) {
        for (Rectangle background : tiles) {
            batch.draw(texture, background.getX(), background.getY());
        }
    }

    void dispose() {
        texture.dispose();
    }
}
