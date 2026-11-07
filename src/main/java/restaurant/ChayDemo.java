package restaurant;

/**
 * Chạy console tự động theo kịch bản scripts/demo-input.txt.
 * Mở file này trong VS Code rồi bấm nút Run (▶) để chạy.
 */
public final class ChayDemo {

    private ChayDemo() {
    }

    /**
     * Chạy ứng dụng.
     *
     * @param args không dùng
     * @throws java.io.IOException nếu không đọc được file kịch bản demo
     */
    public static void main(String[] args) throws java.io.IOException {
        Main.main(new String[] {"--demo"});
    }
}
