package NEM12.SQL;

import NEM12.Constants.DBConstants;

public class SqlGenerator {

    public String buildInsert(String nmi, String timestamp, String value) {
        return String.format(
                "INSERT INTO %s (nmi, timestamp, consumption) VALUES ('%s', '%s', %s);",
                DBConstants.METER_READING,
                nmi,
                timestamp,
                value
        );
    }
}
