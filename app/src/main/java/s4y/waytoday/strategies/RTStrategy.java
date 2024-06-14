package s4y.waytoday.strategies;

public class RTStrategy implements Strategy {
    @Override
    public long getMinDistance() {
        return 1;
    }

    @Override
    public long getMinMs() {
        return 1000;
    }
}
