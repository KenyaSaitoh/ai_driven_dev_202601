package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;

/**
 * 出版社エンティティ
 * 
 * テーブル: PUBLISHER
 */
@Entity
@Table(name = "PUBLISHER")
public class Publisher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PUBLISHER_ID")
    private Integer publisherId;
    
    @Column(name = "PUBLISHER_NAME", length = 30, nullable = false)
    private String publisherName;
    
    // コンストラクタ
    public Publisher() {
    }
    
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
    
    @Override
    public String toString() {
        return "Publisher{" +
                "publisherId=" + publisherId +
                ", publisherName='" + publisherName + '\'' +
                '}';
    }
}

