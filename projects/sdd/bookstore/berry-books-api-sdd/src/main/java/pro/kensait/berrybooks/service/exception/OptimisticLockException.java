package pro.kensait.berrybooks.service.exception;

/**
 * 楽観的ロック競合例外
 * 
 * 在庫のバージョン番号が一致しない場合にスローされる。
 */
public class OptimisticLockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OptimisticLockException(String message) {
        super(message);
    }
}
