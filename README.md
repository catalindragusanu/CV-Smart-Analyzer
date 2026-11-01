# CV Smart Analyzer

A Java desktop app that parses resumes and ranks them against a job description. It extracts text from PDF, Word and plain-text files, runs NLP over it, and scores every CV by how well it matches the role.

## Features

- Reads CVs and job descriptions in **PDF, DOCX, DOC and TXT** format (10 MB limit per file)
- Extracts contact details (name, email, phone) and the skills, education and experience sections
- Scores each CV against the job: **60% keyword match + 40% cosine similarity**, capped at 100%
- Ranks all CVs in a folder so the best matches come first
- Swing GUI (FlatLaf theme) plus a console front-end sharing the same analysis pipeline

## Tech stack

| Area | Library |
| --- | --- |
| UI | Swing, [FlatLaf](https://www.formdev.com/flatlaf/) |
| NLP | Stanford CoreNLP (lemmatization, named entities), Lucene |
| Documents | Apache POI (Word), Apache PDFBox (PDF) |
| Build | Maven, Java 25 |

## Getting started (Windows)

The repo can download its own JDK 25 and Maven into `build-tools/`, so nothing needs installing globally.

```powershell
# 1. One-time: download the JDK and Maven into build-tools/
.\setup-build-tools.ps1

# 2. Build (first run downloads dependencies and CoreNLP models, so it takes a few minutes)
.\build.bat

# 3. Run the GUI
.\run.bat
```

If you already have JDK 25 and Maven, `mvn clean package` produces `target/CVSmartAnalyzer-1.0-SNAPSHOT-jar-with-dependencies.jar`, which you can start with `java -jar`.

## Usage

1. Provide a job description, either from a file or typed in manually.
2. Choose the folder that contains the CVs.
3. The app shows the CVs ranked by match score, along with each candidate's extracted details.

## Sample data

Try the app straight away with the bundled files:

- `test-cvs/`: 20 sample CVs (`.txt`)
- `test-job-descriptions/`: 13 sample roles (backend, data science, DevOps, security, product and more)

## Project layout

```text
src/main/java/
  Launcher.java            entry point
  gui/                     Swing interface
  cvsmartanalyzer/         console front-end
  analysis/                shared pipeline: parsing and scoring
  filehandler/             file validation and text extraction
  nlp/                     preprocessing, keywords, similarity, entities
  models/                  CV and JobDescription
src/main/resources/        stopword list
```

## Continuous integration

GitHub Actions (`.github/workflows/`) builds with JDK 25 and runs tests, code quality checks, security scans and release packaging.

## License

[MIT](LICENSE)

