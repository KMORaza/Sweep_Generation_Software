package simulation.software.codebase;

public class FFTCalculator {
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
            return new Complex(real * other.real - imag * other.imag, real * other.imag + imag * other.real);
        }
    }

    public double[] calculateMagnitudeSpectrum(double[] waveform, int sampleRate) {
        if (waveform == null || waveform.length < 2 || sampleRate <= 0) {
            return new double[0];
        }

        // Find nearest power of 2
        int n = waveform.length;
        int m = (int) Math.ceil(Math.log(n) / Math.log(2));
        int fftSize = 1 << m;
        if (fftSize < n) fftSize <<= 1;

        // Pad with zeros if necessary
        Complex[] input = new Complex[fftSize];
        for (int i = 0; i < fftSize; i++) {
            input[i] = new Complex(i < n ? waveform[i] : 0, 0);
        }

        // Perform FFT
        Complex[] spectrum = fft(input);

        // Compute magnitude spectrum up to Nyquist frequency
        int outputSize = fftSize / 2 + 1;
        double[] magnitudes = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            magnitudes[i] = Math.sqrt(spectrum[i].real * spectrum[i].real + spectrum[i].imag * spectrum[i].imag);
        }

        return magnitudes;
    }

    public double[] computeFFTMagnitude(double[] waveform) {
        // Default sample rate for SpectrumAnalyzer (matches typical audio processing)
        int defaultSampleRate = 44100;
        return calculateMagnitudeSpectrum(waveform, defaultSampleRate);
    }

    private Complex[] fft(Complex[] x) {
        int n = x.length;
        if (n <= 1) return x;

        // Divide
        Complex[] even = new Complex[n / 2];
        Complex[] odd = new Complex[n / 2];
        for (int i = 0; i < n / 2; i++) {
            even[i] = x[2 * i];
            odd[i] = x[2 * i + 1];
        }

        // Conquer
        even = fft(even);
        odd = fft(odd);

        // Combine
        Complex[] result = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double angle = -2 * Math.PI * k / n;
            Complex twiddle = new Complex(Math.cos(angle), Math.sin(angle));
            result[k] = even[k].add(twiddle.multiply(odd[k]));
            result[k + n / 2] = even[k].subtract(twiddle.multiply(odd[k]));
        }
        return result;
    }
}