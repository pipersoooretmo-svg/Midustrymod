package lumina.fx;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.Effect.*;
import mindustry.graphics.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/**
 * Layers extra particles, glows and shockwaves on top of the vanilla effects by wrapping their renderers in place.
 * Everything that references a vanilla effect (turrets, units, blocks, other mods) picks the upgrade up automatically.
 */
public final class FxOverhaul{
    private static final Seq<Entry> entries = new Seq<>();
    private static boolean applied;

    private FxOverhaul(){}

    public static void load(){
        if(entries.any()) return;

        // muzzle flashes
        up(Fx.shootSmall, 15f, 90f, e -> Fxd.muzzle(e, Pal.lightOrange, 0.8f));
        up(Fx.shootSmallColor, 15f, 90f, e -> Fxd.muzzle(e, e.color, 0.8f));
        up(Fx.shootHeal, 15f, 90f, e -> Fxd.muzzle(e, Pal.heal, 0.8f));
        up(Fx.shootHealYellow, 15f, 90f, e -> Fxd.muzzle(e, Pal.lightTrail, 0.8f));
        up(Fx.shootBig, 17f, 120f, e -> Fxd.muzzle(e, Pal.lightOrange, 1.25f));
        up(Fx.shootBig2, 17f, 120f, e -> Fxd.muzzle(e, Pal.lightOrange, 1.4f));
        up(Fx.shootBigColor, 17f, 120f, e -> Fxd.muzzle(e, e.color, 1.3f));
        up(Fx.shootTitan, 20f, 180f, e -> Fxd.muzzle(e, e.color, 2.1f));
        up(Fx.thoriumShoot, 18f, 120f, e -> Fxd.muzzle(e, Pal.thoriumPink, 1.2f));
        up(Fx.lightningShoot, 18f, 110f, e -> Fxd.muzzle(e, Pal.lancerLaser, 1.1f));
        up(Fx.sparkShoot, 18f, 110f, e -> Fxd.muzzle(e, e.color, 1.1f));
        up(Fx.lancerLaserShoot, 0f, 120f, e -> {
            Fxd.glow(e.x, e.y, 22f * (0.5f + 0.5f * e.fout()), Pal.lancerLaser, 0.45f * e.fout());
            Fxd.arcs(e, 20, Fxd.count(3f), 18f, 1f, Pal.lancerLaser);
        });
        up(Fx.lancerLaserCharge, 0f, 0f, e -> Fxd.glow(e.x, e.y, 5f + 9f * e.fin(), Pal.lancerLaser, 0.3f * e.fin()));
        up(Fx.lancerLaserChargeBegin, 0f, 0f, e -> Fxd.glow(e.x, e.y, 4f + 12f * e.fin(), Pal.lancerLaser, 0.35f * e.fin()));
        up(Fx.railShoot, 0f, 260f, e -> heavyMuzzle(e, Pal.orangeSpark));
        up(Fx.instShoot, 0f, 260f, e -> heavyMuzzle(e, Pal.bulletYellowBack));
        up(Fx.shootSmallFlame, 0f, 0f, e -> flameGlow(e, Pal.lightFlame));
        up(Fx.shootPyraFlame, 0f, 0f, e -> flameGlow(e, Pal.lightPyraFlame));

        // barrel smoke
        up(Fx.shootSmallSmoke, 40f, 90f, e -> Fxd.barrelSmoke(e, 0.8f));
        up(Fx.shootBigSmoke, 46f, 120f, e -> Fxd.barrelSmoke(e, 1.25f));
        up(Fx.shootBigSmoke2, 48f, 130f, e -> Fxd.barrelSmoke(e, 1.45f));
        up(Fx.shootSmokeTitan, 0f, 200f, e -> Fxd.barrelSmoke(e, 2.2f));

        // impacts
        up(Fx.hitBulletSmall, 22f, 90f, e -> Fxd.impact(e, Pal.lightOrange, 0.85f));
        up(Fx.hitBulletColor, 22f, 90f, e -> Fxd.impact(e, e.color, 0.85f));
        up(Fx.hitSquaresColor, 22f, 100f, e -> Fxd.impact(e, e.color, 1f));
        up(Fx.hitFuse, 22f, 100f, e -> Fxd.impact(e, Pal.surge, 1.05f));
        up(Fx.hitBulletBig, 24f, 130f, e -> Fxd.impact(e, Pal.lightOrange, 1.4f));
        up(Fx.hitLaserBlast, 22f, 120f, e -> Fxd.impact(e, e.color, 1.2f));
        up(Fx.despawn, 20f, 70f, e -> Fxd.impact(e, Pal.lighterOrange, 0.55f));
        up(Fx.hitLancer, 20f, 110f, e -> Fxd.zap(e, Pal.lancerLaser, 1.1f));
        up(Fx.hitEmpSpark, 0f, 110f, e -> Fxd.zap(e, Pal.heal, 1.2f));
        up(Fx.hitFlameSmall, 0f, 0f, e -> Fxd.sizzle(e, Pal.lightFlame, 0.8f));
        up(Fx.hitFlamePlasma, 0f, 0f, e -> Fxd.sizzle(e, Pal.heal, 0.8f));
        up(Fx.hitFlameBeam, 0f, 0f, e -> Fxd.sizzle(e, e.color, 0.9f));
        up(Fx.hitBeam, 0f, 0f, e -> Fxd.sizzle(e, e.color, 1f));
        up(Fx.hitMeltdown, 0f, 80f, e -> Fxd.sizzle(e, Pal.meltdownHit, 1.3f));
        up(Fx.hitMeltHeal, 0f, 80f, e -> Fxd.sizzle(e, Pal.heal, 1.3f));
        up(Fx.hitLaser, 0f, 0f, e -> Fxd.glow(e.x, e.y, 11f, Pal.heal, 0.4f * e.fout()));
        up(Fx.hitLaserColor, 0f, 0f, e -> Fxd.glow(e.x, e.y, 11f, e.color, 0.4f * e.fout()));

        // explosions
        up(Fx.flakExplosion, 36f, 110f, e -> Fxd.blast(e, Pal.lightOrange, 15f));
        up(Fx.flakExplosionBig, 46f, 150f, e -> Fxd.blast(e, Pal.bulletYellowBack, 26f));
        up(Fx.plasticExplosion, 40f, 130f, e -> Fxd.blast(e, Pal.plastaniumFront, 20f));
        up(Fx.plasticExplosionFlak, 42f, 140f, e -> Fxd.blast(e, Pal.plastaniumFront, 22f));
        up(Fx.blastExplosion, 40f, 140f, e -> Fxd.blast(e, Pal.missileYellowBack, 22f));
        up(Fx.sapExplosion, 40f, 130f, e -> Fxd.blast(e, Pal.sapBullet, 20f));
        up(Fx.massiveExplosion, 50f, 190f, e -> Fxd.blast(e, Pal.missileYellowBack, 33f));
        up(Fx.explosion, 46f, 130f, e -> Fxd.blast(e, Pal.lightOrange, 20f));
        up(Fx.sparkExplosion, 46f, 180f, e -> Fxd.blast(e, e.color, 28f));
        up(Fx.titanExplosionFrag, 36f, 130f, e -> Fxd.blast(e, e.color, 19f));
        up(Fx.titanExplosionSmall, 40f, 180f, e -> Fxd.blast(e, e.color, 30f));
        up(Fx.titanExplosion, 54f, 240f, e -> Fxd.blast(e, e.color, 44f));
        up(Fx.titanExplosionLarge, 68f, 330f, e -> Fxd.blast(e, e.color, 62f));
        up(Fx.scatheExplosionSmall, 0f, 200f, e -> Fxd.blast(e, e.color, 34f));
        up(Fx.scatheExplosion, 0f, 320f, e -> Fxd.blast(e, e.color, 58f));
        up(Fx.instBomb, 36f, 170f, e -> Fxd.blast(e, Pal.bulletYellowBack, 28f));
        up(Fx.instHit, 36f, 200f, e -> Fxd.blast(e, Pal.bulletYellowBack, 24f));
        up(Fx.railHit, 34f, 200f, e -> Fxd.blast(e, Pal.orangeSpark, 22f));
        up(Fx.coreExplosion, 0f, 400f, e -> Fxd.blast(e, e.color, 72f));
        // these three pick their own lifetime every frame, so they are only layered, never stretched
        up(Fx.dynamicExplosion, 0f, 0f, e -> Fxd.blast(e, Pal.lightOrange, Math.min(8f + e.rotation * 10f, 95f)));
        up(Fx.reactorExplosion, 0f, 0f, e -> Fxd.blast(e, Pal.reactorPurple, 85f));
        up(Fx.impactReactorExplosion, 0f, 0f, e -> Fxd.blast(e, Pal.lighterOrange, 95f));

        // trails and fire
        up(Fx.missileTrail, 0f, 0f, e -> Fxd.halo(e, e.color));
        up(Fx.missileTrailShort, 0f, 0f, e -> Fxd.halo(e, e.color));
        up(Fx.artilleryTrail, 0f, 0f, e -> Fxd.halo(e, e.color));
        up(Fx.colorTrail, 0f, 0f, e -> Fxd.halo(e, e.color));
        up(Fx.incendTrail, 0f, 0f, e -> Fxd.halo(e, Pal.lightOrange));
        up(Fx.fire, 0f, 0f, e -> Fxd.glow(e.x, e.y, 10f * e.fslope(), Pal.lightFlame, 0.15f * e.fslope()));
        up(Fx.burning, 0f, 0f, e -> Fxd.glow(e.x, e.y, 9f * e.fslope(), Pal.lightFlame, 0.13f * e.fslope()));

        // blocks and shields
        up(Fx.placeBlock, 26f, 0f, e -> {
            float half = tilesize / 2f * e.rotation;
            float flash = Fxd.fade(e, 11f);
            if(flash > 0f) Fxd.glow(e.x, e.y, half * 1.9f + 5f, Pal.accent, 0.22f * flash);

            Draw.color(Color.white, Pal.accent, e.fin());
            Lines.stroke(1.2f * e.fout() + 0.2f);
            float move = half * Mathf.sqrt2 + 1f + 7f * e.finpow();
            for(int i = 0; i < 4; i++){
                float ang = 45f + i * 90f;
                Lines.lineAngle(e.x + Angles.trnsx(ang, move), e.y + Angles.trnsy(ang, move), ang, 3.5f * e.fout() + 0.5f);
            }
        });
        up(Fx.breakBlock, 26f, 0f, e -> {
            float half = tilesize / 2f * e.rotation;
            Fxd.smoke(e, 30, Fxd.count(2f + e.rotation * 2f), half * 1.7f + 4f, 2.2f + e.rotation, 0f, 180f, Fxd.smokeColor, 0.3f);
            Fxd.embers(e, 31, Fxd.count(2f + e.rotation * 2f), half * 1.8f + 6f, 1.4f, 0f, 180f, Pal.remove, Fxd.emberEnd);
        });
        up(Fx.healBlockFull, 0f, 0f, e -> {
            if(e.data instanceof Block block){
                Fxd.glow(e.x, e.y, block.size * tilesize * 0.95f, e.color, 0.2f * e.fout());
            }
        });
        up(Fx.absorb, 0f, 0f, e -> Fxd.glow(e.x, e.y, 10f, Pal.accent, 0.4f * e.fout()));
        up(Fx.shieldBreak, 0f, 0f, e -> {
            float radius = Math.min(e.rotation, 140f);
            Fxd.glow(e.x, e.y, radius, e.color, 0.22f * e.fout());
            Fxd.sparks(e, 32, Fxd.count(12f), radius * 1.15f, 7f, 1.6f, 0f, 180f, Color.white, e.color);
        });
        up(Fx.spawn, 0f, 0f, e -> {
            Fxd.glow(e.x, e.y, 6f + 16f * e.fin(), Pal.accent, 0.3f * e.fout());
            Fxd.sparks(e, 33, Fxd.count(4f), 20f, 4f, 1f, 0f, 180f, Color.white, Pal.accent);
        });
    }

