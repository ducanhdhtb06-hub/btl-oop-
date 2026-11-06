package restaurant.ui;

/** Báo rằng luồng nhập đã hết dữ liệu (ví dụ Ctrl+D hoặc hết file kịch bản), để app thoát gọn. */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Tạo lỗi hết dữ liệu nhập. */
    public InputClosedException() {
        super("Hết dữ liệu nhập");
    }
}
