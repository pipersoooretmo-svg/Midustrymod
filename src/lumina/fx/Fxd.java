package lumina.fx;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.Effect.*;
import mindustry.graphics.*;

/** Drawing building blocks shared by the upgraded effects. Everything is deterministic per effect id. */
public final class Fxd{
    /** Particle count multiplier, driven by the intensity setting. */
    public static float detail = 1f;

    public static final Color
    smokeColor = Color.valueOf("3a3a40"),
    emberEnd = Color.valueOf("7a3b23"),
    electric = Color.valueOf("b9d4ff"),
    hot = Color.valueOf("fff3c9");

    private static final Rand rand = new Rand();
    private static final Color c1 = new Color(), c2 = new Color();

    private Fxd(){}

    public static int count(float base){
        return Math.max(1, Mathf.round(base * detail));
    }

    /** @return 1 → 0 progress of a sub-timeline lasting {@code life} ticks, or -1 once it is over. */
    public static float fade(EffectContainer e, float life){
        return e.time >= life ? -1f : 1f - e.time / life;
    }

    /** Soft radial glow drawn with plain alpha blending, so it never needs a batch flush. */
    public static void glow(float x, float y, float radius, Color color, float alpha){
        if(radius < 0.5f || alpha < 0.004f) return;
        c1.set(color).a(alpha);
        c2.set(color).a(0f);
        Fill.light(x, y, radius > 30f ? 24 : radius > 12f ? 16 : 10, radius, c1, c2);
    }

    /** Four-pointed star flash. */
    public static void flare(float x, float y, float length, float width, float rotation, Color color){
        if(length < 0.3f || width < 0.1f) return;
        Draw.color(color);
        for(int i = 0; i < 4; i++){
            Drawf.tri(x, y, width, length * (i % 2 == 0 ? 1f : 0.5f), rotation + i * 90f);
        }
    }

    /** Streaks that shoot outwards and slow down. */
    public static void sparks(EffectContainer e, int seed, int amount, float dist, float length, float stroke, float rotation, float cone, Color from, Color to){
        rand.setSeed(e.id * 31L + seed);
        float fin = e.fin(), fout = e.fout(), move = Interp.pow3Out.apply(fin);
        Draw.color(from, to, fin);
        Lines.stroke(stroke * fout + 0.25f);
        for(int i = 0; i < amount; i++){
            float ang = rotation + rand.range(cone);
            float d = dist * rand.random(0.3f, 1f) * move;
            float len = length * rand.random(0.5f, 1f) * fout + 0.6f;
            Lines.lineAngle(e.x + Angles.trnsx(ang, d), e.y + Angles.trnsy(ang, d), ang, len);
        }
    }

    /** Small glowing diamonds that drift outwards and cool down. */
    public static void embers(EffectContainer e, int seed, int amount, float dist, float size, float rotation, float cone, Color from, Color to){
        rand.setSeed(e.id * 17L + seed);
        float fin = e.fin(), fout = e.fout(), move = Interp.pow2Out.apply(fin);
        Draw.color(from, to, fin);
        for(int i = 0; i < amount; i++){
            float ang = rotation + rand.range(cone);
            float d = dist * rand.random(0.25f, 1f) * move;
            float s = size * rand.random(0.5f, 1f) * fout;
            if(s < 0.15f) continue;
            Fill.square(e.x + Angles.trnsx(ang, d), e.y + Angles.trnsy(ang, d), s, 45f);
        }
    }

    /** Slow translucent puffs that grow while they fade. */
    public static void smoke(EffectContainer e, int seed, int amount, float dist, float size, float rotation, float cone, Color color, float alpha){
        rand.setSeed(e.id * 13L + seed);
        float fin = e.fin(), fout = e.fout(), move = Interp.pow2Out.apply(fin);
        Draw.color(color);
        for(int i = 0; i < amount; i++){
            float ang = rotation + rand.range(cone);
            float d = dist * rand.random(0.2f, 1f) * move;
            float r = size * rand.random(0.55f, 1f) * (0.45f + 0.55f * fin);
            Draw.alpha(alpha * fout * rand.random(0.6f, 1f));
            Fill.circle(e.x + Angles.trnsx(ang, d), e.y + Angles.trnsy(ang, d), r);
        }
    }

    public static void ring(float x, float y, float radius, float stroke, Color color, float alpha){
        if(stroke < 0.05f || alpha < 0.004f) return;
        Draw.color(color);
        Draw.alpha(alpha);
        Lines.stroke(stroke);
        Lines.circle(x, y, radius);
    }

