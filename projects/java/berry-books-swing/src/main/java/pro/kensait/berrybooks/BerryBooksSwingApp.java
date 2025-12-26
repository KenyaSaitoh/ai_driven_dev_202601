package pro.kensait.berrybooks;

import pro.kensait.berrybooks.api.BerryBooksApiClient;
import pro.kensait.berrybooks.model.CustomerStats;
import pro.kensait.berrybooks.model.CustomerTO;
import pro.kensait.berrybooks.ui.CustomerEditDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Berry Books 管理者画面（Swingアプリケーション）
public class BerryBooksSwingApp extends JFrame {
    private final BerryBooksApiClient apiClient;
    private DefaultTableModel tableModel;
    private JTable customerTable;

    public BerryBooksSwingApp(String apiUrl) {
        super("Berry Books 管理者画面");
        this.apiClient = new BerryBooksApiClient(apiUrl);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // タイトルパネル
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JLabel titleLabel = new JLabel("顧客一覧", SwingConstants.CENTER);
        titleLabel.setFont(new Font("MS Gothic", Font.BOLD, 24));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // リフレッシュボタン
        JButton refreshButton = new JButton("更新");
        refreshButton.addActionListener(e -> loadCustomers());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);
        titlePanel.add(refreshPanel, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // テーブル
        String[] columnNames = {"顧客ID", "顧客名", "メールアドレス", "生年月日", "住所", "注文件数", "購入冊数", "操作"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // 操作列のみ編集可能
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(35);
        customerTable.setFont(new Font("MS Gothic", Font.PLAIN, 14));
        customerTable.getTableHeader().setFont(new Font("MS Gothic", Font.BOLD, 14));
        customerTable.getTableHeader().setReorderingAllowed(false);
        
        // 罫線の表示設定
        customerTable.setShowGrid(true);
        customerTable.setGridColor(new Color(200, 200, 200));
        customerTable.setIntercellSpacing(new Dimension(1, 1));

        // 列幅設定
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 顧客ID
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 顧客名
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(200); // メール
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 生年月日
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(300); // 住所
        customerTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 注文件数
        customerTable.getColumnModel().getColumn(6).setPreferredWidth(100); // 購入冊数
        customerTable.getColumnModel().getColumn(7).setPreferredWidth(100); // 操作

        // 中央揃え（数値列）
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBorder(BorderFactory.createEmptyBorder()); // 罫線を維持
        customerTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        customerTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        customerTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        customerTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // 操作列にボタンを設定
        customerTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        customerTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        scrollPane.setColumnHeaderView(customerTable.getTableHeader());
        add(scrollPane, BorderLayout.CENTER);

        loadCustomers();
    }

    private void loadCustomers() {
        new Thread(() -> {
            try {
                List<CustomerStats> customers = apiClient.fetchCustomerStats();
                SwingUtilities.invokeLater(() -> updateTable(customers));
            } catch (IOException | InterruptedException ex) {
                SwingUtilities.invokeLater(() -> 
                     JOptionPane.showMessageDialog(
                         BerryBooksSwingApp.this,
                         "顧客情報の取得に失敗しました:\n" + ex.getMessage(),
                         "エラー",
                         JOptionPane.ERROR_MESSAGE
                     )
                );
            }
        }).start();
    }

    private void updateTable(List<CustomerStats> customers) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        for (CustomerStats customer : customers) {
            Object[] row = new Object[]{
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getBirthDate() != null ? customer.getBirthDate().format(formatter) : "",
                customer.getAddress(),
                customer.getOrderCount(),
                customer.getBookCount(),
                "編集"
            };
            tableModel.addRow(row);
        }
    }

    private void editCustomer(int row) {
        Long customerId = (Long) tableModel.getValueAt(row, 0);
        String customerName = (String) tableModel.getValueAt(row, 1);
        String email = (String) tableModel.getValueAt(row, 2);
        String birthDateStr = (String) tableModel.getValueAt(row, 3);
        String address = (String) tableModel.getValueAt(row, 4);
        Long orderCount = (Long) tableModel.getValueAt(row, 5);
        Long bookCount = (Long) tableModel.getValueAt(row, 6);

        CustomerStats customer = new CustomerStats();
        customer.setCustomerId(customerId);
        customer.setCustomerName(customerName);
        customer.setEmail(email);
        // 誕生日が空でない場合のみパース
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            customer.setBirthDate(java.time.LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        }
        customer.setAddress(address);
        customer.setOrderCount(orderCount);
        customer.setBookCount(bookCount);

        CustomerEditDialog dialog = new CustomerEditDialog(this, customer);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            // APIで更新
            String birthDateForUpdate = customer.getBirthDate() != null 
                ? customer.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE) 
                : null;
            CustomerTO customerTO = new CustomerTO(
                customer.getCustomerName(),
                customer.getEmail(),
                birthDateForUpdate,
                customer.getAddress()
            );

            new Thread(() -> {
                try {
                    apiClient.updateCustomer(customerId, customerTO);
                    SwingUtilities.invokeLater(() -> {
                         JOptionPane.showMessageDialog(
                             BerryBooksSwingApp.this,
                             "顧客情報を更新しました。",
                             "成功",
                             JOptionPane.INFORMATION_MESSAGE
                         );
                         loadCustomers();
                    });
                } catch (IOException | InterruptedException ex) {
                    SwingUtilities.invokeLater(() -> 
                         JOptionPane.showMessageDialog(
                             BerryBooksSwingApp.this,
                             "顧客情報の更新に失敗しました:\n" + ex.getMessage(),
                             "エラー",
                             JOptionPane.ERROR_MESSAGE
                         )
                    );
                }
            }).start();
        }
    }

    // ボタンレンダラー
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "編集" : value.toString());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            return this;
        }
    }

    // ボタンエディター
    class ButtonEditor extends javax.swing.DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int editingRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "編集" : value.toString();
            button.setText(label);
            editingRow = row;
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                editCustomer(editingRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        final String apiUrl = (args.length > 0) ? args[0] : "http://localhost:8080/berry-books-rest";
        SwingUtilities.invokeLater(() -> {
            BerryBooksSwingApp app = new BerryBooksSwingApp(apiUrl);
            app.setVisible(true);
        });
    }
}
