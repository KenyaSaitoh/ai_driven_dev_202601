/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Leaf Books テーマカラー（新緑グリーン）
        primary: {
          DEFAULT: '#8BC34A',
          light: '#9CCC65',
          dark: '#7CB342',
          darker: '#558B2F',
        },
        accent: {
          DEFAULT: '#DCEDC8',
          light: '#F1F8E9',
          lighter: '#E8F5E9',
        },
        error: {
          DEFAULT: '#d1495b',
          light: '#fce8eb',
        }
      },
      fontFamily: {
        sans: ['Segoe UI', 'Tahoma', 'Geneva', 'Verdana', 'sans-serif'],
      },
      boxShadow: {
        'primary': '0 2px 8px rgba(124, 179, 66, 0.2)',
        'primary-md': '0 4px 12px rgba(124, 179, 66, 0.3)',
        'primary-lg': '0 8px 32px rgba(124, 179, 66, 0.35)',
        'table': '0 2px 20px rgba(124, 179, 66, 0.1)',
      },
      backgroundImage: {
        'gradient-primary': 'linear-gradient(135deg, #8BC34A 0%, #7CB342 100%)',
        'gradient-primary-hover': 'linear-gradient(135deg, #9CCC65 0%, #8BC34A 100%)',
      }
    },
  },
  plugins: [],
}

