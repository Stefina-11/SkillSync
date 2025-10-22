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
  let showFullDescription: { [key: number]: boolean } = {}; // To manage description toggle for each job

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
      alert('Favorite status updated!'); // Acknowledgment for favorite
    } catch (e) {
      console.error('Failed to toggle favorite', e);
      alert('Failed to update favorite status.');
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
      alert('Skills matched successfully!'); // Acknowledgment for match
    } catch (error) {
      console.error('Error matching skills:', error);
      alert('Failed to match skills.');
    }
  }

  async function apply(jobId: number) {
    if (!token) return;
    try {
      await applyToJob(token, jobId);
      appError = '';
      alert('Application submitted successfully!'); // Acknowledgment for apply
      // Optionally refresh applications list here if it were in this component
    } catch (e: any) {
      appError = e?.message || 'Failed to apply';
      alert(e?.message || 'Failed to apply to job.');
    }
  }
</script>

<div class="bg-white shadow p-4 rounded dark:bg-gray-800 dark:text-gray-200">
  <h2 class="text-2xl font-semibold mb-2">Job Postings</h2>
  <div class="mb-4 p-3 border rounded bg-gray-50 dark:bg-gray-700 dark:border-gray-600">
    <div class="flex flex-wrap gap-2 mb-2">
      <input placeholder="Keyword" bind:value={filterKeyword} class="border px-2 py-1 dark:bg-gray-800 dark:text-gray-200 dark:border-gray-600" />
      <input placeholder="Location" bind:value={filterLocation} class="border px-2 py-1 dark:bg-gray-800 dark:text-gray-200 dark:border-gray-600" />
      <input placeholder="Job Type" bind:value={filterJobType} class="border px-2 py-1 dark:bg-gray-800 dark:text-gray-200 dark:border-gray-600" />
      <input placeholder="Min Salary" type="number" bind:value={filterMinSalary} class="border px-2 py-1 dark:bg-gray-800 dark:text-gray-200 dark:border-gray-600" />
    </div>
    <div>
      <button on:click={fetchJobs} class="bg-pink-500 text-white px-4 py-2 rounded mr-2">Apply Filters</button>
      <button on:click={() => { filterKeyword=''; filterLocation=''; filterJobType=''; filterMinSalary=null; fetchJobs(); }} class="bg-gray-200 dark:bg-gray-700 dark:text-gray-200 px-3 py-1 rounded">Clear</button>
    </div>
  </div>

  {#if jobPostings.length > 0}
    <div class="space-y-4">
      {#each jobPostings as job (job.id)}
        <div class="border p-4 rounded bg-white shadow-lg shadow-pink-500/50 dark:bg-gray-700 dark:border-gray-600">
          <h3 class="text-xl font-semibold dark:text-gray-100">{job.title}</h3>
          <p class="text-gray-600 dark:text-gray-300">{job.company}</p>
          <p class="text-gray-500 text-sm dark:text-gray-400">{job.location} - {job.jobType}</p>
          {#if job.salary}
            <p class="text-gray-500 text-sm dark:text-gray-400">Salary: ${job.salary.toLocaleString()}</p>
          {/if}

          {#if job.skills && job.skills.length > 0}
            <div class="mt-2">
              <p class="font-semibold dark:text-gray-200">Skills:</p>
              <div class="flex flex-wrap gap-1">
                {#each job.skills as skill}
                  <span class="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded-full dark:bg-blue-800 dark:text-blue-100">{skill}</span>
                {/each}
              </div>
            </div>
          {/if}

          <div class="mt-2 text-gray-700 text-sm dark:text-gray-300">
            <p>
              {#if job.description}
                {showFullDescription[job.id] ? job.description : job.description.substring(0, 200) + '...'}
                {#if job.description.length > 200}
                  <button on:click={() => showFullDescription[job.id] = !showFullDescription[job.id]} class="text-blue-600 hover:underline dark:text-blue-400">
                    {showFullDescription[job.id] ? 'Read less' : 'Read more'}
                  </button>
                {/if}
              {:else}
                No description available.
              {/if}
            </p>
          </div>

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
