package si.um.feri.krajnc.N2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Seagull extends GameObject {
    static final Texture TEXTURE = new Texture("images/seagull.png");
    static final Sound SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/eating.mp3"));
    static final float SPAWN_TIME = 4f;
    final float frequency;
    final float amplitude;

    public Seagull() {
        super(TEXTURE, MathUtils.random(250f, 350f));
        frequency = MathUtils.random(3f, 6f);
        amplitude = MathUtils.random(100f, 400f);
    }

    @Override
    void update(float delta) {
        super.update(delta);
        sprite.setX(MathUtils.clamp(sprite.getX() - MathUtils.sin(time * frequency) * amplitude * delta, 0f, Gdx.graphics.getWidth() -  sprite.getWidth()));
    }

    @Override
    void onCollision(GameObject other) {
        if (other instanceof Player) {
            Player player = (Player) other;
            SOUND.play();
            player.setCharges(player.getCharges() + 1);
            setActive(false);
        }
    }

    static void dispose() {
        TEXTURE.dispose();
        SOUND.dispose();
    }
}
