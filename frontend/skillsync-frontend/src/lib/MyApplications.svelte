<script lang="ts">
  import { getMyApplications } from '$lib/apiService';
  import { onMount } from 'svelte';

  export let token: string;
  let myApplications: any[] = [];
  let appError: string = '';

  onMount(() => {
    loadApplications();
  });

  async function loadApplications() {
    if (!token) return;
    try {
      myApplications = await getMyApplications(token);
      appError = '';
    } catch (e: any) {
      appError = e?.message || 'Failed to load applications';
    }
  }
</script>

<div class="bg-white shadow p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">My Applications</h2>
  <button on:click={loadApplications} class="bg-green-500 text-white px-3 py-1 rounded mb-2">Refresh</button>
  {#if appError}
    <div class="text-red-500 mb-2">{appError}</div>
  {/if}
  {#if myApplications.length > 0}
    <div class="space-y-4">
      {#each myApplications as app}
        <div class="border p-4 rounded bg-white shadow-lg shadow-pink-500/50">
          <h4 class="text-lg font-semibold">{app.jobPosting?.title}</h4>
          <p class="text-gray-600">Company: {app.jobPosting?.company}</p>
          <p class="text-gray-500 text-sm">Status: {app.status}</p>
        </div>
      {/each}
    </div>
  {:else}
    <div>No applications yet.</div>
  {/if}
</div>
