package restaurant.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.ParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import restaurant.common.BillFormatter;
import restaurant.common.MoneyFormat;
import restaurant.exception.NoOpenOrderException;
import restaurant.exception.RestaurantException;
import restaurant.model.MenuItem;
import restaurant.model.Order;
import restaurant.model.OrderItem;
import restaurant.model.Table;
import restaurant.pricing.DiscountStrategy;
import restaurant.pricing.FixedAmountDiscount;
import restaurant.pricing.NoDiscount;
import restaurant.pricing.PercentageDiscount;
import restaurant.service.CheckoutResult;
import restaurant.service.MenuService;
import restaurant.service.OrderService;
import restaurant.service.TableService;

/**
 * Giao diện Swing. Dùng chung các service với giao diện console, không chứa logic nghiệp vụ.
 * Lớp giữ một {@link JFrame} bên trong thay vì kế thừa {@code JFrame} (ưu tiên composition hơn kế thừa).
 */
public final class MainWindow {

    private static final String[] DISCOUNTS = {"Không giảm", "Giảm %", "Giảm tiền"};

    private final MenuService menu;
    private final TableService tables;
    private final OrderService orders;

    private final JFrame frame = new JFrame("Nhà hàng - Hệ thống gọi món");
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[] {"Bàn", "Số ghế", "Trạng thái"}, 0);
    private final DefaultTableModel menuModel = new DefaultTableModel(new Object[] {"Mã", "Món", "Giá", "Còn"}, 0);
    private final DefaultTableModel orderModel =
            new DefaultTableModel(new Object[] {"Mã", "Món", "SL", "Đơn giá", "Thành tiền"}, 0);
    private final JTable tableView = readOnlyTable(tableModel);
    private final JTable menuView = readOnlyTable(menuModel);
    private final JTable orderView = readOnlyTable(orderModel);
    private final JLabel orderTitle = new JLabel("Chọn một bàn ở bảng bên trái");
    private final JLabel subtotalLabel = new JLabel("Tạm tính: 0 đ");
    private final JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JComboBox<String> discountKind = new JComboBox<>(DISCOUNTS);
    private final JTextField discountValue = new JTextField("0", 8);

    /** Một thao tác của người dùng có thể gây lỗi nghiệp vụ. */
    @FunctionalInterface
    private interface UiAction {
        void run() throws RestaurantException;
    }

    /**
     * Tạo cửa sổ (chưa hiện).
     *
     * @param menu nghiệp vụ thực đơn
     * @param tables nghiệp vụ bàn
     * @param orders nghiệp vụ đơn
     */
    public MainWindow(MenuService menu, TableService tables, OrderService orders) {
        this.menu = menu;
        this.tables = tables;
        this.orders = orders;
        buildLayout();
        refresh();
    }

    /** Hiện cửa sổ. Gọi trên luồng giao diện của Swing. */
    public void show() {
        frame.setVisible(true);
    }

    /**
     * Hiện các cảnh báo (ví dụ khi đọc file thực đơn) trong một hộp thoại. Không làm gì nếu danh sách rỗng.
     *
     * @param warnings các dòng cảnh báo
     */
    public void showWarnings(List<String> warnings) {
        if (!warnings.isEmpty()) {
            JOptionPane.showMessageDialog(frame, String.join("\n", warnings), "Cảnh báo khi đọc thực đơn",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void buildLayout() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        tableView.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshOrder();
            }
        });
        menuView.getColumnModel().getColumn(1).setPreferredWidth(380);

        frame.add(titled(new JScrollPane(tableView), "Bàn", 230), BorderLayout.WEST);
        frame.add(titled(new JScrollPane(menuView), "Thực đơn", 560), BorderLayout.CENTER);

        JPanel orderPanel = new JPanel(new BorderLayout(4, 4));
        orderPanel.add(orderTitle, BorderLayout.NORTH);
        orderPanel.add(new JScrollPane(orderView), BorderLayout.CENTER);
        subtotalLabel.setFont(subtotalLabel.getFont().deriveFont(Font.BOLD, 14f));
        orderPanel.add(subtotalLabel, BorderLayout.SOUTH);
        frame.add(titled(orderPanel, "Đơn của bàn đang chọn", 420), BorderLayout.EAST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.add(button("Mở bàn", () -> orders.openOrder(selectedTable())));
        actions.add(new JLabel("Số lượng:"));
        actions.add(quantity);
        actions.add(button("Gọi món", () ->
                orders.addItem(selectedTable(), selectedMenuItem(), selectedQuantity())));
        actions.add(button("Bớt món", () -> orders.removeItem(selectedTable(), selectedOrderItem())));
        actions.add(new JLabel("   Giảm giá:"));
        actions.add(discountKind);
        actions.add(discountValue);
        actions.add(button("Thanh toán", this::checkout));
        actions.add(button("Hủy đơn", this::cancelOrder));
        frame.add(actions, BorderLayout.SOUTH);

        frame.setSize(1240, 560);
        frame.setLocationRelativeTo(null);
    }

    private void checkout() throws RestaurantException {
        int tableNo = selectedTable();
        CheckoutResult result = orders.checkout(tableNo, selectedDiscount());
        refresh();
        JTextArea text = new JTextArea(BillFormatter.format(result.bill()));
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        text.setColumns(46);
        JOptionPane.showMessageDialog(frame, new JScrollPane(text), "Hóa đơn bàn " + tableNo,
                JOptionPane.INFORMATION_MESSAGE);
        if (!result.archived()) {
            JOptionPane.showMessageDialog(frame, "Chưa lưu được hóa đơn vào file, thanh toán vẫn được ghi nhận",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelOrder() throws RestaurantException {
        int tableNo = selectedTable();
        orders.currentOrder(tableNo);
        int answer = JOptionPane.showConfirmDialog(frame, "Hủy đơn của bàn " + tableNo + "?", "Hủy đơn",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            orders.cancel(tableNo);
        }
    }

    private DiscountStrategy selectedDiscount() {
        String text = discountValue.getText().trim();
        try {
            return switch (discountKind.getSelectedIndex()) {
                case 1 -> new PercentageDiscount(Integer.parseInt(text));
                case 2 -> new FixedAmountDiscount(Long.parseLong(text));
                default -> new NoDiscount();
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị giảm phải là một số", e);
        }
    }

    private int selectedTable() {
        int row = tableView.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Hãy chọn một bàn ở bảng bên trái");
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private String selectedMenuItem() {
        int row = menuView.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Hãy chọn một món trong thực đơn");
        }
        return (String) menuModel.getValueAt(row, 0);
    }

    private int selectedQuantity() {
        try {
            quantity.commitEdit();
        } catch (ParseException e) {
            ((JSpinner.DefaultEditor) quantity.getEditor()).getTextField().setValue(quantity.getValue());
            throw new IllegalArgumentException("Số lượng phải là số từ 1 đến 99", e);
        }
        return (Integer) quantity.getValue();
    }

    private String selectedOrderItem() {
        int row = orderView.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Hãy chọn một món trong đơn");
        }
        return (String) orderModel.getValueAt(row, 0);
    }

    private JButton button(String label, UiAction action) {
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            try {
                action.run();
            } catch (RestaurantException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            refresh();
        });
        return button;
    }

    private void refresh() {
        int selected = tableView.getSelectedRow();
        String selectedMenuId = selectedId(menuView, menuModel);
        tableModel.setRowCount(0);
        for (Table table : tables.all()) {
            tableModel.addRow(new Object[] {table.getNumber(), table.getSeats(), table.isFree() ? "Trống" : "Có khách"});
        }
        if (selected >= 0 && selected < tableModel.getRowCount()) {
            tableView.setRowSelectionInterval(selected, selected);
        }
        menuModel.setRowCount(0);
        for (MenuItem item : menu.all()) {
            menuModel.addRow(new Object[] {item.getId(), item.describe(), MoneyFormat.format(item.getPrice()),
                item.isAvailable() ? "Còn" : "Hết"});
        }
        reselect(menuView, menuModel, selectedMenuId);
        refreshOrder();
    }

    private void refreshOrder() {
        String selectedLineId = selectedId(orderView, orderModel);
        orderModel.setRowCount(0);
        int row = tableView.getSelectedRow();
        if (row < 0) {
            orderTitle.setText("Chọn một bàn ở bảng bên trái");
            subtotalLabel.setText("Tạm tính: 0 đ");
            return;
        }
        int tableNo = (Integer) tableModel.getValueAt(row, 0);
        try {
            Order order = orders.currentOrder(tableNo);
            orderTitle.setText("Đơn #" + order.getId() + " - Bàn " + tableNo);
            List<OrderItem> lines = order.getItems();
            for (OrderItem line : lines) {
                orderModel.addRow(new Object[] {line.getItem().getId(), line.getItem().getName(), line.getQuantity(),
                    MoneyFormat.format(line.getUnitPrice()), MoneyFormat.format(line.lineTotal())});
            }
            subtotalLabel.setText("Tạm tính: " + MoneyFormat.format(order.subtotal()));
            reselect(orderView, orderModel, selectedLineId);
        } catch (NoOpenOrderException e) {
            orderTitle.setText("Bàn " + tableNo + " chưa có đơn. Bấm Mở bàn để bắt đầu.");
            subtotalLabel.setText("Tạm tính: 0 đ");
        } catch (RestaurantException e) {
            orderTitle.setText(e.getMessage());
        }
    }

    /** Mã ở cột đầu của dòng đang chọn, hoặc {@code null} nếu chưa chọn dòng nào. */
    private static String selectedId(JTable view, DefaultTableModel model) {
        int row = view.getSelectedRow();
        return row < 0 ? null : (String) model.getValueAt(row, 0);
    }

    /** Chọn lại dòng có mã cho trước sau khi bảng được dựng lại, để người dùng không phải chọn lại. */
    private static void reselect(JTable view, DefaultTableModel model, String id) {
        if (id == null) {
            return;
        }
        for (int row = 0; row < model.getRowCount(); row++) {
            if (id.equals(model.getValueAt(row, 0))) {
                view.setRowSelectionInterval(row, row);
                return;
            }
        }
    }

    private static JTable readOnlyTable(DefaultTableModel model) {
        JTable view = new JTable(model);
        view.setDefaultEditor(Object.class, null);
        view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        view.getTableHeader().setReorderingAllowed(false);
        view.setRowHeight(22);
        return view;
    }

    private static JPanel titled(java.awt.Component content, String title, int width) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(content, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(width, 0));
        return panel;
    }
}
