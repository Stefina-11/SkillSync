<script lang="ts">
  import { adminListUsersPaged, adminListJobsPaged, adminUpdateUserRole, adminDeleteUser, adminDeleteJob } from '$lib/apiService';
  import modalStore from '$lib/modalStore';
  import { onMount } from 'svelte';

  export let token: string;
  export let role: string; // Should be 'ROLE_ADMIN'

  let adminUsers: any[] = [];
  let adminJobs: any[] = [];
  let adminError = '';

  // Admin pagination state
  let adminUsersPage = 0;
  let adminJobsPage = 0;
  let adminPageSize = 5;
  let adminUsersTotalPages = 0;
  let adminJobsTotalPages = 0;

  onMount(() => {
    if (role === 'ROLE_ADMIN') {
      loadAdminData();
    }
  });

  async function loadAdminData() {
    if (!token || role !== 'ROLE_ADMIN') return;
    try {
      const usersPage = await adminListUsersPaged(token, adminUsersPage, adminPageSize);
      adminUsers = usersPage.content || [];
      adminUsersTotalPages = usersPage.totalPages || 0;
      const jobsPage = await adminListJobsPaged(token, adminJobsPage, adminPageSize);
      adminJobs = jobsPage.content || [];
      adminJobsTotalPages = jobsPage.totalPages || 0;
      adminError = '';
    } catch (e: any) {
      adminError = e?.message || 'Failed to load admin data';
    }
  }

  async function changeUserRole(userId: number, newRole: string) {
    if (!token) return;
    try {
      await adminUpdateUserRole(token, userId, newRole);
      await loadAdminData();
    } catch (e) {
      console.error('Failed to update role', e);
    }
  }

  async function removeUser(userId: number) {
    if (!token) return;
    try {
      const ok = await modalStore.confirm(`Delete user ${userId}? This action cannot be undone.`, 'Delete user');
      if (!ok) return;
      await adminDeleteUser(token, userId);
      await loadAdminData();
    } catch (e) {
      console.error('Failed to delete user', e);
    }
  }

  async function removeJob(jobId: number) {
    if (!token) return;
    try {
      const ok = await modalStore.confirm(`Delete job ${jobId}? This action cannot be undone.`, 'Delete job');
      if (!ok) return;
      await adminDeleteJob(token, jobId);
      await loadAdminData();
    } catch (e) {
      console.error('Failed to delete job', e);
    }
  }

  function adminUsersNext() { if (adminUsersPage + 1 < adminUsersTotalPages) { adminUsersPage += 1; loadAdminData(); } }
  function adminUsersPrev() { if (adminUsersPage > 0) { adminUsersPage -= 1; loadAdminData(); } }
  function adminJobsNext() { if (adminJobsPage + 1 < adminJobsTotalPages) { adminJobsPage += 1; loadAdminData(); } }
  function adminJobsPrev() { if (adminJobsPage > 0) { adminJobsPage -= 1; loadAdminData(); } }
</script>

<div class="bg-white shadow p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">Admin Dashboard</h2>
  {#if adminError}
    <div class="text-red-500">{adminError}</div>
  {/if}
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <div>
      <h3 class="font-semibold mb-2">Users</h3>
      <button on:click={loadAdminData} class="bg-green-500 text-white px-3 py-1 rounded mb-2">Refresh</button>
      <div class="flex items-center gap-2 mb-2">
        <button on:click={adminUsersPrev} class="px-2 py-1 bg-gray-200 rounded">Prev</button>
        <div>Page {adminUsersPage + 1} of {adminUsersTotalPages || 1}</div>
        <button on:click={adminUsersNext} class="px-2 py-1 bg-gray-200 rounded">Next</button>
      </div>
      <ul>
        {#each adminUsers as u}
          <li class="mb-2">
            {u.username} — {u.role}
            <select bind:value={u.role} on:change={() => changeUserRole(u.id, u.role)} class="ml-2 border px-1 py-0.5">
              <option value="ROLE_USER">User</option>
              <option value="ROLE_RECRUITER">Recruiter</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
            <button on:click={() => removeUser(u.id)} class="ml-2 text-sm text-red-600">Delete</button>
          </li>
        {/each}
      </ul>
    </div>
    <div>
      <h3 class="font-semibold mb-2">Jobs</h3>
      <div class="flex items-center gap-2 mb-2">
        <button on:click={adminJobsPrev} class="px-2 py-1 bg-gray-200 rounded">Prev</button>
        <div>Page {adminJobsPage + 1} of {adminJobsTotalPages || 1}</div>
        <button on:click={adminJobsNext} class="px-2 py-1 bg-gray-200 rounded">Next</button>
      </div>
      <ul>
        {#each adminJobs as j}
          <li class="mb-2">{j.title} — {j.company} <button on:click={() => removeJob(j.id)} class="ml-2 text-sm text-red-600">Delete</button></li>
        {/each}
      </ul>
    </div>
  </div>
</div>
