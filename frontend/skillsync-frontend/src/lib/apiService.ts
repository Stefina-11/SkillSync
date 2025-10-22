const BASE_URL = 'http://localhost:8081'; // Backend runs on 8081 locally

function authHeader(token?: string): Record<string, string> {
  return token ? { 'Authorization': `Bearer ${token}` } : {};
}

export async function applyToJob(token: string, jobId: number) {
  const response = await fetch(`${BASE_URL}/api/applications/apply/${jobId}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to apply to job');
  return response.json();
}

export async function getApplicationsForJob(token: string, jobId: number) {
  const response = await fetch(`${BASE_URL}/api/applications/job/${jobId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch job applications');
  return response.json();
}

export async function getResumeById(token: string, resumeId: number) {
  const response = await fetch(`${BASE_URL}/api/resumes/${resumeId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch resume by ID');
  return response.json();
}

export async function performAtsCheck(token: string, resumeId: number) {
  const response = await fetch(`${BASE_URL}/api/resumes/${resumeId}/ats-check`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to perform ATS check');
  return response.json();
}

export async function rateResume(token: string, resumeId: number, rating: number) {
  const response = await fetch(`${BASE_URL}/api/resumes/${resumeId}/rate`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ rating })
  });
  if (!response.ok) throw new Error('Failed to rate resume');
  return response.json();
}

export async function getMyApplications(token: string) {
  const response = await fetch(`${BASE_URL}/api/applications`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch applications');
  return response.json();
}

export async function deleteApplication(token: string, applicationId: number) {
  const response = await fetch(`${BASE_URL}/api/applications/${applicationId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to delete application');
  return response.json();
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  role: string;
  phone?: string;
  bio?: string;
  linkedin?: string;
  avatarDataUrl?: string; // optional base64 data URL for avatar preview/storage
  github?: string;
  fullName?: string;
  jobTitle?: string;
  careerLevel?: string; // e.g., Student, Fresher, Professional, Manager
  experience?: Array<{ years: number; companies?: string[]; summary?: string }>;
  education?: Array<{ degree?: string; college?: string; passingYear?: number }>;
  preferredLocations?: string[];
  expectedSalaryRange?: { min?: number; max?: number };
  workType?: string; // Remote, Hybrid, On-site
  employmentType?: string; // Full-time, Part-time, Internship
  achievements?: Array<{ title: string; description?: string; year?: number }>;
}

export async function getProfile(token: string): Promise<UserProfile> {
  const response = await fetch(`${BASE_URL}/api/profile`, {
    headers: authHeader(token) as HeadersInit
  });
  if (!response.ok) {
    let errorMessage = 'Failed to fetch profile';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch (e) {
      // If response is not JSON, use status text or a generic message
      errorMessage = `Failed to fetch profile: ${response.statusText || 'Unknown error'}`;
    }
    throw new Error(errorMessage);
  }
  return response.json();
}

export async function updateProfile(token: string, profile: { password?: string; email?: string; phone?: string; bio?: string; linkedin?: string; avatarDataUrl?: string; fullName?: string; jobTitle?: string; careerLevel?: string; experience?: Array<{ years: number; companies?: string[]; summary?: string }>; education?: Array<{ degree?: string; college?: string; passingYear?: number }>; preferredLocations?: string[]; expectedSalaryRange?: { min?: number; max?: number }; workType?: string; employmentType?: string; achievements?: Array<{ title: string; description?: string; year?: number }> }) {
  const response = await fetch(`${BASE_URL}/api/profile`, {
    method: 'PUT',
    headers: ({ ...authHeader(token), 'Content-Type': 'application/json' } as HeadersInit),
    body: JSON.stringify(profile)
  });
  if (!response.ok) {
    let errorMessage = 'Failed to update profile';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch (e) {
      errorMessage = `Failed to update profile: ${response.statusText || 'Unknown error'}`;
    }
    throw new Error(errorMessage);
  }
  return response.json();
}

export async function registerUser(username: string, password: string, role: string = 'ROLE_USER') {
  // Temporarily add a placeholder email as the backend requires it.
  // In a real application, an email input field would be added to the registration form.
  const email = `${username}@example.com`; 
  const response = await fetch(`${BASE_URL}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password, role, email })
  });
  if (!response.ok) throw new Error('Registration failed');
  return response.json();
}

export async function loginUser(username: string, password: string) {
  const response = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  if (!response.ok) throw new Error('Login failed');
  return response.json();
}

export async function uploadResume(file: File): Promise<{ id: number; skills: string[] }> {
  const formData = new FormData();
  formData.append('file', file);

  const token = localStorage.getItem('token');
  const headers: Record<string, string> = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const response = await fetch(`${BASE_URL}/api/resumes/upload`, {
    method: 'POST',
    headers,
    body: formData,
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return await response.json();
}

export async function getMyResume(token: string): Promise<{ id: number; filename: string; content: string; skills: string[]; userId: number }> {
  const response = await fetch(`${BASE_URL}/api/resumes/my`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch resume');
  return response.json();
}

export async function fetchJobPostings(filters?: { keyword?: string; location?: string; jobType?: string; minSalary?: number }): Promise<any[]> {
  const params = new URLSearchParams();
  if (filters) {
    if (filters.keyword) params.append('keyword', filters.keyword);
    if (filters.location) params.append('location', filters.location);
    if (filters.jobType) params.append('jobType', filters.jobType);
    if (filters.minSalary != null) params.append('minSalary', String(filters.minSalary));
  }
  const url = `${BASE_URL}/api/jobs${params.toString() ? `?${params.toString()}` : ''}`;

  const token = localStorage.getItem('token');
  const headers: Record<string, string> = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, { headers });
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return await response.json();
}

export async function fetchMockJobPostings(): Promise<any[]> {
  const response = await fetch(`${BASE_URL}/api/jobs/fetch`);
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return await response.json();
}

export async function matchSkills(jobId: number, resumeId: number): Promise<any> {
  const response = await fetch(`${BASE_URL}/api/jobs/${jobId}/match/${resumeId}`);
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return await response.json();
}

export async function toggleFavorite(token: string, jobId: number) {
  const response = await fetch(`${BASE_URL}/api/favorites/toggle/${jobId}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to toggle favorite');
  return response.json();
}

export async function getFavorites(token: string) {
  const response = await fetch(`${BASE_URL}/api/favorites`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch favorites');
  return response.json();
}

// Admin APIs
export async function adminListUsers(token: string) {
  const response = await fetch(`${BASE_URL}/api/admin/users`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to list users');
  return response.json();
}

export async function adminUpdateUserRole(token: string, userId: number, role: string) {
  const response = await fetch(`${BASE_URL}/api/admin/users/${userId}/role`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ role })
  });
  if (!response.ok) throw new Error('Failed to update user role');
  return response.json();
}

export async function adminDeleteUser(token: string, userId: number) {
  const response = await fetch(`${BASE_URL}/api/admin/users/${userId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to delete user');
  return response.json();
}

export async function adminListJobs(token: string) {
  const response = await fetch(`${BASE_URL}/api/admin/jobs`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to list jobs');
  return response.json();
}

export async function adminDeleteJob(token: string, jobId: number) {
  const response = await fetch(`${BASE_URL}/api/admin/jobs/${jobId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to delete job');
  return response.json();
}

// Recruiter job CRUD
export async function createJob(token: string, job: any) {
  const response = await fetch(`${BASE_URL}/api/jobs`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify(job)
  });
  if (!response.ok) throw new Error('Failed to create job');
  return response.json();
}

export async function updateJob(token: string, jobId: number, job: any) {
  const response = await fetch(`${BASE_URL}/api/jobs/${jobId}`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify(job)
  });
  if (!response.ok) throw new Error('Failed to update job');
  return response.json();
}

export async function deleteJob(token: string, jobId: number) {
  const response = await fetch(`${BASE_URL}/api/jobs/${jobId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to delete job');
  return response.json();
}

// Admin pagination helpers
export async function adminListUsersPaged(token: string, page = 0, size = 10) {
  const response = await fetch(`${BASE_URL}/api/admin/users?page=${page}&size=${size}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to list users');
  return response.json();
}

export async function adminListJobsPaged(token: string, page = 0, size = 10) {
  const response = await fetch(`${BASE_URL}/api/admin/jobs?page=${page}&size=${size}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to list jobs');
  return response.json();
}
