package restaurant.exception;

/** Không có món nào mang mã được yêu cầu. */
public class MenuItemNotFoundException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param itemId mã món được tìm
     */
    public MenuItemNotFoundException(String itemId) {
        super("Không tìm thấy món " + itemId);
    }
}
