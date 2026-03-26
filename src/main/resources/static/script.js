const form = document.getElementById("uploadForm");
const fileInput = document.getElementById("fileInput");
const messageBox = document.getElementById("messageBox");
const submitBtn = document.getElementById("submitBtn");

form.addEventListener("submit", async function (e) {
  e.preventDefault();

  const file = fileInput.files[0];

  if (!file) {
    showMessage("Please select a file", "danger");
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  submitBtn.disabled = true;
  submitBtn.innerText = "Uploading...";

  try {
    const response = await fetch("/upload", {
      method: "POST",
      body: formData
    });

    const data = await response.json();

    if (data.status === "SUCCESS") {
      showMessage("File processed successfully", "success");
    } 
    else if (data.status === "COMPLETED_WITH_ERRORS") {
      showMessage("Completed with errors. Downloading error file...", "warning");

      // 🔥 AUTO DOWNLOAD
      autoDownload(data.downloadUrl);
    } 
    else if (data.status === "FAILED") {
      showMessage(data.error || "Job failed", "danger");
    }

  } catch (error) {
    showMessage("Something went wrong: " + error.message, "danger");
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerText = "Upload";
  }
});

function autoDownload(url) {
  const link = document.createElement("a");
  link.href = url;
  link.download = "error_records.csv"; // optional
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function showMessage(msg, type) {
  messageBox.innerHTML = `
    <div class="alert alert-${type}">
      ${msg}
    </div>
  `;
}