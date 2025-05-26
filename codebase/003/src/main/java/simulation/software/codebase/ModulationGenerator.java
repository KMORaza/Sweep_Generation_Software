package simulation.software.codebase;

public class ModulationGenerator {

    public double[] applyModulation(String modulationType, double[] carrierWaveform, double carrierFrequency, double carrierAmplitude, double modulationFrequency, double modulationIndex, double[] time) {
        double[] modulatedWaveform = new double[time.length];

        switch (modulationType.toLowerCase()) {
            case "am":
                applyAM(carrierWaveform, modulationFrequency, modulationIndex, time, modulatedWaveform);
                break;
            case "fm":
                applyFM(carrierWaveform, carrierFrequency, modulationFrequency, modulationIndex, time, modulatedWaveform);
                break;
            case "pm":
                applyPM(carrierWaveform, carrierFrequency, modulationFrequency, modulationIndex, time, modulatedWaveform);
                break;
            default:
                // No modulation (None)
                System.arraycopy(carrierWaveform, 0, modulatedWaveform, 0, carrierWaveform.length);
                break;
        }

        return modulatedWaveform;
    }

    private void applyAM(double[] carrierWaveform, double modulationFrequency, double modulationIndex, double[] time, double[] modulatedWaveform) {
        for (int i = 0; i < time.length; i++) {
            double t = time[i];
            // AM: (1 + m * sin(2π f_m t)) * carrier
            double modulation = modulationIndex * Math.sin(2 * Math.PI * modulationFrequency * t);
            modulatedWaveform[i] = (1 + modulation) * carrierWaveform[i];
        }
    }

    private void applyFM(double[] carrierWaveform, double carrierFrequency, double modulationFrequency, double modulationIndex, double[] time, double[] modulatedWaveform) {
        for (int i = 0; i < time.length; i++) {
            double t = time[i];
            // FM: A_c * sin(2π f_c t + β * sin(2π f_m t))
            double phase = 2 * Math.PI * carrierFrequency * t + modulationIndex * Math.sin(2 * Math.PI * modulationFrequency * t);
            modulatedWaveform[i] = carrierWaveform[i] * Math.cos(phase); // Simplified: modulate phase of carrier
        }
    }

    private void applyPM(double[] carrierWaveform, double carrierFrequency, double modulationFrequency, double modulationIndex, double[] time, double[] modulatedWaveform) {
        for (int i = 0; i < time.length; i++) {
            double t = time[i];
            // PM: A_c * sin(2π f_c t + β * sin(2π f_m t))
            double phase = 2 * Math.PI * carrierFrequency * t + modulationIndex * Math.sin(2 * Math.PI * modulationFrequency * t);
            modulatedWaveform[i] = carrierWaveform[i] * Math.cos(phase); // Simplified: modulate phase of carrier
        }
    }
}