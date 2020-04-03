import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHasher {
    private TreeMap<Integer, SlaveNode> map;
    public ConsistentHasher() {
        map = new TreeMap<>();
    }

    public void put(SlaveNode node) {
        map.put(node.getId().hashCode(), node);
    }

    public void delete(SlaveNode node) throws IllegalArgumentException {
        if (node == null || map.get(node.getId().hashCode()) == null) {
            throw new IllegalArgumentException("Node not contained in map");
        }
        map.remove(node.getId().hashCode());
    }

    public List<SlaveNode> consistentHash(String objectID, int successors) {
        final List<SlaveNode> inodes = List.of(successor(objectID));
        for (int i = 0; i < successors; i++) {
            inodes.add(successor(inodes.get(i - 1).getId()));
        }
        return inodes;
    }

    public SlaveNode successor(String id) {
        Map.Entry<Integer, SlaveNode> entry = map.higherEntry(id.hashCode());
        if (entry == null) {
            return map.firstEntry().getValue();
        }
        return entry.getValue();
    }
}
