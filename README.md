# SkillSync – Resume & Skill Matcher

SkillSync is a full-stack web application that analyzes resumes and job descriptions to show match percentages and skill gaps.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.x, Maven
- **Frontend:** SvelteKit, TailwindCSS
- **Database:** SQLite
- **Libraries:**
  - Apache POI (DOCX/PDF parsing)
  - Stanford CoreNLP (NLP skill extraction)
  - Jsoup (Web scraping job posts)
  - Jackson (JSON handling)

## Setup Instructions

### Backend

1.  Navigate to the `backend/skillsync-backend` directory.
2.  Install dependencies: `mvn install`
3.  Run the application: `mvn spring-boot:run`
4.  The backend will be running on `http://localhost:8080`.

### Frontend

1.  Navigate to the `frontend/skillsync-frontend` directory.
2.  Install dependencies: `npm install`
3.  Run the application: `npm run dev`
4.  The frontend will be running on `http://localhost:5173`.

## Example Data

### Resumes

-   `example-resumes/resume1.docx`
-   `example-resumes/resume2.docx`

### Job Postings

The backend uses mock job postings for now. You can fetch them by clicking the "Fetch Jobs" button on the frontend.
