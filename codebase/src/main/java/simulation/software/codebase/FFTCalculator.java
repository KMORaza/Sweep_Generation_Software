package simulation.software.codebase;

public class FFTCalculator {
    // Computes the FFT magnitude spectrum of the input waveform
    public double[] computeFFTMagnitude(double[] input) {
        if (input == null || input.length < 2) {
            return new double[0];
        }

        // Ensure input length is a power of 2
        int n = nextPowerOfTwo(input.length);
        double[] paddedInput = new double[n];
        for (int i = 0; i < input.length; i++) {
            paddedInput[i] = input[i];
        }

        // Perform FFT
        Complex[] fftResult = fft(paddedInput);

        // Compute magnitude for positive frequencies
        int outputLength = n / 2;
        double[] magnitude = new double[outputLength];
        for (int i = 0; i < outputLength; i++) {
            magnitude[i] = Math.sqrt(fftResult[i].real * fftResult[i].real + fftResult[i].imag * fftResult[i].imag);
        }

        return magnitude;
    }

    // Computes the FFT phase spectrum of the input waveform
    public double[] computeFFTPhase(double[] input) {
        if (input == null || input.length < 2) {
            return new double[0];
        }

        // Ensure input length is a power of 2
        int n = nextPowerOfTwo(input.length);
        double[] paddedInput = new double[n];
        for (int i = 0; i < input.length; i++) {
            paddedInput[i] = input[i];
        }

        // Perform FFT
        Complex[] fftResult = fft(paddedInput);

        // Compute phase for positive frequencies
        int outputLength = n / 2;
        double[] phase = new double[outputLength];
        for (int i = 0; i < outputLength; i++) {
            phase[i] = Math.atan2(fftResult[i].imag, fftResult[i].real);
        }

        return phase;
    }

    // Computes the magnitude spectrum with sample rate for frequency bin alignment
    public double[] calculateMagnitudeSpectrum(double[] input, int sampleRate) {
        if (input == null || input.length < 2 || sampleRate <= 0) {
            return new double[0];
        }

        // Ensure input length is a power of 2
        int n = nextPowerOfTwo(input.length);
        double[] paddedInput = new double[n];
        for (int i = 0; i < input.length; i++) {
            paddedInput[i] = input[i];
        }

        // Perform FFT
        Complex[] fftResult = fft(paddedInput);

        // Compute magnitude for positive frequencies (up to Nyquist)
        int outputLength = n / 2;
        double[] magnitude = new double[outputLength];
        for (int i = 0; i < outputLength; i++) {
            magnitude[i] = Math.sqrt(fftResult[i].real * fftResult[i].real + fftResult[i].imag * fftResult[i].imag);
        }

        return magnitude;
    }

    // Cooley-Tukey FFT algorithm
    private Complex[] fft(double[] input) {
        int n = input.length;
        Complex[] x = new Complex[n];
        for (int i = 0; i < n; i++) {
            x[i] = new Complex(input[i], 0);
        }

        if (n <= 1) {
            return x;
        }

        // Bit-reversal permutation
        Complex[] result = new Complex[n];
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - Integer.numberOfLeadingZeros(n - 1));
            result[j] = x[i];
        }

        // Butterfly operations
        for (int size = 2; size <= n; size *= 2) {
            int halfSize = size / 2;
            double angle = -2 * Math.PI / size;
            Complex w = new Complex(Math.cos(angle), Math.sin(angle));
            for (int i = 0; i < n; i += size) {
                Complex wk = new Complex(1, 0);
                for (int j = 0; j < halfSize; j++) {
                    Complex t = wk.multiply(result[i + j + halfSize]);
                    result[i + j + halfSize] = result[i + j].subtract(t);
                    result[i + j] = result[i + j].add(t);
                    wk = wk.multiply(w);
                }
            }
        }

        return result;
    }

    // Find next power of 2
    private int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    // Helper class for complex numbers
    private static class Complex {
        double real;
        double imag;

        Complex(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }

        Complex add(Complex other) {
            return new Complex(real + other.real, imag + other.imag);
        }

        Complex subtract(Complex other) {
            return new Complex(real - other.real, imag - other.imag);
        }

        Complex multiply(Complex other) {
            return new Complex(
                    real * other.real - imag * other.imag,
                    real * other.imag + imag * other.real
            );
        }
    }
}