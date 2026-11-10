package lumina.ui;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.ui.*;

/** Restyled interface: replaces the vanilla nine-patches in the UI atlas and adjusts a few shared styles. */
public final class UiTheme{
    public static final String setting = "lumina-uitheme";

    private static boolean packed;

    private UiTheme(){}

    /**
     * Swaps vanilla UI sprites for the restyled ones while the atlas is being repacked.
     * Each replacement has the same size as the original, so the vanilla nine-patch splits and paddings are reused as-is.
     */
    public static void pack(MultiPacker packer, Fi root){
        if(!Core.settings.getBool(setting, true)) return;

        boolean bleed = Core.settings.getBool("linear", true);

        for(Fi file : root.child("lumina-ui").list()){
            if(!file.extEquals("png")) continue;

            String name = file.nameWithoutExtension();
            if(!Core.atlas.has(name)) continue;

            AtlasRegion base = Core.atlas.find(name);

            Pixmap pix = new Pixmap(file.readBytes());
            if(pix.width == base.width && pix.height == base.height){
                if(bleed) Pixmaps.bleed(pix, 2);
                packer.add(PageType.ui, name, new PixmapRegion(pix), base.splits, base.pads);
                packed = true;
            }else{
                Log.warn("[Lumina] Skipping UI sprite '@': size differs from the game's.", name);
            }
            pix.dispose();
        }
    }

    /** Style tweaks that go with the repacked sprites. */
    public static void apply(){
        if(!packed) return;

        var white = (TextureRegionDrawable)Tex.whiteui;

        //deep blue-black dimming behind dialogs instead of flat black
        Styles.defaultDialog.stageBackground = white.tint(0.022f, 0.028f, 0.05f, 0.9f);

        //main menu / mode buttons: accent sweep instead of flat grey blocks
        Styles.flatToggleMenut.over = new Sweep(Pal.accent, 0.2f, false);
        Styles.flatToggleMenut.down = new Sweep(Pal.accent, 0.5f, true);
        Styles.flatToggleMenut.checked = new Sweep(Pal.accent, 0.38f, true);
    }

    /** Horizontal fade from a colour to transparent, optionally with a solid bar on the left edge. */
    static class Sweep extends BaseDrawable{
        final Color color;
        final float alpha;
        final boolean bar;

        Sweep(Color color, float alpha, boolean bar){
            this.color = color;
            this.alpha = alpha;
            this.bar = bar;
        }

        @Override
        public void draw(float x, float y, float width, float height){
            float parentAlpha = Draw.getColor().a;
            float from = Tmp.c1.set(color).a(alpha * parentAlpha).toFloatBits();
            float to = Tmp.c1.set(color).a(0f).toFloatBits();

            Fill.quad(x, y, from, x, y + height, from, x + width, y + height, to, x + width, y, to);

            if(bar){
                Draw.color(color, parentAlpha);
                Fill.crect(x, y, Scl.scl(4f), height);
            }
            Draw.color();
        }
    }
}
