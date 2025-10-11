<script lang="ts">
  import { fetchJobPostings, matchSkills, applyToJob, toggleFavorite, getFavorites } from '$lib/apiService';
  import { onMount } from 'svelte';

  export let token: string;
  export let role: string;
  export let extractedSkills: string[]; // This will be used for skill matching
  export let resumeId: number | null; // New prop for resumeId

  let jobPostings: any[] = [];
  let matchResult: any = null;
  let favorites: any[] = [];
  let appError: string = '';

  // Job filters
  let filterKeyword: string = '';
  let filterLocation: string = '';
  let filterJobType: string = '';
  let filterMinSalary: number | null = null;

  onMount(() => {
    fetchJobs();
    if (token && role === 'ROLE_USER') {
      getFavorites(token).then(f => favorites = f).catch(() => {});
    }
  });

  async function fetchJobs() {
    try {
      const filters: any = {};
      if (filterKeyword) filters.keyword = filterKeyword;
      if (filterLocation) filters.location = filterLocation;
      if (filterJobType) filters.jobType = filterJobType;
      if (filterMinSalary != null) filters.minSalary = filterMinSalary;
      jobPostings = await fetchJobPostings(filters);
    } catch (error) {
      console.error('Error fetching job postings:', error);
    }
  }

  async function toggleFavoriteHandler(jobId: number) {
    if (!token) return;
    try {
      await toggleFavorite(token, jobId);
      favorites = await getFavorites(token);
    } catch (e) {
      console.error('Failed to toggle favorite', e);
    }
  }

  async function matchSkillsHandler(jobId: number) {
    if (resumeId === null) {
      console.error('No resume uploaded for skill matching.');
      // Optionally, show a user-friendly message
      return;
    }
    try {
      const data = await matchSkills(jobId, resumeId);
      matchResult = { ...data, jobId: jobId };
    } catch (error) {
      console.error('Error matching skills:', error);
    }
  }

  async function apply(jobId: number) {
    if (!token) return;
    try {
      await applyToJob(token, jobId);
      appError = '';
      // Optionally refresh applications list here if it were in this component
    } catch (e: any) {
      appError = e?.message || 'Failed to apply';
    }
  }
</script>

<div class="bg-white shadow p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">Job Postings</h2>
  <div class="mb-4 p-3 border rounded bg-gray-50">
    <div class="flex flex-wrap gap-2 mb-2">
      <input placeholder="Keyword" bind:value={filterKeyword} class="border px-2 py-1" />
      <input placeholder="Location" bind:value={filterLocation} class="border px-2 py-1" />
      <input placeholder="Job Type" bind:value={filterJobType} class="border px-2 py-1" />
      <input placeholder="Min Salary" type="number" bind:value={filterMinSalary} class="border px-2 py-1" />
    </div>
    <div>
      <button on:click={fetchJobs} class="bg-green-500 text-white px-4 py-2 rounded mr-2">Apply Filters</button>
      <button on:click={() => { filterKeyword=''; filterLocation=''; filterJobType=''; filterMinSalary=null; fetchJobs(); }} class="bg-gray-200 px-3 py-1 rounded">Clear</button>
    </div>
  </div>

  {#if jobPostings.length > 0}
    <div class="space-y-4">
      {#each jobPostings as job (job.id)}
        <div class="border p-4 rounded bg-white">
          <h3 class="text-xl font-semibold">{job.title}</h3>
          <p class="text-gray-600">{job.company}</p>
          <div class="mt-3 flex flex-wrap gap-2">
            <button on:click={() => matchSkillsHandler(job.id)} class="bg-purple-500 text-white px-4 py-2 rounded">Match</button>
            {#if token && role === 'ROLE_USER'}
              <button on:click={() => apply(job.id)} class="bg-orange-500 text-white px-3 py-1 rounded">Apply</button>
              <button on:click={() => toggleFavoriteHandler(job.id)} class="bg-yellow-400 text-black px-3 py-1 rounded">{favorites.find(f => f.id === job.id) ? 'Unfavorite' : 'Favorite'}</button>
            {/if}
          </div>

          {#if matchResult && matchResult.jobId === job.id}
            <div class="mt-4">
              <p>Match Percentage: {matchResult.matchPercentage?.toFixed(2)}%</p>
              <p>Missing Skills:</p>
              {#if matchResult.missingSkills && matchResult.missingSkills.length > 0}
                <ul class="list-disc list-inside">
                  {#each matchResult.missingSkills as skill}
                    <li>{skill}</li>
                  {/each}
                </ul>
              {:else}
                <p>None</p>
              {/if}
            </div>
          {/if}
        </div>
      {/each}
    </div>
  {:else}
    <div class="text-gray-600">No job postings found. Click "Apply Filters" to load them.</div>
  {/if}
</div>
