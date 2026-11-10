package lumina.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;

/** Animated overlay for the main menu: soft shading behind the buttons and logo plus drifting embers. */
public class MenuBackdrop extends Element{
    public static boolean enabled = true;

    private static final int motes = 80;
    private static final Color
    shade = Color.valueOf("05070d"),
    warm = Color.valueOf("ffd37f"),
    ember = Color.valueOf("f68021"),
    cool = Color.valueOf("a9d8ff");

    private final TextureRegion glow = Core.atlas.find("lumina-glow");

    public MenuBackdrop(){
        setFillParent(true);
        touchable = Touchable.disabled;
    }

    /** Places the backdrop right above the menu's terrain renderer, below the buttons and logo. */
    public static void install(){
        if(Vars.ui.menuGroup.getChildren().isEmpty()) return;

        if(Vars.ui.menuGroup.getChildren().first() instanceof Group group && group.getChildren().size > 0){
            group.addChildAt(1, new MenuBackdrop());
        }
    }

    @Override
    public void draw(){
        if(!enabled) return;

        float w = Core.graphics.getWidth(), h = Core.graphics.getHeight();
        float time = Time.globalTime;

        float clear = Tmp.c1.set(shade).a(0f).toFloatBits();
        float soft = Tmp.c1.set(shade).a(0.22f).toFloatBits();
        float mid = Tmp.c1.set(shade).a(0.55f).toFloatBits();
        float deep = Tmp.c1.set(shade).a(0.78f).toFloatBits();

        //overall tint, then gradients behind the button column, the logo and along the bottom
        Fill.quad(0f, 0f, soft, 0f, h, soft, w, h, soft, w, 0f, soft);
        Fill.quad(0f, 0f, deep, 0f, h, deep, w * 0.6f, h, clear, w * 0.6f, 0f, clear);
        Fill.quad(0f, h, mid, w, h, mid, w, h * 0.66f, clear, 0f, h * 0.66f, clear);
        Fill.quad(0f, 0f, mid, w, 0f, mid, w, h * 0.3f, clear, 0f, h * 0.3f, clear);
        Fill.quad(w, 0f, mid, w, h, mid, w * 0.78f, h, clear, w * 0.78f, 0f, clear);

        if(!glow.found()) return;

        //the UI batch does not flush on blend changes by itself
        Draw.flush();
        Draw.blend(Blending.additive);

        //halo behind the logo
        TextureRegion logo = Core.atlas.find("logo");
        float logow = Math.min(logo.width * Scl.scl(1f) * logo.scale, w - Scl.scl(20f));
        float logoh = logow * (float)logo.height / logo.width;
        float ly = (int)(h - Core.scene.marginTop - 6f - logoh) + logoh / 2f;
        Draw.color(warm, 0.3f + Mathf.absin(time, 55f, 0.08f));
        Draw.rect(glow, w / 2f, ly, logow * 1.5f, logoh * 3.4f);

        //rising embers
        for(int i = 0; i < motes; i++){
            long seed = i * 977L + 13L;
            float speed = Mathf.randomSeed(seed, 0.12f, 0.55f);
            float size = Scl.scl(Mathf.randomSeed(seed + 1, 8f, 38f));
            float span = h + size * 2f;
            float x = Mathf.randomSeed(seed + 2, 0f, w)
                + Mathf.sin(time + Mathf.randomSeed(seed + 3, 0f, 900f), Mathf.randomSeed(seed + 4, 70f, 160f), Scl.scl(34f));
            float y = Mathf.mod(Mathf.randomSeed(seed + 5, 0f, span) + time * speed * Scl.scl(1f), span) - size;
            float twinkle = 0.55f + 0.45f * Mathf.sin(time + Mathf.randomSeed(seed + 6, 0f, 500f), Mathf.randomSeed(seed + 7, 18f, 46f), 1f);
            //fade in near the bottom and out near the top
            float fade = Mathf.clamp(y / (h * 0.2f)) * Mathf.clamp((h - y) / (h * 0.3f));
            float pick = Mathf.randomSeed(seed + 8);

            Draw.color(pick < 0.6f ? warm : pick < 0.85f ? ember : cool, Mathf.randomSeed(seed + 9, 0.28f, 0.7f) * twinkle * fade);
            Draw.rect(glow, x, y, size, size);
            //bright core
            Draw.alpha(Draw.getColor().a * 0.9f);
            Draw.rect(glow, x, y, size * 0.36f, size * 0.36f);
        }

        Draw.flush();
        Draw.blend();
        Draw.reset();
    }
}
