package gs.soni.plane.util;

public class EventHandler implements Cloneable {
    private int mode;
    public int Priority;
    private String string;

    public EventHandler(int mode, int priority, String string){
        this.mode = mode;
        this.Priority = priority;
        this.string = string;
    }

    protected void invoke(){

    }

    public int getMode() {
        return mode;
    }

    public byte getPriority() {
        return (byte) Priority;
    }

    public String getString() {
        return string;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPriority(int priority) {
        this.Priority = priority;
    }

    public void setString(String string) {
        this.string = string;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
