import type { Config } from "tailwindcss";
import typography from "@tailwindcss/typography";

export default {
  content: ["./src/**/*.{html,js,svelte,ts}"],
  darkMode: 'class', // Enable dark mode based on class

  theme: {
    extend: {
      colors: {
        pink: {
          50: '#FDF2F8',
          100: '#FCE7F3',
          200: '#FBCFE8',
          300: '#F9A8D4',
          400: '#F472B6',
          500: '#EC4899', // Hot pink for button
          600: '#DB2777',
          700: '#BE185D',
          800: '#9D174D',
          900: '#831843',
          950: '#500724',
        },
      },
      boxShadow: {
        pink: '0 4px 6px -1px rgba(236, 72, 153, 0.5), 0 2px 4px -2px rgba(236, 72, 153, 0.5)', // Pink shadow
      },
    }
  },

  plugins: [typography]
} as Config;
