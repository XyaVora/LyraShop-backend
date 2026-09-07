package com.lyrashop.catalog.category.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CategoryTests {

    @Test
    void updatesCatalogFieldsWithoutReactivating() {
        Category category = Category.create("Before", "before", "Old", null);
        category.deactivate();
        category.update("After", "after", "New", 3L);
        assertThat(category.getName()).isEqualTo("After");
        assertThat(category.getSlug()).isEqualTo("after");
        assertThat(category.getDescription()).isEqualTo("New");
        assertThat(category.getParentId()).isEqualTo(3L);
        assertThat(category.isActive()).isFalse();
        category.activate();
        assertThat(category.isActive()).isTrue();
    }
}