    private static void heavyMuzzle(EffectContainer e, Color color){
        float flash = Fxd.fade(e, 12f);
        if(flash > 0f){
            Fxd.glow(e.x, e.y, 46f * (0.5f + 0.5f * flash), color, 0.55f * flash);
        }
        Fxd.sparks(e, 21, Fxd.count(9f), 70f, 11f, 1.8f, e.rotation, 20f, Fxd.hot, color);
        Fxd.smoke(e, 22, Fxd.count(5f), 34f, 7f, e.rotation, 28f, Fxd.smokeColor, 0.3f);
    }

    private static void flameGlow(EffectContainer e, Color color){
        float d = 6f + 24f * e.fin();
        Fxd.glow(e.x + Angles.trnsx(e.rotation, d), e.y + Angles.trnsy(e.rotation, d), 9f + 9f * e.fin(), color, 0.13f * e.fout());
    }

    /**
     * Registers an upgrade.
     * @param life new total lifetime; values not above the vanilla lifetime leave timing untouched
     * @param clip minimum clip size needed by the extra particles
     */
    private static void up(Effect fx, float life, float clip, Cons<EffectContainer> extra){
        //custom effect subclasses do not draw through their renderer field
        if(fx == null || fx == Fx.none || fx.getClass() != Effect.class) return;
        entries.add(new Entry(fx, life, clip, extra));
    }

