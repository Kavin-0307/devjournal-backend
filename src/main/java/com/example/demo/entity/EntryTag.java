package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "entry_tags")
public class EntryTag {

    @EmbeddedId
    private EntryTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("journalEntryId") // maps composite key
    @JoinColumn(name = "entry_id")
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId") // maps composite key
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public EntryTag() {}

    public EntryTag(JournalEntry journalEntry, Tag tag) {
        this.journalEntry = journalEntry;
        this.tag = tag;
        this.id = new EntryTagId(journalEntry.getId(), tag.getId());
    }

    public JournalEntry getJournalEntry() { return journalEntry; }
    public Tag getTag() { return tag; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntryTag)) return false;
        EntryTag that = (EntryTag) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
