package restaurant.exception;

/** Số lượng một dòng món nằm ngoài khoảng 1 đến 99. */
public class InvalidQuantityException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param quantity số lượng không hợp lệ
     */
    public InvalidQuantityException(int quantity) {
        super("Số lượng phải từ 1 đến 99 (nhận được " + quantity + ")");
    }
}
