package pro.kensait.backoffice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.backoffice.api.dto.*;
import pro.kensait.backoffice.common.*;
import pro.kensait.backoffice.dao.*;
import pro.kensait.backoffice.entity.*;
import pro.kensait.backoffice.exception.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WorkflowServiceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowDao workflowDao;

    @Mock
    private EmployeeDao employeeDao;

    @Mock
    private BookDao bookDao;

    @Mock
    private DepartmentDao departmentDao;

    @InjectMocks
    private WorkflowService workflowService;

    private Department testDepartment1;
    private Department testDepartment2;
    private Employee testAssociate;
    private Employee testManager;
    private Employee testDirector;
    private Workflow testWorkflow;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // テスト用部署
        testDepartment1 = new Department();
        testDepartment1.setDepartmentId(1L);
        testDepartment1.setDepartmentName("営業部");

        testDepartment2 = new Department();
        testDepartment2.setDepartmentId(2L);
        testDepartment2.setDepartmentName("管理部");

        // テスト用社員（ASSOCIATE）
        testAssociate = new Employee();
        testAssociate.setEmployeeId(1L);
        testAssociate.setEmployeeCode("E0001");
        testAssociate.setEmployeeName("山田太郎");
        testAssociate.setJobRank(1); // ASSOCIATE
        testAssociate.setDepartment(testDepartment1);

        // テスト用社員（MANAGER）
        testManager = new Employee();
        testManager.setEmployeeId(2L);
        testManager.setEmployeeCode("E0002");
        testManager.setEmployeeName("鈴木花子");
        testManager.setJobRank(2); // MANAGER
        testManager.setDepartment(testDepartment1);

        // テスト用社員（DIRECTOR）
        testDirector = new Employee();
        testDirector.setEmployeeId(3L);
        testDirector.setEmployeeCode("E0003");
        testDirector.setEmployeeName("佐藤次郎");
        testDirector.setJobRank(3); // DIRECTOR
        testDirector.setDepartment(testDepartment2);

        // テスト用ワークフロー
        testWorkflow = new Workflow();
        testWorkflow.setOperationId(1L);
        testWorkflow.setWorkflowId(1L);
        testWorkflow.setWorkflowType(WorkflowType.ADD_NEW_BOOK);
        testWorkflow.setState(WorkflowState.CREATED);
        testWorkflow.setBookName("新しい書籍");
        testWorkflow.setAuthor("著者名");
        testWorkflow.setPrice(new BigDecimal("3000"));
        testWorkflow.setCategoryId(1);
        testWorkflow.setPublisherId(1);
        testWorkflow.setOperationType(OperationType.CREATE);
        testWorkflow.setOperator(testAssociate);
        testWorkflow.setOperatedAt(LocalDateTime.now());

        // テスト用書籍
        testBook = new Book();
        testBook.setBookId(1);
        testBook.setBookName("既存の書籍");
        testBook.setPrice(new BigDecimal("2500"));
        testBook.setDeleted(false);
    }

    /**
     * テスト: ワークフロー作成（ADD_NEW_BOOK）
     */
    @Test
    void testCreateWorkflow_AddNewBook() {
        // Given
        WorkflowCreateRequest request = new WorkflowCreateRequest(
            "ADD_NEW_BOOK",
            1L,
            "新しい書籍",
            "著者名",
            new BigDecimal("3000"),
            "http://example.com/image.jpg",
            1,
            1,
            null,
            null,
            null,
            "新商品として追加"
        );

        when(employeeDao.findById(1L)).thenReturn(testAssociate);
        when(workflowDao.generateWorkflowId()).thenReturn(1L);

        // When
        WorkflowTO result = workflowService.createWorkflow(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.workflowId());
        assertEquals("ADD_NEW_BOOK", result.workflowType());
        assertEquals("CREATED", result.state());
        assertEquals("CREATE", result.operationType());
        assertEquals("新しい書籍", result.bookName());
        assertEquals(1L, result.operatedBy());

        verify(workflowDao, times(1)).insert(any(Workflow.class));
    }

    /**
     * テスト: ワークフロー作成（REMOVE_BOOK）
     */
    @Test
    void testCreateWorkflow_RemoveBook() {
        // Given
        WorkflowCreateRequest request = new WorkflowCreateRequest(
            "REMOVE_BOOK",
            1L,
            null,
            null,
            null,
            null,
            null,
            null,
            1,
            null,
            null,
            "在庫切れのため削除"
        );

        when(employeeDao.findById(1L)).thenReturn(testAssociate);
        when(workflowDao.generateWorkflowId()).thenReturn(2L);

        // When
        WorkflowTO result = workflowService.createWorkflow(request);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.workflowId());
        assertEquals("REMOVE_BOOK", result.workflowType());
        assertEquals("CREATED", result.state());
        assertEquals(1, result.bookId());

        verify(workflowDao, times(1)).insert(any(Workflow.class));
    }

    /**
     * テスト: ワークフロー作成（ADJUST_BOOK_PRICE）
     */
    @Test
    void testCreateWorkflow_AdjustPrice() {
        // Given
        WorkflowCreateRequest request = new WorkflowCreateRequest(
            "ADJUST_BOOK_PRICE",
            1L,
            null,
            null,
            new BigDecimal("2800"),
            null,
            null,
            null,
            1,
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 3, 31),
            "キャンペーン価格"
        );

        when(employeeDao.findById(1L)).thenReturn(testAssociate);
        when(workflowDao.generateWorkflowId()).thenReturn(3L);

        // When
        WorkflowTO result = workflowService.createWorkflow(request);

        // Then
        assertNotNull(result);
        assertEquals(3L, result.workflowId());
        assertEquals("ADJUST_BOOK_PRICE", result.workflowType());
        assertEquals("CREATED", result.state());
        assertEquals(new BigDecimal("2800"), result.price());

        verify(workflowDao, times(1)).insert(any(Workflow.class));
    }

    /**
     * テスト: ワークフロー申請（正常系）
     */
    @Test
    void testApplyWorkflow_Success() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(1L, null);

        testWorkflow.setState(WorkflowState.CREATED);
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(1L)).thenReturn(testAssociate);

        // When
        WorkflowTO result = workflowService.applyWorkflow(1L, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.workflowId());

        verify(workflowDao, times(1)).insert(any(Workflow.class));
    }

    /**
     * テスト: ワークフロー申請（不正な状態）
     */
    @Test
    void testApplyWorkflow_InvalidState() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(1L, null);

        testWorkflow.setState(WorkflowState.APPLIED); // すでに申請済み
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);

        // When & Then
        assertThrows(InvalidWorkflowStateException.class, () -> {
            workflowService.applyWorkflow(1L, request);
        });

        verify(workflowDao, never()).insert(any(Workflow.class));
    }

    /**
     * テスト: ワークフロー承認（DIRECTOR）
     */
    @Test
    void testApproveWorkflow_Director() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(3L, "承認します");

        testWorkflow.setState(WorkflowState.APPLIED);
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(3L)).thenReturn(testDirector);

        // When
        WorkflowTO result = workflowService.approveWorkflow(1L, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.workflowId());

        verify(workflowDao, times(1)).insert(any(Workflow.class));
        verify(bookDao, times(1)).insert(any(Book.class)); // 書籍マスタに反映
    }

    /**
     * テスト: ワークフロー承認（MANAGER・同一部署）
     */
    @Test
    void testApproveWorkflow_ManagerSameDepartment() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(2L, "承認します");

        testWorkflow.setState(WorkflowState.APPLIED);
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(2L)).thenReturn(testManager);

        // When
        WorkflowTO result = workflowService.approveWorkflow(1L, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.workflowId());

        verify(workflowDao, times(1)).insert(any(Workflow.class));
        verify(bookDao, times(1)).insert(any(Book.class));
    }

    /**
     * テスト: ワークフロー承認（MANAGER・異なる部署→例外）
     */
    @Test
    void testApproveWorkflow_ManagerDifferentDepartment() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(2L, "承認します");

        testWorkflow.setState(WorkflowState.APPLIED);
        testWorkflow.getOperator().setDepartment(testDepartment2); // 申請者は管理部
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(2L)).thenReturn(testManager); // 承認者は営業部

        // When & Then
        assertThrows(UnauthorizedApprovalException.class, () -> {
            workflowService.approveWorkflow(1L, request);
        });

        verify(workflowDao, never()).insert(any(Workflow.class));
        verify(bookDao, never()).insert(any(Book.class));
    }

    /**
     * テスト: ワークフロー承認（ASSOCIATE→例外）
     */
    @Test
    void testApproveWorkflow_AssociateUnauthorized() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(1L, "承認します");

        testWorkflow.setState(WorkflowState.APPLIED);
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(1L)).thenReturn(testAssociate);

        // When & Then
        assertThrows(UnauthorizedApprovalException.class, () -> {
            workflowService.approveWorkflow(1L, request);
        });

        verify(workflowDao, never()).insert(any(Workflow.class));
        verify(bookDao, never()).insert(any(Book.class));
    }

    /**
     * テスト: ワークフロー承認（不正な状態）
     */
    @Test
    void testApproveWorkflow_InvalidState() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(3L, "承認します");

        testWorkflow.setState(WorkflowState.CREATED); // APPLIED状態でない
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);

        // When & Then
        assertThrows(InvalidWorkflowStateException.class, () -> {
            workflowService.approveWorkflow(1L, request);
        });

        verify(workflowDao, never()).insert(any(Workflow.class));
    }

    /**
     * テスト: ワークフロー却下（正常系）
     */
    @Test
    void testRejectWorkflow_Success() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(2L, "内容不備のため差戻");

        testWorkflow.setState(WorkflowState.APPLIED);
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(2L)).thenReturn(testManager);

        // When
        WorkflowTO result = workflowService.rejectWorkflow(1L, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.workflowId());

        verify(workflowDao, times(1)).insert(any(Workflow.class));
        verify(bookDao, never()).insert(any(Book.class)); // 書籍マスタには反映しない
    }

    /**
     * テスト: ワークフロー却下（不正な状態）
     */
    @Test
    void testRejectWorkflow_InvalidState() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(2L, "差戻");

        testWorkflow.setState(WorkflowState.CREATED); // APPLIED状態でない
        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);

        // When & Then
        assertThrows(InvalidWorkflowStateException.class, () -> {
            workflowService.rejectWorkflow(1L, request);
        });

        verify(workflowDao, never()).insert(any(Workflow.class));
    }

    /**
     * テスト: ワークフロー一覧取得（全件）
     */
    @Test
    void testGetWorkflows_All() {
        // Given
        List<Workflow> mockWorkflows = Arrays.asList(testWorkflow);
        when(workflowDao.findAllLatest()).thenReturn(mockWorkflows);

        // When
        List<WorkflowTO> result = workflowService.getWorkflows(null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).workflowId());

        verify(workflowDao, times(1)).findAllLatest();
    }

    /**
     * テスト: ワークフロー一覧取得（状態フィルタ）
     */
    @Test
    void testGetWorkflows_ByState() {
        // Given
        testWorkflow.setState(WorkflowState.APPLIED);
        List<Workflow> mockWorkflows = Arrays.asList(testWorkflow);
        when(workflowDao.findAllLatestByState(WorkflowState.APPLIED)).thenReturn(mockWorkflows);

        // When
        List<WorkflowTO> result = workflowService.getWorkflows("APPLIED", null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("APPLIED", result.get(0).state());

        verify(workflowDao, times(1)).findAllLatestByState(WorkflowState.APPLIED);
    }

    /**
     * テスト: ワークフロー履歴取得
     */
    @Test
    void testGetWorkflowHistory() {
        // Given
        Workflow workflow1 = new Workflow();
        workflow1.setOperationId(1L);
        workflow1.setWorkflowId(1L);
        workflow1.setState(WorkflowState.CREATED);
        workflow1.setOperationType(OperationType.CREATE);
        workflow1.setOperator(testAssociate);
        workflow1.setOperatedAt(LocalDateTime.now().minusHours(2));

        Workflow workflow2 = new Workflow();
        workflow2.setOperationId(2L);
        workflow2.setWorkflowId(1L);
        workflow2.setState(WorkflowState.APPLIED);
        workflow2.setOperationType(OperationType.APPLY);
        workflow2.setOperator(testAssociate);
        workflow2.setOperatedAt(LocalDateTime.now().minusHours(1));

        List<Workflow> mockHistory = Arrays.asList(workflow1, workflow2);
        when(workflowDao.findHistoryByWorkflowId(1L)).thenReturn(mockHistory);

        // When
        List<WorkflowTO> result = workflowService.getWorkflowHistory(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CREATED", result.get(0).state());
        assertEquals("APPLIED", result.get(1).state());

        verify(workflowDao, times(1)).findHistoryByWorkflowId(1L);
    }

    /**
     * テスト: ワークフロー履歴取得（存在しない）
     */
    @Test
    void testGetWorkflowHistory_NotFound() {
        // Given
        when(workflowDao.findHistoryByWorkflowId(999L)).thenReturn(Arrays.asList());

        // When & Then
        assertThrows(WorkflowNotFoundException.class, () -> {
            workflowService.getWorkflowHistory(999L);
        });
    }

    /**
     * テスト: 書籍マスタ反映（ADD_NEW_BOOK）
     */
    @Test
    void testApproveWorkflow_AddNewBook_BookMasterReflection() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(3L, "承認");

        testWorkflow.setState(WorkflowState.APPLIED);
        testWorkflow.setWorkflowType(WorkflowType.ADD_NEW_BOOK);
        testWorkflow.setBookName("新書籍");
        testWorkflow.setAuthor("著者");
        testWorkflow.setPrice(new BigDecimal("3500"));
        testWorkflow.setCategoryId(1);
        testWorkflow.setPublisherId(1);

        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(3L)).thenReturn(testDirector);

        // When
        workflowService.approveWorkflow(1L, request);

        // Then
        verify(bookDao, times(1)).insert(any(Book.class));
    }

    /**
     * テスト: 書籍マスタ反映（REMOVE_BOOK）
     */
    @Test
    void testApproveWorkflow_RemoveBook_BookMasterReflection() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(3L, "承認");

        testWorkflow.setState(WorkflowState.APPLIED);
        testWorkflow.setWorkflowType(WorkflowType.REMOVE_BOOK);
        testWorkflow.setBookId(1);

        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(3L)).thenReturn(testDirector);
        when(bookDao.findById(1)).thenReturn(testBook);

        // When
        workflowService.approveWorkflow(1L, request);

        // Then
        verify(bookDao, times(1)).findById(1);
        verify(bookDao, times(1)).update(any(Book.class));
        assertTrue(testBook.getDeleted()); // 論理削除フラグが立つ
    }

    /**
     * テスト: 書籍マスタ反映（ADJUST_BOOK_PRICE）
     */
    @Test
    void testApproveWorkflow_AdjustPrice_BookMasterReflection() {
        // Given
        WorkflowOperationRequest request = new WorkflowOperationRequest(3L, "承認");

        testWorkflow.setState(WorkflowState.APPLIED);
        testWorkflow.setWorkflowType(WorkflowType.ADJUST_BOOK_PRICE);
        testWorkflow.setBookId(1);
        testWorkflow.setPrice(new BigDecimal("2800"));

        when(workflowDao.findLatestByWorkflowId(1L)).thenReturn(testWorkflow);
        when(employeeDao.findById(3L)).thenReturn(testDirector);
        when(bookDao.findById(1)).thenReturn(testBook);

        // When
        workflowService.approveWorkflow(1L, request);

        // Then
        verify(bookDao, times(1)).findById(1);
        verify(bookDao, times(1)).update(any(Book.class));
        assertEquals(new BigDecimal("2800"), testBook.getPrice());
    }
}
