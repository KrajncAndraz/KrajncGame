package si.um.feri.krajnc.N4;

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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Iterator;

public class KrajncGame extends ApplicationAdapter {
    private SpriteBatch batch;

    Viewport worldViewport;
    Viewport hudViewport;
    private static final float WORLD_WIDTH = 160f;
    private static final float WORLD_HEIGHT = 90f;
    private static final float PPU = 10f;

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

    private float seagullSpawnTime;
    private float soulSpawnTime;
    private int soulsCollected;

    private float ballistaSpawnTime;
    private int health;
    private int fireballCharges;
    private float fireballSpawnTime;

    private static final float WYVERN_SPEED = 300f / PPU;
    private static final float SEAGULL_SPEED = 250f / PPU;
    private static final float SEAGULL_SPAWN_TIME = 4f;
    private static final float SOUL_SPEED = 200f / PPU;
    private static final float SOUL_SPAWN_TIME = 4f;
    private static final float BALLISTA_SPEED = 300f / PPU;
    private static final float BALLISTA_DAMAGE = 25f;
    private static final float BALLISTA_SPAWN_TIME = 1f;
    private static final float FIREBALL_SPEED = 300f / PPU;
    private static final float FIREBALL_SPAWN_TIME = 0.5f;
    private static final float BACKGROUND_SPEED = 200f / PPU;

    @Override
    public void create() {
        batch = new SpriteBatch();

        worldViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        hudViewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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

        wyvern = new Rectangle();
        wyvern.width = wyvernImg.getWidth() / PPU;
        wyvern.height = wyvernImg.getHeight() / PPU;
        wyvern.x = WORLD_WIDTH * 0.5f - wyvern.getWidth() * 0.5f;
        wyvern.y = 20f  / PPU;

        seagulls = new Array<>();

        souls = new Array<>();
        soulsCollected = 0;

        ballistas = new Array<>();
        health = 100;

        fireballs = new Array<>();
        fireballCharges = 3;

        float bgHeight = backgroundImg.getHeight() * (WORLD_WIDTH / backgroundImg.getWidth());
        backgrounds = new Array<>();
        Rectangle background = new Rectangle();
        background.x = 0;
        background.y = 0;
        background.width = WORLD_WIDTH;
        background.height = bgHeight;
        Rectangle background2 = new Rectangle();
        background2.x = 0;
        background2.y = bgHeight;
        background2.width = WORLD_WIDTH;
        background2.height = bgHeight;
        backgrounds.add(background);
        backgrounds.add(background2);

        healthBarBorder = new Rectangle();
        healthBarBorder.x = 5f;
        healthBarBorder.y = hudViewport.getWorldHeight() - healthBarBorderImg.getHeight() - 45f;
        healthBarBorder.width = healthBarBorderImg.getWidth();
        healthBarBorder.height = healthBarBorderImg.getHeight();
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);

        if (health > 0) {
            handleInput();
            update(Gdx.graphics.getDeltaTime());
        }

        worldViewport.apply();
        batch.setProjectionMatrix(worldViewport.getCamera().combined);
        batch.begin();
        drawWorld();
        batch.end();

        hudViewport.apply();
        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        drawHud();
        batch.end();

        healthBar.setProjectionMatrix(hudViewport.getCamera().combined);
        healthBar.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.setColor(Color.GREEN);
        healthBar.rect(28f, hudViewport.getWorldHeight() - 84f, health * 2f, 20f);
        healthBar.end();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveLeft(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveRight(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) spawnFireball();
    }

