
import java.util.List;

public class WAHVector implements BitmapVector {
    private String id;
    private List<Long> words;
    public WAHVector(String id, List<Long> words) {
        this.id = id;
        this.words = words;
    }
    public BitmapVector and(BitmapVector other) {
        return null; // TODO -- get from David/Jason
    }
    public BitmapVector or(BitmapVector other) {
        return null; // TODO -- get from David/Jason
    }
    public String getId() { return id; }
    public BitmapVectorType getType() { return BitmapVectorType.WAH; }

    @Override
    public List<Long> getWords() { return words; }
}
