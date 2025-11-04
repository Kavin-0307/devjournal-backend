package com.example.demo.entity;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class EntryTagId implements Serializable {

    private Long journalEntryId;
    private Long tagId;

    public EntryTagId() {}

    public EntryTagId(Long journalEntryId, Long tagId) {
        this.journalEntryId = journalEntryId;
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntryTagId)) return false;
        EntryTagId that = (EntryTagId) o;
        return Objects.equals(journalEntryId, that.journalEntryId) &&
               Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journalEntryId, tagId);
    }
}
