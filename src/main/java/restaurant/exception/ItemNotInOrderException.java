package restaurant.exception;

/** Đơn không có món được yêu cầu sửa hoặc bớt. */
public class ItemNotInOrderException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param itemId mã món
     */
    public ItemNotInOrderException(String itemId) {
        super("Đơn không có món " + itemId);
    }
}