    private void update(float delta) {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        if (elapsedTime - seagullSpawnTime > SEAGULL_SPAWN_TIME) spawnSeagull();
        if (elapsedTime - soulSpawnTime > SOUL_SPAWN_TIME) spawnSoul();
        if (elapsedTime - ballistaSpawnTime > BALLISTA_SPAWN_TIME) spawnBallista();

        for (Iterator<Rectangle> it = seagulls.iterator(); it.hasNext(); ) {
            Rectangle seagull = it.next();
            seagull.y -= SEAGULL_SPEED * delta;
            if (seagull.y + seagull.getHeight() < 0) {
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
            if (soul.y + soul.getHeight() < 0) {
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
            if (ballista.y + ballista.getHeight() < 0) {
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
                if (fireball.y > WORLD_HEIGHT) {
                    itf.remove();
                }
            }
            if (ballista.overlaps(wyvern)) {
                health -= (int) BALLISTA_DAMAGE;
                wyvernSound.play();
                it.remove();
            }
        }

        for (int i = 0; i < backgrounds.size; i++) {
            Rectangle background = backgrounds.get(i);
            background.y -= BACKGROUND_SPEED * delta;
            if (background.y + background.getHeight() < 0) {
                background.y = backgrounds.get(i == backgrounds.size-1 ? i-1 : i+1).y + background.getHeight() - BACKGROUND_SPEED * delta;
            }
        }
    }

    private void drawWorld() {
        for (Rectangle background : backgrounds) {
            batch.draw(backgroundImg, background.x, background.y, background.getWidth(), background.getHeight());
        }

        if (health <= 0) {
            return;
        }

        for (Rectangle seagull : seagulls) {
            batch.draw(seagullImg, seagull.x, seagull.y, seagull.getWidth(), seagull.getHeight());
        }

        for (Rectangle soul : souls) {
            batch.draw(soulImg, soul.x, soul.y, soul.getWidth(), soul.getHeight());
        }

        for (Rectangle ballista : ballistas) {
            batch.draw(ballistaImg, ballista.x, ballista.y, ballista.getWidth(), ballista.getHeight());
        }

        for (Rectangle fireball : fireballs) {
            batch.draw(fireballImg, fireball.x, fireball.y, fireball.getWidth(), fireball.getHeight());
        }

        batch.draw(wyvernImg, wyvern.x, wyvern.y, wyvern.getWidth(), wyvern.getHeight());

    }

    private void drawHud() {
        if (health <= 0) {
            font.setColor(Color.RED);
            font.draw(batch,
                "GAME OVER",
                20f, hudViewport.getWorldHeight() - 20f
            );
            return;
        }

        font.setColor(Color.RED);
        font.draw(batch,
            "HEALTH: " + health,
            20f, hudViewport.getWorldHeight() - 20f
        );
        batch.draw(healthBarBorderImg,  healthBarBorder.x, healthBarBorder.y);

        font.setColor(Color.ORANGE);
        font.draw(batch,
            "CHARGES: " + fireballCharges,
            20f, hudViewport.getWorldHeight() - 110f
        );

        font.setColor(Color.GREEN);
        font.draw(batch,
            "SOULS: " + soulsCollected,
            20f, hudViewport.getWorldHeight() - 150f
        );
    }

    private void moveLeft(float delta) {
        wyvern.x -= WYVERN_SPEED * delta;
        if (wyvern.x < 0)
            wyvern.x = 0f;
    }

    private void moveRight(float delta) {
        wyvern.x += WYVERN_SPEED * delta;
        if (wyvern.x > WORLD_WIDTH - wyvern.getWidth())
            wyvern.x = WORLD_WIDTH - wyvern.getWidth();
    }

    private void spawnSeagull() {
        Rectangle seagull = new Rectangle();
        seagull.width = seagullImg.getWidth() / PPU;
        seagull.height = seagullImg.getHeight() / PPU;
        seagull.x = MathUtils.random(0f, WORLD_WIDTH - seagull.getWidth());
        seagull.y = WORLD_HEIGHT;
        seagulls.add(seagull);
        seagullSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnSoul() {
        Rectangle soul = new Rectangle();
        soul.width = soulImg.getWidth() / PPU;
        soul.height = soulImg.getHeight() / PPU;
        soul.x = MathUtils.random(0f, WORLD_WIDTH - soul.getWidth());
        soul.y = WORLD_HEIGHT;
        souls.add(soul);
        soulSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnBallista() {
        Rectangle ballista = new Rectangle();
        ballista.width = ballistaImg.getWidth() / PPU;
        ballista.height = ballistaImg.getHeight() / PPU;
        ballista.x = MathUtils.random(0f, WORLD_WIDTH - ballista.getWidth());
        ballista.y = WORLD_HEIGHT;
        ballistas.add(ballista);
        ballistaSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
    }

    private void spawnFireball() {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        if (elapsedTime - fireballSpawnTime > FIREBALL_SPAWN_TIME && fireballCharges > 0) {
            Rectangle fireball = new Rectangle();
            fireball.width = fireballImg.getWidth() / PPU;
            fireball.height = fireballImg.getHeight() / PPU;
            fireball.x = wyvern.x + (wyvern.getWidth() * 0.5f) - (fireball.getWidth() * 0.5f);
            fireball.y = wyvern.y + wyvern.getHeight();
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
        healthBarBorderImg.dispose();
        healthBar.dispose();
        backgroundImg.dispose();
        wyvernSound.dispose();
        eatingSound.dispose();
        soulCollectSound.dispose();
        fireballSound.dispose();
        fireballHitSound.dispose();
        font.dispose();
    }
}
