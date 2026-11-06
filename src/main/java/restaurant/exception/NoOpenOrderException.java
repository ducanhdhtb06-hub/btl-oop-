package restaurant.exception;

/** Bàn chưa có đơn nào đang mở. */
public class NoOpenOrderException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param tableNo số bàn
     */
    public NoOpenOrderException(int tableNo) {
        super("Bàn " + tableNo + " chưa có đơn đang mở");
    }
}
