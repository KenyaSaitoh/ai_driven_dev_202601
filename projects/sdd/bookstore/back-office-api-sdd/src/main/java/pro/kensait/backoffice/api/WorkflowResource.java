package pro.kensait.backoffice.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.backoffice.api.dto.*;
import pro.kensait.backoffice.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ワークフローリソース
 * 
 * ワークフローAPIのエンドポイントを提供する
 */
@ApplicationScoped
@Path("/workflows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkflowResource {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowResource.class);

    @Inject
    private WorkflowService workflowService;

    /**
     * ワークフロー作成
     * 
     * @param request 作成リクエスト
     * @return 201 Created + ワークフローTO
     */
    @POST
    public Response createWorkflow(@Valid WorkflowCreateRequest request) {
        logger.info("[WorkflowResource#createWorkflow] workflowType: {}, createdBy: {}", 
                   request.workflowType(), request.createdBy());

        WorkflowTO workflow = workflowService.createWorkflow(request);

        logger.info("[WorkflowResource#createWorkflow] Created workflow: workflowId={}, operationId={}", 
                   workflow.workflowId(), workflow.operationId());

        return Response.status(Response.Status.CREATED)
                       .entity(workflow)
                       .build();
    }

    /**
     * ワークフロー更新（一時保存）
     * 
     * @param workflowId ワークフローID
     * @param request 更新リクエスト
     * @return 200 OK + ワークフローTO
     */
    @PUT
    @Path("/{workflowId}")
    public Response updateWorkflow(@PathParam("workflowId") Long workflowId,
                                   @Valid WorkflowUpdateRequest request) {
        logger.info("[WorkflowResource#updateWorkflow] workflowId: {}, updatedBy: {}", 
                   workflowId, request.updatedBy());

        WorkflowTO workflow = workflowService.updateWorkflow(workflowId, request);

        logger.info("[WorkflowResource#updateWorkflow] Updated workflow: workflowId={}, operationId={}", 
                   workflowId, workflow.operationId());

        return Response.ok(workflow).build();
    }

    /**
     * ワークフロー申請
     * 
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return 200 OK + ワークフローTO
     */
    @POST
    @Path("/{workflowId}/apply")
    public Response applyWorkflow(@PathParam("workflowId") Long workflowId,
                                  @Valid WorkflowOperationRequest request) {
        logger.info("[WorkflowResource#applyWorkflow] workflowId: {}, operatedBy: {}", 
                   workflowId, request.operatedBy());

        WorkflowTO workflow = workflowService.applyWorkflow(workflowId, request);

        logger.info("[WorkflowResource#applyWorkflow] Applied workflow: workflowId={}, state={}", 
                   workflowId, workflow.state());

        return Response.ok(workflow).build();
    }

    /**
     * ワークフロー承認
     * 
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return 200 OK + ワークフローTO
     */
    @POST
    @Path("/{workflowId}/approve")
    public Response approveWorkflow(@PathParam("workflowId") Long workflowId,
                                    @Valid WorkflowOperationRequest request) {
        logger.info("[WorkflowResource#approveWorkflow] workflowId: {}, operatedBy: {}", 
                   workflowId, request.operatedBy());

        WorkflowTO workflow = workflowService.approveWorkflow(workflowId, request);

        logger.info("[WorkflowResource#approveWorkflow] Approved workflow: workflowId={}, state={}", 
                   workflowId, workflow.state());

        return Response.ok(workflow).build();
    }

    /**
     * ワークフロー却下
     * 
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return 200 OK + ワークフローTO
     */
    @POST
    @Path("/{workflowId}/reject")
    public Response rejectWorkflow(@PathParam("workflowId") Long workflowId,
                                   @Valid WorkflowOperationRequest request) {
        logger.info("[WorkflowResource#rejectWorkflow] workflowId: {}, operatedBy: {}", 
                   workflowId, request.operatedBy());

        WorkflowTO workflow = workflowService.rejectWorkflow(workflowId, request);

        logger.info("[WorkflowResource#rejectWorkflow] Rejected workflow: workflowId={}, state={}", 
                   workflowId, workflow.state());

        return Response.ok(workflow).build();
    }

    /**
     * ワークフロー一覧取得
     * 
     * @param state 状態（オプション）
     * @param workflowType ワークフロータイプ（オプション）
     * @param employeeId 社員ID（オプション）
     * @return 200 OK + ワークフローTOのリスト
     */
    @GET
    public Response getWorkflows(@QueryParam("state") String state,
                                 @QueryParam("workflowType") String workflowType,
                                 @QueryParam("employeeId") Long employeeId) {
        logger.info("[WorkflowResource#getWorkflows] state: {}, workflowType: {}, employeeId: {}", 
                   state, workflowType, employeeId);

        List<WorkflowTO> workflows = workflowService.getWorkflows(state, workflowType, employeeId);

        logger.info("[WorkflowResource#getWorkflows] Found {} workflows", workflows.size());

        return Response.ok(workflows).build();
    }

    /**
     * ワークフロー履歴取得
     * 
     * @param workflowId ワークフローID
     * @return 200 OK + ワークフローTOのリスト
     */
    @GET
    @Path("/{workflowId}/history")
    public Response getWorkflowHistory(@PathParam("workflowId") Long workflowId) {
        logger.info("[WorkflowResource#getWorkflowHistory] workflowId: {}", workflowId);

        List<WorkflowTO> history = workflowService.getWorkflowHistory(workflowId);

        logger.info("[WorkflowResource#getWorkflowHistory] Found {} history records", history.size());

        return Response.ok(history).build();
    }
}
