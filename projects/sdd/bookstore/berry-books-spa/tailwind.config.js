/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Berry Books テーマカラー（ストロベリーレッド）
        primary: {
          DEFAULT: '#CF3F4E',
          light: '#E55563',
          dark: '#A32D3A',
          darker: '#8B2533',
        },
        accent: {
          DEFAULT: '#FFE5E8',
          light: '#FFF5F6',
          lighter: '#FFF0F2',
        },
        error: {
          DEFAULT: '#d1495b',
          light: '#FFF0F2',
        }
      },
      fontFamily: {
        sans: ['Segoe UI', 'Tahoma', 'Geneva', 'Verdana', 'sans-serif'],
      },
      boxShadow: {
        'primary': '0 2px 8px rgba(207, 63, 78, 0.2)',
        'primary-md': '0 4px 12px rgba(207, 63, 78, 0.3)',
        'primary-lg': '0 8px 32px rgba(207, 63, 78, 0.35)',
        'table': '0 2px 20px rgba(207, 63, 78, 0.1)',
      },
      backgroundImage: {
        'gradient-primary': 'linear-gradient(135deg, #CF3F4E 0%, #A32D3A 100%)',
        'gradient-primary-hover': 'linear-gradient(135deg, #E55563 0%, #CF3F4E 100%)',
      }
    },
  },
  plugins: [],
}

