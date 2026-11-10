package lumina.audio;

import arc.*;
import arc.audio.*;
import arc.files.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

/** Extra sounds: interface feedback and a low-end layer for big explosions. Vanilla sounds are never replaced. */
public final class LuminaSounds{
    public static boolean ui = true, world = true;

    private static Sound hover = new Sound(), open = new Sound(), thump = new Sound(), debris = new Sound();
    private static Element lastHover;
    private static Dialog lastDialog;

    private LuminaSounds(){}

    public static void load(Fi root){
        Fi dir = root.child("sounds");
        hover = load(dir, "lumina-hover");
        open = load(dir, "lumina-open");
        thump = load(dir, "lumina-thump");
        debris = load(dir, "lumina-debris");

        hover.setBus(Vars.control.sound.uiBus);
        open.setBus(Vars.control.sound.uiBus);
        hover.setMinInterval(45);
        open.setMinInterval(150);
        thump.setMinInterval(110);
        debris.setMinInterval(70);

        Core.scene.root.addCaptureListener(new InputListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Element from){
                //mouse hover only; touches report a pointer index
                if(pointer != -1) return;

                Button button = buttonOf(event.targetActor);
                if(button != null && button != lastHover && !button.isDisabled() && ui){
                    hover.play(0.3f, Mathf.random(0.96f, 1.04f), 0f);
                }
                lastHover = button;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element to){
                if(pointer == -1 && lastHover != null && (to == null || !to.isDescendantOf(lastHover))){
                    lastHover = null;
                }
            }
        });

        Events.run(Trigger.update, () -> {
            Dialog dialog = Core.scene.getDialog();
            if(dialog != lastDialog){
                if(dialog != null && ui) open.play(0.32f);
                lastDialog = dialog;
            }
        });

        Events.on(UnitDestroyEvent.class, e -> {
            if(!world || e.unit == null || e.unit.hitSize < 18f) return;
            thump.at(e.unit.x, e.unit.y, Mathf.random(0.85f, 1.1f), Mathf.clamp(e.unit.hitSize / 55f, 0.35f, 1f));
        });

        Events.on(BlockDestroyEvent.class, e -> {
            if(!world || e.tile == null) return;
            Building build = e.tile.build;
            if(build == null) return;

            int size = build.block.size;
            if(size >= 3){
                thump.at(build.x, build.y, Mathf.random(0.9f, 1.15f), Mathf.clamp(0.3f + size * 0.12f, 0f, 1f));
            }else{
                debris.at(build.x, build.y, Mathf.random(0.9f, 1.2f), 0.3f + size * 0.1f);
            }
        });
    }

    private static Sound load(Fi dir, String name){
        try{
            return new Sound(dir.child(name + ".ogg"));
        }catch(Throwable t){
            Log.err("[Lumina] Failed to load sound " + name, t);
            return new Sound();
        }
    }

    private static @Nullable Button buttonOf(@Nullable Element element){
        while(element != null){
            if(element instanceof Button b) return b;
            element = element.parent;
        }
        return null;
    }
}
