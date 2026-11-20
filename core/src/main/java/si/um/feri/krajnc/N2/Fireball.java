package si.um.feri.krajnc.N2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Fireball extends GameObject {
    static final Texture TEXTURE = new Texture("images/fireball.png");
    static final Sound SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/fireball.mp3"));
    static final Sound HIT_SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/fireballHit.mp3"));
    static final float SPAWN_TIME = 0.5f;

    public Fireball(Rectangle player) {
        super(TEXTURE, MathUtils.random(300f, 350f));

        sprite.setPosition(player.getX() + player.getWidth() * 0.5f - sprite.getWidth() * 0.5f, player.getY() + player.getHeight());
        SOUND.play();
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
    }

    @Override
    boolean isOffScreen() {
        return sprite.getY() > Gdx.graphics.getHeight() || sprite.getWidth() <= 0 || sprite.getHeight() <= 0;
    }

    @Override
    void onCollision(GameObject other) {
        if (other instanceof Ballista) {
            HIT_SOUND.play();
            Ballista.SOUND.play();
            setActive(false);
            other.setActive(false);
        }
    }

    static void dispose() {
        TEXTURE.dispose();
        SOUND.dispose();
        HIT_SOUND.dispose();
    }
}
