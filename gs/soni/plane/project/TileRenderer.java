package gs.soni.plane.project;

import gs.soni.plane.v;

public class TileRenderer implements Runnable{
    @Override
    public void run() {
        if (v.TileRender == -1) {
            tileLoader.renderTrans();
            v.TileRender ++;
        }

        while (v.TileRender < tileLoader.GetTextureAmount(0)) {
            if(v.TileRender == -1){
                return;
            }

            tileLoader.render(v.TileRender);
            v.TileRender ++;

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
    }
}
