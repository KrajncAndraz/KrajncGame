package si.um.feri.krajnc.N3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Fireball extends NonPlayerEntity {
    static final Texture TEXTURE = new Texture("images/fireball.png");
    static final Sound SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/fireball.mp3"));
    static final Sound HIT_SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/fireballHit.mp3"));
    static final float SPAWN_TIME = 0.5f;
    ParticleEffectPool.PooledEffect trail;

    public Fireball() {
        super(TEXTURE, MathUtils.random(300f, 350f));
    }

    @Override
    void update(float delta) {
        time += delta;

        float DECAY_RATE = 25f;
        sprite.setPosition(
            sprite.getX() + (DECAY_RATE * delta * 0.5f),
            sprite.getY() + (speed * delta));
        sprite.setSize(
            sprite.getWidth() - (DECAY_RATE * delta),
            sprite.getHeight() - (DECAY_RATE  * delta));

        if (trail != null) {
            trail.setPosition(sprite.getX() + sprite.getWidth() * 0.5f, sprite.getY() + sprite.getHeight() - 5f);
            trail.update(delta);
        }
    }

    @Override
    boolean isOffScreen() {
        return sprite.getY() > Gdx.graphics.getHeight() || sprite.getWidth() <= 0 || sprite.getHeight() <= 0;
    }

    @Override
    void onCollision(Entity other) {
        if (other instanceof Ballista) {
            HIT_SOUND.play();
            Ballista.SOUND.play();
            setActive(false);
            other.setActive(false);
        }
    }

    void fire(Player player) {
        sprite.setPosition(player.sprite.getX() + player.sprite.getWidth() * 0.5f - sprite.getWidth() * 0.5f,
            player.sprite.getY() + player.sprite.getHeight());
        SOUND.play();
    }

    @Override
    public void reset() {
        super.reset();
        speed = MathUtils.random(300f, 350f);
        if (trail != null) {
            trail.free();
            trail = null;
        }
    }

    @Override
    void draw(SpriteBatch batch) {
        if (trail != null) {
            trail.draw(batch);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        super.draw(batch);
    }


    static void dispose() {
        TEXTURE.dispose();
        SOUND.dispose();
        HIT_SOUND.dispose();
    }
}
