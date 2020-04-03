import java.util.List;

public interface BitmapVector {
    public BitmapVector and(BitmapVector other);
    public BitmapVector or(BitmapVector other);
    public String getId();
    public BitmapVectorType getType();
    public List<Long> getWords();
}