    public static void set(boolean enabled){
        if(enabled == applied) return;
        applied = enabled;
        for(Entry entry : entries){
            if(enabled){
                entry.apply();
            }else{
                entry.revert();
            }
        }
    }

    public static int size(){
        return entries.size;
    }

    private static class Entry{
        final Effect fx;
        final Cons<EffectContainer> oldRenderer, newRenderer;
        final float oldLife, newLife, oldClip, newClip;

        Entry(Effect fx, float life, float clip, Cons<EffectContainer> extra){
            this.fx = fx;
            this.oldRenderer = fx.renderer;
            this.oldLife = fx.lifetime;
            this.oldClip = fx.clip;
            this.newLife = Math.max(life, oldLife);
            this.newClip = Math.max(clip, oldClip);

            if(newLife > oldLife){
                //the vanilla part keeps its original timing; only the extras use the longer timeline
                newRenderer = e -> {
                    float full = e.lifetime;
                    if(e.time <= oldLife){
                        e.lifetime = oldLife;
                        oldRenderer.get(e);
                        e.lifetime = full;
                        Draw.reset();
                        Draw.z(fx.layer);
                    }
                    extra.get(e);
                };
            }else{
                newRenderer = e -> {
                    oldRenderer.get(e);
                    Draw.reset();
                    Draw.z(fx.layer);
                    extra.get(e);
                };
            }
        }

        void apply(){
            fx.renderer = newRenderer;
            fx.lifetime = newLife;
            fx.clip = newClip;
        }

        void revert(){
            fx.renderer = oldRenderer;
            fx.lifetime = oldLife;
            fx.clip = oldClip;
        }
    }
}
