package simulation.software.codebase;

public class WaveformGenerator {
    private NoiseGenerator noiseGenerator = new NoiseGenerator();
    private ModulationGenerator modulationGenerator = new ModulationGenerator();

    public double[] generateTimeArray(double duration) {
        int samples = 1000;
        double[] time = new double[samples];
        for (int i = 0; i < samples; i++) {
            time[i] = i * duration / samples;
        }
        return time;
    }

    public double[] generateWaveform(String type, double frequency, double amplitude, double[] time, String noiseType, double noiseAmplitude, String modulationType, double modulationIndex, double modulationFrequency) {
        double[] waveform = new double[time.length];
        for (int i = 0; i < time.length; i++) {
            double t = time[i];
            switch (type.toLowerCase()) {
                case "sine":
                    waveform[i] = amplitude * Math.sin(2 * Math.PI * frequency * t);
                    break;
                case "square":
                    waveform[i] = amplitude * Math.signum(Math.sin(2 * Math.PI * frequency * t));
                    break;
                case "triangle":
                    waveform[i] = amplitude * (2 * Math.abs(2 * (t * frequency - Math.floor(t * frequency + 0.5))) - 1);
                    break;
                default:
                    waveform[i] = 0;
            }
        }

        // Apply modulation if specified
        waveform = modulationGenerator.applyModulation(modulationType, waveform, frequency, amplitude, modulationFrequency, modulationIndex, time);

        // Add noise if specified
        if (!noiseType.equals("None") && noiseAmplitude > 0) {
            double[] noise = noiseGenerator.generateNoise(noiseType, noiseAmplitude, waveform.length);
            for (int i = 0; i < waveform.length; i++) {
                waveform[i] += noise[i];
                // Clamp to [-5, 5] to prevent extreme values
                if (waveform[i] > 5.0) waveform[i] = 5.0;
                if (waveform[i] < -5.0) waveform[i] = -5.0;
            }
        }

        return waveform;
    }
}