import { writable } from 'svelte/store';
import { browser } from '$app/environment';

// Initialize with value from localStorage or default to 'light'
const initialTheme = browser ? localStorage.getItem('theme') || 'light' : 'light';

export const theme = writable<string>(initialTheme);

theme.subscribe((value) => {
  if (browser) {
    localStorage.setItem('theme', value);
    if (value === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
});

export function toggleTheme() {
  theme.update((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'));
}
