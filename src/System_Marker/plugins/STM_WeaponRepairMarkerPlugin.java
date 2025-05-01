package System_Marker.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class STM_WeaponRepairMarkerPlugin extends STM_EveryFramePlugin {
    private float weaponRepairMarkerSizeSmall = 28f;
    private float weaponRepairMarkerSizeMedium = 34f;
    private float weaponRepairMarkerSizeLarge = 48f;
    private float weaponRepairMarkerAlpha = 0.6f;
    private boolean weaponRepairMarkerEnableEnemy = false;

    private Color weaponRepairMarkerColorFriendly = Misc.getPositiveHighlightColor();
    private Color weaponRepairMarkerColorAlly = Misc.getHighlightColor();
    private Color weaponRepairMarkerColorEnemy = Misc.getNegativeHighlightColor();

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");

            weaponRepairMarkerSizeSmall = (float) getDouble(cfg, "weaponRepairMarkerSizeSmall", weaponRepairMarkerSizeSmall);
            weaponRepairMarkerSizeMedium = (float) getDouble(cfg, "weaponRepairMarkerSizeMedium", weaponRepairMarkerSizeMedium);
            weaponRepairMarkerSizeLarge = (float) getDouble(cfg, "weaponRepairMarkerSizeLarge", weaponRepairMarkerSizeLarge);
            weaponRepairMarkerAlpha = (float) getDouble(cfg, "weaponRepairMarkerAlpha", weaponRepairMarkerAlpha);
            weaponRepairMarkerEnableEnemy = getBoolean(cfg, "weaponRepairMarkerEnableEnemy", weaponRepairMarkerEnableEnemy);
            if (overrideColors(cfg, "weaponRepairMarkerOverrideColors", false)) {
                weaponRepairMarkerColorFriendly = getColor(cfg, "weaponRepairMarkerColorFriendly", weaponRepairMarkerColorFriendly);
                weaponRepairMarkerColorAlly = getColor(cfg, "weaponRepairMarkerColorAlly", weaponRepairMarkerColorAlly);
                weaponRepairMarkerColorEnemy = getColor(cfg, "weaponRepairMarkerColorEnemy", weaponRepairMarkerColorEnemy);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (hide()) return;
        ShipAPI ship = engine.getPlayerShip();
        if (ship == null || !ship.isAlive()) return;

        List<WeaponAPI> mapPlayer = new ArrayList<>();
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon == null || weapon.isDecorative()) continue;
            if (!weapon.isDisabled() || weapon.isPermanentlyDisabled()) continue;
            mapPlayer.add(weapon);
        }
        List<WeaponAPI> mapTarget = new ArrayList<>();
        ShipAPI target = ship.getShipTarget();
        if (target != null && target.isAlive() && (target.getOwner() == ship.getOwner() || ship.isAlly() || weaponRepairMarkerEnableEnemy)) {
            for (WeaponAPI weapon : target.getAllWeapons()) {
                if (weapon == null || weapon.isDecorative()) continue;
                if (!weapon.isDisabled() || weapon.isPermanentlyDisabled()) continue;
                mapTarget.add(weapon);
            }
        }

        ViewportAPI viewport = engine.getViewport();
        glIn(viewport);
        if (!mapPlayer.isEmpty()) {
            for (WeaponAPI weapon : mapPlayer) {
                drawWeaponRepair(weapon, weaponRepairMarkerColorFriendly, weaponRepairMarkerAlpha);
            }
        }
        if (!mapTarget.isEmpty()) {
            Color color = target.getOwner() == 0 ? weaponRepairMarkerColorFriendly : weaponRepairMarkerColorEnemy;
            if (target.isAlly()) color = weaponRepairMarkerColorAlly;
            for (WeaponAPI weapon : mapTarget) {
                drawWeaponRepair(weapon, color, weaponRepairMarkerAlpha);
            }
        }
        glOut();
    }

    private void drawWeaponRepair(WeaponAPI weapon, Color color, float alpha) {
        ShipAPI ship = weapon.getShip();
        Vector2f loc = new Vector2f(weapon.getLocation());
        if (!engine.isPaused()) {
            float aVel = (float) (Math.toRadians(ship.getAngularVelocity()));
            Vector2f sLocWeaponF = new Vector2f(-weapon.getSlot().getLocation().y, weapon.getSlot().getLocation().x);
            float t = engine.getElapsedInLastFrame() * ship.getMutableStats().getTimeMult().getModifiedValue();
            loc = new Vector2f(loc.x - (ship.getVelocity().x + rotate(sLocWeaponF, ship.getFacing()).x * aVel) * t, loc.y - (ship.getVelocity().y + rotate(sLocWeaponF, ship.getFacing()).y * aVel) * t);
        }
        float level = weapon.getCurrHealth() / weapon.getMaxHealth();
        float size = weapon.getSize() == WeaponAPI.WeaponSize.SMALL ? weaponRepairMarkerSizeSmall : weaponRepairMarkerSizeMedium;
        if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) size = weaponRepairMarkerSizeLarge;
        Vector2f[] vecs = {new Vector2f(0f, 8f), new Vector2f(4f, 27f), new Vector2f(0f, 30f), new Vector2f(-4f, 27f)};

        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) Math.max(0, Math.min(Math.round(alpha * 255f), 255)));
        GL11.glLineWidth(2f);
        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < Math.min((int) (level * 10f) + 1, 10); i++) {
            for (Vector2f vec : vecs) {
                GL11.glVertex2f(loc.x + rotate(vec, weapon.getShip().getFacing() - 90f - i * 36f).x * size / 80f, loc.y + rotate(vec, weapon.getShip().getFacing() - 90f - i * 36f).y * size / 80f);
            }
        }
        GL11.glEnd();
    }
}