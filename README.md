## Sweep Generation Software

1. This software is written for generating and analyzing frequency sweeps, simulating real-world signal processing with a focus on audio applications. It employs algorithms like Cooley-Tukey FFT for spectral analysis, linear/logarithmic interpolation for sweeps and DAC resampling, and mathematical models for modulation and noise.

2. **Waveform Generation Core Logic**: The `WaveformGenerator` class is responsible for generating waveforms (sine, square, triangle) based on user-specified parameters such as frequency, amplitude, and time array. It uses mathematical functions (`Math.sin`, `Math.signum`, and a triangle wave formula) to compute waveform values, applies modulation via `ModulationGenerator`, and adds noise via `NoiseGenerator`, with amplitude clamping to [-5, 5] to prevent extreme values, ensuring stable waveform output for analysis.

3. **Sweep Type Handling in SweepGeneratorController**: The `SweepGeneratorController` coordinates waveform generation by supporting multiple sweep types (Linear, Logarithmic, Bidirectional, Stepped Linear, Stepped Log, Time, Table) through user selections in a ComboBox. It dynamically updates the waveform based on the selected sweep type, adjusting frequency or amplitude using classes like `FrequencySweep`, `SteppedSweep`, `TimeSweep`, or `TableSweep`, and passes the generated waveform to analyzers for real-time visualization.

4. **Linear Frequency Sweep Algorithm**: The `FrequencySweep` class implements a linear sweep where the frequency changes linearly from a start frequency to an end frequency over a specified sweep time, calculated as `f(t) = f_start + (f_end - f_start) * (t / T)`, providing a straightforward model for smooth frequency transitions commonly used in audio testing.

5. **Logarithmic Frequency Sweep Algorithm**: The `FrequencySweep` and `GlideSweep` classes support logarithmic sweeps, where frequency changes according to `f(t) = f_start * (f_end / f_start)^(t / T)`, ensuring a perceptually uniform frequency progression suitable for applications like audio frequency response analysis where human perception is logarithmic.

6. **Bidirectional Frequency Sweep Logic**: The `FrequencySweep` class implements a bidirectional sweep that progresses linearly from start to end frequency in the first half of the sweep time and reverses back in the second half, calculated as forward (`f_start + (f_end - f_start) * (t / (T/2))`) for `t < T/2` and reverse (`f_end - (f_end - f_start) * ((t - T/2) / (T/2))`) for `t >= T/2`, useful for testing system responses in both directions.

7. **Stepped Sweep Mechanism**: The `SteppedSweep` class divides the sweep into 10 discrete steps, each lasting 0.5 seconds, calculating frequency either linearly (`f_start + (f_end - f_start) * step / (STEPS - 1)`) or logarithmically (`10^(log(f_start) + (log(f_end) - log(f_start)) * step / (STEPS - 1))`), providing a discrete frequency progression for applications requiring stable frequency intervals.

8. **Time-Based Amplitude Sweep**: The `TimeSweep` class implements a linear amplitude sweep, reducing amplitude from a base value to zero over the sweep time using `A(t) = A_base * (1 - t / T)`, enabling simulations of fading signals or time-varying amplitude effects.

9. **Table-Based Sweep Logic**: The `TableSweep` class parses a user-provided text input (frequency, amplitude pairs) to create a table of discrete frequency and amplitude steps, selecting values based on the current time relative to the sweep duration (`index = (t / T) * table.size`), offering flexible, user-defined sweep patterns for custom signal generation.

10. **Modulation Application**: The `ModulationGenerator` class applies Amplitude Modulation (AM: `(1 + m * sin(2π f_m t)) * carrier`), Frequency Modulation (FM: `A_c * sin(2π f_c t + β * sin(2π f_m t))`), or Phase Modulation (PM: similar to FM with phase modulation) to the carrier waveform, using user-specified modulation frequency and index, enabling simulation of modulated signals for communication or audio testing.

11. **Noise Generation Algorithms**: The `NoiseGenerator` class generates white noise (uniform random values in [-amplitude, amplitude]), pink noise (using a simplified Voss-McCartney algorithm with three low-pass filters to approximate 1/f noise), and Brownian noise (integrated white noise with clamping to [-amplitude, amplitude]), adding realistic noise models to waveforms for testing signal robustness.

