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
            if (other == null) return new Complex(real, imag);
            return new Complex(real + other.real, imag + other.imag);
        }

        Complex subtract(Complex other) {
            if (other == null) return new Complex(real, imag);
            return new Complex(real - other.real, imag - other.imag);
        }

        Complex multiply(Complex other) {
            if (other == null) return new Complex(0, 0);
            return new Complex(real * other.real - imag * other.imag, real * other.imag + imag * other.real);
        }
    }

    public double[] computeFFTMagnitude(double[] input) {
        // Pad or truncate input to 1024 (2^10) for power-of-2 FFT
        int n = 1024;
        Complex[] x = new Complex[n];
        for (int i = 0; i < n; i++) {
            double value = (i < input.length) ? input[i] : 0.0;
            x[i] = new Complex(value, 0);
        }

        Complex[] result = fft(x);
        double[] magnitude = new double[n / 2];
        for (int i = 0; i < n / 2; i++) {
            magnitude[i] = Math.sqrt(result[i].real * result[i].real + result[i].imag * result[i].imag);
        }
        return magnitude;
    }

    private Complex[] fft(Complex[] x) {
        int n = x.length;
        if (n <= 1) return x;

        // Ensure arrays are initialized
        Complex[] even = new Complex[n / 2];
        Complex[] odd = new Complex[n / 2];
        for (int i = 0; i < n / 2; i++) {
            even[i] = x[2 * i] != null ? x[2 * i] : new Complex(0, 0);
            odd[i] = x[2 * i + 1] != null ? x[2 * i + 1] : new Complex(0, 0);
        }

        even = fft(even);
        odd = fft(odd);

        Complex[] result = new Complex[n];
        for (int i = 0; i < n; i++) {
            result[i] = new Complex(0, 0); // Initialize to avoid null
        }

        for (int k = 0; k < n / 2; k++) {
            double angle = -2 * Math.PI * k / n;
            Complex t = new Complex(Math.cos(angle), Math.sin(angle));
            result[k] = even[k].add(t.multiply(odd[k]));
            result[k + n / 2] = even[k].subtract(t.multiply(odd[k]));
        }
        return result;
    }
}