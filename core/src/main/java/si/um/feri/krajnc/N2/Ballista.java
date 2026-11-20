package si.um.feri.krajnc.N2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Ballista extends GameObject {
    static final Texture TEXTURE = new Texture("images/ballista.png");
    static final Sound SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/ballista.wav"));
    static final float SPAWN_TIME = 1f;
    static final int DAMAGE = 25;

    public Ballista() {
        super(TEXTURE, MathUtils.random(250f, 350f));
    }

    @Override
    void onCollision(GameObject other) {
        if (other instanceof Player) {
            Player player = (Player) other;
            SOUND.play();
            Player.SOUND.play();
            player.setHealth(player.getHealth() - DAMAGE);
            setActive(false);
        }
    }

    static void dispose() {
        TEXTURE.dispose();
        SOUND.dispose();
    }
}
