package pt.ulisboa.tecnico.cnv.a18.loadbalancer;

public class ScalingPolicy {
    //Attributes used in the scaling policy
    private int _numServers;
    private long _secondsOverThreshold;
    private long _loadThreshold;

    public ScalingPolicy(int numServers, long secondsOverThreshold, long loadThreshold) {
        _numServers = numServers;
        _secondsOverThreshold = secondsOverThreshold;
        _loadThreshold = loadThreshold;
    }

    public int get_numServers() {
        return _numServers;
    }

    public long get_secondsOverThreshold() {
        return _secondsOverThreshold;
    }

    public long get_loadThreshold() {
        return _loadThreshold;
    }
}
