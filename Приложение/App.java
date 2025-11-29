import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.util.List;


class Element {
    double widthRatio = 0.5;
    double heightRatio = 0.5;
    double fontScale = 0.5;



    public Element() {
        
    }

    public Element(double ratio) {
        this.widthRatio = ratio;
        this.heightRatio = ratio;
        this.fontScale = ratio;
    }

    public Element(double widthRatio, double heightRatio, double fontScale) {
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        this.fontScale = fontScale;
    }

    public Element(Element other) {
        this.widthRatio = other.widthRatio;
        this.heightRatio = other.heightRatio;
        this.fontScale = other.fontScale;
    }

    public Element copy() {
        return new Element(this);
    }
}






public class App extends JFrame  {
    private Map<Component, Element> _componentRatios = new HashMap<>();

    private JButton _buttonBack = new JButton("Назад");

    private boolean _init = false;

    private Data _data;
    private Order _order;
    private int _ind_table_pressed;

    private String _table;

    List<Map<String, Object>> _table_data;
    public boolean init() {
        return _init;
    }

    public App() {
        // SwingUtilities.invokeLater(() -> {
            _start();
        // });
    }

    private void _start() {
        // Настройки окна
        setTitle("Мастерская по ремонту автомобилей");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Открыть на весь экран
        setSize(900, 750);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                _updateAllComponents();
            }
        });

        _moveMainMenu();

        setVisible(true);
        _init = true;

        _buttonBack.setFocusPainted(false);

        _data = null;
        _order = null;
    }

    private void _moveMainMenu() {

        _clear();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        int size_elem = 2;
        double coefH = (1.0 - 0.15 * (size_elem + 1)) / size_elem;
        Element el = new Element(coefH, 0.5, 0.7);

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(20, 0, 20, 0);


        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.SOUTH;
        JButton b1 = new JButton("Данные");
        b1.setFocusPainted(false);
        b1.addActionListener(e -> _moveData());
        add(b1, el, gbc);
 
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        JButton b2 = new JButton("Заказы");
        b2.setFocusPainted(false);
        b2.addActionListener(e -> _moveOrders());
        add(b2, el, gbc);

        _update();
    }


    private void _moveData() {
        _orders_new = false;
        _clear();

        String[] tables = DataBase.getTables();

        int w = 1, h = 1;
        int len = tables.length;
        if (len > 1) {
            int val = (int)Math.sqrt(len);

            while (len % val != 0) --len;

            h = val;

            w = len / val;
        }

        int gridWithPaddingH = h + 2;
        int gridWithPaddingW = w + 2;
        setLayout(new BorderLayout());


        JLabel titleLabel = new JLabel("Данные", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonTablePanel = new JPanel(new GridLayout(gridWithPaddingH, gridWithPaddingW, 10, 10));
        buttonTablePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        ActionListener tableButtonHandler = e -> {
            JButton clickedButton = (JButton) e.getSource();
            String tableName = clickedButton.getText();
            _handleTableClickInData(tableName);
        };


        for (int i = 0; i < gridWithPaddingW; i++) {
            buttonTablePanel.add(new JLabel(""));
        }
        
        for (int row = 0; row < h; row++) {
            buttonTablePanel.add(new JLabel(""));
            
            for (int col = 0; col < w; col++) {
                int index = row * w + col;
                if (index < tables.length) {
                    JButton button = new JButton(tables[index]);
                    button.setFocusPainted(false);
                    button.addActionListener(tableButtonHandler);
                    button.setFont(new Font("Arial", Font.BOLD, 30));
                    button.setPreferredSize(new Dimension(300, 100));
                    buttonTablePanel.add(button);
                } else {
                    buttonTablePanel.add(new JLabel(""));
                }
            }
            
            buttonTablePanel.add(new JLabel(""));
        }
        for (int i = 1; i < gridWithPaddingW; i++) {
            buttonTablePanel.add(new JLabel(""));
        }
        add(buttonTablePanel, BorderLayout.CENTER);


        JPanel buttonBackPanel = new JPanel(new FlowLayout());
        buttonBackPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        _buttonBack.setFont(new Font("Arial", Font.BOLD, 16));
        _buttonBack.setPreferredSize(new Dimension(120, 40));
        _buttonBack.addActionListener(e -> {_moveMainMenu();});
        buttonBackPanel.add(_buttonBack);


        add(buttonBackPanel, BorderLayout.SOUTH);
        _update();
    }

    private void _moveOrders() {
        _orders_new = false;
        _clear();

        setLayout(new BorderLayout());

        JPanel up_panel = new JPanel(new FlowLayout());
        up_panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));


        int h = 90;
        int w = 300;
        int f = 25;

        JLabel titleLabel = new JLabel("Заказы", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);


        JButton up_button = new JButton("Новый");
        up_button.setFocusPainted(false);
        up_button.setFont(new Font("Arial", Font.BOLD, f));
        up_button.setPreferredSize(new Dimension(w, h));
        up_button.addActionListener(e -> {
            _orders_new = true;
            _order_data = new String[]{"", ""};
            _order_service = new String[]{""};
            _order_employee = new String[]{""};
            _moveNewOrder();
        });
        up_panel.add(up_button);


        JPanel center_panel = new JPanel(new FlowLayout());
        center_panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[] buttons = new String[]{"Отчет на оплату", "Отчет о работах", "Отчет о сотрудниках"};

        for (String buttonText : buttons) {
            JButton button = new JButton(buttonText);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, f));
            button.setPreferredSize(new Dimension(w, h));
            button.addActionListener(e -> _handleTableButtonClickInOrder(buttonText));
            center_panel.add(button);
        }


        JPanel main_center = new JPanel(new BorderLayout());
        main_center.add(up_panel, BorderLayout.NORTH);
        main_center.add(center_panel, BorderLayout.SOUTH);

        JPanel main_center2 = new JPanel(new GridLayout(3, 1, 0, 0));
        main_center2.add(new JLabel(""));
        main_center2.add(main_center);
        main_center2.add(new JLabel(""));

        JPanel down_panel = new JPanel(new FlowLayout());
        down_panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        _buttonBack.setFont(new Font("Arial", Font.BOLD, 16));
        _buttonBack.setPreferredSize(new Dimension(120, 40));
        _buttonBack.addActionListener(e -> {_moveMainMenu();});
        down_panel.add(_buttonBack);


        add(main_center2, BorderLayout.CENTER);
        add(down_panel, BorderLayout.SOUTH);

        _update();
    }


    private void _insertTable() throws SQLException {
        _table = _data.getTableName();
        String title = "Таблица: " + _data.getTableName();
        _table_data = _data.getDataTable();
        _ind_table_pressed = -1;
        _clear();

        boolean[] FKs = _data.getFKs();
        Property[] actives = _data.getProperties();
        boolean[] _FKs = new boolean[FKs.length];
        int size = 0;
        for (int i = 0; i < FKs.length; ++i) {
            if (actives[i].active) {
                _FKs[size++] = FKs[i];
            }
        }
        FKs = new boolean[size];
        for (int i = 0; i < size; ++i) {
            FKs[i] = _FKs[i];
        }



        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        if (_table_data != null && !_table_data.isEmpty()) {
            Set<String> allKeys = _table_data.get(0).keySet();
            List<String> columnNamesList = new ArrayList<>();
            List<Boolean> columnIsFKList = new ArrayList<>();
            
            int keyIndex = 0;
            for (String key : allKeys) {
                columnNamesList.add(key);
                boolean isFK = (FKs != null && keyIndex < FKs.length && FKs[keyIndex]);
                columnIsFKList.add(isFK);
                keyIndex++;
            }
            
            if (columnNamesList.size() > 10) {
                columnNamesList = columnNamesList.subList(0, 10);
                columnIsFKList = columnIsFKList.subList(0, 10);
            }
            
            // Создаем final копии для использования в анонимном классе
            final List<String> finalColumnNamesList = new ArrayList<>(columnNamesList);
            final List<Boolean> finalColumnIsFKList = new ArrayList<>(columnIsFKList);
            
            // Создаем списки для видимых колонок и FK колонок
            List<String> visibleColumnNames = new ArrayList<>();
            List<String> fkColumnNames = new ArrayList<>();
            
            for (int i = 0; i < finalColumnNamesList.size(); i++) {
                if (finalColumnIsFKList.get(i)) {
                    fkColumnNames.add(finalColumnNamesList.get(i));
                } else {
                    visibleColumnNames.add(finalColumnNamesList.get(i));
                }
            }
            
            // Добавляем колонки для кнопок Просмотреть для каждого FK
            for (String fkColumn : fkColumnNames) {
                visibleColumnNames.add("Просмотреть " + fkColumn);
            }
            
            String[] columnNames = visibleColumnNames.toArray(new String[0]);
            Object[][] data = new Object[_table_data.size()][visibleColumnNames.size()];
            
            // Заполняем данные таблицы
            for (int row = 0; row < _table_data.size(); row++) {
                Map<String, Object> originalRow = _table_data.get(row);
                int colIndex = 0;
                
                // Заполняем обычные колонки
                for (int i = 0; i < finalColumnNamesList.size(); i++) {
                    if (!finalColumnIsFKList.get(i)) {
                        data[row][colIndex] = originalRow.get(finalColumnNamesList.get(i));
                        colIndex++;
                    }
                }
                
                // Заполняем колонки с кнопками (храним данные FK)
                for (int i = 0; i < finalColumnNamesList.size(); i++) {
                    if (finalColumnIsFKList.get(i)) {
                        String columnName = finalColumnNamesList.get(i);
                        Object fkValue = originalRow.get(columnName);
                        // Сохраняем объект с информацией о FK
                        FKButtonData buttonData = new FKButtonData(columnName, fkValue, row);
                        data[row][colIndex] = buttonData;
                        colIndex++;
                    }
                }
            }

            JTable table = new JTable(data, columnNames) {
                @Override
                public Class<?> getColumnClass(int column) {
                    // Определяем класс для колонок с кнопками
                    for (int i = 0; i < finalColumnNamesList.size(); i++) {
                        if (finalColumnIsFKList.get(i)) {
                            int fkColumnIndex = visibleColumnNames.indexOf("Просмотреть " + finalColumnNamesList.get(i));
                            if (column == fkColumnIndex) {
                                return FKButtonData.class;
                            }
                        }
                    }
                    return Object.class;
                }
            };

            // Настраиваем рендерер и редактор для колонок с кнопками
            for (int i = 0; i < finalColumnNamesList.size(); i++) {
                if (finalColumnIsFKList.get(i)) {
                    String fkColumnName = finalColumnNamesList.get(i);
                    int columnIndex = visibleColumnNames.indexOf("Просмотреть " + fkColumnName);
                    if (columnIndex >= 0) {
                        table.getColumnModel().getColumn(columnIndex)
                            .setCellRenderer(new ButtonRenderer());
                        table.getColumnModel().getColumn(columnIndex)
                            .setCellEditor(new ButtonEditor(new JCheckBox(), fkColumnName));
                    }
                }
            }

            table.getTableHeader().setBackground(Color.cyan);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowHeight(30);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    _ind_table_pressed = table.getSelectedRow();
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
            add(scrollPane, BorderLayout.CENTER);
        } else {
            JLabel noDataLabel = new JLabel("Нет данных для отображения", SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noDataLabel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
            add(noDataLabel, BorderLayout.CENTER);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        String[] buttons;
        if (_order != null) {
            buttons = new String[]{"Назад", "Выбрать", "Свойства"};
        }
        else if (_data.isMain()) {
            buttons = new String[]{"Назад", "Добавить", "Изменить", "Удалить", "Свойства"};
        } else {
            buttons = new String[]{"Назад", "Добавить", "Выбрать", "Свойства"};
        }
        
        for (String buttonText : buttons) {
            JButton button = new JButton(buttonText);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setPreferredSize(new Dimension(120, 40));
            button.addActionListener(e -> _handleTableButtonClick(buttonText));
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        _update();
    }

    // Класс для хранения данных о FK для кнопки
    class FKButtonData {
        private String columnName;
        private Object value;
        private int row;
        
        public FKButtonData(String columnName, Object value, int row) {
            this.columnName = columnName;
            this.value = value;
            this.row = row;
        }
        
        public String getColumnName() { return columnName; }
        public Object getValue() { return value; }
        public int getRow() { return row; }
        
        @Override
        public String toString() {
            return "Просмотреть";
        }
    }

    // Рендерер для кнопок
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("Просмотреть");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }

    // Редактор для кнопок
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String fkColumnName;
        private FKButtonData currentData;

        public ButtonEditor(JCheckBox checkBox, String fkColumnName) {
            super(checkBox);
            this.fkColumnName = fkColumnName;
            
            button = new JButton("Просмотреть");
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                // Обрабатываем нажатие сразу здесь
                if (currentData != null) {
                    // System.out.println("Нажата кнопка 'Просмотреть' для колонки: " + currentData.getColumnName());
                    // System.out.println("Значение: " + currentData.getValue());
                    // System.out.println("Строка: " + currentData.getRow());
                    
                    // Вызываем метод обработки
                    _handleFKView(currentData);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentData = (FKButtonData) value;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return currentData;
        }
    }


    private void showFKData(String tableName, String fkColumnName, String fkValue) throws SQLException {
        List<Map<String, Object>> data = DataBase.getReferencedTableData(_table, fkColumnName, fkValue);
        String referencedTable = DataBase.getForeignKeyTable(tableName, fkColumnName);
        _table = referencedTable;
        String title = "Просмотр таблицы " + referencedTable;

        _clear();
        setLayout(new BorderLayout());

        // Заголовок
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        if (data != null && !data.isEmpty()) {
            Set<String> allKeys = data.get(0).keySet();
            List<String> columnNamesList = new ArrayList<>(allKeys);
            
            // Получаем информацию о FK для связанной таблицы
            boolean[] FKs = DataBase.getTableFKs(referencedTable);
            
            List<Boolean> columnIsFKList = new ArrayList<>();
            for (int i = 0; i < columnNamesList.size(); i++) {
                boolean isFK = (FKs != null && i < FKs.length && FKs[i]);
                columnIsFKList.add(isFK);
            }
            
            if (columnNamesList.size() > 10) {
                columnNamesList = columnNamesList.subList(0, 10);
                columnIsFKList = columnIsFKList.subList(0, 10);
            }
            
            // Создаем final копии для использования в анонимном классе
            final List<String> finalColumnNamesList = new ArrayList<>(columnNamesList);
            final List<Boolean> finalColumnIsFKList = new ArrayList<>(columnIsFKList);
            
            // Создаем списки для видимых колонок и FK колонок
            List<String> visibleColumnNames = new ArrayList<>();
            List<String> fkColumnNames = new ArrayList<>();
            
            for (int i = 0; i < finalColumnNamesList.size(); i++) {
                if (finalColumnIsFKList.get(i)) {
                    fkColumnNames.add(finalColumnNamesList.get(i));
                } else {
                    visibleColumnNames.add(finalColumnNamesList.get(i));
                }
            }
            
            // Добавляем колонки для кнопок Просмотреть для каждого FK
            for (String fkColumn : fkColumnNames) {
                visibleColumnNames.add("Просмотреть " + fkColumn);
            }
            
            String[] columnNames = visibleColumnNames.toArray(new String[0]);
            Object[][] tableData = new Object[data.size()][visibleColumnNames.size()];
            
            // Заполняем данные таблицы
            for (int row = 0; row < data.size(); row++) {
                Map<String, Object> originalRow = data.get(row);
                int colIndex = 0;
                
                // Заполняем обычные колонки
                for (int i = 0; i < finalColumnNamesList.size(); i++) {
                    if (!finalColumnIsFKList.get(i)) {
                        tableData[row][colIndex] = originalRow.get(finalColumnNamesList.get(i));
                        colIndex++;
                    }
                }
                
                // Заполняем колонки с кнопками (храним данные FK)
                for (int i = 0; i < finalColumnNamesList.size(); i++) {
                    if (finalColumnIsFKList.get(i)) {
                        String columnName = finalColumnNamesList.get(i);
                        Object fkValueObj = originalRow.get(columnName);
                        // Сохраняем объект с информацией о FK
                        FKButtonData buttonData = new FKButtonData(columnName, fkValueObj, row);
                        tableData[row][colIndex] = buttonData;
                        colIndex++;
                    }
                }
            }

            JTable table = new JTable(tableData, columnNames) {
                @Override
                public Class<?> getColumnClass(int column) {
                    // Определяем класс для колонок с кнопками
                    for (int i = 0; i < finalColumnNamesList.size(); i++) {
                        if (finalColumnIsFKList.get(i)) {
                            int fkColumnIndex = visibleColumnNames.indexOf("Просмотреть " + finalColumnNamesList.get(i));
                            if (column == fkColumnIndex) {
                                return FKButtonData.class;
                            }
                        }
                    }
                    return Object.class;
                }
            };

            // Настраиваем рендерер и редактор для колонок с кнопками
            for (int i = 0; i < finalColumnNamesList.size(); i++) {
                if (finalColumnIsFKList.get(i)) {
                    String fkColumnNameInner = finalColumnNamesList.get(i);
                    int columnIndex = visibleColumnNames.indexOf("Просмотреть " + fkColumnNameInner);
                    if (columnIndex >= 0) {
                        table.getColumnModel().getColumn(columnIndex)
                            .setCellRenderer(new ButtonRenderer());
                        table.getColumnModel().getColumn(columnIndex)
                            .setCellEditor(new ButtonEditor(new JCheckBox(), fkColumnNameInner));
                    }
                }
            }

            table.getTableHeader().setBackground(Color.cyan);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowHeight(30);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    _ind_table_pressed = table.getSelectedRow();
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
            add(scrollPane, BorderLayout.CENTER);
        } else {
            JLabel noDataLabel = new JLabel("Нет данных для отображения", SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noDataLabel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
            add(noDataLabel, BorderLayout.CENTER);
        }

        // Панель с кнопкой Назад
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JButton backButton = new JButton("Назад");
        backButton.setFocusPainted(false);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> {
            try {
                _insertTable();
            } catch (SQLException ex) {
                _showMessage(ex.getMessage());
            }
        });
        
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        _update();
    }

    // Метод для обработки просмотра FK
    private void _handleFKView(FKButtonData fkData) {
        // System.out.println("=== Обработка FK просмотра ===");
        // System.out.println("Колонка: " + fkData.getColumnName());
        // System.out.println("Значение: " + fkData.getValue());
        // System.out.println("Строка в таблице: " + fkData.getRow());
        // System.out.println("=============================");
        
        try {
            showFKData(_table, fkData.getColumnName(), fkData.getValue().toString());
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
        }
    }


    private void _handleTableButtonClick(String buttonText) {
        switch (buttonText) {
            case "Добавить":
                _handleAddButton();
                break;
            case "Изменить":
                _handleEditButton();
                break;
            case "Удалить":
                _handleDeleteButton();
                break;
            case "Свойства":
                _handlePropertyButton();
                break;
            case "Выбрать":
                if (_order != null) {
                    if (_ind_table_pressed < 0 || _table_data.size() <= _ind_table_pressed) {
                        _showMessage("Выберите строку");
                        return;
                    }

                    try {
                        Property[] pr_real = _data.getProperties();
                        Property[] pr = new Property[pr_real.length];

                        for (int i = 0; i < pr_real.length; ++i) {
                            pr[i] = pr_real[i].clone();
                            pr[i].active = true;
                        }

                        _data.setProperties(pr);
                        Map<String, Object> rowData;
                        try {
                            rowData = _data.getDataTable().get(_ind_table_pressed);  
                        } catch (Exception e) {
                            _showMessage(e.getMessage());
                            return;
                        }
                        _data.setProperties(pr_real);


                        int id = (Integer)rowData.get("id");;
                        _order.setId(id);
                        _showFilterAndSortForOrderReport();
                    }
                    catch(Exception e) {
                        _showMessage(e.getMessage());
                    }
                    
                } else {
                    _handleEnterButton();
                }
                break;
            case "Назад":
                if (_orders_new) {
                    try {
                        _data.down();
                    }
                    catch (Exception e) {
                        _showMessage(e.getMessage());
                        return;
                    }

                    if (_data.isMain()) {
                        _moveNewOrder();
                    } else {
                        try {
                            _moveEditValueTable(_data.getType());
                        }
                        catch (Exception e) {
                            _showMessage(e.getMessage());
                            return;
                        }
                    }
                }
                else if (_order != null) {
                    _data = null;
                    _order = null;
                    _moveOrders();
                } else {
                    _handleBackButton();
                }
                break;
        }
    }

    
    private void _moveEditValueTable(String button) {
        String title = button + " данные в табл. " + _data.getTableName();
        String[] columnNames = {"Поле", "Данные"};
        String[] originalFields = _data.getFields();
        boolean[] originalFKs = _data.getFKs();
        String[] originalEnterData = _data.getEnterData();
        _clear();

        
        List<String> filteredFields = new ArrayList<>();
        List<Boolean> filteredFKs = new ArrayList<>();
        List<String> filteredEnterData = new ArrayList<>();
        
        for (int i = 0; i < originalFields.length; i++) {
            if (!"id".equalsIgnoreCase(originalFields[i])) {
                filteredFields.add(originalFields[i]);
                filteredFKs.add(originalFKs[i]);
                filteredEnterData.add(originalEnterData[i]);
            }
        }
        
        
        final String[] fields = filteredFields.toArray(new String[0]);
        final boolean[] FKs = new boolean[filteredFKs.size()];
        for (int i = 0; i < filteredFKs.size(); i++) {
            FKs[i] = filteredFKs.get(i);
        }
        final String[] enter_data = filteredEnterData.toArray(new String[0]);


        Object[][] data = new Object[fields.length][2];
        for (int i = 0; i < fields.length; i++) {
            data[i][0] = fields[i];
            data[i][1] = enter_data[i];
        }


        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; 
            }
        };

        JTable table = new JTable(model);
        table.getTableHeader().setBackground(Color.CYAN);
        

        table.getColumnModel().getColumn(1).setCellRenderer(new ButtonCellRenderer(FKs, enter_data));
        table.getColumnModel().getColumn(1).setCellEditor(new ButtonCellEditor(FKs, fields));

        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));


        setLayout(new BorderLayout());
        

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);
        

        add(scrollPane, BorderLayout.CENTER);
        

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JButton backButton = new JButton("Назад");
        JButton actionButton = new JButton(button);
        backButton.setFocusPainted(false);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setPreferredSize(new Dimension(120, 40));

        actionButton.setFocusPainted(false);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        actionButton.setPreferredSize(new Dimension(120, 40));

        backButton.addActionListener(e -> _handleBackFromEdit());
        actionButton.addActionListener(e -> _handleActionButton(table, fields, FKs, button));
        
        buttonPanel.add(backButton);
        buttonPanel.add(actionButton);
        add(buttonPanel, BorderLayout.SOUTH);


        _currentEditTable = table;
        _currentEditFields = fields;
        _currentEditFKs = FKs;
        _update();
    }
    
    class ButtonCellRenderer implements TableCellRenderer {
        private boolean[] FKs;
        private String[] enter_data;
        private JButton button = new JButton();
        private JTextField textField = new JTextField();
        
        public ButtonCellRenderer(boolean[] FKs, String[] enter_data) {
            this.FKs = FKs;
            this.enter_data = enter_data;
            button.setOpaque(true);
            textField.setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (FKs[row]) {
                button.setText("Выбрать");
                if (isNumeric(enter_data[row])) {
                    button.setBackground(Color.GREEN);
                } else {
                    button.setBackground(Color.RED);
                }
                return button;
            } else {
                textField.setText(value != null ? value.toString() : "");
                textField.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                return textField;
            }
        }
        
        private boolean isNumeric(String str) {
            if (str == null || str.trim().isEmpty()) return false;
            return str.matches("\\d+");
        }
    }



    class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor {
        private boolean[] FKs;
        private String[] fields;
        private int currentRow;
        private JButton button = new JButton();
        private JTextField textField = new JTextField();
        
        public ButtonCellEditor(boolean[] FKs, String[] fields) {
            this.FKs = FKs;
            this.fields = fields;
            
            this.button.addActionListener(e -> {
                fireEditingStopped();
                _handleSelectButtonClick(fields[currentRow], currentRow);
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            
            if (FKs[row]) {
                button.setText("Выбрать");
                return button;
            } else {
                textField.setText(value != null ? value.toString() : "");
                return textField;
            }
        }
        
        @Override
        public Object getCellEditorValue() {
            if (FKs[currentRow]) {
                return button.getText();
            } else {
                return textField.getText();
            }
        }
    }

    // Метод для получения данных из таблицы
    private String[] getDataFromTable(JTable table, String[] fields, boolean[] FKs) {
        String[] result = new String[fields.length];
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        for (int i = 0; i < fields.length; i++) {
            result[i] = model.getValueAt(i, 1).toString();
        }
        
        return result;
    }

    
    private void _handleBackFromEdit() {
        try {
            _insertTable();
        }
        catch( Exception e) {
            _showMessage(e.getMessage());
        }
    }

    private void _handleActionButton(JTable table, String[] fields, boolean[] FKs, String button) {
        String[] data = getDataFromTable(table, fields, FKs);


        // System.out.println("Действие: " + button);
        // for (int i = 0; i < fields.length; i++) {
        //     System.out.println(fields[i] + ": " + data[i]);
        // }
        fields = _data.getFields();
        String[] new_data = new String[fields.length];
        int r = 0;
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].equals("id")) {
                new_data[i] = "";
                r = 1;
            } else {
                new_data[i] = data[i - r];
            }
        }


        _data.setEnterData(new_data);
        try {
            _data.execute();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }

        try {
            _insertTable();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
    }

    private JTable _currentEditTable;
    private String[] _currentEditFields;
    private boolean[] _currentEditFKs;

    private void _handleSelectButtonClick(String fieldName, int rowIndex) {

        String[] data = getDataFromTable(_currentEditTable, _currentEditFields, _currentEditFKs);
        
        
        String[] fields = _data.getFields();
        String[] new_data = new String[fields.length];
        int r = 0;
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].equals("id")) {
                new_data[i] = "";
                r = 1;
            } else {
                new_data[i] = data[i - r];
            }
        }

        _data.setEnterData(new_data);
        try {
            _data.up(fieldName);
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }

        try {
            _insertTable();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
    }



    private void _handleAddButton() {
        _data.setCreate();

        String[] enter_data = _data.getEnterData();

        for (int i = 0; i < enter_data.length; ++i) {
            enter_data[i] = "";
        }
        _data.setEnterData(enter_data);

        _moveEditValueTable("Добавить");
    }

    private void _handleEditButton() {
        if (_ind_table_pressed < 0 || _table_data.size() <= _ind_table_pressed) {
            _showMessage("Выберите строку");
            return;
        }

        _data.setUpdate(_ind_table_pressed);

        String[] fields = _data.getFields();
        String[] enter_data = _data.getEnterData();

        Property[] pr_real = _data.getProperties();
        Property[] pr = new Property[pr_real.length];

        for (int i = 0; i < pr_real.length; ++i) {
            pr[i] = pr_real[i].clone();
            pr[i].active = true;
        }

        _data.setProperties(pr);
        Map<String, Object> rowData;
        try {
            rowData = _data.getDataTable().get(_ind_table_pressed);  
        } catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
        _data.setProperties(pr_real);

        for (int i = 0; i < enter_data.length; ++i) {
            enter_data[i] = rowData.get(fields[i]).toString();
        }
        _data.setEnterData(enter_data);

        _moveEditValueTable("Изменить");
    }

    private void _handleDeleteButton() {
        if (_ind_table_pressed < 0 || _table_data.size() <= _ind_table_pressed) {
            _showMessage("Выберите колонку");
            return;
        }

        Property[] pr_real = _data.getProperties();
        Property[] pr = new Property[pr_real.length];

        for (int i = 0; i < pr_real.length; ++i) {
            pr[i] = pr_real[i].clone();
            pr[i].active = true;
        }

        _data.setProperties(pr);
        Map<String, Object> rowData;
        try {
            rowData = _data.getDataTable().get(_ind_table_pressed);  
        } catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
        _data.setProperties(pr_real);
    


        Integer id = (Integer) rowData.get("id");
    
        try {
            _data.setDelete(id);
            _data.execute();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }

        try {
            _insertTable();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            _moveMainMenu();
            return;
        }
    }

    private void _handlePropertyButton() {
        _insertProperty();
    }
    
    private void _insertProperty() {
        String title = "Свойства таблицы " + _data.getTableName();
        _clear();

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        String[] fields = _data.getFields();
        Property[] properties = _data.getProperties();
        
        Object[][] data = new Object[fields.length][5];
        for (int i = 0; i < fields.length; i++) {
            Property prop = properties[i];
            data[i][0] = fields[i];
            data[i][1] = prop.active;
            data[i][2] = prop.filter_name;
            data[i][3] = prop.filter;
            data[i][4] = prop.sort;
        }

        String[] columnNames = {"Поле", "Активность", "Свойство фильтра", "Фильтр", "Сортировка"};

        JTable table = new JTable(new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1;
            }
        });
        table.getTableHeader().setBackground(Color.cyan);

        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JButton backButton = new JButton("Назад");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> _handleBackFromProperties());
        buttonPanel.add(backButton);

        JButton showButton = new JButton("Показать");
        showButton.setFont(new Font("Arial", Font.BOLD, 16));
        showButton.setPreferredSize(new Dimension(120, 40));
        showButton.addActionListener(e -> _handleShowWithProperties(table, fields));
        buttonPanel.add(showButton);

        add(buttonPanel, BorderLayout.SOUTH);

        _update();
    }

    private void _handleShowWithProperties(JTable table, String[] fields) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        Property[] updatedProperties = new Property[fields.length];
        
        for (int i = 0; i < fields.length; i++) {
            Property prop = new Property();
            prop.active = (Boolean) model.getValueAt(i, 1);
            prop.filter_name = (String) model.getValueAt(i, 2);
            prop.filter = (String) model.getValueAt(i, 3);
            prop.sort = (String) model.getValueAt(i, 4);
            
            updatedProperties[i] = prop;
        }
        
        _data.setProperties(updatedProperties);

        try {
            _insertTable();
        }
        catch(Exception e) {
            _showMessage(e.getMessage());
        }
    }

    private void _handleBackFromProperties() {
        try {
            _insertTable();
        }
        catch(Exception e) {
            _showMessage(e.getMessage());
        }
    }



    private void _handleBackButton() {
        if (_data.isMain()) {
            _data = null;
            _moveData();
        } else {
            try {
                _data.down();
            }
            catch (Exception e) {
                _showMessage(e.getMessage());
                return;
            }

            try {
                _moveEditValueTable(_data.getType());
                // _insertTable();
            }
            catch (Exception e) {
                _showMessage(e.getMessage());
                return;
            }
        }
    }

    private void _handleEnterButton() {
        if (_ind_table_pressed < 0 || _table_data.size() <= _ind_table_pressed) {
            _showMessage("Выберите строку");
            return;
        }

        Property[] pr_real = _data.getProperties();
        Property[] pr = new Property[pr_real.length];

        for (int i = 0; i < pr_real.length; ++i) {
            pr[i] = pr_real[i].clone();
            pr[i].active = true;
        }

        _data.setProperties(pr);
        Map<String, Object> rowData;
        try {
            rowData = _data.getDataTable().get(_ind_table_pressed);  
        } catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
        _data.setProperties(pr_real);

        String id = rowData.get("id").toString();
        try {
            _data.down();
            String[] entered_data = _data.getEnterData();
            String field = _data.getFKField();
            String[] fields = _data.getFields();

            for (int i = 0; i < fields.length; ++i) {
                if (fields[i].equals(field)) {
                    entered_data[i] = id;
                    break;
                }
            }

            _data.setEnterData(entered_data);


        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }

        try { 
            if (_orders_new && _data.isMain()) {
                switch (_order_table) {
                    case 0:
                        _order_data[_order_table_parameter] = id;
                        break;
                    case 1:
                        switch(_order_table_parameter) {
                            case 0:
                                _order_service[_order_service_row] = id;
                                break;
                            case 1:
                                _order_employee[_order_service_row] = id;
                                break;
                        }
                        break;
                }
                _moveNewOrder();
            }  else {
                _moveEditValueTable(_data.getType());
            }
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }


    }







    private void _handleTableClickInData(String tableName) {
        try {
            _data = new Data(tableName);
            _order = null;
            _insertTable();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
    }


    private void _update() {
        revalidate();
        repaint();   
    }







    public Component add(Component comp, Element element) {
        _componentRatios.put(comp, element.copy());
        _updateComponent(comp);
        return super.add(comp);
    }
    
    public Component add(Component comp, Element element, int index) {
        _componentRatios.put(comp, element.copy());
        _updateComponent(comp);
        return super.add(comp, index);
    }
    
    public void add(Component comp, Element element, Object constraints) {
        _componentRatios.put(comp, element.copy());
        _updateComponent(comp);
        super.add(comp, constraints);
    }
    
    public void add(Component comp, Element element, Object constraints, int index) {
        _componentRatios.put(comp, element.copy());
        _updateComponent(comp);
        super.add(comp, constraints, index);
    }


    private void _updateAllComponents() {
        for (Component comp : _componentRatios.keySet()) {
            _updateComponent(comp);
        }
        revalidate();
        repaint();
    }
    
    private void _updateComponent(Component comp) {
        
        Element ratios = _componentRatios.get(comp);
        if (ratios == null) return;
        
        int newWidth = (int)(getWidth() * ratios.heightRatio);
        int newHeight = (int)(getHeight() * ratios.widthRatio);
        
        comp.setPreferredSize(new Dimension(newWidth, newHeight));
        comp.setMinimumSize(new Dimension(newWidth, newHeight));
        comp.setMaximumSize(new Dimension(newWidth, newHeight));
        comp.setSize(newWidth, newHeight);
        
        if (comp.getFont() != null) {
            float newFontSize = Math.min(newHeight, newWidth / _getComponentText(comp).length());
            newFontSize *= (float)ratios.fontScale;
            comp.setFont(comp.getFont().deriveFont(newFontSize));
        }
    }
    
    public void _clear() {
        getContentPane().removeAll();
        _componentRatios.clear();
        revalidate();
        repaint();
    }

    private String _getComponentText(Component comp) {
        if (comp instanceof JButton) {
            return ((JButton) comp).getText();
        } else if (comp instanceof JLabel) {
            return ((JLabel) comp).getText();
        } else if (comp instanceof JTextField) {
            return ((JTextField) comp).getText();
        } else if (comp instanceof JTextArea) {
            return ((JTextArea) comp).getText();
        } else if (comp instanceof JCheckBox) {
            return ((JCheckBox) comp).getText();
        } else if (comp instanceof JRadioButton) {
            return ((JRadioButton) comp).getText();
        } else if (comp instanceof AbstractButton) {
            return ((AbstractButton) comp).getText();
        }
        return " ";
    }


    private void _showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void _showMessage(Exception e) {
        _showMessage(e.getMessage());
    }


    private String[] _order_data;
    private String[] _order_service;
    private String[] _order_employee;

    boolean _orders_new =  false;
    
    private void _moveNewOrder() {
        _clear();
        String title = "Новый заказ";
        String order_title = "Данные заказа";
        String[] columnNames = new String[]{"Данные", "Выбор"};
        String[] data_order = new String[]{"Машина", "Принял заказ"};

        String service_title = "Данные услуг";
        String[] columnNamesService = new String[]{"Номер", "Сервис", "Работник"};

        setLayout(new BorderLayout());

        // Заголовок
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Основная панель с таблицами
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        // Панель для данных заказа
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JLabel orderTitleLabel = new JLabel(order_title);
        orderTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        orderPanel.add(orderTitleLabel, BorderLayout.NORTH);
        
        // Создаем таблицу для данных заказа
        Object[][] orderData = new Object[data_order.length][2];
        for (int i = 0; i < data_order.length; i++) {
            orderData[i][0] = data_order[i];
            orderData[i][1] = _order_data[i]; // Используем существующий массив
        }

        JTable orderTable = new JTable(orderData, columnNames);
        orderTable.getTableHeader().setBackground(Color.cyan);
        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Устанавливаем рендерер и редактор для колонки с кнопками
        orderTable.getColumnModel().getColumn(1).setCellRenderer(new OrderButtonCellRenderer());
        orderTable.getColumnModel().getColumn(1).setCellEditor(new OrderButtonCellEditor());

        JScrollPane orderScrollPane = new JScrollPane(orderTable);
        orderScrollPane.setPreferredSize(new Dimension(400, data_order.length * 35 + 30));
        orderPanel.add(orderScrollPane, BorderLayout.CENTER);

        // Панель для данных услуг
        JPanel servicePanel = new JPanel(new BorderLayout());
        
        JLabel serviceTitleLabel = new JLabel(service_title);
        serviceTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        servicePanel.add(serviceTitleLabel, BorderLayout.NORTH);

        // Создаем таблицу для услуг
        Object[][] serviceData = new Object[_order_service.length][3];
        for (int i = 0; i < _order_service.length; i++) {
            serviceData[i][0] = i + 1; // Номер по счету
            serviceData[i][1] = _order_service[i]; // Сервис
            serviceData[i][2] = _order_employee[i]; // Работник
        }

        JTable serviceTable = new JTable(serviceData, columnNamesService);
        serviceTable.getTableHeader().setBackground(Color.cyan);
        serviceTable.setRowHeight(30);
        serviceTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Устанавливаем рендерер и редактор для колонок с кнопками
        serviceTable.getColumnModel().getColumn(1).setCellRenderer(new ServiceButtonCellRenderer(0));
        serviceTable.getColumnModel().getColumn(1).setCellEditor(new ServiceButtonCellEditor(0));
        
        serviceTable.getColumnModel().getColumn(2).setCellRenderer(new ServiceButtonCellRenderer(1));
        serviceTable.getColumnModel().getColumn(2).setCellEditor(new ServiceButtonCellEditor(1));

        JScrollPane serviceScrollPane = new JScrollPane(serviceTable);
        serviceScrollPane.setPreferredSize(new Dimension(600, Math.min(200, _order_service.length * 35 + 30)));
        servicePanel.add(serviceScrollPane, BorderLayout.CENTER);

        mainPanel.add(orderPanel);
        mainPanel.add(servicePanel);
        add(mainPanel, BorderLayout.CENTER);

        // Панель кнопок
        String[] buttons = new String[]{"Назад", "Добавить услугу", "Создать"};

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        for (String buttonText : buttons) {
            JButton button = new JButton(buttonText);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setPreferredSize(new Dimension(150, 40));
            button.addActionListener(e -> _handleButtonClickNewOrder(buttonText));
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        _update();
    }

    // Классы для рендерера и редактора кнопок данных заказа
    class OrderButtonCellRenderer implements TableCellRenderer {
        private JButton button = new JButton();
        
        public OrderButtonCellRenderer() {
            button.setOpaque(true);
            button.setText("Выбрать");
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String currentValue = table.getModel().getValueAt(row, column).toString();
            if (currentValue.isEmpty()) {
                button.setBackground(Color.RED);
            } else {
                button.setBackground(Color.GREEN);
            }
            return button;
        }
    }

    class OrderButtonCellEditor extends AbstractCellEditor implements TableCellEditor {
        private int currentRow;
        private JButton button = new JButton();
        
        public OrderButtonCellEditor() {
            this.button.setText("Выбрать");
            
            this.button.addActionListener(e -> {
                fireEditingStopped();
                _handleOrderSelectButtonClick(currentRow);
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    // Классы для рендерера и редактора кнопок услуг
    class ServiceButtonCellRenderer implements TableCellRenderer {
        private int type; // 0 - сервис, 1 - работник
        private JButton button = new JButton();
        
        public ServiceButtonCellRenderer(int type) {
            this.type = type;
            button.setOpaque(true);
            button.setText("Выбрать");
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String currentValue = table.getModel().getValueAt(row, column).toString();
            if (currentValue.isEmpty()) {
                button.setBackground(Color.RED);
            } else {
                button.setBackground(Color.GREEN);
            }
            return button;
        }
    }

    class ServiceButtonCellEditor extends AbstractCellEditor implements TableCellEditor {
        private int type; // 0 - сервис, 1 - работник
        private int currentRow;
        private JButton button = new JButton();
        
        public ServiceButtonCellEditor(int type) {
            this.type = type;
            this.button.setText("Выбрать");
            
            this.button.addActionListener(e -> {
                fireEditingStopped();
                _handleServiceSelectButtonClick(currentRow, type);
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    // Обработчик кнопок
    private void _handleButtonClickNewOrder(String buttonText) {
        switch (buttonText) {
            case "Назад":
                _moveOrders();
                break;
            case "Добавить услугу":
                String[] newService = Arrays.copyOf(_order_service, _order_service.length + 1);
                String[] newEmployee = Arrays.copyOf(_order_employee, _order_employee.length + 1);
                newService[newService.length - 1] = "";
                newEmployee[newEmployee.length - 1] = "";
                _order_service = newService;
                _order_employee = newEmployee;
                _moveNewOrder();
                break;
            case "Создать":
                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO Orders (car_id, employee_id_created) VALUES (").append(_order_data[0]).append(", ").append(_order_data[1]).append("); ");
                sql.append("INSERT INTO Order_Services (order_id, service_id, employee_id_assigned, price) VALUES ");

                for (int i = 0; i < _order_service.length; i++) {
                    if (i > 0) sql.append(", ");
                    sql.append("(currval('orders_id_seq'), ").append(_order_service[i]).append(", ").append(_order_employee[i])
                    .append(", (SELECT price FROM Services WHERE id = ").append(_order_service[i]).append("))");
                }

                String sqlString = sql.toString();

                // System.out.println("Order Data: " + Arrays.toString(_order_data));
                // System.out.println("Order Service: " + Arrays.toString(_order_service));
                // System.out.println("Order Employee: " + Arrays.toString(_order_employee));

                 System.out.println(sqlString);
                try {
                    DataBase.executeUpdate(sqlString);
                    _moveOrders();
                } catch (Exception e) {
                    _showMessage(e);
                }
                break;
        }
    }

    private int _order_table;
    private int _order_table_parameter;
    private int _order_service_row;

    // Обработчики выбора для кнопок в таблицах
    private void _handleOrderSelectButtonClick(int row) {
        
        // System.out.println("Выбрана строка: " + row + " в таблице данных заказа");
        _order_table = 0;
        _order_table_parameter = row;
        try {
            _data = new Data("orders");
            switch(row) {
                case 0:
                    _data.up("car_id");
                    break;
                case 1:
                    _data.up("employee_id_created");
                    break;
            }
            _insertTable();
        } catch (Exception e) {
            _showMessage(e);
        }
    }

    private void _handleServiceSelectButtonClick(int row, int type) {
        // System.out.println("Выбрана строка: " + row + ", тип: " + (type == 0 ? "Сервис" : "Работник"));
        _order_table = 1;
        _order_table_parameter = type;
        _order_service_row = row;
        try {
            _data = new Data("order_services");
            switch(type) {
                case 0:
                    _data.up("service_id");
                    break;
                case 1:
                    _data.up("employee_id_assigned");
                    break;
            }
            _insertTable();
        } catch (Exception e) {
            _showMessage(e);
        }
    }



    private void _handleTableButtonClickInOrder(String button) {
        try {
            _data = new Data("orders");
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
        _order = new Order(button);

        try {
            _insertTable();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }
    }

    private JTable _sortTable;
    private JTable _filterTable;

    private void _showFilterAndSortForOrderReport() {
        _clear();
        _sortTable = null;
        _filterTable = null;

        String text = "Данные отсутствуют";

        String main_title = _order.getType();

        String title_sort = "Данные сортировки";
        String[] data_sort = _order.getSort();

        String title_filter = "Данные фильтров";
        String[] data_filter = _order.getFilter();

        setLayout(new BorderLayout());

        // Заголовок
        JLabel titleLabel = new JLabel(main_title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Основная панель с сортировкой и фильтрацией
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        // Панель для сортировки
        JPanel sortPanel = new JPanel(new BorderLayout());
        sortPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JLabel sortTitleLabel = new JLabel(title_sort);
        sortTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        sortPanel.add(sortTitleLabel, BorderLayout.NORTH);

        if (data_sort == null || data_sort.length == 0) {
            JLabel noSortLabel = new JLabel(text);
            noSortLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            sortPanel.add(noSortLabel, BorderLayout.CENTER);
            
        } else {
            // Создаем таблицу для сортировки (2 колонки)
            String[] sortColumnNames = {"Поле", "Сортировка"};
            Object[][] sortData = new Object[data_sort.length][2];
            
            for (int i = 0; i < data_sort.length; i++) {
                sortData[i][0] = data_sort[i];
                sortData[i][1] = ""; // Пустая строка для ввода
            }
            
            _sortTable = new JTable(sortData, sortColumnNames);
            _sortTable.getTableHeader().setBackground(Color.cyan);
            _sortTable.setRowHeight(30);
            _sortTable.setFont(new Font("Arial", Font.PLAIN, 14));
            _sortTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            
            JScrollPane sortScrollPane = new JScrollPane(_sortTable);
            sortScrollPane.setPreferredSize(new Dimension(400, Math.min(200, data_sort.length * 35 + 30)));
            sortPanel.add(sortScrollPane, BorderLayout.CENTER);
        }

        // Панель для фильтров
        JPanel filterPanel = new JPanel(new BorderLayout());
        
        JLabel filterTitleLabel = new JLabel(title_filter);
        filterTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        filterPanel.add(filterTitleLabel, BorderLayout.NORTH);

        if (data_filter == null || data_filter.length == 0) {
            JLabel noFilterLabel = new JLabel(text);
            noFilterLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            filterPanel.add(noFilterLabel, BorderLayout.CENTER);
        } else {
            String[] filterColumnNames = {"Поле фильтра", "Свойство фильтра", "Фильтр"};
            Object[][] filterData = new Object[data_filter.length][3];
            
            for (int i = 0; i < data_filter.length; i++) {
                filterData[i][0] = data_filter[i];
                filterData[i][1] = ""; // Пустая строка для ввода
                filterData[i][2] = ""; // Пустая строка для ввода
            }
            
            _filterTable = new JTable(filterData, filterColumnNames);
            _filterTable.getTableHeader().setBackground(Color.cyan);
            _filterTable.setRowHeight(30);
            _filterTable.setFont(new Font("Arial", Font.PLAIN, 14));

            JScrollPane filterScrollPane = new JScrollPane(_filterTable);
            filterScrollPane.setPreferredSize(new Dimension(600, Math.min(200, data_filter.length * 35 + 30)));
            filterPanel.add(filterScrollPane, BorderLayout.CENTER);
        }

        mainPanel.add(sortPanel);
        mainPanel.add(filterPanel);
        add(mainPanel, BorderLayout.CENTER);

        // Панель кнопок
        String[] buttons = new String[]{"Назад", "Показать"};

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        for (String buttonText : buttons) {
            JButton button = new JButton(buttonText);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setPreferredSize(new Dimension(120, 40));
            button.addActionListener(e -> _handleButtonClickFilterAndSortForOrderReport(buttonText));
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        _update();
    }

    private void _handleButtonClickFilterAndSortForOrderReport(String button) {
        switch (button) {
            case "Назад":
                try {
                    _insertTable();
                } catch (Exception e) {
                    _showMessage(e.getMessage());
                }
                break;
            case "Показать":
                String[] sortInputs = collectSortInputs();
                String[] filterInputsFrom = collectFilterInputsFrom();
                String[] filterInputsTo = collectFilterInputsTo();
                
                _showOrderReport(sortInputs, filterInputsFrom, filterInputsTo);
                break;
        }
    }

    private String[] collectSortInputs() {
        if (_sortTable != null) {
            int rowCount = _sortTable.getRowCount();
            String[] inputs = new String[rowCount];
            for (int i = 0; i < rowCount; i++) {
                Object value = _sortTable.getValueAt(i, 1);
                inputs[i] = value != null ? value.toString() : "";
            }
            return inputs;
        }
        return new String[0];
    }

    private String[] collectFilterInputsFrom() {
        return collectFilterInputs(1);
    }

    private String[] collectFilterInputsTo() {
        return collectFilterInputs(2);
    }

    private String[] collectFilterInputs(int columnIndex) {
        if (_filterTable != null) {
            int rowCount = _filterTable.getRowCount();
            String[] inputs = new String[rowCount];
            for (int i = 0; i < rowCount; i++) {
                Object value = _filterTable.getValueAt(i, columnIndex);
                inputs[i] = value != null ? value.toString() : "";
            }
            return inputs;
        }
        return new String[0];
    }

    private void _showOrderReport(String[] sortInputs, String[] filterInputsFrom, String[] filterInputsTo) {
        // System.out.println("Sort inputs: " + Arrays.toString(sortInputs));
        // System.out.println("Filter inputs FROM: " + Arrays.toString(filterInputsFrom));
        // System.out.println("Filter inputs TO: " + Arrays.toString(filterInputsTo));
        
        _order.setSort(sortInputs);
        _order.setFilter(filterInputsFrom, filterInputsTo);
        
        List<Map<String, Object>> data = null;
        try {
            data = _order.get();
        } catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }

        _clear();
        String title = _order.getType();

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        if (data != null && !data.isEmpty()) {
            Set<String> allKeys = data.get(0).keySet();
            List<String> columnNamesList = new ArrayList<>(allKeys);
            
            String[] columnNames = columnNamesList.toArray(new String[0]);
            Object[][] tableData = new Object[data.size()][columnNames.length];
            
            // Заполняем данные таблицы
            for (int row = 0; row < data.size(); row++) {
                Map<String, Object> rowData = data.get(row);
                for (int col = 0; col < columnNames.length; col++) {
                    tableData[row][col] = rowData.get(columnNames[col]);
                }
            }

            JTable table = new JTable(tableData, columnNames);
            table.getTableHeader().setBackground(Color.cyan);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowHeight(30);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setEnabled(false);
            table.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        Object value = table.getValueAt(row, col);
                        table.setToolTipText(value != null ? value.toString() : null);
                    } else {
                        table.setToolTipText(null);
                    }
                }
            });
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
            add(scrollPane, BorderLayout.CENTER);
        } else {
            JLabel noDataLabel = new JLabel("Нет данных для отображения", SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noDataLabel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
            add(noDataLabel, BorderLayout.CENTER);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JButton backButton = new JButton("Назад");
        backButton.setFocusPainted(false);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> _moveOrders());
        
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        _update();
    }



}
