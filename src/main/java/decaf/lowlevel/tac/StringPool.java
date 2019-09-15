package decaf.lowlevel.tac;

import java.util.Iterator;
import java.util.Vector;

/**
 * A string pool to store string literals. The equal strings will be allocated a same index.
 */
public class StringPool implements Iterable<String> {
    /**
     * Add a string.
     *
     * @param value the string
     * @return the allocated index
     */
    public int add(String value) {
        int index = _strings.indexOf(value);
        if (index == -1) {
            _strings.add(value);
            return _strings.size() - 1;
        }
        return index;
    }

    /**
     * Get a string by index.
     *
     * @param index the index
     * @return the string
     */
    public String get(int index) {
        return _strings.get(index);
    }

    public int find(String value) {
        for (var i = 0; i < _strings.size(); i++) {
            if (_strings.get(i).equals(value)) {
                return i;
            }
        }

        throw new IllegalArgumentException(value + " not found in string pool");
    }

    private Vector<String> _strings = new Vector<>();

    @Override
    public Iterator<String> iterator() {
        return _strings.iterator();
    }
}