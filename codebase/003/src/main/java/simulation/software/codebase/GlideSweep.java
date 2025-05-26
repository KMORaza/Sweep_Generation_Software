package simulation.software.codebase;

public class GlideSweep {
    public double calculateFrequency(double startFreq, double endFreq, double currentTime, double sweepTime) {
        if (currentTime >= sweepTime || startFreq <= 0 || endFreq <= 0) {
            return startFreq;
        }
        // Logarithmic sweep: f(t) = f_start * (f_end / f_start)^(t / T)
        return startFreq * Math.pow(endFreq / startFreq, currentTime / sweepTime);
    }
}