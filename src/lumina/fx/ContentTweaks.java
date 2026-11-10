package lumina.fx;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import mindustry.entities.bullet.*;
import mindustry.type.*;

import static mindustry.Vars.*;

/** Purely visual content changes: ribbon trails for bullets and aircraft. Nothing here affects gameplay. */
public final class ContentTweaks{
    private static final Seq<Runnable> reverts = new Seq<>();
    private static boolean applied;

    private ContentTweaks(){}

    public static void set(boolean enabled){
        if(enabled == applied) return;
        applied = enabled;

        if(enabled){
            apply();
        }else{
            reverts.each(Runnable::run);
            reverts.clear();
        }
    }

    private static void apply(){
        for(BulletType type : content.bullets()){
            if(!(type instanceof BasicBulletType b) || type instanceof ArtilleryBulletType) continue;
            if(b.trailLength > 0 || b.speed < 1.2f || b.lifetime < 6f) continue;

            int oldLength = b.trailLength;
            float oldWidth = b.trailWidth, oldDrawSize = b.drawSize;
            Color oldColor = b.trailColor;

            b.trailLength = Mathf.clamp(Mathf.round(b.height * 2.2f / b.speed) + 2, 4, 14);
            b.trailWidth = Math.max(b.width * 0.21f, 0.9f);
            //the trail colour is shared with trail effects, so only replace it when those are unused
            if(b.trailChance <= 0f && b.trailInterval <= 0f){
                b.trailColor = b.backColor;
            }
            b.drawSize = Math.max(b.drawSize, b.trailLength * b.speed * 2f);

            reverts.add(() -> {
                b.trailLength = oldLength;
                b.trailWidth = oldWidth;
                b.trailColor = oldColor;
                b.drawSize = oldDrawSize;
            });
        }

        for(UnitType unit : content.units()){
            if(!unit.flying || unit.trailLength > 0 || unit.engineSize <= 0f) continue;

            int oldLength = unit.trailLength;
            unit.trailLength = Mathf.clamp(Mathf.round(unit.hitSize * 0.55f) + 3, 5, 16);
            reverts.add(() -> unit.trailLength = oldLength);
        }
    }
}
