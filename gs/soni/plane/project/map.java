package gs.soni.plane.project;

public class map {

    public int tileOff;
    public int palLine;
    public boolean HighPlane;
    public boolean XFlip;
    public boolean YFlip;

    public map() {
        this.tileOff = 0;
        this.palLine = 0;
        this.HighPlane = false;
        this.XFlip = false;
        this.YFlip = false;
    }

    public map(int tileOff, int palLine, boolean HighPlane, boolean XFlip, boolean YFlip) {
        this.tileOff = tileOff;
        this.palLine = palLine;
        this.HighPlane = HighPlane;
        this.XFlip = XFlip;
        this.YFlip = YFlip;
    }

    public map(map m) {
        tileOff = m.tileOff;
        palLine = m.palLine;
        HighPlane = m.HighPlane;
        XFlip = m.XFlip;
        YFlip = m.YFlip;
    }

    public boolean isDefault() {
        return tileOff == 0 && palLine == 0 && !XFlip && !YFlip && !HighPlane;
    }
}
