# Page snapshot

```yaml
- generic [ref=e3]:
  - banner [ref=e4]:
    - generic [ref=e6]:
      - generic [ref=e7]:
        - link "書籍一覧" [ref=e8] [cursor=pointer]:
          - /url: /books
        - link "書籍検索" [ref=e9] [cursor=pointer]:
          - /url: /books/search
        - button "注文履歴（方式1）" [ref=e10] [cursor=pointer]
        - button "注文履歴（方式2）" [ref=e11] [cursor=pointer]
        - button "注文履歴（方式3）" [ref=e12] [cursor=pointer]
      - generic [ref=e13]:
        - link "カート" [ref=e14] [cursor=pointer]:
          - /url: /cart
        - generic [ref=e15]: Alice さん
        - button "ログアウト" [ref=e16] [cursor=pointer]
  - main [ref=e17]:
    - heading "条件を入力して書籍を検索してください" [level=2] [ref=e18]
    - separator [ref=e19]
    - generic [ref=e20]:
      - table [ref=e21]:
        - rowgroup [ref=e22]:
          - row "カテゴリ すべて" [ref=e23]:
            - cell "カテゴリ" [ref=e24]
            - cell "すべて" [ref=e25]:
              - combobox [ref=e26]:
                - option "すべて" [selected]
          - row "検索キーワード" [ref=e27]:
            - cell "検索キーワード" [ref=e28]
            - cell [ref=e29]:
              - textbox "書籍名で検索" [ref=e30]
      - generic [ref=e31]:
        - button "検索実行（静的クエリ）" [ref=e32] [cursor=pointer]
        - button "検索実行（動的クエリ）" [ref=e33] [cursor=pointer]
    - paragraph [ref=e34]: 検索条件を入力して検索してください
```