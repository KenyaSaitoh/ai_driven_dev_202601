package pro.kensait.berrybooks.ui;

import pro.kensait.berrybooks.model.CustomerStats;
import pro.kensait.berrybooks.model.CustomerTO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// 顧客情報編集ダイアログ
public class CustomerEditDialog extends JDialog {
    private final CustomerStats customer;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField birthDateField;
    private JTextField addressField;
    private boolean confirmed = false;

    public CustomerEditDialog(Frame parent, CustomerStats customer) {
        super(parent, "顧客情報編集", true);
        this.customer = customer;
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(500, 350);

        // フォームパネル
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 顧客ID（読み取り専用）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("顧客ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField idField = new JTextField(customer.getCustomerId().toString());
        idField.setEditable(false);
        idField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(idField, gbc);

        // 顧客名
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("顧客名: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = new JTextField(customer.getCustomerName());
        formPanel.add(nameField, gbc);

        // メールアドレス
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("メールアドレス: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        emailField = new JTextField(customer.getEmail());
        formPanel.add(emailField, gbc);

        // 生年月日
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("生年月日:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String birthDateStr = customer.getBirthDate() != null ? customer.getBirthDate().format(formatter) : "";
        birthDateField = new JTextField(birthDateStr);
        formPanel.add(birthDateField, gbc);

        // 生年月日の形式説明
        gbc.gridx = 1;
        gbc.gridy = 4;
        JLabel dateFormatLabel = new JLabel("(形式: yyyy-MM-dd)");
        dateFormatLabel.setFont(new Font(dateFormatLabel.getFont().getName(), Font.ITALIC, 10));
        dateFormatLabel.setForeground(Color.GRAY);
        formPanel.add(dateFormatLabel, gbc);

        // 住所
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("住所: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        addressField = new JTextField(customer.getAddress());
        formPanel.add(addressField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton okButton = new JButton("更新");
        okButton.addActionListener(e -> onOk());

        JButton cancelButton = new JButton("キャンセル");
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onOk() {
        // バリデーション
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String birthDateStr = birthDateField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "顧客名を入力してください。", 
                "入力エラー", 
                JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "メールアドレスを入力してください。", 
                "入力エラー", 
                JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return;
        }

        // メールアドレス形式チェック
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, 
                "有効なメールアドレスを入力してください。", 
                "入力エラー", 
                JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return;
        }

        // 生年月日の形式チェック（空でない場合のみ）
        if (!birthDateStr.isEmpty()) {
            try {
                LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, 
                    "生年月日はyyyy-MM-dd形式で入力してください。\n例: 1990-01-15", 
                    "入力エラー", 
                    JOptionPane.ERROR_MESSAGE);
                birthDateField.requestFocus();
                return;
            }
        }

        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "住所を入力してください。", 
                "入力エラー", 
                JOptionPane.ERROR_MESSAGE);
            addressField.requestFocus();
            return;
        }

        // 顧客情報を更新
        customer.setCustomerName(name);
        customer.setEmail(email);
        customer.setBirthDate(birthDateStr.isEmpty() ? null : LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE));
        customer.setAddress(address);

        confirmed = true;
        dispose();
    }

    private void onCancel() {
        confirmed = false;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public CustomerTO getCustomerTO() {
        return new CustomerTO(
            customer.getCustomerName(),
            customer.getEmail(),
            customer.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
            customer.getAddress()
        );
    }
}
