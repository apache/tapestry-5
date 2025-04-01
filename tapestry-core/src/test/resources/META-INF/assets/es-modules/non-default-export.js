function setMessage() {
	document.getElementById("non-default-export-message").innerHTML = 
		"Non-default exported function!";
}

export { setMessage };