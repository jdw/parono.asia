window.onload = function() {
    render_markdown_from_string(document.getElementById("ingress-pre").innerText, "ingress-div");
	render_markdown_from_string(document.getElementById("main-pre").innerText, "main-div");
	render_markdown_from_string(document.getElementById("addendum-pre").innerText, "addendum-div");
}