    /** Jagged electric arcs radiating from the centre. */
    public static void arcs(EffectContainer e, int seed, int amount, float dist, float stroke, Color color){
        rand.setSeed(e.id * 7L + seed + (long)(e.time / 3f) * 131L);
        float fout = e.fout();
        Draw.color(Color.white, color, e.fin());
        Lines.stroke(stroke * fout + 0.2f);
        for(int i = 0; i < amount; i++){
            float ang = rand.random(360f);
            float x = e.x, y = e.y;
            float step = dist / 3f;
            for(int s = 0; s < 3; s++){
                ang += rand.range(38f);
                float nx = x + Angles.trnsx(ang, step * rand.random(0.6f, 1f));
                float ny = y + Angles.trnsy(ang, step * rand.random(0.6f, 1f));
                Lines.line(x, y, nx, ny);
                x = nx;
                y = ny;
            }
        }
    }

    // ---- composite looks ----

    /** Muzzle flash; {@code e.rotation} is the firing direction. */
    public static void muzzle(EffectContainer e, Color color, float scl){
        float flash = fade(e, 5f + 3f * scl);
        if(flash > 0f){
            float ox = Angles.trnsx(e.rotation, 2.5f * scl), oy = Angles.trnsy(e.rotation, 2.5f * scl);
            glow(e.x + ox, e.y + oy, 13f * scl * (0.55f + 0.45f * flash), color, 0.38f * flash);
            flare(e.x + ox, e.y + oy, 8.5f * scl * flash, 1.9f * scl * flash, e.rotation, c1.set(hot).lerp(color, 1f - flash * 0.6f));
        }
        sparks(e, 1, count(2f + 2f * scl), 20f * scl, 4.5f * scl, 1f, e.rotation, 17f, hot, color);
    }

    /** Lingering barrel smoke; {@code e.rotation} is the firing direction. */
    public static void barrelSmoke(EffectContainer e, float scl){
        smoke(e, 2, count(2f + 2f * scl), 22f * scl, 3.6f * scl, e.rotation, 24f, smokeColor, 0.34f);
    }

    /** Bullet impact. */
    public static void impact(EffectContainer e, Color color, float scl){
        float flash = fade(e, 6f + 2f * scl);
        if(flash > 0f){
            glow(e.x, e.y, 12f * scl * (0.5f + 0.5f * flash), color, 0.38f * flash);
        }
        sparks(e, 3, count(2f + 2f * scl), 21f * scl, 5f * scl, 1.1f, 0f, 180f, hot, color);
        embers(e, 4, count(2f + 2f * scl), 13f * scl, 1.5f * scl, 0f, 180f, color, emberEnd);
    }

    /** Light-weight impact for effects that are spawned many times per second (beams, flames). */
    public static void sizzle(EffectContainer e, Color color, float scl){
        glow(e.x, e.y, 11f * scl * (0.6f + 0.4f * e.fout()), color, 0.3f * e.fout());
        embers(e, 5, count(2f), 12f * scl, 1.3f * scl, 0f, 180f, hot, color);
    }

    /** Explosion with a visual radius in world units. */
    public static void blast(EffectContainer e, Color color, float radius){
        float flash = fade(e, 7f + radius * 0.12f);
        if(flash > 0f){
            glow(e.x, e.y, radius * 1.5f * (0.45f + 0.55f * flash), color, 0.4f * flash);
            float reach = Math.min(radius * 1.15f, 42f);
            flare(e.x, e.y, reach * flash, reach * 0.08f * flash, 45f, c1.set(hot).lerp(color, 1f - flash * 0.6f));
        }
        float wave = fade(e, 12f + radius * 0.2f);
        if(wave > 0f){
            ring(e.x, e.y, radius * 1.3f * Interp.pow2Out.apply(1f - wave), radius * 0.09f * wave + 0.3f, color, 0.75f * wave);
        }
        smoke(e, 6, count(2f + radius / 7f), radius * 0.9f, radius * 0.34f, 0f, 180f, smokeColor, 0.3f);
        sparks(e, 7, count(4f + radius / 4f), radius * 2.1f, radius * 0.3f, 1.2f + radius / 40f, 0f, 180f, hot, color);
        embers(e, 8, count(3f + radius / 5f), radius * 1.5f, 1.2f + radius * 0.06f, 0f, 180f, color, emberEnd);
    }

    /** Electric discharge. */
    public static void zap(EffectContainer e, Color color, float scl){
        float flash = fade(e, 7f);
        if(flash > 0f){
            glow(e.x, e.y, 14f * scl * (0.5f + 0.5f * flash), color, 0.45f * flash);
        }
        arcs(e, 9, count(2f + scl), 15f * scl, 1f, color);
    }

    /** Soft halo behind trail puffs; {@code e.rotation} is the puff radius. */
    public static void halo(EffectContainer e, Color color){
        glow(e.x, e.y, e.rotation * 2.6f * e.fout(), color, 0.22f * e.fout());
    }
}
