package lumina;

import arc.*;
import arc.files.*;
import arc.util.*;
import lumina.audio.*;
import lumina.fx.*;
import lumina.graphics.*;
import lumina.ui.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.mod.*;

/** Lumina: a client-side visual, interface and audio overhaul. Adds no content and changes no gameplay. */
public class LuminaMod extends Mod{

    @Override
    public void packSprites(MultiPacker packer){
        try{
            UiTheme.pack(packer, root());
        }catch(Throwable t){
            Log.err("[Lumina] Failed to pack the UI theme; the vanilla interface is kept.", t);
        }
    }

    @Override
    public void init(){
        if(Vars.headless) return;

        Fi root = root();

        FxOverhaul.load();
        PostFx.init(root);
        UiTheme.apply();
        MenuBackdrop.install();
        LuminaSounds.load(root);

        addSettings();
        refresh();

        Log.info("[Lumina] Loaded: @ upgraded effects.", FxOverhaul.size());
    }

    private Fi root(){
        return Vars.mods.getMod(getClass()).root;
    }

    /** Pushes the current settings into every subsystem. */
    private static void refresh(){
        var s = Core.settings;

        FxOverhaul.set(s.getBool("lumina-fx", true));
        Fxd.detail = s.getInt("lumina-fx-detail", 100) / 100f;
        ContentTweaks.set(s.getBool("lumina-trails", true));

        PostFx.enabled = s.getBool("lumina-postfx", true);
        PostFx.grade = s.getInt("lumina-grade", 60) / 100f;
        PostFx.vignette = s.getInt("lumina-vignette", 35) / 100f;

        MenuBackdrop.enabled = s.getBool("lumina-menu", true);
        LuminaSounds.ui = s.getBool("lumina-uisounds", true);
        LuminaSounds.world = s.getBool("lumina-worldsounds", true);
    }

    private static void addSettings(){
        Vars.ui.settings.addCategory(Core.bundle.get("lumina.settings"), Icon.effect, t -> {
            t.checkPref("lumina-fx", true, v -> refresh());
            t.sliderPref("lumina-fx-detail", 100, 40, 160, 10, v -> v + "%", v -> refresh());
            t.checkPref("lumina-trails", true, v -> refresh());
            t.checkPref("lumina-postfx", true, v -> refresh());
            t.sliderPref("lumina-grade", 60, 0, 100, 5, v -> v + "%", v -> refresh());
            t.sliderPref("lumina-vignette", 35, 0, 100, 5, v -> v + "%", v -> refresh());
            t.checkPref("lumina-menu", true, v -> refresh());
            t.checkPref(UiTheme.setting, true);
            t.checkPref("lumina-uisounds", true, v -> refresh());
            t.checkPref("lumina-worldsounds", true, v -> refresh());
        });
    }
}
