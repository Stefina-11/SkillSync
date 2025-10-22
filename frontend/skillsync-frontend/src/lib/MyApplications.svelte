<script lang="ts">
  import { getMyApplications, deleteApplication } from '$lib/apiService';
  import { onMount } from 'svelte';
  import modalStore from '$lib/modalStore'; // Import modalStore for confirmation

  export let token: string;
  let myApplications: any[] = [];
  let appError: string = '';

  onMount(() => {
    loadApplications();
  });

  async function loadApplications() {
    let currentToken = token;
    if (!currentToken) {
      currentToken = localStorage.getItem('token') || '';
    }

    if (!currentToken) {
      appError = 'Authentication token not found. Please log in.';
      return;
    }

    try {
      myApplications = await getMyApplications(currentToken);
      appError = '';
    } catch (e: any) {
      appError = e?.message || 'Failed to load applications';
    }
  }

  async function withdrawApplication(applicationId: number, jobTitle: string) {
    let currentToken = token;
    if (!currentToken) {
      currentToken = localStorage.getItem('token') || '';
    }

    if (!currentToken) {
      appError = 'Authentication token not found. Please log in.';
      return;
    }

    try {
      const ok = await modalStore.confirm(`Withdraw your application for "${jobTitle}"? This action cannot be undone.`, 'Withdraw Application');
      if (!ok) return;

      await deleteApplication(currentToken, applicationId);
      await loadApplications(); // Refresh the list
      appError = '';
    } catch (e: any) {
      appError = e?.message || 'Failed to withdraw application';
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
      {#each myApplications as app (app.id)}
        <div class="border p-4 rounded bg-white shadow-lg shadow-pink-500/50 flex justify-between items-center">
          <div>
            <h4 class="text-lg font-semibold">{app.jobPosting?.title}</h4>
            <p class="text-gray-600">Company: {app.jobPosting?.company}</p>
            <p class="text-gray-500 text-sm">Status: {app.status}</p>
          </div>
          <button on:click={() => withdrawApplication(app.id, app.jobPosting?.title)} class="bg-red-500 text-white p-2 rounded flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm6 0a1 1 0 012 0v6a1 1 0 11-2 0V8z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
      {/each}
    </div>
  {:else}
    <div>No applications yet.</div>
  {/if}
</div>
