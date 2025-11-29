

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;




public class DataBase {
    
    static private Connection _connection = null;

    static private String _tables[] = null;

    static public void begin() throws SQLException, ClassNotFoundException {
            String url = "jdbc:postgresql://192.168.56.102:5432/student";
            String username = "postgres";
            String password = "#define";

            Class.forName("org.postgresql.Driver");
            
            _connection = DriverManager.getConnection(url, username, password);

            _tables = _getTableNames();
    }

    static public void end() throws SQLException {
        if (_connection != null) _connection.close();
    }



    static private String[] _getTableNames() throws SQLException {
        List<String> tableList = new ArrayList<>();
        
        DatabaseMetaData metaData = _connection.getMetaData();
        
        ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
        
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            tableList.add(tableName);
        }
        tables.close();
        
        return tableList.toArray(new String[0]);
    }

    public static String[] getTables() {
        return Arrays.copyOf(_tables, _tables.length);
    }




    private static void _checkExistTable(String tableName) throws SQLException {
        if (!Arrays.asList(_tables).contains(tableName)) {
            throw new SQLException("Table '" + tableName + "' does not exist");
        }
    }



    public static List<Map<String, Object>> getAllTableData(String tableName) throws SQLException {
        _checkExistTable(tableName);
        
        String query = "SELECT * FROM " + tableName;
        try (Statement statement = _connection.createStatement();
            ResultSet rs = statement.executeQuery(query)) {
            return _resultSetToList(rs);
        }
    }

    public static String[] getTableColumns(String tableName) throws SQLException {
        _checkExistTable(tableName);
        
        List<String> columnList = new ArrayList<>();
        
        DatabaseMetaData metaData = _connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                columnList.add(columnName);
            }
        }
        
        return columnList.toArray(new String[0]);
    }

    public static String[] getTableColumnsWithFK(String tableName) throws SQLException {
        _checkExistTable(tableName);
        
        List<String> columnList = new ArrayList<>();
        
        DatabaseMetaData metaData = _connection.getMetaData();
        Set<String> fkColumns = new HashSet<>();
        
        try (ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName)) {
            while (foreignKeys.next()) {
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                fkColumns.add(fkColumnName);
            }
        }
        
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (fkColumns.contains(columnName)) {
                    columnList.add(columnName + "*");
                } else {
                    columnList.add(columnName);
                }
            }
        }
        
        return columnList.toArray(new String[0]);
    }

    public static List<Map<String, Object>> getTableData(String tableName, String fields, String orderBy, String where) throws SQLException {
        _checkExistTable(tableName);
        
        StringBuilder query = new StringBuilder("SELECT ");
        
        if (fields == null || fields.trim().isEmpty()) {
            query.append("*");
        } else {
            query.append(fields);
        }
        
        query.append(" FROM ").append(tableName);
        
        if (where != null && !where.trim().isEmpty()) {
            query.append(" WHERE ").append(where);
        }
        
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            query.append(" ORDER BY ").append(orderBy);
        }
        
        try (Statement statement = _connection.createStatement();
            ResultSet rs = statement.executeQuery(query.toString())) {
            return _resultSetToList(rs);
        }
    }

    private static List<Map<String, Object>> _resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }
        
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            
            for (int i = 0; i < columnCount; i++) {
                String columnName = columnNames[i];
                Object value = rs.getObject(i + 1);
                row.put(columnName, value);
            }
            result.add(row);
        }
        
        return result;
    }


    public static String[] getTableColumnTypes(String tableName) throws SQLException {
        _checkExistTable(tableName);
        
        List<String> columnTypes = new ArrayList<>();
        DatabaseMetaData metaData = _connection.getMetaData();
        
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {
                String columnType = columns.getString("TYPE_NAME");
                columnTypes.add(columnType);
            }
        }
        // System.out.println("Типы колонок: " + Arrays.toString(columnTypes.toArray(new String[0])));
        return columnTypes.toArray(new String[0]);
    }

    public static String getForeignKeyTable(String tableName, String columnName) throws SQLException {
        _checkExistTable(tableName);
        
        DatabaseMetaData metaData = _connection.getMetaData();
        
        try (ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName)) {
            while (foreignKeys.next()) {
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                
                if (fkColumnName.equals(columnName)) {
                    return pkTableName;
                }
            }
        }
        
        throw new SQLException("Foreign key not found for column '" + columnName + "' in table '" + tableName + "'");
    }




    public static boolean deleteById(String tableName, int id) throws SQLException {
        _checkExistTable(tableName);
        
        String query = "DELETE FROM " + tableName + " WHERE id = ?";
        
        try (PreparedStatement statement = _connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean update(String tableName, String[] fields, String[] values, int id) throws SQLException {
        _checkExistTable(tableName);
        
        if (fields == null || values == null || fields.length != values.length) {
            throw new IllegalArgumentException("Fields and values arrays must not be null and have the same length");
        }
        
        if (fields.length == 0) {
            throw new IllegalArgumentException("Fields array cannot be empty");
        }
        
        StringBuilder query = new StringBuilder("UPDATE " + tableName + " SET ");
        
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                query.append(", ");
            }
            query.append(fields[i]).append(" = ?");
        }
        query.append(" WHERE id = ?");
        
        try (PreparedStatement statement = _connection.prepareStatement(query.toString())) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].matches("-?\\d+")) {
                    statement.setInt(i + 1, Integer.parseInt(values[i]));
                } else if (values[i].matches("-?\\d+\\.\\d+")) {
                    statement.setDouble(i + 1, Double.parseDouble(values[i]));
                } else if (isTimeFormat(values[i])) {
                    // Обработка времени
                    statement.setTime(i + 1, java.sql.Time.valueOf(values[i]));
                } else if (isDateFormat(values[i])) {
                    // Обработка даты
                    statement.setDate(i + 1, java.sql.Date.valueOf(values[i]));
                } else if (isTimestampFormat(values[i])) {
                    // Обработка даты-времени
                    statement.setTimestamp(i + 1, java.sql.Timestamp.valueOf(values[i]));
                } else {
                    statement.setString(i + 1, values[i]);
                }
            }
            
            statement.setInt(values.length + 1, id);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Проверка формата времени (HH:mm:ss)
    private static boolean isTimeFormat(String value) {
        return value.matches("^([01]?\\d|2[0-3]):([0-5]?\\d):([0-5]?\\d)$");
    }

    // Проверка формата даты (YYYY-MM-DD)
    private static boolean isDateFormat(String value) {
        return value.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    // Проверка формата даты-времени (YYYY-MM-DD HH:mm:ss)
    private static boolean isTimestampFormat(String value) {
        return value.matches("^\\d{4}-\\d{2}-\\d{2} ([01]?\\d|2[0-3]):([0-5]?\\d):([0-5]?\\d)$");
    }

    public static boolean insert(String tableName, String[] fields, String[] values) throws SQLException {
        _checkExistTable(tableName);
        
        if (fields == null || values == null || fields.length != values.length) {
            throw new IllegalArgumentException("Fields and values arrays must not be null and have the same length");
        }
        
        if (fields.length == 0) {
            throw new IllegalArgumentException("Fields array cannot be empty");
        }
        
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder("VALUES (");
        
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                query.append(", ");
                placeholders.append(", ");
            }
            query.append(fields[i]);
            placeholders.append("?");
        }
        
        query.append(") ").append(placeholders).append(")");
        
        try (PreparedStatement statement = _connection.prepareStatement(query.toString())) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].matches("-?\\d+")) {
                    statement.setInt(i + 1, Integer.parseInt(values[i]));
                } else if (values[i].matches("-?\\d+\\.\\d+")) {
                    statement.setDouble(i + 1, Double.parseDouble(values[i]));
                } else if (isTimeFormat(values[i])) {
                    // Обработка времени
                    statement.setTime(i + 1, java.sql.Time.valueOf(values[i]));
                } else if (isDateFormat(values[i])) {
                    // Обработка даты
                    statement.setDate(i + 1, java.sql.Date.valueOf(values[i]));
                } else if (isTimestampFormat(values[i])) {
                    // Обработка даты-времени
                    statement.setTimestamp(i + 1, java.sql.Timestamp.valueOf(values[i]));
                } else {
                    statement.setString(i + 1, values[i]);
                }
            }
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }



    public static List<Map<String, Object>> getReferencedTableData(String tableName, String fkColumnName, String fkValue) throws SQLException {
        _checkExistTable(tableName);
        
        // Получаем таблицу, на которую ссылается foreign key
        String referencedTableName = getForeignKeyTable(tableName, fkColumnName);
        
        // Запрос к связанной таблице по ID (serial всегда integer)
        String query = "SELECT * FROM " + referencedTableName + " WHERE id = ?";
        
        try (PreparedStatement statement = _connection.prepareStatement(query)) {
            // Преобразуем строку в integer (serial всегда целочисленный)
            statement.setInt(1, Integer.parseInt(fkValue));
            
            try (ResultSet rs = statement.executeQuery()) {
                return _resultSetToList(rs);
            }
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid foreign key value: '" + fkValue + "'. Expected integer for serial field.");
        }
    }


    public static boolean[] getTableFKs(String tableName) throws SQLException {
        _checkExistTable(tableName);
        
        // Сначала получаем все колонки таблицы
        String[] allColumns = getTableColumns(tableName);
        
        // Создаем массив boolean, где true означает, что колонка является FK
        boolean[] isForeignKey = new boolean[allColumns.length];
        
        // Получаем множество колонок, которые являются внешними ключами
        Set<String> fkColumns = new HashSet<>();
        DatabaseMetaData metaData = _connection.getMetaData();
        
        try (ResultSet importedKeys = metaData.getImportedKeys(null, null, tableName)) {
            while (importedKeys.next()) {
                String fkColumnName = importedKeys.getString("FKCOLUMN_NAME");
                fkColumns.add(fkColumnName);
            }
        }
        
        // Заполняем массив boolean
        for (int i = 0; i < allColumns.length; i++) {
            isForeignKey[i] = fkColumns.contains(allColumns[i]);
        }
        
        return isForeignKey;
    }

    public static List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        try (Statement statement = _connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return _resultSetToList(rs);
        }
    }

    public static boolean executeUpdate(String sql) throws SQLException {
        try (Statement statement = _connection.createStatement()) {
            return statement.execute(sql);
        }
    }
}
