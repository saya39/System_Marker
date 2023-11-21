package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class STM_AmmoRemainingPlugin extends STM_EveryFramePlugin {
    public static final String ID = "STM_AmmoRemainingPlugin";

    private static boolean ammoRemainingMarkerRotateWithShip = false;
    private static float ammoRemainingMarkerFontSize = 12f;
    private static float ammoRemainingMarkerDisplayingTime = 3f;
    private static float ammoRemainingMarkerFadingTime = 0.2f;

    private static Color ammoRemainingMarkerColor = Misc.getPositiveHighlightColor();
    private static Color ammoRemainingMarkerColorHalf = Misc.getHighlightColor();
    private static Color ammoRemainingMarkerColorEmpty = Misc.getNegativeHighlightColor();

    private final Map<WeaponAPI, LazyFont.DrawableString> drawables = new HashMap<>();

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        loadFont(engine);
        engine.getCustomData().put(ID, new LocalData());
        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");
            ammoRemainingMarkerRotateWithShip = getBoolean(cfg, "ammoRemainingMarkerRotateWithShip", ammoRemainingMarkerRotateWithShip);
            ammoRemainingMarkerFontSize = (float) getDouble(cfg, "ammoRemainingMarkerFontSize", ammoRemainingMarkerFontSize);
            ammoRemainingMarkerDisplayingTime = (float) getDouble(cfg, "ammoRemainingMarkerDisplayingTime", ammoRemainingMarkerDisplayingTime);
            ammoRemainingMarkerFadingTime = (float) getDouble(cfg, "ammoRemainingMarkerFadingTime", ammoRemainingMarkerFadingTime);
            if (overrideColors(cfg, "ammoRemainingMarkerOverrideColors", false)) {
                ammoRemainingMarkerColor = getColor(cfg, "ammoRemainingMarkerColor", ammoRemainingMarkerColor);
                ammoRemainingMarkerColorHalf = getColor(cfg, "ammoRemainingMarkerColorHalf", ammoRemainingMarkerColorHalf);
                ammoRemainingMarkerColorEmpty = getColor(cfg, "ammoRemainingMarkerColorEmpty", ammoRemainingMarkerColorEmpty);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (hide()) return;

        ShipAPI ship = engine.getPlayerShip();
        AmmoData value = ((LocalData) engine.getCustomData().get(ID)).ammoData;

        if (ship.getShipTarget() == null || !ship.getShipTarget().isAlive() || ship.getShipTarget().isHulk() || ship.getShipTarget().getOwner() != 0) {
            value.target = null;
            return;
        }
        if (value.target != null && ship.getShipTarget().equals(value.target)) {
            if (value.lockTime > 0f) value.lockTime -= amount;
            else if (value.fading > 0f) value.fading -= amount / ammoRemainingMarkerFadingTime;
        } else {
            value.target = ship.getShipTarget();
            value.lockTime = ammoRemainingMarkerDisplayingTime;
            value.fading = 1f;
        }
    }

    @Override
    public void renderInUICoords(ViewportAPI view) {
        if (hide()) return;
        AmmoData value = ((LocalData) engine.getCustomData().get(ID)).ammoData;
        if (value.target == null || value.fading <= 0f) return;
        for (WeaponAPI weapon : value.target.getAllWeapons()) {
            if (!weapon.usesAmmo()) continue;
            float angle = ammoRemainingMarkerRotateWithShip ? value.target.getFacing() : 90f;
            float maxAmmo = weapon.getMaxAmmo();
            float ammoLevel = maxAmmo >= 1f ? weapon.getAmmo() / maxAmmo : 0f;
            Color color = getMixedColor(ammoRemainingMarkerColorHalf, clamp(1 - ammoLevel * 2f), ammoRemainingMarkerColorEmpty);
            if (ammoLevel >= 0.5f)
                color = getMixedColor(ammoRemainingMarkerColor, clamp(2 - ammoLevel * 2f), ammoRemainingMarkerColorHalf);

            if (!drawables.containsKey(weapon)) {
                drawables.put(weapon, null);
            }
            drawFont(engine, drawables.get(weapon), weapon.getAmmo() + "", color, value.fading, angle, weapon.getLocation(), 12f * value.fading);
        }
    }

    private void drawFont(CombatEngineAPI engine, LazyFont.DrawableString drawable, String text, Color color, float alpha, float angle, Vector2f loc, float size) {
        LazyFont font = (LazyFont) engine.getCustomData().get(STM_EveryFramePlugin.ID + "_font");
        if (font == null) return;
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(Math.round(alpha * 255f), 255)));

        if (drawable == null) {
            drawable = font.createText(text, color, size);
        } else {
            if (!drawable.getText().equals(text)) {
                drawable.setText(text);
            }
            if (!drawable.getBaseColor().equals(color)) {
                drawable.setBaseColor(color);
            }
            if (drawable.getFontSize() != size) {
                drawable.setFontSize(size);
            }
        }

        loc = new Vector2f(engine.getViewport().convertWorldXtoScreenX(loc.x), engine.getViewport().convertWorldYtoScreenY(loc.y));
        drawable.drawAtAngle(loc.x + rotate(new Vector2f(-drawable.getWidth() / 2f, drawable.getHeight() / 2f), angle - 90f).x, loc.y + rotate(new Vector2f(-drawable.getWidth() / 2f, drawable.getHeight() / 2f), angle - 90f).y, angle - 90f);
    }

    private static class LocalData {
        AmmoData ammoData = new AmmoData();

        LocalData() {
        }
    }

    private static class AmmoData {
        ShipAPI target = null;
        float lockTime = ammoRemainingMarkerDisplayingTime;
        float fading = 1f;

        AmmoData() {
        }
    }
}