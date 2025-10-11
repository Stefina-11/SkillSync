<script lang="ts">
  import { getFavorites, toggleFavorite } from '$lib/apiService';
  import { onMount } from 'svelte';

  export let token: string;
  let favorites: any[] = [];

  onMount(() => {
    loadFavorites();
  });

  async function loadFavorites() {
    if (!token) return;
    try {
      favorites = await getFavorites(token);
    } catch (e) {
      console.error('Failed to load favorites', e);
    }
  }

  async function toggleFavoriteHandler(jobId: number) {
    if (!token) return;
    try {
      await toggleFavorite(token, jobId);
      await loadFavorites(); // Refresh favorites after toggling
    } catch (e) {
      console.error('Failed to toggle favorite', e);
    }
  }
</script>

<div class="bg-white shadow p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">Favorites</h2>
  {#if favorites.length > 0}
    <ul>
      {#each favorites as fav}
        <li class="mb-2">{fav.title} — {fav.company} <button on:click={() => toggleFavoriteHandler(fav.id)} class="ml-2 text-sm text-red-600">Remove</button></li>
      {/each}
    </ul>
  {:else}
    <div>No favorites yet.</div>
  {/if}
</div>
