import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { CartItem, Book, CartContextType } from '../types';

const CartContext = createContext<CartContextType | undefined>(undefined);

export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

interface CartProviderProps {
  children: ReactNode;
}

const CART_STORAGE_KEY = 'leaf_books_cart';

export const CartProvider: React.FC<CartProviderProps> = ({ children }) => {
  const [items, setItems] = useState<CartItem[]>([]);

  // LocalStorageからカートデータを読み込み
  useEffect(() => {
    try {
      const savedCart = localStorage.getItem(CART_STORAGE_KEY);
      if (savedCart) {
        setItems(JSON.parse(savedCart));
      }
    } catch (error) {
      console.error('Failed to load cart from localStorage:', error);
    }
  }, []);

  // カートが変更されたらLocalStorageに保存
  useEffect(() => {
    try {
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items));
    } catch (error) {
      console.error('Failed to save cart to localStorage:', error);
    }
  }, [items]);

  // 合計金額を計算
  const totalPrice = items.reduce((sum, item) => sum + item.price * item.count, 0);

  // 合計アイテム数を計算
  const itemCount = items.reduce((sum, item) => sum + item.count, 0);

  // カートにアイテムを追加
  const addItem = (book: Book, count: number = 1): void => {
    setItems((prevItems) => {
      const existingItem = prevItems.find((item) => item.bookId === book.bookId);
      
      if (existingItem) {
        // 既存のアイテムの個数を増やす
        return prevItems.map((item) =>
          item.bookId === book.bookId
            ? { ...item, count: item.count + count }
            : item
        );
      } else {
        // 新しいアイテムを追加
        const newItem: CartItem = {
          bookId: book.bookId,
          bookName: book.bookName,
          publisherName: book.publisher.publisherName,
          price: book.price,
          count,
          version: book.version,
        };
        return [...prevItems, newItem];
      }
    });
  };

  // カートからアイテムを削除
  const removeItem = (bookId: number): void => {
    setItems((prevItems) => prevItems.filter((item) => item.bookId !== bookId));
  };

  // アイテムの個数を更新
  const updateItemCount = (bookId: number, count: number): void => {
    if (count <= 0) {
      removeItem(bookId);
      return;
    }

    setItems((prevItems) =>
      prevItems.map((item) =>
        item.bookId === bookId ? { ...item, count } : item
      )
    );
  };

  // カートをクリア
  const clearCart = (): void => {
    setItems([]);
  };

  const value: CartContextType = {
    items,
    totalPrice,
    itemCount,
    addItem,
    removeItem,
    updateItemCount,
    clearCart,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

