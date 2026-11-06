package restaurant.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.exception.InvalidQuantityException;
import restaurant.exception.ItemNotInOrderException;
import restaurant.exception.ItemUnavailableException;
import restaurant.exception.OrderClosedException;
import restaurant.exception.RestaurantException;

class OrderTest {

    private final Food pho = new Food("F01", "Phở bò", 55_000, true, false);
    private final Drink tra = new Drink("D01", "Trà đá", 5_000, true, 300);
    private final Drink traDao = new Drink("D04", "Trà đào", 35_000, false, 500);
    private final Order order = new Order(1, new Table(3, 4), LocalDateTime.of(2026, 11, 20, 12, 0));

    @Test
    @DisplayName("Thêm món thì đơn tự tạo dòng món và tính tạm tính")
    void addItemsCreatesLines() throws RestaurantException {
        order.addItem(pho, 2);
        order.addItem(tra);
        assertEquals(2, order.getItems().size());
        assertEquals(115_000, order.subtotal());
        assertEquals(110_000, order.getItems().get(0).lineTotal());
        assertFalse(order.isEmpty());
        assertEquals(OrderStatus.OPEN, order.getStatus());
    }

    @Test
    @DisplayName("Gọi lại món đã có thì cộng dồn số lượng, không tạo dòng mới")
    void addSameItemAccumulates() throws RestaurantException {
        order.addItem(pho, 2);
        order.addItem(tra);
        order.addItem(pho, 3);
        assertEquals(5, order.getItems().get(0).getQuantity());
        assertEquals(2, order.getItems().size());
    }

    @Test
    @DisplayName("Số lượng mỗi dòng chỉ từ 1 đến 99, kể cả khi cộng dồn")
    void quantityLimits() throws RestaurantException {
        order.addItem(pho, 5);
        assertThrows(InvalidQuantityException.class, () -> order.addItem(tra, 0));
        assertThrows(InvalidQuantityException.class, () -> order.addItem(tra, 100));
        assertThrows(InvalidQuantityException.class, () -> order.addItem(pho, 95));
        assertEquals(5, order.getItems().get(0).getQuantity());
        assertThrows(InvalidQuantityException.class, () -> order.updateQuantity("F01", 0));
    }

    @Test
    @DisplayName("Không gọi được món đã hết")
    void rejectsUnavailableItem() {
        assertThrows(ItemUnavailableException.class, () -> order.addItem(traDao, 1));
        assertTrue(order.isEmpty());
    }

    @Test
    @DisplayName("Sửa số lượng và bớt món")
    void updateAndRemove() throws RestaurantException {
        order.addItem(pho, 5);
        order.addItem(tra);
        order.updateQuantity("D01", 4);
        assertEquals(4, order.getItems().get(1).getQuantity());
        order.removeItem("D01");
        assertEquals(1, order.getItems().size());
        assertThrows(ItemNotInOrderException.class, () -> order.removeItem("D99"));
        assertThrows(ItemNotInOrderException.class, () -> order.updateQuantity("D99", 1));
    }

    @Test
    @DisplayName("Danh sách món lấy ra chỉ đọc: bên ngoài không thêm hay xóa được (composition)")
    void itemsAreReadOnly() throws RestaurantException {
        order.addItem(pho, 1);
        assertThrows(UnsupportedOperationException.class, () -> order.getItems().clear());
    }

    @Test
    @DisplayName("Đơn giá được chốt lúc gọi món, đổi giá thực đơn sau đó không ảnh hưởng")
    void unitPriceIsCapturedWhenOrdered() throws RestaurantException {
        order.addItem(pho, 2);
        pho.changePrice(70_000);
        assertEquals(55_000, order.getItems().get(0).getUnitPrice());
        assertEquals(110_000, order.subtotal());
    }

    @Test
    @DisplayName("Đơn đã thanh toán thì bị khóa")
    void paidOrderIsClosed() throws RestaurantException {
        order.addItem(pho, 1);
        order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertThrows(OrderClosedException.class, () -> order.addItem(tra));
        assertThrows(OrderClosedException.class, () -> order.removeItem("F01"));
        assertThrows(OrderClosedException.class, order::cancel);
        assertThrows(OrderClosedException.class, order::markPaid);
    }

    @Test
    @DisplayName("Đơn đã hủy thì bị khóa")
    void cancelledOrderIsClosed() throws RestaurantException {
        order.addItem(pho, 1);
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertThrows(OrderClosedException.class, () -> order.updateQuantity("F01", 1));
        assertThrows(OrderClosedException.class, order::markPaid);
    }
}
