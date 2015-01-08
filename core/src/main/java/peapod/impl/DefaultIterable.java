package peapod.impl;

import java.util.Iterator;

/**
 * Created by wisa on 08/01/2015.
 */
public class DefaultIterable<T> implements Iterable<T> {
    private Iterator<T> iterator;

    public DefaultIterable(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }
}
