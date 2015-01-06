package gs.soni.plane.project;

import gs.soni.plane.SP;

public class TileRenderer implements Runnable {
    /* offset for tileRenderer */
    private int off = 0;

    @Override
    public void run() {
        /* render transparent */
        tileLoader.renderTrans();

        /* loop until all done */
        while (off < tileLoader.GetTextureAmount(0)) {
            /* render next tile */
            tileLoader.render(off);
            off ++;
            /* repaint all windows */
            SP.getWM().repaintAll();

            /* delay some time to not overload CPU */
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
    }
}
