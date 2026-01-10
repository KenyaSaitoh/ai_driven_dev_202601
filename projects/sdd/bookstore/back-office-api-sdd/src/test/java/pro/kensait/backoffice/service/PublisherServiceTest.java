package pro.kensait.backoffice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.backoffice.api.dto.PublisherTO;
import pro.kensait.backoffice.dao.PublisherDao;
import pro.kensait.backoffice.entity.Publisher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PublisherServiceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private PublisherDao publisherDao;

    @InjectMocks
    private PublisherService publisherService;

    private Publisher testPublisher1;
    private Publisher testPublisher2;
    private Publisher testPublisher3;

    @BeforeEach
    void setUp() {
        // テスト用出版社
        testPublisher1 = new Publisher(1, "技術評論社");
        testPublisher2 = new Publisher(2, "翔泳社");
        testPublisher3 = new Publisher(3, "オライリー・ジャパン");
    }

    /**
     * テスト: 全出版社取得（正常系）
     */
    @Test
    void testGetAllPublishers() {
        // Given
        List<Publisher> mockPublishers = Arrays.asList(
            testPublisher1,
            testPublisher2,
            testPublisher3
        );
        when(publisherDao.findAll()).thenReturn(mockPublishers);

        // When
        List<PublisherTO> result = publisherService.getAllPublishers();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // 1つ目の出版社を確認
        PublisherTO publisher1 = result.get(0);
        assertEquals(1, publisher1.publisherId());
        assertEquals("技術評論社", publisher1.publisherName());
        
        // 2つ目の出版社を確認
        PublisherTO publisher2 = result.get(1);
        assertEquals(2, publisher2.publisherId());
        assertEquals("翔泳社", publisher2.publisherName());
        
        // 3つ目の出版社を確認
        PublisherTO publisher3 = result.get(2);
        assertEquals(3, publisher3.publisherId());
        assertEquals("オライリー・ジャパン", publisher3.publisherName());

        // モックの呼び出し確認
        verify(publisherDao, times(1)).findAll();
    }

    /**
     * テスト: 全出版社取得（0件）
     */
    @Test
    void testGetAllPublishers_Empty() {
        // Given
        when(publisherDao.findAll()).thenReturn(Collections.emptyList());

        // When
        List<PublisherTO> result = publisherService.getAllPublishers();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());

        // モックの呼び出し確認
        verify(publisherDao, times(1)).findAll();
    }

    /**
     * テスト: 出版社詳細取得（正常系）
     */
    @Test
    void testGetPublisherById_Found() {
        // Given
        when(publisherDao.findById(1)).thenReturn(testPublisher1);

        // When
        PublisherTO result = publisherService.getPublisherById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.publisherId());
        assertEquals("技術評論社", result.publisherName());

        // モックの呼び出し確認
        verify(publisherDao, times(1)).findById(1);
    }

    /**
     * テスト: 出版社詳細取得（存在しない）
     */
    @Test
    void testGetPublisherById_NotFound() {
        // Given
        when(publisherDao.findById(999)).thenReturn(null);

        // When
        PublisherTO result = publisherService.getPublisherById(999);

        // Then
        assertNull(result);

        // モックの呼び出し確認
        verify(publisherDao, times(1)).findById(999);
    }

    /**
     * テスト: 変換メソッド（nullチェック）
     */
    @Test
    void testConvertToTO_Null() {
        // Given
        when(publisherDao.findById(null)).thenReturn(null);

        // When
        PublisherTO result = publisherService.getPublisherById(null);

        // Then
        assertNull(result);
    }

    /**
     * テスト: 全出版社取得（順序確認）
     */
    @Test
    void testGetAllPublishers_Order() {
        // Given
        List<Publisher> mockPublishers = Arrays.asList(
            testPublisher1,  // ID: 1
            testPublisher2,  // ID: 2
            testPublisher3   // ID: 3
        );
        when(publisherDao.findAll()).thenReturn(mockPublishers);

        // When
        List<PublisherTO> result = publisherService.getAllPublishers();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // publisherId昇順であることを確認
        assertEquals(1, result.get(0).publisherId());
        assertEquals(2, result.get(1).publisherId());
        assertEquals(3, result.get(2).publisherId());

        // モックの呼び出し確認
        verify(publisherDao, times(1)).findAll();
    }

    /**
     * テスト: 出版社名が日本語
     */
    @Test
    void testGetPublisherById_JapaneseName() {
        // Given
        when(publisherDao.findById(1)).thenReturn(testPublisher1);

        // When
        PublisherTO result = publisherService.getPublisherById(1);

        // Then
        assertNotNull(result);
        assertEquals("技術評論社", result.publisherName());
        assertTrue(result.publisherName().matches("^[\\p{Script=Han}\\p{Script=Hiragana}\\p{Script=Katakana}]+$"));

        // モックの呼び出し確認
        verify(publisherDao, times(1)).findById(1);
    }
}
