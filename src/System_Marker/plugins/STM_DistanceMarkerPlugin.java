package System_Marker.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONObject;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class STM_DistanceMarkerPlugin extends STM_EveryFramePlugin {
    public static final String ID = "STM_WeaponInfoMarkerPlugin";

    private float weaponRepairMarkerSizeSmall = 28f;
    private float weaponRepairMarkerSizeMedium = 34f;
    private float weaponRepairMarkerSizeLarge = 48f;

    private Color weaponRepairMarkerColorFriendly = Misc.getPositiveHighlightColor();
    private Color weaponRepairMarkerColorAlly = Misc.getHighlightColor();
    private Color weaponRepairMarkerColorEnemy = Misc.getNegativeHighlightColor();

    private boolean showReticle = true;
    private boolean rotateWithFluxReticle = true;
    private float systemMarkerAmmoFontSize = 12f;
    private float systemMarkerAmmoShiftX = 32f;
    private float systemMarkerAmmoShiftY = 0f;

    private Color systemMarkerAmmoColorNormal = new Color(50, 255, 255);

    private LazyFont.DrawableString drawableDistance = null;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");
            rotateWithFluxReticle = getBoolean(cfg, "rotateWithFluxReticle", rotateWithFluxReticle);
            systemMarkerAmmoFontSize = (float) getDouble(cfg, "systemMarkerAmmoFontSize", systemMarkerAmmoFontSize);
            systemMarkerAmmoShiftX = (float) getDouble(cfg, "systemMarkerAmmoShiftX", systemMarkerAmmoShiftX);
            systemMarkerAmmoShiftY = (float) getDouble(cfg, "systemMarkerAmmoShiftY", systemMarkerAmmoShiftY);

            weaponRepairMarkerSizeSmall = (float) getDouble(cfg, "weaponRepairMarkerSizeSmall", weaponRepairMarkerSizeSmall);
            weaponRepairMarkerSizeMedium = (float) getDouble(cfg, "weaponRepairMarkerSizeMedium", weaponRepairMarkerSizeMedium);
            weaponRepairMarkerSizeLarge = (float) getDouble(cfg, "weaponRepairMarkerSizeLarge", weaponRepairMarkerSizeLarge);
            if (overrideColors(cfg, "weaponRepairMarkerOverrideColors", false)) {
                weaponRepairMarkerColorFriendly = getColor(cfg, "weaponRepairMarkerColorFriendly", weaponRepairMarkerColorFriendly);
                weaponRepairMarkerColorAlly = getColor(cfg, "weaponRepairMarkerColorAlly", weaponRepairMarkerColorAlly);
                weaponRepairMarkerColorEnemy = getColor(cfg, "weaponRepairMarkerColorEnemy", weaponRepairMarkerColorEnemy);
            }
        } catch (Exception ignored) {
        }

        if(Global.getSettings().getModManager().isModEnabled("sun_flux_reticle")){
            try{
                JSONObject cfg = Global.getSettings().getMergedJSONForMod("FLUX_RETICLE_OPTIONS.ini", "sun_flux_reticle");
                if(Global.getSettings().getModManager().isModEnabled("lunalib")) {
                    showReticle = Boolean.TRUE.equals(LunaSettings.getBoolean("sun_flux_reticle", "sun_fr_showReticle"));
                }else {
                    showReticle = getBooleanCfg(cfg, "showReticle", showReticle);
                }

//                if(syncColorWithFluxReticle && showReticle){
//                    if (getBooleanCfg(cfg, "overrideDefaultUiColors", false)){
//                        systemMarkerRingColorNormal = getColorCfg(cfg, "reticleColor", systemMarkerRingColorNormal);
//                        systemMarkerAmmoColorNormal = getColorCfg(cfg, "reticleColor", systemMarkerAmmoColorNormal);
//                        shieldMarkerRingColorOn = getColorCfg(cfg, "reticleColor", shieldMarkerRingColorOn);
//                    }else {
//                        systemMarkerRingColorNormal = Misc.getPositiveHighlightColor();
//                        systemMarkerAmmoColorNormal = Misc.getPositiveHighlightColor();
//                        shieldMarkerRingColorOn = Misc.getPositiveHighlightColor();
//                    }
//                }
            }catch (Exception ignored){
            }
        }else showReticle = false;


        loadFont(engine);
        engine.getCustomData().put(ID, new LocalData());
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!engine.isPaused()) return;
        if (hide()) return;
        ShipAPI ship = engine.getPlayerShip();
        WeaponData value = ((LocalData) engine.getCustomData().get(ID)).weaponData;
        value.weapon = getPointOnWeapon(ship, value);

        if(value.weapon != null){
            if(value.weapon != value.lastWeapon){
                value.timeReset();
                value.lastWeapon = value.weapon;
            }

            if(value.delay > 0f){
                value.delay -= amount / WeaponData.DELAY_TIME;
            }else if (value.countDown > 0f){
                value.countDown -= amount / WeaponData.COUNTDOWN_TIME;
            }

            WeaponAPI weapon = value.weapon;
            float size = weapon.getSize() == WeaponAPI.WeaponSize.SMALL ? weaponRepairMarkerSizeSmall : weaponRepairMarkerSizeMedium;
            if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) size = weaponRepairMarkerSizeLarge;
            Vector2f loc = new Vector2f(weapon.getLocation().x, weapon.getLocation().y);

            glIn(engine.getViewport());

            if(value.centerWeapon == null){
                Color colorBg = weaponRepairMarkerColorFriendly;
                Color colorFr = weaponRepairMarkerColorFriendly;
                if (value.delay > 0f){
                    drawArc(colorBg, 0.1f * (1 - value.delay), 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                }else if(value.countDown > 0f) {
                    drawArc(colorBg, 0.1f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    drawArc(colorFr, 0.8f, 360f * (1 - value.countDown), loc, size / 2f / engine.getViewport().getViewMult(), ship.getFacing(), 2f);
                }else {
                    if (!value.shiftCompleted){
                        value.centerWeapon = value.weapon;
                        value.shiftCompleted = true;
                        drawArc(colorBg, 0.8f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    }else {
                        drawArc(colorFr, 0.1f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    }
                }
            }else if(value.weapon != value.centerWeapon){
                Color colorBg = weaponRepairMarkerColorAlly;
                Color colorFr = weaponRepairMarkerColorFriendly;
                if (value.delay > 0f){
                    drawArc(colorBg, 0.1f * (1 - value.delay), 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                }else if(value.countDown > 0f) {
                    drawArc(colorBg, 0.1f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    drawArc(colorFr, 0.8f, 360f * (1 - value.countDown), loc, size / 2f / engine.getViewport().getViewMult(), ship.getFacing(), 2f);
                }else {
                    if (!value.shiftCompleted){
                        value.centerWeapon = value.weapon;
                        value.shiftCompleted = true;
                        drawArc(colorFr, 0.8f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    }else {
                        drawArc(colorFr, 0.1f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    }
                }
            }else {
                Color colorBg = weaponRepairMarkerColorFriendly;
                Color colorFr = weaponRepairMarkerColorFriendly;
                if (value.delay > 0f){
                    drawArc(colorBg, 0.1f * (1 - value.delay), 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    drawArc(colorFr, 0.8f * (1 - value.delay), 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                }else if(value.countDown > 0f) {
                    drawArc(colorBg, 0.1f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    drawArc(colorFr, 0.8f, 360f * value.countDown, loc, size / 2f / engine.getViewport().getViewMult(), ship.getFacing(), 2f);
                }else {
                    if (!value.shiftCompleted){
                        value.centerWeapon = null;
                        value.shiftCompleted = true;
                        drawArc(colorFr, 0.1f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    }else {
                        drawArc(colorBg, 0.8f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), 0f, 2f);
                    }
                }
            }
            glOut();

        }else{
            value.timeReset();
            value.weapon = null;
            value.lastWeapon = null;
        }


//        if(value.weapon != null){
//            glIn(engine.getViewport());
//            WeaponAPI weapon = value.weapon;
//            Color color = weapon.getShip() == ship ? weaponRepairMarkerColorFriendly : weaponRepairMarkerColorEnemy;
//            if (weapon.getShip().isAlly()) color = weaponRepairMarkerColorAlly;
//            float size = weapon.getSize() == WeaponAPI.WeaponSize.SMALL ? weaponRepairMarkerSizeSmall : weaponRepairMarkerSizeMedium;
//            if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) size = weaponRepairMarkerSizeLarge;
//            Vector2f loc = new Vector2f(weapon.getLocation().x, weapon.getLocation().y);
//            drawArc(color, 0.8f, 360f, loc, size / 2f / engine.getViewport().getViewMult(), ship.getFacing(), 2f);
//            glOut();
//        }
    }

    private WeaponAPI getPointOnWeapon(ShipAPI ship, WeaponData value){
        Vector2f mouseLoc = new Vector2f(engine.getViewport().convertScreenXToWorldX(Global.getSettings().getMouseX()), engine.getViewport().convertScreenYToWorldY(Global.getSettings().getMouseY()));
        for(WeaponAPI weapon : ship.getAllWeapons()){
            if(weapon.getSlot().isSystemSlot()) continue;
            float size = weapon.getSize() == WeaponAPI.WeaponSize.SMALL ? weaponRepairMarkerSizeSmall : weaponRepairMarkerSizeMedium;
            if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) size = weaponRepairMarkerSizeLarge;
            if (Vector2f.sub(mouseLoc, weapon.getLocation(), null).length() > size * 0.5f) continue;
            return weapon;
        }
//        // target
//        ShipAPI targetShip = ship.getShipTarget();
//        if (targetShip != null){
//            for(WeaponAPI weapon : targetShip.getAllWeapons()){
//                if(weapon.getSlot().isSystemSlot()) continue;
//                float size = weapon.getSize() == WeaponAPI.WeaponSize.SMALL ? weaponRepairMarkerSizeSmall : weaponRepairMarkerSizeMedium;
//                if (weapon.getSize() == WeaponAPI.WeaponSize.LARGE) size = weaponRepairMarkerSizeLarge;
//                if (Vector2f.sub(mouseLoc, weapon.getLocation(), null).length() > size * 0.5f) continue;
//                return weapon;
//            }
//        }
        return null;
    }

    @Override
    public void renderInUICoords(ViewportAPI view) {
        if (hide()) return;
        if (!enableShipSystemMarker && !enableSpecialSystemMarker) return;

        ShipAPI ship = engine.getPlayerShip();
        boolean hasSystem = ship.getSystem() != null;
        boolean hasSpecial = ship.getPhaseCloak() != null;
        if (!hasSystem && !hasSpecial) return;

        ViewportAPI viewport = engine.getViewport();
        Vector2f mouse2f = new Vector2f(viewport.convertScreenXToWorldX(Global.getSettings().getMouseX()), viewport.convertScreenYToWorldY(Global.getSettings().getMouseY()));

        float aimAngle = 90f;
        if (rotateWithFluxReticle && showReticle) aimAngle = Misc.getAngleInDegrees(ship.getLocation(), mouse2f);

        drawFontDistance(ship, aimAngle, drawableDistance, enableShipSystemMarker, systemMarkerAmmoColorNormal, systemMarkerAmmoShiftX, systemMarkerAmmoShiftY - 64f, systemMarkerAmmoFontSize);
    }


    private void drawFontDistance(ShipAPI ship, float aimAngle, LazyFont.DrawableString drawable, boolean enable, Color normal, float shiftX, float shiftY, float fontSize){
        if (!enable) return;
        Vector2f mouseLoc = new Vector2f(engine.getViewport().convertScreenXToWorldX(Global.getSettings().getMouseX()), engine.getViewport().convertScreenYToWorldY(Global.getSettings().getMouseY()));
        int distance = -1;
        if(engine.getCustomData().containsKey(ID) && engine.getCustomData().get(ID) != null){
            WeaponData value = ((LocalData) engine.getCustomData().get(ID)).weaponData;
            if(value.centerWeapon != null){
                distance = Math.round(Vector2f.sub(mouseLoc, value.centerWeapon.getLocation(), null).length());
            }
        }
        if (distance < 0f){
            distance = Math.round(Vector2f.sub(mouseLoc, ship.getLocation(), null).length());
        }

        Vector2f loc = new Vector2f(Global.getSettings().getMouseX() + rotate(new Vector2f(shiftX, shiftY), aimAngle - 90f).x, Global.getSettings().getMouseY() + rotate(new Vector2f(shiftX, shiftY), aimAngle - 90f).y);

        drawFont(engine, drawable, "" + distance, normal, 90f, loc, fontSize, false);

    }

    private static class LocalData {
        WeaponData weaponData = new WeaponData();
    }

    private static class WeaponData {
        final static float DELAY_TIME = 0.2f;
        final static float COUNTDOWN_TIME = 1f;
        WeaponAPI weapon = null;
        WeaponAPI lastWeapon = null;
        WeaponAPI centerWeapon = null;
        float delay = 1f;
        float countDown = 1f;
        Boolean shiftCompleted = false;

        private void timeReset(){
            delay = 1f;
            countDown = 1f;
            shiftCompleted = false;
        }
    }

    private void drawArc(Color color, float alpha, float arc, Vector2f loc, float radius, float facing, float thickness) {
        GL11.glLineWidth(thickness);
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) clamp(0, Math.round(alpha * 255f), 255));
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < Math.round(arc); i++) {
            Vector2f pointV = getPoint(loc, radius * engine.getViewport().getViewMult(), facing + i);
            GL11.glVertex2f(pointV.x, pointV.y);
//            GL11.glVertex2f(loc.x + getPoint(rotate(new Vector2f(), -90f), radius, facing + i).x * engine.getViewport().getViewMult(), loc.y + getPoint(rotate(new Vector2f(), -90f), radius, facing + i).y * engine.getViewport().getViewMult());
        }
        GL11.glEnd();
    }

}
