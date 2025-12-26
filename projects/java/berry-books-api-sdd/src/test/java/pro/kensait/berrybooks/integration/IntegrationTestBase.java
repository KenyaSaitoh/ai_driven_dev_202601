package pro.kensait.berrybooks.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * 結合テストの基底クラス
 * 
 * 全ての結合テストはこのクラスを継承します。
 * テスト環境のセットアップとクリーンアップを行います。
 */
public abstract class IntegrationTestBase {

    @BeforeAll
    public static void setUpClass() {
        // テスト環境の初期化
        System.out.println("=== Integration Test Setup ===");
        // データベース接続やテストデータの準備はここで行う
    }

    @AfterAll
    public static void tearDownClass() {
        // テスト環境のクリーンアップ
        System.out.println("=== Integration Test Teardown ===");
        // リソースのクリーンアップはここで行う
    }
}


