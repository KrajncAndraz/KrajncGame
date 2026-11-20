package si.um.feri.krajnc.N2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Soul extends GameObject {
    static final Texture TEXTURE = new Texture("images/soul.png");
    static final Sound SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/soul-collect.mp3"));
    static final float SPAWN_TIME = 4f;
    final float frequency;
    final float amplitude;

    public Soul() {
        super(TEXTURE, 200f);
        frequency = MathUtils.random(2f, 4f);
        amplitude = MathUtils.random(20f, 40f);
    }

    @Override
    void update(float delta) {
        super.update(delta);
        float sizeChange = MathUtils.sin(time * frequency) * amplitude;
        sprite.setSize(sprite.getWidth() + (sizeChange * delta), sprite.getHeight() + (sizeChange * delta));
        sprite.setPosition(sprite.getX() - (sizeChange * delta * 0.5f), sprite.getY() - (sizeChange * delta * 0.5f));
    }

    @Override
    void onCollision(GameObject other) {
        if (other instanceof Player) {
            Player player = (Player) other;
            SOUND.play();
            player.setSoulsCollected(player.getSoulsCollected() + 1);
            setActive(false);
        }
    }

    static void dispose() {
        TEXTURE.dispose();
        SOUND.dispose();
    }
}
