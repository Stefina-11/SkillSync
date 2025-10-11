<script lang="ts">
  import modalStore from '$lib/modalStore';
  import { onDestroy } from 'svelte';

  let state: any = { open: false, title: '', message: '' };
  const unsub = modalStore.subscribe(s => state = s);
  onDestroy(unsub);

  function onConfirm() {
    if (state._resolve) state._resolve(true);
    modalStore.close();
  }
  function onCancel() {
    if (state._resolve) state._resolve(false);
    modalStore.close();
  }
</script>

{#if state.open}
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded shadow p-4 w-11/12 max-w-md">
      <h3 class="text-lg font-semibold mb-2">{state.title}</h3>
      <div class="mb-4">{state.message}</div>
      <div class="flex justify-end gap-2">
        <button on:click={onCancel} class="px-3 py-1 rounded bg-gray-200">Cancel</button>
        <button on:click={onConfirm} class="px-3 py-1 rounded bg-red-600 text-white">Confirm</button>
      </div>
    </div>
  </div>
{/if}
