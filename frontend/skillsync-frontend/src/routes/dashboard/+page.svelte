<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import ConfirmModal from '$lib/ConfirmModal.svelte';
  import ResumeUpload from '$lib/ResumeUpload.svelte';
  import JobPostings from '$lib/JobPostings.svelte';
  import MyApplications from '$lib/MyApplications.svelte';
  import Favorites from '$lib/Favorites.svelte';
  import ProfileSettings from '$lib/ProfileSettings.svelte';
  import RecruiterJobManagement from '$lib/RecruiterJobManagement.svelte';
  import AdminDashboard from '$lib/AdminDashboard.svelte';
  import { theme, toggleTheme } from '$lib/themeStore';

  // Auth state read from localStorage
  let token = '';
  let role = 'ROLE_USER';
  let loggedInUser = ''; // Not used directly in this refactored version, but kept for consistency if needed elsewhere

  // State for ResumeUpload component
  let extractedSkills: string[] = [];
  let resumeId: number | null = null;

  // Tab management
  let activeTab: string = 'profile'; // Default tab for users

  onMount(() => {
    if (typeof localStorage === 'undefined') return goto('/');
    const t = localStorage.getItem('token');
    const r = localStorage.getItem('role');
    if (!t) return goto('/');
    token = t;
    role = r || 'ROLE_USER';

    // Set initial active tab based on role
    if (role === 'ROLE_ADMIN') {
      activeTab = 'admin_dashboard';
    } else if (role === 'ROLE_RECRUITER') {
      activeTab = 'recruiter_jobs';
    } else {
      activeTab = 'profile'; // Set profile as default for regular users
    }
  });

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    goto('/');
  }
</script>

