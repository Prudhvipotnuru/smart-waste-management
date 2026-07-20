function logout(){
	localStorage.clear();
	window.location.href = "/";
}

async function alertAndRelocate(res){
	if(!res.ok){
        	const error = await res.json();
        	alert(error.message);
        	window.location.href = "/home.html";
    }
}

function verifyUser(){
	const token = localStorage.getItem("token");

	if (!token) {
		window.location.href = "/home.html";
	}
	// decode payload
	const payload = JSON.parse(atob(token.split('.')[1]));

	const roles = payload.roles;

	const isAdmin = roles.some(r => r.authority === "ROLE_ADMIN");
	
	localStorage.setItem("isAdmin",isAdmin);
	
}