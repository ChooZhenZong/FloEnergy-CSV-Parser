package NEM12;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;

import NEM12.Processor.NEM12Processor;
import NEM12.SQL.SqlGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NEM12ProcessorTest {

    @Mock
    SqlGenerator sqlGenerator;

    @Test
    void shouldProcess300RecordCorrectly() throws Exception {

        String input =
                "200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610\n" +
                        "300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.461";

        StringWriter output = new StringWriter();

        NEM12Processor processor = new NEM12Processor(sqlGenerator);

        when(sqlGenerator.buildInsert(any(), any(), any()))
                .thenReturn("SQL");

        processor.process(
                new BufferedReader(new StringReader(input)),
                new BufferedWriter(output)
        );

        verify(sqlGenerator, times(1))
                .buildInsert(eq("NEM1201009"), anyString(), eq("0.461"));
    }
}