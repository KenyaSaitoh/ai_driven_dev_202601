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
    - heading "現在の買い物カゴの内容です" [level=2] [ref=e18]
    - separator [ref=e19]
    - generic [ref=e20]:
      - paragraph [ref=e21]: カートは空です
      - button "書籍一覧へ" [ref=e22] [cursor=pointer]
```