package simulation.software.codebase;

public class TimeSweep {
    public double calculateAmplitude(double baseAmplitude, double currentTime, double sweepTime) {
        if (currentTime >= sweepTime) {
            return baseAmplitude;
        }
        // Linear amplitude sweep from baseAmplitude to 0 over sweep time
        return baseAmplitude * (1 - currentTime / sweepTime);
    }
}