<div class="w-full mx-auto px-8 py-2"> <!-- Reduced max-w-screen-xl -->
  <header class="flex items-center justify-between mb-2 border-b var(--header-border-color)">
    <h1 class="skillsync-title">SkillSync</h1>
    <div class="flex items-center gap-3">
      <button on:click={toggleTheme} class="bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-white px-3 py-1 rounded">
        {$theme === 'dark' ? 'Light Mode' : 'Dark Mode'}
      </button>
      <div class="text-sm text-gray-600 dark:text-gray-300">{role.replace('ROLE_', '')}</div>
      <button on:click={logout} class="bg-red-500 text-white px-3 py-1 rounded">Logout</button>
    </div>
  </header>

  <nav class="mb-6">
    <ul class="flex border-b border-[var(--tab-border-color)]">
      {#if role === 'ROLE_USER'}
        <li class="-mb-px mr-1">
          <button
            class="bg-[var(--tab-bg-color)] inline-block py-2 px-4 font-semibold {activeTab === 'profile' ? 'border-l border-t border-r rounded-t text-[var(--tab-text-color-active)] border-[var(--tab-border-color)]' : 'text-[var(--tab-text-color-inactive)] hover:text-[var(--tab-text-color-active)]'}"
            on:click={() => (activeTab = 'profile')}
          >
            Profile
          </button>
        </li>
        <li class="-mb-px mr-1">
          <button
            class="bg-[var(--tab-bg-color)] inline-block py-2 px-4 font-semibold {activeTab === 'resume_upload' ? 'border-l border-t border-r rounded-t text-[var(--tab-text-color-active)] border-[var(--tab-border-color)]' : 'text-[var(--tab-text-color-inactive)] hover:text-[var(--tab-text-color-active)]'}"
            on:click={() => (activeTab = 'resume_upload')}
          >
            Upload Resume
          </button>
        </li>
        <li class="-mb-px mr-1">
          <button
            class="bg-[var(--tab-bg-color)] inline-block py-2 px-4 font-semibold {activeTab === 'job_search' ? 'border-l border-t border-r rounded-t text-[var(--tab-text-color-active)] border-[var(--tab-border-color)]' : 'text-[var(--tab-text-color-inactive)] hover:text-[var(--tab-text-color-active)]'}"
            on:click={() => (activeTab = 'job_search')}
          >
            Job Search
          </button>
        </li>
        <li class="-mb-px mr-1">
          <button
            class="bg-[var(--tab-bg-color)] inline-block py-2 px-4 font-semibold {activeTab === 'my_applications' ? 'border-l border-t border-r rounded-t text-[var(--tab-text-color-active)] border-[var(--tab-border-color)]' : 'text-[var(--tab-text-color-inactive)] hover:text-[var(--tab-text-color-active)]'}"
            on:click={() => (activeTab = 'my_applications')}
          >
            My Applications
          </button>
        </li>
        <li class="-mb-px mr-1">
          <button
            class="bg-[var(--tab-bg-color)] inline-block py-2 px-4 font-semibold {activeTab === 'favorites' ? 'border-l border-t border-r rounded-t text-[var(--tab-text-color-active)] border-[var(--tab-border-color)]' : 'text-[var(--tab-text-color-inactive)] hover:text-[var(--tab-text-color-active)]'}"
            on:click={() => (activeTab = 'favorites')}
          >
            Favorites
          </button>
        </li>
      {/if}
      {#if role === 'ROLE_RECRUITER'}
        <li class="-mb-px mr-1">
          <button
            class="bg-[var(--tab-bg-color)] inline-block py-2 px-4 font-semibold {activeTab === 'recruiter_jobs' ? 'border-l border-t border-r rounded-t text-[var(--tab-text-color-active)] border-[var(--tab-border-color)]' : 'text-[var(--tab-text-color-inactive)] hover:text-[var(--tab-text-color-active)]'}"
            on:click={() => (activeTab = 'recruiter_jobs')}
          >
            Manage Jobs
          </button>
        </li>
      {/if}
      {#if role === 'ROLE_ADMIN'}
        <li class="-mb-px mr-1">
          <button
            class="bg-[var(--tab-bg-color)] inline-block py-2 px-4 font-semibold {activeTab === 'admin_dashboard' ? 'border-l border-t border-r rounded-t text-[var(--tab-text-color-active)] border-[var(--tab-border-color)]' : 'text-[var(--tab-text-color-inactive)] hover:text-[var(--tab-text-color-active)]'}"
            on:click={() => (activeTab = 'admin_dashboard')}
          >
            Admin Dashboard
          </button>
        </li>
      {/if}
    </ul>
  </nav>

  <main class="mt-6">
    <div class="{activeTab === 'profile' ? 'profile-full-width' : 'dashboard-grid'}">
      {#if activeTab === 'profile'}
        <div class="tab-content p-4"> <!-- Reduced padding -->
          <ProfileSettings {token} {role} />
        </div>
      {:else if activeTab === 'resume_upload'}
        <div class="tab-content p-4"> <!-- Reduced padding -->
          <ResumeUpload {token} bind:extractedSkills bind:resumeId />
        </div>
      {:else if activeTab === 'job_search'}
        <div class="tab-content p-4"> <!-- Reduced padding -->
          <JobPostings {token} {role} {extractedSkills} />
        </div>
      {:else if activeTab === 'my_applications'}
        <div class="tab-content p-4"> <!-- Reduced padding -->
          <MyApplications {token} />
        </div>
      {:else if activeTab === 'favorites'}
        <div class="tab-content p-4"> <!-- Reduced padding -->
          <Favorites {token} />
        </div>
      {:else if activeTab === 'recruiter_jobs' && role === 'ROLE_RECRUITER'}
        <div class="tab-content p-4"> <!-- Reduced padding -->
          <RecruiterJobManagement {token} {role} />
        </div>
      {:else if activeTab === 'admin_dashboard' && role === 'ROLE_ADMIN'}
        <div class="tab-content p-4"> <!-- Reduced padding -->
          <AdminDashboard {token} {role} />
        </div>
      {/if}
    </div>
  </main>
</div>

<!-- Confirm modal instance (reads modalStore) -->
<ConfirmModal />
