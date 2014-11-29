package gs.soni.plane.util;

public class SortType {
    private String type;
    private String order;

    public SortType(){
    }

    public SortType(String type, String order){
        this.type = type;
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public String getOrder() {
        return order;
    }
}
