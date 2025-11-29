import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Order {


    private int _type;
    private String _typeString;
    public Order(String Type) {
        _typeString = Type;
        switch(Type) {
            case "Отчет на оплату":
                _type = 0;
                break;
            case "Отчет о работах":
                _type = 1;
                break;
            case "Отчет о сотрудниках":
                _type = 2;
                break;
        }
    }
    public String getType() {
        return _typeString;
    }

    private int _id = 0;

    public void setId(int id) {
        _id = id;
    }

    private String[][] _sort = new String[][]{
        {"Услуга", "Стоимость", "Исполнитель"},
        {"Статус", "Кол-во работ", "Сумма", "% от общего"},
        {"Сотрудник", "Должность", "Назначено", "Выполнено", "Сумма", "Эффективность"}
    };
    private String[][] _sort_desciption = new String[][]{
        {"Услуга", "Стоимость", "Исполнитель"},
        {"Статус", "Кол-во работ", "Сумма", "% от общего"},
        {"Сотрудник", "Должность", "Назначено", "Выполнено", "Сумма", "Эффективность"}
    };

    private String[] _sort_execute = null;


    public String[] getSort() {
        return _sort[_type];
    } 
    public void setSort(String[] sort) {
        _sort_execute = sort;
    } 



    private String[][] _filter = new String[][]{
        null,
        {"Статус"},
        {"Фамилия сотрудника", "Должность"}
    };;
    private String[][] _filter_desciption = new String[][]{
        null,
        {"os.service_status"},
        {"e.last_name", "e.position"}
    };
    private String[][] _filter_type = new String[][]{
        null,
        {"text"},
        {"text", "text"}
    };


    private String[] _filter_execute_name = null;
    private String[] _filter_execute_val = null;


    
    public String[] getFilter() {
        return _filter[_type];
    } 
    public void setFilter(String[] filter_name, String[] filter_val) {
        _filter_execute_name = filter_name;
        _filter_execute_val = filter_val;
    }



    public List<Map<String, Object>> get() throws SQLException {
        StringBuilder orderBuilder = new StringBuilder();
        StringBuilder whereBuilder = new StringBuilder();
        if (_sort_execute != null)
        for (int i = 0; i < _sort_execute.length; ++i) {
            if (_sort_execute[i].equals("по возрастанию")) {
                orderBuilder.append(_sort_desciption[_type][i]).append(" ASC, ");
            } else if (_sort_execute[i].equals("по убыванию")) {
                orderBuilder.append(_sort_desciption[_type][i]).append(" DESC, ");
            }
        }

        String order = "";
        if (orderBuilder.length() > 0) {
            order = orderBuilder.substring(0, orderBuilder.length() - 2);
        }

        if (_filter_execute_name != null)
        for (int i = 0; i < _filter_execute_name.length; ++i) {
            switch(_filter_type[_type][i]) {
                case "int":
                    if ("равно".equals(_filter_execute_name[i])) {
                        whereBuilder.append(_filter_desciption[_type][i])
                                .append(" = ")
                                .append(_filter_execute_val[i])
                                .append(" AND ");
                    } else if ("больше".equals(_filter_execute_name[i])) {
                        whereBuilder.append(_filter_desciption[_type][i])
                                .append(" > ")
                                .append(_filter_execute_val[i])
                                .append(" AND ");
                    } else if ("меньше".equals(_filter_execute_name[i])) {
                        whereBuilder.append(_filter_desciption[_type][i])
                                .append(" < ")
                                .append(_filter_execute_val[i])
                                .append(" AND ");
                    }
                    break;
                case "text":
                    if ("содержит".equals(_filter_execute_name[i])) {
                        whereBuilder.append(_filter_desciption[_type][i])
                                .append(" LIKE '%")
                                .append(_filter_execute_val[i])
                                .append("%' AND ");
                    }
                    break;
            }
        }

        String where = "";
        if (whereBuilder.length() > 0) {
            where = whereBuilder.substring(0, whereBuilder.length() - 5);
        }

        switch(_type) {
            case 0:
                return OrderReports.getPaymentReport(_id, order, where);
            case 1:
                return OrderReports.getWorkProgressReport(_id, order, where);
            case 2:
                return OrderReports.getEmployeesByOrderReport(_id, order, where);
        }
        return null;
    }

}
