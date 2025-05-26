package simulation.software.codebase;

public class SteppedSweep {
    private static final int STEPS = 10;
    private static final double STEP_DURATION = 0.5; // seconds per step

    public double calculateFrequency(double startFreq, double endFreq, double currentTime, double sweepTime, boolean isLog) {
        if (currentTime >= sweepTime) {
            return startFreq;
        }
        int currentStep = (int) (currentTime / STEP_DURATION);
        if (currentStep >= STEPS) {
            return endFreq;
        }
        if (isLog && startFreq > 0 && endFreq > 0) {
            // Logarithmic steps
            double logStart = Math.log10(startFreq);
            double logEnd = Math.log10(endFreq);
            double logFreq = logStart + (logEnd - logStart) * currentStep / (STEPS - 1);
            return Math.pow(10, logFreq);
        } else {
            // Linear steps
            return startFreq + (endFreq - startFreq) * currentStep / (STEPS - 1);
        }
    }
}