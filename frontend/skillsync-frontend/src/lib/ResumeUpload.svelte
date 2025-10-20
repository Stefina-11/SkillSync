<script lang="ts">
  import { uploadResume, getMyResume } from '$lib/apiService';
  import { onMount } from 'svelte';

  export let token: string;
  let resumeFile: File | null = null;
  export let extractedSkills: string[] = [];
  export let resumeId: number | null = null; // Export resumeId
  let uploadSuccess: boolean = false;
  let uploadedResumeName: string | null = null; // To display the name of the *saved* resume
  let selectedFileName: string | null = null; // To display the name of the *currently selected* file in the input

  onMount(async () => {
    if (!token) {
      return;
    }
    try {
      const existingResume = await getMyResume(token);
      resumeId = existingResume.id;
      extractedSkills = existingResume.skills || [];
      // If an existing resume is found, display its name
      if (existingResume.filename) { // Corrected from fileName to filename
        uploadedResumeName = existingResume.filename;
      }
    } catch (error) {
      console.log('No existing resume found or failed to fetch:', error);
    }
  });

  async function handleSave() {
    if (!token) return;
    if (!resumeFile) return;
    try {
      const data = await uploadResume(resumeFile);
      resumeId = data.id;
      extractedSkills = data.skills || [];
      uploadedResumeName = resumeFile.name; // Store the name of the newly uploaded file
      selectedFileName = null; // Clear selected file name after saving
      uploadSuccess = true;
      setTimeout(() => {
        uploadSuccess = false;
      }, 3000); // Message disappears after 3 seconds
    } catch (error) {
      console.error('Error uploading resume:', error);
    }
  }

  function handlePreview() {
    // Prioritize the newly selected file for preview, otherwise use the uploaded one if available
    const fileToPreview = resumeFile;
    const nameToDisplay = selectedFileName || uploadedResumeName;

    if (!fileToPreview && !uploadedResumeName) {
      alert('Please select or upload a resume file first.');
      return;
    }

    if (fileToPreview && fileToPreview.type === 'application/pdf') {
      const fileURL = URL.createObjectURL(fileToPreview);
      window.open(fileURL, '_blank');
    } else if (fileToPreview && fileToPreview.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') {
      alert('Direct preview for DOCX files is not supported. Please download to view.');
    } else if (uploadedResumeName && !fileToPreview) {
      // If no new file is selected but an old one exists, we can't preview it directly without fetching it.
      alert('To preview the previously uploaded resume, you might need to re-upload it or implement a download feature.');
    } else {
      alert('Unsupported file type for preview.');
    }
  }
</script>

<div class="bg-white shadow-pink p-4 rounded">
  <h2 class="text-2xl font-semibold mb-2">Upload Resume</h2>
  {#if uploadSuccess}
    <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
      <strong class="font-bold">Success!</strong>
      <span class="block sm:inline">Your resume has been uploaded.</span>
    </div>
  {/if}
  <div class="mb-3">
    {#if uploadedResumeName}
      <p class="text-gray-700 dark:text-gray-300 mb-2">Currently uploaded resume: <span class="font-semibold">{uploadedResumeName}</span></p>
    {/if}
    <input type="file" accept=".docx, .pdf" on:change={(e) => {
      const target = e.target as HTMLInputElement;
      if (target && target.files && target.files.length > 0) {
        resumeFile = target.files[0];
        selectedFileName = target.files[0].name; // Update selected file name
      } else {
        resumeFile = null;
        selectedFileName = null; // Clear selected file name if no file is chosen
      }
    }} />
    {#if selectedFileName}
      <p class="text-gray-600 mt-1">Selected: <span class="font-medium">{selectedFileName}</span></p>
    {/if}
  </div>
  <div class="mt-3 flex space-x-2">
    <button on:click={handlePreview} disabled={!resumeFile && !uploadedResumeName} class="bg-gray-500 text-white px-4 py-2 rounded disabled:opacity-50">Preview</button>
    <button on:click={handleSave} disabled={!resumeFile} class="bg-pink-500 text-white px-4 py-2 rounded disabled:opacity-50">Save</button>
  </div>

</div>
