package restaurant.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Hóa đơn thanh toán, không thay đổi được sau khi tạo.
 *
 * @param orderId mã đơn
 * @param tableNumber số bàn
 * @param issuedAt thời điểm xuất hóa đơn
 * @param lines các dòng món
 * @param subtotal tạm tính
 * @param discountLabel nhãn của cách giảm giá, ví dụ "Giảm 10%"
 * @param discount số tiền được giảm
 * @param total số tiền phải trả
 */
public record Bill(int orderId, int tableNumber, LocalDateTime issuedAt, List<BillLine> lines,
                   long subtotal, String discountLabel, long discount, long total) {

    /** Sao chép danh sách dòng để hóa đơn không bị sửa từ bên ngoài. */
    public Bill {
        lines = List.copyOf(lines);
    }
}
