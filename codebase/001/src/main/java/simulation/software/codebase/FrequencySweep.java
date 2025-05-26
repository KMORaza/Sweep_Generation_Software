package simulation.software.codebase;

public class FrequencySweep {
    public double calculateFrequency(String sweepType, double startFreq, double endFreq, double currentTime, double sweepTime) {
        if (currentTime >= sweepTime || startFreq <= 0 || endFreq <= 0) {
            return startFreq;
        }
        double progress = currentTime / sweepTime;
        switch (sweepType) {
            case "Linear":
                // Linear sweep: f(t) = f_start + (f_end - f_start) * (t / T)
                return startFreq + (endFreq - startFreq) * progress;
            case "Logarithmic":
                // Logarithmic sweep: f(t) = f_start * (f_end / f_start)^(t / T)
                return startFreq * Math.pow(endFreq / startFreq, progress);
            case "Bidirectional":
                // Bidirectional sweep: forward (0 to T/2), reverse (T/2 to T)
                double halfSweepTime = sweepTime / 2.0;
                if (currentTime < halfSweepTime) {
                    // Forward sweep
                    progress = currentTime / halfSweepTime;
                    return startFreq + (endFreq - startFreq) * progress;
                } else {
                    // Reverse sweep
                    progress = (currentTime - halfSweepTime) / halfSweepTime;
                    return endFreq - (endFreq - startFreq) * progress;
                }
            default:
                return startFreq;
        }
    }
}