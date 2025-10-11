<script lang="ts">
    import { loginUser, registerUser } from '$lib/apiService';
    import { goto } from '$app/navigation';


    let username = '';
    let password = '';
    let isLogin = true;
    let role = 'ROLE_USER';
    let authError = '';

    async function handleAuth() {
      authError = '';
      try {
        if (isLogin) {
          const data = await loginUser(username, password);
          localStorage.setItem('token', data.token);
          localStorage.setItem('role', data.role || 'ROLE_USER');
          goto('/dashboard');
        } else {
          await registerUser(username, password, role);
          isLogin = true;
        }
      } catch (e: any) {
        authError = e?.message || 'Auth failed';
      }
    }

    // If already logged in, redirect to dashboard
    if (typeof localStorage !== 'undefined') {
      const t = localStorage.getItem('token');
      if (t) goto('/dashboard');
    }
  </script>

  <div class="h-screen w-screen overflow-hidden flex">
    <div class="w-full h-full bg-white flex flex-col md:flex-row main-content-box">
      <!-- Left visual / branding -->
      <div class="hidden md:flex items-start justify-center bg-pink-600 text-white p-8 flex-[7]">
        <div class="text-center">
          <div class="logo-container w-24 h-24 mx-auto mb-4 flex items-center justify-center text-white text-5xl font-serif">Ss</div>
          <h2 class="text-2xl font-bold">SkillSync</h2>
          <p class="mt-2 text-pink-100">AI-powered resume matching for faster hiring.</p>
        </div>
      </div>

      <!-- Right: form -->
      <div class="p-8 flex-[3] flex items-center justify-center">
        <div class="max-w-md w-full">
          <div class="flex items-center gap-3 mb-6">
            <div class="logo-container w-10 h-10 flex items-center justify-center text-white text-2xl font-serif">Ss</div>
            <div>
              <h1 class="text-2xl font-extrabold">Welcome back</h1>
              <p class="text-sm text-gray-500">Sign in to continue to SkillSync</p>
            </div>
          </div>

          <form on:submit|preventDefault={handleAuth} class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Username</label>
              <input type="text" bind:value={username} required class="w-full rounded px-3 py-2 focus:outline-none input-field" />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <input type="password" bind:value={password} required class="w-full rounded px-3 py-2 focus:outline-none input-field" />
            </div>

            {#if !isLogin}
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Register as</label>
                <select bind:value={role} class="w-full border rounded px-3 py-2">
                  <option value="ROLE_USER">Job Seeker</option>
                  <option value="ROLE_RECRUITER">Recruiter</option>
                  <option value="ROLE_ADMIN">Admin</option>
                </select>
              </div>
            {/if}

            <div class="flex items-center justify-between">
              <button type="submit" class="bg-pink-600 text-white px-4 py-2 rounded font-medium">{isLogin ? 'Login' : 'Register'}</button>
              <button type="button" on:click={() => { isLogin = !isLogin; authError = ''; }} class="text-sm text-pink-600 underline">{isLogin ? 'Create account' : 'Back to login'}</button>
            </div>

            {#if authError}
              <div class="text-sm text-red-600">{authError}</div>
            {/if}
          </form>

          <div class="mt-6 text-center text-xs text-gray-400">Turn your ambition into action</div>
        </div>
      </div>
    </div>
  </div>

<style>
  .main-content-box {
    box-shadow: 0 10px 20px rgba(219, 39, 119, 0.3), 0 6px 6px rgba(219, 39, 119, 0.2);
  }

  .logo-container {
    background-color: #db2777; /* Pink-600 */
    border: 2px solid #db2777; /* Pink border */
    box-shadow: 0 4px 8px rgba(219, 39, 119, 0.4); /* Pink shadow */
    border-radius: 0.5rem; /* Rounded corners */
  }

  .input-field {
    @apply focus:outline-none focus:ring-2 focus:ring-pink-600 focus:border-transparent;
    box-shadow: 0 2px 4px rgba(219, 39, 119, 0.1); /* Pink shadow */
  }

  .input-field:focus {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(219, 39, 119, 0.4);
  }

  .auth-button {
    @apply w-full bg-pink-600 text-white px-4 py-3 rounded-lg font-semibold shadow-lg transition-all duration-300;
    @apply hover:bg-pink-700; /* Slightly darker pink for hover */
    box-shadow: 0 4px 10px rgba(219, 39, 119, 0.3);
  }

  .auth-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(219, 39, 119, 0.4);
  }

  .select-field option:checked {
    background-color: #db2777; /* Pink-600 */
    color: white;
  }

  .select-field option:hover {
    background-color: #f472b6; /* Pink-400 */
  }
</style>
