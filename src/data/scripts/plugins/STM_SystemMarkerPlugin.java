package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class STM_SystemMarkerPlugin extends STM_EveryFramePlugin {

    private static float systemMarkerRingThickness = 2f;
    private static float systemMarkerRingRadius = 8f;
    private static float systemMarkerRingShiftX = 32f;
    private static float systemMarkerRingShiftY = 0f;
    private static float systemMarkerRingAlphaMain = 0.8f;
    private static float systemMarkerRingAlphaBack = 0.1f;
    private static float systemMarkerAmmoFontSize = 12f;
    private static float systemMarkerAmmoShiftX = 32f;
    private static float systemMarkerAmmoShiftY = 0f;

    private static float specialMarkerRingThickness = 2f;
    private static float specialMarkerRingRadius = 8f;
    private static float specialMarkerRingShiftX = 32f;
    private static float specialMarkerRingShiftY = -32f;
    private static float specialMarkerRingAlphaMain = 0.8f;
    private static float specialMarkerRingAlphaBack = 0.1f;
    private static float specialMarkerAmmoFontSize = 12f;
    private static float specialMarkerAmmoShiftX = 32f;
    private static float specialMarkerAmmoShiftY = -32f;

    private static boolean rotateWithFluxReticle = true;
    private static boolean rotateTopOfRings = true;

    private static boolean showReticle = true;

    private static Color systemMarkerRingColorNormal = new Color(50, 255, 255);
    private static Color systemMarkerRingColorUsing = Misc.getHighlightColor();
    private static Color systemMarkerRingColorOutOfAmmo = Misc.getNegativeHighlightColor();
    private static Color systemMarkerAmmoColorNormal = new Color(50, 255, 255);
    private static Color systemMarkerAmmoColorUsing = Misc.getHighlightColor();
    private static Color systemMarkerAmmoColorOutOfAmmo = Misc.getNegativeHighlightColor();

    private static Color specialMarkerRingColorNormal = new Color(255, 50, 255);
    private static Color specialMarkerRingColorUsing = Misc.getHighlightColor();
    private static Color specialMarkerRingColorOutOfAmmo = Misc.getNegativeHighlightColor();
    private static Color specialMarkerAmmoColorNormal = new Color(255, 50, 255);
    private static Color specialMarkerAmmoColorUsing = Misc.getHighlightColor();
    private static Color specialMarkerAmmoColorOutOfAmmo = Misc.getNegativeHighlightColor();

    private LazyFont.DrawableString drawableSystem;
    private LazyFont.DrawableString drawableSpecial;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        loadFont(engine);

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");
            //global
            rotateWithFluxReticle = getBoolean(cfg, "rotateWithFluxReticle", rotateWithFluxReticle);
            rotateTopOfRings = getBoolean(cfg, "rotateTopOfRings", rotateTopOfRings);
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
        } catch (Exception ignored) {
        }

        showReticle = Global.getSettings().getModManager().isModEnabled("sun_flux_reticle");
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
            drawArc(systemColor(ship.getSystem(), systemMarkerRingColorNormal, systemMarkerRingColorUsing, systemMarkerRingColorOutOfAmmo), systemMarkerRingAlphaMain, systemAngle(ship.getSystem()), mouse2f, systemMarkerRingRadius, aimAngle, aimAngleTop, systemMarkerRingShiftX, systemMarkerRingShiftY, specialMarkerRingThickness);
            drawArc(systemColor(ship.getSystem(), systemMarkerRingColorNormal, systemMarkerRingColorUsing, systemMarkerRingColorOutOfAmmo), systemMarkerRingAlphaBack, 360f, mouse2f, systemMarkerRingRadius, aimAngle, aimAngleTop, systemMarkerRingShiftX, systemMarkerRingShiftY, specialMarkerRingThickness);
        }
        if (enableSpecialSystemMarker && ship.getPhaseCloak() != null) {
            drawArc(systemColor(ship.getPhaseCloak(), specialMarkerRingColorNormal, specialMarkerRingColorUsing, specialMarkerRingColorOutOfAmmo), specialMarkerRingAlphaMain, systemAngle(ship.getPhaseCloak()), mouse2f, specialMarkerRingRadius, aimAngle, aimAngleTop, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
            drawArc(systemColor(ship.getPhaseCloak(), specialMarkerRingColorNormal, specialMarkerRingColorUsing, specialMarkerRingColorOutOfAmmo), specialMarkerRingAlphaBack, 360f, mouse2f, specialMarkerRingRadius, aimAngle, aimAngleTop, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
        } else if (enableShipSystemMarker && ship.getShield() != null) {
            drawArc(shieldMarkerColor(ship.getShield(), systemMarkerRingColorNormal), specialMarkerRingAlphaMain, ship.getShield().getActiveArc(), mouse2f, specialMarkerRingRadius, aimAngle, ship.getShield().getFacing() - ship.getShield().getActiveArc() / 2f, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
            drawArc(shieldMarkerColor(ship.getShield(), systemMarkerRingColorNormal), specialMarkerRingAlphaBack, ship.getShield().getArc(), mouse2f, specialMarkerRingRadius, aimAngle, ship.getShield().getFacing() - ship.getShield().getArc() / 2f, specialMarkerRingShiftX, specialMarkerRingShiftY, specialMarkerRingThickness);
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

    private Color shieldMarkerColor(ShieldAPI shield, Color color) {
        if (shield == null || shield.isOn()) return color;
        return Misc.getGrayColor();
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

        drawFont(engine, drawable, "" + system.getAmmo(), systemColor, aimAngle, shiftX, shiftY, fontSize);
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

    private void drawFont(CombatEngineAPI engine, LazyFont.DrawableString drawable, String text, Color color, float aimAngle, float x, float y, float size) {
        LazyFont font = (LazyFont) engine.getCustomData().get(STM_EveryFramePlugin.ID + "_font");
        if (font == null) return;
        color = new Color(color.getRed(), color.getGreen(), color.getBlue());

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

        drawable.draw(Global.getSettings().getMouseX() + rotate(new Vector2f(x, y), aimAngle - 90f).x - (drawable.getWidth() / 2f), Global.getSettings().getMouseY() + rotate(new Vector2f(x, y), aimAngle - 90f).y + (drawable.getHeight() / 2f));
        drawable.dispose();
    }

}