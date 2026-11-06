package restaurant.repository;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import restaurant.common.BillFormatter;
import restaurant.model.Bill;

/** Lưu hóa đơn bằng cách ghi nối tiếp vào một file văn bản UTF-8. */
public class FileBillArchive implements BillArchive {

    private final Path file;

    /**
     * Tạo nơi lưu hóa đơn.
     *
     * @param file file hóa đơn, tự tạo thư mục cha nếu chưa có
     */
    public FileBillArchive(Path file) {
        this.file = file;
    }

    @Override
    public void archive(Bill bill) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(file, BillFormatter.format(bill), UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
