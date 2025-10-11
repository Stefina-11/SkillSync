<script lang="ts">
  import { getProfile, updateProfile, type UserProfile, getMyResume } from '$lib/apiService';
  import { onMount } from 'svelte';

  export let token: string;
  export let role: string;

  let profile: UserProfile | null = null;
  let profileError = '';
  let newPassword = '';
  let email = '';
  let fullName = '';
  let phone = '';
  let bio = '';
  let linkedin = '';
  let github = '';
  let avatarDataUrl = '';
  let editing = false;
  let passwordChangeSuccess = false;
  let profileUpdateSuccess = false;

  // Extended profile fields
  let jobTitle = '';
  let careerLevel = '';
  let experience: Array<{ years: number; companies?: string[]; summary?: string }> = [];
  let education: Array<{ degree?: string; college?: string; passingYear?: number }> = [];
  let preferredLocations: string[] = [];
  let preferredLocationsString = '';
  let expectedSalaryRange: { min?: number; max?: number } = {};
  let workType = '';
  let employmentType = '';
  let achievements: Array<{ title: string; description?: string; year?: number }> = [];
  let achievementTitle = '';
  let achievementYear: string = '';

  let latestResume: { id: number; filename: string; content: string; skills: string[]; userId: number } | null = null;

  // State for collapsible sections
  let showJobPreferences = false;
  let showAchievements = false;
  let showExperience = false; // New state for collapsible experience section
  let showEducation = false; // New state for collapsible education section
  let showAvatarModal = false; // New state for avatar modal

  onMount(() => {
    loadProfile();
  });

  function openAvatarModal() {
    if (avatarDataUrl) {
      showAvatarModal = true;
    }
  }

  function closeAvatarModal() {
    showAvatarModal = false;
  }

  async function loadProfile() {
    if (!token) return;
    try {
      console.log('ProfileSettings: using token=', token);
      profile = await getProfile(token);
      console.log('ProfileSettings: Profile loaded:', profile); // Debug log
      email = profile.email || '';
      fullName = profile.fullName || '';
      phone = profile.phone || '';
      bio = profile.bio || '';
      linkedin = profile.linkedin || '';
  github = profile.github || '';
      avatarDataUrl = profile.avatarDataUrl || '';
      jobTitle = profile.jobTitle || '';
      careerLevel = profile.careerLevel || '';
      experience = profile.experience || [];
      education = profile.education || [];
      preferredLocations = profile.preferredLocations || [];
      preferredLocationsString = preferredLocations.join(', ');
      expectedSalaryRange = profile.expectedSalaryRange || {};
      workType = profile.workType || '';
      employmentType = profile.employmentType || '';
      achievements = profile.achievements || [];
      profileError = '';
      // load latest resume if available
      try {
        latestResume = await getMyResume(token);
      } catch (e) {
        latestResume = null;
      }
    } catch (e: any) {
      // If 403 Forbidden, show a friendly message and offer relogin
      if (e?.message && e.message.toLowerCase().includes('forbidden')) {
        profileError = 'Failed to fetch profile: Forbidden. Please relogin.';
      } else {
        profileError = e?.message || 'Failed to load profile';
      }
    }
  }

  async function saveProfile() {
    if (!token) return;
    try {
      // sync preferredLocations from string
      preferredLocations = preferredLocationsString.split(',').map(s => s.trim()).filter(Boolean);
      const profileToSave = { password: newPassword, email: email, phone, bio, linkedin, github, avatarDataUrl, fullName, jobTitle, careerLevel, experience, education, preferredLocations, expectedSalaryRange, workType, employmentType, achievements };
      console.log('ProfileSettings: Saving profile:', profileToSave); // Debug log
      await updateProfile(token, profileToSave);
      newPassword = '';
      passwordChangeSuccess = true;
      profileUpdateSuccess = true;
      profileError = '';
      setTimeout(() => (passwordChangeSuccess = false), 3000); // Hide success message after 3 seconds
      setTimeout(() => (profileUpdateSuccess = false), 3000); // Hide success message after 3 seconds
      await loadProfile();
      editing = false;
    } catch (e: any) {
      profileError = e?.message || 'Failed to update profile';
      passwordChangeSuccess = false;
      profileUpdateSuccess = false;
    }
  }

  function startEdit() {
    editing = true;
  }

  function cancelEdit() {
    editing = false;
    // revert fields to latest profile
    if (profile) {
      email = profile.email || '';
      fullName = profile.fullName || '';
      phone = profile.phone || '';
      bio = profile.bio || '';
      linkedin = profile.linkedin || '';
      avatarDataUrl = profile.avatarDataUrl || '';
      jobTitle = profile.jobTitle || '';
      careerLevel = profile.careerLevel || '';
      experience = profile.experience || [];
      education = profile.education || [];
      preferredLocations = profile.preferredLocations || [];
      preferredLocationsString = preferredLocations.join(', ');
      expectedSalaryRange = profile.expectedSalaryRange || {};
      workType = profile.workType || '';
      employmentType = profile.employmentType || '';
      achievements = profile.achievements || [];
    }
    newPassword = '';
    profileError = '';
  }

  function onAvatarSelected(file: File | null) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => {
      avatarDataUrl = String(ev.target?.result || '');
    };
    reader.readAsDataURL(file);
  }

  function relogin() {
    // Clear token and redirect to login
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    window.location.href = '/';
  }

  // small helper to compute profile completion percentage
  function computeCompletion() {
    let score = 0;
    const checks = [
      fullName,
      email,
      phone,
      jobTitle,
      avatarDataUrl,
      linkedin,
      github,
      bio,
      careerLevel,
      preferredLocations.length ? 'ok' : '',
      workType,
      employmentType,
      ((expectedSalaryRange.min !== undefined && expectedSalaryRange.min !== null) || (expectedSalaryRange.max !== undefined && expectedSalaryRange.max !== null)) ? 'ok' : '', // Check if min or max salary is set
      experience.some(exp => exp.years > 0 || (exp.companies && exp.companies.length > 0) || (exp.summary && exp.summary.trim() !== '')) ? 'ok' : '',
      education.some(edu => (edu.degree && edu.degree.trim() !== '') || (edu.college && edu.college.trim() !== '') || (edu.passingYear !== undefined && edu.passingYear !== null)) ? 'ok' : '',
      achievements.some(a => a.title && a.title.trim() !== '') ? 'ok' : '',
      latestResume ? 'ok' : ''
    ];
    const weight = 100 / checks.length;
    checks.forEach(c => { if (c) score += weight; });
    return score;
  }
