package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class STM_EveryFramePlugin extends BaseEveryFrameCombatPlugin {
    public static final String ID = "STM_EveryFramePlugin";
    protected static boolean showWhenInterfaceIsHidden = false;
    protected static boolean enableShipSystemMarker = true;
    protected static boolean enableSpecialSystemMarker = true;
    protected static boolean enableAmmoRemainingMarker = true;
    protected static boolean enableWeaponRepairMarker = true;
    protected static boolean enableEngineRepairMarker = true;
    protected static boolean enableTorpedoMarker = true;
    protected static boolean enableMineMarker = true;
    protected static boolean enableLunaColorSetting = false;
    protected CombatEngineAPI engine;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;

        try {
            JSONObject cfg = Global.getSettings().getMergedJSONForMod("SYSTEM_MARKER_OPTIONS.ini", "System_Marker");

            showWhenInterfaceIsHidden = getBoolean(cfg, "showWhenInterfaceIsHidden", showWhenInterfaceIsHidden);

            enableShipSystemMarker = getBoolean(cfg, "enableShipSystemMarker", enableShipSystemMarker);
            enableSpecialSystemMarker = getBoolean(cfg, "enableSpecialSystemMarker", enableSpecialSystemMarker);
            enableAmmoRemainingMarker = getBoolean(cfg, "enableAmmoRemainingMarker", enableAmmoRemainingMarker);
            //from visualized repair progress
            enableWeaponRepairMarker = getBoolean(cfg, "enableWeaponRepairMarker", enableWeaponRepairMarker);
            enableEngineRepairMarker = getBoolean(cfg, "enableEngineRepairMarker", enableEngineRepairMarker);
            //from torpedo alarm
            enableTorpedoMarker = getBoolean(cfg, "enableTorpedoMarker", enableTorpedoMarker);
            enableMineMarker = getBoolean(cfg, "enableMineMarker", enableMineMarker);

            enableLunaColorSetting = getBoolean(cfg, "enableLunaColorSetting", enableLunaColorSetting);

        } catch (Exception ignored) {
        }

        if (enableShipSystemMarker || enableSpecialSystemMarker) {
            engine.addPlugin(new STM_SystemMarkerPlugin());
        }
        if (enableAmmoRemainingMarker) {
            engine.addPlugin(new STM_AmmoRemainingPlugin());
        }
        if (enableWeaponRepairMarker) {
            engine.addPlugin(new STM_WeaponRepairMarkerPlugin());
        }
        if (enableEngineRepairMarker) {
            engine.addPlugin(new STM_EngineRepairMarkerPlugin());
        }
        if (enableTorpedoMarker) {
            engine.addPlugin(new STM_TorpedoMarkerPlugin());
        }
    }

    protected boolean getBoolean(JSONObject cfg, String id, boolean original) {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            if (Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaSetting"))) {
                if (LunaSettings.getBoolean("System_Marker", id) != null) {
                    return LunaSettings.getBoolean("System_Marker", id);
                }
            }
        }
        try {
            return cfg.getBoolean(id);
        } catch (Exception ignored) {
            return original;
        }
    }

    protected int getInt(JSONObject cfg, String id, int original) {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            if (Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaSetting"))) {
                if (LunaSettings.getInt("System_Marker", id) != null) {
                    return LunaSettings.getInt("System_Marker", id);
                }
            }
        }
        try {
            return cfg.getInt(id);
        } catch (Exception ignored) {
            return original;
        }
    }

    protected double getDouble(JSONObject cfg, String id, double original) {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            if (Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaSetting"))) {
                if (LunaSettings.getDouble("System_Marker", id) != null) {
                    return LunaSettings.getDouble("System_Marker", id);
                }
            }
        }
        try {
            return cfg.getDouble(id);
        } catch (Exception ignored) {
            return original;
        }
    }

    protected String getString(JSONObject cfg, String id, String original) {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            if (Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaSetting"))) {
                if (LunaSettings.getString("System_Marker", id) != null) {
                    return LunaSettings.getString("System_Marker", id);
                }
            }
        }
        try {
            return cfg.getString(id);
        } catch (Exception ignored) {
            return original;
        }
    }

    protected Color getColor(JSONObject cfg, String id, Color original) {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            if (Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaSetting")) && Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaColorSetting"))) {
                if (LunaSettings.getColor("System_Marker", id) != null) {
                    return LunaSettings.getColor("System_Marker", id);
                }
            }
        }
        try {
            return new Color((float) cfg.getJSONArray(id).getInt(0), (float) cfg.getJSONArray(id).getInt(1), (float) cfg.getJSONArray(id).getInt(2));
        } catch (Exception ignored) {
            return original;
        }
    }

    protected boolean overrideColors(JSONObject cfg, String id, boolean original) {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            if (Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaSetting")) && Boolean.TRUE.equals(LunaSettings.getBoolean("System_Marker", "enableLunaColorSetting"))) {
                if (LunaSettings.getBoolean("System_Marker", "id") != null) {
                    return LunaSettings.getBoolean("System_Marker", "id");
                }
            }
        }
        try {
            return cfg.getBoolean(id);
        } catch (Exception ignored) {
            return original;
        }
    }

    protected boolean hide() {
        if (engine == null || engine.getCombatUI() == null || engine.getPlayerShip() == null) return true;
        if (!engine.getPlayerShip().isAlive() || engine.getPlayerShip().isHulk()) return true;
        if (engine.getCombatUI().isShowingCommandUI() || engine.isUIShowingDialog()) return true;
        return !engine.isUIShowingHUD() && !showWhenInterfaceIsHidden;
    }

    /**
     * rotate a vector with an angle
     *
     * @param v      vector to rotate
     * @param facing angle to rotate
     * @return vector after rotate
     */
    protected static Vector2f rotate(Vector2f v, float facing) {
        return new Vector2f((float) (v.x * Math.cos(Math.toRadians(facing)) - v.y * Math.sin(Math.toRadians(facing))), (float) (v.x * Math.sin(Math.toRadians(facing)) + v.y * Math.cos(Math.toRadians(facing))));
    }

    /**
     * sum two vectors
     *
     * @param base vector to sum
     * @param sum  vector to sum with a mult
     * @param mult mult length of sum
     * @return vector after sum
     */
    protected static Vector2f sumVector(Vector2f base, Vector2f sum, float mult) {
        return new Vector2f(base.x + sum.x * mult, base.y + sum.y * mult);
    }

    /**
     * mix two color
     *
     * @param c1    first color
     * @param level level of the second color
     * @param c2    second color
     * @return mixed color
     */
    protected static Color getMixedColor(Color c1, float level, Color c2) {
        return new Color((int) clamp(0, c1.getRed() * (1f - level) + c2.getRed() * level, 255), (int) clamp(0, c1.getGreen() * (1f - level) + c2.getGreen() * level, 255), (int) clamp(0, c1.getBlue() * (1f - level) + c2.getBlue() * level, 255));
    }

    /**
     * Get the level of value between start and end
     *
     * @param value the value of level
     * @param start starting value
     * @param end   ending value, can be lower than start
     * @return the level of value between start and end in 0~1, or 0 when end - start == 0f
     * exp. value: 1.6f, start: 0f, end: 2f
     * will return 0.8f
     */
    protected static float getLevel(float value, float start, float end) {
        if (end - start == 0f) return 0f;
        return clamp((value - start) / (end - start));
    }

    protected static Vector2f getPoint(Vector2f center, float radius, float angle) {
        return MathUtils.getPoint(center, radius, angle);
    }

    protected static float getAngle(Vector2f from, Vector2f to) {
        return VectorUtils.getAngle(from, to);
    }

    protected static float clamp(float num) {
        return clamp(0f, num, 1f);
    }

    protected static float clamp(float min, float num, float max) {
        return Math.max(min, Math.min(num, max));
    }

    protected static double clampIntoPM180(float angle) {
        return clampCycle(angle, -180f, 180f);
    }

    protected static double clampCycle(double value, double left, double right) {
        return ((value - left) % (left + right)) + left;
    }

    protected static void loadFont(CombatEngineAPI engine) {
        if (engine.getCustomData().containsKey(ID + "_font") && engine.getCustomData().get(ID + "_font") != null)
            return;
        try {
            engine.getCustomData().put(ID + "_font", LazyFont.loadFont("graphics/fonts/orbitron20aabold.fnt"));
        } catch (FontException ex) {
            throw new RuntimeException("Failed to load font");
        }
    }

    //ogl
    protected void glIn(ViewportAPI viewport) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(viewport.getLLX(), viewport.getLLX() + viewport.getVisibleWidth(), viewport.getLLY(), viewport.getLLY() + viewport.getVisibleHeight(), -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);
    }

    protected void glOut() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}