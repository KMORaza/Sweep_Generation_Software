package simulation.software.codebase;

import java.util.ArrayList;
import java.util.List;

public class TableSweep {
    private List<double[]> table = new ArrayList<>();

    public void parseTable(String input) {
        table.clear();
        String[] lines = input.split("\n");
        for (String line : lines) {
            try {
                String[] parts = line.trim().split(",");
                if (parts.length == 2) {
                    double freq = Double.parseDouble(parts[0].trim());
                    double amp = Double.parseDouble(parts[1].trim());
                    if (freq >= 10 && freq <= 10000 && amp >= 0.1 && amp <= 5.0) {
                        table.add(new double[]{freq, amp});
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid lines
            }
        }
        if (table.isEmpty()) {
            table.add(new double[]{100, 1.0}); // Default value
        }
    }

    public double getCurrentFrequency(double currentTime, double sweepTime) {
        if (table.isEmpty() || currentTime >= sweepTime) {
            return 100; // Default frequency
        }
        int index = (int) ((currentTime / sweepTime) * table.size()) % table.size();
        return table.get(index)[0];
    }

    public double getCurrentAmplitude(double currentTime, double sweepTime) {
        if (table.isEmpty() || currentTime >= sweepTime) {
            return 1.0; // Default amplitude
        }
        int index = (int) ((currentTime / sweepTime) * table.size()) % table.size();
        return table.get(index)[1];
    }
}