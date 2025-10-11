<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';

  onMount(() => {
    if (typeof localStorage === 'undefined') return goto('/');
    const t = localStorage.getItem('token');
    if (!t) goto('/');
  });
</script>

<slot />
