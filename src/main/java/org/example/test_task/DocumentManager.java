package org.example.test_task;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for store data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        Document existDocument = storage.get(document.getId());
        if (existDocument == null && document.getCreated() == null) {
            document.setCreated(Instant.now());
        } else if (existDocument != null) {
            document.setCreated(existDocument.getCreated());
        }
        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(document -> matchRequest(document, request))
                .collect(Collectors.toList());
    }

    private boolean matchRequest(Document document, SearchRequest request) {
        if (request == null) return true;

        boolean matchTitle = Optional.ofNullable(request.getTitlePrefixes())
                .orElse(Collections.emptyList())
                .isEmpty() ||
                request.getTitlePrefixes()
                .stream()
                .anyMatch(prefix -> document.getTitle() != null && document.getTitle().startsWith(prefix));

        boolean matchContent = Optional.ofNullable(request.getContainsContents())
                .orElse(Collections.emptyList())
                .isEmpty() ||
                request.getContainsContents()
                .stream()
                .anyMatch(content -> document.getContent() != null && document.getContent().contains(content));

        boolean matchAuthor = Optional.ofNullable(request.getAuthorIds())
                .orElse(Collections.emptyList())
                .isEmpty() ||
                request.getAuthorIds()
                .stream()
                .anyMatch(authorId -> document.getAuthor() != null && authorId.equals(document.getAuthor().getId()));

        boolean matchCreated = (request.getCreatedFrom() == null || !document.getCreated().isBefore(request.getCreatedFrom()))
                && (request.getCreatedTo() == null || !document.getCreated().isAfter(request.getCreatedTo()));

        return matchTitle && matchContent && matchAuthor && matchCreated;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}