package pro.kensait.backoffice.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.backoffice.api.dto.ErrorResponse;

/**
 * 汎用例外マッパー
 * 
 * 未処理の例外を500 Internal Server Errorレスポンスに変換
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        // WebApplicationExceptionはそのまま返す（他のExceptionMapperで処理される）
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        logger.error("Unexpected error occurred", exception);

        ErrorResponse error = new ErrorResponse(
            "InternalServerError",
            "サーバーエラーが発生しました。管理者にお問い合わせください。"
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
    }
}

