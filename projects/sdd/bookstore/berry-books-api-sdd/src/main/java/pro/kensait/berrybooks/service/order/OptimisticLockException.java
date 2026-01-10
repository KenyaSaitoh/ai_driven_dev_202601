package pro.kensait.berrybooks.service.order;

/**
 * 楽観的ロック競合例外
 * 
 * 在庫更新時に他のユーザーが先に更新していた場合にスローされる
 */
public class OptimisticLockException extends RuntimeException {
    
    public OptimisticLockException(String message) {
        super(message);
    }
    
    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
