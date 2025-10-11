<script lang="ts">
  import { createJob, updateJob, deleteJob, fetchJobPostings } from '$lib/apiService';
  import modalStore from '$lib/modalStore';
  import { onMount } from 'svelte';

  export let token: string;
  export let role: string; // Should be 'ROLE_RECRUITER'

  let recruiterJobForm: any = { title: '', company: '', description: '', skills: '', url: '', location: '', jobType: '', salary: null };
  let editingJobId: number | null = null;
  let recruiterJobs: any[] = []; // To display jobs posted by this recruiter

  onMount(() => {
    if (role === 'ROLE_RECRUITER') {
      loadRecruiterJobs();
    }
  });

  async function loadRecruiterJobs() {
    // Assuming fetchJobPostings can be filtered by recruiter, or we fetch all and filter client-side
    // For now, let's assume fetchJobPostings returns all and we filter by the logged-in recruiter's jobs
    // This might need a backend change to filter by recruiter ID if not already implemented.
    try {
      const allJobs = await fetchJobPostings({}); // Fetch all jobs
      // This is a placeholder. A proper implementation would involve a backend endpoint to get jobs by recruiter.
      // For now, we'll just show all jobs, and the recruiter can edit/delete their own.
      recruiterJobs = allJobs;
    } catch (e) {
      console.error('Failed to load recruiter jobs', e);
    }
  }

  async function saveRecruiterJob() {
    if (!token) return;
    try {
      const jobPayload = { ...recruiterJobForm, skills: recruiterJobForm.skills ? recruiterJobForm.skills.split(',').map((s:string)=>s.trim()) : [] };
      if (editingJobId) {
        await updateJob(token, editingJobId, jobPayload);
      } else {
        await createJob(token, jobPayload);
      }
      recruiterJobForm = { title: '', company: '', description: '', skills: '', url: '', location: '', jobType: '', salary: null };
      editingJobId = null;
      await loadRecruiterJobs(); // Refresh the list
    } catch (e) {
      console.error('Failed to save recruiter job', e);
    }
  }

  async function editRecruiterJob(job: any) {
    editingJobId = job.id;
    recruiterJobForm = { ...job, skills: (job.skills || []).join(', ') };
  }

  async function removeRecruiterJob(jobId: number) {
    if (!token) return;
    try {
      const ok = await modalStore.confirm(`Delete your job posting ${jobId}? This action cannot be undone.`, 'Delete job');
      if (!ok) return;
      await deleteJob(token, jobId);
      await loadRecruiterJobs(); // Refresh the list
    } catch (e) {
      console.error('Failed to delete job', e);
    }
  }
</script>

<div class="bg-white shadow p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">Recruiter Job Management</h2>

  <div class="max-w-lg bg-white shadow p-4 mt-6 rounded">
    <h3 class="text-lg font-semibold mb-2">{editingJobId ? 'Edit Job' : 'Create New Job'}</h3>
    <div class="grid gap-2">
      <input placeholder="Title" bind:value={recruiterJobForm.title} class="border px-2 py-1" />
      <input placeholder="Company" bind:value={recruiterJobForm.company} class="border px-2 py-1" />
      <input placeholder="Location" bind:value={recruiterJobForm.location} class="border px-2 py-1" />
      <input placeholder="Job Type" bind:value={recruiterJobForm.jobType} class="border px-2 py-1" />
      <input placeholder="Salary" type="number" bind:value={recruiterJobForm.salary} class="border px-2 py-1" />
      <input placeholder="Skills (comma separated)" bind:value={recruiterJobForm.skills} class="border px-2 py-1" />
      <input placeholder="URL" bind:value={recruiterJobForm.url} class="border px-2 py-1" />
      <textarea placeholder="Description" bind:value={recruiterJobForm.description} class="border px-2 py-1"></textarea>
      <div class="flex gap-2">
        <button on:click={saveRecruiterJob} class="bg-blue-500 text-white px-3 py-1 rounded">{editingJobId ? 'Update Job' : 'Create Job'}</button>
        {#if editingJobId}
          <button on:click={() => { editingJobId = null; recruiterJobForm = { title: '', company: '', description: '', skills: '', url: '', location: '', jobType: '', salary: null }; }} class="bg-gray-200 px-3 py-1 rounded">Cancel Edit</button>
        {/if}
      </div>
    </div>
  </div>

  <div class="mt-6">
    <h3 class="text-xl font-semibold mb-2">Your Posted Jobs</h3>
    {#if recruiterJobs.length > 0}
      <div class="space-y-4">
        {#each recruiterJobs as job (job.id)}
          <div class="border p-4 rounded bg-white">
            <h4 class="text-lg font-semibold">{job.title}</h4>
            <p class="text-gray-600">{job.company}</p>
            <div class="mt-2 flex gap-2">
              <button on:click={() => editRecruiterJob(job)} class="bg-yellow-500 text-white px-3 py-1 rounded">Edit</button>
              <button on:click={() => removeRecruiterJob(job.id)} class="bg-red-500 text-white px-3 py-1 rounded">Delete</button>
            </div>
          </div>
        {/each}
      </div>
    {:else}
      <p>No jobs posted yet.</p>
    {/if}
  </div>
</div>
