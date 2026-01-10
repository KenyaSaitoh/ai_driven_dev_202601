package pro.kensait.backoffice.common;

/**
 * 職務ランク列挙型
 * 
 * 社員の職務ランクと承認権限を定義する
 */
public enum JobRankType {
    /**
     * 一般社員（承認権限なし）
     */
    ASSOCIATE(1),
    
    /**
     * マネージャー（同一部署のみ承認可）
     */
    MANAGER(2),
    
    /**
     * ディレクター（全部署承認可）
     */
    DIRECTOR(3);
    
    private final int rank;
    
    JobRankType(int rank) {
        this.rank = rank;
    }
    
    /**
     * ランク値を取得
     * 
     * @return ランク値
     */
    public int getRank() {
        return rank;
    }
    
    /**
     * ランク値から職務ランクを取得
     * 
     * @param rank ランク値
     * @return 職務ランク
     * @throws IllegalArgumentException ランク値が不正な場合
     */
    public static JobRankType fromRank(int rank) {
        for (JobRankType type : values()) {
            if (type.rank == rank) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid rank value: " + rank);
    }
    
    /**
     * マネージャー以上かどうかを判定
     * 
     * @return マネージャー以上の場合true
     */
    public boolean isManagerOrAbove() {
        return rank >= MANAGER.rank;
    }
    
    /**
     * ディレクターかどうかを判定
     * 
     * @return ディレクターの場合true
     */
    public boolean isDirector() {
        return this == DIRECTOR;
    }
}
