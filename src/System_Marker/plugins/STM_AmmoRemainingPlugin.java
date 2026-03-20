package System_Marker.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;
import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class STM_AmmoRemainingPlugin extends STM_EveryFramePlugin {
    public static final String ID = "STM_AmmoRemainingPlugin";

    private boolean ammoRemainingMarkerRotateWithShip = true;
    private float ammoRemainingMarkerFontSize = 12f;
    private float ammoRemainingMarkerDisplayingTime = 3f;
    private float ammoRemainingMarkerFadingTime = 0.2f;

    private float ammoRemainingMarkerRingRadiusSmall = 10f;
    private float ammoRemainingMarkerRingRadiusMedium = 14f;
    private float ammoRemainingMarkerRingRadiusLarge = 20f;
    private float ammoRemainingMarkerRingThickness = 2f;
    private float ammoRemainingMarkerRingAlphaMain = 0.8f;
    private float ammoRemainingMarkerRingAlphaBack = 0.1f;

    private String ammoRemainingRepairMarkerMode = "ring";


    private Color ammoRemainingMarkerColor = Misc.getPositiveHighlightColor();
    private Color ammoRemainingMarkerColorHalf = Misc.getHighlightColor();
    private Color ammoRemainingMarkerColorEmpty = Misc.getNegativeHighlightColor();

    private final Map<WeaponAPI, LazyFont.DrawableString> drawables = new HashMap<>();

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");
            ammoRemainingMarkerRotateWithShip = getBoolean(cfg, "ammoRemainingMarkerRotateWithShip", ammoRemainingMarkerRotateWithShip);
            ammoRemainingMarkerFontSize = (float) getDouble(cfg, "ammoRemainingMarkerFontSize", ammoRemainingMarkerFontSize);
            ammoRemainingMarkerDisplayingTime = (float) getDouble(cfg, "ammoRemainingMarkerDisplayingTime", ammoRemainingMarkerDisplayingTime);
            ammoRemainingMarkerFadingTime = (float) getDouble(cfg, "ammoRemainingMarkerFadingTime", ammoRemainingMarkerFadingTime);

            ammoRemainingMarkerRingRadiusSmall = (float) getDouble(cfg, "ammoRemainingMarkerRingRadiusSmall", ammoRemainingMarkerRingRadiusSmall);
            ammoRemainingMarkerRingRadiusMedium = (float) getDouble(cfg, "ammoRemainingMarkerRingRadiusMedium", ammoRemainingMarkerRingRadiusMedium);
            ammoRemainingMarkerRingRadiusLarge = (float) getDouble(cfg, "ammoRemainingMarkerRingRadiusLarge", ammoRemainingMarkerRingRadiusLarge);
            ammoRemainingMarkerRingThickness = (float) getDouble(cfg, "ammoRemainingMarkerRingThickness", ammoRemainingMarkerRingThickness);
            ammoRemainingMarkerRingAlphaMain = (float) getDouble(cfg, "ammoRemainingMarkerRingAlphaMain", ammoRemainingMarkerRingAlphaMain);
            ammoRemainingMarkerRingAlphaBack = (float) getDouble(cfg, "ammoRemainingMarkerRingAlphaBack", ammoRemainingMarkerRingAlphaBack);

            ammoRemainingRepairMarkerMode = getString(cfg, "ammoRemainingRepairMarkerMode", ammoRemainingRepairMarkerMode);

            if (overrideColors(cfg, "ammoRemainingMarkerOverrideColors", false)) {
                ammoRemainingMarkerColor = getColor(cfg, "ammoRemainingMarkerColor", ammoRemainingMarkerColor);
                ammoRemainingMarkerColorHalf = getColor(cfg, "ammoRemainingMarkerColorHalf", ammoRemainingMarkerColorHalf);
                ammoRemainingMarkerColorEmpty = getColor(cfg, "ammoRemainingMarkerColorEmpty", ammoRemainingMarkerColorEmpty);
            }
        } catch (Exception ignored) {
        }

        loadFont(engine);
        engine.getCustomData().put(ID, new LocalData());
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
            if (value.lockTime > 0f) value.lockTime -= amount / ammoRemainingMarkerDisplayingTime;
            else if (value.fading > 0f) value.fading -= amount / ammoRemainingMarkerFadingTime;
        } else {
            value.target = ship.getShipTarget();
            value.lockTime = 1f;
            value.fading = 1f;
        }
        if (value.target == null || value.fading <= 0f) return;

        if (ammoRemainingRepairMarkerMode.equals("ring")) {
            for (WeaponAPI weapon : value.target.getAllWeapons()) {
                if (!weapon.usesAmmo()) continue;
                float angle = ammoRemainingMarkerRotateWithShip ? value.target.getFacing() : 90f;
                float maxAmmo = weapon.getMaxAmmo();
                float ammoLevel = maxAmmo >= 1f ? weapon.getAmmo() / maxAmmo : 0f;
                Color color = getMixedColor(ammoRemainingMarkerColorHalf, clamp(1 - ammoLevel * 2f), ammoRemainingMarkerColorEmpty);
                if (ammoLevel >= 0.5f)
                    color = getMixedColor(ammoRemainingMarkerColor, clamp(2 - ammoLevel * 2f), ammoRemainingMarkerColorHalf);
                float size = weapon.getSize() == WeaponAPI.WeaponSize.SMALL ? ammoRemainingMarkerRingRadiusSmall : ammoRemainingMarkerRingRadiusMedium;
                float thickness = Math.max(1f, ammoRemainingMarkerRingThickness / engine.getViewport().getViewMult());
                if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) size = ammoRemainingMarkerRingRadiusLarge;
                glIn(engine.getViewport());
                drawArcFacing(color, value.fading * ammoRemainingMarkerRingAlphaBack, 360f, weapon.getLocation(), size / engine.getViewport().getViewMult(), angle, thickness);
                drawArcFacing(color, value.fading * ammoRemainingMarkerRingAlphaMain, ammoLevel * 360f, weapon.getLocation(), size / engine.getViewport().getViewMult(), angle, thickness);
                glOut();
            }
        }
    }

    @Override
    public void renderInUICoords(ViewportAPI view) {
        if (hide()) return;
        if (ammoRemainingRepairMarkerMode.equals("ring")) return;

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
            if (ammoRemainingRepairMarkerMode.equals("percentage")) {
                drawFont(engine, drawables.get(weapon), Math.round(ammoLevel * 100f) + "%", color, angle, weapon.getLocation(), ammoRemainingMarkerFontSize * value.fading);
            } else {
                drawFont(engine, drawables.get(weapon), weapon.getAmmo() + "", color, angle, weapon.getLocation(), ammoRemainingMarkerFontSize * value.fading);
            }
        }
    }

    private static class LocalData {
        AmmoData ammoData = new AmmoData();

        LocalData() {
        }
    }

    private static class AmmoData {
        ShipAPI target = null;
        float lockTime = 1f;
        float fading = 1f;

        AmmoData() {
        }
    }
}