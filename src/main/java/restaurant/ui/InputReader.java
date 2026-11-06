package restaurant.ui;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

/**
 * Đọc và kiểm tra dữ liệu người dùng nhập trên console. Nhập sai thì báo và hỏi lại;
 * hết dữ liệu nhập thì báo {@link InputClosedException}.
 */
public class InputReader {

    private final Scanner in;
    private final PrintStream out;

    /**
     * Tạo bộ đọc.
     *
     * @param in nguồn dữ liệu nhập (truyền vào để test được)
     * @param out nơi in câu hỏi và thông báo lỗi
     */
    public InputReader(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Đọc một số nguyên trong khoảng cho trước.
     *
     * @param prompt câu hỏi
     * @param min giá trị nhỏ nhất
     * @param max giá trị lớn nhất
     * @return số người dùng nhập
     * @throws InputClosedException nếu hết dữ liệu nhập
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                out.println("Vui lòng nhập số từ " + min + " đến " + max);
            } catch (NumberFormatException e) {
                out.println("Vui lòng nhập một số");
            }
        }
    }

    /**
     * Đọc một chuỗi khác rỗng, đã bỏ khoảng trắng hai đầu.
     *
     * @param prompt câu hỏi
     * @return chuỗi người dùng nhập
     * @throws InputClosedException nếu hết dữ liệu nhập
     */
    public String readNonBlank(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            out.println("Không được để trống");
        }
    }

    /**
     * Hỏi xác nhận. Nhận "c"/"có" là đồng ý, "k"/"không" là không, không phân biệt hoa thường.
     *
     * @param prompt câu hỏi
     * @return {@code true} nếu người dùng đồng ý
     * @throws InputClosedException nếu hết dữ liệu nhập
     */
    public boolean confirm(String prompt) {
        while (true) {
            String answer = readLine(prompt).toLowerCase(Locale.ROOT);
            if (answer.equals("c") || answer.equals("có")) {
                return true;
            }
            if (answer.equals("k") || answer.equals("không")) {
                return false;
            }
            out.println("Vui lòng nhập c hoặc k");
        }
    }

    private String readLine(String prompt) {
        out.print(prompt);
        if (!in.hasNextLine()) {
            throw new InputClosedException();
        }
        return in.nextLine().trim();
    }
}
