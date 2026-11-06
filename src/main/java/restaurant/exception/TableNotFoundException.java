package restaurant.exception;

/** Nhà hàng không có bàn mang số được yêu cầu. */
public class TableNotFoundException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param tableNo số bàn được tìm
     */
    public TableNotFoundException(int tableNo) {
        super("Không có bàn số " + tableNo);
    }
}
