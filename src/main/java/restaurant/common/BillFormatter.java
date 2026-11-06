package restaurant.common;

import java.time.format.DateTimeFormatter;
import restaurant.model.Bill;
import restaurant.model.BillLine;

/**
 * Biến hóa đơn thành văn bản. Console, Swing và file lưu hóa đơn cùng dùng lớp này,
 * nên mẫu hóa đơn chỉ được viết ở một chỗ (DRY).
 */
public final class BillFormatter {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private BillFormatter() {
    }

    /**
     * Định dạng hóa đơn.
     *
     * @param bill hóa đơn
     * @return văn bản nhiều dòng, kết thúc bằng ký tự xuống dòng
     */
    public static String format(Bill bill) {
        StringBuilder text = new StringBuilder();
        text.append("========== HÓA ĐƠN ==========\n");
        text.append("Đơn #").append(bill.orderId()).append(" - Bàn ").append(bill.tableNumber())
                .append(" - ").append(TIME.format(bill.issuedAt())).append('\n');
        for (BillLine line : bill.lines()) {
            text.append(line.itemName()).append(" x").append(line.quantity())
                    .append(" @ ").append(MoneyFormat.format(line.unitPrice()))
                    .append(" = ").append(MoneyFormat.format(line.lineTotal())).append('\n');
        }
        text.append("Tạm tính: ").append(MoneyFormat.format(bill.subtotal())).append('\n');
        text.append(bill.discountLabel()).append(": ").append(MoneyFormat.format(-bill.discount())).append('\n');
        text.append("TỔNG CỘNG: ").append(MoneyFormat.format(bill.total())).append('\n');
        text.append("=============================\n");
        return text.toString();
    }
}
