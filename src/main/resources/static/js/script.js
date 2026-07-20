verifyUser();
const token = localStorage.getItem("token");

const isAdmin = localStorage.getItem("isAdmin");

if (!isAdmin) {
	alert("Access denied");
	window.location.href = "/waste.html";
}
const form = document.getElementById("uploadForm");
const fileInput = document.getElementById("fileInput");
const messageBox = document.getElementById("messageBox");
const submitBtn = document.getElementById("submitBtn");

const colForm = document.getElementById("collectorForm");
const colFileInput = document.getElementById("colFileInput");
const colMessageBox = document.getElementById("colMessageBox");
const colSubmitBtn = document.getElementById("colSubmitBtn");

form.addEventListener("submit", (e)=>
uploadFile(e,fileInput,messageBox,submitBtn,"/admin/upload")
);

colForm.addEventListener("submit",(e)=>
uploadFile(e,colFileInput,colMessageBox,colSubmitBtn,"/admin/colUpload")
);
async function uploadFile(e,fileInput,messageBox,submitBtn,URL) {
	e.preventDefault();

	const file = fileInput.files[0];

	if (!file) {
		showMessage(messageBox,"Please select a file", "danger");
		return;
	}

	const formData = new FormData();
	formData.append("file", file);

	submitBtn.disabled = true;
	submitBtn.innerText = "Uploading...";

	try {
		const response = await fetch(URL, {
			method: "POST",
			headers: {
				"Authorization": "Bearer " + token
			},
			body: formData
		});
		
		alertAndRelocate(response);

		const data = await response.json();

		if (data.status === "SUCCESS") {
			showMessage(messageBox,"File processed successfully", "success");
		}
		else if (data.status === "COMPLETED_WITH_ERRORS") {
			showMessage(messageBox,"Completed with errors. Downloading error file...", "warning");

			const btn = document.createElement("button");
			btn.onclick = () => autoDownload(data.downloadUrl);
			btn.className = "btn btn-info mt-3";
			btn.innerHTML = "⬇️ Download Error File";

			messageBox.appendChild(btn);
		}
		else if (data.status === "FAILED") {
			showMessage(messageBox,data.error || "Job failed", "danger");
		}

	} catch (error) {
		showMessage("Something went wrong: " + error.message, "danger");
	} finally {
		submitBtn.disabled = false;
		submitBtn.innerText = "Upload";
	}
}

async function autoDownload(url) {
	const token = localStorage.getItem("token");

	try {
		const response = await fetch(url, {
			method: "GET",
			headers: {
				"Authorization": "Bearer " + token
			}
		});

		alertAndRelocate(response);

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

function showMessage(box,msg, type) {
	box.innerHTML = `
    <div class="alert alert-${type}">
      ${msg}
    </div>
  `;
}