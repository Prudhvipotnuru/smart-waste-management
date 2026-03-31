const token = localStorage.getItem("token");

if (!token) {
	window.location.href = "/home.html";
}
// decode payload
const payload = JSON.parse(atob(token.split('.')[1]));

const roles = payload.roles;

const isAdmin = roles.some(r => r.authority === "ROLE_ADMIN");

if (!isAdmin) {
	alert("Access denied");
	window.location.href = "/waste.html";
}
const form = document.getElementById("uploadForm");
const fileInput = document.getElementById("fileInput");
const messageBox = document.getElementById("messageBox");
const submitBtn = document.getElementById("submitBtn");

form.addEventListener("submit", async function(e) {
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
		const response = await fetch("/admin/upload", {
			method: "POST",
			headers: {
				"Authorization": "Bearer " + token
			},
			body: formData
		});

		const data = await response.json();

		if (data.status === "SUCCESS") {
			showMessage("File processed successfully", "success");
		}
		else if (data.status === "COMPLETED_WITH_ERRORS") {
			showMessage("Completed with errors. Downloading error file...", "warning");

			const btn = document.createElement("button");
			btn.onclick = () => autoDownload(data.downloadUrl);
			btn.className = "btn btn-info mt-3";
			btn.innerHTML = "⬇️ Download Error File";

			messageBox.appendChild(btn);
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

async function autoDownload(url) {
	const token = localStorage.getItem("token");

	try {
		const response = await fetch(url, {
			method: "GET",
			headers: {
				"Authorization": "Bearer " + token
			}
		});

		if (!response.ok) {
			throw new Error("Download failed");
		}

		const blob = await response.blob();

		const link = document.createElement("a");
		link.href = window.URL.createObjectURL(blob);
		link.download = "error_records.csv";

		document.body.appendChild(link);
		link.click();
		document.body.removeChild(link);

	} catch (err) {
		alert("Error downloading file: " + err.message);
	}
}

function showMessage(msg, type) {
	messageBox.innerHTML = `
    <div class="alert alert-${type}">
      ${msg}
    </div>
  `;
}