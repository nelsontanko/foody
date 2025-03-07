package dev.core.config.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.domain.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A serializable wrapper for Spring Data's Page interface.
 * This class allows Page objects to be properly cached in Redis.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SerializablePage<T> implements Page<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> content = new ArrayList<>();
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private int numberOfElements;
    private String sortDirection;
    private String[] sortProperties;

    public SerializablePage() {
    }

    public SerializablePage(Page<T> page) {
        this.content = new ArrayList<>(page.getContent());
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.numberOfElements = page.getNumberOfElements();

        if (page.getSort() != null && page.getSort().isSorted()) {
            Iterator<Sort.Order> iterator = page.getSort().iterator();
            if (iterator.hasNext()) {
                Sort.Order order = iterator.next();
                this.sortDirection = order.getDirection().name();

                List<String> properties = new ArrayList<>();
                page.getSort().forEach(o -> properties.add(o.getProperty()));
                this.sortProperties = properties.toArray(new String[0]);
            }
        }
    }

    // Create a Sort object from the stored sort information
    private Sort getPageSort() {
        if (sortDirection == null || sortProperties == null || sortProperties.length == 0) {
            return Sort.unsorted();
        }

        Sort.Direction direction = Sort.Direction.valueOf(sortDirection);
        return Sort.by(direction, sortProperties);
    }

    @Override
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public long getTotalElements() {
        return totalElements;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getNumberOfElements() {
        return numberOfElements;
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public Sort getSort() {
        return getPageSort();
    }

    @Override
    public boolean isFirst() {
        return first;
    }

    @Override
    public boolean isLast() {
        return last;
    }

    @Override
    public boolean hasNext() {
        return !isLast();
    }

    @Override
    public boolean hasPrevious() {
        return !isFirst();
    }

    @Override
    public Pageable nextPageable() {
        return hasNext() ? PageRequest.of(number + 1, size, getPageSort()) : Pageable.unpaged();
    }

    @Override
    public Pageable previousPageable() {
        return hasPrevious() ? PageRequest.of(number - 1, size, getPageSort()) : Pageable.unpaged();
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> result = new ArrayList<>(content.size());
        for (T element : content) {
            result.add(converter.apply(element));
        }

        SerializablePage<U> page = new SerializablePage<>();
        page.setContent(result);
        page.setNumber(number);
        page.setSize(size);
        page.setTotalElements(totalElements);
        page.setTotalPages(totalPages);
        page.setLast(last);
        page.setFirst(first);
        page.setNumberOfElements(numberOfElements);
        page.setSortDirection(sortDirection);
        page.setSortProperties(sortProperties);

        return page;
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public Pageable getPageable() {
        return PageRequest.of(number, size, getPageSort());
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String[] getSortProperties() {
        return sortProperties;
    }

    public void setSortProperties(String[] sortProperties) {
        this.sortProperties = sortProperties;
    }
}