package simulation.software.codebase;

public class FFTProcessor {
    // Simple Cooley-Tukey FFT implementation
    public double[] computeFFT(double[] input, int sampleRate) {
        int N = input.length;
        // Ensure power of 2 for FFT
        int M = (int) (Math.log(N) / Math.log(2));
        int size = 1 << M;
        if (size > N) size = N;

        // Prepare input (real part only, zero-pad if needed)
        double[] real = new double[size];
        double[] imag = new double[size];
        for (int i = 0; i < size && i < N; i++) {
            real[i] = input[i];
            imag[i] = 0.0;
        }

        // Perform FFT
        fft(real, imag, size);

        // Compute magnitude spectrum
        double[] magnitude = new double[size / 2];
        double maxFreq = sampleRate / 2.0; // Nyquist frequency
        for (int i = 0; i < size / 2; i++) {
            magnitude[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]) / size;
            // Normalize to approximate input amplitude scale
            magnitude[i] *= 2.0;
        }

        return magnitude;
    }

    private void fft(double[] real, double[] imag, int N) {
        if (N <= 1) return;

        // Divide
        double[] evenReal = new double[N / 2];
        double[] evenImag = new double[N / 2];
        double[] oddReal = new double[N / 2];
        double[] oddImag = new double[N / 2];
        for (int i = 0; i < N / 2; i++) {
            evenReal[i] = real[2 * i];
            evenImag[i] = imag[2 * i];
            oddReal[i] = real[2 * i + 1];
            oddImag[i] = imag[2 * i + 1];
        }

        // Conquer
        fft(evenReal, evenImag, N / 2);
        fft(oddReal, oddImag, N / 2);

        // Combine
        for (int k = 0; k < N / 2; k++) {
            double theta = -2 * Math.PI * k / N;
            double tReal = Math.cos(theta) * oddReal[k] - Math.sin(theta) * oddImag[k];
            double tImag = Math.sin(theta) * oddReal[k] + Math.cos(theta) * oddImag[k];
            real[k] = evenReal[k] + tReal;
            imag[k] = evenImag[k] + tImag;
            real[k + N / 2] = evenReal[k] - tReal;
            imag[k + N / 2] = evenImag[k] - tImag;
        }
    }
}