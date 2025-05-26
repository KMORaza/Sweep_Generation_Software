package simulation.software.codebase;

import java.util.Random;

public class NoiseGenerator {
    private Random random = new Random();

    public double[] generateNoise(String noiseType, double amplitude, int samples) {
        double[] noise = new double[samples];
        switch (noiseType.toLowerCase()) {
            case "white":
                generateWhiteNoise(noise, amplitude);
                break;
            case "pink":
                generatePinkNoise(noise, amplitude);
                break;
            case "brownian":
                generateBrownianNoise(noise, amplitude);
                break;
            default:
                // No noise (None selected)
                return noise; // Returns array of zeros
        }
        return noise;
    }

    private void generateWhiteNoise(double[] noise, double amplitude) {
        for (int i = 0; i < noise.length; i++) {
            noise[i] = amplitude * (2.0 * random.nextDouble() - 1.0); // Random values in [-amplitude, amplitude]
        }
    }

    private void generatePinkNoise(double[] noise, double amplitude) {
        // Simplified pink noise using Voss-McCartney algorithm approximation
        double[] white = new double[noise.length];
        generateWhiteNoise(white, amplitude);
        double b0 = 0, b1 = 0, b2 = 0;
        for (int i = 0; i < noise.length; i++) {
            double whiteVal = white[i];
            b0 = 0.99886 * b0 + whiteVal * 0.0555179;
            b1 = 0.99332 * b1 + whiteVal * 0.0750759;
            b2 = 0.96900 * b2 + whiteVal * 0.1538520;
            noise[i] = amplitude * (b0 + b1 + b2 + whiteVal * 0.1848940) / 2.0;
        }
    }

    private void generateBrownianNoise(double[] noise, double amplitude) {
        // Brownian noise via integration of white noise
        double[] white = new double[noise.length];
        generateWhiteNoise(white, amplitude);
        double sum = 0;
        for (int i = 0; i < noise.length; i++) {
            sum += white[i];
            noise[i] = sum * 0.1; // Scale to prevent excessive growth
            // Clamp to [-amplitude, amplitude]
            if (noise[i] > amplitude) noise[i] = amplitude;
            if (noise[i] < -amplitude) noise[i] = -amplitude;
        }
    }
}