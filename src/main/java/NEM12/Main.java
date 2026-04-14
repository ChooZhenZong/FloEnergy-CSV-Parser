package NEM12;

import NEM12.Processor.NEM12Processor;
import NEM12.SQL.SqlGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Main {

    public static void main(String[] args) {

        String inputFilePath = args.length > 0 ? args[0] : "src/main/java/NEM12/Resource/test.csv";
        String outputFilePath = args.length > 1 ? args[1] : "output.sql";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            NEM12Processor processor = new NEM12Processor(new SqlGenerator());
            processor.process(br, bw);

            System.out.println("main.java.NEM12.SQL file generated at: " + outputFilePath);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}