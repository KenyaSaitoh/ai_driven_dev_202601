# Page snapshot

```yaml
- generic [ref=e3]:
  - heading "Berry Books オンライン書店" [level=2] [ref=e4]
  - separator [ref=e5]
  - generic [ref=e6]:
    - generic [ref=e7]: お客様情報の登録
    - generic [ref=e8]:
      - table [ref=e9]:
        - rowgroup [ref=e10]:
          - row "お客様名 Frank" [ref=e11]:
            - cell "お客様名" [ref=e12]
            - cell "Frank" [ref=e13]:
              - textbox [active] [ref=e14]: Frank
          - row "メールアドレス" [ref=e15]:
            - cell "メールアドレス" [ref=e16]
            - cell [ref=e17]:
              - textbox [ref=e18]
          - row "パスワード" [ref=e19]:
            - cell "パスワード" [ref=e20]
            - cell [ref=e21]:
              - textbox [ref=e22]
          - row "生年月日（任意）" [ref=e23]:
            - cell "生年月日（任意）" [ref=e24]
            - cell [ref=e25]:
              - textbox "yyyy-mm-dd" [ref=e26]
          - row "住所（任意）" [ref=e27]:
            - cell "住所（任意）" [ref=e28]
            - cell [ref=e29]:
              - textbox "例：東京都渋谷区..." [ref=e30]
      - generic [ref=e31]:
        - button "キャンセル" [ref=e32] [cursor=pointer]
        - button "登録" [ref=e33] [cursor=pointer]
```