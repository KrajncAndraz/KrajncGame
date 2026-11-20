package si.um.feri.krajnc.N3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;

public abstract class NonPlayerEntity extends Entity implements Pool.Poolable {
    public NonPlayerEntity(Texture texture, float speed) {
        super(texture, speed);
    }

    abstract void onCollision(Entity other);

    @Override
    public void reset() {
        sprite.setSize(
            sprite.getTexture().getWidth(),
            sprite.getTexture().getHeight());
        sprite.setOrigin(sprite.getWidth() * 0.5f, sprite.getHeight() * 0.5f);
        sprite.setPosition(
            MathUtils.random(0f, Gdx.graphics.getWidth() - sprite.getTexture().getWidth()),
            Gdx.graphics.getHeight());
        setActive(true);
        time = 0f;
    }
}
