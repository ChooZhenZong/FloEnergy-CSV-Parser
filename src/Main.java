import Constants.CsvConstants;
import Constants.DBConstants;

void main(String[] args) {

    String inputFilePath = args.length > 0 ? args[0] : "src/Resource/test.csv";
    String outputFilePath = args.length > 1 ? args[1] : "output.sql";

    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter sqlDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
         BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

        String line;
        String nmi = null;
        int interval = 0;
        int count300 = 0;

        while ((line = br.readLine()) != null) {

            if (line.startsWith("200")) {
                if (nmi != null) {
                    System.out.println("Processed NMI " + nmi + " with " + count300 + " 300 records");
                }

                String[] parts = line.split(",");
                nmi = parts[CsvConstants.NMI_COLUMN].trim();
                interval = Integer.parseInt(parts[CsvConstants.INTERVAL_COLUMN].trim());
                count300 = 0;

                System.out.println("-----------------------------------------------------------");
                System.out.println("nmi: " + nmi);
                System.out.println("interval: " + interval);

            } else if (line.startsWith("300")) {

                if (nmi == null) {
                    System.err.println("ERROR: 300 record found before 200: " + line);
                    continue;
                }

                count300++;

                String[] parts = line.split(",", -1);
                LocalDate baseDate = LocalDate.parse(parts[CsvConstants.DATE_COLUMN], DATE_FORMAT);

                int intervalCounter = 0;

                // 300 record: values from index 2 onwards are interval consumption
                for (int i = CsvConstants.FIRST_CONSUMPTION_COLUMN; i < parts.length; i++) {
                    String value = parts[i].trim();

                    if (value.isEmpty() || value.equals("0")) {
                        intervalCounter++;
                        continue;
                    }

                    LocalDateTime timestamp = baseDate.atStartOfDay()
                            .plusMinutes((long) intervalCounter * interval);

                    String sqlInsert = SqlStatementsGenerator.buildInsert(
                            DBConstants.METER_READING,
                            nmi,
                            timestamp.format(sqlDateFormatter),
                            value
                    );

                    bw.write(sqlInsert);
                    bw.newLine();

                    intervalCounter++;
                }
            }
        }

        if (nmi != null) {
            System.out.println("Processed NMI " + nmi + " with " + count300 + " 300 records");
            System.out.println("-----------------------------------------------------------");
        }

        System.out.println("SQL file generated at: " + outputFilePath);

    } catch (FileNotFoundException e) {
        System.err.println("File not found: " + inputFilePath);
    } catch (IOException e) {
        System.err.println("Error reading file: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
    }
}