package System_Marker.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONObject;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class STM_SystemMarkerPlugin extends STM_EveryFramePlugin {

    private float systemMarkerRingThickness = 2f;
    private float systemMarkerRingRadius = 8f;
    private float systemMarkerRingShiftX = 32f;
    private float systemMarkerRingShiftY = 0f;
    private float systemMarkerRingAlphaMain = 0.8f;
    private float systemMarkerRingAlphaBack = 0.1f;
    private float systemMarkerAmmoFontSize = 12f;
    private float systemMarkerAmmoShiftX = 32f;
    private float systemMarkerAmmoShiftY = 0f;

    private float specialMarkerRingThickness = 2f;
    private float specialMarkerRingRadius = 8f;
    private float specialMarkerRingShiftX = 32f;
    private float specialMarkerRingShiftY = -32f;
    private float specialMarkerRingAlphaMain = 0.8f;
    private float specialMarkerRingAlphaBack = 0.1f;
    private float specialMarkerAmmoFontSize = 12f;
    private float specialMarkerAmmoShiftX = 32f;
    private float specialMarkerAmmoShiftY = -32f;

    private boolean rotateWithFluxReticle = true;
    private boolean rotateTopOfRings = true;
//    private boolean syncColorWithFluxReticle = true;

    private boolean showReticle = true;

    private Color systemMarkerRingColorNormal = new Color(50, 255, 255);
    private Color systemMarkerRingColorUsing = Misc.getHighlightColor();
    private Color systemMarkerRingColorOutOfAmmo = Misc.getNegativeHighlightColor();
    private Color systemMarkerAmmoColorNormal = new Color(50, 255, 255);
    private Color systemMarkerAmmoColorUsing = Misc.getHighlightColor();
    private Color systemMarkerAmmoColorOutOfAmmo = Misc.getNegativeHighlightColor();

    private Color specialMarkerRingColorNormal = new Color(255, 50, 255);
    private Color specialMarkerRingColorUsing = Misc.getHighlightColor();
    private Color specialMarkerRingColorOutOfAmmo = Misc.getNegativeHighlightColor();
    private Color specialMarkerAmmoColorNormal = new Color(255, 50, 255);
    private Color specialMarkerAmmoColorUsing = Misc.getHighlightColor();
    private Color specialMarkerAmmoColorOutOfAmmo = Misc.getNegativeHighlightColor();

    private Color shieldMarkerRingColorOn = new Color(50, 255, 255);
    private Color shieldMarkerRingColorOff = Misc.getGrayColor();

    private Color phaseMarkerRingColorNormal = new Color(255, 50, 255);
    private Color phaseMarkerRingColorUsing = Misc.getHighlightColor();
    private Color phaseMarkerRingColorOutOfAmmo = Misc.getNegativeHighlightColor();

    private LazyFont.DrawableString drawableSystem = null;
    private LazyFont.DrawableString drawableSpecial = null;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");
            //global
            rotateWithFluxReticle = getBoolean(cfg, "rotateWithFluxReticle", rotateWithFluxReticle);
            rotateTopOfRings = getBoolean(cfg, "rotateTopOfRings", rotateTopOfRings);
//            syncColorWithFluxReticle = getBoolean(cfg, "syncColorWithFluxReticle", syncColorWithFluxReticle);
            //ring
            systemMarkerRingThickness = (float) getDouble(cfg, "systemMarkerRingThickness", systemMarkerRingThickness);
            systemMarkerRingRadius = (float) getDouble(cfg, "systemMarkerRingRadius", systemMarkerRingRadius);
            systemMarkerRingShiftX = (float) getDouble(cfg, "systemMarkerRingShiftX", systemMarkerRingShiftX);
            systemMarkerRingShiftY = (float) getDouble(cfg, "systemMarkerRingShiftY", systemMarkerRingShiftY);
            systemMarkerRingAlphaMain = (float) getDouble(cfg, "systemMarkerRingAlphaMain", systemMarkerRingAlphaMain);
            systemMarkerRingAlphaBack = (float) getDouble(cfg, "systemMarkerRingAlphaBack", systemMarkerRingAlphaBack);
            systemMarkerAmmoFontSize = (float) getDouble(cfg, "systemMarkerAmmoFontSize", systemMarkerAmmoFontSize);
            systemMarkerAmmoShiftX = (float) getDouble(cfg, "systemMarkerAmmoShiftX", systemMarkerAmmoShiftX);
            systemMarkerAmmoShiftY = (float) getDouble(cfg, "systemMarkerAmmoShiftY", systemMarkerAmmoShiftY);

            specialMarkerRingThickness = (float) getDouble(cfg, "specialMarkerRingThickness", specialMarkerRingThickness);
            specialMarkerRingRadius = (float) getDouble(cfg, "specialMarkerRingRadius", specialMarkerRingRadius);
            specialMarkerRingShiftX = (float) getDouble(cfg, "specialMarkerRingShiftX", specialMarkerRingShiftX);
            specialMarkerRingShiftY = (float) getDouble(cfg, "specialMarkerRingShiftY", specialMarkerRingShiftY);
            specialMarkerRingAlphaMain = (float) getDouble(cfg, "specialMarkerRingAlphaMain", specialMarkerRingAlphaMain);
            specialMarkerRingAlphaBack = (float) getDouble(cfg, "specialMarkerRingAlphaBack", specialMarkerRingAlphaBack);
            specialMarkerAmmoFontSize = (float) getDouble(cfg, "specialMarkerAmmoFontSize", specialMarkerAmmoFontSize);
            specialMarkerAmmoShiftX = (float) getDouble(cfg, "specialMarkerAmmoShiftX", specialMarkerAmmoShiftX);
            specialMarkerAmmoShiftY = (float) getDouble(cfg, "specialMarkerAmmoShiftY", specialMarkerAmmoShiftY);
            //colors
            if (overrideColors(cfg, "systemMarkerOverrideColors", false)) {
                systemMarkerRingColorNormal = getColor(cfg, "systemMarkerRingColorNormal", systemMarkerRingColorNormal);
                systemMarkerRingColorUsing = getColor(cfg, "systemMarkerRingColorUsing", systemMarkerRingColorUsing);
                systemMarkerRingColorOutOfAmmo = getColor(cfg, "systemMarkerRingColorOutOfAmmo", systemMarkerRingColorOutOfAmmo);
                systemMarkerAmmoColorNormal = getColor(cfg, "systemMarkerAmmoColorNormal", systemMarkerAmmoColorNormal);
                systemMarkerAmmoColorUsing = getColor(cfg, "systemMarkerAmmoColorUsing", systemMarkerAmmoColorUsing);
                systemMarkerAmmoColorOutOfAmmo = getColor(cfg, "systemMarkerAmmoColorOutOfAmmo", systemMarkerAmmoColorOutOfAmmo);
            }
            if (overrideColors(cfg, "specialMarkerOverrideColors", false)) {
                specialMarkerRingColorNormal = getColor(cfg, "specialMarkerRingColorNormal", specialMarkerRingColorNormal);
                specialMarkerRingColorUsing = getColor(cfg, "specialMarkerRingColorUsing", specialMarkerRingColorUsing);
                specialMarkerRingColorOutOfAmmo = getColor(cfg, "specialMarkerRingColorOutOfAmmo", specialMarkerRingColorOutOfAmmo);
                specialMarkerAmmoColorNormal = getColor(cfg, "specialMarkerAmmoColorNormal", specialMarkerAmmoColorNormal);
                specialMarkerAmmoColorUsing = getColor(cfg, "specialMarkerAmmoColorUsing", specialMarkerAmmoColorUsing);
                specialMarkerAmmoColorOutOfAmmo = getColor(cfg, "specialMarkerAmmoColorOutOfAmmo", specialMarkerAmmoColorOutOfAmmo);
            }
            if (overrideColors(cfg, "shieldMarkerOverrideColors", false)) {
                shieldMarkerRingColorOn = getColor(cfg, "shieldMarkerRingColorOn", shieldMarkerRingColorOn);
                shieldMarkerRingColorOff = getColor(cfg, "shieldMarkerRingColorOff", shieldMarkerRingColorOff);
            }
            if (overrideColors(cfg, "phaseMarkerOverrideColors", false)) {
                phaseMarkerRingColorNormal = getColor(cfg, "phaseMarkerRingColorNormal", phaseMarkerRingColorNormal);
                phaseMarkerRingColorUsing = getColor(cfg, "phaseMarkerRingColorUsing", phaseMarkerRingColorUsing);
                phaseMarkerRingColorOutOfAmmo = getColor(cfg, "phaseMarkerRingColorOutOfAmmo", phaseMarkerRingColorOutOfAmmo);
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
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (hide()) return;
        if (!enableShipSystemMarker && !enableSpecialSystemMarker) return;

        ShipAPI ship = engine.getPlayerShip();
        if (ship.getSystem() == null && ship.getPhaseCloak() == null) return;

        ViewportAPI viewport = engine.getViewport();
        Vector2f mouse2f = new Vector2f(viewport.convertScreenXToWorldX(Global.getSettings().getMouseX()), viewport.convertScreenYToWorldY(Global.getSettings().getMouseY()));
        float aimAngle = 90f;
        float aimAngleTop = 90f;
        if (rotateWithFluxReticle && showReticle) {
            aimAngle = Misc.getAngleInDegrees(ship.getLocation(), mouse2f);
            if (rotateTopOfRings) aimAngleTop = aimAngle;
        }

        glIn(viewport);
        if (enableShipSystemMarker && ship.getSystem() != null) {
            drawArc(systemColor(ship.getSystem(), systemMarkerRingColorNormal, systemMarkerRingColorUsing, systemMarkerRingColorOutOfAmmo), systemMarkerRingAlphaMain, systemAngle(ship.getSystem()), mouse2f, systemMarkerRingRadius, aimAngle, aimAngleTop, systemMarkerRingShiftX, systemMarkerRingShiftY, systemMarkerRingThickness);
            drawArc(systemColor(ship.getSystem(), systemMarkerRingColorNormal, systemMarkerRingColorUsing, systemMarkerRingColorOutOfAmmo), systemMarkerRingAlphaBack, 360f, mouse2f, systemMarkerRingRadius, aimAngle, aimAngleTop, systemMarkerRingShiftX, systemMarkerRingShiftY, systemMarkerRingThickness);
        }
        if (enableSpecialSystemMarker && ship.getPhaseCloak() != null) {
            if(ship.getPhaseCloak().getId().equals("phasecloak")){
                drawArc(systemColor(ship.getPhaseCloak(), phaseMarkerRingColorNormal, phaseMarkerRingColorUsing, phaseMarkerRingColorOutOfAmmo), specialMarkerRingAlphaMain, systemAngle(ship.getPhaseCloak()), mouse2f, specialMarkerRingRadius, aimAngle, aimAngleTop, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
                drawArc(systemColor(ship.getPhaseCloak(), phaseMarkerRingColorNormal, phaseMarkerRingColorUsing, phaseMarkerRingColorOutOfAmmo), specialMarkerRingAlphaBack, 360f, mouse2f, specialMarkerRingRadius, aimAngle, aimAngleTop, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
            }else {
                drawArc(systemColor(ship.getPhaseCloak(), specialMarkerRingColorNormal, specialMarkerRingColorUsing, specialMarkerRingColorOutOfAmmo), specialMarkerRingAlphaMain, systemAngle(ship.getPhaseCloak()), mouse2f, specialMarkerRingRadius, aimAngle, aimAngleTop, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
                drawArc(systemColor(ship.getPhaseCloak(), specialMarkerRingColorNormal, specialMarkerRingColorUsing, specialMarkerRingColorOutOfAmmo), specialMarkerRingAlphaBack, 360f, mouse2f, specialMarkerRingRadius, aimAngle, aimAngleTop, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
            }
        }else if (enableSpecialSystemMarker && ship.getShield() != null) {
            drawArc(shieldMarkerColor(ship.getShield()), specialMarkerRingAlphaMain, ship.getShield().getActiveArc(), mouse2f, specialMarkerRingRadius, aimAngle, ship.getShield().getFacing() - ship.getShield().getActiveArc() / 2f, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
            drawArc(shieldMarkerColor(ship.getShield()), specialMarkerRingAlphaBack, ship.getShield().getArc(), mouse2f, specialMarkerRingRadius, aimAngle, ship.getShield().getFacing() - ship.getShield().getArc() / 2f, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
        }
        glOut();
    }

    private Color systemColor(ShipSystemAPI phase, Color normal, Color using, Color outOfAmmo) {
        if (phase == null) return normal;
        if (phase.getMaxAmmo() > 10000 && phase.isActive()) return using;
        if (phase.getMaxAmmo() > 10000 && phase.isCoolingDown() && phase.getCooldown() > 0f) return outOfAmmo;
        if (phase.getMaxAmmo() <= 10000 && phase.isActive()) return using;
        if (phase.getMaxAmmo() <= 10000 && phase.isOutOfAmmo()) return outOfAmmo;
        if (phase.getMaxAmmo() <= 10000 && phase.isCoolingDown() && phase.getCooldown() > 0f) return using;
        return normal;
    }

    private float systemAngle(ShipSystemAPI system) {
        if (system == null) return 360f;
        if (system.getMaxAmmo() > 10000 && system.isCoolingDown() && system.getCooldown() > 0f)
            return (system.getCooldown() - system.getCooldownRemaining()) / system.getCooldown() * 360f;
        if (system.getMaxAmmo() <= 10000 && system.getAmmo() < system.getMaxAmmo())
            return system.getAmmoReloadProgress() * 360f;
        return 360f;
    }

    private Color shieldMarkerColor(ShieldAPI shield) {
        if (shield == null || shield.isOn()) return shieldMarkerRingColorOn;
        return shieldMarkerRingColorOff;
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

        drawFontSystemMarker(ship.getSystem(), aimAngle, drawableSystem, enableShipSystemMarker, systemMarkerAmmoColorNormal, systemMarkerAmmoColorUsing, systemMarkerAmmoColorOutOfAmmo, systemMarkerAmmoShiftX, systemMarkerAmmoShiftY, systemMarkerAmmoFontSize);
        drawFontSystemMarker(ship.getPhaseCloak(), aimAngle, drawableSpecial, enableSpecialSystemMarker, specialMarkerAmmoColorNormal, specialMarkerAmmoColorUsing, specialMarkerAmmoColorOutOfAmmo, specialMarkerAmmoShiftX, specialMarkerAmmoShiftY, specialMarkerAmmoFontSize);
    }

    private void drawFontSystemMarker(ShipSystemAPI system, float aimAngle, LazyFont.DrawableString drawable, boolean enable, Color normal, Color using, Color outOfAmmo, float shiftX, float shiftY, float fontSize) {
        if (!enable) return;
        if (system == null || system.getMaxAmmo() > 10000) return;

        Color systemColor = normal;
        if (system.isActive()) systemColor = using;
        else if (system.isOutOfAmmo()) systemColor = outOfAmmo;
        else if (system.isCoolingDown()) systemColor = using;

        Vector2f loc = new Vector2f(Global.getSettings().getMouseX() + rotate(new Vector2f(shiftX, shiftY), aimAngle - 90f).x, Global.getSettings().getMouseY() + rotate(new Vector2f(shiftX, shiftY), aimAngle - 90f).y);
        drawFont(engine, drawable, "" + system.getAmmo(), systemColor, 90f, loc, fontSize, false);
    }

    private void drawArc(Color color, float alpha, float arc, Vector2f loc, float radius, float aimAngle, float aimAngleTop, float x, float y, float thickness) {
        GL11.glLineWidth(thickness);
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) Math.max(0, Math.min(Math.round(alpha * 255f), 255)));
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < Math.round(arc); i++) {
            GL11.glVertex2f(loc.x + getPoint(rotate(new Vector2f(x, y), aimAngle - 90f), radius, aimAngleTop + i).x * engine.getViewport().getViewMult(), loc.y + getPoint(rotate(new Vector2f(x, y), aimAngle - 90f), radius, aimAngleTop + i).y * engine.getViewport().getViewMult());
        }
        GL11.glEnd();
    }

}