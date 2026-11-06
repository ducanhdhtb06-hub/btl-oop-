package restaurant.repository;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import restaurant.model.MenuItem;
import restaurant.model.MenuItemFactory;

/**
 * Lưu thực đơn trong file CSV (UTF-8, phân cách bằng {@code ;}). Đọc cả file khi khởi tạo,
 * ghi lại cả file mỗi lần lưu. Dòng lỗi bị bỏ qua khi đọc và được ghi vào danh sách cảnh báo,
 * nhưng vẫn được giữ nguyên trong file khi lưu, để người dùng sửa lại. Nếu không đọc được file
 * (ví dụ không phải UTF-8) thì không bao giờ ghi đè file đó.
 */
public class FileMenuRepository implements Repository<MenuItem, String> {

    /** Dòng tiêu đề của file. */
    public static final String HEADER = "type;id;name;value;available;detail";

    private final Path file;
    private final Map<String, MenuItem> items = new LinkedHashMap<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> skippedRows = new ArrayList<>();
    private boolean writable = true;

    /**
     * Mở thực đơn từ file. Thiếu file thì tạo thư mục và thực đơn mẫu.
     * Không đọc được file thì dùng thực đơn mẫu và không ghi đè file.
     *
     * @param file đường dẫn file menu.csv
     */
    public FileMenuRepository(Path file) {
        this.file = file;
        if (Files.notExists(file)) {
            SampleMenu.items().forEach(item -> items.put(item.getId(), item));
            try {
                writeAll();
            } catch (IOException e) {
                warnings.add("Không ghi được " + file + ": " + e.getMessage());
            }
            return;
        }
        try {
            load();
        } catch (IOException e) {
            items.clear();
            skippedRows.clear();
            writable = false;
            warnings.add("Không đọc được " + file + " (file phải được lưu dạng UTF-8): " + e.getMessage()
                    + ". Đang dùng thực đơn mẫu; mọi thay đổi sẽ không được lưu cho tới khi sửa file.");
            SampleMenu.items().forEach(item -> items.put(item.getId(), item));
        }
    }

    /**
     * Trả về các cảnh báo khi đọc file, mỗi cảnh báo bắt đầu bằng "Dòng N: ".
     *
     * @return danh sách cảnh báo chỉ đọc
     */
    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    @Override
    public List<MenuItem> findAll() {
        return List.copyOf(items.values());
    }

    @Override
    public Optional<MenuItem> findById(String id) {
        return Optional.ofNullable(items.get(id));
    }

    /**
     * Lưu món rồi ghi lại cả file.
     *
     * @param item món cần lưu
     * @throws UncheckedIOException nếu không ghi được file, hoặc file gốc chưa đọc được nên không được ghi đè
     */
    @Override
    public void save(MenuItem item) {
        items.put(item.getId(), item);
        if (!writable) {
            throw new UncheckedIOException("Không lưu vào " + file + " vì chưa đọc được file này; hãy lưu file dạng UTF-8",
                    new IOException("file gốc chưa đọc được"));
        }
        try {
            writeAll();
        } catch (IOException e) {
            throw new UncheckedIOException("Không ghi được thực đơn vào " + file, e);
        }
    }

    private void load() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
            String row;
            int lineNo = 0;
            while ((row = reader.readLine()) != null) {
                lineNo++;
                if (lineNo == 1 && row.startsWith("\uFEFF")) {
                    row = row.substring(1);
                }
                if (row.isBlank() || (lineNo == 1 && row.startsWith("type;"))) {
                    continue;
                }
                try {
                    MenuItem item = MenuItemFactory.fromCsvRow(row, id -> Optional.ofNullable(items.get(id)));
                    if (items.containsKey(item.getId())) {
                        warnings.add("Dòng " + lineNo + ": trùng mã " + item.getId() + " với dòng phía trên, bỏ qua");
                        skippedRows.add(row);
                    } else {
                        items.put(item.getId(), item);
                    }
                } catch (IllegalArgumentException e) {
                    warnings.add("Dòng " + lineNo + ": " + e.getMessage() + ", bỏ qua");
                    skippedRows.add(row);
                }
            }
        }
    }

    private void writeAll() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (MenuItem item : items.values()) {
                writer.write(item.toCsvRow());
                writer.newLine();
            }
            for (String row : skippedRows) {
                writer.write(row);
                writer.newLine();
            }
        }
    }
}
