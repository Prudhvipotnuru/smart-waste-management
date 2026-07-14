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