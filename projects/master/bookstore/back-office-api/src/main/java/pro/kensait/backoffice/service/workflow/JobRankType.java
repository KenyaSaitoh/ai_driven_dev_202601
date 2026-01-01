package pro.kensait.backoffice.service.workflow;

/**
 * 職務ランクタイプ
 */
public enum JobRankType {
    /** 一般社員 */
    ASSOCIATE(1),
    /** マネージャー */
    MANAGER(2),
    /** ディレクター */
    DIRECTOR(3);

    private final int rank;

    JobRankType(int rank) {
        this.rank = rank;
    }

    /**
     * ランク値を取得
     * @return ランク値
     */
    public int getRank() {
        return rank;
    }

    /**
     * ランク値からJobRankTypeを取得
     * @param rank ランク値
     * @return JobRankType
     * @throws IllegalArgumentException 不正なランク値の場合
     */
    public static JobRankType fromRank(int rank) {
        for (JobRankType jobRank : values()) {
            if (jobRank.rank == rank) {
                return jobRank;
            }
        }
        throw new IllegalArgumentException("不正な職務ランク: " + rank);
    }

    /**
     * 指定されたランク以上かチェック
     * @param requiredRank 必要なランク
     * @return 指定されたランク以上の場合true
     */
    public boolean isAtLeast(JobRankType requiredRank) {
        return this.rank >= requiredRank.rank;
    }
}

