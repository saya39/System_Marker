package System_Marker.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class STM_EngineRepairMarkerPlugin extends STM_EveryFramePlugin {
    private float engineRepairMarkerAlpha = 0.4f;
    private float engineRepairMarkerSizeMult = 1f;
    private boolean engineRepairMarkerEnableEnemy = false;

    private Color engineRepairMarkerColorFriendly = Misc.getPositiveHighlightColor();
    private Color engineRepairMarkerColorAlly = Misc.getHighlightColor();
    private Color engineRepairMarkerColorEnemy = Misc.getNegativeHighlightColor();

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");

            engineRepairMarkerAlpha = (float) getDouble(cfg, "engineRepairMarkerAlpha", engineRepairMarkerAlpha);
            engineRepairMarkerSizeMult = (float) getDouble(cfg, "engineRepairMarkerSizeMult", engineRepairMarkerSizeMult);
            engineRepairMarkerEnableEnemy = getBoolean(cfg, "engineRepairMarkerEnableEnemy", engineRepairMarkerEnableEnemy);
            if (overrideColors(cfg, "engineRepairMarkerOverrideColors", false)) {
                engineRepairMarkerColorFriendly = getColor(cfg, "engineRepairMarkerColorFriendly", engineRepairMarkerColorFriendly);
                engineRepairMarkerColorAlly = getColor(cfg, "engineRepairMarkerColorAlly", engineRepairMarkerColorAlly);
                engineRepairMarkerColorEnemy = getColor(cfg, "engineRepairMarkerColorEnemy", engineRepairMarkerColorEnemy);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (hide()) return;
        ShipAPI ship = engine.getPlayerShip();
        if (ship == null || !ship.isAlive()) return;

        List<ShipEngineControllerAPI.ShipEngineAPI> mapPlayer = new ArrayList<>();
        for (ShipEngineControllerAPI.ShipEngineAPI shipEngine : ship.getEngineController().getShipEngines()) {
            if (shipEngine.isDisabled() && !shipEngine.isPermanentlyDisabled()) mapPlayer.add(shipEngine);
        }
        List<ShipEngineControllerAPI.ShipEngineAPI> mapTarget = new ArrayList<>();
        ShipAPI target = ship.getShipTarget();
        if (target != null && target.isAlive() && (target.getOwner() == ship.getOwner() || ship.isAlly() || engineRepairMarkerEnableEnemy)) {
            for (ShipEngineControllerAPI.ShipEngineAPI shipEngine : target.getEngineController().getShipEngines()) {
                if (shipEngine.isDisabled() && !shipEngine.isPermanentlyDisabled()) mapTarget.add(shipEngine);
            }
        }

        ViewportAPI viewport = engine.getViewport();
        glIn(viewport);
        if (!mapPlayer.isEmpty()) {
            for (ShipEngineControllerAPI.ShipEngineAPI shipEngine : mapPlayer) {
                drawEngineRepair(ship, shipEngine, engineRepairMarkerColorFriendly, engineRepairMarkerAlpha);
            }
        }
        if (!mapTarget.isEmpty()) {
            Color color = target.getOwner() == 0 ? engineRepairMarkerColorFriendly : engineRepairMarkerColorEnemy;
            if (target.isAlly()) color = engineRepairMarkerColorAlly;
            for (ShipEngineControllerAPI.ShipEngineAPI shipEngine : mapTarget) {
                drawEngineRepair(target, shipEngine, color, engineRepairMarkerAlpha);
            }
        }
        glOut();
    }

    private void drawEngineRepair(ShipAPI ship, ShipEngineControllerAPI.ShipEngineAPI shipEngine, Color color, float alpha) {
        Vector2f loc = new Vector2f(shipEngine.getLocation());
        EngineSlotAPI engineSlot = shipEngine.getEngineSlot();

        if (!engine.isPaused()) {
            float aVel = (float) (Math.toRadians(ship.getAngularVelocity()));
            Vector2f sLocWeaponF = new Vector2f(-shipEngine.getLocation().y + ship.getLocation().y, shipEngine.getLocation().x - ship.getLocation().x);
            float t = engine.getElapsedInLastFrame() * ship.getMutableStats().getTimeMult().getModifiedValue();
            loc = new Vector2f(loc.x - (ship.getVelocity().x + getPoint(sLocWeaponF, sLocWeaponF.length(), ship.getFacing()).x * aVel) * t, loc.y - (ship.getVelocity().y + getPoint(sLocWeaponF, sLocWeaponF.length(), ship.getFacing()).y * aVel) * t);
        }
        float level = shipEngine.getHitpoints() / shipEngine.getMaxHitpoints();

        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) Math.max(0, Math.min(Math.round(alpha * 255f), 255)));
        GL11.glLineWidth(1f / engine.getViewport().getViewMult() * engineSlot.getLength() / 20f * engineRepairMarkerSizeMult);
        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < Math.max(0, Math.min(10 - (int) (level * 10f), 10)); i++) {
            Vector2f[] vectors = {new Vector2f(-engineSlot.getWidth() / 2f * engineRepairMarkerSizeMult, 2f * (i + 1) * engineSlot.getLength() / 20f * engineRepairMarkerSizeMult), new Vector2f(engineSlot.getWidth() / 2f * engineRepairMarkerSizeMult, 2f * (i + 1) * engineSlot.getLength() / 20f * engineRepairMarkerSizeMult)};
            for (Vector2f vector : vectors) {
                GL11.glVertex2f(loc.x + rotate(vector, ship.getFacing() + engineSlot.getAngle() - 90f).x, loc.y + rotate(vector, ship.getFacing() + engineSlot.getAngle() - 90f).y);
            }
        }
        GL11.glEnd();
    }
}
