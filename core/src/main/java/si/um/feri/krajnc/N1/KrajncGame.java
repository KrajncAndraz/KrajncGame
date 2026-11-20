package si.um.feri.krajnc.N1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class KrajncGame extends ApplicationAdapter {
    private SpriteBatch batch;

    private Texture wyvernImg;
    private Texture seagullImg;
    private Texture soulImg;
    private Texture ballistaImg;
    private Texture fireballImg;
    private Texture backgroundImg;
    private Texture healthBarBorderImg;

    private Sound wyvernSound;
    private Sound eatingSound;
    private Sound soulCollectSound;
    private Sound fireballSound;
    private Sound fireballHitSound;

    private BitmapFont font;

    private Rectangle wyvern;
    private Array<Rectangle> seagulls;
    private Array<Rectangle> souls;
    private Array<Rectangle> ballistas;
    private Array<Rectangle> fireballs;
    private Array<Rectangle> backgrounds;
    private Rectangle healthBarBorder;

    ShapeRenderer healthBar;
    ShapeRenderer circle;

    private float seagullSpawnTime;
    private float soulSpawnTime;
    private int soulsCollected;
    private int points;
    private float startTime;

    private float ballistaSpawnTime;
    private int health;
    private int fireballCharges;
    private float fireballSpawnTime;
    private float invincibilityDuration;

    private static final float WYVERN_SPEED = 300f;
    private static final float SEAGULL_SPEED = 250f;
    private static final float SEAGULL_SPAWN_TIME = 4f;
    private static final float SOUL_SPEED = 200f;
    private static final float SOUL_SPAWN_TIME = 4f;
    private static final float BALLISTA_SPEED = 300f;
    private static final float BALLISTA_DAMAGE = 25f;
    private static final float BALLISTA_SPAWN_TIME = 1f;
    private static final float FIREBALL_SPEED = 300f;
    private static final float FIREBALL_SPAWN_TIME = 0.5f;
    private static final float BACKGROUND_SPEED = 200f;
    private static final float INVINCIBILITY_DURATION = 3f;

    @Override
    public void create() {
        batch = new SpriteBatch();

        wyvernImg = new Texture("images/wyvern.png");
        seagullImg = new Texture("images/seagull.png");
        soulImg = new Texture("images/soul.png");
        ballistaImg = new Texture("images/ballista.png");
        fireballImg = new Texture("images/fireball.png");
        backgroundImg = new Texture("images/background.jpg");
        healthBarBorderImg = new Texture("images/healthBarBorder.png");

        wyvernSound = Gdx.audio.newSound(Gdx.files.internal("sounds/wyvern.mp3"));
        eatingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/eating.mp3"));
        soulCollectSound = Gdx.audio.newSound(Gdx.files.internal("sounds/soul-collect.mp3"));
        fireballSound = Gdx.audio.newSound(Gdx.files.internal("sounds/fireball.mp3"));
        fireballHitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/fireballHit.mp3"));

        font = new BitmapFont(Gdx.files.internal("fonts/arial-32.fnt"));

        healthBar = new ShapeRenderer();
        circle = new ShapeRenderer();

        wyvern = new Rectangle();
        wyvern.x = Gdx.graphics.getWidth() * 0.5f - wyvernImg.getWidth() * 0.5f;
        wyvern.y = 20f;
        wyvern.width = wyvernImg.getWidth();
        wyvern.height = wyvernImg.getHeight();

        seagulls = new Array<>();

        souls = new Array<>();
        soulsCollected = 0;
        points = 0;
        startTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;

        ballistas = new Array<>();
        health = 100;

        fireballs = new Array<>();
        fireballCharges = 3;

        backgrounds = new Array<>();
        Rectangle background = new Rectangle();
        background.x = 0;
        background.y = 0;
        background.width = backgroundImg.getWidth();
        background.height = backgroundImg.getHeight();
        Rectangle background2 = new Rectangle();
        background2.x = 0;
        background2.y = backgroundImg.getHeight();
        background2.width = backgroundImg.getWidth();
        background2.height = backgroundImg.getHeight();
        backgrounds.add(background);
        backgrounds.add(background2);

        healthBarBorder = new Rectangle();
        healthBarBorder.x = 5f;
        healthBarBorder.y = Gdx.graphics.getHeight() - healthBarBorderImg.getHeight() - 45f;
        healthBarBorder.width = healthBarBorderImg.getWidth();
        healthBarBorder.height = healthBarBorderImg.getHeight();
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLUE);

        if (health > 0) {
            handleInput();
            update(Gdx.graphics.getDeltaTime());
        }

        batch.begin();

        draw();

        batch.end();

        healthBar.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.setColor(Color.GREEN);
        healthBar.rect(28f, Gdx.graphics.getHeight() - 84f, health * 2f, 20f);
        healthBar.end();

        circle.begin(ShapeRenderer.ShapeType.Filled);
        circle.setColor(Color.RED);
        circle.circle(Gdx.graphics.getWidth() - 50f, Gdx.graphics.getHeight() - 50f, 50f, 50);
        circle.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveLeft(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveRight(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) spawnFireball();
    }

    private void update(float delta) {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        points = (int)(elapsedTime - startTime);
        if (elapsedTime - seagullSpawnTime > SEAGULL_SPAWN_TIME) spawnSeagull();
        if (elapsedTime - soulSpawnTime > SOUL_SPAWN_TIME) spawnSoul();
        if (elapsedTime - ballistaSpawnTime > BALLISTA_SPAWN_TIME) spawnBallista();

        for (Iterator<Rectangle> it = seagulls.iterator(); it.hasNext(); ) {
            Rectangle seagull = it.next();
            seagull.y -= SEAGULL_SPEED * delta;
            if (seagull.y + seagullImg.getHeight() < 0) {
                it.remove();
            }
            if (seagull.overlaps(wyvern)) {
                eatingSound.play();
                fireballCharges++;
                it.remove();
            }
        }

        for (Iterator<Rectangle> it = souls.iterator(); it.hasNext(); ) {
            Rectangle soul = it.next();
            soul.y -= SOUL_SPEED * delta;
            if (soul.y + soulImg.getHeight() < 0) {
                it.remove();
            }
            if (soul.overlaps(wyvern)) {
                soulsCollected++;
                soulCollectSound.play();
                it.remove();
            }
        }

        for (Iterator<Rectangle> it = ballistas.iterator(); it.hasNext(); ) {
            Rectangle ballista = it.next();
            ballista.y -= BALLISTA_SPEED * delta;
            if (ballista.y + ballistaImg.getHeight() < 0) {
                it.remove();
            }
            for (Iterator<Rectangle> itf = fireballs.iterator(); itf.hasNext(); ) {
                Rectangle fireball = itf.next();
                fireball.y += FIREBALL_SPEED * delta;
                if (fireball.overlaps(ballista)) {
                    fireballHitSound.play();
                    itf.remove();
                    it.remove();
                    break;
                }
                if (fireball.y > Gdx.graphics.getHeight()) {
                    itf.remove();
                }
            }
            if (ballista.overlaps(wyvern) && elapsedTime - invincibilityDuration > INVINCIBILITY_DURATION) {
                health -= (int) BALLISTA_DAMAGE;
                wyvernSound.play();
                it.remove();
                invincibilityDuration = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
            }
        }

        for (int i = 0; i < backgrounds.size; i++) {
            Rectangle background = backgrounds.get(i);
            background.y -= BACKGROUND_SPEED * delta;
            if (background.y + backgroundImg.getHeight() < 0) {
                background.y = backgrounds.get(i == backgrounds.size-1 ? i-1 : i+1).y + backgroundImg.getHeight() - BACKGROUND_SPEED * delta;
            }
        }
    }

    private void draw() {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        if(elapsedTime - invincibilityDuration > INVINCIBILITY_DURATION) {
            for (Rectangle background : backgrounds) {
                batch.draw(backgroundImg, background.x, background.y);
            }
        }

        if (health <= 0) {
            font.setColor(Color.RED);
            font.draw(batch,
                "GAME OVER",
                20f, Gdx.graphics.getHeight() - 20f
            );
            return;
        }
        for (Rectangle seagull : seagulls) {
            batch.draw(seagullImg, seagull.x, seagull.y, seagull.width, seagull.height);
        }
        for (Rectangle soul : souls) {
            batch.draw(soulImg, soul.x, soul.y, soul.width, soul.height);
        }
        for (Rectangle ballista : ballistas) {
            batch.draw(ballistaImg, ballista.x, ballista.y, ballista.width, ballista.height);
        }
        for (Rectangle fireball : fireballs) {
            batch.draw(fireballImg, fireball.x, fireball.y);
        }
        batch.draw(wyvernImg, wyvern.x, wyvern.y);

        font.setColor(Color.RED);
        font.draw(batch,
            "HEALTH: " + health,
            20f, Gdx.graphics.getHeight() - 20f
        );
        batch.draw(healthBarBorderImg,  healthBarBorder.x, healthBarBorder.y);

        font.setColor(Color.ORANGE);
        font.draw(batch,
            "CHARGES: " + fireballCharges,
            20f, Gdx.graphics.getHeight() - 110f
        );

        font.setColor(Color.GREEN);
        font.draw(batch,
            "SOULS: " + soulsCollected,
            20f, Gdx.graphics.getHeight() - 150f
        );

        font.setColor(Color.YELLOW);
        font.draw(batch,
            "POINTS: " + points,
            20f, Gdx.graphics.getHeight() - 190f
        );

        int shieldRemainingDuration = MathUtils.clamp((int)(elapsedTime % invincibilityDuration), 0, 3);
        switch(shieldRemainingDuration) {
            case 0:
                shieldRemainingDuration = 3;
                break;
            case 1:
                shieldRemainingDuration = 2;
                break;
            case 2:
                shieldRemainingDuration = 1;
                break;
        }
        if (elapsedTime - invincibilityDuration < INVINCIBILITY_DURATION) {
            font.setColor(Color.CYAN);
            font.draw(batch,
                "SHIELD: " + shieldRemainingDuration + "s",
                20f, 60f
            );

        }
    }

    private void moveLeft(float delta) {
        wyvern.x -= WYVERN_SPEED * delta;
        if (wyvern.x < 0)
            wyvern.x = 0f;
    }

    private void moveRight(float delta) {
        wyvern.x += WYVERN_SPEED * delta;
        if (wyvern.x > Gdx.graphics.getWidth() * 0.5f - wyvernImg.getWidth())
            wyvern.x = Gdx.graphics.getWidth() * 0.5f - wyvernImg.getWidth();
    }

    private void spawnSeagull() {
        Rectangle seagull = new Rectangle();
        seagull.x = MathUtils.random(0f, Gdx.graphics.getWidth() - wyvernImg.getWidth());
        seagull.y = Gdx.graphics.getHeight();
        seagull.width = MathUtils.random(30f, 80f);
        seagull.height = MathUtils.random(30f, 80f);
        seagulls.add(seagull);
        seagullSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnSoul() {
        Rectangle soul = new Rectangle();
        soul.x = MathUtils.random(0f, Gdx.graphics.getWidth() - wyvernImg.getWidth());
        soul.y = Gdx.graphics.getHeight();
        soul.width = MathUtils.random(30f, 80f);
        soul.height = MathUtils.random(30f, 80f);
        souls.add(soul);
        soulSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnBallista() {
        Rectangle ballista = new Rectangle();
        ballista.x = MathUtils.random(0f, Gdx.graphics.getWidth() - ballistaImg.getWidth());
        ballista.y = Gdx.graphics.getHeight();
        ballista.width = MathUtils.random(30f, 80f);
        ballista.height = MathUtils.random(30f, 80f);
        ballistas.add(ballista);
        ballistaSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnFireball() {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        if (elapsedTime - fireballSpawnTime > FIREBALL_SPAWN_TIME && fireballCharges > 0) {
            Rectangle fireball = new Rectangle();
            fireball.x = wyvern.x;
            fireball.y = wyvern.getHeight();
            fireball.width = fireballImg.getWidth();
            fireball.height = fireballImg.getHeight();
            fireballs.add(fireball);
            fireballSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
            fireballSound.play();
            fireballCharges--;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        wyvernImg.dispose();
        seagullImg.dispose();
        soulImg.dispose();
        ballistaImg.dispose();
        fireballImg.dispose();
        backgroundImg.dispose();
        wyvernSound.dispose();
        eatingSound.dispose();
        soulCollectSound.dispose();
        fireballSound.dispose();
        fireballHitSound.dispose();
        font.dispose();
    }
}