12. **Spectrum Analysis and FFT**: The `SpectrumAnalyzer` uses the `FFTCalculator` to compute the magnitude spectrum of a waveform via Fast Fourier Transform, displaying it as a line chart with frequency (Hz) on the x-axis and magnitude (linear or dB) on the y-axis. It identifies the peak frequency by finding the maximum magnitude, updating in real-time with `AnimationTimer`, and supports logarithmic scaling (`20 * log10(magnitude)`) for better visualization of dynamic ranges.

13. **FFT Algorithm Implementation**: The `FFTCalculator` implements the Cooley-Tukey FFT algorithm, using bit-reversal permutation and butterfly operations to efficiently compute the discrete Fourier transform of a padded (to power of 2) input waveform, returning magnitude (`sqrt(real^2 + imag^2)`) and phase (`atan2(imag, real)`) spectra for positive frequencies up to the Nyquist limit.

14. **Digital-to-Analog Conversion Simulation**: The `DigitalToAnalogConversion` class simulates DAC effects by applying quantization (based on bit depth, with levels `2^bitDepth` and step size `2 * maxAmp / levels`), resampling via linear interpolation, nonlinearity (`y = x + k * x^3`), and thermal noise (random values in [-amplitude, amplitude]). It calculates SNR (`10 * log10(signalPower / noisePower)`) and visualizes the output waveform, simulating real-world DAC imperfections.

15. **Frequency Measurement Logic**: The `FrequencyCounter` class measures waveform frequency using either zero-crossing (counting positive-going crossings over a window, frequency = `crossings / (2 * windowSize)`) or FFT-based methods (identifying the peak magnitude in the spectrum, frequency = `maxIndex * frequencyResolution`), displaying results with a line chart and zero-crossing markers for intuitive analysis.

16. **Phase Analysis Model**: The `PhaseAnalyzer` computes phase differences between two channels (left and simulated right with a 45° shift) using FFT phase spectra from `FFTCalculator`. It identifies the fundamental frequency via the maximum magnitude, calculates phase difference (`rightPhase - leftPhase` in degrees, normalized to [-180, 180]), and displays a constant phase difference over a 20ms window, useful for stereo signal analysis.

17. **Total Harmonic Distortion (THD) Calculation**: The `TotalHarmonicDistortion` class computes THD by analyzing the FFT magnitude spectrum, identifying the fundamental frequency (maximum magnitude), and summing the power of user-specified harmonics (up to 10). THD is calculated as `sqrt(harmonicPower / fundamentalPower) * 100`, displayed with a spectrum chart highlighting fundamental and harmonic frequencies, aiding in signal quality assessment.

18. **Real-Time Visualization and Updates**: All analyzer classes (`SpectrumAnalyzer`, `DigitalToAnalogConversion`, `FrequencyCounter`, `PhaseAnalyzer`, `TotalHarmonicDistortion`) use JavaFX `LineChart` for visualization and `AnimationTimer` for real-time updates, ensuring continuous waveform processing and display, with dynamic axis adjustments based on sweep type or duration for accurate representation.

19. **User Interface and Control Logic**: The `SweepGeneratorController` manages the main UI with sliders (frequency, amplitude, sweep time, noise amplitude, modulation index/frequency), ComboBoxes (sweep type, noise type, modulation type), buttons (waveform selection, start/stop, analyzer toggles), and a TextArea for table sweeps. It binds slider values to labels for real-time feedback and updates the waveform chart based on user inputs.

20. **Data Export Functionality**: Each analyzer and the main controller support exporting data to CSV files using `FileChooser`, saving time-amplitude pairs (`WaveformGenerator`, `SpectrumAnalyzer`, `DigitalToAnalogConversion`), frequency/period (`FrequencyCounter`), phase/fundamental frequency (`PhaseAnalyzer`), or THD/harmonic amplitudes (`TotalHarmonicDistortion`), enabling data analysis outside the application.

21. **Dynamic Axis Management**: The `SweepGeneratorController` adjusts the waveform chart’s x-axis based on sweep type (time in seconds for Time sweep, milliseconds otherwise), while analyzers like `SpectrumAnalyzer` and `TotalHarmonicDistortion` set frequency axes up to the Nyquist limit or 10 kHz, ensuring appropriate scaling for different signal types.


![](https://github.com/KMORaza/Sweep_Generation_Software/blob/main/codebase/src/main/screenshot%20(2).png)

**`NOTE: This software is quite useful for sweep generation and simulation but some of its features have imperfections and might cause errors. If I'll fix these defects in future, I'll either update this repository or will create a new one.`**
