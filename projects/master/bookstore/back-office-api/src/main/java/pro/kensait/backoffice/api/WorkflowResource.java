package pro.kensait.backoffice.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.backoffice.api.dto.WorkflowCreateRequest;
import pro.kensait.backoffice.api.dto.WorkflowOperationRequest;
import pro.kensait.backoffice.api.dto.WorkflowTO;
import pro.kensait.backoffice.service.workflow.WorkflowService;

/**
 * ワークフローAPIリソースクラス
 */
@Path("/workflows")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkflowResource {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowResource.class);

    @Inject
    private WorkflowService workflowService;

    /**
     * ワークフロー作成
     * POST /api/workflows
     * @param request 作成リクエスト
     * @return 201 Created + WorkflowTO
     */
    @POST
    public Response createWorkflow(WorkflowCreateRequest request) {
        logger.info("[ WorkflowResource#createWorkflow ] workflowType={}", request.getWorkflowType());

        try {
            WorkflowTO workflow = workflowService.createWorkflow(request);
            return Response.status(Response.Status.CREATED).entity(workflow).build();
        } catch (IllegalArgumentException e) {
            logger.error("ワークフロー作成エラー（不正な引数）", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            logger.error("ワークフロー作成エラー（内部エラー）", e);
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + message + "\"}")
                    .build();
        }
    }

    /**
     * ワークフロー履歴取得
     * GET /api/workflows/{workflowId}
     * @param workflowId ワークフローID
     * @return 200 OK + List<WorkflowTO>
     */
    @GET
    @Path("/{workflowId}")
    public Response getWorkflowHistory(@PathParam("workflowId") Long workflowId) {
        logger.info("[ WorkflowResource#getWorkflowHistory ] workflowId={}", workflowId);

        try {
            List<WorkflowTO> history = workflowService.getWorkflowHistory(workflowId);
            return Response.ok(history).build();
        } catch (Exception e) {
            logger.error("ワークフロー履歴取得エラー", e);
            throw e;
        }
    }

    /**
     * ワークフロー一覧取得
     * GET /api/workflows?state=xxx&workflowType=xxx
     * @param state 状態（オプション）
     * @param workflowType ワークフロータイプ（オプション）
     * @return 200 OK + List<WorkflowTO>
     */
    @GET
    public Response getWorkflows(
            @QueryParam("state") String state,
            @QueryParam("workflowType") String workflowType) {
        logger.info("[ WorkflowResource#getWorkflows ] state={}, workflowType={}", state, workflowType);

        try {
            List<WorkflowTO> workflows = workflowService.getWorkflows(state, workflowType);
            return Response.ok(workflows).build();
        } catch (Exception e) {
            logger.error("ワークフロー一覧取得エラー", e);
            throw e;
        }
    }

    /**
     * 申請
     * POST /api/workflows/{workflowId}/apply
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return 200 OK + WorkflowTO
     */
    @POST
    @Path("/{workflowId}/apply")
    public Response applyWorkflow(
            @PathParam("workflowId") Long workflowId,
            WorkflowOperationRequest request) {
        logger.info("[ WorkflowResource#applyWorkflow ] workflowId={}", workflowId);

        try {
            WorkflowTO workflow = workflowService.applyWorkflow(workflowId, request);
            return Response.ok(workflow).build();
        } catch (Exception e) {
            logger.error("ワークフロー申請エラー", e);
            throw e;
        }
    }

    /**
     * 承認
     * POST /api/workflows/{workflowId}/approve
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return 200 OK + WorkflowTO
     */
    @POST
    @Path("/{workflowId}/approve")
    public Response approveWorkflow(
            @PathParam("workflowId") Long workflowId,
            WorkflowOperationRequest request) {
        logger.info("[ WorkflowResource#approveWorkflow ] workflowId={}", workflowId);

        try {
            WorkflowTO workflow = workflowService.approveWorkflow(workflowId, request);
            return Response.ok(workflow).build();
        } catch (Exception e) {
            logger.error("ワークフロー承認エラー", e);
            throw e;
        }
    }

    /**
     * 却下
     * POST /api/workflows/{workflowId}/reject
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return 200 OK + WorkflowTO
     */
    @POST
    @Path("/{workflowId}/reject")
    public Response rejectWorkflow(
            @PathParam("workflowId") Long workflowId,
            WorkflowOperationRequest request) {
        logger.info("[ WorkflowResource#rejectWorkflow ] workflowId={}", workflowId);

        try {
            WorkflowTO workflow = workflowService.rejectWorkflow(workflowId, request);
            return Response.ok(workflow).build();
        } catch (Exception e) {
            logger.error("ワークフロー却下エラー", e);
            throw e;
        }
    }
}