</script>

<div class="bg-white p-4 rounded-lg" style="box-shadow: 12px 12px 24px rgba(236,72,153,0.12), -6px -6px 18px rgba(236,72,153,0.06);">
  <h2 class="text-xl font-semibold mb-2 text-pink-600">Your Profile</h2>
  {#if profile}
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-4 gap-y-2 items-start">
      <div class="col-span-1 md:col-span-2 lg:col-span-3 flex justify-between items-center mb-2">
        <div>
          <div class="text-lg text-pink-600 font-semibold">{fullName || profile.username}</div>
          <div class="text-sm text-gray-600">{jobTitle || '—'}</div>
          <div class="mt-0.5 text-sm"><b class="text-gray-700">Role:</b> <span class="text-gray-600">{profile.role.replace('ROLE_', '')}</span></div>
        </div>
        <div class="relative w-20 h-20 cursor-pointer" on:click={openAvatarModal}>
          {#if avatarDataUrl}
            <img src={avatarDataUrl} alt="avatar" class="w-full h-full rounded-full object-cover shadow-lg" />
          {:else}
            <div class="w-full h-full rounded-full bg-gray-200 flex items-center justify-center text-gray-500 text-lg">?</div>
          {/if}
          {#if !editing}
            <button on:click|stopPropagation={startEdit} class="absolute bottom-0 right-0 bg-pink-600 text-white rounded-full p-0.5 shadow-md hover:bg-pink-700">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512" class="h-3 w-3 fill-current"><path d="M402.6 83.2l90.2 90.2c3.8 3.8 3.8 10 0 13.8L274.4 405.6l-92.8 10.3c-12.4 1.4-22.9-9.1-21.5-21.5l10.3-92.8L388.8 83.2c3.8-3.8 10-3.8 13.8 0zm162-22.9l-48.8-48.8c-15.2-15.2-39.9-15.2-55.2 0l-35.4 35.4c-3.8 3.8-3.8 10 0 13.8l90.2 90.2c3.8 3.8 10 3.8 13.8 0l35.4-35.4c15.2-15.3 15.2-40 0-55.2zM384 346.2V448H64V128h229.8c3.2 0 6.2-1.3 8.5-3.5l40-40c7.6-7.6 2.2-20.5-8.5-20.5H48C21.5 64 0 85.5 0 112v352c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V306.2c0-10.7-12.9-16-20.5-8.5l-40 40c-2.2 2.3-3.5 5.3-3.5 8.5z"/></svg>
            </button>
          {:else}
            <button on:click|stopPropagation={cancelEdit} class="absolute bottom-0 right-0 bg-gray-400 text-white rounded-full p-0.5 shadow-md hover:bg-gray-500">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512" class="h-3 w-3 fill-current"><path d="M342.6 150.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192 210.7 86.6 105.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L146.7 256 41.4 361.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192 301.3 297.4 406.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.3 256 342.6 150.6z"/></svg>
            </button>
          {/if}
        </div>
      </div>

      <div class="col-span-1">
        <label for="emailInput" class="block mb-0.5 text-pink-600 text-xs">Email:</label>
        <input id="emailInput" type="email" bind:value={email} class="border px-2 py-2 w-full max-w-lg focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" disabled={!editing} />
      </div>

      <div class="col-span-1">
        <label for="phoneInput" class="block mb-0.5 text-pink-600 text-xs">Phone:</label>
        <input id="phoneInput" type="text" bind:value={phone} class="border px-2 py-2 w-full max-w-lg focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" disabled={!editing} />
      </div>

      <div class="col-span-1">
        <label for="fullNameInput" class="block mb-0.5 text-pink-600 text-xs">Full Name:</label>
        <input id="fullNameInput" type="text" bind:value={fullName} class="border px-2 py-2 w-full max-w-lg focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" disabled={!editing} />
      </div>

      <div class="col-span-1">
        <label for="jobTitleInput" class="block mb-0.5 text-pink-600 text-xs">Current Job Title:</label>
        <input id="jobTitleInput" type="text" bind:value={jobTitle} class="border px-2 py-2 w-full max-w-lg focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" disabled={!editing} />
      </div>

      <div class="col-span-1">
        <label class="block mb-0.5 text-pink-600 text-xs">Career Level:</label>
        <select bind:value={careerLevel} class="border px-2 py-2 w-full max-w-lg focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" on:change={() => editing = true}>
          <option value="">Select...</option>
          <option value="Student">Student</option>
          <option value="Fresher">Fresher</option>
          <option value="Professional">Professional</option>
          <option value="Manager">Manager</option>
        </select>
      </div>

      <div class="col-span-1 md:col-span-2 lg:col-span-3">
        <label for="bioInput" class="block mb-0.5 text-pink-600 text-xs">Bio:</label>
        <textarea id="bioInput" bind:value={bio} class="border px-2 py-1 w-full focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" rows="5" disabled={!editing}></textarea>
      </div>

      <div class="col-span-1">
        <label for="linkedinInput" class="block mb-0.5 text-pink-600 text-xs">LinkedIn:</label>
        <input id="linkedinInput" type="url" bind:value={linkedin} placeholder="https://www.linkedin.com/in/yourname" class="border px-2 py-2 w-full max-w-lg focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" on:input={() => editing = true} disabled={!editing} />
      </div>
      <div class="col-span-1">
        <label for="githubInput" class="block mb-0.5 text-pink-600 text-xs">GitHub:</label>
        <input id="githubInput" type="url" bind:value={github} class="border px-2 py-2 w-full max-w-lg focus:outline-none focus:ring-2 focus:ring-pink-300 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" placeholder="https://github.com/yourusername" disabled={!editing} />
      </div>

      {#if editing}
        <div class="col-span-1">
          <label class="block mb-0.5 text-pink-600 text-xs">Change Avatar:</label>
          <input type="file" accept="image/*" on:change={(e) => onAvatarSelected(e.target.files?.[0] || null)} class="text-xs" />
        </div>

        <div class="col-span-1">
          <label class="block mb-0.5 text-pink-600 text-xs">Change Password:</label>
          <input id="newPasswordInput" type="password" bind:value={newPassword} class="border px-2 py-2 w-full max-w-lg rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" />
        </div>

        <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2">
          <button on:click={saveProfile} class="bg-pink-600 text-white px-3 py-1.5 rounded shadow hover:bg-pink-700 text-sm">Save Profile</button>
          <button on:click={cancelEdit} class="ml-2 bg-gray-100 px-2.5 py-1 rounded text-sm">Cancel</button>
        </div>
      {/if}
      <div class="col-span-1 md:col-span-2 lg:col-span-3">
        {#if profileUpdateSuccess}
          <div class="text-green-500 mt-1 text-xs">Profile updated successfully!</div>
        {/if}
        {#if profileError}
          <div class="text-red-500 mt-1 text-xs">{profileError}</div>
          {#if profileError.toLowerCase().includes('forbidden')}
            <div class="mt-1">
              <button on:click={relogin} class="bg-yellow-500 text-black px-2.5 py-1 rounded text-xs">Relogin</button>
              <button on:click={loadProfile} class="ml-1 bg-gray-200 px-2.5 py-1 rounded text-xs">Retry</button>
            </div>
          {/if}
        {/if}
      </div>

      <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2 border-t pt-2 section-box">
        <button on:click={() => (showJobPreferences = !showJobPreferences)} class="w-full text-left font-semibold text-pink-600 text-base mb-1 flex justify-between items-center">
          Job Preferences
          <svg class="w-3 h-3 transition-transform {showJobPreferences ? 'rotate-90' : ''}" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
        </button>
        {#if showJobPreferences}
          <div class="mb-1">
            <label class="block mb-0.5 text-pink-600 text-xs">Preferred Locations (comma separated)</label>
            <input type="text" bind:value={preferredLocationsString} class="border px-2 py-2 w-full max-w-lg rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" on:input={() => editing = true} />
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
            <div>
              <label class="block mb-0.5 text-pink-600 text-xs">Work Type</label>
              <select bind:value={workType} class="border px-2 py-1 w-full rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" on:change={() => editing = true}>
                <option value="">Select...</option>
                <option value="Remote">Remote</option>
                <option value="Hybrid">Hybrid</option>
                <option value="On-site">On-site</option>
              </select>
            </div>
            <div>
              <label class="block mb-0.5 text-pink-600 text-xs">Employment Type</label>
              <select bind:value={employmentType} class="border px-2 py-1 w-full rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" on:change={() => editing = true}>
                <option value="">Select...</option>
                <option value="Full-time">Full-time</option>
                <option value="Part-time">Part-time</option>
                <option value="Internship">Internship</option>
              </select>
            </div>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 mt-2">
            <div>
              <label class="block mb-0.5 text-pink-600 text-xs">Expected Salary (Min)</label>
              <input type="number" bind:value={expectedSalaryRange.min} class="border px-2 py-1 w-full rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" on:input={() => editing = true} />
            </div>
            <div>
              <label class="block mb-0.5 text-pink-600 text-xs">Expected Salary (Max)</label>
              <input type="number" bind:value={expectedSalaryRange.max} class="border px-2 py-1 w-full rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" on:input={() => editing = true} />
            </div>
          </div>
        {/if}
      </div>

      <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2 border-t pt-2 section-box">
        <button on:click={() => (showExperience = !showExperience)} class="w-full text-left font-semibold text-pink-600 text-base mb-1 flex justify-between items-center">
          Experience
          <svg class="w-3 h-3 transition-transform {showExperience ? 'rotate-90' : ''}" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
        </button>
        {#if showExperience}
          <div class="flex flex-col gap-2 mb-1">
            {#each experience as exp, idx}
              <div class="border p-2 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);">
                <div class="flex justify-between items-center mb-1">
                  <input type="number" placeholder="Years" bind:value={exp.years} class="border px-2 py-1 w-20 rounded text-xs" on:input={() => editing = true} />
                  <button on:click={() => { experience = experience.filter((_, i) => i !== idx); editing = true; }} class="text-red-500 p-1 rounded-full hover:bg-red-100">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 fill-current" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
                <input type="text" placeholder="Companies (comma separated)" bind:value={exp.companies} class="border px-2 py-1 w-full rounded text-xs mb-1" on:input={() => editing = true} />
                <textarea placeholder="Summary" bind:value={exp.summary} class="border px-2 py-1 w-full rounded text-xs" rows="2" on:input={() => editing = true}></textarea>
              </div>
            {/each}
            <button class="bg-gray-200 px-2 py-1 rounded text-xs" on:click={() => { experience = [...experience, { years: 0, companies: [], summary: '' }]; editing = true; }}>Add Experience</button>
          </div>
        {/if}
      </div>

      <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2 border-t pt-2 section-box">
        <button on:click={() => (showEducation = !showEducation)} class="w-full text-left font-semibold text-pink-600 text-base mb-1 flex justify-between items-center">
          Education
          <svg class="w-3 h-3 transition-transform {showEducation ? 'rotate-90' : ''}" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
        </button>
        {#if showEducation}
          <div class="flex flex-col gap-2 mb-1">
            {#each education as edu, idx}
              <div class="border p-2 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);">
                <div class="flex justify-between items-center mb-1">
                  <input type="text" placeholder="Degree" bind:value={edu.degree} class="border px-2 py-1 flex-grow w-full rounded text-xs" on:input={() => editing = true} />
                  <button on:click={() => { education = education.filter((_, i) => i !== idx); editing = true; }} class="text-red-500 p-1 rounded-full hover:bg-red-100">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 fill-current" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
                <input type="text" placeholder="College" bind:value={edu.college} class="border px-2 py-1 w-full rounded text-xs mb-1" on:input={() => editing = true} />
                <input type="number" placeholder="Passing Year" bind:value={edu.passingYear} class="border px-2 py-1 w-full rounded text-xs" on:input={() => editing = true} />
              </div>
            {/each}
            <button class="bg-gray-200 px-2 py-1 rounded text-xs" on:click={() => { education = [...education, { degree: '', college: '', passingYear: undefined }]; editing = true; }}>Add Education</button>
          </div>
        {/if}
      </div>

      <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2 border-t pt-2 section-box">
        <button on:click={() => (showAchievements = !showAchievements)} class="w-full text-left font-semibold text-pink-600 text-base mb-1 flex justify-between items-center">
          Achievements
          <svg class="w-3 h-3 transition-transform {showAchievements ? 'rotate-90' : ''}" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
        </button>
        {#if showAchievements}
          <div class="flex flex-col sm:flex-row gap-2 mb-1">
            <input type="text" placeholder="Title" bind:value={achievementTitle} class="border px-2 py-2 flex-grow w-full max-w-lg rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" />
            <input type="text" placeholder="Year" bind:value={achievementYear} class="border px-2 py-1 w-24 rounded text-xs" style="box-shadow: 0 0 6px rgba(236,72,153,0.25);" />
            <button class="bg-gray-200 px-2 py-1 rounded text-xs" on:click={() => { if (achievementTitle.trim()) { achievements = [...achievements, { title: achievementTitle.trim(), year: Number(achievementYear)||undefined }]; achievementTitle = ''; achievementYear=''; editing = true; } }}>Add</button>
          </div>
          <div class="mt-1">
            {#each achievements as a, idx}
              <div class="flex items-center gap-1 text-xs">
                <div>{a.title} {a.year ? `(${a.year})` : ''}</div>
                <button on:click={() => { achievements = achievements.filter((_, i) => i !== idx); editing = true; }} class="text-red-500 p-1 rounded-full hover:bg-red-100">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 fill-current" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            {/each}
          </div>
        {/if}
      </div>

      <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2">
        <label class="block mb-0.5 text-pink-600 text-xs">Profile Completion</label>
        <div class="w-full bg-gray-200 rounded h-1.5">
          <div class="bg-green-500 h-1.5 rounded" style="width: {Math.min(100, Math.round(computeCompletion()))}%"></div>
        </div>
        <div class="text-xs mt-0.5">{Math.min(100, Math.round(computeCompletion()))}% complete</div>
      </div>

      {#if latestResume}
        <div class="col-span-1 md:col-span-2 lg:col-span-3 mt-2 border-t pt-2 section-box">
          <h3 class="font-semibold text-pink-600 text-base mb-1">Latest Uploaded Resume</h3>
          <div class="text-xs mb-0.5">{latestResume.filename}</div>
          <div>
            <a class="text-blue-600 text-xs hover:underline" href={`data:application/octet-stream;base64,${latestResume.content}`} download={latestResume.filename}>Download</a>
          </div>
        </div>
      {/if}
    </div>
  {:else}
    <p class="text-sm">Loading profile...</p>
    {#if profileError}
      <div class="text-red-500 mt-1 text-xs">{profileError}</div>
      {#if profileError.toLowerCase().includes('forbidden')}
        <div class="mt-1">
          <button on:click={relogin} class="bg-yellow-500 text-black px-2 py-0.5 rounded text-xs">Relogin</button>
          <button on:click={loadProfile} class="ml-1 bg-gray-200 px-2 py-0.5 rounded text-xs">Retry</button>
        </div>
      {/if}
    {/if}
  {/if}
</div>

{#if showAvatarModal}
  <div class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50" on:click={closeAvatarModal}>
    <div class="relative" on:click|stopPropagation>
      <img src={avatarDataUrl} alt="profile avatar" class="max-w-full max-h-screen" />
      <button on:click={closeAvatarModal} class="absolute top-2 right-2 bg-white text-gray-800 rounded-full p-1 hover:bg-gray-200">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  </div>
{/if}

<style>
  .section-box {
    background: #fff;
    box-shadow: 12px 12px 28px rgba(236,72,153,0.06), -6px -6px 18px rgba(236,72,153,0.03);
    padding: 12px;
    border-radius: 8px;
  }
</style>
