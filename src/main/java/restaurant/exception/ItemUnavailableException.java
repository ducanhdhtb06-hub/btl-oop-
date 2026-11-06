package restaurant.exception;

/** Món đã hết, không gọi được. */
public class ItemUnavailableException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param itemName tên món đã hết
     */
    public ItemUnavailableException(String itemName) {
        super("Món " + itemName + " đã hết");
    }
}
