
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;




class DataList {
    String table;
    Property[] properties;
    String[] entered_data;
    String FK_field;
    int id;
    String type;
}


public class Data {
    private List<DataList> _dataList;

    private String[] _fields;
    private boolean[] _FKs;
    private String[] _columnTypes;
    
    
    private int _ind;




    Data(String table) throws SQLException {
        _dataList = new ArrayList<>();
        _ind = 0;


        _add(table);
    }



    public String[] getFields() {
        return _fields;
    }

    public boolean[] getFKs() {
        return _FKs;
    }

    // public String[] getColumnTypes() {
    //     return _columnTypes;
    // }


    public String getFKField() {
        return _dataList.get(_ind).FK_field;
    }
    public String getTableName() {
        return _dataList.get(_ind).table;
    }
    public void setEnterData(String[] data) {
        _dataList.get(_ind).entered_data = data;
    }
    public String[] getEnterData() {
        return _dataList.get(_ind).entered_data;
    }
    public void setProperties(Property[] data) {
        _dataList.get(_ind).properties = data;
    }
    public Property[] getProperties() {
        return _dataList.get(_ind).properties;
    }

    public void setDelete(int id) {
        _dataList.get(_ind).id = id;
        _dataList.get(_ind).type = "Удалить";
    }
    public void setUpdate(int id) {
        _dataList.get(_ind).id = id;
        _dataList.get(_ind).type = "Изменить";
    }
    public void setCreate() {
        _dataList.get(_ind).id = -1;
        _dataList.get(_ind).type = "Создать";
    }
    public String getType() {
        return _dataList.get(_ind).type;
    }
    public int getId() {
        return _dataList.get(_ind).id;
    }

    public void execute() throws SQLException {
        String str = _dataList.get(_ind).type;
        String table = _dataList.get(_ind).table;
        int id = _dataList.get(_ind).id;
        String[] fields = getFields();
        String[] filteredFields = new String[fields.length - 1];
        String[] entered_data = _dataList.get(_ind).entered_data;
        String[] filteredEnterData = new String[fields.length - 1];

        int r = 0;
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].equals("id")) {
                r = 1;
            } else {
                filteredFields[i - r] = fields[i];
                filteredEnterData[i - r] = entered_data[i];
            }
        } 

        switch(str) {
            case "Удалить":
                DataBase.deleteById(table, id);
                break;
            case "Изменить":
                DataBase.update(table, filteredFields, filteredEnterData, id);
                break;
            case "Создать":
                DataBase.insert(table, filteredFields, filteredEnterData);
                break;
        };
        _dataList.get(_ind).type = "";
        _dataList.get(_ind).id = -1;
    }


    public void _add(String table) throws SQLException {
        _resumeData(table);
        
        DataList data_list = new DataList();
        
        int size = _fields.length;
        data_list.table = table;

        data_list.properties = new Property[size];
        data_list.entered_data = new String[size];
        for (int i = 0; i < size; i++) {
            data_list.properties[i] = new Property();
            data_list.entered_data[i] = "";
        }
        data_list.type = "";
        data_list.id = -1;
        data_list.FK_field = "";
        _dataList.add(data_list);
    }


    public void up(String field) throws SQLException {
        String table = DataBase.getForeignKeyTable(_dataList.get(_ind).table, field);
        _dataList.get(_ind).FK_field = field;
        ++_ind;
        _add(table);
    }

    public void down() throws SQLException {
        if (!isMain()) {
            _dataList.remove(_ind);
            --_ind;
            _resumeData(_dataList.get(_ind).table);
        }
    }


    public boolean isMain() {
        return _ind == 0;
    }




    public List<Map<String, Object>> getDataTable() throws SQLException {
        StringBuilder fields = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();
        StringBuilder where = new StringBuilder();

        Property[] properties = getProperties();

        if (properties != null && _fields != null && _columnTypes != null) {
            for (int i = 0; i < properties.length && i < _fields.length && i < _columnTypes.length; i++) {
                Property property = properties[i];
                if (!property.active) {
                    continue;
                }
                String fieldName = _fields[i];
                String fieldType = _columnTypes[i];
                if (fieldType.equals("text") || fieldType.equals("bpchar") ) {
                    fieldType = "text";
                } else if (fieldType.equals("numeric") || fieldType.equals("int4")) {
                    fieldType = "Integer";
                }

                if (fields.length() > 0) {
                    fields.append(", ");
                }
                fields.append(fieldName);

                // 2. Обрабатываем сортировку
                if (property.sort != null) {
                    if ("по возрастанию".equals(property.sort)) {
                        if (orderBy.length() > 0) {
                            orderBy.append(", ");
                        }
                        orderBy.append(fieldName).append(" ASC");
                    } else if ("по убыванию".equals(property.sort)) {
                        if (orderBy.length() > 0) {
                            orderBy.append(", ");
                        }
                        orderBy.append(fieldName).append(" DESC");
                    }
                }

                if (property.filter_name != null && !"Не задан".equals(property.filter_name) && 
                    property.filter != null && !property.filter.trim().isEmpty()) {
                    
                    String filterValue = property.filter.trim();

                    if ("Integer".equals(fieldType) || "BigDecimal".equals(fieldType)) {
                        // Числовые типы
                        if ("равно".equals(property.filter_name)) {
                            if (where.length() > 0) {
                                where.append(" AND ");
                            }
                            where.append(fieldName).append(" = ").append(filterValue);
                        } else if ("больше".equals(property.filter_name)) {
                            if (where.length() > 0) {
                                where.append(" AND ");
                            }
                            where.append(fieldName).append(" > ").append(filterValue);
                        } else if ("меньше".equals(property.filter_name)) {
                            if (where.length() > 0) {
                                where.append(" AND ");
                            }
                            where.append(fieldName).append(" < ").append(filterValue);
                        }
                    } else if ("text".equals(fieldType)) {
                        if ("содержит".equals(property.filter_name)) {
                            if (where.length() > 0) {
                                where.append(" AND ");
                            }
                            where.append(fieldName).append(" LIKE '%").append(filterValue).append("%'");
                        }
                    }
                }
            }
        }

        return DataBase.getTableData(
            getTableName(), 
            fields.length() > 0 ? fields.toString() : "", 
            orderBy.length() > 0 ? orderBy.toString() : "", 
            where.length() > 0 ? where.toString() : ""
        );
    }


    private void _resumeData(String table) throws SQLException {
        String[] fields = DataBase.getTableColumnsWithFK(table);

        int size = fields.length;

        _FKs = new boolean[size];
        _fields = new String[size];

        for (int i = 0; i < size; ++i) {
            String str = fields[i];
            if (str.charAt(str.length() - 1) == '*') {
                _FKs[i] = true;
                _fields[i] = str.substring(0, str.length() - 1);
            } else {
                _FKs[i] = false;
                _fields[i] = str;
            }
        }

        _columnTypes = DataBase.getTableColumnTypes(table);
    }

}
