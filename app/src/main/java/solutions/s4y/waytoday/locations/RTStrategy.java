package solutions.s4y.waytoday.locations;

public class RTStrategy implements Strategy {
    @Override
    public long getMinDistance() {
        return 1;
    }

    @Override
    public long getMinTime() {
        return 1000;
    }
}
