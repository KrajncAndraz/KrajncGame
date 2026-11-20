package si.um.feri.krajnc.N3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public abstract class Entity {
    Sprite sprite;
    float speed;
    float time;
    boolean active;

    public Entity(Texture texture, float speed) {
        sprite = new Sprite(texture);
        sprite.setSize(texture.getWidth(), texture.getHeight());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(
            MathUtils.random(0f, Gdx.graphics.getWidth() - texture.getWidth()),
            Gdx.graphics.getHeight());

        this.speed = speed;
        time = 0f;
        active = true;
    }

    void update(float delta) {
        time += delta;
        sprite.setY(sprite.getY() - (speed * delta));
    }

    boolean isOffScreen() {
        return sprite.getY() + sprite.getHeight() < 0;
    }

    boolean hasCollided(Entity other) {
        return sprite.getBoundingRectangle().overlaps(other.sprite.getBoundingRectangle());
    }

    boolean isActive() {
        return active;
    }

    void setActive(boolean active) {
        this.active = active;
    }

    void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
