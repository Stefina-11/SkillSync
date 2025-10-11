<script lang="ts">
  import { uploadResume, getMyResume } from '$lib/apiService';
  import { onMount } from 'svelte';

  export let token: string;
  let resumeFile: File | null = null;
  export let extractedSkills: string[] = [];
  export let resumeId: number | null = null; // Export resumeId

  onMount(async () => {
    if (!token) return;
    try {
      const existingResume = await getMyResume(token);
      resumeId = existingResume.id;
      extractedSkills = existingResume.skills || [];
    } catch (error) {
      console.log('No existing resume found or failed to fetch:', error);
    }
  });

  async function handleUpload() {
    if (!token) return;
    if (!resumeFile) return;
    try {
      const data = await uploadResume(resumeFile);
      resumeId = data.id;
      extractedSkills = data.skills || [];
    } catch (error) {
      console.error('Error uploading resume:', error);
    }
  }
</script>

<div class="bg-white shadow p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">Upload Resume</h2>
  <input type="file" accept=".docx, .pdf" on:change={(e) => {
    const target = e.target as HTMLInputElement;
    if (target && target.files) {
      resumeFile = target.files[0];
    }
  }} />
  <div class="mt-3">
    <button on:click={handleUpload} disabled={!resumeFile} class="bg-blue-500 text-white px-4 py-2 rounded disabled:opacity-50">Upload</button>
  </div>

  {#if extractedSkills.length > 0}
    <div class="mt-4">
      <h3 class="text-xl font-semibold">Extracted Skills:</h3>
      <ul class="list-disc list-inside">
        {#each extractedSkills as skill}
          <li>{skill}</li>
        {/each}
      </ul>
    </div>
  {/if}
</div>
