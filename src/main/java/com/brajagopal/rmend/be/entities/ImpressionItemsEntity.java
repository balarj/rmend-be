package com.brajagopal.rmend.be.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author <bxr4261>
 */
@Entity
@Table(name = "IMPRESSIONS_ITEMS")
public class ImpressionItemsEntity {

    @Id
    private Long itemId;

    private ImpressionItemsEntity(Long _itemId) {
        itemId = _itemId;
    }

    public static ImpressionItemsEntity createInstance(Long itemId) {
        return new ImpressionItemsEntity(itemId);
    }

    public ImpressionItemsEntity() {}

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "ImpressionItemsEntity {" +
                "itemId=" + itemId +
                '}';
    }
}
