package pt.ulisboa.tecnico.cnv.a18.storage;

public class Request {
    private String key;
    private int x0;
    private int x1;
    private int y0;
    private int y1;
    private int s0;
    private int s1;
    private int height;
    private int width;
    private String strategy;
    private String image;

    public Request(){}

    public Request(String key, int x0, int x1, int y0, int y1, int s0, int s1, int height, int width, String strategy, String image) {
        this.key = key;
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.height = height;
        this.width = width;
        this.strategy = strategy;
        this.image = image;
    }

    public String getKey(){
        return this.key;
    }

    public void setKey(String key){
        this.key = key;
    }

    public int getX0() {
        return x0;
    }

    public void setX0(int x0) {
        this.x0 = x0;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY0() {
        return y0;
    }

    public void setY0(int y0) {
        this.y0 = y0;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getS0() {
        return s0;
    }

    public void setS0(int s0) {
        this.s0 = s0;
    }

    public int getS1() {
        return s1;
    }

    public void setS1(int s1) {
        this.s1 = s1;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
