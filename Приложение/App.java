import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
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

        
        // addWindowListener(new WindowAdapter() {
        //     @Override
        //     public void windowClosing(WindowEvent e) {
        //         _init = false;
        //     }
            
        //     @Override
        //     public void windowClosed(WindowEvent e) {
        //         _init = false;
        //     }
        // });

        _moveMainMenu();

        setVisible(true);
        _init = true;

        _buttonBack.setFocusPainted(false);
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
        
        setLayout(new GridLayout(gridWithPaddingH, gridWithPaddingW, 10, 10));
        
        ActionListener tableButtonHandler = e -> {
            JButton clickedButton = (JButton) e.getSource();
            String tableName = clickedButton.getText();
            _handleTableClickInData(tableName);
        };
        
        double coefH = (1.0 - 0.15 * (h + 1)) / h;
        double coefW = (1.0 - 0.15 * (w + 1)) / w;
        Element el = new Element(coefH, coefW, 1);


        for (int i = 0; i < gridWithPaddingW; i++) {
            add(new JLabel(""));
        }
        
        for (int row = 0; row < h; row++) {
            add(new JLabel(""));
            
            for (int col = 0; col < w; col++) {
                int index = row * w + col;
                if (index < tables.length) {
                    String str = tables[index];
                    JButton button = new JButton(str);
                    button.setFocusPainted(false);
                    button.addActionListener(tableButtonHandler);

                    double font = 0.6 + 0.1 * str.length();
                    el.fontScale = font;
                    add(button, el);
                } else {
                    add(new JLabel(""));
                }
            }
            
            add(new JLabel(""));
        }
        
        _buttonBack.addActionListener(e -> {_moveMainMenu();});
        el.fontScale = 1;
        add(_buttonBack, el);
        for (int i = 1; i < gridWithPaddingW; i++) {
            add(new JLabel(""));
        }

        _update();
    }

    private void _moveOrders() {
        _clear();
    }


    private void _insertTable() throws SQLException {
        _table = _data.getTableName();
        String title = "Таблица: " + _data.getTableName();
        _table_data = _data.getDataTable();
        _ind_table_pressed = -1;
        _clear();

        boolean[] FKs = _data.getFKs();

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
        if (_data.isMain()) {
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
                _handleEnterButton();
                break;
            case "Назад":
                _handleBackButton();
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
            _showMessage("Выберите колонку");
            return;
        }

        _data.setUpdate(_ind_table_pressed);

        String[] fields = _data.getFields();
        String[] enter_data = _data.getEnterData();
        Map<String, Object> rowData = _table_data.get(_ind_table_pressed);  
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

        Map<String, Object> rowData = _table_data.get(_ind_table_pressed);
    
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
        Map<String, Object> rowData = _table_data.get(_ind_table_pressed);  
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
            _moveEditValueTable(_data.getType());
            // _insertTable();
        }
        catch (Exception e) {
            _showMessage(e.getMessage());
            return;
        }


    }







    private void _handleTableClickInData(String tableName) {
        try {
            _data = new Data(tableName);
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

    private void _showMessage() {
        _showMessage("Что-то пошло не так");
    }
}
