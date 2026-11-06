package restaurant.repository;

import java.io.IOException;
import restaurant.model.Bill;

/**
 * Nơi lưu hóa đơn đã thanh toán. {@code OrderService} chỉ biết interface này, không biết hóa đơn
 * được ghi ra file hay đi đâu (nguyên tắc D), nên test có thể truyền vào một bản giả.
 */
@FunctionalInterface
public interface BillArchive {

    /**
     * Lưu một hóa đơn.
     *
     * @param bill hóa đơn cần lưu
     * @throws IOException nếu không lưu được
     */
    void archive(Bill bill) throws IOException;
}
