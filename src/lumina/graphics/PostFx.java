package lumina.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.gl.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;

/** Full-screen colour grading and vignette for the world view. The HUD is drawn afterwards and stays untouched. */
public final class PostFx{
    public static boolean enabled = true;
    /** 0-1 strength of the contrast/saturation grade and of the vignette. */
    public static float grade = 0.6f, vignette = 0.35f;

    private static FrameBuffer buffer;
    private static Shader shader;
    private static boolean capturing;

    private PostFx(){}

    public static void init(Fi root){
        try{
            shader = new Shader(Core.files.internal("shaders/screenspace.vert"), root.child("shaders").child("lumina-grade.frag")){
                @Override
                public void apply(){
                    setUniformf("u_grade", grade);
                    setUniformf("u_vignette", vignette);
                }
            };
            buffer = new FrameBuffer();
        }catch(Throwable t){
            Log.err("[Lumina] Post-processing is unavailable on this device.", t);
            return;
        }

        Events.run(Trigger.preDraw, PostFx::begin);
        Events.run(Trigger.postDraw, PostFx::end);
    }

    private static void begin(){
        //map screenshots and pixelation render into their own differently sized buffers
        if(!enabled || capturing || Vars.disableUI || Vars.renderer.pixelate || (grade < 0.01f && vignette < 0.01f)) return;

        buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        buffer.begin();
        capturing = true;
    }

    private static void end(){
        if(!capturing) return;
        capturing = false;

        buffer.end();
        buffer.blit(shader);
    }
}
