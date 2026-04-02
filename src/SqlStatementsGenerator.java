public class SqlStatementsGenerator {

    private static String INSERT = "INSERT INTO '%s' (nmi, timestamp, consumption) VALUES('%s','%s','%s')";

    public static String buildInsert(String tableName, String nmi, String timeStamp, String consumption) {
        return String.format(INSERT, tableName, nmi, timeStamp, consumption);
    }
}
