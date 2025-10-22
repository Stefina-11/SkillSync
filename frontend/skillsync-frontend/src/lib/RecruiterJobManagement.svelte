<script lang="ts">
  import { createJob, updateJob, deleteJob, fetchJobPostings, getApplicationsForJob, getResumeById, performAtsCheck, rateResume } from '$lib/apiService';
  import modalStore from '$lib/modalStore';
  import { onMount } from 'svelte';

  export let token: string;
  export let role: string; // Should be 'ROLE_RECRUITER'

  let recruiterJobForm: any = { title: '', company: '', description: '', skills: '', url: '', location: '', jobType: '', salary: null };
  let editingJobId: number | null = null;
  let recruiterJobs: any[] = []; // To display jobs posted by this recruiter

  let selectedJobApplications: any[] = [];
  let showApplicationsModal: boolean = false;
  let currentJobPostingId: number | null = null;

  let selectedResume: any = null;
  let showResumeModal: boolean = false;
  let currentResumeRating: number = 0;
  let loadingResume: boolean = false;
  let loadingAtsCheck: boolean = false;
  let loadingRating: boolean = false;

  onMount(() => {
    if (role === 'ROLE_RECRUITER') {
      loadRecruiterJobs();
    }
  });

  async function loadRecruiterJobs() {
    if (!token) return;
    try {
      const allJobs = await fetchJobPostings({});
      // In a real app, you'd filter jobs by the logged-in recruiter's ID.
      // For this example, we'll assume allJobs are relevant or filter client-side if recruiterId is available in job object.
      recruiterJobs = allJobs;

      // For each job, fetch its applications
      for (const job of recruiterJobs) {
        try {
          job.applications = await getApplicationsForJob(token, job.id);
        } catch (e) {
          console.error(`Failed to load applications for job ${job.id}`, e);
          job.applications = [];
        }
      }
    } catch (e) {
      console.error('Failed to load recruiter jobs', e);
    }
  }

  async function viewApplications(jobId: number) {
    currentJobPostingId = jobId;
    const job = recruiterJobs.find(j => j.id === jobId);
    if (job) {
      selectedJobApplications = job.applications;
      showApplicationsModal = true;
    }
  }

  async function viewResume(resumeId: number) {
    if (!token) return;
    loadingResume = true;
    try {
      selectedResume = await getResumeById(token, resumeId);
      currentResumeRating = selectedResume.recruiterRating || 0;
      showResumeModal = true;
    } catch (e) {
      console.error('Failed to fetch resume', e);
      alert('Failed to fetch resume.');
    } finally {
      loadingResume = false;
    }
  }

  async function runAtsCheck() {
    if (!token || !selectedResume) return;
    loadingAtsCheck = true;
    try {
      const updatedResume = await performAtsCheck(token, selectedResume.id);
      selectedResume = updatedResume; // Update the displayed resume with new ATS data
      alert('ATS check completed!');
    } catch (e) {
      console.error('Failed to perform ATS check', e);
      alert('Failed to perform ATS check.');
    } finally {
      loadingAtsCheck = false;
    }
  }

  async function submitRating() {
    if (!token || !selectedResume || currentResumeRating === 0) return;
    loadingRating = true;
    try {
      const updatedResume = await rateResume(token, selectedResume.id, currentResumeRating);
      selectedResume = updatedResume; // Update the displayed resume with new rating
      alert('Resume rated successfully!');
    } catch (e) {
      console.error('Failed to submit rating', e);
      alert('Failed to submit rating.');
    } finally {
      loadingRating = false;
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

<div class="bg-white shadow-pink p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">Recruiter Job Management</h2>

  <div class="max-w-3xl bg-white shadow-pink p-4 mt-6 rounded">
    <h3 class="text-lg font-semibold mb-2">{editingJobId ? 'Edit Job' : 'Create New Job'}</h3>
    <div class="grid gap-2">
      <input placeholder="Title" bind:value={recruiterJobForm.title} class="border px-2 py-1 shadow-pink" />
      <input placeholder="Company" bind:value={recruiterJobForm.company} class="border px-2 py-1 shadow-pink" />
      <input placeholder="Location" bind:value={recruiterJobForm.location} class="border px-2 py-1 shadow-pink" />
      <input placeholder="Job Type" bind:value={recruiterJobForm.jobType} class="border px-2 py-1 shadow-pink" />
      <input placeholder="Salary" type="number" bind:value={recruiterJobForm.salary} class="border px-2 py-1 shadow-pink" />
      <input placeholder="Skills (comma separated)" bind:value={recruiterJobForm.skills} class="border px-2 py-1 shadow-pink" />
      <input placeholder="URL" bind:value={recruiterJobForm.url} class="border px-2 py-1 shadow-pink" />
      <textarea placeholder="Description" bind:value={recruiterJobForm.description} class="border px-2 py-1 shadow-pink"></textarea>
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
          <div class="border p-4 rounded bg-white shadow-pink">
            <h4 class="text-lg font-semibold">{job.title}</h4>
            <p class="text-gray-600">{job.company}</p>
            <div class="mt-2 flex gap-2">
              <button on:click={() => editRecruiterJob(job)} class="bg-yellow-500 text-white px-3 py-1 rounded">Edit</button>
              <button on:click={() => removeRecruiterJob(job.id)} class="bg-red-500 text-white p-2 rounded flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm6 0a1 1 0 012 0v6a1 1 0 11-2 0V8z" clip-rule="evenodd" />
                </svg>
              </button>
              <button on:click={() => viewApplications(job.id)} class="bg-green-500 text-white px-3 py-1 rounded">View Applications ({job.applications ? job.applications.length : 0})</button>
            </div>
          </div>
        {/each}
      </div>
    {:else}
      <p>No jobs posted yet.</p>
    {/if}
  </div>
</div>

{#if showApplicationsModal}
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50 flex justify-center items-center">
    <div class="bg-white p-8 rounded-lg shadow-xl max-w-2xl w-full">
      <h3 class="text-xl font-semibold mb-4">Applications for Job ID: {currentJobPostingId}</h3>
      {#if selectedJobApplications.length > 0}
        <ul class="space-y-2">
          {#each selectedJobApplications as application (application.id)}
            <li class="border p-3 rounded flex justify-between items-center">
              <span>Applicant: {application.user.username} (Status: {application.status})</span>
              <button on:click={() => viewResume(application.user.resumeId)} class="bg-blue-500 text-white px-3 py-1 rounded">View Resume</button>
            </li>
          {/each}
        </ul>
      {:else}
        <p>No applications for this job yet.</p>
      {/if}
      <button on:click={() => showApplicationsModal = false} class="mt-4 bg-gray-300 px-4 py-2 rounded">Close</button>
    </div>
  </div>
{/if}

{#if showResumeModal}
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50 flex justify-center items-center">
    <div class="bg-white p-8 rounded-lg shadow-xl max-w-3xl w-full">
      <h3 class="text-xl font-semibold mb-4">Resume for {selectedResume?.filename}</h3>
      {#if loadingResume}
        <p>Loading resume...</p>
      {:else if selectedResume}
        <div class="border p-4 rounded mb-4 max-h-96 overflow-y-auto">
          <h4 class="font-semibold">Content:</h4>
          <pre class="whitespace-pre-wrap text-sm">{selectedResume.content}</pre>
          <h4 class="font-semibold mt-2">Skills:</h4>
          <p>{selectedResume.skills ? selectedResume.skills.join(', ') : 'N/A'}</p>
          <h4 class="font-semibold mt-2">ATS Score:</h4>
          <p>{selectedResume.atsScore !== null ? selectedResume.atsScore : 'N/A'}</p>
          <h4 class="font-semibold mt-2">ATS Feedback:</h4>
          <p>{selectedResume.atsFeedback || 'No feedback yet.'}</p>
          <h4 class="font-semibold mt-2">Recruiter Rating:</h4>
          <p>{selectedResume.recruiterRating !== null ? selectedResume.recruiterRating : 'Not rated yet.'}</p>
        </div>

        <div class="flex items-center gap-4 mb-4">
          <button on:click={runAtsCheck} disabled={loadingAtsCheck} class="bg-purple-500 text-white px-4 py-2 rounded disabled:opacity-50">
            {#if loadingAtsCheck}
              Running ATS Check...
            {:else}
              Run ATS Check
            {/if}
          </button>
          <div class="flex items-center gap-2">
            <label for="rating">Rate:</label>
            <select id="rating" bind:value={currentResumeRating} class="border p-2 rounded">
              <option value={0}>Select Rating</option>
              <option value={1}>1 Star</option>
              <option value={2}>2 Stars</option>
              <option value={3}>3 Stars</option>
              <option value={4}>4 Stars</option>
              <option value={5}>5 Stars</option>
            </select>
            <button on:click={submitRating} disabled={loadingRating || currentResumeRating === 0} class="bg-orange-500 text-white px-4 py-2 rounded disabled:opacity-50">
              {#if loadingRating}
                Submitting...
              {:else}
                Submit Rating
              {/if}
            </button>
          </div>
        </div>
      {:else}
        <p>No resume content available.</p>
      {/if}
      <button on:click={() => showResumeModal = false} class="mt-4 bg-gray-300 px-4 py-2 rounded">Close</button>
    </div>
  </div>
{/if}
