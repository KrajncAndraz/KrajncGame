package si.um.feri.krajnc.N2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Player extends GameObject {
    private int health;
    private int charges;
    private int soulsCollected;
    static final Texture TEXTURE = new Texture("images/wyvern.png");
    static final Sound SOUND = Gdx.audio.newSound(Gdx.files.internal("sounds/wyvern.mp3"));
    private final float turningSpeed;

    public Player() {
        super(TEXTURE, 300f);

        sprite.setPosition(Gdx.graphics.getWidth() * 0.5f - TEXTURE.getWidth() * 0.5f, 20f);

        health = 100;
        charges = 3;
        soulsCollected = 0;
        turningSpeed = 150f;
    }

    int getHealth() {
        return health;
    }

    int getCharges() {
        return charges;
    }

    int getSoulsCollected() {
        return soulsCollected;
    }

    void setHealth(int health) {
        this.health = health;
    }

    void setCharges(int charges) {
        this.charges = charges;
    }

    void setSoulsCollected(int soulsCollected) {
        this.soulsCollected = soulsCollected;
    }

    void moveLeft(float delta) {
        sprite.setX(MathUtils.clamp(sprite.getX() - speed * delta, 0f, Gdx.graphics.getWidth() - TEXTURE.getWidth()));
        sprite.setRotation(MathUtils.clamp(sprite.getRotation() + turningSpeed * delta, -15f, 15f));
    }

    void moveRight(float delta) {
        sprite.setX(MathUtils.clamp(sprite.getX() + speed * delta, 0f, Gdx.graphics.getWidth() - TEXTURE.getWidth()));
        sprite.setRotation(MathUtils.clamp(sprite.getRotation() - turningSpeed * delta, -15f, 15f));
    }

    @Override
    void update(float delta) {
        sprite.setRotation(MathUtils.lerp(sprite.getRotation(), 0f, 8f * delta));
    }

    @Override
    void onCollision(GameObject other) {}

    static void dispose() {
        TEXTURE.dispose();
        SOUND.dispose();
    }
}
