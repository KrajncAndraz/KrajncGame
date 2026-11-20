package si.um.feri.krajnc.N2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class KrajncGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;

    private Player wyvern;
    private Array<GameObject> entities;
    private Background background;
    HealthBar healthBar;

    private float seagullSpawnTime;
    private float soulSpawnTime;
    private float fireballSpawnTime;
    private float ballistaSpawnTime;

    @Override
    public void create() {
        batch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("fonts/arial-32.fnt"));

        background = new Background(new Texture(Gdx.files.internal("images/background.jpg")), 200f);

        healthBar = new HealthBar(new Texture(Gdx.files.internal("images/healthBarBorder.png")));

        wyvern = new Player();

        entities = new Array<>();
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);

        if (wyvern.getHealth() > 0) {
            handleInput();
            update(Gdx.graphics.getDeltaTime());
        }

        batch.begin();

        draw();

        batch.end();

        healthBar.render(wyvern);
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) wyvern.moveLeft(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.D)) wyvern.moveRight(Gdx.graphics.getDeltaTime());
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
            if (elapsedTime - fireballSpawnTime > Fireball.SPAWN_TIME && wyvern.getCharges() > 0) {
                entities.add(new Fireball(wyvern.sprite.getBoundingRectangle()));
                fireballSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
                wyvern.setCharges(wyvern.getCharges() - 1);
            }
        }
    }

    private void update(float delta) {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        if (elapsedTime - seagullSpawnTime > Seagull.SPAWN_TIME) {
            entities.add(new Seagull());
            seagullSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        }
        if (elapsedTime - soulSpawnTime > Soul.SPAWN_TIME) {
            entities.add(new Soul());
            soulSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        }
        if (elapsedTime - ballistaSpawnTime > Ballista.SPAWN_TIME) {
            entities.add(new Ballista());
            ballistaSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;
        }

        for (int i = 0; i < entities.size; i++) {
            GameObject entity = entities.get(i);
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
                    GameObject other = entities.get(j);
                    if (other instanceof Ballista && other.isActive() && entity.hasCollided(other)) {
                        entity.onCollision(other);
                        break;
                    }
                }
            }
        }

        for (int i = entities.size - 1; i >= 0; i--) {
            if (!entities.get(i).isActive()) entities.removeIndex(i);
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
        for (GameObject entity : entities) {
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
    }
}
