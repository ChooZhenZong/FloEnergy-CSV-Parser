package NEM12.Processor;


import NEM12.Constants.CsvConstants;
import NEM12.SQL.SqlGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NEM12Processor {

    private final SqlGenerator sqlGenerator;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter SQL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NEM12Processor(SqlGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public void process(BufferedReader br, BufferedWriter bw) throws IOException {

        String line;
        String nmi = null;
        int interval = 0;

        StringBuilder batch = new StringBuilder();
        boolean hasBatch = false;

        while ((line = br.readLine()) != null) {

            if (line.startsWith("200")) {
                // flush previous batch before switching NMI
                flushBatch(batch, bw);
                hasBatch = false;

                String[] parts = line.split(",");
                nmi = parts[CsvConstants.NMI_COLUMN].trim();
                interval = Integer.parseInt(parts[CsvConstants.INTERVAL_COLUMN].trim());

            } else if (line.startsWith("300")) {

                if (nmi == null) {
                    System.err.println("ERROR: 300 record before 200");
                    continue;
                }

                hasBatch = process300Record(line, nmi, interval, batch, hasBatch);
            }
        }

        flushBatch(batch, bw);
    }

    private boolean process300Record(
            String line,
            String nmi,
            int interval,
            StringBuilder batch,
            boolean hasBatch
    ) {

        String[] parts = line.split(",", -1);
        LocalDate baseDate = LocalDate.parse(parts[CsvConstants.DATE_COLUMN], DATE_FORMAT);

        int intervalCounter = 0;

        for (int i = 2; i < parts.length; i++) {

            String value = parts[i].trim();

            if (value.isEmpty() || value.equals("0")) {
                intervalCounter++;
                continue;
            }

            LocalDateTime timestamp = baseDate.atStartOfDay()
                    .plusMinutes((long) intervalCounter * interval);

            if (!hasBatch) {
                batch.append("INSERT INTO meter_readings (nmi, timestamp, value) VALUES ");
                hasBatch = true;
            } else {
                batch.append(", ");
            }

            batch.append("(")
                    .append("'").append(nmi).append("', ")
                    .append("'").append(timestamp.format(SQL_DATE_FORMAT)).append("', ")
                    .append(value)
                    .append(")");

            intervalCounter++;
        }

        return hasBatch;
    }

    private void flushBatch(StringBuilder batch, BufferedWriter bw) throws IOException {
        if (batch.length() == 0) return;

        bw.write(batch.toString());
        bw.write(";");
        bw.newLine();

        batch.setLength(0);
    }
}