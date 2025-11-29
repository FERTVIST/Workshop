import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderReports {
    
    // 1) Отчет на оплату (для конкретного заказа) - каждая услуга отдельной строкой + группировка
    public static List<Map<String, Object>> getPaymentReport(int orderId, String orderBy, String where) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                s.service_name as "Услуга",
                COUNT(*) OVER (PARTITION BY o.id) as "Кол-во услуг",
                os.price as "Стоимость",
                e.last_name || ' ' || e.first_name || ' ' || COALESCE(e.patronymic, '') as "Исполнитель",
                c.last_name || ' ' || c.first_name || ' ' || COALESCE(c.patronymic, '') as "Клиент",
                car.brand as "Автомобиль",
                car.vin as "VIN автомобиля",
                SUM(os.price) OVER (PARTITION BY o.id) as "Общая сумма"
            FROM Orders o
            INNER JOIN Cars car ON o.car_id = car.id
            INNER JOIN Clients c ON car.client_id = c.id
            INNER JOIN Order_Services os ON o.id = os.order_id
            INNER JOIN Services s ON os.service_id = s.id
            INNER JOIN Employees e ON os.employee_id_assigned = e.id
            WHERE o.id = """ + orderId + """ 
            AND o.order_status = 'Готов'
        """);

        if (where != null && !where.trim().isEmpty()) {
            sql.append(" AND ").append(where);
        }
        
        // Группировка по услугам с дополнительными категориями
        sql.append(" GROUP BY s.service_name, os.price, e.last_name, e.first_name, e.patronymic,  ")
           .append("c.last_name, c.first_name, c.patronymic, car.brand, car.vin, o.id");
        
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        // Вывод итогового запроса в консоль
        String finalQuery = sql.toString();
        // System.out.println("=== Payment Report SQL Query ===");
        // System.out.println(finalQuery);
        // System.out.println("================================\n");
        
        return DataBase.executeQuery(finalQuery);
    }
    
    // 2) Отчет о работах (для конкретного заказа) - с группировкой по статусу
    public static List<Map<String, Object>> getWorkProgressReport(int orderId, String orderBy, String where) throws SQLException {
StringBuilder sql = new StringBuilder("""
            SELECT 
                statuses.service_status as "Статус",
                COUNT(os.id) as "Кол-во работ",
                COALESCE(SUM(os.price), 0) as "Сумма",
                CASE 
                    WHEN total.total_count > 0 
                    THEN ROUND(COUNT(os.id) * 100.0 / total.total_count, 2)
                    ELSE 0 
                END as "% от общего",
                STRING_AGG(COALESCE(s.service_name || ' (' || os.price || ' ₽) - ' || e.last_name, 'Нет работ'), '; ') as "Детали работ"
            FROM (VALUES ('Назначена'), ('Выполнена')) AS statuses(service_status)
            CROSS JOIN Orders o
            CROSS JOIN (
                SELECT COUNT(*) as total_count 
                FROM Order_Services 
                WHERE order_id = """ + orderId + """
            ) as total
            LEFT JOIN Order_Services os ON o.id = os.order_id AND os.service_status = statuses.service_status
            LEFT JOIN Services s ON os.service_id = s.id
            LEFT JOIN Employees e ON os.employee_id_assigned = e.id
            WHERE o.id = """ + orderId);

if (where != null && !where.trim().isEmpty()) {
    sql.append(" AND ").append(where);
}

sql.append(" GROUP BY statuses.service_status, total.total_count");

if (orderBy != null && !orderBy.trim().isEmpty()) {
    sql.append(" ORDER BY ").append(orderBy);
} else {
    sql.append(" ORDER BY statuses.service_status");
}
        
        // Вывод итогового запроса в консоль
        String finalQuery = sql.toString();
        // System.out.println("=== Work Progress Report SQL Query ===");
        // System.out.println(finalQuery);
        // System.out.println("======================================\n");
        
        return DataBase.executeQuery(finalQuery);
    }
    
    // 3) Отчет о сотрудниках (для конкретного заказа) - с группировкой по сотрудникам
    public static List<Map<String, Object>> getEmployeesByOrderReport(int orderId, String orderBy, String where) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                e.last_name || ' ' || e.first_name || ' ' || COALESCE(e.patronymic, '') as "Сотрудник",
                e.position as "Должность",
                e.phone_number as "Номер телефона",
                COUNT(CASE WHEN os.service_status = 'Выполнена' THEN 1 END) as "Назначено",
                COUNT(CASE WHEN os.service_status = 'Назначена' THEN 1 END) as "Выполнено",
                COALESCE(SUM(os.price), 0) as "Сумма",
                CASE 
                    WHEN COUNT(os.id) > 0 
                    THEN ROUND(COUNT(CASE WHEN os.service_status = 'Выполнена' THEN 1 END) * 100.0 / COUNT(os.id), 2)
                    ELSE 0 
                END as "Эффективность",
                STRING_AGG(s.service_name || ' (' || os.price || ' ₽)', ', ') as "Список услуг"
            FROM Orders o
            INNER JOIN Cars car ON o.car_id = car.id
            INNER JOIN Order_Services os ON o.id = os.order_id
            INNER JOIN Employees e ON os.employee_id_assigned = e.id
            INNER JOIN Services s ON os.service_id = s.id
            WHERE o.id = """ + orderId + """
        """);
        
        if (where != null && !where.trim().isEmpty()) {
            sql.append(" AND ").append(where);
        }
        
        sql.append(" GROUP BY e.id, e.last_name, e.first_name, e.patronymic, e.position, e.phone_number");
        
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        // Вывод итогового запроса в консоль
        String finalQuery = sql.toString();
        // System.out.println("=== Employees Report SQL Query ===");
        // System.out.println(finalQuery);
        // System.out.println("==================================\n");
        
        return DataBase.executeQuery(finalQuery);
    }
}