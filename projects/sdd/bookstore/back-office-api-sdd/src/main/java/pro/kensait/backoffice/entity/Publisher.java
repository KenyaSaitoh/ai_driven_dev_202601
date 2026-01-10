package pro.kensait.backoffice.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 出版社エンティティ
 * 
 * テーブル: PUBLISHER
 */
@Entity
@Table(name = "PUBLISHER")
public class Publisher implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PUBLISHER_ID")
    private Integer publisherId;

    @Column(name = "PUBLISHER_NAME", length = 100)
    private String publisherName;

    // デフォルトコンストラクタ（JPA必須）
    public Publisher() {
    }

    // コンストラクタ
    public Publisher(Integer publisherId, String publisherName) {
        this.publisherId = publisherId;
        this.publisherName = publisherName;
    }

    // Getter/Setter
    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    // equals/hashCode（主キーベース）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Publisher publisher = (Publisher) o;
        return Objects.equals(publisherId, publisher.publisherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publisherId);
    }

    // toString
    @Override
    public String toString() {
        return "Publisher{" +
                "publisherId=" + publisherId +
                ", publisherName='" + publisherName + '\'' +
                '}';
    }
}
