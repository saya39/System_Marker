package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class STM_TorpedoMarkerPlugin extends STM_EveryFramePlugin {
    public static final String ID = "STM_TorpedoMarkerPlugin";

    private static int torpedoMarkerPerFrame = 1;
    private static String torpedoMarkerMode = "bound"; // bound, rangeRough
    private static float torpedoMarkerSizeMult = 1f;
    private static float torpedoMarkerThickness = 2f;
    private static float torpedoMarkerAlpha = 2f;

    private static boolean torpedoMarkerEnableCountMissile = true;
    private static float torpedoMarkerMissileWithinAngle = 90f;

    private static Color torpedoMarkerColorAlarm = Misc.getHighlightColor();
    private static Color torpedoMarkerColorDanger = Misc.getNegativeHighlightColor();

    private static float mineMarkerSizeMult = 1f;
    private static float mineMarkerAlpha = 1f;
    private static float mineMarkerBlinkRangeMult = 1f;

    private static Color mineMarkerColor = Misc.getNegativeHighlightColor();

    private static boolean enableAlarm = true;

    private static float alarmVolumeMult = 1f;
    private static float alarmDamageLv1 = 2000f;
    private static float alarmDamageLv2 = 4000f;
    private static float alarmDamageLv3 = 8000f;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        engine.getCustomData().put(ID, new LocalData());

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");

            torpedoMarkerPerFrame = getInt(cfg, "torpedoMarkerPerFrame", torpedoMarkerPerFrame);
            torpedoMarkerMode = getString(cfg, "torpedoMarkerMode", torpedoMarkerMode);
            torpedoMarkerSizeMult = (float) getDouble(cfg, "torpedoMarkerSizeMult", torpedoMarkerSizeMult);
            torpedoMarkerThickness = (float) getDouble(cfg, "torpedoMarkerThickness", torpedoMarkerThickness);
            torpedoMarkerAlpha = (float) getDouble(cfg, "torpedoMarkerAlpha", torpedoMarkerAlpha);
            torpedoMarkerEnableCountMissile = getBoolean(cfg, "torpedoMarkerEnableCountMissile", torpedoMarkerEnableCountMissile);
            torpedoMarkerMissileWithinAngle = (float) getDouble(cfg, "torpedoMarkerMissileWithinAngle", torpedoMarkerMissileWithinAngle);

            if (overrideColors(cfg, "torpedoMarkerOverrideColors", false)) {
                torpedoMarkerColorAlarm = getColor(cfg, "torpedoMarkerColorAlarm", torpedoMarkerColorAlarm);
                torpedoMarkerColorDanger = getColor(cfg, "torpedoMarkerColorDanger", torpedoMarkerColorDanger);
            }

            mineMarkerSizeMult = (float) getDouble(cfg, "mineMarkerSizeMult", mineMarkerSizeMult);
            mineMarkerAlpha = (float) getDouble(cfg, "mineMarkerAlpha", mineMarkerAlpha);
            mineMarkerBlinkRangeMult = (float) getDouble(cfg, "mineMarkerBlinkRangeMult", mineMarkerBlinkRangeMult);
            if (overrideColors(cfg, "mineMarkerOverrideColors", false)) {
                mineMarkerColor = getColor(cfg, "mineMarkerColor", mineMarkerColor);
            }

            enableAlarm = getBoolean(cfg, "enableAlarm", enableAlarm);
            alarmVolumeMult = (float) getDouble(cfg, "alarmVolumeMult", alarmVolumeMult);
            alarmDamageLv1 = (float) getDouble(cfg, "alarmDamageLv1", alarmDamageLv1);
            alarmDamageLv2 = (float) getDouble(cfg, "alarmDamageLv2", alarmDamageLv2);
            alarmDamageLv3 = (float) getDouble(cfg, "alarmDamageLv3", alarmDamageLv3);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (hide()) return;
        if (!enableTorpedoMarker && !enableMineMarker) return;

        ViewportAPI viewport = engine.getViewport();
        float alphaMult = viewport.getAlphaMult();
        if (alphaMult <= 0f) return;

        ShipAPI ship = engine.getPlayerShip();
        if(engine.getPlayerShip() == null) return;

        float dangerClass = 0f;

        if (!engine.getCustomData().containsKey(ID)) return;
        LocalData localData = (LocalData) engine.getCustomData().get(ID);
        Map<MissileAPI, Float> torpedoList = localData.torpedoList;
        Map<MissileAPI, Float> missileList = localData.missileList;
        Map<MissileAPI, MineData> mineData = localData.mineData;

        if (localData.framePassed + 1 >= torpedoMarkerPerFrame){
            localData.framePassed = 0;
            torpedoList.clear();
            missileList.clear();

            for (MissileAPI missile : engine.getMissiles()) {
                if (!engine.isEntityInPlay(missile) || missile.getProjectileSpecId() == null || missile.didDamage() || missile.isFading()) continue;
                if (missile.getSource() != null && missile.getSource().equals(ship)) continue;
                if (missile.getSource() != null && ship.isShipWithModules() && ship.getChildModulesCopy().contains(missile.getSource())) continue;

                putTorpedoList(missile, torpedoList, ship);
                putMissileList(missile, missileList, ship);
                putMineData(missile, mineData, ship);
            }

        } else{
            localData.framePassed += 1;
        }

        glIn(viewport);
        dangerClass += drawTorpedoMarkerList(torpedoList, torpedoMarkerAlpha * alphaMult, viewport);
        dangerClass += drawTorpedoMarkerList(missileList, torpedoMarkerAlpha * alphaMult, viewport);
        dangerClass += drawMineMarkerList(mineData, ship, mineMarkerAlpha * alphaMult, viewport, amount);
        glOut();

        playAlarm(dangerClass, ship);
    }

    private void putTorpedoList(MissileAPI missile, Map<MissileAPI, Float> torpedoList, ShipAPI ship) {
        if (!enableTorpedoMarker || missile.isGuided() || missile.isMine() || torpedoList.containsKey(missile)) return;

        float sec;
        if(torpedoMarkerMode.equals("rangeRough")) sec = getSecToHitRangeRough(missile, ship);
        else sec = getSecToHitBound(missile, ship);
//        else if(torpedoMarkerMode.equals("rangeAccurate")) sec = getSecToHitRangeAccurate(missile, ship);
        if(sec <= 5f){
            torpedoList.put(missile, sec);
        }
    }

    private void putMissileList(MissileAPI missile, Map<MissileAPI, Float> missileList, ShipAPI ship) {
        if (!enableTorpedoMarker || !torpedoMarkerEnableCountMissile  || !missile.isGuided() || missile.isMine() || missileList.containsKey(missile)) return;
        if(!missile.getEngineController().isDisabled() && missile.getOwner() == engine.getPlayerShip().getOwner()) return;
        float angle = (float) clampIntoPM180(getAngle(missile.getLocation(), ship.getLocation()) - missile.getFacing());
        if(Math.abs(angle) > torpedoMarkerMissileWithinAngle) return;
        float sec = getSecToHitRangeRough(missile, ship);
        if (sec <= 5f) {
            missileList.put(missile, sec);
        }
    }

    private void putMineData(MissileAPI missile, Map<MissileAPI, MineData> mineData, ShipAPI ship) {
        if (!enableMineMarker || !missile.isMine() || mineData.containsKey(missile)) return;
        if (Vector2f.sub(ship.getLocation(), missile.getLocation(), null).length() - ship.getCollisionRadius() >= missile.getMineExplosionRange())
            return;
        mineData.put(missile, new MineData());
    }

    private boolean getCollides(MissileAPI missile, ShipAPI ship){
        Vector2f from = missile.getLocation();
        if (CollisionUtils.getCollides(from, sumVector(from, missile.getVelocity(), 5f), ship.getLocation(), ship.getCollisionRadius())) return true;
        else if (!ship.isShipWithModules()) return false;
        for (ShipAPI module : ship.getChildModulesCopy()) {
            if (CollisionUtils.getCollides(from, sumVector(from, missile.getVelocity(), 5f), module.getLocation(), module.getCollisionRadius())) return true;
        }
        return false;
    }

    private float getSecToHitBound(MissileAPI missile, ShipAPI ship) {
        if(!getCollides(missile, ship)) return 10000f;
        Vector2f from = missile.getLocation();
        Vector2f velocity = missile.getVelocity();

        Vector2f point = CollisionUtils.getCollisionPoint(from, sumVector(from, velocity, 5f), ship);
        float sec = point == null ? 10000f : Vector2f.sub(point, from, null).length() / velocity.length();
        if (!ship.isShipWithModules()) return sec;
        for (ShipAPI module : ship.getChildModulesCopy()) {
            point = CollisionUtils.getCollisionPoint(from, sumVector(from, velocity, 5f), module);
            sec = point == null ? sec : Math.min(sec, Vector2f.sub(point, from, null).length() / velocity.length());
        }
        return sec;
    }

