// import java.sql.*;

// public class Test {
    
//     public static void main(String[] args) {
//         Connection connection = null;
//         try {
//             // Для PostgreSQL - замени на свои значения
//             String url = "jdbc:postgresql://192.168.56.102:5432/student"; // URL
//             String username = "postgres"; // Обычно "postgres"
//             String password = "#define"; // Твой пароль
            
//             // Драйвер для PostgreSQL
//             Class.forName("org.postgresql.Driver");
            
//             connection = DriverManager.getConnection(url, username, password);
//             System.out.println("Успешное подключение к PostgreSQL!");
            
//             // Тестовый запрос
//             String tableName = "Clients";
//             String sql = "SELECT * FROM " + tableName;

//             Statement statement = connection.createStatement();
//             ResultSet resultSet = statement.executeQuery(sql);

//             // Выводим результаты
//             ResultSetMetaData metaData = resultSet.getMetaData();
//             int columnCount = metaData.getColumnCount();

//             System.out.println("\nРезультаты запроса:");

//             // Проверяем есть ли данные
//             if (!resultSet.isBeforeFirst()) {
//                 System.out.println("В таблице нет данных!");
//             } else {
//                 while (resultSet.next()) {
//                     for (int i = 1; i <= columnCount; i++) {
//                         System.out.print(metaData.getColumnName(i) + ": " + resultSet.getString(i) + " | ");
//                     }
//                     System.out.println();
//                 }
//             }
//             System.out.println();
                        
            
//             resultSet.close();
//             statement.close();
            
//         } catch (ClassNotFoundException e) {
//             System.out.println("Драйвер не найден: " + e.getMessage());
//         } catch (SQLException e) {
//             System.out.println("Ошибка SQL: " + e.getMessage());
//         } finally {
//             // Закрываем соединение
//             if (connection != null) {
//                 try {
//                     connection.close();
//                     System.out.println("\nСоединение закрыто.");
//                 } catch (SQLException e) {
//                     System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
//                 }
//             }
//         }
//     }
// }