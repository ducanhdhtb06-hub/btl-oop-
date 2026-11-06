package restaurant.exception;

/**
 * Lớp gốc của mọi lỗi nghiệp vụ trong nhà hàng (checked exception).
 * Giao diện bắt lớp này để in thông báo tiếng Việt rồi quay lại menu.
 */
public class RestaurantException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi nghiệp vụ.
     *
     * @param message thông báo tiếng Việt cho người dùng
     */
    public RestaurantException(String message) {
        super(message);
    }
}