//    private float getSecToHitRangeAccurate(MissileAPI missile, ShipAPI ship){
//        if(!getCollides(missile, ship)) return 10000f;
//        float OA = Vector2f.sub(ship.getLocation(), missile.getLocation(), null).length();
//        if(OA <= ship.getCollisionRadius()) return 0f;
//        float cos = (float) Math.cos(VectorUtils.getFacing(Vector2f.sub(ship.getLocation(), missile.getLocation(), null)) - VectorUtils.getFacing(missile.getVelocity()));
//
//        float dist = OA * cos - (float) Math.sqrt(ship.getCollisionRadius() * ship.getCollisionRadius() - OA * OA * (1f - cos * cos));
//        if(!ship.isShipWithModules()) return (dist / missile.getMoveSpeed());
//        for(ShipAPI module : ship.getChildModulesCopy()){
//            OA = Vector2f.sub(module.getLocation(), missile.getLocation(), null).length();
//            if(OA <= module.getCollisionRadius()) return 0f;
//            cos = (float) Math.cos(VectorUtils.getFacing(Vector2f.sub(module.getLocation(), missile.getLocation(), null)) - VectorUtils.getFacing(missile.getVelocity()));
//            float distM = OA * cos - (float) Math.sqrt(module.getCollisionRadius() * module.getCollisionRadius() - OA * OA * (1f - cos * cos));
//            if(distM < dist) dist = distM;
//        }
//        return (dist / missile.getMoveSpeed());
//    }

    private float getSecToHitRangeRough(MissileAPI missile, ShipAPI ship){
        if(!getCollides(missile, ship)) return 10000f;
        float dist = Vector2f.sub(ship.getLocation(), missile.getLocation(), null).length() - ship.getCollisionRadius();
        if(dist <= 0f) return 0f;
        if(!ship.isShipWithModules()) return (dist / missile.getMoveSpeed());
        for(ShipAPI module : ship.getChildModulesCopy()){
            float distM = Vector2f.sub(module.getLocation(), missile.getLocation(), null).length() - module.getCollisionRadius();
            if(distM <= 0f) return 0f;
            if(distM < dist) dist = distM;
        }
        return (dist / missile.getMoveSpeed());
    }

    private float drawTorpedoMarkerList(Map<MissileAPI, Float> torpedoList, float alphaMult, ViewportAPI viewport) {
        if (torpedoList.isEmpty()) return 0f;
        float dangerClass = 0f;
        List<MissileAPI> toRemoveList = new ArrayList<>();
        for (Map.Entry<MissileAPI, Float> entry : torpedoList.entrySet()) {
            MissileAPI missile = entry.getKey();
            float sec = entry.getValue();
            if (missile == null || missile.isExpired() || missile.didDamage()) {
                toRemoveList.add(missile);
                continue;
            }
            dangerClass += drawTorpedoMarker(missile, alphaMult, viewport, sec);
        }
        if(!toRemoveList.isEmpty()) {
            for(MissileAPI toRemove : toRemoveList){
                torpedoList.remove(toRemove);
            }
        }
        return dangerClass;
    }

    private float drawTorpedoMarker(MissileAPI missile, float alphaMult, ViewportAPI viewport, float sec) {
        float dangerClass = 0f;
        if (sec > 5f) return 0f;
        Color color;
        float alpha;
        if (sec > 3f) {
            color = torpedoMarkerColorAlarm;
            alpha = getLevel(sec, 5f, 3f) * alphaMult;
        } else if (sec > 1f) {
            color = getMixedColor(torpedoMarkerColorAlarm, getLevel(sec, 3f, 1f), torpedoMarkerColorDanger);
            alpha = alphaMult;
            dangerClass += missile.getDamageAmount();
        } else {
            color = torpedoMarkerColorDanger;
            alpha = alphaMult;
            dangerClass += missile.getDamageAmount();
        }
        if (alpha <= 0f) return dangerClass;

        Vector2f loc = new Vector2f(missile.getLocation());
        if (!engine.isPaused()) {
            float aVel = (float) (Math.toRadians(missile.getAngularVelocity()));
            float t = engine.getElapsedInLastFrame();
            loc = new Vector2f(loc.x - (missile.getVelocity().x + rotate(new Vector2f(), missile.getFacing()).x * aVel) * t, loc.y - (missile.getVelocity().y + rotate(new Vector2f(), missile.getFacing()).y * aVel) * t);
        }

        drawArc(color, alpha, 360f, loc, torpedoMarkerSizeMult * missile.getCollisionRadius() / viewport.getViewMult(), 0f, torpedoMarkerThickness / viewport.getViewMult());
        return dangerClass;
    }

    private float drawMineMarkerList(Map<MissileAPI, MineData> mineData, ShipAPI ship, float alphaMult, ViewportAPI viewport, float amount) {
        if (!enableMineMarker || mineData.isEmpty()) return 0f;
        float dangerClass = 0f;
        List<MissileAPI> removeList = new ArrayList<>();
        for (Map.Entry<MissileAPI, MineData> entry : mineData.entrySet()) {
            MissileAPI mine = entry.getKey();
            MineData value = entry.getValue();
            if (mine == null || !engine.isEntityInPlay(mine) || mine.getProjectileSpecId() == null || mine.didDamage() || mine.isFading()) {
                removeList.add(mine);
                continue;
            }

            Vector2f from = mine.getLocation();
            float length = Vector2f.sub(ship.getLocation(), from, null).length() - ship.getCollisionRadius();
            float range = mine.getMineExplosionRange() * mineMarkerBlinkRangeMult;

            if (length > range) {
                value.timer = 2f; //set to max
                continue;
            }

            if (length >= range * 0.5f) {
                dangerClass += mine.getDamageAmount();
                if (!engine.isPaused()) value.timer = Math.min(Math.abs(value.timer) + amount * 8f, 2f); // move to max
            } else {
                dangerClass += mine.getDamageAmount() * 2f;
                if (!engine.isPaused() && value.timer <= -2f) value.timer = 2f;
                else if (!engine.isPaused()) value.timer = Math.max(value.timer - amount * 8f, -2f); // blink
            }
            float glowAlpha = (float) (2f - Math.pow(2f + value.timer, 2)) * 0.5f;
            if (value.timer > 1f) glowAlpha = (float) (2f - Math.pow(2f - value.timer, 2)) * 0.5f;
            else if (value.timer > -1f) glowAlpha = (float) Math.pow(value.timer, 2) * 0.5f;
            Color color = mineMarkerColor;
            float alpha = 1f * alphaMult * glowAlpha;
            if (alpha <= 0f) continue;
            drawMineMarker(color, alpha, mine.getLocation(), mineMarkerSizeMult * mine.getCollisionRadius(), mine.getFacing() - 90f, viewport);
        }
        if(!removeList.isEmpty()){
            for (MissileAPI toRemove : removeList){
                mineData.remove(toRemove);
            }
        }
        return dangerClass;
    }

    private void drawMineMarker(Color color, float alpha, Vector2f loc, float radius, float facing, ViewportAPI viewport) {
        float radiusArc = radius / viewport.getViewMult();
        for (int i = 0; i < 3; i++) {
            drawArc(color, alpha, 60f, loc, radiusArc, i * 120f + 90f - 30f - 180f + facing, 2f / engine.getViewport().getViewMult());
            drawTriangle(color, alpha, getPoint(loc, radius, i * 120f + 90f + facing), radius / 2.5f, i * 120f + 180f + facing);
        }

    }

    private void playAlarm(float dangerClass, ShipAPI ship) {
        if (!enableAlarm || engine.isPaused()) return;
        if (dangerClass >= alarmDamageLv1 && dangerClass <= alarmDamageLv2) {
            Global.getSoundPlayer().playLoop("STM_alarm_lv1", ship, 1f, alarmVolumeMult, ship.getLocation(), ship.getVelocity(), 0f, 0.5f);
        } else if (dangerClass > alarmDamageLv2 && dangerClass <= alarmDamageLv3) {
            Global.getSoundPlayer().playLoop("STM_alarm_lv2", ship, 1f, alarmVolumeMult, ship.getLocation(), ship.getVelocity(), 0f, 0.5f);
        } else if (dangerClass > alarmDamageLv3) {
            Global.getSoundPlayer().playLoop("STM_alarm_lv3", ship, 1f, alarmVolumeMult, ship.getLocation(), ship.getVelocity(), 0f, 0.5f);
        }
    }

    private void drawArc(Color color, float alpha, float arc, Vector2f loc, float radius, float facing, float thickness) {
        GL11.glLineWidth(thickness);
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) Math.max(0, Math.min(Math.round(alpha * 255f), 255)));
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < Math.round(arc); i++) {
            GL11.glVertex2f(getPoint(loc, radius * engine.getViewport().getViewMult(), facing + i).x, getPoint(loc, radius * engine.getViewport().getViewMult(), facing + i).y);
        }
        GL11.glEnd();
    }

    private void drawTriangle(Color color, float alpha, Vector2f loc, float radius, float facing) {
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) Math.max(0, Math.min(Math.round(alpha * 255f), 255)));
        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i = 0; i < 3; i++) {
            GL11.glVertex2f(getPoint(loc, radius, i * 120f + facing + 90f).x, getPoint(loc, radius, i * 120f + facing + 90f).y);
        }
        GL11.glEnd();
    }

    private final static class LocalData {
        private final Map<MissileAPI, Float> torpedoList = new HashMap<>();
        private final Map<MissileAPI, Float> missileList = new HashMap<>();
        private final Map<MissileAPI, MineData> mineData = new HashMap<>();

        private int framePassed = 0;
    }

    private final static class MineData {
        private float timer;

        MineData() {
            this.timer = 2f;
        }
    }
}