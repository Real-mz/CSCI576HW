
public class Point3 {
    private int r,g,b;

    public int getr() {
        return r;
    }

    public void setr(int r) {
        this.r = r;
    }

    public int getg() {
        return g;
    }

    public void setg(int g) {
        this.g = g;
    }

    public int getb() {
        return b;
    }

    public void setb(int b) {
        this.b = b;
    }

    public void setLocation(int r,int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Point3(int r, int g, int b) {
        super();
        this.r = r;
        this.g = g;
        this.b = b;
    }
    public Point3() {
        super();
    
    }

    double getDistance(Point3 p)
    {
        double diff = (r-p.r)*(r-p.r)+(g-p.g)*(g-p.g)+(b-p.b)*(b-p.b);

        return Math.sqrt(diff);
    }

    boolean equals(Point3 p){
        if(r==p.r && g == p.g && b == p.b){
            return true;
        }else{
            return false;
        }
    }

    }
