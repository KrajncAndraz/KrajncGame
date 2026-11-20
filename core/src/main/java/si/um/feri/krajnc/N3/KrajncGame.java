package si.um.feri.krajnc.N3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class KrajncGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;

    private Player wyvern;
    private Array<NonPlayerEntity> entities;
    private Background background;
    HealthBar healthBar;

    PoolManager entityPools;

    private float seagullSpawnTime;
    private float soulSpawnTime;
    private float fireballSpawnTime;
    private float ballistaSpawnTime;

    private si.um.feri.util.debug.DebugCameraController debugCameraController;
    private si.um.feri.util.debug.MemoryInfo memoryInfo;
    private boolean debug = false;
    private ShapeRenderer shapeRenderer;
    private Viewport viewport;
    private OrthographicCamera camera;

    ParticleEffect fireTrail = new ParticleEffect();
    ParticleEffect explosion = new ParticleEffect();
    ParticleEffectPool fireTrailPool;
    ParticleEffectPool explosionPool;
    Array<ParticleEffectPool.PooledEffect> explosionEffects;

    @Override
    public void create() {
        batch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("fonts/arial-32.fnt"));

        background = new Background(new Texture(Gdx.files.internal("images/background.jpg")), 200f);

        healthBar = new HealthBar(new Texture(Gdx.files.internal("images/healthBarBorder.png")));

        fireTrail.load(Gdx.files.internal("particles/fireball_trail.p"), Gdx.files.internal("particles"));
        fireTrail.setEmittersCleanUpBlendFunction(false);
        fireTrailPool = new ParticleEffectPool(fireTrail, 4, 6);
        explosion.load(Gdx.files.internal("particles/fireball_explosion.p"), Gdx.files.internal("particles"));
        explosion.setEmittersCleanUpBlendFunction(false);
        explosionPool = new ParticleEffectPool(explosion, 3, 5);
        explosionEffects = new Array<>();

        wyvern = new Player();

        entities = new Array<>();

        entityPools = new PoolManager();
        entityPools.addPool(Seagull.class, new Pool<Seagull>(2, 4) {
            @Override
            protected Seagull newObject() {
                return new Seagull();
            }
        });
        entityPools.getPool(Seagull.class).fill(1);

        entityPools.addPool(Soul.class, new Pool<Soul>(2, 4) {
            @Override
            protected Soul newObject() {
                return new Soul();
            }
        });
        entityPools.getPool(Soul.class).fill(1);

        entityPools.addPool(Ballista.class, new Pool<Ballista>(3, 5) {
            @Override
            protected Ballista newObject() {
                return new Ballista();
            }
        });
        entityPools.getPool(Ballista.class).fill(3);

        entityPools.addPool(Fireball.class, new Pool<Fireball>(4, 6) {
            @Override
            protected Fireball newObject() {
                return new Fireball();
            }
        });
        entityPools.getPool(Fireball.class).fill(4);

        debugCameraController = new si.um.feri.util.debug.DebugCameraController();
        debugCameraController.setStartPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        memoryInfo = new si.um.feri.util.debug.MemoryInfo(500);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);

        if (wyvern.getHealth() > 0) {
            handleInput();
            update(Gdx.graphics.getDeltaTime());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debug = !debug;
            if (!debug) {
                viewport.getCamera().position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
                camera.zoom = 1f;
                camera.update();
                batch.setProjectionMatrix(camera.combined);
            }
        }

        if (debug) {
            debugCameraController.handleDebugInput(Gdx.graphics.getDeltaTime());
            debugCameraController.applyTo(camera);
            batch.setProjectionMatrix(camera.combined);
            memoryInfo.update();
        }

        camera.update();

        batch.begin();
        draw();
        batch.end();

        healthBar.render(wyvern, camera);

        if (debug) {
            batch.begin();
            drawDebug();
            batch.end();

            batch.totalRenderCalls = 0;
            si.um.feri.util.ViewportUtils.drawGrid(viewport, shapeRenderer, 50);

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 1, 0, 1);
            for (NonPlayerEntity entity : entities) {
                shapeRenderer.rect(entity.sprite.getX(), entity.sprite.getY(), entity.sprite.getWidth(), entity.sprite.getHeight());
            }
            shapeRenderer.rect(wyvern.sprite.getBoundingRectangle().getX(), wyvern.sprite.getBoundingRectangle().getY(), wyvern.sprite.getBoundingRectangle().getWidth(), wyvern.sprite.getBoundingRectangle().getHeight());
            shapeRenderer.end();
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) wyvern.moveLeft(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.D)) wyvern.moveRight(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
            spawnFireball(elapsedTime);
        }
    }

    private void update(float delta) {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        spawnSeagull(elapsedTime);
        spawnSoul(elapsedTime);
        spawnBallista(elapsedTime);

        for(int i = explosionEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = explosionEffects.get(i);
            effect.update(delta);
            if (effect.isComplete()) {
                effect.free();
                explosionEffects.removeIndex(i);
            }
        }

        for (int i = 0; i < entities.size; i++) {
            NonPlayerEntity entity = entities.get(i);
            if (!entity.isActive()) {
                continue;
            }

            entity.update(delta);
            if (entity.isOffScreen()) {
                entity.setActive(false);
            }
            else if (!(entity instanceof Fireball) && entity.hasCollided(wyvern)) {
                entity.onCollision(wyvern);
            }
            else if (entity instanceof Fireball) {
                for (int j = 0; j < entities.size; j++) {
                    Entity other = entities.get(j);
                    if (other instanceof Ballista && other.isActive() && entity.hasCollided(other)) {
                        entity.onCollision(other);
                        ParticleEffectPool.PooledEffect explosion = explosionPool.obtain();
                        explosion.setPosition(other.sprite.getX() + (other.sprite.getWidth() * 0.5f),
                            other.sprite.getY());
                        explosionEffects.add(explosion);
                        break;
                    }
                }
            }
        }

        for (int i = entities.size - 1; i >= 0; i--) {
            NonPlayerEntity entity = entities.get(i);
            if (!entity.isActive()) {
                entities.removeIndex(i);
                entityPools.free(entity);
            }
        }

        background.update(delta);

        wyvern.update(delta);
    }

    private void draw() {
        background.draw(batch);

        if (wyvern.getHealth() <= 0) {
            font.setColor(Color.RED);
            font.draw(batch,
                "GAME OVER",
                20f, Gdx.graphics.getHeight() - 20f
            );
            return;
        }

        for(ParticleEffectPool.PooledEffect effect : explosionEffects) {
            effect.draw(batch);
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        for (Entity entity : entities) {
            entity.draw(batch);
        }
        wyvern.draw(batch);

        font.setColor(Color.RED);
        font.draw(batch,
            "HEALTH: " + wyvern.getHealth(),
            20f, Gdx.graphics.getHeight() - 20f
        );
        healthBar.draw(batch);

        font.setColor(Color.ORANGE);
        font.draw(batch,
            "CHARGES: " + wyvern.getCharges(),
            20f, Gdx.graphics.getHeight() - 110f
        );

        font.setColor(Color.GREEN);
        font.draw(batch,
            "SOULS: " + wyvern.getSoulsCollected(),
            20f, Gdx.graphics.getHeight() - 150f
        );

        // font.setColor(Color.BLACK);
        // font.draw(batch,
        //     "Peak Seagulls: " + entityPools.getPool(Seagull.class).peak,
        //     20f, Gdx.graphics.getHeight() - 200f
        // );
    }

    private void drawDebug() {
        GlyphLayout layout = new GlyphLayout(font, "FPS:" + Gdx.graphics.getFramesPerSecond());
        font.setColor(Color.YELLOW);
        font.draw(batch, layout, Gdx.graphics.getWidth() - layout.width, Gdx.graphics.getHeight() - 50);

        // number of rendering calls, ever; will not be reset unless set manually
        font.setColor(Color.YELLOW);
        font.draw(batch, "RC:" + batch.totalRenderCalls, Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 20);

        memoryInfo.render(batch, font);
    }

    private void spawnFireball(float elapsedTime) {
        if (elapsedTime - fireballSpawnTime > Fireball.SPAWN_TIME && wyvern.getCharges() > 0) {
            Fireball fireball = entityPools.obtain(Fireball.class);
            fireball.fire(wyvern);
            fireball.trail = fireTrailPool.obtain();
            entities.add(fireball);
            fireballSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
            wyvern.setCharges(wyvern.getCharges() - 1);
        }
    }

    private void spawnSeagull(float elapsedTime) {
        if (elapsedTime - seagullSpawnTime > Seagull.SPAWN_TIME) {
            entities.add(entityPools.obtain(Seagull.class));
            seagullSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        }
    }

    private void spawnSoul(float elapsedTime) {
        if (elapsedTime - soulSpawnTime > Soul.SPAWN_TIME) {
            entities.add(entityPools.obtain(Soul.class));
            soulSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        }
    }

    private void spawnBallista(float elapsedTime) {
        if (elapsedTime - ballistaSpawnTime > Ballista.SPAWN_TIME) {
            entities.add(entityPools.obtain(Ballista.class));
            ballistaSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        healthBar.dispose();
        Player.dispose();
        Seagull.dispose();
        Soul.dispose();
        Ballista.dispose();
        Fireball.dispose();
        background.dispose();
        fireTrail.dispose();
        explosion.dispose();
        entityPools.clear();
    }
}
